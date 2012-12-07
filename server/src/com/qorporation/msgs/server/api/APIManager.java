package com.qorporation.msgs.server.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicService;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.view.ViewAuthenticator;
import com.qorporation.qluster.view.ViewManager;

public class APIManager extends ViewManager<APIView, User> {

	public APIManager(EntityService entityService, LogicService logicService, ViewAuthenticator<User> authenticator) {
		super(entityService, logicService, authenticator);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) {
		Transaction transaction = this.entityService.startGlobalTransaction();
		
		APIRequest apiRequest = new APIRequest(request, response);
		handle(apiRequest);
		
		transaction.finish();
	}
	
}
