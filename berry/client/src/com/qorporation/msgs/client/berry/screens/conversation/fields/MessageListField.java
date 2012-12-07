package com.qorporation.msgs.client.berry.screens.conversation.fields;

import java.util.Hashtable;
import java.util.Vector;

import com.qorporation.msgs.client.berry.screens.fields.WrappedTextField;
import com.qorporation.msgs.client.berry.store.AccountStore;
import com.qorporation.msgs.client.berry.store.ConversationStore;
import com.qorporation.msgs.client.berry.store.MessageStore;
import com.qorporation.msgs.client.berry.store.UserStore;

import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.IntHashtable;

public class MessageListField extends VerticalFieldManager {

	private Vector rows;
	private int conversation = -1;
	private long firstReadTime = -1;
	private long lastReadTime = -1;
	private IntHashtable usersByID;
	
	public MessageListField(int conversation) {
	    super(Manager.VERTICAL_SCROLL | Manager.USE_ALL_HEIGHT | Manager.USE_ALL_WIDTH);
	    this.rows = new Vector();
	    this.usersByID = new IntHashtable();
	    this.conversation = conversation;
	    
	    Hashtable convData = ConversationStore.getInstance().getByID(conversation);
	    if (convData != null) {
	    	this.firstReadTime = ((Long) convData.get("firstread")).longValue();
	    	this.lastReadTime = ((Long) convData.get("lastread")).longValue();
	    }
	    
	    this.refresh();
	}
	
	public void setConversation(int conversation) {
		this.conversation = conversation;
		this.refresh();
	}
	
	public ContextMenu getContextMenu() {
	    ContextMenu menu = super.getContextMenu();
	    return menu;        
	}

	protected boolean trackwheelClick(int status, int time) {
	    return true;
	}
	
	public void refresh() {
		if (this.conversation >= 0) {
	        UiApplication.getUiApplication().invokeLater(new Runnable() {
	            public void run() {
	        	   MessageListField.this.refreshInternal();     
	            } 
	        });
		}
	}
	
	private void refreshInternal() {		
		Vector messages = MessageStore.getInstance().messagesForConversationAfter(this.conversation, this.firstReadTime);
		
		IntHashtable newUsersByID = new IntHashtable();
		
		int r = 0;
	    for (int m = 0; m < messages.size(); m++) {
	    	Hashtable message = (Hashtable) messages.elementAt(m);
	    	
	    	Integer userID = (Integer) message.get("sender");
	    	if (!newUsersByID.containsKey(userID.intValue())) {
	    		newUsersByID.put(userID.intValue(), userID);
	    		this.usersByID.put(userID.intValue(), UserStore.getInstance().getByID(userID.intValue()));
	    	}
	    	
	    	if (this.rows.size() > r) {
	    		MessageRowManager row = (MessageRowManager) this.rows.elementAt(r);
	    		Integer rowID = (Integer) row.getMessage().get("id");
	    		Integer messageID = (Integer) message.get("id");
	    		
	    		if (!rowID.equals(messageID)) {
	    			Long messageTime = (Long) message.get("time");
	    			Long rowTime = (Long) row.getMessage().get("time");
	    			
	    			if (messageTime.longValue() < rowTime.longValue()) {
	    				MessageRowManager messageRow = new MessageRowManager(message);
	    				this.rows.insertElementAt(messageRow, r);
	    				this.insert(messageRow, r);
	    				++r;
	    			} else if (r == this.rows.size() - 1) {
	    				MessageRowManager messageRow = new MessageRowManager(message);
	    				this.rows.addElement(messageRow);
	    				this.add(messageRow);    				
	    			}
	    		} else {
	    			row.checkInvalidation();
	    		}
	    		
	    		++r;
	    	} else {
	    		MessageRowManager row = new MessageRowManager(message);
	    		this.rows.addElement(row);
	        	this.add(row);
	    	}
	    }
	    
	    this.updateLayout();
	    this.updateScroll();
	}
	
	private void updateScroll() {
		int currY = this.getVerticalScroll();
		int currHeight = this.getVirtualHeight();
		int currViewHeight = this.getVisibleHeight();
		int currRemainder = currHeight - (currY + currViewHeight);
		int currFrame = currRemainder / currViewHeight;
		
		if (currFrame < 1 || currY < currViewHeight) {
			this.scrollBottom();
		}
	}

