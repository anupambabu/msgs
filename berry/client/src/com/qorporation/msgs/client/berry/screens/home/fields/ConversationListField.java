package com.qorporation.msgs.client.berry.screens.home.fields;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.global.Formatter;

import com.qorporation.msgs.client.berry.networking.HttpRequest;
import com.qorporation.msgs.client.berry.networking.HttpRequestDelegate;
import com.qorporation.msgs.client.berry.screens.conversation.ConversationScreen;
import com.qorporation.msgs.client.berry.screens.fields.StyledTextLabel;
import com.qorporation.msgs.client.berry.store.AccountStore;
import com.qorporation.msgs.client.berry.store.ConversationStore;
import com.qorporation.msgs.client.berry.store.MessageStore;
import com.qorporation.msgs.client.berry.store.UserStore;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.util.IntHashtable;

public class ConversationListField extends ListField implements ListFieldCallback {
	private Vector rows;
	private IntHashtable conversationsByID;
	private IntHashtable messagesByConversationID;
	private IntHashtable usersByID;
	
	public ConversationListField() {
	    super(0, ListField.MULTI_SELECT);
	    this.rows = new Vector();
	    this.setEmptyString("No Conversations Found", DrawStyle.HCENTER);
	    this.setRowHeight(Font.getDefault().getHeight() * 2 + 15);
	    this.setCallback(this);	    
	    this.refresh();
	}      
	
	private class ConversationRowManager extends Manager {
		private Hashtable conversation = null;
		
		private StyledTextLabel nameField = null;
		private StyledTextLabel bodyField = null;
		private StyledTextLabel timeField = null;
		
		private int y = 0;
		
	    public ConversationRowManager(Hashtable conversation) {
	        super(0);
	        
	        this.conversation = conversation;
	        this.nameField = new StyledTextLabel("", DrawStyle.ELLIPSIS | Field.READONLY, 0x00000000, Font.getDefault().derive(Font.BOLD | Font.UNDERLINED));
	        this.bodyField = new StyledTextLabel("", DrawStyle.ELLIPSIS | Field.READONLY, 0x00878787);
	        this.timeField = new StyledTextLabel("", DrawStyle.ELLIPSIS | LabelField.USE_ALL_WIDTH | DrawStyle.RIGHT | Field.READONLY, 0x008787870);
	        
		    this.add(this.nameField);
		    this.add(this.bodyField);
		    this.add(this.timeField);
	    }
	    
		public Hashtable getConversation() {
			return this.conversation;
		}

	    public void drawRow(Graphics g, int x, int y, int width, int height) {
	    	this.y = y;
	    	
	    	if (this.y > 0) {
	    		g.setColor(0xCCCCCC);
	    		g.drawLine(0, y, width, y);
	    	}
	    	
	        this.layout(width, height);
	        this.subpaint(g);
	    }

	    protected void sublayout(int width, int height) {	    	
	    	Hashtable message = (Hashtable) ConversationListField.this.messagesByConversationID.get(((Integer) this.conversation.get("id")).intValue());
	    	if (message != null) {
		    	this.bodyField.setText((String) message.get("body"));
		    	this.timeField.setText(((Long) message.get("time")).toString());
		    	
		    	Hashtable user = (Hashtable) ConversationListField.this.usersByID.get(((Integer) message.get("sender")).intValue());
		    	if (user != null) {
		    		this.nameField.setText((String) user.get("name"));
		    	}
	    	}
	    	
	        this.layoutChild(this.nameField, width - 20, Font.getDefault().getHeight());
	        this.setPositionChild(this.nameField, 10, this.y + 5);
 
	        this.layoutChild(this.bodyField, width - 20, Font.getDefault().getHeight());
	        this.setPositionChild(this.bodyField, 10, this.y + this.nameField.getHeight() + 10);
	
	        this.setExtent(width, this.nameField.getHeight() + this.bodyField.getHeight() + 15);
	    }

		public void checkInvalidation() {
			Hashtable message = (Hashtable) ConversationListField.this.messagesByConversationID.get(((Integer) this.conversation.get("id")).intValue());
	    	if (message != null) {
				String messageBody = (String) message.get("body");
		    	String messageTime = ((Long) message.get("time")).toString();
		    	
		    	if (!(messageBody.equals(this.bodyField.getText()) && messageTime.equals(this.timeField.getText()))) {
		    		this.bodyField.setText(messageBody);
		    		this.timeField.setText(messageTime);
		    		this.invalidate();
		    	}
		    	
		    	Hashtable user = (Hashtable) ConversationListField.this.usersByID.get(((Integer) message.get("sender")).intValue());
		    	if (user != null) {
			    	String userName = (String) user.get("name");
			    	if (!userName.equals(this.nameField.getText())) {
			    		this.nameField.setText((String) user.get("name"));
			    		this.invalidate();
			    	}
		    	}
	    	}
		}
	}

	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {
		ConversationListField list = (ConversationListField) listField;
		ConversationRowManager row = (ConversationRowManager) list.rows.elementAt(index);
		row.drawRow(g, 0, y, width, row.getPreferredHeight());
	}
	
	public Object get(ListField listField, int index) {
		ConversationListField list = (ConversationListField) listField;
		ConversationRowManager rowManager = (ConversationRowManager) list.rows.elementAt(index);
		return rowManager;
	}
	
	public int indexOfList(ListField list, String p, int s) {
	    return -1;
	}
	
	public int getPreferredWidth(ListField listField) {
		return Display.getWidth();
	}
	
