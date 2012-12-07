package com.qorporation.msgs.client.berry.screens.home.fields;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.global.Formatter;

import com.qorporation.msgs.client.berry.networking.HttpRequest;
import com.qorporation.msgs.client.berry.networking.HttpRequestDelegate;
import com.qorporation.msgs.client.berry.screens.conversation.ConversationScreen;
import com.qorporation.msgs.client.berry.screens.fields.StyledTextLabel;
import com.qorporation.msgs.client.berry.store.AccountStore;
import com.qorporation.msgs.client.berry.store.ContactStore;
import com.qorporation.msgs.client.berry.store.ConversationStore;
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
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.util.IntHashtable;

public class ContactListField extends ListField implements ListFieldCallback {
	private Vector rows = null;
	private Integer status = null;
	
	public ContactListField(Integer status) {
	    super(0, ListField.MULTI_SELECT);
	    this.rows = new Vector();
	    this.status = status;
	    this.setRowHeight(Font.getDefault().getHeight() + 20);
	    this.setEmptyString("No Contacts Found", DrawStyle.HCENTER);
	    this.setCallback(this);
	    this.refresh();
	}      
	
	private class ContactRowManager extends Manager {
		private Hashtable contact = null;
		
		private StyledTextLabel nameField = null;
		
		private int y = 0;
		
	    public ContactRowManager(Hashtable contact) {
	        super(0);
	        
	        this.contact = contact;
	        this.nameField = new StyledTextLabel("", DrawStyle.ELLIPSIS | Field.READONLY, 0x00000000, Font.getDefault().derive(Font.BOLD | Font.UNDERLINED));
	        
		    this.add(this.nameField);
	    }
	    
		public Hashtable getContact() {
			return this.contact;
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
	    	Hashtable user = UserStore.getInstance().getByID(((Integer) this.contact.get("user")).intValue());
	    	if (user != null) {
	    		this.nameField.setText((String) user.get("name"));
	    	}
	    	
	        this.layoutChild(this.nameField, width - 20, Font.getDefault().getHeight());
	        this.setPositionChild(this.nameField, 10, this.y + 10);
	
	        this.setExtent(width, this.nameField.getHeight() + 20);
	    }

	    public int getPreferredWidth() {
	        return Display.getWidth();
	    }

	    public int getPreferredHeight() {
	        return ContactListField.this.getRowHeight();
	    }

		public void checkInvalidation() {
	    	Hashtable user = UserStore.getInstance().getByID(((Integer) this.contact.get("user")).intValue());
	    	if (user != null) {
		    	String userName = (String) user.get("name");
		    	if (!userName.equals(this.nameField.getText())) {
		    		this.nameField.setText((String) user.get("name"));
		    		this.invalidate();
		    	}
	    	}
		}
	}

	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {
		ContactListField list = (ContactListField) listField;
		ContactRowManager row = (ContactRowManager) list.rows.elementAt(index);
		row.drawRow(g, 0, y, width, list.getRowHeight());
	}
	
	public Object get(ListField listField, int index) {
		ContactListField list = (ContactListField) listField;
		ContactRowManager rowManager = (ContactRowManager) list.rows.elementAt(index);
		return rowManager;
	}
	
	public int indexOfList(ListField list, String p, int s) {
	    return -1;
	}
	
