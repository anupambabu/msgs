package com.qorporation.msgs.server.logic;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qorporation.msgs.server.entity.definition.Contact;
import com.qorporation.msgs.server.entity.definition.Conversation;
import com.qorporation.msgs.server.entity.definition.ConversationParticipant;
import com.qorporation.msgs.server.entity.definition.Message;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.entity.manager.ContactManager;
import com.qorporation.msgs.server.entity.manager.ConversationManager;
import com.qorporation.msgs.server.entity.manager.ConversationParticipantManager;
import com.qorporation.msgs.server.entity.manager.UserDeviceManager;
import com.qorporation.msgs.server.entity.manager.UserEventManager;
import com.qorporation.msgs.server.entity.manager.UserManager;
import com.qorporation.msgs.server.messenger.MessengerService;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLInPredicate;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicController;

public class UserLogic extends LogicController {

	private ConversationManager conversationManager = null;
	private ConversationParticipantManager conversationParticipantManager = null;
	private ContactManager contactManager = null;
	private UserManager userManager = null;
	private UserDeviceManager userDeviceManager = null;
	private UserEventManager userEventManager = null;

	@Override
	public void init() {
		this.conversationManager = this.serviceManager.getService(EntityService.class).getManager(Conversation.class, ConversationManager.class);
		this.conversationParticipantManager = this.serviceManager.getService(EntityService.class).getManager(ConversationParticipant.class, ConversationParticipantManager.class);
		this.contactManager = this.serviceManager.getService(EntityService.class).getManager(Contact.class, ContactManager.class);
		this.userManager = this.serviceManager.getService(EntityService.class).getManager(User.class, UserManager.class);
		this.userDeviceManager = this.serviceManager.getService(EntityService.class).getManager(UserDevice.class, UserDeviceManager.class);
		this.userEventManager = this.serviceManager.getService(EntityService.class).getManager(UserEvent.class, UserEventManager.class);
	}
	
	public Entity<User> create(String email) {
		Entity<User> user = this.userManager.create();
		
		user.set(User.email, email);
		
		if (this.userManager.save(user)) {
			return user;
		} else {
			return null;
		}
	}
	
	public void delete(String key) {
		this.userManager.delete(key);
	}
	
	public void save(Entity<User> user) {
		this.userManager.save(user);
	}
	
	public Entity<User> get(String key) {
		return this.userManager.get(key);
	}
	
	public Entity<User> getByEmail(String email) {
		List<Entity<User>> found = this.userManager.query(User.email, email);
		if (found.size() > 0) {
			return found.get(0);
		} else {
			return null;
		}
	}
	
	public Entity<User> getByDevice(String device) {
		return this.userDeviceManager.get(device).get(UserDevice.user);
	}
	
	public Set<Entity<User>> getViewable(Entity<User> user) {
		List<Entity<Contact>> contacts = this.contactManager.query(new SQLFieldPredicate<Contact, Entity<User>>(Contact.user, user)
				.and(new SQLFieldPredicate<Contact, Integer>(Contact.status, SQLFieldPredicate.Comparator.NE, Contact.Status.DELETED.getIndex())));
		Map<String, Entity<User>> contactUsers = this.contactManager.getHelper().getValueMap(Contact.contact, contacts);
		
		List<Entity<Conversation>> conversations = this.conversationManager.getConversations(user);
		List<Entity<ConversationParticipant>> participants = this.conversationParticipantManager.query(new SQLInPredicate<ConversationParticipant, Entity<Conversation>>(ConversationParticipant.conversation, conversations));
		Map<String, Entity<User>> conversationUsers = this.conversationParticipantManager.getHelper().getValueMap(ConversationParticipant.participant, participants);
		
		Set<Entity<User>> users = new HashSet<Entity<User>>(contactUsers.size() + conversationUsers.size());
		
		users.addAll(contactUsers.values());
		users.addAll(conversationUsers.values());
		
		return users;
	}
	
	@SuppressWarnings("unchecked")
	public void sendMessageEvent(Entity<User> user, Entity<Message> message) { this.sendMessageEvent(Arrays.asList(user), message); }
	public void sendMessageEvent(List<Entity<User>> users, Entity<Message> message) {
		Map<Entity<User>, Entity<UserEvent>> userEvents = new HashMap<Entity<User>, Entity<UserEvent>>(users.size());
		for (Entity<User> user: users) {
			userEvents.put(user, this.userEventManager.message(user, message));
		}
		
		sendEvents(users, userEvents);
	}
	
