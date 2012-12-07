package com.qorporation.msgs.server.device;

import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.qluster.entity.Entity;

public class BlackberryController extends DeviceController {

	@Override
	protected void push(Entity<UserDevice> device, Entity<UserEvent> event) {}

}
