package com.qorporation.msgs.server.messenger.endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.msgs.server.entity.definition.Contact;
import com.qorporation.msgs.server.entity.definition.Conversation;
import com.qorporation.msgs.server.entity.definition.ConversationParticipant;
import com.qorporation.msgs.server.entity.definition.Message;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.messenger.MessengerService;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.transaction.TransactionService;
import com.qorporation.qluster.util.ErrorControl;

public class MessengerEndpointHandler extends SimpleChannelUpstreamHandler {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private TransactionService transactionService = null;
	private MessengerService messengerService = null;
	private ChannelGroup channels = null;

	private ConcurrentHashMap<Channel, Entity<UserDevice>> deviceByChannel = null;
	private ConcurrentHashMap<Entity<UserDevice>, Channel> channelByDevice = null;
	
    public MessengerEndpointHandler(TransactionService transcationService, MessengerService messengerService) {
    	this.transactionService = transcationService;
    	this.messengerService = messengerService;
    	this.channels = new DefaultChannelGroup();
    	this.channelByDevice = new ConcurrentHashMap<Entity<UserDevice>, Channel>();
    	this.deviceByChannel = new ConcurrentHashMap<Channel, Entity<UserDevice>>();
	}

	@Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) { this.logger.info(e.toString()); }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    	if (MessengerEndpoint.USE_SSL) {
    		SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
        	ChannelFuture handshakeFuture = sslHandler.handshake();
        	handshakeFuture.addListener(new SslHandershakeListener());
    	} else {
    		this.channels.add(e.getChannel());
    	}
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        this.channels.remove(e.getChannel());
        Entity<UserDevice> device = this.deviceByChannel.remove(e.getChannel());
        if (device != null) {
        	this.channelByDevice.remove(device);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    	Transaction t = this.transactionService.startGlobalTransaction();
    	try {
	        String request = (String) e.getMessage();
	        JSONObject json = JSONObject.fromObject(request);
	        
	        String cmd = json.getString("cmd");
	        if (cmd.equals("connect")) {
	        	String deviceType = json.getString("devicetype");
	        	String deviceIdent = json.getString("deviceident");
	        	String authToken = json.getString("authtoken");
	        	String lastSync = json.getString("lastsync");
	        	Entity<UserDevice> device = this.messengerService.endpointValidateDevice(deviceType, deviceIdent, authToken);
	        	if (device != null) {
	            	this.channelByDevice.put(device, e.getChannel());
	            	this.deviceByChannel.put(e.getChannel(), device); 
	            	this.messengerService.endpointRegisterDevice(device);
	            	
	            	List<Entity<UserEvent>> events = this.messengerService.endpointRetrieveEvents(device, Long.parseLong(lastSync));
	
	            	Map<String, Object> payload = new HashMap<String, Object>();
	            	payload.put("status", "ok");
	            	payload.put("type", "events");
	            	payload.put("events", serializeEvents(events));
          	
	            	e.getChannel().write(JSONSerializer.toJSON(payload).toString());
	        	} else {
	        		Map<String, Object> payload = new HashMap<String, Object>();
	        		payload.put("status", "error");
	        		e.getChannel().write(JSONSerializer.toJSON(payload).toString());
	        	}
	        } else if (cmd.equals("ack")) {
	        	Entity<UserDevice> device = this.deviceByChannel.get(e.getChannel());
	        	if (device != null) {
	        		Set<String> checksum = new HashSet<String>();
	        		JSONArray checksumArray = json.getJSONArray("checksum");
	        		for (int i = 0; i < checksumArray.size(); i++) {
	        			checksum.add(checksumArray.getString(i));
	        		}
	        		
	        		String checkSync = json.getString("checksync");
	        		String lastSync = json.getString("lastsync");
	        		
	        		Set<String> missing = new HashSet<String>();
	        		List<Entity<UserEvent>> events = this.messengerService.endpointRetrieveEvents(device, Long.parseLong(lastSync), Long.parseLong(checkSync));
	        		for (Entity<UserEvent> event: events) {
	        			if (!checksum.contains(event.getKey())) {
	        				missing.add(event.getKey());
	        			}
	        		}
	        		
	        		if (missing.size() > 0) {
		            	Map<String, Object> payload = new HashMap<String, Object>();
		            	payload.put("status", "ok");
		            	payload.put("type", "events");
		            	payload.put("events", serializeEvents(events));
		            	
		            	e.getChannel().write(JSONSerializer.toJSON(payload).toString());	        			
	        		} else {
		            	Map<String, Object> payload = new HashMap<String, Object>();
		            	payload.put("status", "ok");
		            	payload.put("type", "checksum");
		            	payload.put("lastsync", checkSync);
		            	
		            	this.messengerService.endpointUpdateUserSync(device, Long.parseLong(lastSync), Long.parseLong(checkSync));
		            	
		            	e.getChannel().write(JSONSerializer.toJSON(payload).toString());	        			
	        		}
	        	} else {
	        		this.logger.warn(String.format("Got free ack from unregistered channel: %s", request));
	        	}	        	
	        } else {
	        	Entity<UserDevice> device = this.deviceByChannel.get(e.getChannel());
	        	if (device != null) {
	        		this.messengerService.endpointResponse(device, cmd, json);
	        	} else {
	        		this.logger.warn(String.format("Got free request from unregistered channel: %s", request));
	        	}
	        }
	        
        	t.finish();
    	} catch (Exception ex) {
    		ErrorControl.logException(ex);
    		t.failure();
    	}
    }

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        this.channels.remove(e.getChannel());
        Entity<UserDevice> device = this.deviceByChannel.remove(e.getChannel());
        if (device != null) {
        	this.channelByDevice.remove(device);
        }
        
        this.logger.info(e.toString());
        e.getChannel().close();
    }

    private final class SslHandershakeListener implements ChannelFutureListener {
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
            	MessengerEndpointHandler.this.channels.add(future.getChannel());
            } else {
                future.getChannel().close();
            }
        }
    }
	
    private List<Map<String, Object>> serializeEvents(List<Entity<UserEvent>> events) {
    	List<Map<String, Object>> payload = new ArrayList<Map<String, Object>>(events.size());
    	
    	for (Entity<UserEvent> event: events) {
    		Map<String, Object> m = new HashMap<String, Object>();
    		
    		m.put("id", event.getKey());
    		m.put("time", event.get(UserEvent.time).getTime());
    		
    		if (event.get(UserEvent.message) != null) {
    			m.put("type", "message");
    			serializeMessage(m, event.get(UserEvent.message));
    		} else if (event.get(UserEvent.contact) != null) {
    			m.put("type", "contact");
    			serializeContact(m, event.get(UserEvent.contactAction), event.get(UserEvent.contact));
    		} else if (event.get(UserEvent.conversation) != null) {
    			m.put("type", "conversation");
    			serializeConversation(m, event.get(UserEvent.conversationAction), event.get(UserEvent.conversation));
    		} else if (event.get(UserEvent.participant) != null) {
    			m.put("type", "participant");
    			serializeParticipant(m, event.get(UserEvent.participantAction), event.get(UserEvent.participant));
    		} else if (event.get(UserEvent.avatar) != null) {
    			m.put("type", "avatar");
    			serializeAvatar(m, event.get(UserEvent.avatar));
    		}
    		
    		payload.add(m);
    	}
    	
    	return payload;
	}
	
	private void serializeParticipant(Map<String, Object> payload, String action, Entity<ConversationParticipant> participant) {
		payload.put("action", action);
		payload.put("participant", participant.getKey());
		payload.put("conversation", participant.get(ConversationParticipant.conversation).getKey());
		payload.put("conversationsync", participant.get(ConversationParticipant.lastSync).getTime());
		payload.put("jointime", participant.get(ConversationParticipant.joinTime).getTime());
		payload.put("user", participant.get(ConversationParticipant.participant).getKey());
		payload.put("name", participant.get(ConversationParticipant.participant).get(User.name));
		payload.put("status", participant.get(ConversationParticipant.participant).get(User.status));
		payload.put("avatar", participant.get(ConversationParticipant.participant).get(User.avatar));
		payload.put("usersync", participant.get(ConversationParticipant.participant).get(User.lastSync).getTime());
	}
    
	private void serializeConversation(Map<String, Object> payload, String action, Entity<Conversation> conversation) {
		payload.put("action", action);
		payload.put("conversation", conversation.getKey());
		payload.put("privacy", conversation.get(Conversation.type));
		payload.put("lastmessage", conversation.get(Conversation.lastMessageTime).getTime());
	}
    
	private void serializeContact(Map<String, Object> payload, String action, Entity<Contact> contact) {
		payload.put("action", action);
		payload.put("contact", contact.getKey());
		payload.put("user", contact.get(Contact.contact).getKey());
		payload.put("status", contact.get(Contact.status));
	}
	
	private void serializeAvatar(Map<String, Object> payload, Entity<User> avatar) {
		payload.put("user", avatar.getKey());
		payload.put("name", avatar.get(User.name));
		payload.put("status", avatar.get(User.status));
		payload.put("avatar", avatar.get(User.avatar));
		payload.put("lastsync", avatar.get(User.lastSync).getTime());
	}

	private void serializeMessage(Map<String, Object> payload, Entity<Message> message) {
		payload.put("message", message.getKey());
		payload.put("sender", message.get(Message.sender).getKey());
		payload.put("conversation", message.get(Message.conversation).getKey());
		payload.put("body", message.get(Message.body));
	}

	@SuppressWarnings("unchecked")
	public boolean deliverEvent(Entity<UserDevice> device, Entity<UserEvent> event) {
		Channel channel = this.channelByDevice.get(device);
		if (channel == null || !channel.isOpen()) {
			return false;
		}
		
    	Map<String, Object> payload = new HashMap<String, Object>();
    	payload.put("status", "ok");
    	payload.put("type", "events");
    	payload.put("events", serializeEvents(Arrays.asList(event)));
    	
    	channel.write(JSONSerializer.toJSON(payload).toString());
		
		return true;
	}
	
}
