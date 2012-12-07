package com.qorporation.msgs.server.entity.definition;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.qorporation.msgs.server.entity.definition.UserEvent.ContactAction;
import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class Contact extends SQLTable {
	
	public static enum Status {
		NONE(0, 0, false, new HashSet<Integer>(), ContactAction.INVALID),
		ACTIVE(1, 1, true, new HashSet<Integer>(Arrays.asList(4, 7)), ContactAction.ACCEPTOR),
		DELETOR(2, 3, false, new HashSet<Integer>(Arrays.asList(1, 4, 7)), ContactAction.DELETOR),
		DELETED(3, 2, false, new HashSet<Integer>(), ContactAction.INVALID),
		IGNOROR(4, 5, false, new HashSet<Integer>(Arrays.asList(7)), ContactAction.IGNOROR),
		IGNORED(5, 4, false, new HashSet<Integer>(), ContactAction.INVALID),
		REQUESTOR(6, 7, true, new HashSet<Integer>(Arrays.asList(0, 2, 3)), ContactAction.REQUESTOR),
		REQUESTED(7, 6, false, new HashSet<Integer>(), ContactAction.REQUESTED);

		private Integer index;
		private Integer mirror;
		private Boolean avatarEvent;
		private Set<Integer> validPriors;
		private ContactAction contactAction;
		
		Status(Integer index, Integer mirror, Boolean avatarEvent, Set<Integer> validPriors, ContactAction contactAction) { 
			this.index = index;
			this.mirror = mirror;
			this.avatarEvent = avatarEvent;
			this.validPriors = validPriors;
			this.contactAction = contactAction;
		}
		
		public Integer getIndex() { return this.index; }
		public Status getMirror() { return this.mirror != null ? Status.values()[this.mirror] : this; }
		public boolean isAvatarEvent() { return this.avatarEvent; }
		public boolean isValidPrior(Status prior) { return this.validPriors.contains(prior.getIndex()); }
		public boolean isValidPrior(Integer prior) { return this.validPriors.contains(prior); }
		public ContactAction getEventAction() { return this.contactAction; }
	};
	
	@Indexed
	public static FieldKey<Entity<User>> user;
	
	@Indexed
	public static FieldKey<Entity<User>> contact;
	
	public static FieldKey<Integer> status;
	public static FieldKey<Timestamp> statusTime;
	
	@Indexed(fields={"user", "contact"}, unique=true)
	public static IndexMetaKey userContactContstraint;
	
}
