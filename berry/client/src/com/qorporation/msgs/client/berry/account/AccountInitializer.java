package com.qorporation.msgs.client.berry.account;

import javax.microedition.global.Formatter;

import org.json.JSONObject;

import com.qorporation.msgs.client.berry.networking.HttpRequest;
import com.qorporation.msgs.client.berry.networking.HttpRequestDelegate;
import com.qorporation.msgs.client.berry.store.AccountStore;

public class AccountInitializer implements HttpRequestDelegate {

	private AccountInitializerDelegate delegate = null;
	private HttpRequest request = null;

	public AccountInitializer(AccountInitializerDelegate delegate) {
		this.delegate = delegate;
	}
	
	public void authenticate() {
		this.request = new HttpRequest(Formatter.formatMessage(
				HttpRequest.AUTH_REQUEST_URL_FORMAT,
				new String[] {
					AccountStore.getInstance().getDeviceIdent(),
					AccountStore.getInstance().getDeviceVerifier(),
					AccountStore.getInstance().getAuthToken()
				}), this);
		
		this.request.send();
	}
	
	public void onRequestFailure(HttpRequest request) {
		this.delegate.onAccountInitializationFailed();
	}

	public void onRequestSending(int sent, int total, HttpRequest request) {
	}

	public void onRequestReceiving(int received, int total, HttpRequest request) {
	}

	public void onRequestComplete(String response, HttpRequest request) {
		try {
			JSONObject json = new JSONObject(response);
			String status = json.getString("status");
			if (status != null && status.equals("ok")) {
				String userID = json.getString("user_id");
				String userName = json.getString("user_name");
				String userEmail = json.getString("user_email");
				String userEmailPending = json.getString("user_email_pending");
				String userPhone = json.getString("user_phone");
				String userPhonePending = json.getString("user_phone_pending");
				String authToken = json.getString("auth_token");
				String authSecret = json.getString("auth_secret");
				
				AccountStore.getInstance().setUserID(userID);
				AccountStore.getInstance().setUserName(userName);
				AccountStore.getInstance().setUserEmail(userEmail);
				AccountStore.getInstance().setUserEmailPending(userEmailPending);
				AccountStore.getInstance().setUserPhone(userPhone);
				AccountStore.getInstance().setUserPhonePending(userPhonePending);
				AccountStore.getInstance().setAuthToken(authToken);
				AccountStore.getInstance().setAuthSecret(authSecret);
				
				this.delegate.onAccountInitialized(userID, userEmail, authToken, authSecret);
			} else {
				this.delegate.onAccountInitializationFailed();
			}
		} catch (Exception e) {
			this.delegate.onAccountInitializationException(e);
		}
	}

}
