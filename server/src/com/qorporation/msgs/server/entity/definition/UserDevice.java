package com.qorporation.msgs.server.entity.definition;

import com.qorporation.msgs.server.device.AndroidController;
import com.qorporation.msgs.server.device.BlackberryController;
import com.qorporation.msgs.server.device.DeviceController;
import com.qorporation.msgs.server.device.IPhoneController;
import com.qorporation.msgs.server.device.NoneController;
import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class UserDevice extends SQLTable {

	public static enum DeviceType {
		NONE(new NoneController()),
		BLACKBERRY(new BlackberryController()),
		IPHONE(new IPhoneController()),
		ANDROID(new AndroidController());
		
		private String name;
		private DeviceController deviceController;
		
		DeviceType(DeviceController deviceController) { 
			this.name = this.name().toLowerCase();
			this.deviceController = deviceController;
		}
		
		public String getName() { return this.name; }
		
		public static DeviceController getController(String typeName) { return DeviceType.valueOf(typeName.toUpperCase()).deviceController; }
	};
	
	@Indexed
	public static FieldKey<Entity<User>> user;
	
	public static FieldKey<String> deviceType;
	public static FieldKey<String> deviceIdentifier;
	public static FieldKey<String> deviceVerifier;
	
	public static FieldKey<String> authToken;
	public static FieldKey<String> authSecret;
	
	public static FieldKey<String> phone;
	
	public static FieldKey<String> pendingPhone;
	
	@Indexed(fields={"deviceType", "deviceIdentifier"}, unique=true)
	public static IndexMetaKey deviceIdentifierLookup;
	
}