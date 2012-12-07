package com.qorporation.msgs.client.berry.store;

import java.util.Enumeration;

import javax.microedition.io.file.FileSystemRegistry;

import com.qorporation.msgs.client.berry.util.ErrorControl;

import net.rim.device.api.database.Database;
import net.rim.device.api.database.DatabaseFactory;
import net.rim.device.api.database.DatabaseSecurityOptions;
import net.rim.device.api.database.Statement;
import net.rim.device.api.io.URI;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

public class Store {
	protected static final String DB_LOCATION = "/SDCard/databases/msgs/";
	protected static Database SHARED_DATABASE = null;
	
	protected Database db;
	
	private String[] initStatements;
	private boolean isInitialized;
	
	protected boolean isStillProcessingEvents;
	
	public Store(String[] initStatements) {
		this.db = null;
		this.initStatements = initStatements;
		this.isInitialized = false;
		this.isStillProcessingEvents = false;
	}
	
	public void init() throws Exception {
		this.db = this.initDB();
		
		if (this.db != null) {
			this.runInitStatements();
			this.isInitialized = true;
		}		
	}
	
	private void runInitStatements() {
		String[] defaultInitStatements = {
				// "PRAGMA CACHE_SIZE=1000",
				// "PRAGMA encoding = \"UTF-8\"",
				// "PRAGMA auto_vacuum=1",
				// "PRAGMA synchronous=NORMAL",
			};
		
		String[] statements = new String[defaultInitStatements.length + this.initStatements.length];
		
		System.arraycopy(defaultInitStatements, 0, statements, 0, defaultInitStatements.length);
		System.arraycopy(this.initStatements, 0, statements, defaultInitStatements.length, this.initStatements.length);
		
		for (int i = 0; i < statements.length; i++) {
	        try {
	        	String statement = statements[i];
	            Statement s = this.db.createStatement(statement); 
	            s.prepare();                  
	            s.execute();                                           
	            s.close();
	        } catch(Exception e) {
	        	ErrorControl.logException(e);
	        }  
		}
	}

	private synchronized Database initDB() throws Exception {
        boolean foundSD = false;
        Enumeration roots = FileSystemRegistry.listRoots();
        while (roots.hasMoreElements()) {
            if (((String) roots.nextElement()).equalsIgnoreCase("sdcard/")) {
            	foundSD = true;
            }     
        }
        
        if (!foundSD) {
        	
            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    Dialog.alert("This application requires an SD card to be present."); 
                } 
            });
            
            return null;
            
        } else {
        	
        	if (Store.SHARED_DATABASE == null) {
        		URI uri = URI.create(DB_LOCATION + "msgs.db");
        		Store.SHARED_DATABASE = DatabaseFactory.openOrCreate(uri, new DatabaseSecurityOptions(false));  
        	}
        	
            return Store.SHARED_DATABASE;
            
        }
	}
	
	public boolean isInitialized() {
		return this.isInitialized;
	}
	
	public void onStartProcessingEvents() { this.isStillProcessingEvents = true; }
	public void onStopProcessingEvents(long firstEventTime) { this.isStillProcessingEvents = true; this.onStopProcessingEventsInternal(firstEventTime); }
	protected void onStopProcessingEventsInternal(long firstEventTime) {}
	
	public static void onGlobalStartProcessingEvents() {
		AccountStore.getInstance().onStartProcessingEvents();
		ContactStore.getInstance().onStartProcessingEvents();
		ConversationStore.getInstance().onStartProcessingEvents();
		EventStore.getInstance().onStartProcessingEvents();
		MessageStore.getInstance().onStartProcessingEvents();
		UserStore.getInstance().onStartProcessingEvents();
	}
	
	public static void onGlobalStopProcessingEvents(long firstEventTime) {
		AccountStore.getInstance().onStopProcessingEvents(firstEventTime);
		ContactStore.getInstance().onStopProcessingEvents(firstEventTime);
		ConversationStore.getInstance().onStopProcessingEvents(firstEventTime);
		EventStore.getInstance().onStopProcessingEvents(firstEventTime);
		MessageStore.getInstance().onStopProcessingEvents(firstEventTime);
		UserStore.getInstance().onStopProcessingEvents(firstEventTime);		
	}
	
}
