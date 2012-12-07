package com.qorporation.msgs.server.page.handler;

import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.msgs.server.page.PageHandler;
import com.qorporation.msgs.server.page.PageRequest;

public class Home extends PageHandler {
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/"})
	public void index(PageRequest request) {		
		request.renderTemplate("home/index.html");
	}
	
}
