package com.qorporation.msgs.client.android.store;

import java.util.Arrays;

import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

public class EventStore extends Store {
	private static EventStore instance = null;
	public static synchronized EventStore getInstance() {
		if (instance == null) instance = new EventStore(Store.DEFAULT_CONTEXT);
		return instance;
	}
	
	private EventStore(Context context) {
		super(context, "event.db", Arrays.asList(
				"CREATE TABLE IF NOT EXISTS event (pk INTEGER PRIMARY KEY, "
				+ "id INTEGER, "
				+ "time INTEGER, "
				+ "type VARCHAR(50), "
				+ "entity INTEGER, "
				+ "action VARCHAR(50))",

				"CREATE UNIQUE INDEX IF NOT EXISTS event_id ON event(id);"
		));
	}
	
	public boolean setEvent(int id, long time, String type, int entity, String action) {
		boolean ret = false;
		
		try {
			SQLiteStatement s = this.db.compileStatement("INSERT OR REPLACE INTO event(id, time, type, entity, action) VALUES(?, ?, ?, ?, ?)");
            s.bindLong(1, id);
            s.bindLong(2, time);
            s.bindString(3, type);
            s.bindLong(4, entity);
            s.bindString(5, action);
            
            ret = s.executeInsert() > 0;
            s.close();
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	public boolean setEvent(JSONObject val) throws Exception {
		return setEvent(val.getInt("event"), val.getLong("time"), val.getString("type"), val.getInt("entity"), val.getString("action"));
	}

}
