package com.qorporation.msgs.server;

import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.qluster.Qluster;
import com.qorporation.qluster.config.Config;

public class Msgs {
	
	public static void main(String[] args) {		
		Qluster.start(new Config() {{
			this.rootPackage = "com.qorporaiton.msgs";
			this.cassandraKeySpace = "Msgs";
			this.mysqlDB = "msgs";
			this.userDefinition = User.class;
		}});
	}
	
}
