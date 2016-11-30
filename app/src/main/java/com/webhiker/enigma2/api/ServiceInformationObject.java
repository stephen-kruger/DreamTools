package com.webhiker.enigma2.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceInformationObject extends DTO {
	private static String root = "e2currentserviceinformation";

	public ServiceInformationObject(JSONObject jo) throws JSONException {
		super(jo.getJSONObject(root));
	}

	public ServiceObject getService() throws JSONException {
		return new ServiceObject(getJSONObject("e2service"));
	}

	public List<EPGObject> getEPG() throws JSONException {
		List<EPGObject> epg = new ArrayList<EPGObject>();
		JSONArray ja = getJSONObject("e2eventlist").getJSONArray("e2event");
		for (int i = 0; i < ja.length();i++) {
			epg.add(new EPGObject(ja.getJSONObject(i)));
		}
		return epg;

	}

}
