package com.qorporation.msgs.server.entity.manager;

import java.sql.Timestamp;
import java.util.List;

import com.qorporation.msgs.server.entity.definition.Contact;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.qluster.common.Pair;
import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.entity.Entity;

public class ContactManager extends SQLBackedEntityManager<Contact> {

	public Entity<Contact> getContact(Entity<User> user, Entity<User> other) {		
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		
		while (true) {
			List<Entity<Contact>> requestedList = this.query(new SQLFieldPredicate<Contact, Entity<User>>(Contact.user, user)
					.and(new SQLFieldPredicate<Contact, Entity<User>>(Contact.contact, other)));
			if (requestedList.size() != 1) {
				Entity<Contact> e = this.create();
				e.set(Contact.user, user);
				e.set(Contact.contact, other);
				e.set(Contact.status, Contact.Status.NONE.getIndex());
				e.set(Contact.statusTime, ts);
				return e;
			} else {
				return requestedList.get(0);
			}
		}
	}
	
	public Entity<Contact> getMirror(Entity<Contact> contact) {
		Entity<Contact> mirror = getContact(contact.get(Contact.contact), contact.get(Contact.user));
		
		if (mirror.get(Contact.status).equals(Contact.Status.NONE)) {
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			mirror.set(Contact.status, Contact.Status.values()[contact.get(Contact.status)].getMirror().getIndex());
			mirror.set(Contact.statusTime, ts);
		}
		
		return mirror;
	}
	
	public Pair<Entity<Contact>, Entity<Contact>> updateContactState(Entity<User> user, Entity<User> other, Contact.Status status) {
		Entity<Contact> contact = this.getContact(user, other);
		if (contact == null) return null;
		
		Entity<Contact> mirror = getMirror(contact);
		if (mirror == null) return null;
		
		if (status.isValidPrior(contact.get(Contact.status))) {
			contact.set(Contact.status, status.getIndex());
			this.save(contact);
			
			mirror.set(Contact.status, status.getMirror().getIndex());
			this.save(mirror);
			
			return new Pair<Entity<Contact>, Entity<Contact>>(contact, mirror);
		} else {
			return null;
		}
	}
	
	public List<Entity<User>> getContacts(Entity<User> user, Contact.Status status) {
		List<Entity<Contact>> contacts = this.query(new SQLFieldPredicate<Contact, Entity<User>>(Contact.user, user)
				.and(new SQLFieldPredicate<Contact, Integer>(Contact.status, status.getIndex())));

		List<Entity<User>> contactUsers = this.getHelper().getValues(Contact.contact, contacts);
		this.entityService.getManager(User.class, UserManager.class).materialize(contactUsers);
		
		return contactUsers;
	}
	
	public boolean isContact(Entity<User> user, Entity<User> other, Contact.Status status) {
		List<Entity<Contact>> contacts = this.query(new SQLFieldPredicate<Contact, Entity<User>>(Contact.user, user)
				.and(new SQLFieldPredicate<Contact, Entity<User>>(Contact.contact, other))
				.and(new SQLFieldPredicate<Contact, Integer>(Contact.status, status.getIndex())));
		
		return contacts.size() == 1;
	}
	
}
