package com.qorporation.msgs.server.messenger.cluster;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.messenger.MessengerService;
import com.qorporation.qluster.cluster.ClusterNode;
import com.qorporation.qluster.cluster.ClusterProtocol;
import com.qorporation.qluster.cluster.ClusterService;
import com.qorporation.qluster.cluster.socket.SocketConnection;
import com.qorporation.qluster.common.FutureResponse;
import com.qorporation.qluster.common.ImmediateFuture;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.service.EventDispatcher;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.util.ErrorControl;

public class MessengerProtocol extends ClusterProtocol {

	public static enum Operation {
		REGISTER, LOOKUP, MESSAGE, EVENT
	}
	
	private ServiceManager serviceManager = null;
	private MessengerService messengerService = null;
	
	private InetAddress localAddress = null;
	private ConcurrentHashMap<String, byte[]> deviceRegistry = null;
	private ConcurrentHashMap<Long, FutureResponse<Object>> responses = null;
	
	public MessengerProtocol(EventDispatcher eventDispatcher, ClusterService clusterService, Integer port) {
		super(eventDispatcher, clusterService, port);
		this.localAddress = this.heartbeat.getLocalAddress();
		this.deviceRegistry = new ConcurrentHashMap<String, byte[]>();
		this.responses = new ConcurrentHashMap<Long, FutureResponse<Object>>();
	}
	
	@Override
	public void init(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
		this.messengerService = this.serviceManager.getService(MessengerService.class);
	}

	@Override
	public Future<byte[]> onRequest(byte[] request) {
		ByteBuffer buffer = ByteBuffer.wrap(request);
		
		long reqID = buffer.getLong();
		int operation = buffer.getInt();
		
		switch (Operation.values()[operation]) {
			case REGISTER: {
				int deviceLen = buffer.getInt();
				byte[] deviceBytes = new byte[deviceLen];
				buffer.get(deviceBytes);
				String device = new String(deviceBytes);
				
				int addrLen = buffer.getInt();
				byte[] addrBytes = new byte[addrLen];
				buffer.get(addrBytes);
				
				this.deviceRegistry.put(device, addrBytes);
				
				ByteBuffer response = ByteBuffer.allocate(8 + 4 + addrLen);
				response.putLong(reqID);
				response.putInt(operation);
				response.put(addrBytes);
				
				return new ImmediateFuture<byte[]>(response.array());
			}
			
			case LOOKUP: {
				int profileLen = buffer.getInt();
				byte[] profileBytes = new byte[profileLen];
				buffer.get(profileBytes);
				String profile = new String(profileBytes);
				
				byte[] addrBytes = this.deviceRegistry.get(profile);
				if (addrBytes == null) {
					addrBytes = new byte[]{};
				}
				
				ByteBuffer response = ByteBuffer.allocate(8 + 4 + addrBytes.length);
				response.putLong(reqID);
				response.putInt(operation);
				response.put(addrBytes);
				
				return new ImmediateFuture<byte[]>(response.array());
			}
			
			case EVENT: {
				int deviceLen = buffer.getInt();
				byte[] deviceBytes = new byte[deviceLen];
				buffer.get(deviceBytes);
				String device = new String(deviceBytes);
				
				int eventLen = buffer.getInt();
				byte[] eventBytes = new byte[eventLen];
				buffer.get(eventBytes);
				String event = new String(eventBytes);
				
				return this.messengerService.protocolDeliverEvent(reqID, operation, device, event);
			}
		}
		
		ByteBuffer response = ByteBuffer.allocate(8 + 4);
		response.putLong(reqID);
		response.putInt(operation);
		
		return new ImmediateFuture<byte[]>(response.array());
	}

