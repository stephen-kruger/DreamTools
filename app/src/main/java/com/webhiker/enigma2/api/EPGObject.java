package com.webhiker.enigma2.api;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class EPGObject extends ServiceObject {

	/*
	 *    {
	 *      "e2eventcurrenttime": "1322332681",
	 *      "e2eventdescription": "Actionthriller",
	 *      "e2eventdescriptionextended": "CIA-Spezialagent Marcus (Wesley Snipes) muss in Detroit einen heißen Job erledigen. Er soll einen Waffenschmuggler mitsamt dessen Finanzier aus dem Weg räumen. Aber bei dem Einsatz machen sich seine Kollegen Zander (Gary Daniels) und Floria (Zoe Bell) selbstständig. Sie töten seinen Mentor, klauen das Geld und schieben ihm alles in die Schuhe. Marcus hat keine Wahl: Er muss gnadenlos Rache nehmen. - Mit coolen Martial-Arts-Fights kämpft sich Wesley Snipes durch die Agenten-Action.\nUSA 2009. 94 Min. Von Giorgio Serafini, mit Wesley Snipes, Zoe Bell, Gary Daniels.",
	 *      "e2eventduration": "6000",
	 *      "e2eventid": "14543",
	 *      "e2eventservicename": "Sky Cinema +24",
	 *      "e2eventservicereference": "1:0:1:2B:2:85:C00000:0:0:0:",
	 *      "e2eventstart": "1322605500",
	 *      "e2eventtitle": "Game of Death"
	 *   }
	 */
	public EPGObject(JSONObject jo) {
		super(jo);
	}

	public EPGObject(String s) throws JSONException {
		super(s);
	}

	public Date getCurrentTime() {
		return new Date(1000*getLong("e2eventcurrenttime"));
	}

	public Date getStart() {
		return new Date(1000*getLong("e2eventstart"));
	}
	
	public void setStart(Date start) {
		putLong("e2eventstart",start.getTime()/1000);
	}

	public Date getEnd() {
		return new Date((1000*getLong("e2eventstart"))+(1000*getLong("e2eventduration")));
	}

	public void setEnd(Date end) {
		long duration = end.getTime()-getStart().getTime();
		putLong("e2eventduration",duration/1000);
	}
	
	public String getDescription() {
		return getString("e2eventdescription");
	}

	public String getDescriptionExtended() {
		return getString("e2eventdescriptionextended");
	}

	public String getBestDescription() {
		String d1 = getDescription();
		String d2 = getDescriptionExtended();
		if (d1.length()>d2.length())
			return d1;
		else
			return d2;
	}

	public String getTitle() {
		return getTitle("");
	}

	public String getTitle(String defaultValue) {
		return getString("e2eventtitle",defaultValue);
	}

	/*
	 * Length of the event in ms
	 */
	public long getDuration() {
		return (1000*getLong("e2eventduration"));
	}

	public String getDurationMinutes() {
		try {
			long duration = getLong("e2eventduration") / 60;
			long hours = duration / 60;
			long minutes = duration % 60;
			return String.format("%d:%02d", (int)hours, (int)minutes);
		}
		catch (Throwable t) {
			return "00:00";
		}
	}

	public ServiceObject getServiceObject() {
		return new ServiceObject(getJSONObject());
	}



}
