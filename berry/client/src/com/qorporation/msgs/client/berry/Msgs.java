package com.qorporation.msgs.client.berry;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.file.FileSystemListener;
import javax.microedition.io.file.FileSystemRegistry;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qorporation.msgs.client.berry.account.AccountInitializer;
import com.qorporation.msgs.client.berry.account.AccountInitializerDelegate;
import com.qorporation.msgs.client.berry.networking.MessageSocket;
import com.qorporation.msgs.client.berry.networking.MessageSocketDelegate;
import com.qorporation.msgs.client.berry.screens.home.HomeScreen;
import com.qorporation.msgs.client.berry.store.AccountStore;
import com.qorporation.msgs.client.berry.store.ContactStore;
import com.qorporation.msgs.client.berry.store.ConversationStore;
import com.qorporation.msgs.client.berry.store.EventStore;
import com.qorporation.msgs.client.berry.store.MessageStore;
import com.qorporation.msgs.client.berry.store.Store;
import com.qorporation.msgs.client.berry.store.UserStore;
import com.qorporation.msgs.client.berry.util.ErrorControl;

import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.notification.NotificationsConstants;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.ui.UiApplication;

public class Msgs extends UiApplication implements AccountInitializerDelegate, MessageSocketDelegate {
	private static final long MESSAGE_NOTIFIER = 0xa38f34a38f34L;
	public static final long DEBUG_GUID = 0xa38f34a38f34L;
	
	public static final String HOST = "msgs.io";
	
	private boolean requiresAccountInitialization = true;
	private AccountInitializer accountInitializer = null;
	private MessageSocket socket = null;
	private HomeScreen homeScreen = null;
	
	private MsgsFileSystemListener fileListener = null;
	private MsgsRadioListener radioListener = null;
	
    public static void main(String[] args) {
        Msgs msgs = new Msgs();       
        msgs.enterEventDispatcher();
    }

    public Msgs() {
    	EventLogger.register(Msgs.DEBUG_GUID, "msgs", EventLogger.VIEWER_STRING);
    	EventLogger.setMinimumLevel(EventLogger.DEBUG_INFO);
    	
    	EncodedImage indicatorImage = EncodedImage.getEncodedImageResource("indicator.png");
    	ApplicationIcon indicatorIcon = new ApplicationIcon(indicatorImage);
    	ApplicationIndicatorRegistry.getInstance().register(indicatorIcon, false, false);
    	
    	NotificationsManager.registerSource(MESSAGE_NOTIFIER, "Message", NotificationsConstants.IMPORTANT);
    	
    	this.fileListener = new MsgsFileSystemListener();
    	this.addFileSystemListener(this.fileListener);
    	this.tryInit();
    }
    
    private void tryInit() {
        boolean foundSD = false;
        Enumeration roots = FileSystemRegistry.listRoots();
        while (roots.hasMoreElements()) {
            if (((String) roots.nextElement()).equalsIgnoreCase("sdcard/")) {
            	foundSD = true;
            }     
        }
        
        if (foundSD) {
        	try {
	        	AccountStore.getInstance().init();
	        	ContactStore.getInstance().init();
	        	ConversationStore.getInstance().init();
	        	EventStore.getInstance().init();
	        	MessageStore.getInstance().init();
	        	UserStore.getInstance().init();
	        	
				this.homeScreen = new HomeScreen();
				this.pushScreen(this.homeScreen);
				
		    	this.requiresAccountInitialization = !AccountStore.getInstance().hasAuthSecret() || !AccountStore.getInstance().hasAuthToken();
		    	this.accountInitializer = new AccountInitializer(this);
		    	this.socket = new MessageSocket(Msgs.HOST, MessageSocket.PORT, this);
		        
		    	this.radioListener = new MsgsRadioListener();
		    	this.addRadioListener(this.radioListener);
		    	
				if (TransportInfo.hasSufficientCoverage(TransportInfo.TRANSPORT_TCP_CELLULAR)
						|| TransportInfo.hasSufficientCoverage(TransportInfo.TRANSPORT_TCP_WIFI)) {
					this.radioListener.onSignal();
				}
        	} catch (Exception e) {
        		ErrorControl.logException(e);
        	}
        } else {
        }
	}

	private class MsgsRadioListener implements RadioStatusListener {
		public void baseStationChange() {}
		public void networkScanComplete(boolean success) {}
		public void networkServiceChange(int networkId, int service) {}
		public void networkStarted(int networkId, int service) {}
		public void networkStateChange(int state) {}
		public void pdpStateChange(int apn, int state, int cause) {}
		public void radioTurnedOff() {}
		public void signalLevel(int level) {
			if (level != RadioInfo.LEVEL_NO_COVERAGE && !Msgs.this.socket.isConnected()) {
				this.onSignal();
			}
		}
		
		public void onSignal() {
			Msgs.this.accountInitializer.authenticate();
			if (Msgs.this.socket.connect()) {
				if (!Msgs.this.requiresAccountInitialization) {
					Msgs.this.sendSocketAuthentication();
				}
			}
		}
    }
    
    private class MsgsFileSystemListener implements FileSystemListener {
		public void rootChanged(int state, String rootName) {
			if (state == ROOT_ADDED && Msgs.this.homeScreen == null) {
				try {
					Msgs.this.tryInit();
				} catch (Exception e) {
					ErrorControl.logException(e);
				}
			} else if (state == ROOT_REMOVED && Msgs.this.homeScreen != null) {
			}
		}
    }
    
