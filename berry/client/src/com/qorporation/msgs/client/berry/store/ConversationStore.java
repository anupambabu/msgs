package com.qorporation.msgs.client.berry.store;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.global.Formatter;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qorporation.msgs.client.berry.networking.HttpRequest;
import com.qorporation.msgs.client.berry.networking.HttpRequestDelegate;
import com.qorporation.msgs.client.berry.store.delegates.ConversationParticipantPrimaryStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.ConversationParticipantStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.ConversationStoreDelegate;
import com.qorporation.msgs.client.berry.util.ErrorControl;
import com.qorporation.msgs.client.berry.util.JSONHelper;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.Row;
import net.rim.device.api.database.Statement;

public class ConversationStore extends Store {
	private static ConversationStore instance = null;
	public static synchronized ConversationStore getInstance() {
		if (instance == null) instance = new ConversationStore();
		return instance;
	}
	
	private ConversationStoreDelegate delegate = null;
	public void setDelegate(ConversationStoreDelegate delegate) { this.delegate = delegate; }
	
	private ConversationParticipantStoreDelegate participantDelegate = null;
	public void setParticipantDelegate(ConversationParticipantStoreDelegate participantDelegate) { this.participantDelegate = participantDelegate; }
	
	private ConversationParticipantPrimaryStoreDelegate primaryParticipantDelegate = null;
	public void setPrimaryParticipantDelegate(ConversationParticipantPrimaryStoreDelegate participantDelegate) { this.primaryParticipantDelegate = participantDelegate; }
	
	private ConversationStore() {
		super(new String[] {
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
		});
	}