	public ContextMenu getContextMenu() {
	    ContextMenu menu = super.getContextMenu();	    
	    return menu;        
	}
	
    protected boolean keyChar(char key, int status, int time) { return key == Characters.ENTER ? this.onAction() :  super.keyChar(key, status, time); } 
    protected boolean invokeAction(int action) { return this.onAction(); } 
	protected boolean trackwheelClick(int status, int time) { return this.onAction(); }
	private boolean onAction() {
		int idx = this.getSelectedIndex();
		if (idx < 0) return true;
		
		Hashtable conversation = ((ConversationRowManager) this.get(this, idx)).getConversation();
		UiApplication app = UiApplication.getUiApplication();
		app.pushScreen(new ConversationScreen(((Integer) conversation.get("id")).intValue(), -1)); 
		
	    return true;
	}

	public void refresh() {
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
        	    ConversationListField.this.refreshInternal();     
            } 
        });
	}
	
	private void refreshInternal() {
		Vector conversations = ConversationStore.getInstance().list();
		
		this.conversationsByID = new IntHashtable();
		this.messagesByConversationID = new IntHashtable();
		this.usersByID = new IntHashtable();
		
		IntHashtable ids = new IntHashtable();
		for (int e = 0; e < conversations.size(); e++) {
			Hashtable el = (Hashtable) conversations.elementAt(e);
			int id = ((Integer) el.get("id")).intValue();
			ids.put(id, el);
	    	
			this.conversationsByID.put(id, el);
			Hashtable message = MessageStore.getInstance().getMostRecentForConversation(id);
	    	if (message != null) {
	    		long messageTime = ((Long) message.get("time")).longValue();
	    		long firstReadTime = ((Long) el.get("firstread")).longValue();
	    		if (messageTime >= firstReadTime) {
		    		this.messagesByConversationID.put(id, message);
		    		int userID = ((Integer) message.get("sender")).intValue();
		    		if (!this.usersByID.containsKey(userID)) {
		    			Hashtable user = UserStore.getInstance().getByID(userID);
		    			if (user != null) {
		    				this.usersByID.put(userID, user);
		    			}
		    		}
	    		}
	    	}
		}
		
		for (int r = this.rows.size() - 1; r >= 0; r--) {
    		ConversationRowManager row = (ConversationRowManager) this.rows.elementAt(r);
    		Integer rowID = (Integer) row.getConversation().get("id");
    		if (!(ids.containsKey(rowID.intValue()) && this.messagesByConversationID.containsKey(rowID.intValue()))) {
    			this.rows.removeElementAt(r);
    		}
		}
		
		int r = 0;
	    for (int e = 0; e < conversations.size(); e++) {
	    	Hashtable conversation = (Hashtable) conversations.elementAt(e);
    		Integer conversationID = (Integer) conversation.get("id");
    		
    		if (!this.messagesByConversationID.containsKey(conversationID.intValue())) continue;
    		
	    	if (this.rows.size() > r) {
	    		ConversationRowManager row = (ConversationRowManager) this.rows.elementAt(r);
	    		Integer rowID = (Integer) row.getConversation().get("id");
	    		
	    		if (!rowID.equals(conversationID)) {
	    			if (conversationID.intValue() < rowID.intValue()) {
	    				ConversationRowManager contactRow = new ConversationRowManager(conversation);
	    				this.rows.insertElementAt(contactRow, r);
	    				++r;
	    			} else if (r == this.rows.size() - 1) {
    					ConversationRowManager contactRow = new ConversationRowManager(conversation);
	    				this.rows.addElement(contactRow);
	    			}
	    		} else {
	    			row.checkInvalidation();
	    		}
	    		
	    		++r;
	    	} else {
	    		ConversationRowManager row = new ConversationRowManager(conversation);
	    		this.rows.addElement(row);
	    		++r;
	    	}
	    }
	    
	    if (r < this.rows.size()) {
	    	while (this.rows.size() > r + 1) {
	    		this.rows.removeElementAt(r);
	    	}
	    }
	    
	    this.setSize(this.rows.size());
	    this.updateLayout();
	}

	public Integer getSelectedConversation() {
		int selected = this.getSelectedIndex();
		if (selected > -1 && selected < this.rows.size()) {
			Hashtable contact = ((ConversationRowManager) this.rows.elementAt(selected)).conversation;
			return (Integer) contact.get("id");
		}
		
		return null;
	}

	public void onUserEvent(Hashtable user) { this.refresh(); }
	public void onConversationEvent(Hashtable conversation) { this.refresh(); }
	public void onMessageEvent(Hashtable message) { this.refresh(); }
	public void onContactEvent(Hashtable contact) {}
	
	protected void makeContextMenu(ContextMenu menu) {
		super.makeContextMenu(menu);
        menu.addItem(new MenuItem("Leave Conversation", 0x230010, 0) {
            public void run() {
            	Integer conversation = ConversationListField.this.getSelectedConversation();
            	if (conversation == null) return;
            	
            	HttpRequest req = new HttpRequest(Formatter.formatMessage(
        				HttpRequest.MSGS_LEAVE_CONVERSATION_FORMAT,
        				new String[] {
        					conversation.toString(),
        					AccountStore.getInstance().getRequestAuthentication()
        				}), new HttpRequestDelegate() {
							public void onRequestSending(int sent, int total, HttpRequest request) {}
							public void onRequestReceiving(int received, int total, HttpRequest request) {}
							public void onRequestFailure(HttpRequest request) {}
							public void onRequestComplete(String response, HttpRequest request) {}
            			});
            	
            	req.send();                    
            }});
	}
}