	@Override
	public Future<byte[]> onResponse(byte[] response) {
		ByteBuffer buffer = ByteBuffer.wrap(response);
		
		long reqID = buffer.getLong();
		int operation = buffer.getInt();
		
		switch (Operation.values()[operation]) {
			case REGISTER: {
				byte[] bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				
				this.responses.get(reqID).set(new String(bytes));
				break;
			}
			
			case LOOKUP: {
				byte[] addrBytes = new byte[buffer.remaining()];
				buffer.get(addrBytes);
				
				try {
					InetAddress addr = InetAddress.getByAddress(addrBytes);
					ClusterNode node = new ClusterNode(addr);
					this.responses.get(reqID).set(node);
				} catch (Exception e) {
					this.responses.get(reqID).set(ClusterNode.NONE);
				}
				
				break;
			}
			
			case MESSAGE: {
				this.responses.get(reqID).set(buffer.getInt());
				break;
			}	
			
			case EVENT: {
				this.responses.get(reqID).set(buffer.getInt());
				break;
			}
		}
		
		return new ImmediateFuture<byte[]>(response);
	}
	
	public FutureResponse<Object> registerDevice(Entity<UserDevice> device) {
		long reqID = this.requestCounter.getAndIncrement();
		
		ClusterNode node = this.heartbeat.hashPosition(device.getKey());
		SocketConnection connection = this.connectionPool.getConnection(node);
		
		ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 4 + device.getKey().getBytes().length + 4 + this.localAddress.getHostAddress().getBytes().length);
		
		buffer.putLong(reqID);
		buffer.putInt(Operation.REGISTER.ordinal());
		buffer.putInt(device.getKey().getBytes().length);
		buffer.put(device.getKey().getBytes());
		buffer.putInt(this.localAddress.getAddress().length);
		buffer.put(this.localAddress.getAddress());
		
		FutureResponse<Object> ret = new FutureResponse<Object>();
		this.responses.put(reqID, ret);
		
		connection.send(buffer.array());
		
		return ret;
	}
	
	public FutureResponse<Object> lookupDevice(Entity<UserDevice> device) {
		long reqID = this.requestCounter.getAndIncrement();
		
		ClusterNode node = this.heartbeat.hashPosition(device.getKey());
		SocketConnection connection = this.connectionPool.getConnection(node);
		
		ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 4 + device.getKey().getBytes().length);
		
		buffer.putLong(reqID);
		buffer.putInt(Operation.LOOKUP.ordinal());
		buffer.putInt(device.getKey().getBytes().length);
		buffer.put(device.getKey().getBytes());
		
		FutureResponse<Object> ret = new FutureResponse<Object>();
		this.responses.put(reqID, ret);
		
		connection.send(buffer.array());
		
		return ret;
	}
	
	public FutureResponse<Object> deliverEvent(ClusterNode node, Entity<UserDevice> device, Entity<UserEvent> event) {
		long reqID = this.requestCounter.getAndIncrement();
		
		SocketConnection connection = this.connectionPool.getConnection(node);
		
		ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 4 + device.getKey().getBytes().length + 4 + event.getKey().getBytes().length);
		
		buffer.putLong(reqID);
		buffer.putInt(Operation.EVENT.ordinal());
		buffer.putInt(device.getKey().getBytes().length);
		buffer.put(device.getKey().getBytes());
		buffer.putInt(event.getKey().getBytes().length);
		buffer.put(event.getKey().getBytes());
		
		FutureResponse<Object> ret = new FutureResponse<Object>();
		this.responses.put(reqID, ret);
		
		connection.send(buffer.array());
		
		return ret;
	}
	
	public Integer deliverEvent(Entity<UserDevice> device, Entity<UserEvent> event) {
		try {
			FutureResponse<Object> lookupResponse = this.lookupDevice(device);
			ClusterNode node = (ClusterNode) lookupResponse.get();
			
			if (!node.equals(ClusterNode.NONE)) {
				FutureResponse<Object> messageResponse = this.deliverEvent(node, device, event);
				return (Integer) messageResponse.get();
			} else {
				return -1;
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
			return -1;
		}
	}

}
