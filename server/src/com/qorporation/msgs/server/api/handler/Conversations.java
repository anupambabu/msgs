package com.qorporation.msgs.server.api.handler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.qorporation.msgs.server.api.APIHandler;
import com.qorporation.msgs.server.api.APIRequest;
import com.qorporation.msgs.server.entity.definition.Conversation;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.logic.ConversationLogic;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;

public class Conversations extends APIHandler {

	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/conversations/list"})
	public void list(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("conversations", this.logicService.get(ConversationLogic.class).getConversationListInfo(user));
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/conversations/send/(?<conversation>[0-9]+)"})
	public void send(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		String conversationKey = request.getParameter("conversation");
		Entity<Conversation> conversation = this.logicService.get(ConversationLogic.class).get(conversationKey);
		
		String message = request.getParameter("message");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("result", this.logicService.get(ConversationLogic.class).sendMessage(conversation, user, message));
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/conversations/leave/(?<conversation>[0-9]+)"})
	public void leave(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		String conversationKey = request.getParameter("conversation");
		Entity<Conversation> conversation = this.logicService.get(ConversationLogic.class).get(conversationKey);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("action", "leave");
		result.put("conversation", conversation.getKey());
		result.put("result", this.logicService.get(ConversationLogic.class).leave(conversation, user));
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/conversations/sync"})
	public void sync(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		ArrayList<Object> res = new ArrayList<Object>();
		
		String data = request.getPostString();
		if (data.length() > 0) {
			JSONArray json = JSONArray.fromObject(data);
			for (int i = 0; i < json.size(); i++) {
				JSONObject conv = json.getJSONObject(i);
				Integer id = conv.getInt("id");
				Long lastread = conv.getLong("lastread");
				Long needsync = conv.getLong("needsync");

				Entity<Conversation> conversation = this.logicService.get(ConversationLogic.class).get(id.toString());
				this.logicService.get(ConversationLogic.class).setConversationSync(user, conversation, new Timestamp(lastread));
				
				Map<String, Object> r = new HashMap<String, Object>();
				r.put("id", id);
				r.put("time", needsync);
				res.add(r);
			}
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("result", res);
		
		request.sendResponse(result);
	}
	
}
