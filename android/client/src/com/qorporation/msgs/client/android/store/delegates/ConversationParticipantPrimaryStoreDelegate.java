package com.qorporation.msgs.client.android.store.delegates;

import java.util.HashMap;

public interface ConversationParticipantPrimaryStoreDelegate {

	public int listeningParticipant();
	public void onPrimaryConversationEvent(HashMap<String, Object> conversation);
	
}
