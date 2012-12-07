package com.qorporation.msgs.server.messenger.endpoint;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.qorporation.msgs.server.entity.definition.UserDevice;
import com.qorporation.msgs.server.entity.definition.UserEvent;
import com.qorporation.msgs.server.messenger.MessengerService;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.transaction.TransactionService;

public class MessengerEndpoint {
	protected static final int MESSENGER_ENDPOINT_PORT = 7456;
	protected static final boolean USE_SSL = false;
	
	private ServerBootstrap bootstrap = null;
	private MessengerEndpointHandler handler = null;
	
	public MessengerEndpoint(TransactionService transactionService, MessengerService messengerService) {
		this.handler = new MessengerEndpointHandler(transactionService, messengerService);
		
		this.bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		this.bootstrap.setPipelineFactory(new MessengerEndpointPipelineFactory(this.handler));
		this.bootstrap.bind(new InetSocketAddress(MESSENGER_ENDPOINT_PORT));
	}
	
	public boolean shutdown() {
		if (this.bootstrap != null && this.bootstrap.getPipeline().getChannel().isOpen()) {
			this.bootstrap.getPipeline().getChannel().close();
			return true;
		}
		
		return false;
	}
	
	public boolean deliverEvent(Entity<UserDevice> device, Entity<UserEvent> event) {
		return this.handler.deliverEvent(device, event);
	}
	
}
