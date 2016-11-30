package com.webhiker.enigma2.api;

import org.json.JSONException;
import org.json.JSONObject;

public class ServiceObject extends DTO {

	public ServiceObject(String s) throws JSONException {
		super(new JSONObject(s));
	}
	
	public ServiceObject(JSONObject jo) {
		super(jo);
	}

	public String getName() {
		return getName("");
	}

	public String getName(String defaultValue) {
		if (has("e2servicename")) {
			return getString("e2servicename",defaultValue);
		}
		else {
			return  getString("e2eventservicename",defaultValue);
		}
	}

	public String getReference() {
		if (has("e2servicereference")) {
			return getString("e2servicereference");
		} 
		else {
			return getString("e2eventservicereference","");
		}
	}

	@Override
	public String toString() {
		return getName();
	}
	
	

}
