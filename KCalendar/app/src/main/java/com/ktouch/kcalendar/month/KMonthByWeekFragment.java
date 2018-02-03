package com.ktouch.kcalendar.month;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ktouch.kcalendar.CalendarController;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
//import com.ktouch.kcalendar.event.CreateEventDialogFragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class KMonthByWeekFragment extends Fragment implements CalendarController.EventHandler {
    private static final String TAG = "KMonthByWeekFragment";
    private static final boolean DEBUG = false;
    public final static int WEEK_MODE = 0;
    public final static int MONTH_MODE = 1;

    private final static int WEEK_COUNT = 3497;//4186
    private final static int REFRESH_PAGER_DELAY = 10;

    private static final String TAG_EVENT_DIALOG = "event_dialog";

    private ViewPager mPager;
    private KMonthByWeekPagerAdapter mPagerAdapter;
    private Calendar mTodayCalendar;

    private long mInitialTime;
    private int mMode = MONTH_MODE;

    private CalendarController mController;

    public KMonthByWeekFragment() {
        this(System.currentTimeMillis());
		Log.i("wanghg", "KMonthByWeekFragment 1");
    }

    public KMonthByWeekFragment(long initialTime) {
		Log.i("wanghg", "KMonthByWeekFragment 2");
        mInitialTime = initialTime;
    }

    public KMonthByWeekFragment(long initialTime, int mode) {
		Log.i("wanghg", "KMonthByWeekFragment 3");
        mInitialTime = initialTime;
        mMode = mode;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mController = CalendarController.getInstance(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.month_by_week_pager, container, false);
        mPager = (ViewPager) v.findViewById(R.id.pager);
//        mPager.setVisibility(View.INVISIBLE);
        mPagerAdapter = new KMonthByWeekPagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mTodayCalendar = Calendar.getInstance();
        Time time = new Time();
        time.setToNow();
        mTodayCalendar.setTimeInMillis(time.toMillis(false));

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                Log.i("wanghg", "onPageSelected mMode = " + mMode);
                Log.d(TAG, "-=-=-= onPageSelected position = " + position);
                Time time = new Time();
                if (mMode == MONTH_MODE) {
                    int year = position / 12 + Utils.YEAR_MIN;
                    int month = ((position < 12) ? position : (position % 12));
                    time.set(Utils.CLICKED_DAY, month, year);
                    if(!Utils.SELECTED_DAY_IS_DEFAULT && mTodayCalendar!=null && year==mTodayCalendar.get(Calendar.YEAR)
                            && month==mTodayCalendar.get(Calendar.MONTH)) {
                        time.set(mTodayCalendar.get(Calendar.DAY_OF_MONTH), month, year);
                    }
                    mController.sendEvent(getActivity(), CalendarController.EventType.UPDATE_TITLE, time, time, null,
                            -1, CalendarController.ViewType.CURRENT,
                            DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY, null, null);
					mInitialTime = time.toMillis(true);
					Log.d("wanghg","1 -=-=-= year = " + time.year + ", month = " + time.month + ", day = " + time.monthDay);
                } else {
                    time.setJulianDay(Utils.getJulianMondayFromWeeksSinceEpoch(position));
                    if (DEBUG) {
                        Log.d(TAG,"-=-=-= year = " + time.year + ", month = " + time.month + ", day = " + time.monthDay);
                    }
                    if(!Utils.SELECTED_DAY_IS_DEFAULT && mTodayCalendar!=null && time.year==mTodayCalendar.get(Calendar.YEAR)
                            && time.month==mTodayCalendar.get(Calendar.MONTH)) {
                        time.set(mTodayCalendar.get(Calendar.DAY_OF_MONTH), time.month, time.year);
                    }
                    mController.sendEvent(this, CalendarController.EventType.UPDATE_TITLE, time, time, null,
                            -1, CalendarController.ViewType.WEEK,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR, null, null);
					mInitialTime = time.toMillis(true);
					Log.d("wanghg","2 -=-=-= year = " + time.year + ", month = " + time.month + ", day = " + time.monthDay);
                }
                Utils.mCanRefresh = false;
				Log.i("wanghg", "onPageSelected time = " + time.monthDay);
                time.set(time.monthDay-1, time.month, time.year);
                Log.i("wanghg", "onPageSelected time2 = " + time.monthDay);
                mController.sendEvent(this, CalendarController.EventType.GO_TO, time, time, -1,
                        CalendarController.ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null, null);
            }

        });

        goTo(mInitialTime, true);
        // We have to move to left first, because the selected circle will refresh very slow.
