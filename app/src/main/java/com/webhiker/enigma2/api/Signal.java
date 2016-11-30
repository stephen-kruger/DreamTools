package com.webhiker.enigma2.api;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * 
 *{"e2frontendstatus": {
 *   "e2acg": {"text": "56 %"},
 *   "e2ber": {"text": "0"},
 *   "e2snr": {"text": "80 %"},
 *   "e2snrdb": {"text": "12.90 dB"}
 *}}
 *
 *SNR = Signal to Noise Ratio - which means signal quality
 *AGC = Automatic Gain Control - which means signal strength
 *BER = Bit Error Rate - which shows the error rate of the signal.
 */
public class Signal extends DTO {
	private static final String root = "e2frontendstatus";

	public Signal(JSONObject jo) throws JSONException {
		super(jo.getJSONObject(root));
	}

	public Signal() throws JSONException {
		this(new JSONObject("{\"e2frontendstatus\": {\"e2acg\": \"0 %\",\"e2ber\": \"0\",\"e2snr\": \"0 %\",\"e2snrdb\": \"0.0 dB\"}}"));
	}

	public String getBER() {
		return getString("e2ber");
	}

	public String getACG() {
		return getString("e2acg");
	}

	public int getACGPercentage() {
		try {
			return Integer.parseInt(getACG().replace('%', ' ').trim());
		} 
		catch (Exception e) {
			return 0;
		}
	}

	public String getSNR() {
		return getString("e2snr");
	}

	public int getSNRPercentage() {
		try {
			return Integer.parseInt(getSNR().replace('%', ' ').trim());
		} 
		catch (Exception e) {
			return 0;
		}
	}

	public String getSNRDB() {
		return getString("e2snrdb");
	}

}
