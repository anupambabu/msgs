package com.qorporation.msgs.client.berry.networking;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.transport.TransportInfo;

import com.qorporation.msgs.client.berry.Msgs;
import com.qorporation.msgs.client.berry.util.ErrorControl;
import com.qorporation.msgs.client.berry.util.JSONHelper;

public class HttpRequest {
	public static final String DEVICE_SIDE_CONSTANT = ";deviceside=true";
	
	public static final String HOST_ADDRESS = "http://" + Msgs.HOST + ":8080";
	public static final String AUTH_REQUEST_URL_FORMAT = HOST_ADDRESS + "/api/auth/device/blackberry/{0}/{1}/{2}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_UPDATE_ACCOUNT_FORMAT = HOST_ADDRESS + "/api/auth/update?name={0}&email={1}&phone={2}&{3}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_CONTACT_SEARCH_FORMAT = HOST_ADDRESS + "/api/contacts/list?{0}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_CONTACT_ACCEPT_FORMAT = HOST_ADDRESS + "/api/contacts/accept/{0}?{1}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_CONTACT_REQUEST_FORMAT = HOST_ADDRESS + "/api/contacts/request/{0}?{1}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_CONTACT_REQUEST_BY_EMAIL_FORMAT = HOST_ADDRESS + "/api/contacts/request/email/{0}?{1}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_CONTACT_REQUEST_BY_PHONE_FORMAT = HOST_ADDRESS + "/api/contacts/request/phone/{0}?{1}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_CONTACT_IGNORE_FORMAT = HOST_ADDRESS + "/api/contacts/ignore/{0}?{1}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_CONTACT_DELETE_FORMAT = HOST_ADDRESS + "/api/contacts/delete/{0}?{1}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_SEND_MESSAGE_FORMAT = HOST_ADDRESS + "/api/conversations/send/{0}?message={1}&{2}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_SEND_MESSAGE_DIRECT_FORMAT = HOST_ADDRESS + "/api/messages/send/{0}?message={1}&{2}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_LEAVE_CONVERSATION_FORMAT = HOST_ADDRESS + "/api/conversations/leave/{0}?{1}" + DEVICE_SIDE_CONSTANT;
	public static final String MSGS_SYNC_CONVERSATIONS_FORMAT = HOST_ADDRESS + "/api/conversations/sync?{0}" + DEVICE_SIDE_CONSTANT;
	
	private String url = null;
	private String postData = null;
	private HttpRequestDelegate delegate = null;
	private Thread requestThread = null;

	public HttpRequest(String url, HttpRequestDelegate delegate) {
		this.url = url;
		this.postData = null;
		this.delegate = delegate;
	}
	
	public HttpRequest(String url, String postData, HttpRequestDelegate delegate) {
		this.url = url;
		this.postData = postData;
		this.delegate = delegate;
	}
	
	public HttpRequest(String url, Hashtable postData, HttpRequestDelegate delegate) {
		this.url = url;
		this.postData = JSONHelper.fromHashtable(postData).toString();
		this.delegate = delegate;
	}
	
	public void send() {
		if (!TransportInfo.hasSufficientCoverage(TransportInfo.TRANSPORT_TCP_CELLULAR)
				&& !TransportInfo.hasSufficientCoverage(TransportInfo.TRANSPORT_TCP_WIFI)) {
			HttpRequest.this.delegate.onRequestFailure(HttpRequest.this);
			return;
		}
		
		this.requestThread = new Thread(new Runnable() {
			public void run() {
				try {                        
					int connectorFlags = HttpRequest.this.postData != null ? Connector.READ_WRITE : Connector.READ;
			        HttpConnection conn = (HttpConnection) Connector.open(HttpRequest.this.url, connectorFlags);
			        
			        if (HttpRequest.this.postData != null) {
						byte[] bytes = HttpRequest.this.postData.getBytes("UTF-8");
						int sending = bytes.length;
						
			        	conn.setRequestMethod(HttpConnection.POST);
			        	conn.setRequestProperty("content-type", "text/plain");
			        	conn.setRequestProperty("content-length", Integer.toString(sending));
			        	
			        	int sent = 0;
			        	OutputStream output = conn.openOutputStream();
			        	output.write(bytes);
			        	output.flush();
			        	output.close();
			        	sent = sending;
			        	sending = 0;
			        	
			        	HttpRequest.this.delegate.onRequestSending(sent, sent + sending, HttpRequest.this);
			        } else {
				        conn.setRequestMethod(HttpConnection.GET);
				        
				        HttpRequest.this.delegate.onRequestSending(0, 0, HttpRequest.this);
			        }
			        
			        if (conn.getResponseCode() == HttpConnection.HTTP_OK) {			
			        	int expecting = conn.getHeaderFieldInt("content-length", 0);
			        	if (expecting > 0) {
				        	int offset = 0;
				        	byte[] data = new byte[expecting];
	
				        	InputStream input = conn.openInputStream();
				        	while (input.available() > 0 || offset < expecting) {
				        		int read = input.read(data, offset, expecting);
				        		offset += read;
				        		expecting -= read;
				        		
				        		HttpRequest.this.delegate.onRequestReceiving(offset, offset + expecting, HttpRequest.this);
				        	}
				        	
				        	String response = new String(data, "UTF-8");
				        	HttpRequest.this.delegate.onRequestComplete(response, HttpRequest.this);
				        	
				        	input.close(); 
			        	} else {
			        		HttpRequest.this.delegate.onRequestFailure(HttpRequest.this);
			        	}
			        } else {                            
			        	HttpRequest.this.delegate.onRequestFailure(HttpRequest.this);
			        }

			        conn.close();                    
				} catch (Exception e) {
					ErrorControl.logException(e);
					HttpRequest.this.delegate.onRequestFailure(HttpRequest.this);
				}
			}
		});
		
		this.requestThread.start();
	}
	
}
