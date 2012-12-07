package com.qorporation.msgs.client.berry.store.delegates;

import java.util.Hashtable;

public interface MessageStoreDelegate {

	public int listeningConversation();
	public void onMessageEvent(Hashtable message);
	public void onMessageEvents(long earliestMessage);
	
}
