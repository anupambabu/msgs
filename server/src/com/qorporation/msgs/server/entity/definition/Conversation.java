package com.qorporation.msgs.server.entity.definition;

import java.sql.Timestamp;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.typesafety.FieldKey;

public class Conversation extends SQLTable {

	public static enum Type {
		GENERAL(0),
		PRIMARY(1);

		private Integer index;
		
		Type(Integer index) { 
			this.index = index;
		}
		
		public Integer getIndex() { return this.index; }
	};
	
	@Indexed
	public static FieldKey<Entity<User>> originator;
	public static FieldKey<Integer> type;
	public static FieldKey<Timestamp> startTime;
	
	public static FieldKey<Entity<Message>> lastMessage;
	public static FieldKey<Timestamp> lastMessageTime;
	
}
