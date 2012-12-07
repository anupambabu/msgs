package com.qorporation.msgs.client.android.store;

import java.util.Arrays;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.qorporation.msgs.client.android.store.delegates.AccountStoreDelegate;

public class AccountStore extends Store {
	private static AccountStore instance = null;
	public static synchronized AccountStore getInstance() {
		if (instance == null) instance = new AccountStore(Store.DEFAULT_CONTEXT);
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
	private static String AUTHTOKEN_FORMAT = "dt=%s&di=%s&dv=%s&at=%s";
	
	private AccountStoreDelegate delegate = null;
	public void setDelegate(AccountStoreDelegate delegate) { this.delegate = delegate; }
	
	private AccountStore(Context context) {
		super(context, "account.db", Arrays.asList(
				"CREATE TABLE IF NOT EXISTS account (pk INTEGER PRIMARY KEY, "
				+ "ident VARCHAR(50), "
				+ "key VARCHAR(50), "
				+ "value VARCHAR(50))",
				
				"CREATE UNIQUE INDEX IF NOT EXISTS account_key_ident ON account(key, ident);"
		));
	}
	
	public String getDeviceIdent() {
		return "";
	}
	
	public String getDeviceVerifier() {
		return this.getDeviceIdent();
	}

	public String getKey(String key) { return this.getKey(key, ""); }
	public String getKey(String key, String defValue) {
		String value = defValue;
		
		try {
	        Cursor c = this.db.query("account", new String[] { "value" }, "key = ? AND ident = ?", new String[] { key, this.getDeviceIdent() }, null, null, null);
	
	        if (c.moveToNext()) {
	        	value = c.getString(1);
	        }

	        c.close();		
		} catch (Exception e) {
		}
        
        return value;
	}
	
	public boolean setKey(String key, String value) {
		boolean ret = false;
		
		try {
			SQLiteStatement s = this.db.compileStatement("INSERT OR REPLACE INTO account(key, value, ident) VALUES(?, ?, ?)");
            s.bindString(1, key);
            s.bindString(2, value);
            s.bindString(3, this.getDeviceIdent());
            
            ret = s.executeInsert() > 0;
            s.close();
            
            if (this.delegate != null) {
            	this.delegate.onAccountEvent();
            }
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	public String getUserID() { return this.getKey(USERID_KEY); }
	public boolean setUserID(String val) { return this.setKey(USERID_KEY, val); }
	
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
		if (lastSyncString == null) {
			return new Long(0);
		} else {
			return new Long(Long.parseLong(lastSyncString));
		}
	}
	
	public boolean setLastSync(Long val) {
		return this.setKey(LASTSYNC_KEY, val.toString());
	}
	
	public String getRequestAuthentication() {
		return String.format(AUTHTOKEN_FORMAT,
		"android",
		this.getDeviceIdent(),
		this.getDeviceVerifier(),
		this.getAuthToken());
	}
	
}
