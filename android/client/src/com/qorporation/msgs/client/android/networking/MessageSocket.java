package com.qorporation.msgs.client.android.networking;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.json.JSONObject;

import com.qorporation.msgs.client.android.util.JSONHelper;

public class MessageSocket {
	public static final int PORT = 7456;
	
	private String host = null;
	private int port = -1;
	private MessageSocketDelegate delegate = null;
	private boolean connected = false;
	
	private Socket connection = null;

	private InputStream in = null;
	private Thread inThread = null;
	
	private OutputStream out = null;
	private Thread outThread = null;
	
	public MessageSocket(String host, int port, MessageSocketDelegate delegate) {
		this.host = host;
		this.port = port;
		this.delegate = delegate;
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	public boolean connect() {
        try {                                    
            this.connection = new Socket(this.host, this.port);
            this.startInThread();
            this.startOutThread();
            this.connected = true;
            
            return true;
        } catch (Exception e) {
            System.err.println(e.toString());
        	
            this.connected = false;
            try { this.connection.close(); } catch (Exception ex) {}
            
            this.delegate.onConnectionAttemptFailure(this);
        }
        
        return false;
	}

	private byte[] decompress(byte[] compressed) {
		try {
			Inflater inflater = new Inflater();
			inflater.setInput(compressed);
			
			byte[] decompressed = new byte[inflater.getTotalOut()];
			inflater.inflate(decompressed);
			
			return decompressed;
		} catch (Exception e) {
			return new byte[0];
		}
	}
	
	private void startInThread() {
		this.inThread = new Thread(new Runnable() {

			public void run() {
				try {
					MessageSocket.this.in = MessageSocket.this.connection.getInputStream();
					
					byte[] inbound = null;
					int expecting = -1;
					int offset = -1;
					
					while (MessageSocket.this.connected) {
						while (MessageSocket.this.in.available() > 0) {
							if (expecting < 0 && MessageSocket.this.in.available() >= 4) {
								expecting = MessageSocket.this.in.read();
								inbound = new byte[expecting];
							}
							
							if (expecting > 0 && MessageSocket.this.in.available() > 0) {
								int read = MessageSocket.this.in.read(inbound, offset, expecting);
								offset += read;
								expecting -= read;
							}
							
							if (expecting == 0) {
								String str = new String(inbound, "UTF-8");

								MessageSocket.this.delegate.onReceivedNetworkPacket(str, MessageSocket.this);
								
								inbound = null;
								offset = -1;
								expecting = -1;
							}
						}
						
						Thread.sleep(10);
					}
				} catch (Exception e) {
					MessageSocket.this.onInThreadError();
				}
			}
			
		});
		
		this.inThread.start();
	}
	
	private void onInThreadError() {
		this.onThreadError();
	}
	
	private void startOutThread() {
		this.outThread = new Thread(new Runnable() {

			public void run() {
				try {
					MessageSocket.this.out = MessageSocket.this.connection.getOutputStream();
					while (MessageSocket.this.connected) {
						Thread.sleep(10);
					}
				} catch (Exception e) {
					MessageSocket.this.onOutThreadError();
				}
			}
			
		});
		
		this.outThread.start();
	}
	
	private void onOutThreadError() {
		this.onThreadError();
	}

	private void onThreadError() {
		this.shutdownSocket();
		this.close();
	}

	private void shutdownSocket() {
		this.connected = false;
		
		try { this.in.close(); this.in = null; } catch (Exception e) {}
		try { this.out.close(); this.out = null; } catch (Exception e) {}
		try { this.connection.close(); this.connection = null; } catch (Exception e) {}
	}

	public void close() {
		if (this.connected) {
			this.shutdownSocket();
		}
		
		this.delegate.onConnectionTermination(this);
	}
	
    private byte[] compress(byte[] uncompressed) {
    	Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
    	deflater.setInput(uncompressed);
    	
    	byte[] compressed = new byte[deflater.getTotalOut()];
    	deflater.deflate(compressed);

    	return compressed;
    }
	
	public void sendNetworkJSON(HashMap<String, Object> packet) {
		try {
			JSONObject json = JSONHelper.fromMap(packet);
			String jsonString = json.toString();
			
			byte[] bytes = jsonString.getBytes("UTF-8");
			int byteLength = bytes.length;
			
			this.out.write(byteLength);
			this.out.write(bytes, 0, byteLength); 
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
}
