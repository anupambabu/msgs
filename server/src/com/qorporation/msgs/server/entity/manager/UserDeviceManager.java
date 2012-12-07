package com.qorporation.msgs.server.entity.manager;

import java.util.List;

import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.entity.Entity;

public class UserDeviceManager extends SQLBackedEntityManager<UserDevice> {

	public Entity<UserDevice> getDeviceMapping(String deviceType, String deviceIdent) {
		List<Entity<UserDevice>> devices = this.query(new SQLFieldPredicate<UserDevice, String>(UserDevice.deviceType, deviceType)
				.and(new SQLFieldPredicate<UserDevice, String>(UserDevice.deviceIdentifier, deviceIdent)));
		if (devices.size() == 1) {
			return devices.get(0);
		} else {
			return null;
		}
	}
	
	public Entity<UserDevice> getDeviceMapping(String deviceType, String deviceIdent, String deviceVerify) {
		List<Entity<UserDevice>> devices = this.query(new SQLFieldPredicate<UserDevice, String>(UserDevice.deviceType, deviceType)
				.and(new SQLFieldPredicate<UserDevice, String>(UserDevice.deviceIdentifier, deviceIdent)));
		if (devices.size() == 1) {
			return devices.get(0);
		} else {
			Entity<UserDevice> device = this.create();
			device.set(UserDevice.deviceType, deviceType);
			device.set(UserDevice.deviceIdentifier, deviceIdent);
			device.set(UserDevice.deviceVerifier, deviceVerify);
			if (this.save(device)) {
				return device;
			} else {
				return getDeviceMapping(deviceType, deviceIdent, deviceVerify);
			}
		}
	}
	
}
