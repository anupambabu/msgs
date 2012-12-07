package com.qorporation.msgs.server.api.handler;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.qorporation.msgs.server.api.APIHandler;
import com.qorporation.msgs.server.api.APIRequest;
import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.logic.AuthenticationLogic;
import com.qorporation.msgs.server.logic.UserLogic;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;

public class Auth extends APIHandler {
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/device/(?<devicetype>[A-Za-z0-9_]*)/(?<deviceident>[-A-Za-z0-9_]*)/(?<deviceverify>[-A-Za-z0-9_]*)/?(?<authtoken>[-A-Za-z0-9_]*)"})
	public void device(APIRequest request) {		
		String deviceType = request.getParameter("devicetype");
		String deviceIdent = request.getParameter("deviceident");
		String deviceVerify = request.getParameter("deviceverify");
		String authToken = request.getParameter("authtoken");
		
		Entity<UserDevice> device = this.logicService.get(AuthenticationLogic.class).deviceLogin(deviceType, deviceIdent, deviceVerify, authToken);
		
		Map<String, Object> result = new HashMap<String, Object>();
		if (device != null) {
			result.put("status", "ok");
			result.put("user_id", device.get(UserDevice.user).getKey());
			result.put("user_name", device.get(UserDevice.user).get(User.name));
			result.put("user_email", device.get(UserDevice.user).get(User.email));
			result.put("user_email_pending", device.get(UserDevice.user).get(User.pendingEmail));
			result.put("user_phone", device.get(UserDevice.phone));
			result.put("user_phone_pending", device.get(UserDevice.pendingPhone));
			result.put("auth_token", device.get(UserDevice.authToken));
			result.put("auth_secret", device.get(UserDevice.authSecret));
			
			this.logicService.get(UserLogic.class).sendAvatarEvent(device.get(UserDevice.user), device.get(UserDevice.user), new Timestamp(System.currentTimeMillis()));
		} else {
			result.put("status", "error");
		}
		
		request.sendResponse(result);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/update"})
	public void update(APIRequest request) {		
		Entity<UserDevice> device = this.logicService.get(AuthenticationLogic.class).getDeviceFromViewRequest(request);
		
		String userName = request.getParameter("name");
		String userEmail = request.getParameter("email");
		String userPhone = request.getParameter("phone");
		
		Map<String, Object> result = new HashMap<String, Object>();
		if (device != null && this.logicService.get(UserLogic.class).updateAccount(device, userName, userEmail, userPhone)) {
			result.put("status", "ok");
			result.put("user_id", device.get(UserDevice.user).getKey());
			result.put("user_name", device.get(UserDevice.user).get(User.name));
			result.put("user_email", device.get(UserDevice.user).get(User.email));
			result.put("user_email_pending", device.get(UserDevice.user).get(User.pendingEmail));
			result.put("user_phone", device.get(UserDevice.phone));
			result.put("user_phone_pending", device.get(UserDevice.pendingPhone));
			result.put("auth_token", device.get(UserDevice.authToken));
			result.put("auth_secret", device.get(UserDevice.authSecret));
			
			this.logicService.get(UserLogic.class).sendAvatarEvent(device.get(UserDevice.user), device.get(UserDevice.user), new Timestamp(System.currentTimeMillis()));
		} else {
			result.put("status", "error");
		}
		
		request.sendResponse(result);
	}
	
}
