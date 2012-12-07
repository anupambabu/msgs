package com.qorporation.msgs.server.entity.manager;

import java.sql.Timestamp;
import java.util.List;

import com.qorporation.msgs.server.entity.definition.Contact;
import com.qorporation.msgs.server.entity.definition.Conversation;
import com.qorporation.msgs.server.entity.definition.ConversationParticipant;
import com.qorporation.msgs.server.entity.definition.Message;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.operation.params.SQLValue;
import com.qorporation.qluster.conn.sql.operation.params.SQLValueList;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLInPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLOrderPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLPredicate;
import com.qorporation.qluster.entity.Entity;

public class UserEventManager extends SQLBackedEntityManager<UserEvent> {

	public Entity<UserEvent> message(Entity<User> user, Entity<Message> message) {
		List<Entity<UserEvent>> messages = this.query(new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
				.and(new SQLFieldPredicate<UserEvent, Entity<Message>>(UserEvent.message, message)));
		if (messages.size() == 1) {
			return messages.get(0);
		}
		
		Entity<UserEvent> m = this.create();
		m.set(UserEvent.user, user);
		m.set(UserEvent.message, message);
		m.set(UserEvent.time, message.get(Message.time));
		if (this.save(m)) {
			this.update(new SQLValueList<UserEvent>(new SQLValue<UserEvent, Boolean>(UserEvent.filtered, true)),
					new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
							.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, SQLFieldPredicate.Comparator.LT, message.get(Message.time)))
							.and(new SQLFieldPredicate<UserEvent, Entity<Message>>(UserEvent.message, message)));
			
