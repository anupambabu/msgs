package com.qorporation.msgs.client.berry.account;

public interface AccountInitializerDelegate {

	public void onAccountInitialized(String userID, String userEmail, String authToken, String authSecret);
	public void onAccountInitializationFailed();
	public void onAccountInitializationException(Exception e);
	
}
