package com.ktouch.kcalendar.month;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ktouch.kcalendar.CalendarController;
import com.ktouch.kcalendar.Event;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
import com.ktouch.kcalendar.VacationUtils;
import com.ktouch.kcalendar.agenda.AgendaFragment;
import com.ktouch.kcalendar.agenda.AgendaListView;
import com.ktouch.kcalendar.contentprovider.KCalendarProvider;
import com.ktouch.kcalendar.vacation.Vacation;
//import com.ktouch.kcalendar.event.CreateEventDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhang-yi on 2016/4/29 0029.
 */
public class KMonthByWeekViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnTouchListener {
    private static final String TAG = "KMonthByWeekViewFragment";
    private static final boolean DEBUG = true;

    private static final String TAG_EVENT_DIALOG = "event_dialog";

    private TextView mMonthName;
    private AgendaFragment mAgendaFragment;

    private CalendarController mController;

    private FragmentTransaction mFt;

    private Context mContext;
    public int mMonthPosition;
    private int mMode;
    private long mTimeMillis;

    private Time mFirstDayOfMonth = new Time();
    private Time mFirstVisibleDay = new Time();

    private String[] mDayLabels;
    private int mSaturdayColor = 0;
    private int mSundayColor = 0;
    private int mDayNameColor = 0;

    private float mMinimumFlingVelocity;
    private int mCurrentMonthDisplayed;

    private KMonthByWeekContent mKMonthByWeekContent;

    protected Time mSelectedDay = new Time();
    protected Time mTempTime = new Time();

    protected boolean mShowWeekNumber = false;
    protected int mFirstDayOfWeek;
    protected int mDaysPerWeek = 7;

//    private CreateEventDialogFragment mEventDialog;

    private volatile boolean mShouldLoad = true;
    private CursorLoader mLoader;
    private CursorLoader mVacationLoader;

    protected static final int SCROLL_HYST_WEEKS = 2;
    // How long the GoTo fling animation should last
    protected static final int GOTO_SCROLL_DURATION = 500;
    // How long to wait after receiving an onScrollStateChanged notification
    // before acting on it
    protected static final int SCROLL_CHANGE_DELAY = 40;
    // The number of days to display in each week
    public static final int DAYS_PER_WEEK = 7;
    // The size of the month name displayed above the week list
    protected static final int MINI_MONTH_NAME_TEXT_SIZE = 18;
    public static int LIST_TOP_OFFSET = -1;  // so that the top line will be under the separator
    protected int WEEK_MIN_VISIBLE_HEIGHT = 12;
    protected int BOTTOM_BUFFER = 20;

    protected float mFriction = 1.0f;

    protected Handler mHandler;

    protected long mPreviousScrollPosition;
    protected boolean mIsScrollingUp = false;
    protected int mPreviousScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
    protected int mCurrentScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    /////////////////////////////

    private static final String WHERE_CALENDARS_VISIBLE = CalendarContract.Calendars.VISIBLE + "=1";
    private static final String INSTANCES_SORT_ORDER = CalendarContract.Instances.START_DAY + ","
            + CalendarContract.Instances.START_MINUTE + "," + CalendarContract.Instances.TITLE;
    protected static boolean mShowDetailsInMonth = false;

    protected float mMinimumTwoMonthFlingVelocity;
    protected boolean mHideDeclined;

    protected int mFirstLoadedJulianDay;
    protected int mLastLoadedJulianDay;

    private static final int WEEKS_BUFFER = 1;
    private static final int LOADER_DELAY = 200;
    private static final int LOADER_THROTTLE_DELAY = 500;

    private Uri mEventUri;
    private final Time mDesiredDay = new Time();

    private boolean mUserScrolled = false;

    private int mEventsLoadingDelay;
    private boolean mShowCalendarControls;
    private boolean mIsDetached;

    protected int mNumWeeks = 6;

