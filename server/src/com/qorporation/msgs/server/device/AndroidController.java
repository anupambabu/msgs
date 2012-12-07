package com.qorporation.msgs.server.device;

import com.google.android.c2dm.server.C2DMessaging;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.util.ErrorControl;

public class AndroidController extends DeviceController {
	
	private C2DMessaging c2dm = null;
	
	public AndroidController() {
		this.c2dm = C2DMessaging.get();
	}
	
	@Override
	protected void push(Entity<UserDevice> device, Entity<UserEvent> event) {
		try {
			this.c2dm.sendWithRetry(device.get(UserDevice.deviceIdentifier), "1", "message", event.getKey(), null, null);
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}

}
