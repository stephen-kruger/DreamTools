package com.webhiker.enigma2.api;

import org.json.JSONObject;

public class MovieObject extends EPGObject {

	/*
     * "e2description": "Harry Potter's Jason Issacs plays Hook in this captivating adaptation of the classic tale. Rachel Hurd-Wood plays Wendy, who is whisked away to Neverland by the Lost Boys. (2003)(113 mins)",
     * "e2descriptionextended": "",
     * "e2filename": "/hdd/movie/20110430 1608 - Sky Family HD - Peter Pan.ts",
     * "e2filesize": "",
     * "e2length": "92:42",
     * "e2servicename": "Sky Family HD",
     * "e2servicereference": "1:0:0:0:0:0:0:0:0:0:/hdd/movie/20110430 1608 - Sky Family HD - Peter Pan.ts",
     * "e2tags": "",
     * "e2time": "1304176097",
     * "e2title": "Peter Pan"
	 */
	public MovieObject(JSONObject jo) {
		super(jo);
	}

	public String getTitle() {
		return getString("e2title");
	}
	
	public String getLength() {
		return getString("e2length");
	}
	
	public long getTime() {
		return getLong("e2time");
	}
	
	public long getFileSize() {
		return getLong("e2filesize");
	}
	
	@Override
	public String getDescription() {
		return getString("e2description");
	}
	
	@Override
	public String getDescriptionExtended() {
		return getString("e2descriptionextended");
	}

}