	public int getPreferredWidth(ListField list) {
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
		
    	Hashtable contact = ((ContactRowManager) this.get(this, idx)).getContact();
    	int userID = ((Integer) contact.get("user")).intValue();
    	
    	switch (((Integer) contact.get("status")).intValue()) {
	    	case 1: {
		    	Hashtable conversation = ConversationStore.getInstance().getPrimary(userID);
		    	UiApplication app = UiApplication.getUiApplication();
		    	app.pushScreen(new ConversationScreen(conversation != null ? ((Integer) conversation.get("id")).intValue() : -1, userID));
	    	} break;
	    	
	    	case 6: {
	    		switch (Dialog.ask((String) UserStore.getInstance().getByID(userID).get("name"), new String[] { "Delete", "Cancel" }, 0)) {
		    		case 0: {
		            	new HttpRequest(Formatter.formatMessage(
		        				HttpRequest.MSGS_CONTACT_DELETE_FORMAT,
		        				new String[] {
		        					Integer.toString(userID),
		        					AccountStore.getInstance().getRequestAuthentication()
		        				}), new HttpRequestDelegate() {
									public void onRequestSending(int sent, int total, HttpRequest request) {}
									public void onRequestReceiving(int received, int total, HttpRequest request) {}
									public void onRequestFailure(HttpRequest request) {}
									public void onRequestComplete(String response, HttpRequest request) {}
		            			}).send();	 		    			
		    		} break;
	    		}
	    	} break;
	    	
	    	case 7: {
	    		switch (Dialog.ask((String) UserStore.getInstance().getByID(userID).get("name"), new String[] { "Accept", "Ignore", "Cancel" }, 0)) {
		    		case 0: {
		            	new HttpRequest(Formatter.formatMessage(
		        				HttpRequest.MSGS_CONTACT_ACCEPT_FORMAT,
		        				new String[] {
		        					Integer.toString(userID),
		        					AccountStore.getInstance().getRequestAuthentication()
		        				}), new HttpRequestDelegate() {
									public void onRequestSending(int sent, int total, HttpRequest request) {}
									public void onRequestReceiving(int received, int total, HttpRequest request) {}
									public void onRequestFailure(HttpRequest request) {}
									public void onRequestComplete(String response, HttpRequest request) {}
		            			}).send();	    			
		    		} break;
		    		
		    		case 1: {
		            	new HttpRequest(Formatter.formatMessage(
		        				HttpRequest.MSGS_CONTACT_IGNORE_FORMAT,
		        				new String[] {
		        					Integer.toString(userID),
		        					AccountStore.getInstance().getRequestAuthentication()
		        				}), new HttpRequestDelegate() {
									public void onRequestSending(int sent, int total, HttpRequest request) {}
									public void onRequestReceiving(int received, int total, HttpRequest request) {}
									public void onRequestFailure(HttpRequest request) {}
									public void onRequestComplete(String response, HttpRequest request) {}
		            			}).send();		    			
		    		} break;
	    		}
	    	} break;
    	}
		
	    return true;
	}
	
