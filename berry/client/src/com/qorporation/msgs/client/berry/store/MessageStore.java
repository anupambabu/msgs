package com.qorporation.msgs.client.berry.store;

import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONObject;

import com.qorporation.msgs.client.berry.store.delegates.MessageStoreDelegate;
import com.qorporation.msgs.client.berry.util.ErrorControl;

import net.rim.blackberry.api.messagelist.ApplicationMessage;
import net.rim.blackberry.api.messagelist.ApplicationMessageFolder;
import net.rim.blackberry.api.messagelist.ApplicationMessageFolderRegistry;
import net.rim.device.api.collection.util.SortedReadableList;
import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.Row;
import net.rim.device.api.database.Statement;
import net.rim.device.api.util.Comparator;

public class MessageStore extends Store {
	private static final boolean USE_MESSAGE_FOLDER = false;
	
	private static MessageStore instance = null;
	public static synchronized MessageStore getInstance() {
		if (instance == null) instance = new MessageStore();
		return instance;
	}
	
	public class AppMessage implements ApplicationMessage {
		public int conversation = -1;
		public String contact = null;
		public String text = null;
		public String subject = null;
		public long timestamp = -1;
		public int status = -1;
		public int type = -1;
		
		public AppMessage(int conversation, String contact, String text, String subject, long timestamp, int status, int type) {
			this.conversation = conversation;
			this.contact = contact;
			this.text = text;
			this.subject = subject;
			this.timestamp = timestamp;
			this.status = status;
			this.type = type;
		}
		
		public String getContact() { return this.contact; }
		public Object getCookie(int cookieId) { return null; }
		public Object getPreviewPicture() { return null; }
		public String getPreviewText() { return this.text; }
		public int getStatus() { return this.status; }
		public String getSubject() { return this.subject; }
		public long getTimestamp() { return this.timestamp; }
		public int getType() { return this.type; }
	}
	
	private class MessageList extends SortedReadableList {
		public MessageList() {
			super(new Comparator() {
				public int compare(Object msg1, Object msg2) {
					long t1 = ((AppMessage) msg1).timestamp;
					long t2 = ((AppMessage) msg2).timestamp;
					return t1 < t2 ? -1 : t1 == t2 ? 0 : 1;
				}
			});
		}
		
		public void add(AppMessage msg) { this.doAdd(msg); }
	}
	
	private static long MESSAGE_FOLDER_ID = 0x61131337;
	private ApplicationMessageFolder messageFolder = null;
	private MessageList messageList = null;
	
	private MessageStoreDelegate delegate = null;
	public void setDelegate(MessageStoreDelegate delegate) { this.delegate = delegate; }
	
	private MessageStore() {
		super(new String[] {
				"CREATE TABLE IF NOT EXISTS message (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "sender INTEGER, "
				+ "conversation INTEGER, "
				+ "time INTEGER, "
				+ "body VARCHAR(512))",

				"CREATE UNIQUE INDEX IF NOT EXISTS message_id ON message(id);"
		});
		
		if (USE_MESSAGE_FOLDER) {
			this.messageFolder = ApplicationMessageFolderRegistry.getInstance().getApplicationFolder(MESSAGE_FOLDER_ID);
			if (this.messageFolder == null) {
				this.messageList = new MessageList();
				this.messageFolder = ApplicationMessageFolderRegistry.getInstance().registerFolder(MESSAGE_FOLDER_ID, "Msgs", this.messageList);
			} else {
				this.messageList = (MessageList) this.messageFolder.getMessages();
			}
		}
	}

	private Hashtable rowToItem(Row row) {
		Hashtable val = new Hashtable();
		
		try {
			val.put("id", new Integer(row.getInteger(0)));
			val.put("sender", new Integer(row.getInteger(1)));
			val.put("conversation", new Integer(row.getInteger(2)));
			val.put("time", new Long(row.getLong(3)));
			val.put("body", row.getString(4));
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return val;
	}
	
	public boolean setMessage(int id, int sender, int conversation, long time, String body) {
		boolean ret = false;
		
		try {
            Statement s = this.db.createStatement("INSERT OR REPLACE INTO message(id, sender, conversation, time, body) VALUES(?, ?, ?, ?, ?)");
            s.prepare();
            s.bind(1, id);
            s.bind(2, sender);
            s.bind(3, conversation);
            s.bind(4, time);
            s.bind(5, body);
            s.execute();      
            s.close();
            
            ret = this.db.getNumberOfChanges() > 0;
            
            if (this.delegate != null) {
            	if (this.delegate.listeningConversation() == -1 
            		|| this.delegate.listeningConversation() == conversation) {
            		this.delegate.onMessageEvent(this.getByID(id));
            	}
            }
            
            if (ret && USE_MESSAGE_FOLDER) {
            	Hashtable user = UserStore.getInstance().getByID(sender);
            	if (user != null) {
	            	AppMessage msg = new AppMessage(conversation, (String) user.get("name"), body, body, time, ApplicationMessage.Status.UNOPENED, 1);
	            	this.messageList.add(msg);
	            	this.messageFolder.fireElementAdded(msg);
            	}
            }
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return ret;
	}
	
	public boolean setMessage(JSONObject val) throws Exception {
		return setMessage(val.getInt("message"), val.getInt("sender"), val.getInt("conversation"), val.getLong("time"), val.getString("body"));
	}
    
    public Hashtable getByID(int id) {
        Hashtable ret = null;
        
        try {          
            Statement s = this.db.createStatement("SELECT id, sender, conversation, time, body FROM message WHERE id = ?");
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
    
    public Hashtable getMostRecentForConversation(int conversation) {
        Hashtable ret = null;
        
        try {
            Statement s = this.db.createStatement("SELECT DISTINCT(id), sender, conversation, time, body FROM message WHERE conversation = ? ORDER BY time DESC");
            s.prepare();
            s.bind(1, conversation);
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
    
    public Vector messagesForConversation(int conversation) {
        Vector list = new Vector();
        
        try {          
            Statement s = this.db.createStatement("SELECT DISTINCT(id), sender, conversation, time, body FROM message WHERE conversation = ? ORDER BY id ASC");
            s.prepare();
            s.bind(1, conversation);
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
    
    public Vector messagesForConversationAfter(int conversation, long time) {
        Vector list = new Vector();
        
        try {          
            Statement s = this.db.createStatement("SELECT DISTINCT(id), sender, conversation, time, body FROM message WHERE conversation = ? AND time > ? ORDER BY id ASC");
            s.prepare();
            s.bind(1, conversation);
            s.bind(2, time);
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
        	this.delegate.onMessageEvents(firstEventTime);
        }   	
    }

}
