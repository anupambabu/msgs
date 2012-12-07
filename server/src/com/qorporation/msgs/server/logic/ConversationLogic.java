package com.qorporation.msgs.server.logic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.qorporation.msgs.server.entity.definition.Contact;
import com.qorporation.msgs.server.entity.definition.Conversation;
import com.qorporation.msgs.server.entity.definition.ConversationParticipant;
import com.qorporation.msgs.server.entity.definition.Message;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.entity.manager.ContactManager;
import com.qorporation.msgs.server.entity.manager.ConversationManager;
import com.qorporation.msgs.server.entity.manager.ConversationParticipantManager;
import com.qorporation.msgs.server.entity.manager.MessageManager;
import com.qorporation.qluster.common.Pair;
import com.qorporation.qluster.common.PairZipIterator;
import com.qorporation.qluster.conn.sql.operation.params.SQLValueList;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLEntityPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLInPredicate;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicController;

public class ConversationLogic extends LogicController {

	private ConversationManager conversationManager = null;
	private ConversationParticipantManager conversationParticipantManager = null;
	private ContactManager contactManager = null;
	private MessageManager messageManager = null;
	
	@Override
	public void init() {
		this.conversationManager = this.serviceManager.getService(EntityService.class).getManager(Conversation.class, ConversationManager.class);
		this.conversationParticipantManager = this.serviceManager.getService(EntityService.class).getManager(ConversationParticipant.class, ConversationParticipantManager.class);
		this.contactManager = this.serviceManager.getService(EntityService.class).getManager(Contact.class, ContactManager.class);
		this.messageManager = this.serviceManager.getService(EntityService.class).getManager(Message.class, MessageManager.class);
	}
	
	public Entity<Conversation> get(String key) {
		return this.conversationManager.get(key);
	}
	
	public List<Map<String, Object>> getConversationListInfo(Entity<User> user) {		
		List<Entity<Conversation>> conversations = this.conversationManager.getConversations(user);
		List<Entity<ConversationParticipant>> participants = this.conversationParticipantManager.query(new SQLInPredicate<ConversationParticipant, Entity<Conversation>>(ConversationParticipant.conversation, conversations));
		
		Map<String, Entity<Message>> messages = this.conversationManager.getHelper().getValueMap(Conversation.lastMessage, conversations);
		Map<Entity<Conversation>, List<Entity<ConversationParticipant>>> participantMap = this.conversationParticipantManager.getHelper().map(ConversationParticipant.conversation, participants);
		Map<String, Entity<User>> users = this.conversationParticipantManager.getHelper().getValueMap(ConversationParticipant.participant, participants);
		
		List<Map<String, Object>> conversationList = new ArrayList<Map<String, Object>>(conversations.size());
		for (Entity<Conversation> c: conversations) {			
			Map<String, Object> participantList = new HashMap<String, Object>();
			for (Entity<ConversationParticipant> p: participantMap.get(c)) {
				Entity<User> u = users.get(p.get(ConversationParticipant.participant).getKey());
				
				Map<String, Object> participantInfo = new HashMap<String, Object>();
				participantInfo.put("username", u.get(User.name));
				participantInfo.put("joinTime", p.get(ConversationParticipant.joinTime).getTime());
				participantInfo.put("lastSync", p.get(ConversationParticipant.lastSync).getTime());
				
				participantList.put(u.getKey(), participantInfo);
			}
						
			Map<String, Object> conversationInfo = new HashMap<String, Object>();
			conversationInfo.put("conversation", c.getKey());
			conversationInfo.put("type", c.get(Conversation.type));
			conversationInfo.put("originator", c.get(Conversation.originator).getKey());
			conversationInfo.put("originatorname", users.get(c.get(Conversation.originator).getKey()).get(User.name));
			conversationInfo.put("startTime", c.get(Conversation.startTime).getTime());
			conversationInfo.put("participants", participantList);
			conversationInfo.put("lastMessageTime", c.get(Conversation.lastMessageTime).getTime());
			
			Entity<Message> lastMessage = messages.get(c.get(Conversation.lastMessage).getKey());
			if (!lastMessage.getKey().equals("0")) {
				conversationInfo.put("lastMessageAuthor", users.get(lastMessage.get(Message.sender).getKey()).get(User.name));
				conversationInfo.put("lastMessageBody", lastMessage.get(Message.body));
			}
			
			conversationList.add(conversationInfo);
		}
		
		return conversationList;
	}
	
	public boolean setConversationSync(Entity<User> user, Entity<Conversation> conversation, Timestamp timestamp) {
		if (!this.conversationManager.isParticipant(conversation, user)) return false;
		List<Entity<ConversationParticipant>> entities = this.conversationParticipantManager.query(new SQLFieldPredicate<ConversationParticipant, Entity<User>>(ConversationParticipant.participant, user)
												.and(new SQLFieldPredicate<ConversationParticipant, Entity<Conversation>>(ConversationParticipant.conversation, conversation)));
		if (entities.size() != 1) return false;
		
		Entity<ConversationParticipant> p = entities.get(0);
		p.set(ConversationParticipant.lastSync, timestamp);
		if (this.conversationParticipantManager.save(p)) {
			this.logicService.get(UserLogic.class).sendParticipantEvent(this.conversationManager.getUsers(conversation), p, UserEvent.ConversationParticipantAction.UPDATE, timestamp);
			return true;
		} else {
			return false;
		}
	}
	
