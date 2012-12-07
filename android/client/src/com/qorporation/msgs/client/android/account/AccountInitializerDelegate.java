package com.qorporation.msgs.client.android.account;

public interface AccountInitializerDelegate {

	public void onAccountInitialized(String userID, String userEmail, String authToken, String authSecret);
	public void onAccountInitializationFailed();
	
}
