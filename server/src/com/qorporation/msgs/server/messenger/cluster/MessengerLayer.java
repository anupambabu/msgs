package com.qorporation.msgs.server.messenger.cluster;

import com.qorporation.qluster.cluster.layer.ClusterLayer;
import com.qorporation.qluster.service.ServiceManager;

public class MessengerLayer extends ClusterLayer {

	@Override
	public void activate(ServiceManager serviceManager) {
		System.out.println("Activating me!");
	}

	@Override
	public void deactivate(ServiceManager serviceManager) {
		System.out.println("Deactivating me!");
	}

}