			return m;
		} else {
			return message(user, message);
		}
	}
	
	public Entity<UserEvent> avatar(Entity<User> user, Entity<User> avatar, Timestamp timestamp) {
		List<Entity<UserEvent>> events = this.query(new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
				.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, timestamp))
				.and(new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.avatar, avatar)));
		if (events.size() == 1) {
			return events.get(0);
		}
		
		Entity<UserEvent> e = this.create();
		e.set(UserEvent.user, user);
		e.set(UserEvent.avatar, avatar);
		e.set(UserEvent.time, timestamp);
		if (this.save(e)) {
			this.update(new SQLValueList<UserEvent>(new SQLValue<UserEvent, Boolean>(UserEvent.filtered, true)),
					new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
							.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, SQLFieldPredicate.Comparator.LT, timestamp))
							.and(new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.avatar, avatar)));
			
			return e;
		} else {
			return avatar(user, avatar, timestamp);
		}
	}
	
	public Entity<UserEvent> contact(Entity<User> user, Entity<Contact> contact, UserEvent.ContactAction action, Timestamp timestamp) {		
		List<Entity<UserEvent>> events = this.query(new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
				.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, timestamp))
				.and(new SQLFieldPredicate<UserEvent, Entity<Contact>>(UserEvent.contact, contact)));
		if (events.size() == 1) {
			return events.get(0);
		}
		
		Entity<UserEvent> e = this.create();
		e.set(UserEvent.user, user);
		e.set(UserEvent.contactAction, action.toString().toLowerCase());
		e.set(UserEvent.contact, contact);
		e.set(UserEvent.time, timestamp);
		if (this.save(e)) {
			this.update(new SQLValueList<UserEvent>(new SQLValue<UserEvent, Boolean>(UserEvent.filtered, true)),
					new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
							.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, SQLFieldPredicate.Comparator.LT, timestamp))
							.and(new SQLFieldPredicate<UserEvent, Entity<Contact>>(UserEvent.contact, contact)));
			
			return e;
		} else {
			return contact(user, contact, action, timestamp);
		}
	}
	
	public Entity<UserEvent> conversation(Entity<User> user, Entity<Conversation> conversation, UserEvent.ConversationAction action, Timestamp timestamp) {
		List<Entity<UserEvent>> events = this.query(new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
				.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, timestamp))
				.and(new SQLFieldPredicate<UserEvent, Entity<Conversation>>(UserEvent.conversation, conversation)));
		if (events.size() == 1) {
			return events.get(0);
		}
		
		Entity<UserEvent> e = this.create();
		e.set(UserEvent.user, user);
		e.set(UserEvent.conversationAction, action.toString().toLowerCase());
		e.set(UserEvent.conversation, conversation);
		e.set(UserEvent.time, timestamp);
		if (this.save(e)) {
			this.update(new SQLValueList<UserEvent>(new SQLValue<UserEvent, Boolean>(UserEvent.filtered, true)),
					new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
							.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, SQLFieldPredicate.Comparator.LT, timestamp))
							.and(new SQLFieldPredicate<UserEvent, Entity<Conversation>>(UserEvent.conversation, conversation)));
			
			return e;
		} else {
			return conversation(user, conversation, action, timestamp);
		}
	}
	
	public Entity<UserEvent> participant(Entity<User> user, Entity<ConversationParticipant> participant, UserEvent.ConversationParticipantAction action, Timestamp timestamp) {
		List<Entity<UserEvent>> events = this.query(new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
				.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, timestamp))
				.and(new SQLFieldPredicate<UserEvent, Entity<ConversationParticipant>>(UserEvent.participant, participant)));
		if (events.size() == 1) {
			return events.get(0);
		}
		
		Entity<UserEvent> e = this.create();
		e.set(UserEvent.user, user);
		e.set(UserEvent.participantAction, action.toString().toLowerCase());
		e.set(UserEvent.participant, participant);
		e.set(UserEvent.time, timestamp);
		if (this.save(e)) {
			this.update(new SQLValueList<UserEvent>(new SQLValue<UserEvent, Boolean>(UserEvent.filtered, true)),
					new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
							.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, SQLFieldPredicate.Comparator.LT, timestamp))
							.and(new SQLFieldPredicate<UserEvent, Entity<ConversationParticipant>>(UserEvent.participant, participant)));
			
			return e;
		} else {
			return participant(user, participant, action, timestamp);
		}
	}
	
	public List<Entity<UserEvent>> retrieve(Entity<User> user, Long startTime, Long endTime) {
		SQLPredicate<UserEvent> predicate = new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
													.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, SQLFieldPredicate.Comparator.GTE, new Timestamp(startTime)));
		
		if (endTime != null) {
			predicate = predicate.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, SQLFieldPredicate.Comparator.LTE, new Timestamp(endTime)));
		}
		
		predicate = predicate.order(UserEvent.time, SQLOrderPredicate.Order.ASC);
		
		return this.query(predicate);
	}
	
	public List<Entity<UserEvent>> retrieveFiltered(Entity<User> user, Long startTime, Long endTime) { return retrieveFiltered(user, startTime, endTime, false); }
	public List<Entity<UserEvent>> retrieveFiltered(Entity<User> user, Long startTime, Long endTime, boolean includeMessages) {
		SQLPredicate<UserEvent> predicate = new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
													.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, SQLFieldPredicate.Comparator.GTE, new Timestamp(startTime))
													.and(new SQLFieldPredicate<UserEvent, Boolean>(UserEvent.filtered, false)));
		
		if (endTime != null) {
			predicate = predicate.and(new SQLFieldPredicate<UserEvent, Timestamp>(UserEvent.time, SQLFieldPredicate.Comparator.LTE, new Timestamp(endTime)));
		}
		
		if (!includeMessages) {
			Entity<Message> nullMessage = this.entityService.getManager(Message.class, MessageManager.class).create("0");
			predicate = predicate.and(new SQLFieldPredicate<UserEvent, Entity<Message>>(UserEvent.message, nullMessage)
												.or(new SQLFieldPredicate<UserEvent, Entity<Message>>(UserEvent.message, SQLFieldPredicate.Comparator.ISNULL)));
		}
		
		predicate = predicate.order(UserEvent.time, SQLOrderPredicate.Order.ASC);
		
		return this.query(predicate);
	}
	
	public List<Entity<UserEvent>> retrieveCurrent(Entity<User> user) {
		Entity<Message> nullMessage = this.entityService.getManager(Message.class, MessageManager.class).create("0");
		
		SQLPredicate<UserEvent> predicate = new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
													.and(new SQLFieldPredicate<UserEvent, Boolean>(UserEvent.filtered, false))
													.and(new SQLFieldPredicate<UserEvent, Entity<Message>>(UserEvent.message, nullMessage)
															.or(new SQLFieldPredicate<UserEvent, Entity<Message>>(UserEvent.message, SQLFieldPredicate.Comparator.ISNULL)))
													.order(UserEvent.time, SQLOrderPredicate.Order.ASC);
		
		return this.query(predicate);
	}

	public List<Entity<UserEvent>> getMessageLatestEvents(Entity<User> user, List<Entity<Conversation>> conversations) {
		List<Entity<Message>> messages = this.entityService.getManager(Conversation.class, ConversationManager.class).getHelper().getValues(Conversation.lastMessage, conversations);
		
		SQLPredicate<UserEvent> predicate = new SQLFieldPredicate<UserEvent, Entity<User>>(UserEvent.user, user)
													.and(new SQLInPredicate<UserEvent, Entity<Message>>(UserEvent.message, messages))
													.order(UserEvent.time, SQLOrderPredicate.Order.ASC);
		
		return this.query(predicate);
	}
	
}
