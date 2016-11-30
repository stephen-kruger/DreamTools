package com.madibasoft.dreamtools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String USERNAME_PREF = "usernamePref";
	public static final String PASSWORD_PREF = "passwordPref";
	public static final String HOSTNAME_PREF = "hostnamePref";
	public static final String PORT_PREF     = "portPref";
	public static final String STYLE_PREF     = "stylePref";
	public static final String TEXTOVERLAY_PREF = "drawTextPref";
	public static final String DELAY_PREF     = "delayPref";
	public static final String ZAP1_PREF     = "zapChannel1Pref";
	public static final String ZAP2_PREF     = "zapChannel2Pref";
	public static final String ZAP3_PREF     = "zapChannel3Pref";
	public static final String ZAP4_PREF     = "zapChannel4Pref";
	public static final String CACHE_DELAY_PREF = "syncDelay";
	public static final int ZAP1_RESULT = 1;
	public static final int ZAP2_RESULT = 2;
	public static final int ZAP3_RESULT = 3;
	public static final int ZAP4_RESULT = 4;
	public static final String MONITOR_QUALITY_PREF = "monitor_size_Pref";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.settings));
		
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		onSharedPreferenceChanged(sp,HOSTNAME_PREF);
		onSharedPreferenceChanged(sp,PORT_PREF);
		onSharedPreferenceChanged(sp,USERNAME_PREF);
		onSharedPreferenceChanged(sp,ZAP1_PREF);
		onSharedPreferenceChanged(sp,ZAP2_PREF);
		onSharedPreferenceChanged(sp,ZAP3_PREF);
		onSharedPreferenceChanged(sp,ZAP4_PREF);
		sp.registerOnSharedPreferenceChangeListener(this);

		// set up the zap settings intents
		Preference preference;
		preference=findPreference(ZAP1_PREF);			
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref) {
				Intent serviceActivity = new Intent(getBaseContext(),ServiceSelectorActivity.class);
				startActivityForResult(serviceActivity, ZAP1_RESULT);
				return true;
			}

		});
		preference=findPreference(ZAP2_PREF);			
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref) {
				Intent serviceActivity = new Intent(getBaseContext(),ServiceSelectorActivity.class);
				startActivityForResult(serviceActivity, ZAP2_RESULT);
				return true;
			}

		});
		preference=findPreference(ZAP3_PREF);			
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref) {
				Intent serviceActivity = new Intent(getBaseContext(),ServiceSelectorActivity.class);
				startActivityForResult(serviceActivity, ZAP3_RESULT);
				return true;
			}

		});
		preference=findPreference(ZAP4_PREF);			
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref) {
				Intent serviceActivity = new Intent(getBaseContext(),ServiceSelectorActivity.class);
				startActivityForResult(serviceActivity, ZAP4_RESULT);
				return true;
			}

		});		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(HOSTNAME_PREF)) {
			getPreferenceManager().findPreference(HOSTNAME_PREF).setSummary(getPreference(this,HOSTNAME_PREF,""));
		}
		if (key.equals(PORT_PREF)) {
			getPreferenceManager().findPreference(PORT_PREF).setSummary(getPreference(this,PORT_PREF,""));
		}
		if (key.equals(USERNAME_PREF)) {
			getPreferenceManager().findPreference(USERNAME_PREF).setSummary(getPreference(this,USERNAME_PREF,""));
		}
		if (key.equals(ZAP1_PREF)) {
			getPreferenceManager().findPreference(ZAP1_PREF).setSummary(SatfinderActivity.getServiceName(getPreference(this,ZAP1_PREF,""),"Not set"));
		}
		if (key.equals(ZAP2_PREF)) {
			getPreferenceManager().findPreference(ZAP2_PREF).setSummary(SatfinderActivity.getServiceName(getPreference(this,ZAP2_PREF,""),"Not set"));
		}
		if (key.equals(ZAP3_PREF)) {
			getPreferenceManager().findPreference(ZAP3_PREF).setSummary(SatfinderActivity.getServiceName(getPreference(this,ZAP3_PREF,""),"Not set"));
		}
		if (key.equals(ZAP4_PREF)) {
			getPreferenceManager().findPreference(ZAP4_PREF).setSummary(SatfinderActivity.getServiceName(getPreference(this,ZAP4_PREF,""),"Not set"));
		}
	}

	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		if (resultCode == Activity.RESULT_OK) { 				
			switch(requestCode) { 
			case (ZAP1_RESULT) : { 
				String channel = data.getStringExtra(ServiceSelectorActivity.SERVICE_RESULT);
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(ZAP1_PREF,channel);
				editor.commit();
				break; 
			} 
			case (ZAP2_RESULT) : { 
				String channel = data.getStringExtra(ServiceSelectorActivity.SERVICE_RESULT);
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(ZAP2_PREF,channel);
				editor.commit();
				break; 
			} 
			case (ZAP3_RESULT) : { 
				String channel = data.getStringExtra(ServiceSelectorActivity.SERVICE_RESULT);
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(ZAP3_PREF,channel);
				editor.commit();
				break; 
			} 
			case (ZAP4_RESULT) : { 
				String channel = data.getStringExtra(ServiceSelectorActivity.SERVICE_RESULT);
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(ZAP4_PREF,channel);
				editor.commit();
				break; 
			} 
			} 
		}
		else {
			// some connection error occurred
			String error = "Cancelled";
			if (data!=null) {
				if (data.hasExtra(ServiceSelectorActivity.SERVICE_RESULT)) {
					error = data.getStringExtra(ServiceSelectorActivity.SERVICE_RESULT);
				}			
			}
			Toast.makeText(this, error, Toast.LENGTH_LONG).show();
		}
	}

	public static long getPreference(Context a, String key, long defValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a);
		return Long.parseLong(sp.getString(key, Long.toString(defValue)));
	}

	public static boolean getPreference(Context a, String key, boolean defValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a);
		return Boolean.parseBoolean(sp.getString(key, Boolean.toString(defValue)));
	}

	public static boolean getBooleanPreference(Context a, String key, boolean defValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a);
		return sp.getBoolean(key, defValue);
	}

	public static String getPreference(Context a, String key, String defValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a);
		return sp.getString(key, defValue);
	}

}