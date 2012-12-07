package com.qorporation.msgs.client.berry.store;

import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONObject;

import com.qorporation.msgs.client.berry.store.delegates.ContactStoreDelegate;
import com.qorporation.msgs.client.berry.util.ErrorControl;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.Row;
import net.rim.device.api.database.Statement;

public class ContactStore extends Store {
	public static final Integer STATUS_CONTACT = new Integer(1);
	public static final Integer STATUS_REQUEST = new Integer(7);
	public static final Integer STATUS_PENDING = new Integer(6);
	
	private static ContactStore instance = null;
	public static synchronized ContactStore getInstance() {
		if (instance == null) instance = new ContactStore();
		return instance;
	}
	
	private ContactStoreDelegate delegate = null;
	public void setDelegate(ContactStoreDelegate delegate) { this.delegate = delegate; }
	
	private ContactStore() {
		super(new String[] {
				"CREATE TABLE IF NOT EXISTS contact (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "user INTEGER, "
				+ "status INTEGER)",
				
				"CREATE UNIQUE INDEX IF NOT EXISTS contact_id ON contact(id);"
		});
	}
	
	private Hashtable rowToItem(Row row) {
		Hashtable val = new Hashtable();
		
		try {
			val.put("id", new Integer(row.getInteger(0)));
			val.put("user", new Integer(row.getInteger(1)));
			val.put("status", new Integer(row.getInteger(2)));
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return val;
	}
	
	public boolean setContact(int id, int user, int status) {
		boolean ret = false;
		
		try {
            Statement s = this.db.createStatement("INSERT OR REPLACE INTO contact(id, user, status) VALUES(?, ?, ?)");
            s.prepare();
            s.bind(1, id);
            s.bind(2, user);
            s.bind(3, status);
            s.execute();      
            s.close();
            
            ret = this.db.getNumberOfChanges() > 0;
            
            if (this.delegate != null) {
            	this.delegate.onContactEvent(this.getByID(id));
            }
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return ret;
	}
	
	public boolean setContact(JSONObject val) throws Exception {
		return setContact(val.getInt("contact"), val.getInt("user"), val.getInt("status"));
	}
    
    public Hashtable getByID(int id) {
        Hashtable ret = null;
        
        try {          
            Statement s = this.db.createStatement("SELECT id, user, status FROM contact WHERE id = ?");
            s.prepare();
            s.bind(1, id);
            Cursor c = s.getCursor();

            if (c.next()) {                    
            	ret = this.rowToItem(c.getRow());                                
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        } 
        
    	return ret;
    }
    
    public Hashtable getByUser(Integer user) {
        Hashtable ret = null;
        
        try {          
            Statement s = this.db.createStatement("SELECT id, user, status FROM contact WHERE user = ?");
            s.prepare();
            s.bind(1, user.intValue());
            Cursor c = s.getCursor();

            if (c.next()) {                    
            	ret = this.rowToItem(c.getRow());                                
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        } 
        
    	return ret;
    }
    
    public Vector list() {
        Vector list = new Vector();
        
        try {          
            Statement s = this.db.createStatement("SELECT DISTINCT(id), user, status FROM contact ORDER BY id ASC"); 
            s.prepare();
            Cursor c = s.getCursor();

            while (c.next()) {                    
                list.addElement(this.rowToItem(c.getRow()));                                  
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        } 
        
        return list;          
    }
    
    public Vector list(Integer status) {
        Vector list = new Vector();
        
        try {          
            Statement s = this.db.createStatement("SELECT DISTINCT(id), user, status FROM contact WHERE status = ? ORDER BY id ASC");
            s.prepare();
            s.bind(1, status.intValue());
            Cursor c = s.getCursor();

            while (c.next()) {                    
                list.addElement(this.rowToItem(c.getRow()));                                  
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        } 
        
        return list;          
    }
    
    protected void onStopProcessingEventsInternal(long firstEventTime) {
        if (this.delegate != null) {
        	this.delegate.onContactEvents(firstEventTime);
        }   	
    }

}
