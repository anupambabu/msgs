package com.qorporation.msgs.client.berry.util;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONObject;

public class JSONHelper {
	
	public static JSONObject fromHashtable(Hashtable hashtable) {
		JSONObject json = new JSONObject();
		
		try {
			Enumeration keys = hashtable.keys();
			while (keys.hasMoreElements()) {
				Object key = keys.nextElement();
				Object value = hashtable.get(key);
	
				json.put(key.toString(), value);
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return json;
	}

}
