package com.qorporation.msgs.client.android.networking;

import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.qorporation.msgs.client.android.Msgs;
import com.qorporation.msgs.client.android.util.JSONHelper;

public class HttpRequest {
	public static final String HOST_ADDRESS = "http://" + Msgs.HOST + ":8080";
	public static final String AUTH_REQUEST_URL_FORMAT = HOST_ADDRESS + "/api/auth/device/iphone/%s/%s/%s";
	public static final String MSGS_UPDATE_ACCOUNT_FORMAT = HOST_ADDRESS + "/api/auth/update?name=%s&email=%s&phone=%s&%s";
	public static final String MSGS_CONTACT_SEARCH_FORMAT = HOST_ADDRESS + "/api/contacts/list?%s";
	public static final String MSGS_CONTACT_ACCEPT_FORMAT = HOST_ADDRESS + "/api/contacts/accept/%s?%s";
	public static final String MSGS_CONTACT_REQUEST_FORMAT = HOST_ADDRESS + "/api/contacts/request/%s?%s";
	public static final String MSGS_CONTACT_REQUEST_BY_EMAIL_FORMAT = HOST_ADDRESS + "/api/contacts/request/email/%s?%s";
	public static final String MSGS_CONTACT_REQUEST_BY_PHONE_FORMAT = HOST_ADDRESS + "/api/contacts/request/phone/%s?%s";
	public static final String MSGS_CONTACT_IGNORE_FORMAT = HOST_ADDRESS + "/api/contacts/ignore/%s?%s";
	public static final String MSGS_CONTACT_DELETE_FORMAT = HOST_ADDRESS + "/api/contacts/delete/%s?%s";
	public static final String MSGS_SEND_MESSAGE_FORMAT = HOST_ADDRESS + "/api/conversations/send/%s?message=%s&%s";
	public static final String MSGS_SEND_MESSAGE_DIRECT_FORMAT = HOST_ADDRESS + "/api/messages/send/%s?message=%s&%s";
	public static final String MSGS_LEAVE_CONVERSATION_FORMAT = HOST_ADDRESS + "/api/conversations/leave/%s?%s";
	public static final String MSGS_SYNC_CONVERSATIONS_FORMAT = HOST_ADDRESS + "/api/conversations/sync?%s";
	
	private String url = null;
	private HashMap<String, Object> postData = null;
	private HttpRequestDelegate delegate = null;
	private Thread requestThread;

	public HttpRequest(String url, HttpRequestDelegate delegate) {
		this(url, null, delegate);
	}
	
	public HttpRequest(String url, HashMap<String, Object> postData, HttpRequestDelegate delegate) {
		this.url = url;
		this.postData = postData;
		this.delegate = delegate;
	}
	
	public void send() {
		this.requestThread = new Thread(new Runnable() {

			public void run() {
				try {                    
					HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(HttpRequest.this.url);
			        int sending = 0;
			        
			        if (HttpRequest.this.postData != null) {			            
						JSONObject json = JSONHelper.fromMap(HttpRequest.this.postData);
						
						String jsonString = json.toString();
						byte[] bytes = jsonString.getBytes("UTF-8");
						sending = bytes.length;
						
						post.setEntity(new ByteArrayEntity(bytes));
			        }
			        
			        HttpRequest.this.delegate.onRequestSending(0, sending, HttpRequest.this);
			        
			        HttpResponse response = client.execute(post);
			        
			        HttpRequest.this.delegate.onRequestSending(sending, sending, HttpRequest.this);

			        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			        	HttpEntity entity = response.getEntity();
			        	int expecting = (int) entity.getContentLength();
			        	if (expecting > 0) {
				        	int offset = 0;
				        	byte[] data = new byte[expecting];
				        	
				        	InputStream input = entity.getContent();
				        	while (input.available() > 0 || offset < expecting) {
				        		int read = input.read(data, offset, expecting);
				        		offset += read;
				        		expecting -= read;
				        		
				        		HttpRequest.this.delegate.onRequestReceiving(offset, offset + expecting, HttpRequest.this);
				        	}
				        	
				        	String responseData = new String(data, "UTF-8");
				        	HttpRequest.this.delegate.onRequestComplete(responseData, HttpRequest.this);
				        	
				        	input.close(); 
			        	} else {
			        		HttpRequest.this.delegate.onRequestFailure(HttpRequest.this);
			        	}
			        } else {                            
			        	HttpRequest.this.delegate.onRequestFailure(HttpRequest.this);
			        }                
				} catch (Exception e) {
					System.out.println(e.toString());                        
					HttpRequest.this.delegate.onRequestFailure(HttpRequest.this);
				}
			}
			
		});
		
		this.requestThread.start();
	}
	
}
