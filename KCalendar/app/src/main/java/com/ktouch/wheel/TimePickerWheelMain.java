package com.ktouch.wheel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TimePickerWheelMain {

    private static final String TAG="TimePicker_WheelMain";

    private View mView;
    private WheelView mWVDate;

    private WheelView mWVHours;
    private WheelView mWVMinutes;

    private TextView mDateField;
    private TextView mTimeField;
    private long mSelectedDate;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;

    private Calendar mCalendar;

    private int START_YEAR = Utils.YEAR_MIN, END_YEAR;

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        mView = view;
    }

    public int getSTART_YEAR() {
        return START_YEAR;
    }

    public void setSTART_YEAR(int sTART_YEAR) {
        START_YEAR = sTART_YEAR;
    }

    public int getEND_YEAR() {
        return END_YEAR;
    }

    public void setEND_YEAR(int eND_YEAR) {
        END_YEAR = eND_YEAR;
    }

    public void setDateField(TextView textView) {
        mDateField = textView;
    }

    public void setTimeField(TextView textView) {
        mTimeField = textView;
    }

    public long getSelectedDate() {
        return mSelectedDate;
    }

    public TimePickerWheelMain(View view) {
        super();
        mView = view;
        setView(view);
    }

    public TimePickerWheelMain(View view, boolean hasSelectTime) {
        super();
        mView = view;
        setView(view);
    }

    public void initDateTimePicker(Context context, int year, int month, int day) {
        this.initDateTimePicker(context, year, month, day, -1, -1, -1);
    }

    private void setAllField(){
        mCalendar.set(mYear, mMonth, mDay);
        mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
        mCalendar.set(Calendar.MINUTE, mMinute);
        mSelectedDate = mCalendar.getTimeInMillis();
        Log.e(TAG, "setAllField = "+mCalendar.get(Calendar.YEAR)+", "+mCalendar.get(Calendar.MONTH)
                +", "+mCalendar.get(Calendar.DAY_OF_MONTH)+";     "+mCalendar.get(Calendar.HOUR_OF_DAY)
                +":"+mCalendar.get(Calendar.MINUTE));
    }

    private void setDate(Context context, TextView textView){
        textView.setText(Utils.getWholeDateFormat(context, mSelectedDate));
    }

    private void setTime(Context context, TextView textView){
        textView.setText(Utils.getWholeTimeFormat(context, mSelectedDate));
    }

    public void initDateTimePicker(final Context context, int year, int month, int day, int h, int m, int s) {

        mWVDate = (WheelView) mView.findViewById(R.id.date_wheel);
        mWVHours = (WheelView) mView.findViewById(R.id.hour_wheel);
        mWVMinutes = (WheelView) mView.findViewById(R.id.min_wheel);

        mCalendar = Calendar.getInstance(TimeZone.getTimeZone(Utils.getTimeZone(context, null)));
        Calendar newCalendar = (Calendar) mCalendar.clone();
        newCalendar.set(1970, 0, 1, h, m);

        mYear = year;
        mMonth = month;
        mDay = day;
        mHour = h;
        mMinute = m;

        DayArrayAdapter dateAdapter = new DayArrayAdapter(context, newCalendar);
        mWVDate.setAdapter(dateAdapter);
        mWVDate.setCyclic(false);
        mCalendar.set(year, month, day, h, m);
        mSelectedDate = mCalendar.getTimeInMillis();
        int currentIndex = dateAdapter.getIndexByMillis(mSelectedDate);
        mWVDate.setCurrentItem(currentIndex);
        mDateField.setText(Utils.getWholeDateFormat(context, mSelectedDate));

        OnWheelChangedListener dateWheelListener = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (mDateField != null) {
                    int index = wheel.getCurrentItem();
                    mSelectedDate = Utils.mDatesArray.get(index);
                    Date date = new Date(mSelectedDate);
                    mCalendar.setTime(date);
                    mYear = mCalendar.get(Calendar.YEAR);
                    mMonth = mCalendar.get(Calendar.MONTH);
                    mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
                    setAllField();
                    setDate(context, mDateField);
                }
            }
        };
        mWVDate.addChangingListener(dateWheelListener);

        mWVHours.setAdapter(new NumericWheelAdapter(0, 23));
        mWVHours.setCyclic(true);
        mWVHours.setCurrentItem(h);

        OnWheelChangedListener hourWheelListener = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (mTimeField != null) {
                    mHour = wheel.getCurrentItem();
                    setAllField();
                    mSelectedDate = mCalendar.getTimeInMillis();
                    setTime(context, mTimeField);
                }
            }
        };
        mWVHours.addChangingListener(hourWheelListener);

        mWVMinutes.setAdapter(new NumericWheelAdapter(0, 59));
        mWVMinutes.setCyclic(true);
        mWVMinutes.setCurrentItem(m);
        mTimeField.setText(Utils.getWholeTimeFormat(context, mSelectedDate));

        OnWheelChangedListener minuteWheelListener = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (mTimeField != null) {
                    mMinute = wheel.getCurrentItem();
                    setAllField();
                    setTime(context, mTimeField);
                }
            }
        };
        mWVMinutes.addChangingListener(minuteWheelListener);
    }


    private class DayArrayAdapter extends TextWheelAdapter {
        // Calendar
        Calendar mCalendar;

        @Override
        public String getItem(int index) {
            String strDate = Utils.getDateWithWeek(context, Utils.mDatesArray.get(index));
            return strDate;
        }

        public int getIndexByMillis(long millis) {
            Calendar newCalendar = (Calendar) mCalendar.clone();
            newCalendar.set(1970, 0, 1);
            long startDateMillis = newCalendar.getTimeInMillis();
            int index = (int) ((millis - startDateMillis) / Utils.DAY_IN_MILLIS);
            return index;
        }

        @Override
        public int getMaximumLength() {
            return Utils.mDatesArray.size();
        }

        /**
         * Constructor
         */
        protected DayArrayAdapter(Context context, Calendar calendar) {
            super(context, -1, NO_RESOURCE);
            mCalendar = calendar;
        }

        public int getItemsCount() {
            return Utils.mDatesArray.size() + 1;
        }

        @Override
        protected CharSequence getItemText(int index) {
            return "";
        }
    }


}
