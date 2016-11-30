package com.madibasoft.dreamtools;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.madibasoft.dreamtools.widgets.HorizontalSlider;
import com.madibasoft.dreamtools.widgets.HorizontalSlider.OnProgressChangeListener;
import com.webhiker.enigma2.api.EPGObject;
import com.webhiker.enigma2.api.Enigma2API;
import com.webhiker.enigma2.api.ServiceInformationObject;
import com.webhiker.enigma2.api.ServiceObject;
import com.webhiker.enigma2.api.Volume;


public class ControlActivity extends Activity implements OnProgressChangeListener {

	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private Handler mHandler = new Handler();
	private ServiceObject currentSO;
	private List<EPGObject> currentEPG = new ArrayList<EPGObject>(0);
	private int epgIndex = 0;
	private Volume volume;

	public void onCreate(Bundle savedInstanceState) {
		try {
			volume = new Volume();
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		// start thread to poll epg changes
		mHandler.postAtTime(mUpdateTimeTask, SystemClock.uptimeMillis());
		// add listener to volume control
		HorizontalSlider volume = (HorizontalSlider)findViewById(R.id.volumeControl);
		volume.setOnProgressChangeListener(this);

		initWheels();

	}

	public void initWheels() {
		AsyncTask<Activity, Object, List<ServiceObject>> task = new AsyncTask<Activity, Object, List<ServiceObject>>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected void onPostExecute(List<ServiceObject> bouquets) {
				// set up rollers
				final WheelView bouquetRoller = (WheelView) findViewById(R.id.bouquetRoller);
				try {
					bouquetRoller.setViewAdapter(new ArrayWheelAdapter(ControlActivity.this, bouquets.toArray()));
					OnWheelScrollListener bouquetListener = new OnWheelScrollListener() {

						@Override
						public void onScrollingStarted(WheelView wheel) {

						}

						@Override
						public void onScrollingFinished(WheelView wheel) {
							try {						
								ArrayWheelAdapter awa = (ArrayWheelAdapter)wheel.getViewAdapter();
								ServiceObject selectedBouquet = (ServiceObject) awa.getItem(wheel.getCurrentItem());
								loadBouquet(selectedBouquet);
							} 
							catch (Throwable e) {
								e.printStackTrace();
							}	
						}

					};
					bouquetRoller.addScrollingListener(bouquetListener);
					if (bouquets.size()>0)
						loadBouquet(bouquets.get(0));
				}
				catch (Throwable t) {
					t.printStackTrace();
				}		
			}

			@Override
			protected List<ServiceObject> doInBackground(Activity... params) {
				List<ServiceObject> bouquets = new ArrayList<ServiceObject>();
				try {
					bouquets = DreamToolsActivity.getAPI(params[0]).getBouquets();
				}
				catch (Throwable t) {
					t.printStackTrace();
				}
				return bouquets;
			}
		};
		task.execute(ControlActivity.this);
	}

