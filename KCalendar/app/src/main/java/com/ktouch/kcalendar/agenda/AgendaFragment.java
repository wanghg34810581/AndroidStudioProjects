/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ktouch.kcalendar.agenda;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.CalendarContract.Attendees;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ktouch.kcalendar.CalendarController;
import com.ktouch.kcalendar.CalendarController.EventInfo;
import com.ktouch.kcalendar.CalendarController.EventType;
import com.ktouch.kcalendar.CalendarController.ViewType;
import com.ktouch.kcalendar.GeneralPreferences;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.CalendarUtils.ShareEventListener;
import com.ktouch.kcalendar.StickyHeaderListView;
import com.ktouch.kcalendar.Utils;

import java.util.Date;

public class AgendaFragment extends Fragment implements CalendarController.EventHandler,
        OnScrollListener {

    private static final String TAG = AgendaFragment.class.getSimpleName();
    private static boolean DEBUG = false;

    protected static final String BUNDLE_KEY_RESTORE_TIME = "key_restore_time";
    protected static final String BUNDLE_KEY_RESTORE_INSTANCE_ID = "key_restore_instance_id";

    private AgendaListView mAgendaListView;
    private Activity mActivity;
    private final Time mTime;
    private String mTimeZone;
    private final long mInitialTimeMillis;
    private boolean mShowEventDetailsWithAgenda;
    private CalendarController mController;
    //    private EventInfoFragment mEventFragment;
    private String mQuery;
    private boolean mUsedForSearch = false;
    private boolean mIsTabletConfig;
    private EventInfo mOnAttachedInfo = null;
    private boolean mOnAttachAllDay = false;
    private AgendaWindowAdapter mAdapter = null;
    private boolean mForceReplace = true;
    private long mLastShownEventId = -1;
    private boolean mLaunchedInShareMode;
    private boolean mShouldSelectSingleEvent;
    private ShareEventListener mShareEventListener;

    private TextView mMonthViewNoDataDefault;
    private TextView mWeekViewNoDataDefault;
    private TextView mYearViewNoDataDefault;
    private LinearLayout mNoDataLayout;
    private View mTopView;

    /*K-touch_2016-5-3_zhuyansong_Different agenda with different window type_START*/
    private int mWindowType;
    /*K-touch_2016-5-3_zhuyansong_Different agenda with different window type_END*/

    // Tracks the time of the top visible view in order to send UPDATE_TITLE messages to the action
    // bar.
    int mJulianDayOnTop = -1;

    private final Runnable mTZUpdater = new Runnable() {
        @Override
        public void run() {
            mTimeZone = Utils.getTimeZone(getActivity(), this);
            mTime.switchTimezone(mTimeZone);
        }
    };

    public AgendaFragment() {
        this(0, false, false);
    }

    public AgendaFragment(long timeMillis, boolean usedForSearch) {
        this(0, usedForSearch, false);
    }

    // timeMillis - time of first event to show
    // usedForSearch - indicates if this fragment is used in the search fragment
    // inShareMode - indicates whether the fragment was started to share calendar events
    public AgendaFragment(long timeMillis, boolean usedForSearch, boolean inShareMode) {
        mInitialTimeMillis = timeMillis;
        mTime = new Time();
        mLastHandledEventTime = new Time();

        if (mInitialTimeMillis == 0) {
            mTime.setToNow();
        } else {
            mTime.set(mInitialTimeMillis);
        }
        mLastHandledEventTime.set(mTime);
        mUsedForSearch = usedForSearch;
        mLaunchedInShareMode = inShareMode;
    }

    /*K-touch_2016-5-3_zhuyansong_Different agenda with different window type_START*/
    public AgendaFragment(long timeMillis, boolean usedForSearch, int windowType) {
        this(timeMillis, usedForSearch);
        mWindowType = windowType;
    }
    /*K-touch_2016-5-3_zhuyansong_Different agenda with different window type_END*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTimeZone = Utils.getTimeZone(activity, mTZUpdater);
        mTime.switchTimezone(mTimeZone);
        mActivity = activity;
        if (mOnAttachedInfo != null) {
            showEventInfo(mOnAttachedInfo, mOnAttachAllDay, true);
            mOnAttachedInfo = null;
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mController = CalendarController.getInstance(mActivity);
        mShowEventDetailsWithAgenda =
                Utils.getConfigBool(mActivity, R.bool.show_event_details_with_agenda);
        mIsTabletConfig =
                Utils.getConfigBool(mActivity, R.bool.tablet_config);
        if (icicle != null) {
            long prevTime = icicle.getLong(BUNDLE_KEY_RESTORE_TIME, -1);
            if (prevTime != -1) {
                mTime.set(prevTime);
                if (DEBUG) {
                    Log.d(TAG, "Restoring time to " + mTime.toString());
                }
            }
        }
    }

    public void setAgendaDefaultVisible(boolean visible) {
        if (mMonthViewNoDataDefault == null || mWeekViewNoDataDefault == null
                || mYearViewNoDataDefault == null || mTopView == null) {
            return;
        }
        View view = null;
        switch (mAgendaListView.getWindowType()) {
            case AgendaListView.WINDOW_TYPE_MONTH:
                view = mMonthViewNoDataDefault;
                mWeekViewNoDataDefault.setVisibility(View.INVISIBLE);
                mYearViewNoDataDefault.setVisibility(View.INVISIBLE);
                mTopView.setVisibility(View.GONE);
                break;
            case AgendaListView.WINDOW_TYPE_WEEK:
                view = mWeekViewNoDataDefault;
                mMonthViewNoDataDefault.setVisibility(View.INVISIBLE);
                mYearViewNoDataDefault.setVisibility(View.INVISIBLE);
                mTopView.setVisibility(View.GONE);
                break;
            case AgendaListView.WINDOW_TYPE_YEAR:
                if (visible) {
                    mNoDataLayout.setVisibility(View.VISIBLE);
                    mMonthViewNoDataDefault.setVisibility(View.INVISIBLE);
                    mWeekViewNoDataDefault.setVisibility(View.INVISIBLE);
                    mYearViewNoDataDefault.setVisibility(View.VISIBLE);
                    mTopView.setVisibility(View.VISIBLE);
                } else {
                    mNoDataLayout.setVisibility(View.INVISIBLE);
                }
                return;
            case AgendaListView.WINDOW_TYPE_SEARCH:
                mNoDataLayout.setVisibility(View.INVISIBLE);
                return;
        }
        mNoDataLayout.setVisibility(View.VISIBLE);
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    DataSetObserver mListDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            if (mAdapter.getCount() <= 1) {
                setAgendaDefaultVisible(true);
            } else {
                setAgendaDefaultVisible(false);
            }
            super.onChanged();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int screenWidth = mActivity.getResources().getDisplayMetrics().widthPixels;
        View v = inflater.inflate(R.layout.agenda_fragment, null);

        mMonthViewNoDataDefault = (TextView) v.findViewById(R.id.month_view_no_data_text);
        mWeekViewNoDataDefault = (TextView) v.findViewById(R.id.week_view_no_data_text);
        mYearViewNoDataDefault = (TextView) v.findViewById(R.id.year_view_no_data_text);
        mTopView = v.findViewById(R.id.top_view);
        mNoDataLayout = (LinearLayout) v.findViewById(R.id.agenda_default);

        mAgendaListView = (AgendaListView) v.findViewById(R.id.agenda_events_list);
        mAgendaListView.setClickable(true);
        /*K-touch_2016-5-3_zhuyansong_Different agenda with different window type_START*/
        mAgendaListView.setWindowType(mWindowType);
        /*K-touch_2016-5-3_zhuyansong_Different agenda with different window type_END*/

        if (savedInstanceState != null) {
            long instanceId = savedInstanceState.getLong(BUNDLE_KEY_RESTORE_INSTANCE_ID, -1);
            if (instanceId != -1) {
                mAgendaListView.setSelectedInstanceId(instanceId);
            }
        }

        View eventView = v.findViewById(R.id.agenda_event_info);
        if (!mShowEventDetailsWithAgenda) {
            eventView.setVisibility(View.GONE);
        }

        View topListView;
        // Set adapter & HeaderIndexer for StickyHeaderListView
        StickyHeaderListView lv =
                (StickyHeaderListView) v.findViewById(R.id.agenda_sticky_header_list);
        if (lv != null) {
            Adapter a = mAgendaListView.getAdapter();
            lv.setAdapter(a);

            if (a instanceof HeaderViewListAdapter) {
                mAdapter = (AgendaWindowAdapter) ((HeaderViewListAdapter) a).getWrappedAdapter();
                if (mLaunchedInShareMode) {
                    mAdapter.launchInShareMode(true, mShouldSelectSingleEvent);
                    mAgendaListView.launchInShareMode(true, mShouldSelectSingleEvent);
                    if (mShareEventListener != null) {
                        mAgendaListView.setShareEventListener(mShareEventListener);
                    }
                }
                lv.setIndexer(mAdapter);
                lv.setHeaderHeightListener(mAdapter);

            } else if (a instanceof AgendaWindowAdapter) {
                mAdapter = (AgendaWindowAdapter) a;
                lv.setIndexer(mAdapter);
                lv.setHeaderHeightListener(mAdapter);

            } else {
                Log.wtf(TAG, "Cannot find HeaderIndexer for StickyHeaderListView");
            }

            // Set scroll listener so that the date on the ActionBar can be set while
            // the user scrolls the view
            lv.setOnScrollListener(this);
            lv.setHeaderSeparator(getResources().getColor(R.color.agenda_list_separator_color), 1);
            topListView = lv;
        } else {
            topListView = mAgendaListView;
        }

        // Since using weight for sizing the two panes of the agenda fragment causes the whole
        // fragment to re-measure when the sticky header is replaced, calculate the weighted
        // size of each pane here and set it

        if (!mShowEventDetailsWithAgenda) {
            ViewGroup.LayoutParams params = topListView.getLayoutParams();
            params.width = screenWidth;
            topListView.setLayoutParams(params);
        } else {
            ViewGroup.LayoutParams listParams = topListView.getLayoutParams();
            listParams.width = screenWidth * 4 / 10;
            topListView.setLayoutParams(listParams);
            ViewGroup.LayoutParams detailsParams = eventView.getLayoutParams();
            detailsParams.width = screenWidth - listParams.width;
            eventView.setLayoutParams(detailsParams);
        }
        return v;
    }

    // configure share mode launch options
    public void setShareModeOptions(ShareEventListener listener, boolean selectSingleEvent) {
        mShareEventListener = listener;
        mShouldSelectSingleEvent = selectSingleEvent;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            Log.v(TAG, "OnResume to " + mTime.toString());
        }

        SharedPreferences prefs = GeneralPreferences.getSharedPreferences(
                getActivity());
        boolean hideDeclined = prefs.getBoolean(
                GeneralPreferences.KEY_HIDE_DECLINED, false);

        mAgendaListView.setHideDeclinedEvents(hideDeclined);
        if (mLastHandledEventId != -1) {
            mAgendaListView.goTo(mLastHandledEventTime, mLastHandledEventId, mQuery, true, false);
            mLastHandledEventTime = null;
            mLastHandledEventId = -1;
        } else {
            mAgendaListView.goTo(mTime, -1, mQuery, true, false);
        }
        mAgendaListView.onResume();

        mAdapter.registerDataSetObserver(mListDataObserver);
