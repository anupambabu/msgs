package com.qorporation.msgs.server.logic;

import java.sql.Timestamp;
import java.util.UUID;

import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.entity.definition.UserToken;
import com.qorporation.msgs.server.entity.manager.UserDeviceManager;
import com.qorporation.msgs.server.entity.manager.UserManager;
import com.qorporation.msgs.server.entity.manager.UserTokenManager;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicController;
import com.qorporation.qluster.view.ViewAuthenticator;
import com.qorporation.qluster.view.ViewRequest;
import com.qorporation.qluster.view.ViewType;

public class AuthenticationLogic extends LogicController implements ViewAuthenticator<User> {

	private UserDeviceManager deviceManager = null;
	private UserManager userManager = null;
	private UserTokenManager tokenManager = null;
	private UserLogic userLogic = null;
	
	@Override
	public void init() {
		this.deviceManager = this.serviceManager.getService(EntityService.class).getManager(UserDevice.class, UserDeviceManager.class);
		this.userManager = this.serviceManager.getService(EntityService.class).getManager(User.class, UserManager.class);
		this.tokenManager = this.serviceManager.getService(EntityService.class).getManager(UserToken.class, UserTokenManager.class);
		this.userLogic  = this.logicService.get(UserLogic.class);
	}
	
	public boolean authenticate(AuthenticationLevel level, Entity<User> user) {
		if (level == AuthenticationLevel.PUBLIC) {
			return true;
		}
		
		if (level == AuthenticationLevel.USER && user != null) {
			return true;
		}
		
		if (level == AuthenticationLevel.ADMIN && user != null) {
			Boolean isAdmin = user.get(User.isAdmin);
			if (isAdmin != null && isAdmin.booleanValue()) {
				return true;
			}
		}
		
		return false;
	}

	public Entity<User> login(String email, String password) {
		Entity<User> user = this.userLogic.getByEmail(email);
		
		if (user != null) {
			String userPassword = user.get(User.password);
			if (userPassword != null && password == null || !password.equals(userPassword)) {
				user = null;
			}
		}
		
		return user;
	}
	
	public Entity<User> register(String email, String password) {
		Entity<User> user = this.userLogic.getByEmail(email);
		
		if (user == null) {
			user = this.userLogic.create(email);
			user.set(User.password, password);
			this.userLogic.save(user);
		}
		
		return user;
	}
	
	public Entity<User> fromToken(String key, String token) {
		if (key == null || token == null) return null;
		
		Entity<User> user = null;
		Entity<UserToken> userToken = this.tokenManager.get(token);
		
		if (userToken != null && userToken.get(UserToken.key).equals(key)) {
			user = userToken.get(UserToken.user);
		}
		
		return user;
	}

	public Entity<UserDevice> deviceLogin(String deviceType, String deviceIdent, String deviceVerify, String authToken) {
		Entity<UserDevice> device = this.deviceManager.getDeviceMapping(deviceType, deviceIdent, deviceVerify);
		
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		
		if (device.get(UserDevice.user) == null) {
			String userToken = UUID.randomUUID().toString().replace("-", "");
			
			Entity<User> user = this.userManager.create();
			user.set(User.token, userToken);
			user.set(User.name, userToken);
			user.set(User.lastLogin, ts);
			user.set(User.lastSync, ts);
			this.userManager.save(user);
			
			device.set(UserDevice.user, user);
			device.set(UserDevice.authToken, UUID.randomUUID().toString().replace("-", ""));
			device.set(UserDevice.authSecret, UUID.randomUUID().toString().replace("-", ""));
			this.deviceManager.save(device);
			
			return device;
		} else {
			if (device.get(UserDevice.authToken).equals(authToken)) {
				if (!device.get(UserDevice.deviceVerifier).equals(deviceVerify)) {
					device.set(UserDevice.deviceVerifier, deviceVerify);
					this.deviceManager.save(device);
				}
				
				Entity<User> user = device.get(UserDevice.user);
				user.set(User.lastLogin, ts);
				this.userManager.save(user);
				
				return device;
			} else if (device.get(UserDevice.deviceVerifier).equals(deviceVerify)) {
				Entity<User> user = device.get(UserDevice.user);
				user.set(User.lastLogin, ts);
				this.userManager.save(user);
				
				return device;
			}
		}
		
		return null;
	}

	@Override
	public <T extends ViewType> Entity<User> getUserFromViewRequest(ViewRequest<T, User> req) {
		String deviceType = req.getParameter("dt");
		String deviceIdent = req.getParameter("di");
		String deviceVerify = req.getParameter("dv");
		String authToken = req.getParameter("at");
		Entity<UserDevice> device = this.deviceManager.getDeviceMapping(deviceType, deviceIdent);
		
		if (device != null && device.getKey() != null 
				&& device.get(UserDevice.deviceVerifier) != null
				&& device.get(UserDevice.deviceVerifier).equalsIgnoreCase(deviceVerify)
				&& device.get(UserDevice.authToken) != null 
				&& device.get(UserDevice.authToken).equalsIgnoreCase(authToken)) {
			Entity<User> user = device.get(UserDevice.user);
			if (user.getKey().equals("0")) {
				return null;
			}
			
			return user;
		}
		
		return null;
	}
	
	public <T extends ViewType> Entity<UserDevice> getDeviceFromViewRequest(ViewRequest<T, User> req) {
		String deviceType = req.getParameter("dt");
		String deviceIdent = req.getParameter("di");
		String deviceVerify = req.getParameter("dv");
		String authToken = req.getParameter("at");
		Entity<UserDevice> device = this.deviceManager.getDeviceMapping(deviceType, deviceIdent, deviceVerify);
		
		if (device != null && device.getKey() != null && device.get(UserDevice.authToken) != null && device.get(UserDevice.authToken).equals(authToken)) {
			return device;
		}
		
		return null;
	}
	
}