	public void scrollBottom() {
		int currHeight = this.getVirtualHeight();
		int currViewHeight = this.getVisibleHeight();
		int newOffset = currHeight - currViewHeight;
		if (newOffset > 0) {
			this.setVerticalScroll(newOffset);
		}
		
		if (this.rows.size() > 0) {
			long lastMessageTime = ((Long) this.getLastMessage().get("time")).longValue();
			this.updateLastRead(lastMessageTime);
		}
	}
	
	public Hashtable getUserByID(int userID) {
		return (Hashtable) this.usersByID.get(userID);
	}
	
	public Hashtable getLastMessage() {
		return (Hashtable) ((MessageRowManager) this.rows.lastElement()).getMessage();
	}

	public void onConversationEvent(Hashtable conversation) { this.refresh(); }
	public void onMessageEvent(Hashtable message) { this.refresh(); }
	public void onPrimaryConversationEvent(Hashtable conversation) { this.setConversation(((Integer) conversation.get("id")).intValue()); }
	
	public void onContactEvent(Hashtable contact) {
		for (int i = 0; i < this.rows.size(); i++) {
			MessageRowManager row = (MessageRowManager) this.rows.elementAt(i);
			row.updateDeliveredStatus(contact);
		}
	}
	
	public void onConversationParticipantEvent(Hashtable participant) { 
		for (int i = 0; i < this.rows.size(); i++) {
			MessageRowManager row = (MessageRowManager) this.rows.elementAt(i);
			row.updateReadStatus(participant);
		}
	}
	
	public void onUserEvent(Hashtable user) {
		int userID = ((Integer) user.get("id")).intValue();
		if (this.usersByID.containsKey(userID)) {
			this.usersByID.put(userID, user);
			
			for (int i = 0; i < this.rows.size(); i++) {
				MessageRowManager row = (MessageRowManager) this.rows.elementAt(i);
				row.checkInvalidation();
			}
			
			return;
		}
	}
	
	private void updateLastRead(long time) {
		if (time > this.lastReadTime) {
			this.lastReadTime = time;
			ConversationStore.getInstance().updateConversation(this.conversation, this.firstReadTime, time);
		}
	}
	
	public class MessageRowManager extends Manager {
	    private static final int EDGEOFFSET = 5;
	    private static final int TEXTPADDING = 5;
	    private static final int EDGEPADDING = EDGEOFFSET * 2 + 5 + TEXTPADDING * 2;
	    
		private Hashtable message = null;
		
		private WrappedTextField nameField = null;
		private WrappedTextField bodyField = null;
		private WrappedTextField timeField = null;
		private boolean isLocalUser = false;
		
	    public MessageRowManager(Hashtable message) {
	        super(0);
	        
	        this.message = message;
	        this.isLocalUser = ((Integer) this.message.get("sender")).intValue() == AccountStore.getInstance().getUserID();
	        this.nameField = new WrappedTextField("", DrawStyle.ELLIPSIS, 0x00000000, Font.getDefault().derive(Font.BOLD | Font.UNDERLINED));
	        this.bodyField = new WrappedTextField((String) this.message.get("body"), DrawStyle.ELLIPSIS, 0x00878787);
	        this.timeField = new WrappedTextField(((Long) this.message.get("time")).toString(), DrawStyle.ELLIPSIS | DrawStyle.RIGHT, 0x00878787);
	        
		    this.add(this.nameField);
		    this.add(this.bodyField);
		    this.add(this.timeField);
	    }
	    
