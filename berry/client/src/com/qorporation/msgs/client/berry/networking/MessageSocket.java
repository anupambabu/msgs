package com.qorporation.msgs.client.berry.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.compress.ZLibInputStream;
import net.rim.device.api.compress.ZLibOutputStream;
import net.rim.device.api.io.transport.TransportInfo;

import org.json.JSONObject;

import com.qorporation.msgs.client.berry.util.ErrorControl;
import com.qorporation.msgs.client.berry.util.JSONHelper;

public class MessageSocket {
	public static final int PORT = 7456;
	
	private String host = null;
	private int port = -1;
	private MessageSocketDelegate delegate = null;
	private boolean connected = false;
	
	private StreamConnection connection = null;

	private DataInputStream in = null;
	private Thread inThread = null;
	
	private DataOutputStream out = null;
	private Thread outThread = null;
	
	public MessageSocket(String host, int port, MessageSocketDelegate delegate) {
		this.host = host;
		this.port = port;
		this.delegate = delegate;
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	public String getURL() {
        return "socket://" + this.host + ":" + this.port + HttpRequest.DEVICE_SIDE_CONSTANT;
	}
	
	public boolean connect() {
		if (!TransportInfo.hasSufficientCoverage(TransportInfo.TRANSPORT_TCP_CELLULAR)
			&& !TransportInfo.hasSufficientCoverage(TransportInfo.TRANSPORT_TCP_WIFI)) {
			return false;
		}
		
        try {                                    
            this.connection = (StreamConnection) Connector.open(this.getURL(), Connector.READ_WRITE);
            this.startInThread();
            this.startOutThread();
            this.connected = true;
            
            return true;
        } catch (Exception e) {
            this.connected = false;
            try { this.connection.close(); } catch (Exception ex) {}
            
            this.delegate.onConnectionAttemptFailure(this, e);
        }
        
        return false;
	}
	    
	private byte[] decompress(byte[] compressed) {
		try {
		    InputStream inputStream = new ByteArrayInputStream(compressed);
		    ZLibInputStream zlibInputStream = new ZLibInputStream(inputStream, true);
		    
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		    while (true) {
		    	byte[] bytes = new byte[compressed.length];
		    	int len = zlibInputStream.read(bytes, 0, bytes.length);
		    	outputStream.write(bytes, 0, len);
		    	
		    	if (len < bytes.length) {
		    		break;
		    	}
		    }
		    
		    zlibInputStream.close();
			
			return outputStream.toByteArray();
		} catch (Exception e) {
			ErrorControl.logException(e);
			return new byte[0];
		}
	}
	
	private void startInThread() {
		this.inThread = new Thread(new Runnable() {

			public void run() {
				try {
					MessageSocket.this.in = MessageSocket.this.connection.openDataInputStream();
					
					byte[] inbound = null;
					int expecting = -1;
					int offset = -1;
					
					while (MessageSocket.this.connected) {
						if (expecting < 0) {
							offset = 0;
							expecting = MessageSocket.this.in.readInt();
							inbound = new byte[expecting];
						}
						
						if (expecting > 0) {
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
						
						Thread.sleep(10);
					}
				} catch (Exception e) {
					MessageSocket.this.onInThreadError(e);
				}
			}
			
		});
		
		this.inThread.start();
	}
	
	private void onInThreadError(Exception e) {
		this.onThreadError(e);
	}
	
	private void startOutThread() {
		this.outThread = new Thread(new Runnable() {

			public void run() {
				try {
					MessageSocket.this.out = MessageSocket.this.connection.openDataOutputStream();
					while (MessageSocket.this.connected) {
						MessageSocket.this.out.flush();
						Thread.sleep(10);
					}
				} catch (Exception e) {
					MessageSocket.this.onOutThreadError(e);
				}
			}
			
		});
		
		this.outThread.start();
	}
	
	private void onOutThreadError(Exception e) {
		this.onThreadError(e);
	}

	private void onThreadError(Exception e) {
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
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    ZLibOutputStream zlibOutputStream = new ZLibOutputStream(outputStream);
		    
		    zlibOutputStream.write(uncompressed);
		    zlibOutputStream.close();
		    
		    return outputStream.toByteArray();
		} catch (Exception e) {
			ErrorControl.logException(e);
			return new byte[0];
		}
    }
	
	public void sendNetworkJSON(Hashtable packet) {
		try {
			JSONObject json = JSONHelper.fromHashtable(packet);
			String jsonString = json.toString();
			
			byte[] bytes = jsonString.getBytes("UTF-8");
			int byteLength = bytes.length;
			
			this.out.writeInt(byteLength);
			this.out.write(bytes, 0, byteLength);
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}
	
}
