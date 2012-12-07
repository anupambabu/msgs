package com.qorporation.msgs.client.android.store;

import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.qorporation.msgs.client.android.store.delegates.UserStoreDelegate;

public class UserStore extends Store {
	private static UserStore instance = null;
	public static synchronized UserStore getInstance() {
		if (instance == null) instance = new UserStore(Store.DEFAULT_CONTEXT);
		return instance;
	}
	
	private UserStoreDelegate delegate = null;
	public void setDelegate(UserStoreDelegate delegate) { this.delegate = delegate; }
	
	private UserStore(Context context) {
		super(context, "user.db", Arrays.asList(
				"CREATE TABLE IF NOT EXISTS user (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "name VARCHAR(50), "
				+ "status VARCHAR(255), "
				+ "avatar VARCHAR(255), "
				+ "lastsync INTEGER)",

				"CREATE UNIQUE INDEX IF NOT EXISTS user_id ON user(id);"
		));
	}

	private HashMap<String, Object> rowToItem(Cursor cursor) {
		HashMap<String, Object> val = new HashMap<String, Object>();
		
		try {
			val.put("id", new Integer(cursor.getInt(1)));
			val.put("name", cursor.getString(2));
			val.put("status", cursor.getString(3));
			val.put("avatar", cursor.getString(4));
			val.put("lastsync", new Long(cursor.getLong(5)));
		} catch (Exception e) {
		}
		
		return val;
	}
	
	public boolean setUser(int id, String name, String status, String avatar, long lastSync) {
		boolean ret = false;
		
		try {
            SQLiteStatement s = this.db.compileStatement("INSERT OR REPLACE INTO user(id, name, status, avatar, lastsync) VALUES(?, ?, ?, ?, ?)");
            s.bindLong(1, id);
            s.bindString(2, name);
            s.bindString(3, status);
            s.bindString(4, avatar);
            s.bindLong(5, lastSync);
            
            ret = s.executeInsert() > 0;
            s.close();
            
            if (this.delegate != null) {
            	this.delegate.onUserEvent(this.getByID(id));
            }
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	public boolean setUser(JSONObject val) throws Exception {
		return setUser(val.getInt("user"), val.getString("name"), val.getString("status"), val.getString("avatar"), val.getInt("lastsync"));
	}
    
    public HashMap<String, Object> getByID(int id) {
    	HashMap<String, Object> ret = null;
    	
        try {          
            Cursor c = this.db.query("user", new String[] { "id", "name", "status", "avatar", "lastsync" }, "id = ?", new String[] { Integer.toString(id) }, null, null, null);

            if (c.moveToNext()) {                    
            	this.rowToItem(c);                                
            }

            c.close();
        } catch(Exception e) {
        } 
        
    	return ret;
    }

}