		public void paint(Graphics g) {
	    	g.setColor(0xF6F6F6);
	    	g.fillRect(0, 0, this.getWidth(), this.getHeight());
	    	
	    	if (this.isLocalUser) {
		        g.setColor(0xEEEEEE);
		    	g.drawFilledPath(
		    			new int[]{ this.getWidth() - EDGEOFFSET, this.getWidth() - EDGEOFFSET, 5 + EDGEOFFSET, 5 + EDGEOFFSET, this.getWidth() - 5 - EDGEOFFSET, this.getWidth() - EDGEOFFSET }, 
		    			new int[]{ this.getHeight(), 5, 5, this.getHeight() - 5, this.getHeight() - 5, this.getHeight() }, 
		    			new byte[]{ Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT }, 		    			null);
		    	
		        g.setColor(0x999999);
		    	g.drawOutlinedPath(
		    			new int[]{ this.getWidth() - EDGEOFFSET, this.getWidth() - EDGEOFFSET, 5 + EDGEOFFSET, 5 + EDGEOFFSET, this.getWidth() - 5 - EDGEOFFSET, this.getWidth() - EDGEOFFSET }, 
		    			new int[]{ this.getHeight(), 5, 5, this.getHeight() - 5, this.getHeight() - 5, this.getHeight() }, 
		    			new byte[]{ Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT }, 		    			null,
		    			true);
	    	} else {
		        g.setColor(0xE1F1F2);
		    	g.drawFilledPath(
		    			new int[]{ EDGEOFFSET, EDGEOFFSET, this.getWidth() - 5 - EDGEOFFSET, this.getWidth() - 5 - EDGEOFFSET, 5 + EDGEOFFSET, EDGEOFFSET }, 
		    			new int[]{ this.getHeight(), 5, 5, this.getHeight() - 5, this.getHeight() - 5, this.getHeight() }, 
		    			new byte[]{ Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT }, 
		    			null);
		    	
		        g.setColor(0x999999);
		    	g.drawOutlinedPath(
		    			new int[]{ EDGEOFFSET, EDGEOFFSET, this.getWidth() - 5 - EDGEOFFSET, this.getWidth() - 5 - EDGEOFFSET, 5 + EDGEOFFSET, EDGEOFFSET }, 
		    			new int[]{ this.getHeight(), 5, 5, this.getHeight() - 5, this.getHeight() - 5, this.getHeight() }, 
		    			new byte[]{ Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT, Graphics.CURVEDPATH_END_POINT }, 
		    			null,
		    			true);	    		
	    	}
	        
	    	super.paint(g);
	    }
	    
	    protected void onFocus(int direction) {
	    	if (direction > 0) {
	    		this.nameField.setFocus();
	    	} else if (direction < 0) {
	    		this.bodyField.setFocus();
	    		
	    		long messageTime = ((Long) this.message.get("time")).longValue();
	    		if (messageTime > MessageListField.this.lastReadTime) {
	    			MessageListField.this.updateLastRead(messageTime);
	    		}
	    	}
	    }
	    
	    protected void sublayout(int width, int height) {
	    	Hashtable user = MessageListField.this.getUserByID(((Integer) this.message.get("sender")).intValue());
	    	this.nameField.setText((String) user.get("name"));
	    	
	        this.layoutChild(this.nameField, width - EDGEPADDING, height);
	        this.layoutChild(this.bodyField, width - EDGEPADDING, height - this.nameField.getHeight());
	        
	        if (this.isLocalUser) {
	        	this.setPositionChild(this.nameField, EDGEOFFSET + 5 + TEXTPADDING, 10);
	        	this.setPositionChild(this.bodyField, EDGEOFFSET + 5 + TEXTPADDING, this.nameField.getHeight() + 13);
	        } else {
	        	this.setPositionChild(this.nameField, EDGEOFFSET + TEXTPADDING, 10);
	        	this.setPositionChild(this.bodyField, EDGEOFFSET + TEXTPADDING, this.nameField.getHeight() + 13);	        	
	        }
	
	        this.setExtent(width, this.nameField.getHeight() + this.bodyField.getHeight() + 23);
	    }

		public WrappedTextField getNameField() { return this.nameField; }
		public WrappedTextField getBodyField() { return this.bodyField; }
		public Hashtable getMessage() { return this.message; }

		public void updateDeliveredStatus(Hashtable contact) {}
	    public void updateReadStatus(Hashtable participant) {}
		public void checkInvalidation() {
	    	Hashtable user = MessageListField.this.getUserByID(((Integer) this.message.get("sender")).intValue());
	    	String userName = (String) user.get("name");
	    	if (!userName.equals(this.nameField.getText())) {
	    		this.nameField.setText((String) user.get("name"));
	    		this.invalidate();
	    	}
		}
	}

}
