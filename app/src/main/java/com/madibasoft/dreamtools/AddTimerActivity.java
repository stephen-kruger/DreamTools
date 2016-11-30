package com.madibasoft.dreamtools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.webhiker.enigma2.api.EPGObject;
import com.webhiker.enigma2.api.Enigma2API;
import com.webhiker.enigma2.api.ServiceObject;

public class AddTimerActivity extends Activity {

	public static final String SERVICE_OBJECT = "serviceobject";
	public static final String EPG_OBJECT = "epgobject";

	private Date startTime, endTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_timer);
		try {
			EPGObject epg = new EPGObject(getIntent().getStringExtra(EPG_OBJECT));
			setTitle(R.string.add_timer);				
			((EditText)findViewById(R.id.timerTitle)).setText(epg.getTitle());
			((EditText)findViewById(R.id.timerDescription)).setText(epg.getBestDescription());

			startTime = epg.getStart(); if (new Date().after(startTime)) startTime = new Date();
			endTime = epg.getEnd();if (new Date().after(endTime)) endTime = new Date();

			initTime(startTime,(WheelView) findViewById(R.id.startHour), (WheelView) findViewById(R.id.startMinute), 
					(WheelView) findViewById(R.id.startDay), (WheelView) findViewById(R.id.startMonth),(WheelView) findViewById(R.id.startYear),
					(TextView) findViewById(R.id.selectedStartTime), true);
			initTime(endTime,(WheelView) findViewById(R.id.endHour), (WheelView) findViewById(R.id.endMinute), 
					(WheelView) findViewById(R.id.endDay), (WheelView) findViewById(R.id.endMonth),(WheelView) findViewById(R.id.endYear),
					(TextView) findViewById(R.id.selectedEndTime), false);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void initTime(Date time, final WheelView hour, final WheelView minute, 
			final WheelView day, final WheelView month, final WheelView year, 
			final TextView selectedView, final boolean isStart) {
		OnWheelChangedListener listener = new OnWheelChangedListener() {

			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (isStart)
					startTime =updateDays(hour, minute, day, month, year, selectedView);
				else
					endTime = updateDays(hour, minute, day, month, year, selectedView);
			}
		};

		// hour
		int curHour = time.getHours();
		String hours[] = new String[24];
		for (int i = 0; i < hours.length;i++) {
			hours[i] = ""+i;
			if (i<10)
				hours[i] = "0"+hours[i];
		}
		hour.setViewAdapter(new DateArrayAdapter(this, hours, curHour));
		hour.setCurrentItem(curHour);
		hour.addChangingListener(listener);

		// min
		int curMin = time.getMinutes();
		String mins[] = new String[60];
		for (int i = 0; i < mins.length;i++) {
			mins[i] = ""+i;
			if (i<10)
				mins[i] = "0"+mins[i];
		}
		minute.setViewAdapter(new DateArrayAdapter(this, mins, curMin));
		minute.setCurrentItem(curMin);
		minute.addChangingListener(listener);

		// month
		int curMonth = time.getMonth();//calendar.get(Calendar.MONTH);
		String months[] = new String[12];
		for (int i = 0; i < months.length;i++) {
			months[i] = ""+(i+1);
		}
		month.setViewAdapter(new DateArrayAdapter(this, months, curMonth));
		month.setCurrentItem(curMonth);
		month.addChangingListener(listener);

		// year
		SimpleDateFormat simpleDateformat=new SimpleDateFormat("yyyy");
		int curYear = Integer.parseInt(simpleDateformat.format(time));
		year.setViewAdapter(new DateNumericAdapter(this, curYear, curYear + 10, 0));
		year.setCurrentItem(curYear);
		year.addChangingListener(listener);

		//day
		updateDays(hour, minute, day, month, year, selectedView);
		day.setCurrentItem(time.getDate()-1);
		day.addChangingListener(listener);
		// update again so our text display is updated
		updateDays(hour, minute, day, month, year, selectedView);

	}

	/**
	 * Updates day wheel. Sets max days according to selected month and year
	 */
	private Date updateDays(WheelView hour, WheelView minute, WheelView day, WheelView month, WheelView year, TextView selectedView) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + year.getCurrentItem());
		calendar.set(Calendar.MONTH, month.getCurrentItem());

		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		day.setViewAdapter(new DateNumericAdapter(this, 1, maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1));
		int curDay = Math.min(maxDays, day.getCurrentItem() + 1);
		day.setCurrentItem(curDay - 1, true);

		int y = year.getCurrentItem()+2011-1900;
		int m = month.getCurrentItem();
		int d = day.getCurrentItem()+1;
		int h = hour.getCurrentItem();
		int min = minute.getCurrentItem();

		Date date = new Date(y, m, d, h, min);
