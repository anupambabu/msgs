package com.qorporation.msgs.client.android.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.qorporation.msgs.client.android.store.delegates.ConversationParticipantPrimaryStoreDelegate;
import com.qorporation.msgs.client.android.store.delegates.ConversationStoreDelegate;

public class ConversationStore extends Store {
	private static ConversationStore instance = null;
	public static synchronized ConversationStore getInstance() {
		if (instance == null) instance = new ConversationStore(Store.DEFAULT_CONTEXT);
		return instance;
	}
	
	private ConversationStoreDelegate delegate = null;
	public void setDelegate(ConversationStoreDelegate delegate) { this.delegate = delegate; }

	private ConversationParticipantPrimaryStoreDelegate participantDelegate = null;
	public void setParticipantDelegate(ConversationParticipantPrimaryStoreDelegate participantDelegate) { this.participantDelegate = participantDelegate; }
	
	private ConversationStore(Context context) {
		super(context, "conversation.db", Arrays.asList(
				"CREATE TABLE IF NOT EXISTS conversation (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "privacy INTEGER, "
				+ "lastmessage INTEGER, "
				+ "firstread INTEGER, "
				+ "lastread INTEGER, "
				+ "needsync INTEGER)",

				"CREATE UNIQUE INDEX IF NOT EXISTS conversation_id ON conversation(id);",

				"CREATE TABLE IF NOT EXISTS participant (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "conversation INTEGER, "
				+ "user INTEGER, "
				+ "lastsync INTEGER, "
				+ "jointime INTEGER)",

				"CREATE UNIQUE INDEX IF NOT EXISTS conversation_participant_id ON participant(id, conversation);"
		));
	}

	private HashMap<String, Object> rowToItem(Cursor cursor) {
		HashMap<String, Object> val = new HashMap<String, Object>();
		
		try {
			val.put("id", new Integer(cursor.getInt(1)));
			val.put("privacy", new Integer(cursor.getInt(2)));
			val.put("lastmessage", new Long(cursor.getLong(3)));
			val.put("firstread", new Long(cursor.getLong(4)));
			val.put("lastread", new Long(cursor.getLong(5)));
			val.put("needsync", new Long(cursor.getLong(6)));
		} catch (Exception e) {
		}
		
		return val;
	}
	
	public boolean setConversation(int id, int privacy, long lastMessage) {
		boolean ret = false;
		
		try {
			SQLiteStatement s = this.db.compileStatement("INSERT OR REPLACE INTO conversation(id, privacy, lastmessage, firstread, lastread) VALUES(?, ?, ?, ?, ?)");
            s.bindLong(1, id);
            s.bindLong(2, privacy);
            s.bindLong(3, lastMessage);
            s.bindLong(4, lastMessage - 1);
            s.bindLong(5, lastMessage - 1);
            
            ret = s.executeInsert() > 0;
            s.close();
            
            if (!ret) {
            	SQLiteStatement u = this.db.compileStatement("UPDATE conversation SET lastmessage = ? WHERE id = ?");
                u.bindLong(1, lastMessage);
                u.bindLong(2, id);
                
                ret = u.executeInsert() > 0;
                u.close();
            }
            
            if (this.delegate != null) {
            	this.delegate.onConversationEvent(this.getByID(id));
            }
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	public boolean setConversation(JSONObject val) throws Exception {
		return setConversation(val.getInt("conversation"), val.getInt("privacy"), val.getLong("lastmessage"));
	}
	
	public boolean updateConversation(int id, long firstRead, long lastRead) {
		boolean ret = false;
		
		try {
			SQLiteStatement s = this.db.compileStatement("UPDATE conversation SET firstread = ?, lastread = ?, needsync = ? WHERE id = ?");
            s.bindLong(1, firstRead);
            s.bindLong(2, lastRead);
            s.bindLong(3, System.currentTimeMillis());
            s.bindLong(4, id);
            
            ret = s.executeInsert() > 0;
            s.close();
            
            if (this.delegate != null) {
            	this.delegate.onConversationEvent(this.getByID(id));
            }
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	public boolean setParticipant(int id, int conversation, int user, long lastSync, long joinTime) {
		boolean ret = false;
		
		try {
			SQLiteStatement s = this.db.compileStatement("INSERT OR REPLACE INTO participant(id, conversation, user, lastsync, jointime) VALUES (?, ?, ?, ?, ?");
            s.bindLong(1, id);
            s.bindLong(2, conversation);
            s.bindLong(3, user);
            s.bindLong(4, lastSync);
            s.bindLong(5, joinTime);
            
            ret = s.executeInsert() > 0;
            s.close();
            
            HashMap<String, Object> conv = this.getByID(conversation);
            if (conv != null && ((Integer) conv.get("privacy")).intValue() > 0) {
	            if (this.participantDelegate != null) {
	            	if (this.participantDelegate.listeningParticipant() == id) {
	            		this.participantDelegate.onPrimaryConversationEvent(this.getByID(conversation));
	            	}
	            }
            }
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	public boolean setParticipant(JSONObject val) throws Exception {
		return setParticipant(val.getInt("participant"), val.getInt("conversation"), val.getInt("user"), val.getLong("conversationsync"), val.getLong("jointime"));
	}
	
    public HashMap<String, Object> getByID(int id) {
    	HashMap<String, Object> ret = null;
    	
        try {          
            Cursor c = this.db.query("conversation", new String[] { "id", "privacy", "lastmessage", "firstread", "lastread", "needsync" }, "id = ?", new String[] { Integer.toString(id) }, null, null, null);

            if (c.moveToNext()) {                    
            	ret = this.rowToItem(c);                                
            }

            c.close();
        } catch(Exception e) {
        } 
        
    	return ret;
    }
    
    public HashMap<String, Object> getPrimary(int contact) {
    	HashMap<String, Object> ret = null;
    	
        try {          
            Cursor c = this.db.query("participant", new String[] { "privacy", "conversation" }, "user = ?", new String[] { Integer.toString(contact) }, null, null, null);

            while (c.moveToNext()) {
            	if (c.getInt(1) > 0) {
            		ret = this.getByID(new Integer(c.getInt(2)));
            		break;
            	}
            }

            c.close();
        } catch(Exception e) {
        } 
        
    	return ret;
    }
	
    public List<HashMap<String, Object>> list() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        
        try {          
            Cursor c = this.db.query("conversation", new String[] { "id", "privacy", "lastmessage", "firstread", "lastread", "needsync" }, null, null, null, null, null);

            while(c.moveToNext()) {                    
                list.add(this.rowToItem(c));                                  
            }

            c.close();
        } catch(Exception e) {
        } 
        
        return list;          
    }

}
