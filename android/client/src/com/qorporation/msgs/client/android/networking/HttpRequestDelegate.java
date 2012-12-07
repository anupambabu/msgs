package com.qorporation.msgs.client.android.networking;

public interface HttpRequestDelegate {

	public void onRequestFailure(HttpRequest request);
	public void onRequestSending(int sent, int total, HttpRequest request);
	public void onRequestReceiving(int received, int total, HttpRequest request);
	public void onRequestComplete(String response, HttpRequest request);
	
}