//		Log.i("xxx", "year="+y+" month="+m+" day="+d+" hour="+h+" min="+min+" date="+date.toLocaleString());
		selectedView.setText(date.toLocaleString());

		return date;
	}

	/**
	 * Adapter for numeric wheels. Highlights the current value.
	 */
	private class DateNumericAdapter extends NumericWheelAdapter {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		/**
		 * Constructor
		 */
		public DateNumericAdapter(Context context, int minValue, int maxValue, int current) {
			super(context, minValue, maxValue);
			this.currentValue = current;
			setTextSize(16);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (currentItem == currentValue) {
				view.setTextColor(0xFF0000F0);
			}
			view.setTypeface(Typeface.SANS_SERIF);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			currentItem = index;
			return super.getItem(index, cachedView, parent);
		}
	}

	/**
	 * Adapter for string based wheel. Highlights the current value.
	 */
	private class DateArrayAdapter extends ArrayWheelAdapter<String> {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		/**
		 * Constructor
		 */
		public DateArrayAdapter(Context context, String[] items, int current) {
			super(context, items);
			this.currentValue = current;
			setTextSize(16);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (currentItem == currentValue) {
				view.setTextColor(0xFF0000F0);
			}
			view.setTypeface(Typeface.SANS_SERIF);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			currentItem = index;
			return super.getItem(index, cachedView, parent);
		}
	}

	public void okView(View view) {
		Log.i("addtimeractivity", startTime+" "+endTime);
		AsyncTask<Activity, Object, String> task = new AsyncTask<Activity, Object, String>() {
			
			@Override
			protected void onPostExecute(String result) {
				Utils.infoDialog(AddTimerActivity.this, getString(R.string.success_timer), result, true);
			}

			@Override
			protected String doInBackground(Activity... params) {
				Enigma2API api = DreamToolsActivity.getAPI(AddTimerActivity.this);
				try {
					int timerType = 0;
					RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);					 
					int checkedRadioButton = radioGroup.getCheckedRadioButtonId();					 
					switch (checkedRadioButton) {
					  case R.id.radioRecord : timerType = Enigma2API.TIMER_RECORD;
					                   	              break;
					  case R.id.radioZap : timerType = Enigma2API.TIMER_SWITCH;
					}
					ServiceObject service = new ServiceObject(getIntent().getStringExtra(SERVICE_OBJECT));
					EPGObject epg = new EPGObject(getIntent().getStringExtra(EPG_OBJECT));
					EditText name = (EditText) params[0].findViewById(R.id.timerTitle);
					EditText description = (EditText) params[0].findViewById(R.id.timerDescription);
					epg.setStart(startTime);
					epg.setEnd(endTime);
					api.addTimer(name.getText().toString(), description.getText().toString(), service, epg, timerType);
					return getString(R.string.success);
				} 
				catch (Throwable t) {
					t.printStackTrace();
					return t.getMessage();
				}

			}
		};
		task.execute(AddTimerActivity.this);
	}

	public void cancelView(View view) {
		finish();
	}
}
