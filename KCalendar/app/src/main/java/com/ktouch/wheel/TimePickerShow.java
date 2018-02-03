package com.ktouch.wheel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class TimePickerShow {

	private Context context;
	private TimePickerWheelMain mTimePickerWheelMain;

	public TimePickerShow(Context context) {
		super();
		this.context = context;
	}

	public long getSelectedDateAndTime(){
		if(mTimePickerWheelMain ==null){
			return -1;
		}
		return mTimePickerWheelMain.getSelectedDate();
	}

	public View timePickerView(long millis, TextView dateField, TextView timeField) {

		View timepickerview = View.inflate(context, R.layout.timepicker, null);
		mTimePickerWheelMain = new TimePickerWheelMain(timepickerview);
		mTimePickerWheelMain.setDateField(dateField);
		mTimePickerWheelMain.setTimeField(timeField);

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		// int second = calendar.get(Calendar.SECOND);
		mTimePickerWheelMain.setEND_YEAR(year);

		if (millis>0) {
			Date date = new Date(millis);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			try {
				calendar.setTime(date);
				year = calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH);
				day = calendar.get(Calendar.DAY_OF_MONTH);
				hour = calendar.get(Calendar.HOUR_OF_DAY);
				min = calendar.get(Calendar.MINUTE);
				mTimePickerWheelMain.initDateTimePicker(context, year, month, day, hour, min, -1);// 传-1表示不显示

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			mTimePickerWheelMain.initDateTimePicker(context, year, month, day, hour, min, -1);
		}
		return timepickerview;
	}

}
