package com.ktouch.kcalendar.month;

import android.content.Context;
import android.content.res.Configuration;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import com.ktouch.kcalendar.CalendarController;
import com.ktouch.kcalendar.Event;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
import com.ktouch.kcalendar.VacationUtils;
import com.ktouch.kcalendar.vacation.Vacation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhang-yi on 2016/5/17 0017.
 */
public class KMonthByWeekContent implements View.OnTouchListener {
    private static final String TAG = "wanghg";
    private static final boolean DEBUG = true;

    private Context mContext;

    private int mWeekHeight;
    private int mMonthPosition;
    private int mMode;
    private View mParentView;

    private Time mSelectedDay;
    private final boolean mShowAgendaWithMonth;

    private static int mOnDownDelay;
    private static float mMovedPixelToCancel;
    private static final int mOnTapDelay = 100;
    private static int mTotalClickDelay;

    private CalendarController mController;

    private String mHomeTimeZone;
    private Time mToday;
    private Time mTempTime = new Time();

    private MonthWeekEventsView mClickedView;
    private MonthWeekEventsView mLastClickedView;
    private float mClickedXLocation;
    private float mClickedYLocation;
    private boolean isMove;

    private HashMap<String, MonthWeekEventsView> mSelectedViewMap;

    private int mSelectedWeek;
    private int mFirstJulianDay;
    private int mQueryDays;

    private int mFirstDayOfWeek;
    private boolean mShowWeekNumber = false;
    private int mDaysPerWeek = 7;

    private ArrayList<ArrayList<Event>> mEventDayList = new ArrayList<ArrayList<Event>>();
    private ArrayList<Event> mEvents = null;

    private int mOrientation = Configuration.ORIENTATION_LANDSCAPE;

    private LinearLayout mContentLayout;

    private List<MonthWeekEventsView> mWeekList;

    private Calendar mCalendar;

    private HashMap<String, Vacation> mVacationMap = new HashMap<String, Vacation>();

    public KMonthByWeekContent(Context context, Calendar calendar, int mode, View parentView, int monthPosition) {
        mContext = context;
        mWeekHeight = (int)context.getResources().getDimension(R.dimen.line_height);
        mCalendar = calendar;
        mMode = mode;
        mMonthPosition = monthPosition;
        mParentView = parentView;
        mWeekList = new ArrayList<MonthWeekEventsView>();

        mFirstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
        mShowWeekNumber = Utils.getShowWeekNumber(mContext);
        mHomeTimeZone = Utils.getTimeZone(mContext, null);
        mOrientation = mContext.getResources().getConfiguration().orientation;

        mSelectedDay = new Time();
        mSelectedDay.setToNow();
        mShowAgendaWithMonth = Utils.getConfigBool(context, R.bool.show_agenda_with_month);

        ViewConfiguration vc = ViewConfiguration.get(context);
        mOnDownDelay = ViewConfiguration.getTapTimeout();
        mMovedPixelToCancel = vc.getScaledTouchSlop();
        mTotalClickDelay = mOnDownDelay + mOnTapDelay;

        mController = CalendarController.getInstance(context);

        mHomeTimeZone = Utils.getTimeZone(context, null);
        mSelectedDay.switchTimezone(mHomeTimeZone);
        mToday = new Time(mHomeTimeZone);
        mToday.setToNow();
        mTempTime = new Time(mHomeTimeZone);

        mContentLayout = (LinearLayout) mParentView.findViewById(R.id.month_content);
    }

    public LinearLayout getContentLayout() {
        return mContentLayout;
    }

