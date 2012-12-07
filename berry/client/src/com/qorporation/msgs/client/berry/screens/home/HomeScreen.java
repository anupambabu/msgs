package com.qorporation.msgs.client.berry.screens.home;

import java.util.Hashtable;

import javax.microedition.global.Formatter;

import com.qorporation.msgs.client.berry.networking.HttpRequest;
import com.qorporation.msgs.client.berry.networking.HttpRequestDelegate;
import com.qorporation.msgs.client.berry.screens.delegates.RedrawResponder;
import com.qorporation.msgs.client.berry.screens.fields.GroupExpander;
import com.qorporation.msgs.client.berry.screens.home.fields.ContactListField;
import com.qorporation.msgs.client.berry.screens.home.fields.ConversationListField;
import com.qorporation.msgs.client.berry.screens.settings.SettingsScreen;
import com.qorporation.msgs.client.berry.store.AccountStore;
import com.qorporation.msgs.client.berry.store.ContactStore;
import com.qorporation.msgs.client.berry.store.ConversationStore;
import com.qorporation.msgs.client.berry.store.MessageStore;
import com.qorporation.msgs.client.berry.store.UserStore;
import com.qorporation.msgs.client.berry.store.delegates.ContactStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.ConversationStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.MessageStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.UserStoreDelegate;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public final class HomeScreen extends MainScreen implements ContactStoreDelegate, MessageStoreDelegate, ConversationStoreDelegate, UserStoreDelegate, RedrawResponder {
	
	private TitleField titleField = null;
	
    private ConversationListField conversations = null;
    private ContactListField contacts = null;
    private ContactListField contactsRequested = null;
    private ContactListField contactsPending = null;
    
    private GroupExpander conversationsExpander = null;
    private GroupExpander contactsExpander = null;
    private GroupExpander contactsRequestedExpander = null;
    private GroupExpander contactsPendingExpander = null;
    
    private class TitleField extends Field {
    	private String title = null;
    	private Font font = null;
    	public TitleField(String title) { this.title = title; this.font = Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight() + 1); }
		protected void layout(int width, int height) { this.setExtent(width, this.font.getHeight() + 20); }
		protected void paint(Graphics g) {
			g.setColor(0x000000);
			g.fillRect(0, 0, this.getWidth(), this.font.getHeight() + 20);
            g.setColor(0xFFFFFF);
            g.setFont(this.font);
            g.drawText(this.title, 10, 10);
		}
    };

	public HomeScreen() {        
        ContactStore.getInstance().setDelegate(this);
        MessageStore.getInstance().setDelegate(this);
        ConversationStore.getInstance().setDelegate(this);
        UserStore.getInstance().setDelegate(this);
        
        this.conversations = new ConversationListField();
        this.contacts = new ContactListField(ContactStore.STATUS_CONTACT);
        this.contactsRequested = new ContactListField(ContactStore.STATUS_REQUEST);
        this.contactsPending = new ContactListField(ContactStore.STATUS_PENDING);
        
        this.conversationsExpander = new GroupExpander("Converastions", this.conversations, this, true);
        this.contactsExpander = new GroupExpander("Contacts", this.contacts, this, true);
        this.contactsRequestedExpander = new GroupExpander("Invites", this.contactsRequested, this, true);
        this.contactsPendingExpander = new GroupExpander("Pending", this.contactsPending, this, true);
        
        this.titleField = new TitleField("msgs");
        
        this.add(this.titleField);
        this.add(this.contactsRequestedExpander);
        this.add(this.conversationsExpander);
        this.add(this.contactsExpander);
        this.add(this.contactsPendingExpander);
        
        this.toggleFields();
    }
	
	protected void onExposed() {
        ContactStore.getInstance().setDelegate(this);
        MessageStore.getInstance().setDelegate(this);
        ConversationStore.getInstance().setDelegate(this);
        UserStore.getInstance().setDelegate(this);
        
		this.contactsRequested.refresh();
		this.contactsPending.refresh();
		this.contacts.refresh();
		this.conversations.refresh();
		
		this.toggleFields();	
	}

	private void toggleFields() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {        		
        		if (HomeScreen.this.contactsRequested.getSize() > 0) {
        			HomeScreen.this.contactsRequestedExpander.show();
        		} else {
        			HomeScreen.this.contactsRequestedExpander.hide();
        		}
        		
        		if (HomeScreen.this.contactsPending.getSize() > 0) {
        			HomeScreen.this.contactsPendingExpander.show();
        		} else {
        			HomeScreen.this.contactsPendingExpander.hide();
        		}    	
            }
		});
	}
	
	public void redraw(Field field) {
		this.updateLayout();
	}
	
	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);
		
        menu.add(MenuItem.separator(0x230010));

        menu.add(new MenuItem("Settings", 0x230010, 0) {
            public void run() {
            	UiApplication app = UiApplication.getUiApplication();
            	app.pushScreen(new SettingsScreen());               
            }});
        
        menu.add(new MenuItem("Add Contact", 0x230010, 0) {
            public void run() {
            	UiApplication.getUiApplication().pushModalScreen(
            			new PopupScreen(new VerticalFieldManager(), Field.FOCUSABLE) {{
            					this.setBackground(BackgroundFactory.createSolidTransparentBackground(Color.BLACK, 200));
            					this.setBorder(BorderFactory.createRoundedBorder(new XYEdges(15, 15, 15, 15), Color.BLACK, Border.STYLE_FILLED));
            					
            					final PopupScreen thisScreen = this;
            					
            					final EditField emailField = new EditField("Email: ", "", 255, EditField.FILTER_EMAIL);
            					final EditField phoneField = new EditField("Phone: ", "", 255, EditField.FILTER_PHONE);
            					
            					final ButtonField requestButton = new ButtonField("Request", ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
            					requestButton.setChangeListener(new FieldChangeListener() {
            						public void fieldChanged(Field field, int context){
            							String reqURL = phoneField.getTextLength() > 0 ? HttpRequest.MSGS_CONTACT_REQUEST_BY_PHONE_FORMAT : HttpRequest.MSGS_CONTACT_REQUEST_BY_EMAIL_FORMAT;
            							String reqParam = phoneField.getTextLength() > 0 ? phoneField.getText() : emailField.getText();
            							
            			            	HttpRequest req = new HttpRequest(Formatter.formatMessage(
            			        				reqURL,
            			        				new String[] {
            			        					reqParam,
            			        					AccountStore.getInstance().getRequestAuthentication()
            			        				}), new HttpRequestDelegate() {
            										public void onRequestSending(int sent, int total, HttpRequest request) {}
            										public void onRequestReceiving(int received, int total, HttpRequest request) {}
            										public void onRequestFailure(HttpRequest request) { thisScreen.close(); }
            										public void onRequestComplete(String response, HttpRequest request) { thisScreen.close(); }
            			            			});
            			            	
            			            	req.send();  
            						}});
            					
            					final ButtonField cancelButton = new ButtonField("Cancel");
            					cancelButton.setChangeListener(new FieldChangeListener() {
            						public void fieldChanged(Field field, int context) {
            							thisScreen.close();
            						}});
            					
            					this.add(emailField);
            					this.add(phoneField);
            					this.add(new HorizontalFieldManager() {{
                					this.add(requestButton);
                					this.add(cancelButton);
            					}});
            				}});
            }});
	}
	
	public int listeningConversation() { return -1; }
	public void onContactEvents(long earliestMessage) {}
	public void onMessageEvents(long earliestMessage) {}
	
	public void onContactEvent(Hashtable contact) {
		this.contactsRequested.onContactEvent(contact);
		this.contactsPending.onContactEvent(contact);
		this.contacts.onContactEvent(contact);
		this.conversations.onContactEvent(contact);
		this.toggleFields();
	}

	public void onMessageEvent(Hashtable message) {
		this.conversations.onMessageEvent(message);	
		this.toggleFields();
	}
	
	public void onUserEvent(Hashtable user) {
		this.contactsRequested.onUserEvent(user);
		this.contactsPending.onUserEvent(user);
		this.contacts.onUserEvent(user);
		this.conversations.onUserEvent(user);
		this.toggleFields();
	}
	
	public void onConversationEvent(Hashtable conversation) {
		this.conversations.onConversationEvent(conversation);
		this.toggleFields();
	}
    
}