    public KMonthByWeekViewFragment(Context context, int position, int mode) {//long initialTime,
        mContext = context;
        mMonthPosition = position;
        mMode = mode;
        mHandler = new Handler();
        mController = CalendarController.getInstance(context);
    }

    public KMonthByWeekViewFragment(Context context, long timeMillis) {//long initialTime,
        mContext = context;
        mTimeMillis = timeMillis;
        mMode = KMonthByWeekFragment.MONTH_MODE;
        mHandler = new Handler();
        mController = CalendarController.getInstance(context);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "-=-=-= onAttach mMonthPosition = " + mMonthPosition);
        super.onAttach(activity);

        String tz = Time.getCurrentTimezone();
        ViewConfiguration viewConfig = ViewConfiguration.get(activity);
        mMinimumFlingVelocity = viewConfig.getScaledMinimumFlingVelocity();

//        mSelectedDay.switchTimezone(tz);
//        mSelectedDay.normalize(true);
        mFirstDayOfMonth.timezone = tz;
        mFirstDayOfMonth.normalize(true);
        mFirstVisibleDay.timezone = tz;
        mFirstVisibleDay.normalize(true);
        mTempTime.timezone = tz;

        Resources res = activity.getResources();
        mSaturdayColor = res.getColor(R.color.month_saturday);
        mSundayColor = res.getColor(R.color.month_sunday);
        mDayNameColor = res.getColor(R.color.month_day_names_color);

        mTZUpdater.run();

//        if (mKMonthByWeekContent != null) {
//            Log.d(TAG, "-=-=-= onAttach mSelectedDay = " + mSelectedDay);
//            mKMonthByWeekContent.setSelectedDay(mSelectedDay);
//        }
        mIsDetached = false;

        mMinimumTwoMonthFlingVelocity = viewConfig.getScaledMaximumFlingVelocity() / 2;
        mShowCalendarControls = Utils.getConfigBool(activity, R.bool.show_calendar_controls);
        if (mShowCalendarControls) {
            mEventsLoadingDelay = res.getInteger(R.integer.calendar_controls_animation_time);
        }
        mShowDetailsInMonth = res.getBoolean(R.bool.show_details_in_month);
    }

    @Override
    public void onDetach() {
        mIsDetached = true;
        super.onDetach();
        if (mShowCalendarControls) {
            if (mKMonthByWeekContent != null) {
                LinearLayout contentLayout = mKMonthByWeekContent.getContentLayout();
                contentLayout.removeCallbacks(mLoadingRunnable);
            }
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "-=-=-= onResume mMonthPosition = " + mMonthPosition);
        super.onResume();

        setUpContent();
        if (mShowCalendarControls) {
            mKMonthByWeekContent.getContentLayout().postDelayed(mLoadingRunnable, mEventsLoadingDelay);
        } else {
            mLoader = (CursorLoader) getLoaderManager().initLoader(0, null, this);
            mVacationLoader = (CursorLoader) getLoaderManager().initLoader(1, null, this);
        }
//        setAgendaContent();
        doResumeUpdates();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "-=-=-= onPause mMonthPosition = " + mMonthPosition);
        super.onPause();
//        mController.deregisterEventHandler(R.id.month_content);
//        mAgendaFragment = null;
    }

    private Calendar mTodayCalendar;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "-=-=-= onActivityCreated mMonthPosition = " + mMonthPosition);
        super.onActivityCreated(savedInstanceState);
        mTodayCalendar = Calendar.getInstance();
        Time time = new Time();
        time.setToNow();
        mTodayCalendar.setTimeInMillis(time.toMillis(false));
//        setUpHeader();
        setUpContent();

        mMonthName = (TextView) getView().findViewById(R.id.month_name);

        SimpleWeekView child = (SimpleWeekView) mKMonthByWeekContent.getWeekAt(0);
        if (child == null) {
            return;
        }
        int julianDay = child.getFirstJulianDay();
        mFirstVisibleDay.setJulianDay(julianDay);
        // set the title to the month of the second week
        mTempTime.setJulianDay(julianDay + DAYS_PER_WEEK);
        setMonthDisplayed(mTempTime, true);


