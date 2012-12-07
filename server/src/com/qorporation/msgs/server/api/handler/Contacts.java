package com.qorporation.msgs.server.api.handler;

import java.util.HashMap;
import java.util.Map;

import com.qorporation.msgs.server.api.APIHandler;
import com.qorporation.msgs.server.api.APIRequest;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.logic.ContactLogic;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;

public class Contacts extends APIHandler {
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/contacts/list"})
	public void list(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("contacts", this.logicService.get(ContactLogic.class).getContactListInfo(user));
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/contacts/request/(?<contact>[A-Za-z0-9_]*)"})
	public void request(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		String contactKey = request.getParameter("contact");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("result", this.logicService.get(ContactLogic.class).requestContact(user, contactKey));
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/contacts/request/email/(?<email>[A-Za-z0-9_\\-\\+@\\.]*)"})
	public void requestByEmail(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		String email = request.getParameter("email");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("result", this.logicService.get(ContactLogic.class).requestContactByEmail(user, email));
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/contacts/request/phone/(?<phone>[0-9_-]*)"})
	public void requestByPhone(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		String phone = request.getParameter("phone");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("result", this.logicService.get(ContactLogic.class).requestContactByPhone(user, phone));
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/contacts/accept/(?<contact>[A-Za-z0-9_]*)"})
	public void accept(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		String contactKey = request.getParameter("contact");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("result", this.logicService.get(ContactLogic.class).acceptContact(user, contactKey));
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/contacts/ignore/(?<contact>[A-Za-z0-9_]*)"})
	public void ignore(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		String contactKey = request.getParameter("contact");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("result", this.logicService.get(ContactLogic.class).ignoreContact(user, contactKey));
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/contacts/delete/(?<contact>[A-Za-z0-9_]*)"})
	public void delete(APIRequest request) {		
		Entity<User> user = request.getUser();
		
		String contactKey = request.getParameter("contact");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", user.getKey());
		result.put("result", this.logicService.get(ContactLogic.class).deleteContact(user, contactKey));
		
		request.sendResponse(result);
	}
	
}
