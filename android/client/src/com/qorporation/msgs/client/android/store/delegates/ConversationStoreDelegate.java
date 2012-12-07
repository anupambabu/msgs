package com.qorporation.msgs.client.android.store.delegates;

import java.util.HashMap;

public interface ConversationStoreDelegate {

	public void onConversationEvent(HashMap<String, Object> conversation);
	
}
