package com.qorporation.msgs.server.entity.manager;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.qorporation.msgs.server.entity.definition.Conversation;
import com.qorporation.msgs.server.entity.definition.ConversationParticipant;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.entity.Entity;

public class ConversationManager extends SQLBackedEntityManager<Conversation> {
	
	public List<Entity<Conversation>> getConversations(Entity<User> user) {
		ConversationParticipantManager cpm = this.entityService.getManager(ConversationParticipant.class, ConversationParticipantManager.class);
		List<Entity<ConversationParticipant>> participantList = cpm.query(new SQLFieldPredicate<ConversationParticipant, Entity<User>>(ConversationParticipant.participant, user));
		List<Entity<Conversation>> conversations = cpm.getHelper().getValues(ConversationParticipant.conversation, participantList);
		this.materialize(conversations);
		return conversations;
	}
	
	public List<Entity<User>> getUsers(Entity<Conversation> conversation) {
		ConversationParticipantManager cpm = this.entityService.getManager(ConversationParticipant.class, ConversationParticipantManager.class);
		List<Entity<ConversationParticipant>> participantList = cpm.query(new SQLFieldPredicate<ConversationParticipant, Entity<Conversation>>(ConversationParticipant.conversation, conversation));
		List<Entity<User>> users = cpm.getHelper().getValues(ConversationParticipant.participant, participantList);
		this.entityService.getManager(User.class, UserManager.class).materialize(users);
		return users;		
	}
	
	public List<Entity<ConversationParticipant>> getParticipants(Entity<Conversation> conversation) {
		ConversationParticipantManager cpm = this.entityService.getManager(ConversationParticipant.class, ConversationParticipantManager.class);
		List<Entity<ConversationParticipant>> participantList = cpm.query(new SQLFieldPredicate<ConversationParticipant, Entity<Conversation>>(ConversationParticipant.conversation, conversation));
		return participantList;	
	}
	
	public boolean isParticipant(Entity<Conversation> conversation, Entity<User> user) {
		ConversationParticipantManager cpm = this.entityService.getManager(ConversationParticipant.class, ConversationParticipantManager.class);
		List<Entity<ConversationParticipant>> participantList = cpm.query(new SQLFieldPredicate<ConversationParticipant, Entity<User>>(ConversationParticipant.participant, user)
																		.and(new SQLFieldPredicate<ConversationParticipant, Entity<Conversation>>(ConversationParticipant.conversation, conversation)));
		return participantList.size() == 1;
	}
	
	public Entity<Conversation> startGeneral(Entity<User> user) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		
		Entity<Conversation> e = this.create();
		e.set(Conversation.originator, user);
		e.set(Conversation.type, Conversation.Type.GENERAL.getIndex());
		e.set(Conversation.startTime, ts);
		e.set(Conversation.lastMessageTime, ts);
		
		if (this.save(e)) {
			this.addParticipant(e, user);
			return e;
		} else {
			return null;
		}
	}
	
	public Entity<Conversation> startPrimary(Entity<User> user, Entity<User> other) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		
		Entity<Conversation> e = this.create();
		e.set(Conversation.originator, user);
		e.set(Conversation.type, Conversation.Type.PRIMARY.getIndex());
		e.set(Conversation.startTime, ts);
		e.set(Conversation.lastMessageTime, ts);
		
		if (this.save(e)) {
			this.addParticipant(e, user);
			this.addParticipant(e, other);
		} else {
			e = null;
		}
		
		Set<Entity<Conversation>> userConversations = new HashSet<Entity<Conversation>>(this.getConversations(user));
		Set<Entity<Conversation>> otherConversations = new HashSet<Entity<Conversation>>(this.getConversations(other));
		
		userConversations.retainAll(otherConversations);

		List<Entity<Conversation>> primaryConversations = this.getHelper().filter(userConversations, Conversation.type, Conversation.Type.PRIMARY.getIndex());
		Entity<Conversation> primary = null;
		
		for (Entity<Conversation> p: primaryConversations) {
			if (primary == null) {
				primary = p;
			} else {
				if (primary.get(Conversation.startTime).after(p.get(Conversation.startTime))) {
					primary = p;
				}
			}
		}
		
		if (e != null && !primary.getKey().equals(e.getKey())) {
			this.removeParticipant(e, user);
			this.removeParticipant(e, other);
			e.delete();
		}
		
		return primary;
	}
	
	public Entity<ConversationParticipant> addParticipant(Entity<Conversation> conversation, Entity<User> invitee) {
		if (conversation.get(Conversation.type).equals(Conversation.Type.PRIMARY)) {
			conversation.set(Conversation.type, Conversation.Type.GENERAL.getIndex());
			if (!this.save(conversation)) {
				return null;
			}			
		}
		
		ConversationParticipantManager cpm = this.entityService.getManager(ConversationParticipant.class, ConversationParticipantManager.class);
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		
		Entity<ConversationParticipant> e = cpm.create();
		e.set(ConversationParticipant.conversation, conversation);
		e.set(ConversationParticipant.participant, invitee);
		e.set(ConversationParticipant.joinTime, ts);
		e.set(ConversationParticipant.lastSync, ts);
		
		if (cpm.save(e)) {
			return e;
		} else {
			return null;
		}
	}
	
	public Entity<ConversationParticipant> removeParticipant(Entity<Conversation> conversation, Entity<User> user) {
		ConversationParticipantManager cpm = this.entityService.getManager(ConversationParticipant.class, ConversationParticipantManager.class);
		List<Entity<ConversationParticipant>> participantList = cpm.query(new SQLFieldPredicate<ConversationParticipant, Entity<User>>(ConversationParticipant.participant, user)
				.and(new SQLFieldPredicate<ConversationParticipant, Entity<Conversation>>(ConversationParticipant.conversation, conversation)));
		if (participantList.size() != 1) return null;
		
		Entity<ConversationParticipant> participant = participantList.get(0);
		participant.delete();
		
		return participant;
	}
	
}
