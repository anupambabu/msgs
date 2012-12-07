package com.qorporation.msgs.client.berry.networking;

public interface MessageSocketDelegate {

	public void onConnectionAttemptFailure(MessageSocket socket, Exception e);
	public void onConnectionTermination(MessageSocket socket);
	public void onReceivedNetworkPacket(String message, MessageSocket socket);
	
}
