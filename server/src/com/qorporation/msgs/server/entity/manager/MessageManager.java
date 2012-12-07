package com.qorporation.msgs.server.entity.manager;

import java.sql.Timestamp;
import java.util.List;

import com.qorporation.msgs.server.entity.definition.Conversation;
import com.qorporation.msgs.server.entity.definition.Message;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.entity.Entity;

public class MessageManager extends SQLBackedEntityManager<Message> {

	public Entity<Message> create(Entity<Conversation> conversation, Entity<User> user, String body) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());

		Entity<Message> m = this.create();
		m.set(Message.conversation, conversation);
		m.set(Message.sender, user);
		m.set(Message.body, body);
		m.set(Message.time, ts);
		
		if (this.save(m)) {
			return m;
		} else {
			return null;
		}
	}
	
	public List<Entity<Message>> getConversation(Entity<Conversation> conversation) {
		return this.query(Message.conversation, conversation);
	}

}
