package com.qorporation.msgs.client.berry.store.delegates;

import java.util.Hashtable;

public interface ConversationParticipantStoreDelegate {

	public int listeningConversation();
	public void onConversationParticipantEvent(Hashtable participant);
	
}
