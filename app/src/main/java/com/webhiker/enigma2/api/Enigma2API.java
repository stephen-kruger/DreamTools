package com.webhiker.enigma2.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.webhiker.dreambox.api.Utils;


public class Enigma2API {
	private Logger log = Logger.getAnonymousLogger();

	/** The password. */
	private String host, username, password;

	/** The port. */
	private int port;

	private File cacheDir;
	private long cacheDelay = 200000;
	public static final int TIMER_RECORD = 0;
	public static final int TIMER_SWITCH = 1;

	/**
	 * Instantiates a new dreambox api.
	 * 
	 * @param host the host
	 * @param port the port
	 * @param username the username
	 * @param password the password
	 */
	public Enigma2API(String host, int port, String username, String password, File cacheDir, long cacheDelay) {
		log.fine("Creating API object");
		setHost(host);
		setPort(port);
		setUsername(username);
		setPassword(password);
		this.cacheDir = cacheDir;
		this.cacheDelay = cacheDelay;
	}

	/**
	 * Instantiates a new dreambox api.
	 * 
	 * @param host the host
	 * @param port the port
	 * @param username the username
	 * @param password the password
	 */
	protected Enigma2API(String host, int port, String username, String password) {
		this(host, port, username, password, null,0);
	}

	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host.
	 * 
	 * @param host the new host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 * 
	 * @param username the new username
	 */
	public void setUsername(String username) {
		this.username = username;
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication (getUsername(), getPassword().toCharArray());
			}
		});
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication (getUsername(), getPassword().toCharArray());
			}
		});
	}

	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 * 
	 * @param port the new port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Gets the base.
	 * 
	 * @return the base
	 */
	private String getBase() {
		return "http://"+getHost()+":"+getPort();
	}

	/**
	 * Gets the connection.
	 * 
	 * @param location the location
	 * 
	 * @return the connection
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private URLConnection getConnection(String location, int timeout) throws IOException {
		// Need to work with URLConnection to set request property
		URL url = new URL(getBase()+location);
		URLConnection uc = url.openConnection();
		uc.setConnectTimeout(timeout);
		return uc;
	}

	private URLConnection getConnection(String location) throws IOException {
		return getConnection(location,3000);
	}

	private JSONObject getCachedStream(String location) throws IOException, JSONException {
		if (cacheDir==null) {
			// cache not enabled
			URLConnection urlConn = getConnection(location);
			Utils.validateResponse(urlConn);
			return Utils.convertXML(urlConn.getInputStream());
		}
		else {
			String key = location.replace('/', '_');
			File f = new File(cacheDir.getCanonicalPath()+File.separator+key);
			if (!f.exists()) {
				log.info("No cache for "+f.getName());
				URL url = new URL(getBase()+location);
				URLConnection uc = url.openConnection();
				uc.setConnectTimeout(3000);
				JSONObject jo = Utils.convertXML(uc.getInputStream());
				Utils.spoolJSONObject(jo,new FileOutputStream(f));
				return jo;
			}
			else {
				Date expireTime = new Date(f.lastModified()+cacheDelay);
				Date currentTime = new Date();
				if (currentTime.after(expireTime)) {
					log.info("Cache expired "+f.getName());
					f.delete();
					return getCachedStream(location);
				}
				else {
					log.info("Using cached response "+f.getName());
					return Utils.loadJSON(new FileInputStream(f));
				}
			}
		}
	}

	public JSONObject getAbout() throws IOException, JSONException {
		String location = "/web/about";
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		return Utils.convertXML(urlConn.getInputStream());
	}

	public Signal getSignal() throws IOException, SAXException, ParserConfigurationException, JSONException {
		String location = "/web/signal";
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		return new Signal(Utils.convertXML(urlConn.getInputStream()));
	}

	public List<MovieObject> getMovies() throws IOException, JSONException {
		List<MovieObject> result = new ArrayList<MovieObject>();
		String location = "/web/movielist";
		JSONObject jo = getCachedStream(location);
		JSONObject e2movielist = jo.getJSONObject("e2movielist");
		JSONArray ja = e2movielist.getJSONArray("e2movie");
		for (int i = 0; i < ja.length(); i++) {
			result.add(new MovieObject(ja.getJSONObject(i)));
		}
		return result;
	}

	public List<ServiceObject> getBouquets() throws IOException, JSONException {
		String location = "/web/getservices";
		JSONObject jo = getCachedStream(location);
		JSONArray ja = jo.getJSONObject("e2servicelist").getJSONArray("e2service");
		List<ServiceObject> result = new ArrayList<ServiceObject>();
		for (int i = 0; i < ja.length(); i++) {
			result.add(new ServiceObject(ja.getJSONObject(i)));
		}
		return result;
	}

	public  List<ServiceObject> getBouquetServices(ServiceObject bouquet) throws JSONException, IOException {
		String location = "/web/getservices?sRef="+URLEncoder.encode(bouquet.getReference(),"UTF-8");
		JSONObject jo = getCachedStream(location);
		List<ServiceObject> result = new ArrayList<ServiceObject>();
		if (jo.has("e2servicelist")) {
			if (jo.getJSONObject("e2servicelist").has("e2service")) {
				JSONArray ja = jo.getJSONObject("e2servicelist").getJSONArray("e2service");
				for (int i = 0; i < ja.length(); i++) {
					result.add(new ServiceObject(ja.getJSONObject(i)));
				}
			}
		}
		return result;
	}

	public JSONObject zapTo(ServiceObject service) throws IOException {
		String location = "/web/zap?sRef="+URLEncoder.encode(service.getReference(),"UTF-8");
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		return Utils.convertXML(urlConn.getInputStream());
	}

	public ServiceInformationObject getCurrent() throws IOException, JSONException {
		String location = "/web/getcurrent";
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		return new ServiceInformationObject(Utils.convertXML(urlConn.getInputStream()));
	}

	public ServiceObject getSubservices() throws IOException, JSONException {
		String location = "/web/subservices";
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		return new ServiceObject(Utils.convertXML(urlConn.getInputStream()).getJSONObject("e2servicelist").getJSONObject("e2service"));
	}

	public List<EPGObject> getServiceEPG(ServiceObject service) throws IOException, JSONException {
		String location = "/web/epgservice?sRef="+URLEncoder.encode(service.getReference(),"UTF-8");
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		JSONObject jo = Utils.convertXML(urlConn.getInputStream());
		List<EPGObject> result = new ArrayList<EPGObject>();
		if (jo.getJSONObject("e2eventlist").has("e2event")) {
			JSONArray ja = jo.getJSONObject("e2eventlist").getJSONArray("e2event");
			for (int i = 0; i < ja.length(); i++) {
				result.add(new EPGObject(ja.getJSONObject(i)));
			}
		}
		return result;
	}

	public JSONObject sendMessage(String message) throws IOException, JSONException {
		String location = "/web/message?text="+URLEncoder.encode(message)+"&type=2";
		log.info(location);
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		return Utils.convertXML(urlConn.getInputStream());
	}

	public Volume getVolume() throws IOException, JSONException {
		String location = "/web/vol";
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		return new Volume(Utils.convertXML(urlConn.getInputStream()));
	}

	public Volume setVolume(Volume v) throws IOException, JSONException {
		String location = "/web/vol?set=set"+v.getCurrent();
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		return new Volume(Utils.convertXML(urlConn.getInputStream()));
	}

	public Volume toggleMute() throws IOException {
		String location = "/web/vol?set=mute";
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		return new Volume(Utils.convertXML(urlConn.getInputStream()));		
	}

	public void addTimer(String name, String description, ServiceObject service, EPGObject epg, int type) throws IOException, JSONException {
		// web/timeradd?sRef=1%3A0%3A1%3A1910%3A7FF%3A2%3A11A0000%3A0%3A0%3A0%3A&eit=0&begin=1332686800&end=1332692200&name=Bobby&description=xxxxx
		// can't record in past
		if (new Date().after(epg.getStart())) {
			epg.setStart(new Date());
		}
		String location = "/web/timeradd?sRef="+URLEncoder.encode(service.getReference(),"UTF-8")+
				"&eit=0"+
				"&afterevent=0"+
				"&justplay="+type+
				"&begin="+epg.getStart().getTime()/1000+
				"&end="+epg.getEnd().getTime()/1000+
				"&name="+URLEncoder.encode(name,"UTF-8")+
				"&description="+URLEncoder.encode(description,"UTF-8");
		log.info(location);
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		JSONObject result = Utils.convertXML(urlConn.getInputStream());
		//{"e2simplexmlresult":{"e2statetext":"Timer added successfully!","e2state":"True"}}
		if (!result.getJSONObject("e2simplexmlresult").getBoolean("e2state")) {
			throw new IOException(result.getJSONObject("e2simplexmlresult").getString("e2statetext"));
		}
	}

	public void addZapTimer(String name, String description, ServiceObject service, EPGObject epg) throws IOException, JSONException {
		addTimer(name, description, service, epg, TIMER_SWITCH);
	}

	public void addRecordingTimer(String name, String description, ServiceObject service, EPGObject epg) throws IOException, JSONException {
		addTimer(name, description, service, epg, TIMER_RECORD);
	}

	public URLConnection getScreenshot(int width) throws IOException {
		if (width>720) {
			width = 720;
		}
		// /grab?format=jpg&r=720&&v=&filename=/tmp/1322852558124
		String location = "/grab?format=jpg&r="+width;
		URLConnection urlConn = getConnection(location,20000);
		Utils.validateResponse(urlConn);
		return urlConn;
	}

	public void toggleStandby() throws IOException {
		// /web/powerstate?newstate=0
		String location = "/web/powerstate?newstate=0";
		URLConnection urlConn = getConnection(location);
		Utils.validateResponse(urlConn);
		Utils.convertXML(urlConn.getInputStream());	
	}

	public String getCurrentStream(ServiceObject service) {
		//http://192.168.1.151:8001/1:0:1:232C:803:2:11A0000:0:0:0:
		String surl = getBase()+"/"+URLEncoder.encode(service.getReference());
		return surl;
	}
	
	public String getCurrentStream() {
		return getBase()+"/web/streamcurrent.m3u";
	}
}
