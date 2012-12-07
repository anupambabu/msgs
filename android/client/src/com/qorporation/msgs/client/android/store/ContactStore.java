package com.qorporation.msgs.client.android.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.qorporation.msgs.client.android.store.delegates.ContactStoreDelegate;

public class ContactStore extends Store {
	private static ContactStore instance = null;
	public static synchronized ContactStore getInstance() {
		if (instance == null) instance = new ContactStore(Store.DEFAULT_CONTEXT);
		return instance;
	}
	
	private ContactStoreDelegate delegate = null;
	public void setDelegate(ContactStoreDelegate delegate) { this.delegate = delegate; }
	
	private ContactStore(Context context) {
		super(context, "contact.db", Arrays.asList(
				"CREATE TABLE IF NOT EXISTS contact (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "user INTEGER, "
				+ "status INTEGER)",
				
				"CREATE UNIQUE INDEX IF NOT EXISTS contact_id ON contact(id);"
		));
	}
	
	private HashMap<String, Object> rowToItem(Cursor cursor) {
		HashMap<String, Object> val = new HashMap<String, Object>();
		
		try {
			val.put("id", new Integer(cursor.getInt(1)));
			val.put("user", new Integer(cursor.getInt(2)));
			val.put("status", new Integer(cursor.getInt(3)));
		} catch (Exception e) {
		}
		
		return val;
	}
	
	public boolean setContact(int id, int user, int status) {
		boolean ret = false;
		
		try {
            SQLiteStatement s = this.db.compileStatement("INSERT OR REPLACE INTO contact(id, user, status) VALUES(?, ?, ?)");
            s.bindLong(1, id);
            s.bindLong(2, user);
            s.bindLong(3, status);
            
            ret = s.executeInsert() > 0;
            s.close();
            
            if (this.delegate != null) {
            	this.delegate.onContactEvent(this.getByID(id));
            }
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	public boolean setContact(JSONObject val) throws Exception {
		return setContact(val.getInt("contact"), val.getInt("user"), val.getInt("status"));
	}
    
    public HashMap<String, Object> getByID(int id) {
        try {
            Cursor c = this.db.query("contact", new String[] {"id", "user"}, "id = ?", new String[] { Integer.toString(id) }, null, null, null);
            
            if (c.moveToNext()) {                    
            	this.rowToItem(c);                                
            }

            c.close();
        } catch(Exception e) {
        } 
        
    	return null;
    }
    
    public HashMap<String, Object> getByUser(int user) {
    	HashMap<String, Object> ret = null;
    	
        try {          
            Cursor c = this.db.query("contact", new String[] { "id", "user", "status" }, "user = ?", new String[] { Integer.toString(user) }, null, null, null);

            if (c.moveToNext()) {                    
            	ret = this.rowToItem(c);                                
            }

            c.close();
        } catch(Exception e) {
        } 
        
    	return ret;
    }
    
    public List<HashMap<String, Object>> list() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        
        try {          
            Cursor c = this.db.query("contact", new String[] { "id", "user", "status" }, null, null, null, null, null);

            while (c.moveToNext()) {                    
                list.add(this.rowToItem(c));                                  
            }

            c.close();
        } catch(Exception e) {
        } 
        
        return list;          
    }
    
    public List<HashMap<String, Object>> list(int status) {
    	ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        
        try {          
            Cursor c = this.db.query("contact", new String[] { "id", "user", "status" }, "status = ?", new String[] { Integer.toString(status) }, null, null, null);

            while (c.moveToNext()) {                    
                list.add(this.rowToItem(c));                                  
            }

            c.close();
        } catch(Exception e) {
        } 
        
        return list;          
    }

}
