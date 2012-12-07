package com.qorporation.msgs.server.entity.definition;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.typesafety.FieldKey;

public class UserToken extends SQLTable {
	
	@Indexed
	public static FieldKey<Entity<User>> user;
	
	@Indexed
	public static FieldKey<String> key;
	
	public static FieldKey<String> hash;	

}