	public void loadBouquet(final ServiceObject bouquet) {
		final WheelView channelRoller = (WheelView) findViewById(R.id.channelRoller);
		AsyncTask<Activity, Object, List<ServiceObject>> task = new AsyncTask<Activity, Object, List<ServiceObject>>() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			protected void onPostExecute(List<ServiceObject> services) {
				Log.i("xxx","Received bouqet services");
				channelRoller.setViewAdapter(new ArrayWheelAdapter(ControlActivity.this, services.toArray()));
			}

			@Override
			protected List<ServiceObject> doInBackground(Activity... params) {
				Log.i("xxx","Requesting bouqet services");
				try {						
					List<ServiceObject> bouquets =  DreamToolsActivity.getAPI(ControlActivity.this).getBouquetServices(bouquet);
					return bouquets;
				} 
				catch (Throwable t) {
					t.printStackTrace();
					return new ArrayList<ServiceObject>();
				}	
			}
		};
		task.execute(ControlActivity.this);	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// stop the epg update thread
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	private void updateCurrentEvent() {
		AsyncTask<Activity, Object, ServiceInformationObject> task = new AsyncTask<Activity, Object, ServiceInformationObject>() {
			@Override
			protected void onPostExecute(ServiceInformationObject sio) {
				synchronized (currentEPG) {
					try {
						if (sio!=null) {
							ServiceObject so = sio.getService();
							if (so!=null) {
								currentSO = so;
								List<EPGObject> epg = sio.getEPG();
								currentEPG = epg;
								if (epgIndex>=currentEPG.size()) {
									epgIndex = 0;
								}
								updateCurrentDisplay();
							}
						}
					}
					catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}

			@Override
			protected ServiceInformationObject doInBackground(Activity... params) {
				Enigma2API api = DreamToolsActivity.getAPI(ControlActivity.this);
				try {
					ServiceInformationObject sio = api.getCurrent();
					volume = api.getVolume();
					return sio;
				} 
				catch (Throwable t) {
					Log.e(getClass().getName(), "Error updating current event :"+t.getMessage());
				}


				return null;
			}
		};
		task.execute(ControlActivity.this);
	}

	public void updateCurrentDisplay() {
		synchronized (currentEPG) {
			if ((currentSO!=null)&&(currentEPG!=null)) {
				((TextView)findViewById(R.id.currentService)).setText(currentSO.getName(ControlActivity.this.getString(R.string.no_title)));
				if (currentEPG.size()>0) {
					((TextView)findViewById(R.id.pageIndex)).setText((epgIndex+1)+"/"+currentEPG.size());
					((TextView)findViewById(R.id.currentTitle)).setText(currentEPG.get(epgIndex).getTitle(ControlActivity.this.getString(R.string.no_title)));
					((TextView)findViewById(R.id.currentStart)).setText(timeFormat.format(currentEPG.get(epgIndex).getStart()));
					((TextView)findViewById(R.id.currentEnd)).setText(timeFormat.format(currentEPG.get(epgIndex).getEnd()));
					((TextView)findViewById(R.id.currentDuration)).setText(currentEPG.get(epgIndex).getDurationMinutes());
					//					((TextView)findViewById(R.id.currentDescription)).setText(currentEPG.get(epgIndex).getBestDescription());

					// reflect the latest volume indicators
					HorizontalSlider volumeSlider = (HorizontalSlider)findViewById(R.id.volumeControl);
					volumeSlider.setProgress(volume.getCurrent());
					ImageView volumeMute = (ImageView)findViewById(R.id.muteControl);
					if (volume.isMuted()) {
						volumeMute.setImageResource(R.drawable.ic_menu_audio_mute);
					}
					else {
						volumeMute.setImageResource(R.drawable.ic_menu_audio);						
					}
				}
				else {
					Log.i(getClass().getName(), "no epg found");
				}
			}
			else {
				Log.i(getClass().getName(), "No service info exists yet");
			}
		}
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			Log.i(getClass().getName(),"EPG updating");
			updateCurrentEvent();
			if (!ControlActivity.this.isFinishing()) {
				mHandler.postAtTime(this, SystemClock.uptimeMillis()+15000);
			}
		}
	};

	public void epgPrevView(View view) {
		synchronized (currentEPG) {
			epgIndex--;
			if (epgIndex<0) {
				epgIndex = currentEPG.size()-1;
				if (epgIndex<0) epgIndex = 0;
			}
		}
		updateCurrentDisplay();
	}

	public void epgNextView(View view) {
		synchronized (currentEPG) {
			epgIndex++;
			if (epgIndex>=currentEPG.size()) {
				epgIndex = 0;
			}
		}
		updateCurrentDisplay();
	}

