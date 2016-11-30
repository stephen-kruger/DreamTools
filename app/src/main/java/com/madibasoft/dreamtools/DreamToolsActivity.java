package com.madibasoft.dreamtools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.webhiker.enigma2.api.Enigma2API;

public class DreamToolsActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dreamtools);
	}

	public static Enigma2API getAPI(Activity view) {
		String hostname = SettingsActivity.getPreference(view, SettingsActivity.HOSTNAME_PREF, "");
		String port;
		try {
			port = SettingsActivity.getPreference(view, SettingsActivity.PORT_PREF, "80");
		}
		catch (Throwable t) {
			port = "80";
		}
		String username = SettingsActivity.getPreference(view, SettingsActivity.USERNAME_PREF, "root");
		String password = SettingsActivity.getPreference(view, SettingsActivity.PASSWORD_PREF, "dreambox");
		long cacheDelay = SettingsActivity.getPreference(view, SettingsActivity.CACHE_DELAY_PREF, 604800000L);
		return new Enigma2API(hostname, Integer.parseInt(port), username, password, view.getCacheDir(),cacheDelay);
	}

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
	
	public void aboutView(View view) {
		Intent addActivity = new Intent(getBaseContext(),AboutActivity.class);
		startActivity(addActivity);
	}
	
	public void satfinderView(View view) {
		Intent addActivity = new Intent(getBaseContext(),SatfinderActivity.class);
		startActivity(addActivity);
	}
	
	public void settingsView(View view) {
		Intent act = new Intent(getBaseContext(),SettingsActivity.class);
		startActivity(act);
	}
	
	public void moviesView(View view) {
//		Utils.infoDialog(this, getString(R.string.under_construction_title), getString(R.string.under_construction_description), false);
		Intent act = new Intent(getBaseContext(),MovieActivity.class);
		startActivity(act);
	}
	
//	public void zapView(View view) {
//		Intent act = new Intent(getBaseContext(),ZapActivity.class);
//		startActivity(act);
//	}
	
	public void monitorView(View view) {
		Intent act = new Intent(getBaseContext(),MonitorActivity.class);
		startActivity(act);
	}
	
	public void controlView(View view) {
		Intent act = new Intent(getBaseContext(),ControlActivity.class);
		startActivity(act);
	}
	
	public void close(View view) {
		finish();
	}

	public void streamView(View view) {
		Intent videoActivity = new Intent(getBaseContext(),VideoActivity.class);
		startActivity(videoActivity);
	}
	
	public void mediaView(View view) {
		Utils.infoDialog(this, getString(R.string.under_construction_title), getString(R.string.under_construction_description), false);
	}
}