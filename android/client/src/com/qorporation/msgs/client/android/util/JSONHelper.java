package com.qorporation.msgs.client.android.util;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONObject;

public class JSONHelper {
	
	public static JSONObject fromMap(HashMap<String, Object> message) {
		JSONObject json = new JSONObject();
		
		try {
			for (Entry<String, Object> e: message.entrySet()) {
				json.put(e.getKey(), e.getValue());
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		
		return json;
	}

}
