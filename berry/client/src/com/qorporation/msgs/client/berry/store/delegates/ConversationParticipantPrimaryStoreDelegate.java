package com.qorporation.msgs.client.berry.store.delegates;

import java.util.Hashtable;

public interface ConversationParticipantPrimaryStoreDelegate {

	public int listeningParticipant();
	public void onPrimaryConversationEvent(Hashtable conversation);
	
}
