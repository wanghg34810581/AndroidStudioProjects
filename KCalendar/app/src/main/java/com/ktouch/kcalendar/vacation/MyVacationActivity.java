package com.ktouch.kcalendar.vacation;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktouch.kcalendar.AbstractCalendarActivity;
import com.ktouch.kcalendar.CalendarController;
import com.ktouch.kcalendar.CalendarViewAdapter;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
import com.ktouch.kcalendar.VacationUtils;
import com.ktouch.kcalendar.contentprovider.KCalendarProviderHelper;
import com.ktouch.kcalendar.month.KMonthByWeekFragment;

import java.util.Calendar;

public class MyVacationActivity extends AbstractCalendarActivity implements CalendarController.EventHandler {

    public static final String EXTRA_TIMEMILLIS = "extra_timemillis";
    protected int mFirstDayOfWeek;
    protected int mDaysPerWeek = 7;
    private String[] mDayLabels;
    private int mSaturdayColor = 0;
    private int mSundayColor = 0;
    private int mDayNameColor = 0;
    private long mTimeMillis;

    private ViewGroup mDayNamesHeader;
    private CalendarController mController;
    private long mStartTimeForFirst = 0;
    private long mCurrentTime;

    private TextView mTitle;
    private TextView mTodayState;
    private TextView mTomorrowState;

    private boolean mIsFirst = true;
    private int mFragTotalHeight = 0;
    private int mWeekLines = 0;
    private LinearLayout mVacationStateLayout;

    private CalendarViewAdapter mCalendarViewAdapter;

