package com.ktouch.kcalendar.vacation;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ktouch.kcalendar.AbstractCalendarActivity;
import com.ktouch.kcalendar.CalendarController;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
import com.ktouch.kcalendar.month.KMonthByWeekViewFragment;

/**
 * Created by zhang-yi on 2016/6/27 0027.
 */
public class MyVacationOperationActivity extends AbstractCalendarActivity implements CalendarController.EventHandler,
        View.OnClickListener{
    private CalendarController mController;
    private long mTimeMillis;
    protected int mFirstDayOfWeek;
    protected int mDaysPerWeek = 7;
    private String[] mDayLabels;
    private int mSaturdayColor = 0;
    private int mSundayColor = 0;
    private int mDayNameColor = 0;

    private ViewGroup mDayNamesHeader;
    private TextView mTitle;
    private TextView mTxtCancel;
    private TextView mTxtSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_vacation_op);

        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        long millis = time.toMillis(true);
        Intent intent = getIntent();
        mTimeMillis = intent.getLongExtra(MyVacationActivity.EXTRA_TIMEMILLIS, millis);
        mController = CalendarController.getInstance(this);
        mController.registerFirstEventHandler(0, this);

        setUpHeader();
        updateHeader();
        setMainPane(mTimeMillis);
        mTitle = (TextView) findViewById(R.id.vacation_toolbar_title);
        String date = Utils.getYearAndMonthFormat(this, mTimeMillis);
        mTitle.setText(getString(R.string.my_vacation_toolbar_title, date));
        mTxtCancel = (TextView) findViewById(R.id.left_tv);
        mTxtSave = (TextView) findViewById(R.id.right_tv);
        mTxtCancel.setText(R.string.vacation_cancel);
        mTxtSave.setText(R.string.vacation_save);
        mTxtCancel.setOnClickListener(this);
        mTxtSave.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public long getSupportedEventTypes() {
        return 0;
    }

    @Override
    public void handleEvent(CalendarController.EventInfo event) {

    }

    @Override
    public void eventsChanged() {

    }

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
        Fragment frag = new KMonthByWeekViewFragment(getApplicationContext(), timeMillis);
        FragmentTransaction ft = fragmentManager.beginTransaction();

//        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.month_pane, frag);
        ft.commit();
        // If the key is already registered this will replace it
//        mController.registerEventHandler(R.id.month_pane, (CalendarController.EventHandler) frag);
    }

    public void onBackPressed(View v) {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_tv:

                break;
            case R.id.right_tv:

                break;
            default:
                break;
        }
    }
}