//        // Register for Intent broadcasts
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_TIME_CHANGED);
//        filter.addAction(Intent.ACTION_DATE_CHANGED);
//        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
//        registerReceiver(mIntentReceiver, filter);
//
//        mContentResolver.registerContentObserver(Events.CONTENT_URI, true, mObserver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAgendaListView == null) {
            return;
        }
        if (mShowEventDetailsWithAgenda) {
            long timeToSave;
            if (mLastHandledEventTime != null) {
                timeToSave = mLastHandledEventTime.toMillis(true);
                mTime.set(mLastHandledEventTime);
            } else {
                timeToSave = System.currentTimeMillis();
                mTime.set(timeToSave);
            }
            outState.putLong(BUNDLE_KEY_RESTORE_TIME, timeToSave);
            mController.setTime(timeToSave);
        } else {
            AgendaWindowAdapter.AgendaItem item = mAgendaListView.getFirstVisibleAgendaItem();
            if (item != null) {
                long firstVisibleTime = mAgendaListView.getFirstVisibleTime(item);
                if (firstVisibleTime > 0) {
                    mTime.set(firstVisibleTime);
                    mController.setTime(firstVisibleTime);
                    outState.putLong(BUNDLE_KEY_RESTORE_TIME, firstVisibleTime);
                }
                // Tell AllInOne the event id of the first visible event in the list. The id will be
                // used in the GOTO when AllInOne is restored so that Agenda Fragment can select a
                // specific event and not just the time.
                mLastShownEventId = item.id;
            }
        }
        if (DEBUG) {
            Log.v(TAG, "onSaveInstanceState " + mTime.toString());
        }

        long selectedInstance = mAgendaListView.getSelectedInstanceId();
        if (selectedInstance >= 0) {
            outState.putLong(BUNDLE_KEY_RESTORE_INSTANCE_ID, selectedInstance);
        }
    }

    /**
     * This cleans up the event info fragment since the FragmentManager doesn't
     * handle nested fragments. Without this, the action bar buttons added by
     * the info fragment can come back on a rotation.
     *
     * @param fragmentManager
     */
    public void removeFragments(FragmentManager fragmentManager) {
        if (getActivity().isFinishing()) {
            return;
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment f = fragmentManager.findFragmentById(R.id.agenda_event_info);
        if (f != null) {
            ft.remove(f);
        }
        ft.commit();
    }

    @Override
    public void onPause() {
        super.onPause();

        mAgendaListView.onPause();
        mAdapter.unregisterDataSetObserver(mListDataObserver);

//        mContentResolver.unregisterContentObserver(mObserver);
//        unregisterReceiver(mIntentReceiver);

        // Record Agenda View as the (new) default detailed view.
//        Utils.setDefaultView(this, CalendarApplication.AGENDA_VIEW_ID);
    }

    private void goTo(EventInfo event, boolean animate) {
		Log.i("wanghg", "AgendaFragment goTo");
        if (event.selectedTime != null) {
            mTime.set(event.selectedTime);
        } else if (event.startTime != null) {
            mTime.set(event.startTime);
        }
        Log.d(TAG, "-=-=-= goTo mAgendaListView = " + mAgendaListView);
        if (mAgendaListView == null) {
            // The view hasn't been set yet. Just save the time and use it
            // later.
            return;
        }
        Log.d(TAG, "-=-=-= goTo mTime = " + mTime);
        mAgendaListView.goTo(mTime, event.id, mQuery, false,
                ((event.extraLong & CalendarController.EXTRA_GOTO_TODAY) != 0 &&
                        mShowEventDetailsWithAgenda) ? true : false);
        AgendaAdapter.ViewHolder vh = mAgendaListView.getSelectedViewHolder();
        // Make sure that on the first time the event info is shown to recreate it
        Log.d(TAG, "selected viewholder is null: " + (vh == null));
        showEventInfo(event, vh != null ? vh.allDay : false, mForceReplace);
        mForceReplace = false;
    }

    private void search(String query, Time time) {
        mQuery = query;
        if (time != null) {
            mTime.set(time);
        }
        if (mAgendaListView == null) {
            // The view hasn't been set yet. Just return.
            return;
        }
        mAgendaListView.goTo(time, -1, mQuery, true, false);
    }

    @Override
    public void eventsChanged() {
        if (mAgendaListView != null) {
            mAgendaListView.refresh(true);
        }
    }

    @Override
    public long getSupportedEventTypes() {
        return EventType.GO_TO | EventType.EVENTS_CHANGED | ((mUsedForSearch) ? EventType.SEARCH : 0);
    }

    private long mLastHandledEventId = -1;
    private Time mLastHandledEventTime = null;

    @Override
    public void handleEvent(EventInfo event) {
    Log.i("wanghg", "handleEvent  7");
        if (event.eventType == EventType.GO_TO) {
            // TODO support a range of time
            // TODO support event_id
            // TODO figure out the animate bit
            mLastHandledEventId = event.id;
            mLastHandledEventTime =
                    (event.selectedTime != null) ? event.selectedTime : event.startTime;
            Log.d(TAG, "-=-=-= handleEvent mLastHandledEventTime = " + mLastHandledEventTime);
            goTo(event, true);
        } else if (event.eventType == EventType.SEARCH) {
            search(event.query, event.startTime);
        } else if (event.eventType == EventType.EVENTS_CHANGED) {
            eventsChanged();
        }
    }

    public long getLastShowEventId() {
        return mLastShownEventId;
    }

    // Shows the selected event in the Agenda view
    private void showEventInfo(EventInfo event, boolean allDay, boolean replaceFragment) {

        // Ignore unknown events
        if (event.id == -1) {
            Log.e(TAG, "showEventInfo, event ID = " + event.id);
            return;
        }

        mLastShownEventId = event.id;

        // Create a fragment to show the event to the side of the agenda list
        if (mShowEventDetailsWithAgenda) {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager == null) {
                // Got a goto event before the fragment finished attaching,
                // stash the event and handle it later.
                mOnAttachedInfo = event;
                mOnAttachAllDay = allDay;
                return;
            }
            FragmentTransaction ft = fragmentManager.beginTransaction();

            if (allDay) {
                event.startTime.timezone = Time.TIMEZONE_UTC;
                event.endTime.timezone = Time.TIMEZONE_UTC;
            }

            if (DEBUG) {
                Log.d(TAG, "***");
                Log.d(TAG, "showEventInfo: start: " + new Date(event.startTime.toMillis(true)));
                Log.d(TAG, "showEventInfo: end: " + new Date(event.endTime.toMillis(true)));
                Log.d(TAG, "showEventInfo: all day: " + allDay);
                Log.d(TAG, "***");
            }

            long startMillis = event.startTime.toMillis(true);
            long endMillis = event.endTime.toMillis(true);
//            EventInfoFragment fOld =
//                    (EventInfoFragment)fragmentManager.findFragmentById(R.id.agenda_event_info);
//            if (fOld == null || replaceFragment || fOld.getStartMillis() != startMillis ||
//                    fOld.getEndMillis() != endMillis || fOld.getEventId() != event.id) {
//                mEventFragment = new EventInfoFragment(mActivity, event.id,
//                        startMillis, endMillis,
//                        Attendees.ATTENDEE_STATUS_NONE, false,
//                        EventInfoFragment.DIALOG_WINDOW_STYLE, null);
//                ft.replace(R.id.agenda_event_info, mEventFragment);
//                ft.commit();
//            } else {
//                fOld.reloadEvents();
//            }
        }
    }

    // OnScrollListener implementation to update the date on the pull-down menu of the app

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Save scroll state so that the adapter can stop the scroll when the
        // agenda list is fling state and it needs to set the agenda list to a new position
        Log.d(TAG, "-=-=-= onScrollStateChanged");
        if (mAdapter != null) {
            mAdapter.setScrollState(scrollState);
        }
    }

    // Gets the time of the first visible view. If it is a new time, send a message to update
    // the time on the ActionBar
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
//        int julianDay = mAgendaListView.getJulianDayFromPosition(firstVisibleItem
//                - mAgendaListView.getHeaderViewsCount());
//        // On error - leave the old view
//        if (julianDay == 0) {
//            return;
//        }
//        // If the day changed, update the ActionBar
//        if (mJulianDayOnTop != julianDay) {
//            mJulianDayOnTop = julianDay;
//            Time t = new Time(mTimeZone);
//            t.setJulianDay(mJulianDayOnTop);
//            mController.setTime(t.toMillis(true));
//            // Cannot sent a message that eventually may change the layout of the views
//            // so instead post a runnable that will run when the layout is done
//            if (!mIsTabletConfig) {
//                view.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Time t = new Time(mTimeZone);
//                        t.setJulianDay(mJulianDayOnTop);
//                        Log.d(TAG, "-=-=-= onScroll UPDATE_TITLE");
//                        mController.sendEvent(this, EventType.UPDATE_TITLE, t, t, null, -1,
//                                ViewType.CURRENT, 0, null, null);
//                    }
//                });
//            }
//        }
    }
}
