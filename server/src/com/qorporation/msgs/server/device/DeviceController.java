package com.qorporation.msgs.server.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.messenger.MessengerService;
import com.qorporation.qluster.entity.Entity;

public abstract class DeviceController {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void deliverEvent(MessengerService messengerService, Entity<UserDevice> device, Entity<UserEvent> event) {
		messengerService.deliverEvent(device, event);
		this.push(device, event);
	}
	
	protected abstract void push(Entity<UserDevice> device, Entity<UserEvent> event);
	
}
