package com.qorporation.msgs.client.berry.screens.conversation;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.global.Formatter;

import org.json.JSONObject;

import com.qorporation.msgs.client.berry.networking.HttpRequest;
import com.qorporation.msgs.client.berry.networking.HttpRequestDelegate;
import com.qorporation.msgs.client.berry.screens.conversation.fields.InputField;
import com.qorporation.msgs.client.berry.screens.conversation.fields.InputField.InputFieldListener;
import com.qorporation.msgs.client.berry.screens.conversation.fields.MessageListField;
import com.qorporation.msgs.client.berry.screens.conversation.fields.MessageListField.MessageRowManager;
import com.qorporation.msgs.client.berry.screens.fields.StyledTextLabel;
import com.qorporation.msgs.client.berry.screens.fields.WrappedTextField;
import com.qorporation.msgs.client.berry.store.AccountStore;
import com.qorporation.msgs.client.berry.store.ContactStore;
import com.qorporation.msgs.client.berry.store.ConversationStore;
import com.qorporation.msgs.client.berry.store.MessageStore;
import com.qorporation.msgs.client.berry.store.UserStore;
import com.qorporation.msgs.client.berry.store.delegates.ContactStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.ConversationParticipantPrimaryStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.ConversationParticipantStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.ConversationStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.MessageStoreDelegate;
import com.qorporation.msgs.client.berry.store.delegates.UserStoreDelegate;
import com.qorporation.msgs.client.berry.util.ErrorControl;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

public final class ConversationScreen extends MainScreen {
	
	private int conversation = -1;
	private ConversationLayoutManager manager = null;

	public ConversationScreen(int conversation, int participant) {
		super(MainScreen.NO_VERTICAL_SCROLL);
		
		this.conversation = conversation;
        this.manager = new ConversationLayoutManager(conversation, participant);
        
        this.add(this.manager);
    }
    