//        setAgendaContent();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "-=-=-= onCreateView mMonthPosition = " + mMonthPosition);
        View v = inflater.inflate(R.layout.kmonth_by_week,
                container, false);
        return v;
    }

    private Calendar getMonthByMonthPosition(int monthPosition) {
        if (DEBUG)
            Log.d(TAG, "-=-=-= getMonthByMonthPosition monthPosition = " + monthPosition);

        Calendar calendar = Calendar.getInstance();
        int year = monthPosition / 12 + Utils.YEAR_MIN;
        int month = ((monthPosition < 12) ? monthPosition : (monthPosition % 12));
        calendar.set(year, month, 1);
        return calendar;
    }

    private void setUpContent() {
        Log.d(TAG, "-=-=-= setUpContent mMonthPosition = " + mMonthPosition);

        Calendar calendar = null;
        if (mMode == KMonthByWeekFragment.MONTH_MODE) {
            calendar = getMonthByMonthPosition(mMonthPosition);
            if(Utils.SELECTED_DAY_IS_DEFAULT && Utils.CLICKED_DAY>calendar.getActualMaximum(Calendar.DAY_OF_MONTH)){
                Utils.CLICKED_DAY = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            calendar.set(Calendar.DAY_OF_MONTH, Utils.CLICKED_DAY);
        } else {
            Time time = new Time();
            time.setJulianDay(Utils.getJulianMondayFromWeeksSinceEpoch(mMonthPosition));
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time.toMillis(true));
        }

        if(!Utils.SELECTED_DAY_IS_DEFAULT && mTodayCalendar!=null && calendar.get(Calendar.YEAR)==mTodayCalendar.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH)==mTodayCalendar.get(Calendar.MONTH)
                && calendar.get(Calendar.WEEK_OF_MONTH)==mTodayCalendar.get(Calendar.WEEK_OF_MONTH)){
            calendar.setTimeInMillis(mTodayCalendar.getTimeInMillis());
        }

        mSelectedDay.set(calendar.getTimeInMillis());

//        if (mKMonthByWeekContent == null)
        mKMonthByWeekContent = new KMonthByWeekContent(mContext, calendar, mMode, getView(), mMonthPosition);

        mKMonthByWeekContent.setSelectedDay(mSelectedDay);
        mKMonthByWeekContent.refresh();


    }

    private void setAgendaContent() {
        Log.d(TAG, "-=-=-= setAgendaContent mMonthPosition = " + mMonthPosition);
        mFt = getChildFragmentManager().beginTransaction();
        Log.d(TAG, "-=-=-= setAgendaContent month = " + mSelectedDay.month);
        mAgendaFragment = new AgendaFragment(mSelectedDay.toMillis(true), false, AgendaListView.WINDOW_TYPE_MONTH);
        mFt.replace(R.id.agenda_content, mAgendaFragment);
        mController.registerEventHandler(R.id.month_content, (CalendarController.EventHandler) mAgendaFragment);
        mFt.commit();
        Log.d(TAG, "-=-=-= setAgendaContent id = " + mAgendaFragment.getId());
    }

