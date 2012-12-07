package com.qorporation.msgs.server.device;

import java.io.File;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.util.RelativePath;

public class IPhoneController extends DeviceController {
	private static final String CERT_PASSWORD = "secret";
	private static final String CERT_PATH = RelativePath.root().getAbsolutePath()
											.concat(File.separator).concat("data")
											.concat(File.separator).concat("resources")
											.concat(File.separator).concat("apns")
											.concat(File.separator).concat("certificate.p12").toString();
	
	private ApnsService service = null;
	
	public IPhoneController() {
		try {
			this.service = APNS.newService()
			     .withCert(CERT_PATH, CERT_PASSWORD)
			     .withSandboxDestination()
			     .build();
		} catch (Exception e) {
			this.service = null;
		}
	}
	
	@Override
	protected void push(Entity<UserDevice> device, Entity<UserEvent> event) {
		 String payload = APNS.newPayload().badge(1).alertBody(event.getKey()).build();
		 String token = device.get(UserDevice.deviceIdentifier);
		 
		 if (this.service != null) {
			 this.service.push(token, payload);
		 }
	}

}
