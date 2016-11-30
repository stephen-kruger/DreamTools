package com.webhiker.enigma2.api;

import org.json.JSONException;
import org.json.JSONObject;

public class DTO {

	private JSONObject jo;

	public DTO(JSONObject jo) {
		this.jo = jo;
	}

	public boolean has(String key) {
		return jo.has(key);
	}

	public String getString(String key) {
		return getString(key,"");
	}

	public long getLong(String key) {
		try {
			return jo.getLong(key);
		} 
		catch (JSONException e) {
			return 0;
		}
	}

	public void putLong(String key, long val) {
		try {
			jo.put(key, val);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public JSONObject getJSONObject(String key) throws JSONException {
		return jo.getJSONObject(key);
	}

	public String getString(String key, String defaultValue) {
		try {
			if (jo.has(key)) {
				if (jo.getString(key).length()>0)
					return jo.getString(key);
				else
					return defaultValue;
			}
		} 
		catch (JSONException e) {
			return defaultValue;
		}
		return defaultValue;
	}

	public String toJSONString() {
		return jo.toString();
	}

	public String toJSONString(int indent) throws JSONException {
		return jo.toString(indent);
	}

	public JSONObject getJSONObject() {
		return jo;
	}


}
