package com.madibasoft.dreamtools;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.madibasoft.dreamtools.widgets.ExpandableListAdapter;
import com.webhiker.enigma2.api.EPGObject;
import com.webhiker.enigma2.api.Enigma2API;
import com.webhiker.enigma2.api.ServiceInformationObject;
import com.webhiker.enigma2.api.ServiceObject;


public abstract class AbstractServiceObjectActivity extends Activity {

	private ExpandableListAdapter adapter;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private Handler mHandler = new Handler();
	private ServiceObject currentSO;
	private List<EPGObject> currentEPG;

	public void onCreate(Bundle savedInstanceState, final boolean isZap) {
		super.onCreate(savedInstanceState);
		if (isZap) requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_zap);
		if (isZap) getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_zap);

		// Retrive the ExpandableListView from the layout
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);

		listView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView listView, View view, int groupPosition, int childPosition, long id) {
				try {
					ServiceObject service = (ServiceObject) adapter.getChild(groupPosition, childPosition);
					childClicked(service);
					Toast.makeText(getBaseContext(), service.getName(), Toast.LENGTH_SHORT).show();
					if (isZap) updateCurrentEvent();
				}
				catch (Throwable t) {
					Toast.makeText(getBaseContext(), t.getMessage(), Toast.LENGTH_LONG).show();
				}					
				return true;
			}
		});

		listView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView listView, View view, int groupPosition, long id) {
				if (adapter.getChildrenCount(groupPosition)==0) {
					final ProgressBar pb = (ProgressBar) view.findViewById(R.id.bouquetProgress);
					pb.setVisibility(ProgressBar.VISIBLE);
					final ServiceObject group = ((ServiceObject)adapter.getGroup(groupPosition));

					// ---------------------------
					AsyncTask<Activity, Object, Integer> task = new AsyncTask<Activity, Object, Integer>() {

						@Override
						protected void onProgressUpdate(Object... progress) {
						}

						@Override
						protected void onPostExecute(Integer result) {
							pb.setVisibility(ProgressBar.INVISIBLE);
						}

						@Override
						protected Integer doInBackground(Activity... params) {
							// lazy load the group services
							try {
								@SuppressWarnings("unchecked")
								List<ServiceObject> services = (List<ServiceObject>) getChildren(group);
								for (ServiceObject service : services) {
									adapter.addItem(group,service);
								}
								handler.sendEmptyMessage(1);
							}
							catch (Throwable t) {
								// sometimes we get an exception here : Only one Looper may be created per thread
								try {
									Looper.prepare();
								}
								catch (Throwable tx) {
									tx.printStackTrace();
								}
								Toast.makeText(getBaseContext(), t.getMessage(), Toast.LENGTH_LONG).show();
							}							
							return 0;
						}


					};
					task.execute(AbstractServiceObjectActivity.this);
					// ---------------------------
				}
				else {
					Log.i(getClass().getName(), "Services already fetched");
				}
				return false;
			}
		});

		// Initialize the adapter with blank groups and children
		// We will be adding children on a thread, and then update the ListView
		adapter = new ExpandableListAdapter(this, new ArrayList<ServiceObject>(),new ArrayList<ArrayList<ServiceObject>>());

		// Set this blank adapter to the list view
		listView.setAdapter(adapter);

		// ---------------------------
		final ProgressDialog dialog = ProgressDialog.show(AbstractServiceObjectActivity.this, getText(R.string.sync_title), getText(R.string.sync_description),false);
		AsyncTask<Activity, Object, Integer> task = new AsyncTask<Activity, Object, Integer>() {

			@Override
			protected void onProgressUpdate(Object... progress) {
				dialog.setMessage((String)progress[0]);
				dialog.setTitle((String)progress[1]);
			}

			@Override
			protected void onPostExecute(Integer result) {
				adapter.notifyDataSetChanged();
				dialog.dismiss();
			}

			@Override
			protected Integer doInBackground(Activity... params) {
				publishProgress(params[0].getString(R.string.sync_description),params[0].getString(R.string.sync_title));
				try {
					@SuppressWarnings("unchecked")
					List<ServiceObject> ja = (List<ServiceObject>) getGroups();
					for (ServiceObject bouquet : ja) {
						adapter.addItem(bouquet);
					}
				} 
				catch (Throwable t) {
					t.printStackTrace();
					Log.e(getClass().getName(), "Error:"+t.getMessage());
				}
				finally {
					handler.sendEmptyMessage(1);
				}				
				return 0;
			}
		};
		task.execute(AbstractServiceObjectActivity.this);

		// start thread to poll epg changes
		if (isZap) mHandler.postAtTime(mUpdateTimeTask, SystemClock.uptimeMillis()+500);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreate(savedInstanceState,true);
	}

	abstract List<? extends ServiceObject> getGroups() throws IOException, JSONException;

	abstract void childClicked(ServiceObject service) throws IOException;

	abstract List<? extends ServiceObject> getChildren(ServiceObject group) throws JSONException, IOException;

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
				try {
					ServiceObject so = sio.getService();
					currentSO = so;
					List<EPGObject> epg = sio.getEPG();
					currentEPG = epg;
					updateCurrentDisplay();
				}
				catch (Throwable t) {
					t.printStackTrace();
				}
			}

			@Override
			protected ServiceInformationObject doInBackground(Activity... params) {
				Enigma2API api = DreamToolsActivity.getAPI(AbstractServiceObjectActivity.this);
				try {
					ServiceInformationObject sio = api.getCurrent();
					return sio;
				} 
				catch (Throwable t) {
					Log.e(getClass().getName(), "Error updating current event :"+t.getMessage());
				}

				return null;
			}
		};
		task.execute(AbstractServiceObjectActivity.this);
	}

	public void updateCurrentDisplay() {
		if ((currentSO!=null)&&(currentEPG!=null)) {
			((TextView)findViewById(R.id.currentService)).setText(currentSO.getName(AbstractServiceObjectActivity.this.getString(R.string.no_title)));
			if (currentEPG.size()>0) {
				((TextView)findViewById(R.id.currentTitle)).setText(currentEPG.get(0).getTitle(AbstractServiceObjectActivity.this.getString(R.string.no_title)));
				((TextView)findViewById(R.id.currentStart)).setText(timeFormat.format(currentEPG.get(0).getStart())+"-"+timeFormat.format(currentEPG.get(0).getEnd()));
				if (currentEPG.size()>1) {
					((TextView)findViewById(R.id.nextTitle)).setText(currentEPG.get(1).getTitle(AbstractServiceObjectActivity.this.getString(R.string.no_title)));
					((TextView)findViewById(R.id.nextStart)).setText(timeFormat.format(currentEPG.get(1).getStart())+"-"+timeFormat.format(currentEPG.get(1).getEnd()));					
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

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			Log.i(getClass().getName(),"<><><><><><>update thread<><><><><><>");
			updateCurrentEvent();
			mHandler.postAtTime(this, SystemClock.uptimeMillis()+20000);
		}
	};

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		// this is to improve dithering for gradients
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
			super.handleMessage(msg);
		}

	};

	public void controlView(View view) {
		Intent act = new Intent(getBaseContext(),ControlActivity.class);
		startActivity(act);
	}

}