	private Hashtable conversationRowToItem(Row row) {
		Hashtable val = new Hashtable();
		
		try {
			val.put("id", new Integer(row.getInteger(0)));
			val.put("privacy", new Integer(row.getInteger(1)));
			val.put("lastmessage", new Long(row.getLong(2)));
			val.put("firstread", new Long(row.getLong(3)));
			val.put("lastread", new Long(row.getLong(4)));
			val.put("needsync", new Long(row.getLong(5)));
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return val;
	}
	
	private Hashtable participantRowToItem(Row row) {
		Hashtable val = new Hashtable();
		
		try {
			val.put("id", new Integer(row.getInteger(0)));
			val.put("conversation", new Integer(row.getInteger(1)));
			val.put("user", new Integer(row.getInteger(2)));
			val.put("lastsync", new Long(row.getLong(3)));
			val.put("jointime", new Long(row.getLong(4)));
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return val;
	}
	
	public boolean setConversation(int id, int privacy, long lastMessage) {
		boolean ret = false;
		
		try {
            Statement s = this.db.createStatement("INSERT OR IGNORE INTO conversation(id, privacy, lastmessage, firstread, lastread, needsync) VALUES(?, ?, ?, ?, ?, ?)");
	        s.prepare();
	        s.bind(1, id);
	        s.bind(2, privacy);
	        s.bind(3, lastMessage);
	        s.bind(4, lastMessage - 1);
	        s.bind(5, lastMessage - 1);
	        s.bind(6, 0);
	        s.execute();
	        
            ret = this.db.getNumberOfChanges() > 0;
            
            if (!ret) {
                Statement u = this.db.createStatement("UPDATE conversation SET lastmessage = ? WHERE id = ?");
                u.prepare();
                u.bind(1, lastMessage);
                u.bind(2, id);
                u.execute();      
                u.close();
                
                ret = this.db.getNumberOfChanges() > 0;           	
            }
            
            if (this.delegate != null) {
            	this.delegate.onConversationEvent(this.getByID(id));
            }
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return ret;
	}
	
	public boolean setConversation(JSONObject val) throws Exception {
		return setConversation(val.getInt("conversation"), val.getInt("privacy"), val.getLong("lastmessage"));
	}
	
	public boolean updateConversation(int id, long firstRead, long lastRead) {
		boolean ret = false;
		
		try {
            Statement s = this.db.createStatement("UPDATE conversation SET firstread = ?, lastread = ?, needsync = ? WHERE id = ?");
            s.prepare();
            s.bind(1, firstRead);
            s.bind(2, lastRead);
            s.bind(3, System.currentTimeMillis());
            s.bind(4, id);
            s.execute();      
            s.close();
            
            ret = this.db.getNumberOfChanges() > 0; 
            
            if (this.delegate != null) {
            	this.delegate.onConversationEvent(this.getByID(id));
            }
            
            if (ret) {
            	this.synchronize();
            }
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return ret;
	}
	
	private void synchronize() {
        JSONArray conversations = new JSONArray();
        
        try {          
            Statement s = this.db.createStatement("SELECT * FROM conversation WHERE needsync > 0"); 
            s.prepare();
            Cursor c = s.getCursor();

            while(c.next()) {                    
            	conversations.put(JSONHelper.fromHashtable(this.conversationRowToItem(c.getRow())));                                  
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        }
        
        if (conversations.length() > 0) {
	        URLEncodedPostData encoder = new URLEncodedPostData("UTF-8", false);
	        encoder.append("m", conversations.toString());
	        String encoded = encoder.toString().substring(2);
			
			HttpRequest req = new HttpRequest(Formatter.formatMessage(
					HttpRequest.MSGS_SYNC_CONVERSATIONS_FORMAT,
					new String[] {
						AccountStore.getInstance().getRequestAuthentication()
					}), 
					encoded,
					new HttpRequestDelegate() {
						public void onRequestFailure(HttpRequest request) {}
						public void onRequestReceiving(int received, int total, HttpRequest request) {}
						public void onRequestSending(int sent, int total, HttpRequest request) {}
						public void onRequestComplete(String response, HttpRequest request) {
							try {
								JSONObject json = new JSONObject(response);
								JSONArray synchronizations = json.getJSONArray("result");
								for (int i = 0; i < synchronizations.length(); i++) {
						            Statement s = ConversationStore.this.db.createStatement("UPDATE conversation SET needsync = ? WHERE id = ? AND needsync = ?");
						            s.prepare();
						            s.bind(1, 0);
						            s.bind(2, ((JSONObject) synchronizations.get(i)).getInt("id"));
						            s.bind(3, ((JSONObject) synchronizations.get(i)).getLong("time"));
						            s.execute();      
						            s.close();									
								}
							} catch (Exception e) {
								ErrorControl.logException(e);
							}
						}
					});
			
			req.send();
        }
	}

	public boolean setParticipant(int id, int conversation, int user, long lastSync, long joinTime) {
		boolean ret = false;
		
		try {
            Statement s = this.db.createStatement("INSERT OR REPLACE INTO participant(id, conversation, user, lastsync, jointime) VALUES (?, ?, ?, ?, ?)");
            s.prepare();
            s.bind(1, id);
            s.bind(2, conversation);
            s.bind(3, user);
            s.bind(4, lastSync);
            s.bind(5, joinTime);
            s.execute();      
            s.close();
            
            ret = this.db.getNumberOfChanges() > 0;
            
            Hashtable conv = this.getByID(conversation);
            if (conv != null && ((Integer) conv.get("privacy")).intValue() > 0) {
	            if (this.primaryParticipantDelegate != null) {
	            	if (this.primaryParticipantDelegate.listeningParticipant() == user) {
	            		this.primaryParticipantDelegate.onPrimaryConversationEvent(this.getByID(conversation));
	            	}
	            }
            }
            
            if (this.participantDelegate != null) {
            	if (this.participantDelegate.listeningConversation() == conversation) {
            		this.participantDelegate.onConversationParticipantEvent(this.getByID(id));
            	}
            }
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return ret;
	}
	
	public boolean setParticipant(JSONObject val) throws Exception {
		return setParticipant(val.getInt("participant"), val.getInt("conversation"), val.getInt("user"), val.getLong("conversationsync"), val.getLong("jointime"));
	}
	
    public Hashtable getByID(int id) {
        Hashtable ret = null;
        
        try {          
            Statement s = this.db.createStatement("SELECT id, privacy, lastmessage, firstread, lastread, needsync FROM conversation WHERE id = ?");
            s.prepare();
            s.bind(1, id);
            Cursor c = s.getCursor();

            if (c.next()) {                    
            	ret = this.conversationRowToItem(c.getRow());                                
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        } 
        
    	return ret;
    }
    
    public Hashtable getPrimary(int contact) {
    	Hashtable ret = null;
    	
        try {          
            Statement s = this.db.createStatement("SELECT DISTINCT(conversation) FROM participant WHERE user = ?");
            s.prepare();
            s.bind(1, contact);
            Cursor c = s.getCursor();

            while (c.next()) {         
            	Row r = c.getRow();
            	if (r.getInteger(0) > 0) {
            		ret = this.getByID(r.getInteger(0));
            		break;
            	}
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
            Statement s = this.db.createStatement("SELECT DISTINCT(id), privacy, lastmessage, firstread, lastread, needsync FROM conversation ORDER BY id ASC"); 
            s.prepare();
            Cursor c = s.getCursor();

            while (c.next()) {                    
                list.addElement(this.conversationRowToItem(c.getRow()));                                  
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        } 
        
        return list;          
    }
    
    public long getNewestMessageTime() {
    	long time = 0l;
    	
        try {          
            Statement s = this.db.createStatement("SELECT MAX(lastmessage) FROM conversation"); 
            s.prepare();
            Cursor c = s.getCursor();

            if (c.next()) {
            	time = c.getRow().getLong(0);	
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        } 
        
    	return time;    	
    }
    
    public int getUnreadConversationCount() {
    	int count = 0;
    	
        try {          
            Statement s = this.db.createStatement("SELECT COUNT(id) FROM (SELECT DISTINCT(id) FROM conversation WHERE lastmessage > lastread)"); 
            s.prepare();
            Cursor c = s.getCursor();

            if (c.next()) {
            	count = c.getRow().getInteger(0);	
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        } 
        
    	return count;
    }

	public Vector getParticipants(int conversation) {
        Vector list = new Vector();
        
        try {          
            Statement s = this.db.createStatement("SELECT DISTINCT(id), conversation, user, lastsync, jointime FROM participant WHERE conversation = ? ORDER BY id ASC"); 
            s.prepare();
            s.bind(1, conversation);
            Cursor c = s.getCursor();

            while(c.next()) {                    
                list.addElement(this.participantRowToItem(c.getRow()));                                  
            }
            
            s.close();
            c.close();
        } catch(Exception e) {
        	ErrorControl.logException(e);
        } 
        
        return list;   
	}

}
