package com.qorporation.msgs.server.api;

import java.lang.reflect.Method;
import java.util.List;

import com.google.code.regexp.NamedPattern;

import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.view.ViewAuthenticator;
import com.qorporation.qluster.view.ViewHandler;
import com.qorporation.qluster.view.ViewMethod;
import com.qorporation.qluster.view.ViewRequest;

public class APIMethod extends ViewMethod<APIView, User> {

	public APIMethod(Method method, ViewHandler<APIView, User> handler,
			List<NamedPattern> patterns, ViewAuthenticator<User> authenticator) {
		super(method, handler, patterns, authenticator);
	}
	
	public boolean authenticate(ViewRequest<APIView, User> req) {
		Entity<User> user = req.getUser();
		
		if (user == null || user.getKey() == null || user.getKey().equals("0")) {
			user = authenticator.getUserFromViewRequest(req);
			req.setUser(user);
		}
		
		return authenticator.authenticate(method.getAnnotation(AuthenticationPolicy.class).level(), user);
	}

}
