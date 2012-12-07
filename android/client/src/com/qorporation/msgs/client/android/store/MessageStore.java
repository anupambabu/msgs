package com.qorporation.msgs.client.android.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.qorporation.msgs.client.android.store.delegates.MessageStoreDelegate;

public class MessageStore extends Store {
	private static MessageStore instance = null;
	public static synchronized MessageStore getInstance() {
		if (instance == null) instance = new MessageStore(Store.DEFAULT_CONTEXT);
		return instance;
	}
	
	private MessageStoreDelegate delegate = null;
	public void setDelegate(MessageStoreDelegate delegate) { this.delegate = delegate; }
	
	private MessageStore(Context context) {
		super(context, "message.db", Arrays.asList(
				"CREATE TABLE IF NOT EXISTS message (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "sender INTEGER, "
				+ "conversation INTEGER, "
				+ "time INTEGER, "
				+ "body VARCHAR(512))",

				"CREATE UNIQUE INDEX IF NOT EXISTS message_id ON message(id);"
		));
	}

	private HashMap<String, Object> rowToItem(Cursor cursor) {
		HashMap<String, Object> val = new HashMap<String, Object>();
		
		try {
			val.put("id", new Integer(cursor.getInt(1)));
			val.put("sender", new Integer(cursor.getInt(2)));
			val.put("conversation", new Integer(cursor.getInt(3)));
			val.put("time", new Long(cursor.getLong(4)));
			val.put("body", cursor.getString(5));
		} catch (Exception e) {
		}
		
		return val;
	}
	
	public boolean setMessage(int id, int sender, int conversation, long time, String body) {
		boolean ret = false;
		
		try {
			SQLiteStatement s = this.db.compileStatement("INSERT OR REPLACE INTO message(id, sender, conversation, time, body) VALUES(?, ?, ?, ?, ?)");
            s.bindLong(1, id);
            s.bindLong(2, sender);
            s.bindLong(3, conversation);
            s.bindLong(4, time);
            s.bindString(5, body);
            
            ret = s.executeInsert() > 0;
            s.close();
            
            if (this.delegate != null) {
            	if (this.delegate.listeningConversation() == -1 
            		|| this.delegate.listeningConversation() == conversation) {
            		this.delegate.onMessageEvent(this.getByID(id));
            	}
            }
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	public boolean setMessage(JSONObject val) throws Exception {
		return setMessage(val.getInt("message"), val.getInt("sender"), val.getInt("conversation"), val.getLong("time"), val.getString("body"));
	}
    
    public HashMap<String, Object> getByID(int id) {
        try {          
            Cursor c = this.db.query("message", new String[] { "id", "sender", "conversation", "time", "body" }, "id = ?", new String[] { Integer.toString(id) }, null, null, null);

            if (c.moveToNext()) {                    
            	this.rowToItem(c);                                
            }

            c.close();
        } catch(Exception e) {
        } 
        
    	return null;
    }
    
    public HashMap<String, Object> getMostRecentForConversation(int conversation) {
        try {          
            Cursor c = this.db.query("message", new String[] { "id", "sender", "conversation", "time", "body" }, "conversation = ?", new String[] { Integer.toString(conversation) }, null, null, "time DESC");

            if (c.moveToNext()) {                    
            	this.rowToItem(c);                                
            }

            c.close();
        } catch(Exception e) {
        } 
        
    	return null;
    }
    
    public List<HashMap<String, Object>> messagesForConversation(int conversation) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        
        try {          
            Cursor c = this.db.query("message", new String[] { "id", "sender", "conversation", "time", "body" }, "conversation = ?", new String[] { Integer.toString(conversation) }, null, null, "time DESC");

            while (c.moveToNext()) {                    
                list.add(this.rowToItem(c));                                  
            }

            c.close();
        } catch(Exception e) {
        } 
        
        return list;          
    }
    
    public List<HashMap<String, Object>> messagesForConversationAfter(int conversation, long time) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        
        try {          
            Cursor c = this.db.query("message", new String[] { "id", "sender", "conversation", "time", "body" }, "conversation = ? AND time > ?", new String[] { Integer.toString(conversation), Long.toString(time) }, null, null, "time DESC");

            while (c.moveToNext()) {                    
                list.add(this.rowToItem(c));                                  
            }

            c.close();
        } catch(Exception e) {
        } 
        
        return list;          
    }

}