    private int getLineCount(Calendar calendar, int daysPerWeek) {
        calendar.set(Calendar.DATE, 1);
        calendar.roll(Calendar.DATE, -1);
        int dayCount = calendar.get(Calendar.DATE);
        int firstDayOfWeek = Utils.getFirstDayOfWeekAsCalendar(mContext);
        if (DEBUG) {
            android.util.Log.d(TAG, "-=-=-= dayCount = " + dayCount);
            android.util.Log.d(TAG, "-=-=-= dayCount / daysPerWeek = " + dayCount / daysPerWeek);
            android.util.Log.d(TAG, "-=-=-= dayCount % daysPerWeek = " + dayCount % daysPerWeek);
            android.util.Log.d(TAG, "-=-=-= year : " + calendar.get(Calendar.YEAR)
                    + ", month : " + calendar.get(Calendar.MONTH));
        }

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        if (DEBUG)
            android.util.Log.d(TAG, "-=-=-= DAY_OF_WEEK = " + calendar.get(Calendar.DAY_OF_WEEK));

        int noFirstLineDayCount = dayCount - (daysPerWeek - calendar.get(Calendar.DAY_OF_WEEK) + firstDayOfWeek);
        //last +1 is first line
        int lineCount = noFirstLineDayCount / daysPerWeek + ((noFirstLineDayCount % daysPerWeek > 0) ? 1 : 0) + 1;
        return lineCount;
    }

    private void updateTimeZones() {
        mSelectedDay.timezone = mHomeTimeZone;
        mSelectedDay.normalize(true);
        mToday.timezone = mHomeTimeZone;
        mToday.setToNow();
        mTempTime.switchTimezone(mHomeTimeZone);
    }

    public void setSelectedDay(Time selectedTime) {
        Log.d(TAG, "-=-=-= setSelectedDay selectedTime = " + selectedTime);
        mSelectedDay.set(selectedTime);
        long millis = mSelectedDay.normalize(true);
        mSelectedWeek = Utils.getWeeksSinceEpochFromJulianDay(
                Time.getJulianDay(millis, mSelectedDay.gmtoff), Time.SUNDAY);
        Log.d(TAG, "-=-=-= setSelectedDay mSelectedWeek = "+mSelectedWeek);
//        refresh();
    }

    public void setEvents(int firstJulianDay, int numDays, ArrayList<Event> events) {
        mEvents = events;
        mFirstJulianDay = firstJulianDay;
        mQueryDays = numDays;
        // Create a new list, this is necessary since the weeks are referencing
        // pieces of the old list
        ArrayList<ArrayList<Event>> eventDayList = new ArrayList<ArrayList<Event>>();
        for (int i = 0; i < numDays; i++) {
            eventDayList.add(new ArrayList<Event>());
        }

        if (events == null || events.size() == 0) {
            mEventDayList = eventDayList;
            refresh();
            return;
        }

        for (Event event : events) {
            int startDay = event.startDay - mFirstJulianDay;
            int endDay = event.endDay - mFirstJulianDay + 1;
            if (startDay < numDays || endDay >= 0) {
                if (startDay < 0) {
                    startDay = 0;
                }
                if (startDay > numDays) {
                    continue;
                }
                if (endDay < 0) {
                    continue;
                }
                if (endDay > numDays) {
                    endDay = numDays;
                }
                for (int j = startDay; j < endDay; j++) {
                    eventDayList.get(j).add(event);
                }
            }
        }
        mEventDayList = eventDayList;
        refresh();
    }

    private void sendEventsToView(MonthWeekEventsView v) {
        if (mEventDayList.size() == 0) {
            v.setEvents(null, null);
            return;
        }
        int viewJulianDay = v.getFirstJulianDay();
        int start = viewJulianDay - mFirstJulianDay;
        int end = start + v.mNumDays;
        if (start < 0 || end > mEventDayList.size()) {
            v.setEvents(null, null);
            return;
        }
        v.setEvents(mEventDayList.subList(start, end), mEvents);
    }

