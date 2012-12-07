package com.qorporation.msgs.server.messenger;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import net.sf.json.JSONObject;

import com.qorporation.msgs.server.entity.definition.Conversation;
import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.entity.manager.UserDeviceManager;
import com.qorporation.msgs.server.entity.manager.UserEventManager;
import com.qorporation.msgs.server.entity.manager.UserManager;
import com.qorporation.msgs.server.messenger.cluster.MessengerProtocol;
import com.qorporation.msgs.server.messenger.endpoint.MessengerEndpoint;
import com.qorporation.qluster.cluster.ClusterService;
import com.qorporation.qluster.common.ImmediateFuture;
import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.service.Service;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.transaction.TransactionService;

public class MessengerService extends Service {
	
	public static enum EndpointStatus {
		PASS, FAIL
	}
	
	private ClusterService clusterService = null;
	private TransactionService transactionService = null;
	
	private EntityService entityService = null;
	private UserDeviceManager deviceManager = null;
	private UserEventManager eventManager = null;
	
	private MessengerEndpoint messengerEndpoint = null;
	
	@Override
	public void init(ServiceManager serviceManager, Config config) {
		this.logger.info("Loading messenger service");
				
		this.clusterService = serviceManager.getService(ClusterService.class);
		this.transactionService = serviceManager.getService(TransactionService.class);
		
		this.entityService = serviceManager.getService(EntityService.class);
		this.deviceManager = this.entityService.getManager(UserDevice.class, UserDeviceManager.class);
		this.eventManager = this.entityService.getManager(UserEvent.class, UserEventManager.class);
		
		this.messengerEndpoint = new MessengerEndpoint(this.transactionService, this);
	}
	
	public Entity<UserDevice> endpointValidateDevice(String deviceType, String deviceIdent, String authToken) {
		Entity<UserDevice> device = this.deviceManager.getDeviceMapping(deviceType, deviceIdent);
		if (device == null) return null;
		if (!device.get(UserDevice.authToken).equals(authToken)) return null;
		return device;
	}

	public void endpointRegisterDevice(Entity<UserDevice> device) {
		this.getMessengerProtocol().registerDevice(device);
	}

	public List<Entity<UserEvent>> endpointRetrieveEvents(Entity<UserDevice> device, Long startTime) { return endpointRetrieveEvents(device, startTime, null); }
	public List<Entity<UserEvent>> endpointRetrieveEvents(Entity<UserDevice> device, Long startTime, Long endTime) {
		if (startTime.longValue() == 0) {
			Entity<User> user = device.get(UserDevice.user);
			List<Entity<UserEvent>> events = this.eventManager.retrieveFiltered(user, startTime, endTime, false);
			
			List<Entity<Conversation>> conversations = new ArrayList<Entity<Conversation>>();
			for (Entity<UserEvent> event: events) {
				Entity<Conversation> conversation = event.get(UserEvent.conversation);
				if (conversation != null) {
					conversations.add(conversation);
				}
			}
			
			List<Entity<UserEvent>> messageEvents = this.eventManager.getMessageLatestEvents(user, conversations);
			
			Iterator<Entity<UserEvent>> msgItr = messageEvents.iterator();
			Iterator<Entity<UserEvent>> eventItr = events.iterator();
			
			Entity<UserEvent> curMsg = msgItr.hasNext() ? msgItr.next() : null;
			Entity<UserEvent> curEvent = eventItr.hasNext() ? eventItr.next() : null;
			List<Entity<UserEvent>> merged = new ArrayList<Entity<UserEvent>>(events.size() + messageEvents.size());
			while (true) {
				if (curMsg == null && curEvent == null) break;
				
				if (curEvent == null && curMsg != null) {
					merged.add(curMsg);
					curMsg = msgItr.hasNext() ? msgItr.next() : null;
					continue;
				}
				
				if (curMsg == null && curEvent != null) {
					merged.add(curEvent);
					curEvent = eventItr.hasNext() ? eventItr.next() : null;
					continue;
				}
				
				if (curMsg.get(UserEvent.time).compareTo(curEvent.get(UserEvent.time)) <= 0) {
					merged.add(curMsg);
					curMsg = msgItr.hasNext() ? msgItr.next() : null;
					continue;					
				} else {
					merged.add(curEvent);
					curEvent = eventItr.hasNext() ? eventItr.next() : null;
					continue;					
				}
			}
			
			return merged;
		} else {
			return this.eventManager.retrieveFiltered(device.get(UserDevice.user), startTime, endTime, true);
		}
	}
	
	public boolean endpointUpdateUserSync(Entity<UserDevice> device, Long startTime) { return endpointUpdateUserSync(device, startTime, null); }
	public boolean endpointUpdateUserSync(Entity<UserDevice> device, Long startTime, Long endTime) {
		Entity<User> user = device.get(UserDevice.user);
		if (user.get(User.lastSync).getTime() < endTime) {
			user.set(User.lastSync, new Timestamp(endTime));
			return this.entityService.getManager(User.class, UserManager.class).save(user);
		} else {
			return false;
		}
	}

	public void endpointResponse(Entity<UserDevice> device, String cmd, JSONObject json) {
	}
	
	public Future<byte[]> protocolDeliverEvent(long reqID, int operation, String deviceID, String eventID) {
		Transaction t = this.transactionService.startGlobalTransaction();
		
		try {
			Entity<UserDevice> device = this.deviceManager.get(deviceID);
			Entity<UserEvent> event = this.eventManager.get(eventID);
			
			if (this.messengerEndpoint.deliverEvent(device, event)) {
				ByteBuffer response = ByteBuffer.allocate(16);
				response.putLong(reqID);
				response.putInt(operation);
				response.putInt(EndpointStatus.PASS.ordinal());
				
				t.finish();
				return new ImmediateFuture<byte[]>(response.array());
			} else {
				ByteBuffer response = ByteBuffer.allocate(16);
				response.putLong(reqID);
				response.putInt(operation);
				response.putInt(EndpointStatus.FAIL.ordinal());
				
				t.finish();
				return new ImmediateFuture<byte[]>(response.array());
			}
		} catch (Exception e) {
			t.failure();
			return null;
		}
	}
	
	public int deliverEvent(Entity<UserDevice> device, Entity<UserEvent> event) {
		return this.getMessengerProtocol().deliverEvent(device, event);
	}
	
	private MessengerProtocol getMessengerProtocol() {
		return this.clusterService.getProtocol(MessengerProtocol.class);
	}

}