//    public void setSelectedDay(Time selectedTime) {
//        Log.d(TAG, "-=-=-= setSelectedDay selectedTime = " + selectedTime);
//        mSelectedDay.set(selectedTime);
//        if (mKMonthByWeekContent != null)
//            mKMonthByWeekContent.setSelectedDay(mSelectedDay);
//    }

    private void doResumeUpdates() {
        mFirstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
        mShowWeekNumber = Utils.getShowWeekNumber(mContext);
        boolean prevHideDeclined = mHideDeclined;
        mHideDeclined = Utils.getHideDeclinedEvents(mContext);
        if (prevHideDeclined != mHideDeclined && mLoader != null) {
            mLoader.setSelection(updateWhere());
        }
        mDaysPerWeek = Utils.getDaysPerWeek(mContext);
        mTZUpdater.run();
        mTodayUpdater.run();
    }

    private void setMonthDisplayed(Time time, boolean updateHighlight) {
        CharSequence oldMonth = mMonthName.getText();
        mMonthName.setText(Utils.formatMonthYear(mContext, time));
        mMonthName.invalidate();
        if (!TextUtils.equals(oldMonth, mMonthName.getText())) {
            mMonthName.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        }
        mCurrentMonthDisplayed = time.month;
    }

//    private Handler mEventDialogHandler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            final FragmentManager manager = getFragmentManager();
//            if (manager != null) {
//                Time day = (Time) msg.obj;
//                mEventDialog = new CreateEventDialogFragment(day);
//                mEventDialog.show(manager, TAG_EVENT_DIALOG);
//            }
//        }
//    };

    private final Runnable mTZUpdater = new Runnable() {
        @Override
        public void run() {
            String tz = Utils.getTimeZone(mContext, mTZUpdater);
            mSelectedDay.timezone = tz;
            mSelectedDay.normalize(true);
            mTempTime.timezone = tz;
            mFirstDayOfMonth.timezone = tz;
            mFirstDayOfMonth.normalize(true);
            mFirstVisibleDay.timezone = tz;
            mFirstVisibleDay.normalize(true);

            if(mKMonthByWeekContent != null)
                mKMonthByWeekContent.refresh();
        }
    };

    private final Runnable mUpdateLoader = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                if (!mShouldLoad || mLoader == null) {
                    return;
                }
                // Stop any previous loads while we update the uri
                stopLoader();

                // Start the loader again
                mEventUri = updateUri();

                mLoader.setUri(mEventUri);
                mLoader.startLoading();
                mLoader.onContentChanged();
            }
        }
    };

    Runnable mLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsDetached) {
                mLoader = (CursorLoader) getLoaderManager().initLoader(0, null,
                        KMonthByWeekViewFragment.this);
            }
        }
    };

    private Uri updateUri() {
        SimpleWeekView child = (SimpleWeekView) mKMonthByWeekContent.getWeekAt(0);

        if (child != null) {
            int julianDay = child.getFirstJulianDay();
            mFirstLoadedJulianDay = julianDay;
        }
        // -1 to ensure we get all day events from any time zone
        mTempTime.setJulianDay(mFirstLoadedJulianDay - 1);
        long start = mTempTime.toMillis(true);
        mLastLoadedJulianDay = mFirstLoadedJulianDay + (mNumWeeks + 2 * WEEKS_BUFFER) * 7;
        // +1 to ensure we get all day events from any time zone
        mTempTime.setJulianDay(mLastLoadedJulianDay + 1);
        long end = mTempTime.toMillis(true);

        // Create a new uri with the updated times
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, start);
        ContentUris.appendId(builder, end);
        return builder.build();
    }

    private void updateLoadedDays() {
        List<String> pathSegments = mEventUri.getPathSegments();
        int size = pathSegments.size();
        if (size <= 2) {
            return;
        }
        long first = Long.parseLong(pathSegments.get(size - 2));
        long last = Long.parseLong(pathSegments.get(size - 1));
        mTempTime.set(first);
        mFirstLoadedJulianDay = Time.getJulianDay(first, mTempTime.gmtoff);
        mTempTime.set(last);
        mLastLoadedJulianDay = Time.getJulianDay(last, mTempTime.gmtoff);
    }

    protected String updateWhere() {
        // TODO fix selection/selection args after b/3206641 is fixed
        String where = WHERE_CALENDARS_VISIBLE;
        if (mHideDeclined || !mShowDetailsInMonth) {
            where += " AND " + CalendarContract.Instances.SELF_ATTENDEE_STATUS + "!="
                    + CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED;
        }
        return where;
    }

    private void stopLoader() {
        synchronized (mUpdateLoader) {
            mHandler.removeCallbacks(mUpdateLoader);
            if (mLoader != null) {
                mLoader.stopLoading();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "-=-=-= onCreateLoader id = " + id + ", mMonthPosition = "+mMonthPosition);
        CursorLoader loader;
        if (id == 1) {
            Calendar calendar = getMonthByMonthPosition(mMonthPosition);
            String where = KCalendarProvider.Columns.YEAR + "=" + calendar.get(Calendar.YEAR)
                    + " AND " + KCalendarProvider.Columns.MONTH + "=" + calendar.get(Calendar.MONTH);
            if (VacationUtils.CONTENT_MODE == VacationUtils.EVENTS_MODE) {
                where = KCalendarProvider.Columns.STATE + " in (1,2)";
            } else {
                where = KCalendarProvider.Columns.STATE + "=3";
            }
            Log.d(TAG, "-=-=-= onCreateLoader where = " + where);
            loader = new CursorLoader(getActivity(), KCalendarProvider.Columns.CONTENT_URI,
                    KCalendarProvider.Columns.allColumns,
                    where, null, null);
            return loader;
        }
        synchronized (mUpdateLoader) {
            mFirstLoadedJulianDay =
                    Time.getJulianDay(mSelectedDay.toMillis(true), mSelectedDay.gmtoff)
                            - (mNumWeeks * 7 / 2);
            mEventUri = updateUri();
            String where = updateWhere();

            loader = new CursorLoader(
                    getActivity(), mEventUri, Event.EVENT_PROJECTION, where,
                    null /* WHERE_CALENDARS_SELECTED_ARGS */, INSTANCES_SORT_ORDER);
            loader.setUpdateThrottle(LOADER_THROTTLE_DELAY);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 1 && VacationUtils.mVacationMap==null) {
            Log.d(TAG, "-=-=-= onLoadFinished mMonthPosition = "+mMonthPosition);
            VacationUtils.mVacationMap = VacationUtils.initVacationMap(data);
            return;
        }
        synchronized (mUpdateLoader) {
            CursorLoader cLoader = (CursorLoader) loader;
            if (mEventUri == null) {
                mEventUri = cLoader.getUri();
                updateLoadedDays();
            }
            if (cLoader.getUri().compareTo(mEventUri) != 0) {
                // We've started a new query since this loader ran so ignore the
                // result
                return;
            }
            ArrayList<Event> events = new ArrayList<Event>();
            Event.buildEventsFromCursor(
                    events, data, mContext, mFirstLoadedJulianDay, mLastLoadedJulianDay);
            Log.d(TAG, "-=-=-= onLoadFinished events = " + events.size() + ", mMonthPosition = "+mMonthPosition);
            mKMonthByWeekContent.setEvents(mFirstLoadedJulianDay,
                    mLastLoadedJulianDay - mFirstLoadedJulianDay + 1, events);
            mKMonthByWeekContent.setDefaultView();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mDesiredDay.setToNow();
        return false;
        // TODO post a cleanup to push us back onto the grid if something went
        // wrong in a scroll such as the user stopping the view but not
        // scrolling
    }

    protected Runnable mTodayUpdater = new Runnable() {
        @Override
        public void run() {
            Time midnight = new Time(mFirstVisibleDay.timezone);
            midnight.setToNow();
            long currentMillis = midnight.toMillis(true);

            midnight.hour = 0;
            midnight.minute = 0;
            midnight.second = 0;
            midnight.monthDay++;
            long millisToMidnight = midnight.normalize(true) - currentMillis;
            mHandler.postDelayed(this, millisToMidnight);

            if (mKMonthByWeekContent != null) {
                mKMonthByWeekContent.refresh();
            }
        }
    };
}
