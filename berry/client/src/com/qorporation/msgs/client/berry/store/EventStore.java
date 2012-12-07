package com.qorporation.msgs.client.berry.store;

import org.json.JSONObject;

import com.qorporation.msgs.client.berry.util.ErrorControl;

import net.rim.device.api.database.Statement;

public class EventStore extends Store {
	private static EventStore instance = null;
	public static synchronized EventStore getInstance() {
		if (instance == null) instance = new EventStore();
		return instance;
	}
	
	private EventStore() {
		super(new String[] {
				"CREATE TABLE IF NOT EXISTS event (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "time INTEGER, "
				+ "type VARCHAR(50), "
				+ "entity INTEGER, "
				+ "action VARCHAR(50))",

				"CREATE UNIQUE INDEX IF NOT EXISTS event_id ON event(id);"
		});
	}
	
	public boolean setEvent(int id, long time, String type, int entity, String action) {
		boolean ret = false;
		
		try {
            Statement s = this.db.createStatement("INSERT OR REPLACE INTO event(id, time, type, entity, action) VALUES(?, ?, ?, ?, ?)");
            s.prepare();
            s.bind(1, id);
            s.bind(2, time);
            s.bind(3, type);
            s.bind(4, entity);
            s.bind(5, action);
            s.execute();      
            s.close();
            
            ret = this.db.getNumberOfChanges() > 0;
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return ret;
	}
	
	public boolean setEvent(JSONObject val) throws Exception {
		return setEvent(val.getInt("event"), val.getLong("time"), val.getString("type"), val.getInt("entity"), val.getString("action"));
	}

}
