package com.qorporation.msgs.server.entity.definition;

import java.sql.Timestamp;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.annotation.AllowNull;
import com.qorporation.qluster.entity.annotation.Prefetch;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class UserEvent extends SQLTable {

	@Indexed
	public static FieldKey<Entity<User>> user;
	public static FieldKey<Timestamp> time;
	public static FieldKey<Boolean> filtered;
	public static FieldKey<Boolean> cleared;
	
	@AllowNull
	@Prefetch
	public static FieldKey<Entity<Conversation>> conversation;
	public static enum ConversationAction { INVALID, JOIN, LEAVE, UPDATE }
	public static FieldKey<String> conversationAction;
	
	@AllowNull
	@Prefetch
	public static FieldKey<Entity<ConversationParticipant>> participant;
	public static enum ConversationParticipantAction { INVALID, JOIN, LEAVE, UPDATE }	
	public static FieldKey<String> participantAction;
	
	@AllowNull
	@Prefetch
	public static FieldKey<Entity<Contact>> contact;
	public static enum ContactAction { INVALID, REQUESTOR, REQUESTED, ACCEPTOR, IGNOROR, DELETOR, UPDATE }
	public static FieldKey<String> contactAction;
	
	@AllowNull
	@Prefetch
	public static FieldKey<Entity<Message>> message;
	
	@AllowNull
	@Prefetch
	public static FieldKey<Entity<User>> avatar;
	
	@Indexed(fields={"user", "time", "filtered", "cleared"}, unique=false)
	public static IndexMetaKey messageUserTimeFilteredClearedIndex;

	@Indexed(fields={"user", "time", "conversation"}, unique=true)
	public static IndexMetaKey uniqueConversation;
	
	@Indexed(fields={"user", "time", "participant"}, unique=true)
	public static IndexMetaKey uniqueParticipant;
	
	@Indexed(fields={"user", "time", "contact"}, unique=true)
	public static IndexMetaKey uniqueContact;
	
	@Indexed(fields={"user", "time", "avatar"}, unique=true)
	public static IndexMetaKey uniqueAvatar;
	
	@Indexed(fields={"user", "message"}, unique=true)
	public static IndexMetaKey uniqueMessage;
	
}