    public void activate() {
    }
	
	public void onAccountInitialized(String userID, String userEmail, String authToken, String authSecret) {
		if (this.requiresAccountInitialization) {
			this.sendSocketAuthentication();
		}
	}

	public void onAccountInitializationFailed() {
		ErrorControl.logException(new Exception("Account Initialization Failed"));
	}
	
	public void onAccountInitializationException(Exception e) {
		ErrorControl.logException(e);
	}

	private void sendSocketAuthentication() {
		Hashtable message = new Hashtable();
		message.put("cmd", "connect");
		message.put("devicetype", "blackberry");
		message.put("deviceident", AccountStore.getInstance().getDeviceIdent());
		message.put("authtoken", AccountStore.getInstance().getAuthToken());
		message.put("lastsync", AccountStore.getInstance().getLastSync());
		this.socket.sendNetworkJSON(message);
	}

	public void onConnectionAttemptFailure(MessageSocket socket, Exception e) {
		ErrorControl.logException(e);
		//this.socket.connect();
	}

	public void onConnectionTermination(MessageSocket socket) {
		ErrorControl.logException(new Exception("Message Socket Connection Termination"));
		//this.socket.connect();
	}

	public void onReceivedNetworkPacket(String message, MessageSocket socket) {
		long firstEventTime = 0l;
		long newestMessageTime = ConversationStore.getInstance().getUnreadConversationCount();
		
		try {
			Store.onGlobalStartProcessingEvents();
			
			JSONObject json = new JSONObject(message);
			
			String type = json.getString("type");
			if (type.equals("events")) {
				JSONArray events = json.getJSONArray("events");
				if (events.length() > 0) {
					long latestSync = 0l;
					JSONArray eventIDs = new JSONArray();
					
					for (int i = 0; i < events.length(); i++) {
						JSONObject event = events.getJSONObject(i);
						
						long eventTime = event.getLong("time");
						if (eventTime > latestSync) {
							latestSync = eventTime;
						}
						
						if (firstEventTime == 0 || eventTime < firstEventTime) {
							firstEventTime = eventTime;
						}
						
						eventIDs.put(event.getInt("id"));
						
						this.onReceivedEvent(event, events);
					}
					
					Hashtable response = new Hashtable();
					response.put("cmd", "ack");
					response.put("checksum", eventIDs);
					response.put("checksync", Long.toString(latestSync));
					response.put("lastsync", AccountStore.getInstance().getLastSync().toString());
					
					this.socket.sendNetworkJSON(response);
				}
			} else if (type.equals("checksum")) {
				long lastSync = json.getLong("lastsync");
				AccountStore.getInstance().setLastSync(new Long(lastSync));
			} else if (type.equals("pong")) {
				System.out.println("receieved reply from server, socket still active");
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
		} finally {
			Store.onGlobalStopProcessingEvents(firstEventTime);
		}
		
		int unreadCount = ConversationStore.getInstance().getUnreadConversationCount();
		if (unreadCount > 0) {
			ApplicationIndicatorRegistry.getInstance().getApplicationIndicator().setVisible(true);
			ApplicationIndicatorRegistry.getInstance().getApplicationIndicator().setValue(unreadCount);
			NotificationsManager.triggerImmediateEvent(MESSAGE_NOTIFIER, 0, this, null);
		} else {
			ApplicationIndicatorRegistry.getInstance().getApplicationIndicator().setVisible(false);
		}
		
		if (ConversationStore.getInstance().getNewestMessageTime() > newestMessageTime) {
			NotificationsManager.triggerImmediateEvent(MESSAGE_NOTIFIER, 0, this, null);
		}
	}
	
	public void onReceivedEvent(JSONObject event, JSONArray events) throws Exception {
		String type = event.getString("type");
		
		if (type.equals("message")) {
			MessageStore.getInstance().setMessage(event);
			EventStore.getInstance().setEvent(
					event.getInt("id"),
					event.getLong("time"),
					event.getString("type"),
					event.getInt("message"),
					"");
		} else if (type.equals("contact")) {
			ContactStore.getInstance().setContact(event);
			EventStore.getInstance().setEvent(
					event.getInt("id"),
					event.getLong("time"),
					event.getString("type"),
					event.getInt("contact"),
					event.getString("action"));
		} else if (type.equals("conversation")) {
			ConversationStore.getInstance().setConversation(event);
			EventStore.getInstance().setEvent(
					event.getInt("id"),
					event.getLong("time"),
					event.getString("type"),
					event.getInt("conversation"),
					event.getString("action"));
		} else if (type.equals("participant")) {
			ConversationStore.getInstance().setParticipant(event);
			EventStore.getInstance().setEvent(
					event.getInt("id"),
					event.getLong("time"),
					event.getString("type"),
					event.getInt("participant"),
					event.getString("action"));
		} else if (type.equals("avatar")) {
			UserStore.getInstance().setUser(event);
			EventStore.getInstance().setEvent(
					event.getInt("id"),
					event.getLong("time"),
					event.getString("type"),
					event.getInt("user"),
					"");
		}
	}
    
}
