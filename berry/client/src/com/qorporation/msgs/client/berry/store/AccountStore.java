package com.qorporation.msgs.client.berry.store;

import javax.microedition.global.Formatter;

import com.qorporation.msgs.client.berry.store.delegates.AccountStoreDelegate;
import com.qorporation.msgs.client.berry.util.ErrorControl;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.Row;
import net.rim.device.api.database.Statement;
import net.rim.device.api.system.DeviceInfo;

public class AccountStore extends Store {
	private static AccountStore instance = null;
	public static synchronized AccountStore getInstance() {
		if (instance == null) instance = new AccountStore();
		return instance;
	}
	
	private static String USERID_KEY = "userID";
	private static String USERNAME_KEY = "userName";
	private static String USEREMAIL_KEY = "userEmail";
	private static String USEREMAILPENDING_KEY = "userEmailPending";
	private static String USERPHONE_KEY = "userPhone";
	private static String USERPHONEPENDING_KEY = "userPhonePending";
	private static String AUTHTOKEN_KEY = "authToken";
	private static String AUTHSECRET_KEY = "authSecret";
	private static String LASTSYNC_KEY = "lastSync";
	private static String AUTHTOKEN_FORMAT = "dt={0}&di={1}&dv={2}&at={3}";
	
	private AccountStoreDelegate delegate = null;
	public void setDelegate(AccountStoreDelegate delegate) { this.delegate = delegate; }
	
	private AccountStore() {
		super(new String[] {
				"CREATE TABLE IF NOT EXISTS account (pk INTEGER PRIMARY KEY, "
				+ "ident VARCHAR(50), "
				+ "key VARCHAR(50), "
				+ "value VARCHAR(50))",
				
				"CREATE UNIQUE INDEX IF NOT EXISTS account_key_ident ON account(key, ident);"
		});
	}
	
	public String getDeviceIdent() {
		return Integer.toHexString(DeviceInfo.getDeviceId());
	}
	
	public String getDeviceVerifier() {
		return this.getDeviceIdent();
	}

	public String getKey(String key) { return this.getKey(key, ""); }
	public String getKey(String key, String defValue) {
		String value = defValue;
		
		try {
	        Statement s = this.db.createStatement("SELECT value FROM account WHERE key = ? AND ident = ?");
	        s.prepare();
	        s.bind(1, key);
	        s.bind(2, this.getDeviceIdent());
	        Cursor c = s.getCursor();
	
	        if (c.next()) {
	        	Row row = c.getRow();
	        	value = row.getString(0);
	        }
	        
	        s.close();
	        c.close();		
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
        
        return value;
	}
	
	public boolean setKey(String key, String value) {
		boolean ret = false;
		
		try {
            Statement s = this.db.createStatement("INSERT OR REPLACE INTO account(key, value, ident) VALUES(?, ?, ?)");
            s.prepare();
            s.bind(1, key);
            s.bind(2, value);
            s.bind(3, this.getDeviceIdent());
            s.execute();      
            s.close();
            
            ret = this.db.getNumberOfChanges() > 0;
            
            if (this.delegate != null) {
            	this.delegate.onAccountEvent();
            }
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return ret;
	}
	
	private int userID = -1;
	public int getUserID() { return this.userID; }
	public boolean setUserID(String val) { if (this.setKey(USERID_KEY, val)) { this.userID = Integer.parseInt(val); return true; } return false; }
	
	public String getUserName() { return this.getKey(USERNAME_KEY); }
	public boolean setUserName(String val) { return this.setKey(USERNAME_KEY, val); }
	
	public String getUserEmail() { return this.getKey(USEREMAIL_KEY); }
	public boolean setUserEmail(String val) { return this.setKey(USEREMAIL_KEY, val); }
	
	public String getUserEmailPending() { return this.getKey(USEREMAILPENDING_KEY); }
	public boolean setUserEmailPending(String val) { return this.setKey(USEREMAILPENDING_KEY, val); }
	
	public String getUserPhone() { return this.getKey(USERPHONE_KEY); }
	public boolean setUserPhone(String val) { return this.setKey(USERPHONE_KEY, val); }
	
	public String getUserPhonePending() { return this.getKey(USERPHONEPENDING_KEY); }
	public boolean setUserPhonePending(String val) { return this.setKey(USERPHONEPENDING_KEY, val); }

	public String getAuthToken() { return this.getKey(AUTHTOKEN_KEY); }
	public boolean setAuthToken(String val) { return this.setKey(AUTHTOKEN_KEY, val); }
	public boolean hasAuthToken() { return this.getAuthToken().length() > 0; }
	
	public String getAuthSecret() { return this.getKey(AUTHSECRET_KEY); }
	public boolean setAuthSecret(String val) { return this.setKey(AUTHSECRET_KEY, val); }
	public boolean hasAuthSecret() { return this.getAuthSecret().length() > 0; }
	
	public Long getLastSync() {
		String lastSyncString = this.getKey(LASTSYNC_KEY);
		if (lastSyncString == null || lastSyncString.length() == 0) {
			return new Long(0);
		} else {
			return new Long(Long.parseLong(lastSyncString));
		}
	}
	
	public boolean setLastSync(Long val) {
		return this.setKey(LASTSYNC_KEY, val.toString());
	}
	
	public String getRequestAuthentication() {
		return Formatter.formatMessage(AUTHTOKEN_FORMAT, new String[] {
			"blackberry",
			this.getDeviceIdent(),
			this.getDeviceVerifier(),
			this.getAuthToken()
		});
	}
	
}
