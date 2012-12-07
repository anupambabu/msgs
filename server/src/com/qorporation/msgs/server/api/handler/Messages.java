package com.qorporation.msgs.server.api.handler;

import java.util.HashMap;
import java.util.Map;

import com.qorporation.msgs.server.api.APIHandler;
import com.qorporation.msgs.server.api.APIRequest;
import com.qorporation.msgs.server.entity.definition.Message;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.logic.ConversationLogic;
import com.qorporation.msgs.server.logic.UserLogic;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;

public class Messages extends APIHandler {

	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/messages/send/(?<contact>[A-Za-z0-9_]*)"})
	public void send(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		String contactKey = request.getParameter("contact");
		Entity<User> contact = this.logicService.get(UserLogic.class).get(contactKey);
		
		String message = request.getParameter("message");
		Entity<Message> sentMesasge = this.logicService.get(ConversationLogic.class).sendMessageDirectly(user, contact, message);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("action", "send");
		result.put("conversation", sentMesasge.get(Message.conversation).getKey());
		result.put("result", sentMesasge != null);
		
		request.sendResponse(result);
	}
	
}
