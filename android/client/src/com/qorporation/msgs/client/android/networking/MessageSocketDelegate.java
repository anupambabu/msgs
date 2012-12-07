package com.qorporation.msgs.client.android.networking;

public interface MessageSocketDelegate {

	public void onConnectionAttemptFailure(MessageSocket socket);
	public void onConnectionTermination(MessageSocket socket);
	public void onReceivedNetworkPacket(String message, MessageSocket socket);
	
}
