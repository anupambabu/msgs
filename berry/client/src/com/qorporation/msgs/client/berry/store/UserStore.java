package com.qorporation.msgs.client.berry.store;

import java.util.Hashtable;

import org.json.JSONObject;

import com.qorporation.msgs.client.berry.store.delegates.UserStoreDelegate;
import com.qorporation.msgs.client.berry.util.ErrorControl;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.Row;
import net.rim.device.api.database.Statement;

public class UserStore extends Store {
	private static UserStore instance = null;
	public static synchronized UserStore getInstance() {
		if (instance == null) instance = new UserStore();
		return instance;
	}
	
	private UserStoreDelegate delegate = null;
	public void setDelegate(UserStoreDelegate delegate) { this.delegate = delegate; }
	
	private UserStore() {
		super(new String[] {
				"CREATE TABLE IF NOT EXISTS user (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "name VARCHAR(50), "
				+ "status VARCHAR(255), "
				+ "avatar VARCHAR(255), "
				+ "lastsync INTEGER)",

				"CREATE UNIQUE INDEX IF NOT EXISTS user_id ON user(id);"
		});
	}

	private Hashtable rowToItem(Row row) {
		Hashtable val = new Hashtable();
		
		try {
			val.put("id", new Integer(row.getInteger(0)));
			val.put("name", row.getString(1));
			val.put("status", row.getString(2));
			val.put("avatar", row.getString(3));
			val.put("lastsync", new Long(row.getLong(4)));
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return val;
	}
	
	public boolean setUser(int id, String name, String status, String avatar, long lastSync) {
		boolean ret = false;
		
		try {
            Statement s = this.db.createStatement("INSERT OR REPLACE INTO user(id, name, status, avatar, lastsync) VALUES(?, ?, ?, ?, ?)");
            s.prepare();
            s.bind(1, id);
            s.bind(2, name);
            s.bind(3, status);
            s.bind(4, avatar);
            s.bind(5, lastSync);
            s.execute();      
            s.close();
            
            ret = this.db.getNumberOfChanges() > 0;
            
            if (this.delegate != null) {
            	this.delegate.onUserEvent(this.getByID(id));
            }
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return ret;
	}
	
	public boolean setUser(JSONObject val) throws Exception {
		return setUser(val.getInt("user"), val.getString("name"), val.getString("status"), val.getString("avatar"), val.getLong("lastsync"));
	}
    
    public Hashtable getByID(int id) {
        Hashtable ret = null;
        
        try {          
            Statement s = this.db.createStatement("SELECT id, name, status, avatar, lastsync FROM user WHERE id = ?");
            s.prepare();
            s.bind(1, id);
            Cursor c = s.getCursor();

            if (c.next()) {                    
            	ret = this.rowToItem(c.getRow());                                
            }
            
            s.close();
            c.close();
        } catch (Exception e) {
        	ErrorControl.logException(e);
        } 
        
    	return ret;
    }

}
