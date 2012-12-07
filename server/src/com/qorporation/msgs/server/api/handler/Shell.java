package com.qorporation.msgs.server.api.handler;

import javax.servlet.http.HttpServletResponse;

import bsh.Interpreter;
import net.sf.json.JSONSerializer;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.msgs.server.api.APIHandler;
import com.qorporation.msgs.server.api.APIRequest;

public class Shell extends APIHandler {

	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/shell"})
	public void exec(APIRequest request) {
		String script = request.getParameter("script");
		
		try {
			Interpreter i = new Interpreter();
			i.set("logicService", this.logicService);
			Object res = i.eval(script);
			
			this.logger.info(String.format("\nExecuted script: %s\nResponse: %s", 
											script, 
											JSONSerializer.toJSON(res)));
			
			request.sendResponse(res);
		} catch (Exception e) {
			ErrorControl.logException(e);
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
}
