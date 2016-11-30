package com.madibasoft.dreamtools;

import java.net.URLConnection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.webhiker.enigma2.api.Enigma2API;


public class MonitorActivity extends Activity {
	private static final long DELAY = 500;
	private Handler mHandler = new Handler();
	private boolean requesting = false;
	private boolean paused = false;
	private AsyncTask<Activity, Object, Drawable> task;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);

		// start thread to poll screen changes
		mHandler.postAtTime(mUpdateTimeTask, SystemClock.uptimeMillis());
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		// stop the epg update thread
		mHandler.removeCallbacks(mUpdateTimeTask);
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_monitor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent addActivity = new Intent(getBaseContext(),SettingsActivity.class);
		startActivityForResult(addActivity,R.id.menu_settings);
		return true;
	}

	private void updateCurrentEvent() {
		// prevent double calling
		mHandler.removeCallbacks(mUpdateTimeTask);
		final ImageView screenShot = (ImageView) findViewById(R.id.nowplayingImage);
		task = new AsyncTask<Activity, Object, Drawable>() {
			@Override
			protected void onPostExecute(Drawable screenshotImage) {
				requesting = false;
				if (screenshotImage!=null) {
					screenShot.setImageDrawable(screenshotImage);
					Log.e("monitoractivity","got screenshot :"+screenshotImage.getIntrinsicWidth());
				}
				else {
//					screenShot.setImageResource(R.drawable.ic_launcher_monitor);
					Log.e("monitoractivity","screenshot was null");
				}
				if ((!paused)&&(!MonitorActivity.this.isFinishing())) {
					mHandler.postAtTime(mUpdateTimeTask, SystemClock.uptimeMillis()+DELAY);
				}
				else {
					Log.i("monitoractivity","asked to stop");
				}
			}

			@Override
			protected Drawable doInBackground(Activity... params) {
				if (requesting) {
					Log.i("monitoractivity","Aborting overlapping request");
					return null;
				}
				else {
					requesting = true;
					Enigma2API api = DreamToolsActivity.getAPI(MonitorActivity.this);
					try {
						int imageSize = (int) SettingsActivity.getPreference(params[0], SettingsActivity.MONITOR_QUALITY_PREF, 360);
						Log.i(getClass().getName(),"Requesting screenshot quality "+imageSize);
						URLConnection conn = api.getScreenshot(imageSize);
						Drawable screenshotImage = Drawable.createFromStream(conn.getInputStream(), "screenshot");
						return screenshotImage;
					} 
					catch (Throwable t) {
						t.printStackTrace();
						return null;
					}
				}
			}
		};
		task.execute(MonitorActivity.this);
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			updateCurrentEvent();
		}
	};
	
	public void togglePause(View view) {
		paused = !paused;
		if (paused) {
			if (task!=null) {
				task.cancel(true);
			}
			Utils.toastImage(this, R.drawable.ic_menu_pause);
		}
		else {
			// restart the thread
			mHandler.postAtTime(mUpdateTimeTask, SystemClock.uptimeMillis()+DELAY);
			Utils.toastImage(this, R.drawable.ic_menu_play);
		}
	}

}