	public void refresh() {
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                ContactListField.this.refreshInternal();   
            } 
        });
	}
        
	private void refreshInternal() {
		Vector contacts = ContactStore.getInstance().list(this.status);
		
		IntHashtable ids = new IntHashtable();
		for (int e = 0; e < contacts.size(); e++) {
			Hashtable el = (Hashtable) contacts.elementAt(e);
			int id = ((Integer) el.get("id")).intValue();
			ids.put(id, el);
		}
		
		for (int r = this.rows.size() - 1; r >= 0; r--) {
    		ContactRowManager row = (ContactRowManager) this.rows.elementAt(r);
    		Integer rowID = (Integer) row.getContact().get("id");
    		if (!ids.containsKey(rowID.intValue())) {
    			this.rows.removeElementAt(r);
    		}
		}
		
		int r = 0;
	    for (int e = 0; e < contacts.size(); e++) {
	    	Hashtable contact = (Hashtable) contacts.elementAt(e);
	    	
	    	if (this.rows.size() > r) {
	    		ContactRowManager row = (ContactRowManager) this.rows.elementAt(r);
	    		Integer rowID = (Integer) row.getContact().get("id");
	    		Integer contactID = (Integer) contact.get("id");
	    		
	    		if (!rowID.equals(contactID)) {
	    			if (contactID.intValue() < rowID.intValue()) {
	    				ContactRowManager contactRow = new ContactRowManager(contact);
	    				this.rows.insertElementAt(contactRow, r);
	    				++r;
	    			} else if (r == this.rows.size() - 1) {
	    				ContactRowManager contactRow = new ContactRowManager(contact);
	    				this.rows.addElement(contactRow);				
	    			}
	    		} else {
	    			row.checkInvalidation();
	    		}
	    		
	    		++r;
	    	} else {
	    		ContactRowManager row = new ContactRowManager(contact);
	    		this.rows.addElement(row);
	    		++r;
	    	}
	    }
	    
	    if (r < this.rows.size()) {
	    	while (this.rows.size() > r + 1) {
	    		this.rows.removeElementAt(r);
	    	}
	    }
	    
	    this.setSize(rows.size());
	    this.updateLayout();
	}

	public Integer getSelectedContact() {
		int selected = this.getSelectedIndex();
		if (selected > -1 && selected < this.rows.size()) {
			Hashtable contact = ((ContactRowManager) this.rows.elementAt(selected)).contact;
			return (Integer) contact.get("user");
		}
		
		return null;
	}

	public void onContactEvent(Hashtable contact) { this.refresh(); }
	public void onUserEvent(Hashtable user) {
		for (int i = 0; i < this.rows.size(); i++) {
    		ContactRowManager row = (ContactRowManager) this.rows.elementAt(i);
    		Integer userID = (Integer) row.getContact().get("user");
    		if (userID.equals((Integer) user.get("id"))) {
    			this.refresh();
    			return;
    		}
		}
	}
	
	protected void makeContextMenu(ContextMenu menu) {
		super.makeContextMenu(menu);

		switch (this.status.intValue()) {
			case 1: {
		        menu.addItem(new MenuItem("Open Conversation", 0x230010, 0) {
		        	public void run() {
		            	Integer contact = ContactListField.this.getSelectedContact();
		            	if (contact == null) return;
		            	
				    	Hashtable conversation = ConversationStore.getInstance().getPrimary(contact.intValue());
				    	UiApplication app = UiApplication.getUiApplication();
				    	app.pushScreen(new ConversationScreen(conversation != null ? ((Integer) conversation.get("id")).intValue() : -1, contact.intValue()));                 
		            }});
		    	
		        menu.addItem(new MenuItem("Remove Contact", 0x230010, 0) {
		        	public void run() {
		            	Integer contact = ContactListField.this.getSelectedContact();
		            	if (contact == null) return;
		            	
		            	HttpRequest req = new HttpRequest(Formatter.formatMessage(
		        				HttpRequest.MSGS_CONTACT_DELETE_FORMAT,
		        				new String[] {
		        					contact.toString(),
		        					AccountStore.getInstance().getRequestAuthentication()
		        				}), new HttpRequestDelegate() {
									public void onRequestSending(int sent, int total, HttpRequest request) {}
									public void onRequestReceiving(int received, int total, HttpRequest request) {}
									public void onRequestFailure(HttpRequest request) {}
									public void onRequestComplete(String response, HttpRequest request) {}
		            			});
		            	
		            	req.send();                   
		            }});				
			} break;
			
			case 6: {
		        menu.addItem(new MenuItem("Delete Request", 0x230010, 0) {
		        	public void run() {
		            	Integer contact = ContactListField.this.getSelectedContact();
		            	if (contact == null) return;
		            	
		            	HttpRequest req = new HttpRequest(Formatter.formatMessage(
		        				HttpRequest.MSGS_CONTACT_DELETE_FORMAT,
		        				new String[] {
		        					contact.toString(),
		        					AccountStore.getInstance().getRequestAuthentication()
		        				}), new HttpRequestDelegate() {
									public void onRequestSending(int sent, int total, HttpRequest request) {}
									public void onRequestReceiving(int received, int total, HttpRequest request) {}
									public void onRequestFailure(HttpRequest request) {}
									public void onRequestComplete(String response, HttpRequest request) {}
		            			});
		            	
		            	req.send();                   
		            }});					
			} break;
			
			case 7: {
		        menu.addItem(new MenuItem("Accept Request", 0x230010, 0) {
		        	public void run() {
		            	Integer contact = ContactListField.this.getSelectedContact();
		            	if (contact == null) return;
		            	
		            	HttpRequest req = new HttpRequest(Formatter.formatMessage(
		        				HttpRequest.MSGS_CONTACT_ACCEPT_FORMAT,
		        				new String[] {
		        					contact.toString(),
		        					AccountStore.getInstance().getRequestAuthentication()
		        				}), new HttpRequestDelegate() {
									public void onRequestSending(int sent, int total, HttpRequest request) {}
									public void onRequestReceiving(int received, int total, HttpRequest request) {}
									public void onRequestFailure(HttpRequest request) {}
									public void onRequestComplete(String response, HttpRequest request) {}
		            			});
		            	
		            	req.send();                   
		            }});
		        
		        menu.addItem(new MenuItem("Ignore Request", 0x230010, 0) {
		        	public void run() {
		            	Integer contact = ContactListField.this.getSelectedContact();
		            	if (contact == null) return;
		            	
		            	HttpRequest req = new HttpRequest(Formatter.formatMessage(
		        				HttpRequest.MSGS_CONTACT_IGNORE_FORMAT,
		        				new String[] {
		        					contact.toString(),
		        					AccountStore.getInstance().getRequestAuthentication()
		        				}), new HttpRequestDelegate() {
									public void onRequestSending(int sent, int total, HttpRequest request) {}
									public void onRequestReceiving(int received, int total, HttpRequest request) {}
									public void onRequestFailure(HttpRequest request) {}
									public void onRequestComplete(String response, HttpRequest request) {}
		            			});
		            	
		            	req.send();                   
		            }});				
			} break;
		}
	}
}
