package com.qorporation.msgs.server.page;

import java.lang.reflect.Method;
import java.util.List;

import com.google.code.regexp.NamedPattern;

import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.qluster.view.ViewAuthenticator;
import com.qorporation.qluster.view.ViewHandler;
import com.qorporation.qluster.view.ViewMethod;

public class PageMethod extends ViewMethod<PageView, User> {

	public PageMethod(Method method, ViewHandler<PageView, User> handler,
			List<NamedPattern> patterns, ViewAuthenticator<User> authenticator) {
		super(method, handler, patterns, authenticator);
	}

}
