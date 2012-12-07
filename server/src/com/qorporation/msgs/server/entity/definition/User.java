package com.qorporation.msgs.server.entity.definition;

import java.sql.Timestamp;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.typesafety.FieldKey;

public class User extends SQLTable {
	
	@Indexed(unique=true)
	public static FieldKey<String> token;
	
	public static FieldKey<String> name;
	public static FieldKey<String> avatar;
	public static FieldKey<String> status;
	
	@Indexed(unique=true)
	public static FieldKey<String> email;
	public static FieldKey<String> pendingEmail;
	
	public static FieldKey<String> password;
	public static FieldKey<Boolean> isAdmin;
	
	public static FieldKey<Timestamp> lastLogin;
	public static FieldKey<Timestamp> lastSync;

}
