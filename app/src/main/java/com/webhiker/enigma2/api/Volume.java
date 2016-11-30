package com.webhiker.enigma2.api;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * {
 * 		"e2volume": {
 *  		"e2current": "100",
 *  		"e2ismuted": "False",
 *  		"e2result": "True",
 *  		"e2resulttext": "State"
 * 			}
 * }
 */
public class Volume extends DTO {

	public Volume(JSONObject jo) {
		super(jo);
	}

	public Volume() throws JSONException {
		this(new JSONObject("{\"e2volume\": {\"e2current\": \"100\",\"e2ismuted\": \"False\",\"e2result\": \"True\",\"e2resulttext\": \"State\"}}"));
	}

	public int getCurrent() {
		try {
			return getJSONObject("e2volume").getInt("e2current");
		} 
		catch (JSONException e) {
			return 50;
		}
	}
	
	public void setCurrent(int value) {
		try {
			getJSONObject("e2volume").put("e2current",value);
		} 
		catch (JSONException e) {
		}
	}
	
	public boolean isMuted() {
		try {
			return getJSONObject("e2volume").getBoolean("e2ismuted");
		} 
		catch (JSONException e) {
			return false;
		}
	}
	
	public void setMuted(boolean muted) {
		try {
			getJSONObject("e2volume").put("e2ismuted",muted);
		} 
		catch (JSONException e) {
		}
	}

}
