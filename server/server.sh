#!/bin/bash  

START_STOP_DAEMON=1

usage()
{
    echo "Usage: $0 {start|stop|restart} [ CONFIGS ... ] "
    exit 1
}

[ $# -gt 0 ] || usage

##################################################
# Some utility functions
##################################################

findDirectory()
{
    OP=$1
    shift
    for L in $* ; do
        [ $OP $L ] || continue 
        echo $L
        break
    done 
}

running()
{
    [ -f $1 ] || return 1
    PID=$(cat $1)
    ps -p $PID >/dev/null 2>/dev/null || return 1
    return 0
}

##################################################
# Get the action & configs
##################################################

ACTION=$1
shift
ARGS="$*"
CONFIGS=""
NO_START=0


if [ -z "$TMP" ] 
then
  TMP=/tmp
fi

TMPJ=$TMP/j$$

##################################################
# Try to determine SERVER_HOME if not set
##################################################

if [ -z "$SERVER_HOME" ] 
then
  SERVER_HOME_1=`dirname "$0"`
  SERVER_HOME_1=`dirname "$SERVER_HOME_1"`
  SERVER_HOME=${SERVER_HOME_1} 
fi

cd $SERVER_HOME
SERVER_HOME=`pwd`

#####################################################
# Find a location for the pid file
#####################################################

if [ -z "$SERVER_RUN" ] 
then
  SERVER_RUN=`findDirectory -w /var/run /usr/var/run /tmp`
fi

#####################################################
# Find a PID for the pid file
#####################################################

if [  -z "$SERVER_PID" ] 
then
  SERVER_PID="$SERVER_RUN/msgs.pid"
fi

##################################################
# Check for JAVA_HOME
##################################################

if [ -z "$JAVA_HOME" ]
then
    # If a java runtime is not defined, search the following
    # directories for a JVM and sort by version. Use the highest
    # version number.

    # Java search path
    JAVA_LOCATIONS="\
        /usr/java \
        /usr/bin \
        /usr/local/bin \
        /usr/local/java \
        /usr/local/jdk \
        /usr/local/jre \
	/usr/lib/jvm \
        /opt/java \
        /opt/jdk \
        /opt/jre \
    " 
    JAVA_NAMES="java jdk jre"
    for N in $JAVA_NAMES ; do
        for L in $JAVA_LOCATIONS ; do
            [ -d $L ] || continue 
            find $L -name "$N" ! -type d | grep -v threads | while read J ; do
                [ -x $J ] || continue
                VERSION=`eval $J -version 2>&1`       
                [ $? = 0 ] || continue
                VERSION=`expr "$VERSION" : '.*"\(1.[0-9\.]*\)["_]'`
                [ "$VERSION" = "" ] && continue
                expr $VERSION \< 1.2 >/dev/null && continue
                echo $VERSION:$J
            done
        done
    done | sort | tail -1 > $TMPJ
    JAVA=`cat $TMPJ | cut -d: -f2`
    JVERSION=`cat $TMPJ | cut -d: -f1`

    JAVA_HOME=`dirname $JAVA`
    while [ ! -z "$JAVA_HOME" -a "$JAVA_HOME" != "/" -a ! -f "$JAVA_HOME/lib/tools.jar" ] ; do
        JAVA_HOME=`dirname $JAVA_HOME`
    done
    [ "$JAVA_HOME" = "" ] && JAVA_HOME=

    echo "Found JAVA=$JAVA in JAVA_HOME=$JAVA_HOME"
fi

##################################################
# Determine which JVM of version >1.2
# Try to use JAVA_HOME
##################################################
if [ "$JAVA" = "" -a "$JAVA_HOME" != "" ]
then
  if [ ! -z "$JAVACMD" ] 
  then
     JAVA="$JAVACMD" 
  else
    [ -x $JAVA_HOME/bin/jre -a ! -d $JAVA_HOME/bin/jre ] && JAVA=$JAVA_HOME/bin/jre
    [ -x $JAVA_HOME/bin/java -a ! -d $JAVA_HOME/bin/java ] && JAVA=$JAVA_HOME/bin/java
  fi
fi

if [ "$JAVA" = "" ]
then
    echo "Cannot find a JRE or JDK. Please set JAVA_HOME to a >=1.2 JRE" 2>&2
    exit 1
fi

JAVA_VERSION=`expr "$($JAVA -version 2>&1 | head -1)" : '.*1\.\([0-9]\)'`
JAVA_OPTIONS=""

#####################################################
# This is how the server will be started
#####################################################

JAR_PATH = ""
for N in `echo "$SERVER_HOME/lib/**"` ; do
  JAR_PATH="$JAR_PATH:$N/*"
done

SERVER_ARGS=""
SERVER_START="-Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y,suspend=n com.qorporation.msgs.server.Msgs"
RUN_ARGS="-classpath $SERVER_HOME/bin$JAR_PATH:$SERVER_HOME/data/resources $JAVA_OPTIONS $SERVER_START $SERVER_ARGS $CONFIGS"
RUN_CMD="$JAVA $RUN_ARGS"

#####################################################
# Comment these out after you're happy with what 
# the script is doing.
#####################################################

echo "SERVER_HOME       = $SERVER_HOME"
echo "SERVER_RUN        = $SERVER_RUN"
echo "SERVER_PID        = $SERVER_PID"
echo "JAVA_OPTIONS      = $JAVA_OPTIONS"
echo "JAVA              = $JAVA"
echo "RUN_CMD           = $RUN_CMD"

##################################################
# Do the action
##################################################
case "$ACTION" in
  start)
        echo -n "Starting Server: "

	if [ "$START_STOP_DAEMON" = "1" ] && type start-stop-daemon > /dev/null 2>&1
	then
          [ x$SERVER_USER = x ] && SERVER_USER=$(whoami)
	  [ $UID = 0 ] && CH_USER="-c $SERVER_USER"
	  if start-stop-daemon -S -p$SERVER_PID $CH_USER -d $SERVER_HOME -b -m -a $JAVA -- $RUN_ARGS 
	  then
	      sleep 1
	      if running $SERVER_PID
	      then
                  echo OK
              else
                  echo FAILED
              fi
	  fi

	else

          if [ -f $SERVER_PID ]
          then
            if running $SERVER_PID
            then
              echo "Already Running!!"
              exit 1
            else
              # dead pid file - remove
              rm -f $SERVER_PID
            fi
          fi

          if [ x$SERVER_USER != x ] 
          then
              touch $SERVER_PID
              chown $SERVER_USER $SERVER_PID
              su - $SERVER_USER -c "
                $RUN_CMD &
                PID=\$!
                disown \$PID
                echo \$PID > $SERVER_PID"
          else
              $RUN_CMD &
              PID=$!
              disown $PID
              echo $PID > $SERVER_PID
          fi

          echo "STARTED Server `date`" 
        fi

        ;;

  stop)
        echo -n "Stopping Server: "
	if [ "$START_STOP_DAEMON" = "1" ] && type start-stop-daemon > /dev/null 2>&1; then
	  start-stop-daemon -K -p $SERVER_PID -d $SERVER_HOME -a $JAVA -s HUP 
	  sleep 1
	  if running $SERVER_PID
	  then
	      sleep 3
	      if running $SERVER_PID
	      then
		  sleep 30
	          if running $SERVER_PID
	          then
	             start-stop-daemon -K -p $SERVER_PID -d $SERVER_HOME -a $JAVA -s KILL
		  fi
              fi
	  fi

	  rm -f $SERVER_PID
          echo OK
	else
	  PID=`cat $SERVER_PID 2>/dev/null`
          TIMEOUT=30
          while running $SERVER_PID && [ $TIMEOUT -gt 0 ]
          do
            kill $PID 2>/dev/null
            sleep 1
            let TIMEOUT=$TIMEOUT-1
          done
          
          [ $TIMEOUT -gt 0 ] || kill -9 $PID 2>/dev/null

	  rm -f $SERVER_PID
          echo OK
	fi
        ;;

  restart)
        SERVER_SH=$0
        if [ ! -f $SERVER_SH ]; then
          if [ ! -f $SERVER_HOME/server.sh ]; then
            echo "$SERVER_HOME/server.sh does not exist."
            exit 1
          fi
          SERVER_SH=$SERVER_HOME/server.sh
        fi
        $SERVER_SH stop $*
        sleep 5
        $SERVER_SH start $*
        ;;

*)
        usage
	;;
esac

exit 0