    private CursorLoader mLoader;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.my_vacation);
        VacationUtils.CONTENT_MODE = VacationUtils.VACATION_MODE;
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        long millis = time.toMillis(true);
        Intent intent = getIntent();
        mTimeMillis = intent.getLongExtra(EXTRA_TIMEMILLIS, millis);
        mCurrentTime = mTimeMillis;
        mController = CalendarController.getInstance(this);
        mController.registerFirstEventHandler(0, this);
        setUpHeader();
        updateHeader();
        setMainPane(mTimeMillis);
        mTitle = (TextView) findViewById(R.id.vacation_toolbar_title);
        mTodayState = (TextView) findViewById(R.id.today_state);
        mTomorrowState = (TextView) findViewById(R.id.tomorrow_state);
        mVacationStateLayout = (LinearLayout) findViewById(R.id.vacation_state);

        TextView txtVacationCustom = (TextView) findViewById(R.id.vacation_custom_tv);
        txtVacationCustom.setOnClickListener(mClickListener);

        String date = Utils.getYearAndMonthFormat(this, mTimeMillis);
        mTitle.setText(getString(R.string.my_vacation_toolbar_title, date));

        mCalendarViewAdapter = new CalendarViewAdapter(this, CalendarController.ViewType.MONTH, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VacationUtils.CONTENT_MODE = VacationUtils.EVENTS_MODE;
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.vacation_custom_tv:
                    Intent i = new Intent(getApplicationContext(), MyVacationOperationActivity.class);
                    i.putExtra(EXTRA_TIMEMILLIS, mCurrentTime);
                    startActivity(i);
                    break;
            }
        }
    };

    private void setUpHeader() {
        mDayNamesHeader = (ViewGroup) findViewById(R.id.week_names);
        mFirstDayOfWeek = Utils.getFirstDayOfWeek(this);
        Resources res = getResources();
        mSaturdayColor = res.getColor(R.color.month_saturday);
        mSundayColor = res.getColor(R.color.month_sunday);
        mDayNameColor = res.getColor(R.color.month_day_names_color);
        mDayLabels = res.getStringArray(R.array.week_days_title_array);
    }

    private void updateHeader() {
        TextView label = (TextView) mDayNamesHeader.findViewById(R.id.wk_label);
        mDayNamesHeader.setVisibility(View.VISIBLE);
        int offset = mFirstDayOfWeek - 1;
        for (int i = 1; i < 8; i++) {
            label = (TextView) mDayNamesHeader.getChildAt(i);
            if (i < mDaysPerWeek + 1) {
                int position = (offset + i) % 7;
                label.setText(mDayLabels[position]);
                label.setVisibility(View.VISIBLE);
                if (position == Time.SATURDAY) {
                    label.setTextColor(mSaturdayColor);
                } else if (position == Time.SUNDAY) {
                    label.setTextColor(mSundayColor);
                } else {
                    label.setTextColor(mDayNameColor);
                }
            } else {
                label.setVisibility(View.GONE);
            }
        }
        mDayNamesHeader.invalidate();
    }

    private void setMainPane(long timeMillis) {

        FragmentManager fragmentManager = getFragmentManager();
        // Create new fragment
        Fragment frag = new KMonthByWeekFragment(timeMillis);
        FragmentTransaction ft = fragmentManager.beginTransaction();

//        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.month_pane, frag);
        ft.commit();
        // If the key is already registered this will replace it
        mController.registerEventHandler(R.id.month_pane, (CalendarController.EventHandler) frag);
    }

    @Override
    public void handleEvent(CalendarController.EventInfo event) {
        long displayTime = -1;
        if (event.eventType == CalendarController.EventType.UPDATE_TITLE) {
            long time = mController.getTime();
            mCurrentTime = time;
            String date = Utils.getYearAndMonthFormat(this, time);
            mTitle.setText(getString(R.string.my_vacation_toolbar_title, date));
            if (mStartTimeForFirst == 0) {
                mStartTimeForFirst = event.startTime.toMillis(false);
            }
            refreshPane(event.startTime.toMillis(false));
        } else if (event.eventType == CalendarController.EventType.VACATION_GO_TO) {
            KCalendarProviderHelper helper = new KCalendarProviderHelper(getApplicationContext());
            Time selectedTime = event.selectedTime;
            Vacation today = helper.readFromDatabaseByTime(selectedTime);
            int todayState = today == null ? 0 :today.mState;
            Time tomorrowTime = Utils.getTomorrow(selectedTime);
            Vacation tomorrow = helper.readFromDatabaseByTime(tomorrowTime);
            int tomorrowState = tomorrow == null ? 0 : tomorrow.mState;
            mTodayState.setText(todayState == 3 ? R.string.rest_today : R.string.work_today);
            mTomorrowState.setText(tomorrowState == 3 ? R.string.rest_tomorrow : R.string.work_tomorrow);
        }

    }

    @Override
    public long getSupportedEventTypes() {
        return CalendarController.EventType.GO_TO | CalendarController.EventType.UPDATE_TITLE
                | CalendarController.EventType.VACATION_GO_TO;
    }

    @Override
    public void eventsChanged() {
        mController.sendEvent(this, CalendarController.EventType.EVENTS_CHANGED, null, null, -1, CalendarController.ViewType.CURRENT);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mIsFirst) {
            refreshPane(mStartTimeForFirst);
            mIsFirst = false;
        }
    }

    public void onBackPressed(View v) {
        onBackPressed();
    }

    public void goToToday(View v) {
        Time t = null;
        long extras = CalendarController.EXTRA_GOTO_TIME;
        int viewType = CalendarController.ViewType.MONTH;
        t = new Time(Time.getCurrentTimezone());
        t.setToNow();
        extras |= CalendarController.EXTRA_GOTO_TODAY;
        mController.sendEvent(this, CalendarController.EventType.GO_TO, t, null, t, -1, viewType, extras, null, null);
    }

    private boolean setAction(int actionId) {

        return true;
    }

    private void refreshPane(long millis) {
        if (mCalendarViewAdapter != null) {
            mCalendarViewAdapter.setTime(millis);
        }
        if (mFragTotalHeight == 0) {
            RelativeLayout totalLayout = (RelativeLayout) findViewById(R.id.vacation_total_layout);
            int h0 = totalLayout.getHeight();
            int h1 = mDayNamesHeader.getHeight();
            LinearLayout appBar = (LinearLayout) findViewById(R.id.vacation_toolbar);
            int h2 = appBar.getHeight();
            RelativeLayout bottomBar = (RelativeLayout) findViewById(R.id.vacation_bottom_bar);
            int h3 = bottomBar.getHeight();
            mFragTotalHeight = h0 - h1 / 4 - h2 - h3;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        mWeekLines = Utils.getLineCount(this, calendar, CalendarController.ViewType.MONTH);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500);
        layoutParams.height = mFragTotalHeight - Utils.mWeekLineHeight * mWeekLines;
        if (mVacationStateLayout != null) {
            mVacationStateLayout.setLayoutParams(layoutParams);
        }
    }
}