	public List<Entity<Conversation>> getUnsycnedConversations(Entity<User> user) {
		List<Entity<ConversationParticipant>> participantList = this.conversationParticipantManager.query(new SQLFieldPredicate<ConversationParticipant, Entity<User>>(ConversationParticipant.participant, user));
		List<Entity<Conversation>> conversations = this.conversationParticipantManager.getHelper().getValues(ConversationParticipant.conversation, participantList);
		this.conversationManager.materialize(conversations);
		
		Iterator<Pair<Entity<ConversationParticipant>, Entity<Conversation>>> itr = new PairZipIterator<Entity<ConversationParticipant>, Entity<Conversation>>(participantList, conversations);
		while (itr.hasNext()) {
			Pair<Entity<ConversationParticipant>, Entity<Conversation>> p = itr.next();
			if (!p.a().get(ConversationParticipant.lastSync).before(p.b().get(Conversation.lastMessageTime))) {
				itr.remove();
			}
		}
		
		return conversations;
	}
	
	public List<Entity<Message>> getConversationMessages(Entity<User> user, Entity<Conversation> conversation) {
		if (!this.conversationManager.isParticipant(conversation, user)) return new ArrayList<Entity<Message>>();
		return this.messageManager.getConversation(conversation);
	}
	
	public Entity<Conversation> startGeneral(Entity<User> user) {
		return this.conversationManager.startGeneral(user);
	}
	
	@SuppressWarnings("unchecked")
	public Entity<Conversation> startPrimary(Entity<User> user, Entity<User> other) {
		if (!this.contactManager.isContact(user, other, Contact.Status.ACTIVE)) return null;
		
		Entity<Conversation> conversation = this.conversationManager.startPrimary(user, other);
		this.logicService.get(UserLogic.class).sendConversationEvent(Arrays.asList(user, other), conversation, UserEvent.ConversationAction.JOIN, conversation.get(Conversation.startTime));
		for (Entity<ConversationParticipant> p: this.conversationManager.getParticipants(conversation)) {
			this.logicService.get(UserLogic.class).sendParticipantEvent(Arrays.asList(user, other), p, UserEvent.ConversationParticipantAction.JOIN, p.get(ConversationParticipant.joinTime));
		}
		
		return conversation;
	}
	
	public boolean invite(Entity<Conversation> conversation, Entity<User> user, Entity<User> invitee) {
		if (!this.contactManager.isContact(user, invitee, Contact.Status.ACTIVE)) return false;
		if (!this.conversationManager.isParticipant(conversation, user)) return false;
		
		Entity<ConversationParticipant> participant = this.conversationManager.addParticipant(conversation, invitee);
		if (participant != null) {
			this.logicService.get(UserLogic.class).sendConversationEvent(invitee, conversation, UserEvent.ConversationAction.JOIN, participant.get(ConversationParticipant.joinTime));
			this.logicService.get(UserLogic.class).sendParticipantEvent(this.conversationManager.getUsers(conversation), participant, UserEvent.ConversationParticipantAction.JOIN, participant.get(ConversationParticipant.joinTime));
			return true;
		} else {
			return false;
		}
	}
	
	public boolean leave(Entity<Conversation> conversation, Entity<User> user) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		
		Entity<ConversationParticipant> participant = this.conversationManager.removeParticipant(conversation, user);
		if (participant != null) {
			this.logicService.get(UserLogic.class).sendConversationEvent(user, conversation, UserEvent.ConversationAction.LEAVE, ts);
			this.logicService.get(UserLogic.class).sendParticipantEvent(this.conversationManager.getUsers(conversation), participant, UserEvent.ConversationParticipantAction.LEAVE, ts);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean sendMessage(Entity<Conversation> conversation, Entity<User> user, String body) {
		if (!this.conversationManager.isParticipant(conversation, user)) return false;
		Entity<Message> message = this.messageManager.create(conversation, user, body);
		if (message == null) return false;
		
		this.conversationManager.update(new SQLValueList<Conversation>()
				.add(Conversation.lastMessage, message)
				.add(Conversation.lastMessageTime, message.get(Message.time)), 
				new SQLEntityPredicate<Conversation>(conversation).and(
				new SQLFieldPredicate<Conversation, Timestamp>(Conversation.lastMessageTime, SQLFieldPredicate.Comparator.LT, message.get(Message.time))));
				
		List<Entity<User>> users = this.conversationManager.getUsers(conversation);
		
		this.logicService.get(UserLogic.class).sendMessageEvent(users, message);
		this.logicService.get(UserLogic.class).sendConversationEvent(users, conversation, UserEvent.ConversationAction.UPDATE, message.get(Message.time));
		
		return true;
	}

	@SuppressWarnings("unchecked")
	public Entity<Message> sendMessageDirectly(Entity<User> user, Entity<User> contact, String body) {
		if (!this.contactManager.isContact(user, contact, Contact.Status.ACTIVE)) return null;
		
		Entity<Conversation> conversation = this.conversationManager.startPrimary(user, contact);
		Entity<Message> message = this.messageManager.create(conversation, user, body);
		
		this.conversationManager.update(new SQLValueList<Conversation>()
				.add(Conversation.lastMessage, message)
				.add(Conversation.lastMessageTime, message.get(Message.time)), 
				new SQLEntityPredicate<Conversation>(conversation).and(
				new SQLFieldPredicate<Conversation, Timestamp>(Conversation.lastMessageTime, SQLFieldPredicate.Comparator.LT, message.get(Message.time))));
		
		this.logicService.get(UserLogic.class).sendMessageEvent(Arrays.asList(user, contact), message);
		this.logicService.get(UserLogic.class).sendConversationEvent(Arrays.asList(user, contact), conversation, UserEvent.ConversationAction.UPDATE, message.get(Message.time));
		for (Entity<ConversationParticipant> p: this.conversationManager.getParticipants(conversation)) {
			this.logicService.get(UserLogic.class).sendParticipantEvent(Arrays.asList(user, contact), p, UserEvent.ConversationParticipantAction.JOIN, p.get(ConversationParticipant.joinTime));
		}
		
		return message;
	}
	
}