    public void refresh() {
        updateTimeZones();

        mContentLayout.removeAllViews();

        Calendar calendar = mCalendar;
        Time time = new Time();
        time.set(calendar.getTime().getTime());

        int position = mMode == KMonthByWeekFragment.MONTH_MODE
                ? Utils.getWeeksSinceEpochFromJulianDay(Time.getJulianDay(time.toMillis(true), time.gmtoff),Time.SUNDAY)
                : mMonthPosition;
        if (DEBUG)
            Log.d(TAG, "-=-=-= refresh position = "+position);

        int lineCount = mMode == KMonthByWeekFragment.MONTH_MODE ? getLineCount(calendar, mDaysPerWeek)
                : 1;
		Log.i("wanghg", "refresh lineCount = " + lineCount);

        Calendar selectCalendar = Calendar.getInstance();
        selectCalendar.setTimeInMillis(mSelectedDay.toMillis(false));

        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTimeInMillis(mToday.toMillis(false));

        for (int i = 0; i < lineCount; i++) {
            int selectedDay = -1;
            MonthWeekEventsView v = new MonthWeekEventsView(mContext);
            if (mSelectedWeek == (position + i)) {
                selectedDay = mSelectedDay.weekDay;
            }

            if(mMode == KMonthByWeekFragment.MONTH_MODE) {
                if ((i + 1) == selectCalendar.get(Calendar.WEEK_OF_MONTH)) {
                    mClickedView = (MonthWeekEventsView) v;
                }
                else if (!Utils.SELECTED_DAY_IS_DEFAULT && isTheSameMonth() && (i + 1) == todayCalendar.get(Calendar.WEEK_OF_MONTH)) {
                    selectedDay = mToday.weekDay;
                    mClickedView = (MonthWeekEventsView) v;
                }
            } else {
                if (!Utils.SELECTED_DAY_IS_DEFAULT && isTheSameMonth() && (i + 1) == todayCalendar.get(Calendar.WEEK_OF_MONTH)) {
                    selectedDay = mToday.weekDay;
                }
                mClickedView = (MonthWeekEventsView) v;
            }

            HashMap<String, Integer> drawingParams = new HashMap<String, Integer>();
            drawingParams.put(SimpleWeekView.VIEW_PARAMS_HEIGHT, mWeekHeight);
            drawingParams.put(SimpleWeekView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
            drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_START, mFirstDayOfWeek);
            drawingParams.put(SimpleWeekView.VIEW_PARAMS_NUM_DAYS, mDaysPerWeek);
            drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK, position + i);
            drawingParams.put(SimpleWeekView.VIEW_PARAMS_FOCUS_MONTH, calendar.get(Calendar.MONTH));
            drawingParams.put(MonthWeekEventsView.VIEW_PARAMS_ORIENTATION, mOrientation);
            drawingParams.put(MonthWeekEventsView.VIEW_PARAMS_MODE, mMode);
            v.setWeekParams(drawingParams, mSelectedDay.timezone);

            v.setClickable(true);
            v.setOnTouchListener(this);
            mContentLayout.addView(v);
            mWeekList.add(i, v);
            sendEventsToView(v);
        }
    }

    public void setVacationMap(HashMap<String, Vacation> map) {
        mVacationMap = map;
    }

    public MonthWeekEventsView getWeekAt(int position) {
        return mWeekList.get(position);
    }

    protected void onDayTapped(Time day) {
        setDayParameters(day);
        Log.d(TAG,"-=-=-= onDayTapped day = " + day);
        if (VacationUtils.CONTENT_MODE == VacationUtils.VACATION_MODE) {
            mController.sendEvent(mContext, CalendarController.EventType.VACATION_GO_TO, day, day, -1,
                    CalendarController.ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null, null);
            return;
        }
        if (mShowAgendaWithMonth) {
            mController.sendEvent(mContext, CalendarController.EventType.GO_TO, day, day, -1,
                    CalendarController.ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null, null);
        } else {
            mController.sendEvent(mContext, CalendarController.EventType.GO_TO, day, day, -1,
                    CalendarController.ViewType.DETAIL,
                    CalendarController.EXTRA_GOTO_DATE
                            | CalendarController.EXTRA_GOTO_BACK_TO_PREVIOUS, null, null);
        }
    }

    private void setDayParameters(Time day) {
        day.timezone = mHomeTimeZone;
        Time currTime = new Time(mHomeTimeZone);
        currTime.set(mController.getTime());
        day.hour = currTime.hour;
        day.minute = currTime.minute;
        day.allDay = false;
        day.normalize(true);
    }

    public void setDefaultView(){
        mContentLayout.post(mDefaultClick);
    }

    private void setClickedDay(MonthWeekEventsView monthWeekEventsView,Time day) {
        if (mLastClickedView != null)
            return;
        if (day != null) {
            onDayTapped(day);
        }
        Log.d(TAG, "-=-=-= day.weekDay = " + day.weekDay);
//        monthWeekEventsView.setClickedDay(day.weekDay);
        mLastClickedView = monthWeekEventsView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!(v instanceof MonthWeekEventsView)) {
            return onTouch(v, event);
        }

        int action = event.getAction();
        Log.d(TAG, "-=-=-= onTouch action = " +action);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "-=-=-= onTouch ACTION_DOWN");
                isMove = false;
                mClickedXLocation = event.getX();
                mClickedYLocation = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "-=-=-= onTouch ACTION_UP");
                if (isMove)
                    break;

                if (mLastClickedView != null) {
                    clearClickedView(mLastClickedView);
                }
                Utils.mCanRefresh = true;
                mClickedView = (MonthWeekEventsView)v;
                mContentLayout.postDelayed(mDoClick, mOnDownDelay);
                break;
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "-=-=-= onTouch ACTION_SCROLL ACTION_CANCEL");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "-=-=-= onTouch ACTION_MOVE");
                if (Math.abs(event.getX() - mClickedXLocation) > mMovedPixelToCancel
                        || Math.abs(event.getY() - mClickedYLocation) > mMovedPixelToCancel) {
                    isMove = true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    private void clearClickedView(MonthWeekEventsView v) {
        mContentLayout.removeCallbacks(mDoClick);
        synchronized(v) {
            Log.d(TAG, "-=-=-= clearClickedView clearClickedDay");
            v.clearClickedDay();
            v = null;
        }
    }

    private final Runnable mDoClick = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "-=-=-= mDoClick run");
            if (mClickedView != null) {
                synchronized(mClickedView) {
                    Time day = mClickedView.getDayFromLocation(mClickedXLocation);
                    if (day != null) {
                        onDayTapped(day);
                    }
                    if(Utils.SELECTED_DAY_IS_DEFAULT) {
                        Utils.CLICKED_DAY = day.monthDay;
                    }
                    mSelectedDay = day;
                    Log.d(TAG, "-=-=-= mDoClick setClickedDay");
                    mClickedView.setClickedDay(mClickedXLocation);
                }
                mLastClickedView = mClickedView;
                mClickedView = null;
                mContentLayout.invalidate();
            }
        }
    };

    private boolean isTheSameMonth(){
        if(mSelectedDay==null || mToday==null){
            return false;
        }
        return mSelectedDay.year == mToday.year && mSelectedDay.month==mToday.month;
    }

    private final Runnable mDefaultClick = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "-=-=-= mDoClick run");
            if (mClickedView != null) {
                synchronized(mClickedView) {
                    if(!Utils.SELECTED_DAY_IS_DEFAULT && isTheSameMonth()
                            && (mMode!=KMonthByWeekFragment.WEEK_MODE || (mMode==KMonthByWeekFragment.WEEK_MODE && mSelectedDay.getWeekNumber()==mToday.getWeekNumber()))){
                        mSelectedDay = mToday;
                    }
                    mClickedView.setClickedDay(mSelectedDay.weekDay);
//                    onDayTapped(mSelectedDay);
                }
                mLastClickedView = mClickedView;
                mClickedView = null;
                mContentLayout.invalidate();
            }
        }
    };

}
