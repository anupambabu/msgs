package com.qorporation.msgs.client.android.store.delegates;

import java.util.HashMap;

public interface MessageStoreDelegate {

	public int listeningConversation();
	public void onMessageEvent(HashMap<String, Object> message);
	
}
