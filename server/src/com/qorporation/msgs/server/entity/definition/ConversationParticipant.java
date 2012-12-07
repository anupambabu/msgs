package com.qorporation.msgs.server.entity.definition;

import java.sql.Timestamp;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class ConversationParticipant extends SQLTable {

	@Indexed
	public static FieldKey<Entity<Conversation>> conversation;
	
	@Indexed
	public static FieldKey<Entity<User>> participant;
	
	public static FieldKey<Timestamp> joinTime;
	public static FieldKey<Timestamp> lastSync;
	
	@Indexed(fields={"conversation", "participant"}, unique=true)
	public static IndexMetaKey conversationParticipantContstraint;
	
}