//        if(!Utils.mYearToMonth) {
//            final int current = mPager.getCurrentItem();
//            mPager.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (current > 0) {
//                        mPager.setCurrentItem(current - 1);
//                        mPager.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (current > 0) {
//                                    mPager.setCurrentItem(current);
//                                    mPager.setVisibility(View.VISIBLE);
//                                }
//                            }
//                        });
//                    }
//                }
//            }, REFRESH_PAGER_DELAY);
//        } else {
//            mPager.setVisibility(View.VISIBLE);
//        }
        Utils.mYearToMonth = false;
        return v;
    }

    private void goTo(long timeInMillis, boolean isOffset) {
		
		Log.i("wanghg", "KMonthByWeekFragment goTo");
		Log.i("wanghg", "goTo 1111");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
		Log.d("wanghg","2 -=-=-= goTo" + year + ", month = " + month + ", day = " + day);
        if (mMode == MONTH_MODE) {
			Log.i("wanghg", "goTo 222");
            mPager.setCurrentItem((year - Utils.YEAR_MIN) * 12 + month);
        	}
        else {
			Log.i("wanghg", "goTo 333");
            Time time = new Time();
            time.set(timeInMillis);
            int position = Utils.getWeeksSinceEpochFromJulianDay(Time.getJulianDay(time.toMillis(true), time.gmtoff),
                    Time.SUNDAY);
			Log.i("wanghg", "goTo position = " + position);
            mPager.setCurrentItem(position);
        }
        Time goDay = new Time();
        if(Utils.SELECTED_DAY_IS_DEFAULT) {
            Utils.CLICKED_DAY = day;
        }
//        if(isOffset) {
//            goDay.set(day, month+1, year);
//        }else {
            goDay.set(day, month, year);
//        }
        Utils.mCanRefresh = true;
        mController.sendEvent(this, CalendarController.EventType.GO_TO, goDay, goDay, -1,
                CalendarController.ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null, null);
    }

    @Override
    public long getSupportedEventTypes() {
        return CalendarController.EventType.GO_TO | CalendarController.EventType.EVENTS_CHANGED;
    }

    @Override
    public void handleEvent(CalendarController.EventInfo event) {
    Log.i("wanghg", "handleEvent  3");
        if (event.eventType == CalendarController.EventType.GO_TO) {
			Log.i("wanghg", "handleEvent  33");
            if (mMode == MONTH_MODE && event.viewType == CalendarController.ViewType.MONTH
                    || mMode == WEEK_MODE && event.viewType == CalendarController.ViewType.WEEK) {
                    Log.i("wanghg", "handleEvent  333");
                if (event.extraLong == (CalendarController.EXTRA_GOTO_TIME | CalendarController.EXTRA_GOTO_TODAY)) {
					Log.i("wanghg", "handleEvent  3333");
                    goTo(event.selectedTime.toMillis(true), false);
                    final long time = event.selectedTime.toMillis(true);
                    mPager.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            goTo(time, false);
                        }
                    }, 100);
                } else if (event.extraLong == (CalendarController.EXTRA_GOTO_DATE)) {
                Log.i("wanghg", "handleEvent  33333");
//                    if(mPagerAdapter!=null && Utils.mCanRefresh){
//                        mPagerAdapter.notifyDataSetChanged();
//                        Utils.mCanRefresh = false;
//                    }
                }
            }
        } else if (event.eventType == CalendarController.EventType.EVENTS_CHANGED) {
        Log.i("wanghg", "handleEvent  333333");
            eventsChanged();
        }
    }

    @Override
    public void eventsChanged() {

    }

    public class KMonthByWeekPagerAdapter extends FragmentPagerAdapter {

        public KMonthByWeekPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mMode == MONTH_MODE ? 12 * (Utils.YEAR_MAX - Utils.YEAR_MIN)
                    : WEEK_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "-=-=-= getItem position = " + position);
            return new KMonthByWeekViewFragment(getActivity(), position, mMode);
        }

//        @Override
//        public int getItemPosition(Object object) {
//            return POSITION_NONE;
//        }
    }
}