	@SuppressWarnings("unchecked")
	public void sendContactEvent(Entity<User> user, Entity<Contact> contact, UserEvent.ContactAction action, Timestamp timestamp) { this.sendContactEvent(Arrays.asList(user), contact, action, timestamp); }
	public void sendContactEvent(List<Entity<User>> users, Entity<Contact> contact, UserEvent.ContactAction action, Timestamp timestamp) {
		Map<Entity<User>, Entity<UserEvent>> userEvents = new HashMap<Entity<User>, Entity<UserEvent>>(users.size());
		for (Entity<User> user: users) {
			userEvents.put(user, this.userEventManager.contact(user, contact, action, timestamp));
		}
		
		sendEvents(users, userEvents);
	}
	
	@SuppressWarnings("unchecked")
	public void sendConversationEvent(Entity<User> user, Entity<Conversation> conversation, UserEvent.ConversationAction action, Timestamp timestamp) { this.sendConversationEvent(Arrays.asList(user), conversation, action, timestamp); }
	public void sendConversationEvent(List<Entity<User>> users, Entity<Conversation> conversation, UserEvent.ConversationAction action, Timestamp timestamp) {
		Map<Entity<User>, Entity<UserEvent>> userEvents = new HashMap<Entity<User>, Entity<UserEvent>>(users.size());
		for (Entity<User> user: users) {
			userEvents.put(user, this.userEventManager.conversation(user, conversation, action, timestamp));
		}
		
		sendEvents(users, userEvents);
	}
	
	@SuppressWarnings("unchecked")
	public void sendParticipantEvent(Entity<User> user, Entity<ConversationParticipant> participant, UserEvent.ConversationParticipantAction action, Timestamp timestamp) { this.sendParticipantEvent(Arrays.asList(user), participant, action, timestamp); }
	public void sendParticipantEvent(List<Entity<User>> users, Entity<ConversationParticipant> participant, UserEvent.ConversationParticipantAction action, Timestamp timestamp) {
		Map<Entity<User>, Entity<UserEvent>> userEvents = new HashMap<Entity<User>, Entity<UserEvent>>(users.size());
		for (Entity<User> user: users) {
			userEvents.put(user, this.userEventManager.participant(user, participant, action, timestamp));
		}
		
		sendEvents(users, userEvents);
	}
	
	@SuppressWarnings("unchecked")
	public void sendAvatarEvent(Entity<User> user, Entity<User> avatar, Timestamp timestamp) { this.sendAvatarEvent(Arrays.asList(user), avatar, timestamp); }
	public void sendAvatarEvent(List<Entity<User>> users, Entity<User> avatar, Timestamp timestamp) {
		Map<Entity<User>, Entity<UserEvent>> userEvents = new HashMap<Entity<User>, Entity<UserEvent>>(users.size());
		for (Entity<User> user: users) {
			userEvents.put(user, this.userEventManager.avatar(user, avatar, timestamp));
		}
		
		sendEvents(users, userEvents);
	}
	
	private void sendEvents(List<Entity<User>> users, Map<Entity<User>, Entity<UserEvent>> userEvents) {
		List<Entity<UserDevice>> devices = this.userDeviceManager.query(new SQLInPredicate<UserDevice, Entity<User>>(UserDevice.user, users));
		for (Entity<UserDevice> device: devices) {
			Entity<UserEvent> userEvent = userEvents.get(device.get(UserDevice.user));
			UserDevice.DeviceType.getController(device.get(UserDevice.deviceType)).deliverEvent(this.serviceManager.getService(MessengerService.class), device, userEvent);
		}
	}

	public boolean updateAccount(Entity<UserDevice> device, String userName, String userEmail, String userPhone) {
		Entity<User> user = device.get(UserDevice.user);
		user.set(User.name, userName);
		user.set(User.pendingEmail, userEmail);
		boolean savedUser = this.userManager.save(user);
		
		device.set(UserDevice.pendingPhone, userPhone);
		boolean savedDevice = this.userDeviceManager.save(device);
		
		return savedUser && savedDevice;
	}
	
}