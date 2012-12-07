package com.qorporation.msgs.client.berry.store.delegates;

import java.util.Hashtable;

public interface ContactStoreDelegate {

	public void onContactEvent(Hashtable contact);
	public void onContactEvents(long firstEventTime);
	
}
