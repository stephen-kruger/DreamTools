package com.madibasoft.dreamtools.widgets;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.ViewSwitcher;

import com.madibasoft.dreamtools.R;

public class DateTimePicker extends RelativeLayout implements View.OnClickListener, OnDateChangedListener, OnTimeChangedListener {

        // DatePicker reference
        private DatePicker              datePicker;
        // TimePicker reference
        private TimePicker              timePicker;
        // ViewSwitcher reference
        private ViewSwitcher    viewSwitcher;
        // Calendar reference
        private Calendar                mCalendar;

        // Constructor start
        public DateTimePicker(Context context) {
                this(context, null);
        }

        public DateTimePicker(Context context, AttributeSet attrs) {
                this(context, attrs, 0);
        }

        public DateTimePicker(Context context, AttributeSet attrs, int defStyle) {
                super(context, attrs, defStyle);

                // Get LayoutInflater instance
                final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                // Inflate myself
                inflater.inflate(R.layout.widget_datetimepicker, this, true);

                // Inflate the date and time picker views
                final View datePickerView = inflater.inflate(R.layout.widget_datepicker, null);
                final View timePickerView = inflater.inflate(R.layout.widget_timepicker, null);

                // Grab a Calendar instance
                mCalendar = Calendar.getInstance();
                // Grab the ViewSwitcher so we can attach our picker views to it
                viewSwitcher = (ViewSwitcher) this.findViewById(R.id.DateTimePickerVS);

                // Init date picker
                datePicker = (DatePicker) datePickerView.findViewById(R.id.DatePicker);
                datePicker.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), this);

                // Init time picker
                timePicker = (TimePicker) timePickerView.findViewById(R.id.TimePicker);
                timePicker.setOnTimeChangedListener(this);

                // Handle button clicks
                ((Button) findViewById(R.id.SwitchToTime)).setOnClickListener(this); // shows the time picker
                ((Button) findViewById(R.id.SwitchToDate)).setOnClickListener(this); // shows the date picker

                // Populate ViewSwitcher
                viewSwitcher.addView(datePickerView, 0);
                viewSwitcher.addView(timePickerView, 1);
        }
        // Constructor end

        // Called every time the user changes DatePicker values
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Update the internal Calendar instance
                mCalendar.set(year, monthOfYear, dayOfMonth, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
        }

        // Called every time the user changes TimePicker values
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                // Update the internal Calendar instance
                mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
        }

        // Handle button clicks
        public void onClick(View v) {
                if (v.getId() == R.id.SwitchToDate) {
                        v.setEnabled(false);
                        findViewById(R.id.SwitchToTime).setEnabled(true);
                        viewSwitcher.showPrevious();
                } else if (v.getId() == R.id.SwitchToTime) {
                        v.setEnabled(false);
                        findViewById(R.id.SwitchToDate).setEnabled(true);
                        viewSwitcher.showNext();
                }
        }

        // Convenience wrapper for internal Calendar instance
        public int get(final int field) {
                return mCalendar.get(field);
        }

        // Reset DatePicker, TimePicker and internal Calendar instance
        public void reset() {
                final Calendar c = Calendar.getInstance();
                updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                updateTime(c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE));
        }

        // Convenience wrapper for internal Calendar instance
        public long getDateTimeMillis() {
                return mCalendar.getTimeInMillis();
        }

        // Convenience wrapper for internal TimePicker instance
        public void setIs24HourView(boolean is24HourView) {
                timePicker.setIs24HourView(is24HourView);
        }
        
        // Convenience wrapper for internal TimePicker instance
        public boolean is24HourView() {
                return timePicker.is24HourView();
        }

        // Convenience wrapper for internal DatePicker instance
        public void updateDate(int year, int monthOfYear, int dayOfMonth) {
                datePicker.updateDate(year, monthOfYear, dayOfMonth);
        }

        // Convenience wrapper for internal TimePicker instance
        public void updateTime(int currentHour, int currentMinute) {
                timePicker.setCurrentHour(currentHour);
                timePicker.setCurrentMinute(currentMinute);
        }
}