	public void muteView(View view) {
		synchronized (volume) {
			Enigma2API api  = DreamToolsActivity.getAPI(this);
			try {
				volume = api.toggleMute();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		updateCurrentDisplay();
	}

	public void addTimerView(View view) {
		Intent activity = new Intent(getBaseContext(),AddTimerActivity.class);
		if ((currentEPG!=null)&&(currentEPG.size()>0)) {
			activity.putExtra(AddTimerActivity.EPG_OBJECT,currentEPG.get(epgIndex).toJSONString());
			activity.putExtra(AddTimerActivity.SERVICE_OBJECT,currentSO.toJSONString());
			startActivity(activity);
		}
		else {
			try {
				// no epg info found, so just add timer for currently selected channels
				WheelView channelRoller = (WheelView) findViewById(R.id.channelRoller);
				@SuppressWarnings("rawtypes")
				ServiceObject serviceObject = (ServiceObject) ((ArrayWheelAdapter)channelRoller.getViewAdapter()).getItem(channelRoller.getCurrentItem());
				String epgString = "{\"e2eventdescription\": \""+getString(R.string.app_name)+"\",\"e2eventduration\": \"6000\",\"e2eventid\": \"\",\"e2eventservicename\": \""+serviceObject.getName()+"\",\"e2eventservicereference\": \""+serviceObject.getReference()+"\",\"e2eventstart\": \""+(new Date().getTime()/1000)+"\",\"e2eventtitle\": \""+serviceObject.getName()+"\"}";
				activity.putExtra(AddTimerActivity.EPG_OBJECT,epgString);
				activity.putExtra(AddTimerActivity.SERVICE_OBJECT,serviceObject.toJSONString());
				//			Utils.infoDialog(this, getString(R.string.error), getString(R.string.error_no_epg), false);		
				startActivity(activity);
			}
			catch (Throwable t) {
				Utils.infoDialog(this, getString(R.string.error), getString(R.string.error_no_epg), false);
			}
		}		
	}

	public void messageView(View view) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.send_message_title));
		alert.setMessage(getString(R.string.send_message_descr));

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				final String message = input.getText().toString();
				AsyncTask<Activity, Object, String> task = new AsyncTask<Activity, Object, String>() {

					@Override
					protected void onPostExecute(String result) {
						Utils.infoDialog(ControlActivity.this, getString(R.string.success), result, false);
					}

					@Override
					protected String doInBackground(Activity... params) {
						Enigma2API api = DreamToolsActivity.getAPI(ControlActivity.this);
						try {
							api.sendMessage(message);
							return getString(R.string.success_message);
						} 
						catch (Throwable t) {
							t.printStackTrace();
							return t.getMessage();
						}

					}
				};
				task.execute(ControlActivity.this);
			}
		});

		alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();

	}

	public void epgView(View view) {
		if (currentSO!=null) {
			EPGObject epg = currentEPG.get(epgIndex);

			Dialog dialog = new Dialog(this);

			dialog.setContentView(R.layout.alert_epg);
			dialog.setTitle(getString(R.string.epgDetails));

			TextView epgChannelTitle = (TextView) dialog.findViewById(R.id.epgChannelTitle);
			epgChannelTitle.setText(currentSO.getName());


			TextView epgServiceTitle = (TextView) dialog.findViewById(R.id.epgServiceTitle);
			epgServiceTitle.setText(epg.getTitle());

			TextView epgServiceDescription = (TextView) dialog.findViewById(R.id.epgServiceDescription);
			epgServiceDescription.setText(epg.getBestDescription());

			TextView epgStart = (TextView) dialog.findViewById(R.id.epgStart);
			epgStart.setText(timeFormat.format(epg.getStart()));

			TextView epgEnd = (TextView) dialog.findViewById(R.id.epgEnd);
			epgEnd.setText(timeFormat.format(epg.getEnd()));

			TextView epgDuration = (TextView) dialog.findViewById(R.id.epgDuration);
			epgDuration.setText(epg.getDurationMinutes());
			dialog.show();
		}
	}

	public void zapView(View view) {
		try {
			WheelView channelRoller = (WheelView) findViewById(R.id.channelRoller);
			@SuppressWarnings("rawtypes")
			ServiceObject serviceObject = (ServiceObject) ((ArrayWheelAdapter)channelRoller.getViewAdapter()).getItem(channelRoller.getCurrentItem());
			DreamToolsActivity.getAPI(ControlActivity.this).zapTo(serviceObject);
		}
		catch (Throwable t) {
			Utils.infoDialog(this, getString(R.string.error), t.getMessage(), false);		
		}
	}

	public void powerView(View view) {
		Enigma2API api  = DreamToolsActivity.getAPI(this);
		try {
			api.toggleStandby();
			Toast.makeText(this, getString(R.string.standyby_toggle), Toast.LENGTH_SHORT).show();
		} 
		catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public void monitorView(View view) {
		Intent act = new Intent(getBaseContext(),MonitorActivity.class);
		startActivity(act);
	}

	@Override
	public void onProgressChanged(View v, int progress) {
		Log.i("controlactivity", "Setting volume to "+progress);
		AsyncTask<Activity, Object, Volume> task = new AsyncTask<Activity, Object, Volume>() {
			@Override
			protected void onPostExecute(Volume volume) {
				if (volume!=null)
					volume.setCurrent(volume.getCurrent());
			}

			@Override
			protected Volume doInBackground(Activity... params) {
				try {
					Enigma2API api  = DreamToolsActivity.getAPI(params[0]);
					return api.setVolume(volume);
				} 
				catch (Throwable e) {
					e.printStackTrace();
					return null;
				}
			}
		};
		task.execute(ControlActivity.this);	



	}

}
