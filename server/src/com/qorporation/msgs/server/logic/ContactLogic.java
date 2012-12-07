package com.qorporation.msgs.server.logic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qorporation.msgs.server.entity.definition.Contact;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.entity.manager.ContactManager;
import com.qorporation.msgs.server.entity.manager.UserDeviceManager;
import com.qorporation.msgs.server.entity.manager.UserManager;
import com.qorporation.qluster.common.Pair;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicController;

public class ContactLogic extends LogicController {

	private ContactManager contactManager = null;
	private UserManager userManager = null;
	private UserDeviceManager userDeviceManager = null;
	
	@Override
	public void init() {
		this.contactManager = this.serviceManager.getService(EntityService.class).getManager(Contact.class, ContactManager.class);
		this.userManager = this.serviceManager.getService(EntityService.class).getManager(User.class, UserManager.class);
		this.userDeviceManager = this.serviceManager.getService(EntityService.class).getManager(UserDevice.class, UserDeviceManager.class);
	}
	
	public List<Map<String, Object>> getContactListInfo(Entity<User> user) {		
		List<Entity<Contact>> contacts = this.contactManager.query(new SQLFieldPredicate<Contact, Entity<User>>(Contact.user, user)
																.and(new SQLFieldPredicate<Contact, Integer>(Contact.status, SQLFieldPredicate.Comparator.NE, Contact.Status.DELETED.getIndex())));
		
		Map<String, Entity<User>> contactUsers = this.contactManager.getHelper().getValueMap(Contact.contact, contacts);
		
		List<Map<String, Object>> contactList = new ArrayList<Map<String, Object>>(contacts.size());
		for (Entity<Contact> contact: contacts) {
			Map<String, Object> contactInfo = new HashMap<String, Object>();
			
			contactInfo.put("user", contact.get(Contact.contact).getKey());
			contactInfo.put("name", contactUsers.get(contact.get(Contact.contact).getKey()).get(User.name));
			contactInfo.put("status", contact.get(Contact.status).toString());
			
			contactList.add(contactInfo);
		}
		
		return contactList;
	}

	private boolean updateContactState(Entity<User> user, String contactKey, Contact.Status status) {
		Entity<User> other = this.logicService.get(UserLogic.class).get(contactKey);
		if (other == null || user.getKey().equals(other.getKey())) {
			return false;
		} else {
			Pair<Entity<Contact>, Entity<Contact>> contactAndMirror = this.contactManager.updateContactState(user, other, status); 
			if (contactAndMirror != null) {
				Timestamp ts = new Timestamp(System.currentTimeMillis());
				
				if (!status.getEventAction().equals(UserEvent.ContactAction.INVALID)) {
					Entity<Contact> contact = contactAndMirror.a();
					this.logicService.get(UserLogic.class).sendContactEvent(user, contact, status.getEventAction(), ts);
				}
								
				if (!status.getMirror().getEventAction().equals(UserEvent.ContactAction.INVALID)) {
					Entity<Contact> mirror = contactAndMirror.b();
					this.logicService.get(UserLogic.class).sendContactEvent(other, mirror, status.getEventAction(), ts);
				}
				
				if (status.isAvatarEvent()) {
					this.logicService.get(UserLogic.class).sendAvatarEvent(other, user, ts);
				}
				
				if (status.getMirror().isAvatarEvent()) {
					this.logicService.get(UserLogic.class).sendAvatarEvent(user, other, ts);
				}
				
				return true;
			} else {
				return false;
			}
		}
	}
	
	public boolean requestContact(Entity<User> user, String contactKey) { return updateContactState(user, contactKey, Contact.Status.REQUESTOR); }
	public boolean acceptContact(Entity<User> user, String contactKey) { return updateContactState(user, contactKey, Contact.Status.ACTIVE); }
	public boolean ignoreContact(Entity<User> user, String contactKey) { return updateContactState(user, contactKey, Contact.Status.IGNOROR); }
	public boolean deleteContact(Entity<User> user, String contactKey) { return updateContactState(user, contactKey, Contact.Status.DELETOR); }
	
	public boolean requestContactByEmail(Entity<User> user, String email) {
		List<Entity<User>> found = this.userManager.query(User.email, email);
		if (found.size() > 0) {
			return requestContact(user, found.get(0).getKey());
		} else {
			return false;
		}
	}
	
	public boolean requestContactByPhone(Entity<User> user, String phone) { 
		List<Entity<UserDevice>> found = this.userDeviceManager.query(UserDevice.phone, phone);
		if (found.size() > 0) {
			return requestContact(user, found.get(0).get(UserDevice.user).getKey());
		} else {
			return false;
		}
	}
	
}
