package com.madibasoft.dreamtools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.madibasoft.dreamtools.widgets.GraphView;
import com.webhiker.enigma2.api.Enigma2API;
import com.webhiker.enigma2.api.ServiceObject;
import com.webhiker.enigma2.api.Signal;

public class SatfinderActivity extends Activity implements OnSharedPreferenceChangeListener {
	private static boolean connected = false;
	private static final int MAX_HISTORY = 50;
	private List<Signal> data = new ArrayList<Signal>();

	String[] verlabels = new String[] { "100", "50", "0" };

	private AsyncTask<SatfinderActivity, Signal, String> task;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_satfinder);
		setTitle(getString(R.string.satfinder));
		// set up initial data sets
		for (int i = 0; i < MAX_HISTORY;i++) {
			try {
				data.add(new Signal());
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		try {
			drawGraphs(new Signal());
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		// register interest in the settings changes
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);
		
		// if we are the result of a screen rotation, re-connect
		if (connected) {
			start(findViewById(R.id.start));
		}
	}

	private void drawGraphs(Signal signal) {
		boolean graphType = SettingsActivity.getPreference(this, SettingsActivity.STYLE_PREF, GraphView.BAR);
		boolean drawTextOverlay = SettingsActivity.getBooleanPreference(this, SettingsActivity.TEXTOVERLAY_PREF, true);
		// update the acg graph
		data.add(signal);
		data.remove(0);
		float[] valuesAGC = new float[data.size()];// { 2.0f,1.5f, 2.5f, 1.0f , 3.0f };
		float[] valuesSNR = new float[data.size()];// { 2.0f,1.5f, 2.5f, 1.0f , 3.0f };
		String[] horlabels = new String[data.size()];//{ "today", "tomorrow", "next week", "next month" };
		int i = 0;
		for (Signal s : data) {
			valuesAGC[i] = (float)s.getACGPercentage();
			valuesSNR[i] = (float)s.getSNRPercentage();
			horlabels[i]="";
			i++;
		}
		GraphView graphView;
		graphView = ((GraphView)findViewById(R.id.graphAGC));
		graphView.setData(valuesAGC, "AGC "+signal.getACG(),horlabels, verlabels, graphType, drawTextOverlay);
		graphView.invalidate();

		graphView = ((GraphView)findViewById(R.id.graphSNR));
		graphView.setData(valuesSNR, "SNR "+signal.getSNR(),horlabels, verlabels, graphType, drawTextOverlay);
		graphView.invalidate();				
	}

	public void start(View view) {
		task = new AsyncTask<SatfinderActivity, Signal, String>() {

			@Override
			protected void onProgressUpdate(Signal... signals) {
				Log.i("dreamtools", "refreshing data");
				Signal signal = signals[0];

				((TextView)findViewById(R.id.textBER)).setText(signal.getBER());
				((TextView)findViewById(R.id.textSNRDB)).setText(signal.getSNRDB());	

				drawGraphs(signal);

			}

			@Override
			protected void onPreExecute() {
				findViewById(R.id.start).setEnabled(false);
				findViewById(R.id.stop).setEnabled(true);		
			}

			@Override
			protected void onPostExecute(String result) {
				findViewById(R.id.start).setEnabled(true);
				findViewById(R.id.stop).setEnabled(false);
			}

			@Override
			protected String doInBackground(SatfinderActivity... params) {
				//				String hostname = SettingsActivity.getPreference(params[0], SettingsActivity.HOSTNAME_PREF, "");
				//				String port = SettingsActivity.getPreference(params[0], SettingsActivity.PORT_PREF, "80");
				//				String username = SettingsActivity.getPreference(params[0], SettingsActivity.USERNAME_PREF, "root");
				//				String password = SettingsActivity.getPreference(params[0], SettingsActivity.PASSWORD_PREF, "dreambox");

				Enigma2API api = DreamToolsActivity.getAPI(params[0]); //new Enigma2API(hostname, Integer.parseInt(port), username, password);
				while(!isCancelled()) {
					try {
						Signal signal = api.getSignal();
						publishProgress(signal);
						long delay = SettingsActivity.getPreference(params[0], SettingsActivity.DELAY_PREF, 250);
						Thread.sleep(delay);
					} 
					catch (Throwable e) {
						e.printStackTrace();
					}
				}
				return "";
			}
		};

		task.execute(this);
		connected = true;
	}

	public void stop(View view) {
		Log.i("dreamtools", "stop");
		findViewById(R.id.start).setEnabled(true);
		findViewById(R.id.stop).setEnabled(false);
		task.cancel(false);
		connected = false;
	}

	public void zap(View view) {
		final View bodyView=getLayoutInflater().inflate(R.layout.alert_zap,null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.zap));
		builder.setIcon(R.drawable.ic_launcher_monitor);
		builder.setView(bodyView);

		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		final AlertDialog alert = builder.create();		

		View.OnClickListener l = new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				RadioButton rb = (RadioButton) v;
				alert.dismiss();
				try {
					switch (rb.getId()) {
					case R.id.zap1 :zapTo(sp.getString(SettingsActivity.ZAP1_PREF, ""));break;
					case R.id.zap2 :zapTo(sp.getString(SettingsActivity.ZAP2_PREF, ""));break;
					case R.id.zap3 :zapTo(sp.getString(SettingsActivity.ZAP3_PREF, ""));break;
					case R.id.zap4 :zapTo(sp.getString(SettingsActivity.ZAP4_PREF, ""));break;
					}
					Toast.makeText(SatfinderActivity.this, rb.getText(), Toast.LENGTH_SHORT).show();
				}
				catch (Throwable t) {
					Toast.makeText(SatfinderActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();					
				}
			}

			private void zapTo(String json) throws IOException {
				if (json.trim().length()==0) {
					Toast.makeText(SatfinderActivity.this, getString(R.string.zapEmpty), Toast.LENGTH_LONG).show();
				}
				else {
					Enigma2API api = DreamToolsActivity.getAPI(SatfinderActivity.this);
					try {
						api.zapTo(new ServiceObject(new JSONObject(json)));
					} 
					catch (JSONException e) {
						e.printStackTrace();
						Utils.infoDialog(SatfinderActivity.this, "Error", e.getMessage(), false);
					}
				}
			}
		};
		final RadioButton zap1 = (RadioButton) bodyView.findViewById(R.id.zap1);
		zap1.setText(getServiceName(sp.getString(SettingsActivity.ZAP1_PREF, ""),getString(R.string.notset)));
		zap1.setOnClickListener(l);
		final RadioButton zap2 = (RadioButton) bodyView.findViewById(R.id.zap2);
		zap2.setText(getServiceName(sp.getString(SettingsActivity.ZAP2_PREF, ""),getString(R.string.notset)));
		zap2.setOnClickListener(l);
		final RadioButton zap3 = (RadioButton) bodyView.findViewById(R.id.zap3);
		zap3.setText(getServiceName(sp.getString(SettingsActivity.ZAP3_PREF, ""),getString(R.string.notset)));
		zap3.setOnClickListener(l);
		final RadioButton zap4 = (RadioButton) bodyView.findViewById(R.id.zap4);
		zap4.setText(getServiceName(sp.getString(SettingsActivity.ZAP4_PREF, ""),getString(R.string.notset)));
		zap4.setOnClickListener(l);

		alert.show();	
	}

	public static String getServiceName(String s, String defaultValue) {
		try {
			return new JSONObject(s).getString("e2servicename");
		} catch (JSONException e) {
			return defaultValue;
		}
	}
	//
	//public static String getServiceRef(String s, String defaultValue) {
	//	try {
	//		return new JSONObject(s).getString("e2servicereference");
	//	} catch (JSONException e) {
	//		return defaultValue;
	//	}
	//}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("dreamtools", "Settings menu option selected");
		Intent addActivity = new Intent(getBaseContext(),SettingsActivity.class);
		startActivityForResult(addActivity,R.id.menu_settings);
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		try {
			drawGraphs(new Signal());
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}		
	}

}