	protected boolean navigationMovement(int dx, int dy, int status, int time) {
		int prior = 0;
		
		Field f = this.getLeafFieldWithFocus();
		if (f != null && f instanceof WrappedTextField) {
			Manager m = f.getManager();
			if (m != null && m instanceof MessageRowManager) {
				WrappedTextField wf = (WrappedTextField) f;
				MessageRowManager r = (MessageRowManager) m;
				
				if (r.getNameField().equals(wf)) {
					prior = 1;
				} else if (r.getBodyField().equals(wf)) {
					prior = 2;
				}
			}
		}
		
		boolean ret = super.navigationMovement(dx, dy, status, time);
		
		f = this.getLeafFieldWithFocus();
		if (f != null && f instanceof WrappedTextField) {
			Manager m = f.getManager();
			if (m != null && m instanceof MessageRowManager) {
				WrappedTextField wf = (WrappedTextField) f;
				MessageRowManager r = (MessageRowManager) m;
				
				if (wf.isFocus() && !r.getNameField().equals(wf) && !r.getBodyField().equals(wf)) {
					switch (prior) {
						case 1: {
							if (dx < 0 || dy < 0) {
								int idx = this.manager.messageField.getFieldWithFocusIndex();
								if (idx > 0) {
									Field newField = this.manager.messageField.getField(idx - 1);
									newField.setFocus();
								} else {
									this.manager.inputField.setFocus();
								}
							}
						} break;
						case 2: {
							if (dx > 0 || dy > 0) {
								int count = this.manager.messageField.getFieldCount();
								int idx = this.manager.messageField.getFieldWithFocusIndex();
								if (idx < count - 1) {
									Field newField = this.manager.messageField.getField(idx + 1);
									newField.setFocus();
								} else {
									this.manager.inputField.setFocus();
								}
							}
						} break;
					}
				}
			}
		} else if (f != null) {
			this.manager.messageField.scrollBottom();
		}
		
		return ret;
	}
	
	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);
		
		menu.deleteAll();
		
        menu.add(MenuItem.separator(0x230010));

        menu.add(new MenuItem("Invite Contact", 0x230010, 0) {
            public void run() {
                
            }});
        
        menu.add(MenuItem.separator(0x230010));
        
        menu.add(new MenuItem("Clear Conversation", 0x230010, 0) {
            public void run() {
            	Hashtable lastMessage = ConversationScreen.this.manager.getLastMessage();
            	long lastMessageTime = ((Long) lastMessage.get("time")).longValue();
            	ConversationStore.getInstance().updateConversation(ConversationScreen.this.conversation, lastMessageTime + 1, lastMessageTime + 1);
            	ConversationScreen.this.close();
            }});
        
        menu.add(MenuItem.separator(0x230010));
        
        menu.add(new MenuItem("Leave Conversation", 0x230010, 0) {
            public void run() {
            	HttpRequest req = new HttpRequest(Formatter.formatMessage(
        				HttpRequest.MSGS_LEAVE_CONVERSATION_FORMAT,
        				new String[] {
        					Integer.toString(ConversationScreen.this.conversation),
        					AccountStore.getInstance().getAuthToken()
        				}), new HttpRequestDelegate() {
							public void onRequestSending(int sent, int total, HttpRequest request) {}
							public void onRequestReceiving(int received, int total, HttpRequest request) {}
					
							public void onRequestFailure(HttpRequest request) {
							}

							public void onRequestComplete(String response, HttpRequest request) {
								ConversationScreen.this.close();
							}
            			});
            	
            	req.send();   
            }});
	}
	
	private static class ConversationLayoutManager extends Manager implements ContactStoreDelegate, MessageStoreDelegate, ConversationStoreDelegate, ConversationParticipantStoreDelegate, ConversationParticipantPrimaryStoreDelegate, UserStoreDelegate, InputFieldListener, HttpRequestDelegate {
		private int conversation = -1;
		private int participant = -1;
		
		private StyledTextLabel titleField = null;
	    private MessageListField messageField = null;
	    private InputField inputField = null;
		
		public ConversationLayoutManager(int conversation, int participant) {
			super(0);
			
			this.conversation = conversation;
			this.participant = participant;
			
	        ContactStore.getInstance().setDelegate(this);
	        MessageStore.getInstance().setDelegate(this);
	        ConversationStore.getInstance().setDelegate(this);
	        ConversationStore.getInstance().setParticipantDelegate(this);
	        ConversationStore.getInstance().setPrimaryParticipantDelegate(this);
	        UserStore.getInstance().setDelegate(this);
			
	        this.titleField = new StyledTextLabel("", DrawStyle.ELLIPSIS | Field.READONLY, 0xFFFFFF, Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight() + 1));
	        this.messageField = new MessageListField(conversation);
	        this.inputField = new InputField(this);
			
	        this.add(this.titleField);
			this.add(this.messageField);
			this.add(this.inputField);
			
			this.refreshTitle();
		}

		public void paint(Graphics g) {
	    	g.setColor(0x000000);
	    	g.fillRect(0, 0, this.getWidth(), this.titleField.getFont().getHeight() + 20);
	    	super.paint(g);
		}
		
		protected void sublayout(int width, int height) {
			int titleHeight = this.titleField.getFont().getHeight() + 20;
			
	        this.layoutChild(this.inputField, width, height);
	        this.setPositionChild(this.inputField, 0, height - this.inputField.getHeight());
	        
	        this.layoutChild(this.messageField, width, height - this.inputField.getHeight() - titleHeight);
	        this.setPositionChild(this.messageField, 0, titleHeight);
	        
	        this.layoutChild(this.titleField, width, this.titleField.getFont().getHeight() + 20);
	        this.setPositionChild(this.titleField, 10, 10);
	        
	        this.setExtent(width, height);
		}
		
		private void refreshTitle() {
			String title = null;
			
			if (this.conversation > 0) {
				Vector participants = ConversationStore.getInstance().getParticipants(this.conversation);
				if (participants.size() == 2) {
					for (int r = 0; r < participants.size(); r++) {
						Hashtable participant = (Hashtable) participants.elementAt(r);
						int userID = ((Integer) participant.get("user")).intValue();
						if (userID != AccountStore.getInstance().getUserID()) {
							Hashtable user = UserStore.getInstance().getByID(userID);
							title = (String) user.get("name");
							break;
						}
					}
				} else {
					long firstJoin = -1;
					
					for (int r = 0; r < participants.size(); r++) {
						Hashtable participant = (Hashtable) participants.elementAt(r);
						int userID = ((Integer) participant.get("user")).intValue();
						if (userID != AccountStore.getInstance().getUserID()) {
							long joinTime = ((Long) participant.get("jointime")).longValue();
							if (firstJoin < 0 || joinTime < firstJoin) {
								Hashtable user = UserStore.getInstance().getByID(userID);
								title = (String) user.get("name");
								firstJoin = joinTime;
							}
						}
					}
					
					title += " +" + Integer.toString(participants.size() - 2);
				}
			} else {
				Hashtable user = UserStore.getInstance().getByID(this.participant);
				title = (String) user.get("name");
			}
			
			if (title != null) {
				final String titleText = title;
		        UiApplication.getUiApplication().invokeLater(new Runnable() {
		            public void run() {
		            	ConversationLayoutManager.this.titleField.setText(titleText);
		            	ConversationLayoutManager.this.invalidate();     
		            } 
		        });
			}
		}

		public void redrawInputHeight() { this.updateLayout(); }
		public void receivedInputMessage(String message) {
			this.inputField.reset();
			
	        URLEncodedPostData encoder = new URLEncodedPostData("UTF-8", false);
	        encoder.append("m", message);
	        String encoded = encoder.toString().substring(2);
			
			HttpRequest req = new HttpRequest(Formatter.formatMessage(
					this.conversation > 0 ? HttpRequest.MSGS_SEND_MESSAGE_FORMAT : HttpRequest.MSGS_SEND_MESSAGE_DIRECT_FORMAT,
					new String[] {
						Integer.toString(this.conversation > 0 ? this.conversation : this.participant),
						encoded,
						AccountStore.getInstance().getRequestAuthentication()
					}), this);
			
			req.send();
		}

		public void onRequestFailure(HttpRequest request) {}
		public void onRequestSending(int sent, int total, HttpRequest request) {}
		public void onRequestReceiving(int received, int total, HttpRequest request) {}
		public void onRequestComplete(String response, HttpRequest request) {
			try {
				JSONObject json = new JSONObject(response);
				String action = json.optString("action");
				if (action.equals("leave")) {
					
				} else if (this.conversation < 0 && json.has("conversation") && action.equals("message")) {
					this.conversation = json.getInt("conversation");
					this.messageField.setConversation(this.conversation);
				}
			} catch (Exception e) {
				ErrorControl.logException(e);
			}
		}

		private Hashtable getLastMessage() { return this.messageField.getLastMessage(); }
		
		public int listeningConversation() { return this.conversation; }
		public int listeningParticipant() { return this.participant; }
		public void onUserEvent(Hashtable user) { this.messageField.onUserEvent(user); }
		public void onConversationEvent(Hashtable conversation) { this.messageField.onConversationEvent(conversation); this.refreshTitle(); }
		public void onMessageEvent(Hashtable message) { this.messageField.onMessageEvent(message); }
		public void onContactEvent(Hashtable contact) { this.messageField.onContactEvent(contact); this.refreshTitle(); }
		public void onConversationParticipantEvent(Hashtable participant) { this.messageField.onConversationParticipantEvent(participant); this.refreshTitle();  }
		public void onMessageEvents(long earliestMessage) {}
		public void onContactEvents(long earliestMessage) {}
		
		public void onPrimaryConversationEvent(Hashtable conversation) {
			this.conversation = ((Integer) conversation.get("id")).intValue();
			this.messageField.onPrimaryConversationEvent(conversation);
		}
	}
	
}
