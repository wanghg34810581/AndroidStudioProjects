/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.ktouch.kcalendar;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;

import com.ktouch.kcalendar.CalendarController.EventInfo;
import com.ktouch.kcalendar.CalendarController.EventType;
import com.ktouch.kcalendar.CalendarController.ViewType;
import com.ktouch.kcalendar.agenda.AgendaFragment;
import com.ktouch.kcalendar.agenda.AgendaListView;
import com.ktouch.kcalendar.agenda.AgendaRecentAdapter;
import com.ktouch.kcalendar.event.EditEventActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.provider.CalendarContract.EXTRA_EVENT_ALL_DAY;
import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;

public class MySearchActivity extends Activity implements CalendarController.EventHandler,
        SearchView.OnQueryTextListener {

    private static final String TAG = MySearchActivity.class.getSimpleName();

    private static final boolean DEBUG = true;

    private static final int HANDLER_KEY = 0;

    protected static final String BUNDLE_KEY_RESTORE_TIME = "key_restore_time";

    protected static final String BUNDLE_KEY_RESTORE_SEARCH_QUERY =
        "key_restore_search_query";

    // display event details to the side of the event list
   private boolean mShowEventDetailsWithAgenda;
   private static boolean mIsMultipane;

    private CalendarController mController;

//    private EventInfoFragment mEventInfoFragment;

    private long mCurrentEventId = -1;

    private String mQuery;

    private SearchView mSearchView;

    private DeleteEventHelper mDeleteEventHelper;

    private Handler mHandler;
    private BroadcastReceiver mTimeChangesReceiver;
    private ContentResolver mContentResolver;

    private EditText searchBox;
    private ListView mListView;
    private ViewGroup defaultResultLayout;
    private ViewGroup searchResultLayout;
    private AgendaRecentAdapter adapter;
    private ArrayList<Map<String, Object>> defaultAgendas = new ArrayList<>();

    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            eventsChanged();
        }
    };

    // runs when a timezone was changed and updates the today icon
    private final Runnable mTimeChangesUpdater = new Runnable() {
        @Override
        public void run() {
            Utils.setMidnightUpdater(mHandler, mTimeChangesUpdater,
                    Utils.getTimeZone(MySearchActivity.this, mTimeChangesUpdater));
            MySearchActivity.this.invalidateOptionsMenu();
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String query = s.toString();
            if (query == null || "".equals(query)) {
                searchResultLayout.setVisibility(View.GONE);
                defaultResultLayout.setVisibility(View.VISIBLE);
                return;
            } else {
                searchResultLayout.setVisibility(View.VISIBLE);
                defaultResultLayout.setVisibility(View.GONE);
            }
            mQuery = query;
            mController.sendEvent(MySearchActivity.this, EventType.SEARCH, null, null, -1, ViewType.CURRENT, 0, query,
                    getComponentName());
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // This needs to be created before setContentView
        mController = CalendarController.getInstance(this);
        mHandler = new Handler();

        mIsMultipane = Utils.getConfigBool(this, R.bool.multiple_pane_config);
        mShowEventDetailsWithAgenda =
            Utils.getConfigBool(this, R.bool.show_event_details_with_agenda);

        setContentView(R.layout.my_search);
        searchBox = (EditText) findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(mTextWatcher);
        mListView = (ListView) findViewById(R.id.default_results);
        defaultResultLayout = (ViewGroup) findViewById(R.id.default_results_layout);
        searchResultLayout = (ViewGroup) findViewById(R.id.search_results);

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        mContentResolver = getContentResolver();

        /*
        if (mIsMultipane) {
            getActionBar().setDisplayOptions(
                    ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        } else {
            getActionBar().setDisplayOptions(0,
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
        }
        */

        // Must be the first to register because this activity can modify the
        // list of event handlers in it's handle method. This affects who the
        // rest of the handlers the controller dispatches to are.
        //mController.registerEventHandler(HANDLER_KEY, this);

        mDeleteEventHelper = new DeleteEventHelper(this, this,
                false /* don't exit when done */);

        long millis = 0;
        if (icicle != null) {
            // Returns 0 if key not found
            millis = icicle.getLong(BUNDLE_KEY_RESTORE_TIME);
            if (DEBUG) {
                Log.v(TAG, "Restore value from icicle: " + millis);
            }
        }
        if (millis == 0) {
            // Didn't find a time in the bundle, look in intent or current time
            millis = Utils.timeFromIntentInMillis(getIntent());
        }

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query;
            if (icicle != null && icicle.containsKey(BUNDLE_KEY_RESTORE_SEARCH_QUERY)) {
                query = icicle.getString(BUNDLE_KEY_RESTORE_SEARCH_QUERY);
            } else {
                query = intent.getStringExtra(SearchManager.QUERY);
            }
            if ("TARDIS".equalsIgnoreCase(query)) {
                Utils.tardis();
            }
            initFragments(millis, query);
        }
    }

    public void onBackPressed(View view) {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mController.deregisterAllEventHandlers();
        CalendarController.removeInstance(this);
    }

    private void initFragments(long timeMillis, String query) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        AgendaFragment searchResultsFragment = new AgendaFragment(timeMillis, true, AgendaListView.WINDOW_TYPE_SEARCH);
        ft.replace(R.id.search_results, searchResultsFragment);
        mController.registerEventHandler(R.id.search_results, searchResultsFragment);

        ft.commit();
        //Time t = new Time();
        //t.set(timeMillis);
        //search(query, t);
    }

    private void showEventInfo(EventInfo event) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI, event.id);
        intent.setData(eventUri);
        intent.putExtra(AllInOneActivity.BUNDLE_KEY_EVENT_ID, event.id);
        intent.setClass(this, EditEventActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_EVENT_BEGIN_TIME, event.startTime.toMillis(false));
        intent.putExtra(EXTRA_EVENT_END_TIME, event.endTime.toMillis(false));
        intent.putExtra(EXTRA_EVENT_ALL_DAY, event.isAllDay());
        intent.putExtra(Events.TITLE, event.eventTitle);
        intent.putExtra(Events.CALENDAR_ID, event.calendarId);
        startActivity(intent);
        Log.v(TAG, "Binzo. showEventInfo,");

        mCurrentEventId = event.id;
    }

    private void search(String searchQuery, Time goToTime) {
        // save query in recent queries
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                Utils.getSearchAuthority(this),
                CalendarRecentSuggestionsProvider.MODE);
        suggestions.saveRecentQuery(searchQuery, null);


        EventInfo searchEventInfo = new EventInfo();
        searchEventInfo.eventType = EventType.SEARCH;
        searchEventInfo.query = searchQuery;
        searchEventInfo.viewType = ViewType.AGENDA;
        if (goToTime != null) {
            searchEventInfo.startTime = goToTime;
        }
        mController.sendEvent(this, searchEventInfo);
        mQuery = searchQuery;
        //if (mSearchView != null) {
        //    mSearchView.setQuery(mQuery, false);
            //mSearchView.clearFocus();
        //}
    }

    private void deleteEvent(long eventId, long startMillis, long endMillis) {
        mDeleteEventHelper.delete(startMillis, endMillis, eventId, -1);
//        if (mIsMultipane && mEventInfoFragment != null
//                && eventId == mCurrentEventId) {
//            FragmentManager fragmentManager = getFragmentManager();
//            FragmentTransaction ft = fragmentManager.beginTransaction();
//            ft.remove(mEventInfoFragment);
//            ft.commit();
//            mEventInfoFragment = null;
//            mCurrentEventId = -1;
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // From the Android Dev Guide: "It's important to note that when
        // onNewIntent(Intent) is called, the Activity has not been restarted,
        // so the getIntent() method will still return the Intent that was first
        // received with onCreate(). This is why setIntent(Intent) is called
        // inside onNewIntent(Intent) (just in case you call getIntent() at a
        // later time)."
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query, null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_RESTORE_TIME, mController.getTime());
        outState.putString(BUNDLE_KEY_RESTORE_SEARCH_QUERY, mQuery);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Utils.setMidnightUpdater(
                mHandler, mTimeChangesUpdater, Utils.getTimeZone(this, mTimeChangesUpdater));
        // Make sure the today icon is up to date
        invalidateOptionsMenu();
        mTimeChangesReceiver = Utils.setTimeChangesReceiver(this, mTimeChangesUpdater);
        //mContentResolver.registerContentObserver(Events.CONTENT_URI, true, mObserver);
        // We call this in case the user changed the time zone
        //eventsChanged();
        if (adapter == null) {
            queryItems(defaultAgendas, 3, TYPE_LAST);
            queryItems(defaultAgendas, 3, TYPE_RECENT);
            for (int i = 0; i < defaultAgendas.size(); i++) {
                Log.v(TAG, "binzo:item" + i +
                        defaultAgendas.get(i).get(CalendarContract.Instances.TITLE));
            }
            adapter = new AgendaRecentAdapter(this, defaultAgendas,
                    R.layout.agenda_item_with_icon_for_search);
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(listener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.resetMidnightUpdater(mHandler, mTimeChangesUpdater);
        Utils.clearTimeChangesReceiver(this, mTimeChangesReceiver);
        mContentResolver.unregisterContentObserver(mObserver);
    }

    @Override
    public void eventsChanged() {
        String query = searchBox.getText().toString();
        if (query == null || "".equals(query))
            return;
        mController.sendEvent(this, EventType.EVENTS_CHANGED, null, null, -1, ViewType.CURRENT);
    }

    @Override
    public long getSupportedEventTypes() {
        return EventType.VIEW_EVENT | EventType.DELETE_EVENT;
    }

    @Override
    public void handleEvent(EventInfo event) {
        Log.v(TAG, "Binzo. handleEvent");
        long endTime = (event.endTime == null) ? -1 : event.endTime.toMillis(false);
        Log.v(TAG, "Binzo. handleEvent, endTime="+endTime);
        if (event.eventType == EventType.VIEW_EVENT) {
            showEventInfo(event);
        } else if (event.eventType == EventType.DELETE_EVENT) {
            deleteEvent(event.id, event.startTime.toMillis(false), endTime);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query == null || "".equals(query))
            return false;
        mQuery = query;
        mController.sendEvent(this, EventType.SEARCH, null, null, -1, ViewType.CURRENT, 0, query,
                getComponentName());
        return false;
    }

    // liubzh. Added for default results. begin
    private static final String[] PROJECTION = new String[]{
            CalendarContract.Instances._ID, // 0
            CalendarContract.Instances.TITLE, // 1
            CalendarContract.Instances.ALL_DAY, // 2
            CalendarContract.Instances.BEGIN, // 3
            CalendarContract.Instances.DESCRIPTION, // 4
            CalendarContract.Instances.START_DAY, // 5
            CalendarContract.Instances.END, // 6
            CalendarContract.Instances.EVENT_TIMEZONE, //7
            CalendarContract.Instances.CALENDAR_ID, //8
            CalendarContract.Instances.EVENT_ID // 9
    };
    private static final int TYPE_RECENT = 1;
    private static final int TYPE_LAST = 2;
    private static final String AGENDA_SORT_ORDER_RECENT =
            CalendarContract.Instances.START_DAY + " DESC, " +
                    CalendarContract.Instances.BEGIN + " DESC, " +
                    CalendarContract.Events.TITLE + " ASC";
    private static final String AGENDA_SORT_ORDER_LAST =
            CalendarContract.Instances._ID + " DESC";

    private Uri buildQueryUri(int start, int end, String searchQuery) {
        Uri rootUri = searchQuery == null ?
                CalendarContract.Instances.CONTENT_BY_DAY_URI :
                CalendarContract.Instances.CONTENT_SEARCH_BY_DAY_URI;

        Uri.Builder builder = rootUri.buildUpon();
        ContentUris.appendId(builder, start);
        ContentUris.appendId(builder, end);
        if (searchQuery != null) {
            builder.appendPath(searchQuery);
        }
        return builder.build();
    }

    private void queryItems (ArrayList<Map<String, Object>> toList,
                                    int count, int type) {
        String orderBy = AGENDA_SORT_ORDER_RECENT;
        if (type == TYPE_LAST)
            orderBy = AGENDA_SORT_ORDER_LAST;

        Time time = new Time();
        time.set(1, 0, 1970);
        if (type == TYPE_RECENT)
            time.set(System.currentTimeMillis());
        long timeInMillis = time.normalize(true);
        int start = Time.getJulianDay(timeInMillis, time.gmtoff);
        Time time2 = new Time();
        time2.set(31, 11, 2037);
        long timeInMillis2 = time2.normalize(true);
        int end = Time.getJulianDay(timeInMillis2, time2.gmtoff);

        Uri uri = buildQueryUri(start, end, null);

        ContentResolver resolver = getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, PROJECTION, null, null, orderBy);
            Log.v("binzo", "binzo_count="+cursor.getCount());
            int i = 0;
            while (cursor.moveToNext()) {
                if (idInList(toList, cursor.getLong(0))) {
                    continue;
                }
                Map<String, Object> item = new HashMap<>();
                item.put(CalendarContract.Instances._ID, cursor.getLong(0));
                item.put(CalendarContract.Instances.TITLE, cursor.getString(1));
                item.put(CalendarContract.Instances.ALL_DAY, cursor.getInt(2));
                item.put(CalendarContract.Instances.BEGIN, cursor.getLong(3));
                item.put(CalendarContract.Instances.DESCRIPTION, cursor.getString(4));
                item.put(CalendarContract.Instances.START_DAY, cursor.getInt(5));
                item.put(CalendarContract.Instances.END, cursor.getLong(6));
                item.put(CalendarContract.Instances.EVENT_TIMEZONE, cursor.getLong(7));
                item.put(CalendarContract.Instances.CALENDAR_ID, cursor.getLong(8));
                item.put(CalendarContract.Instances.EVENT_ID, cursor.getLong(9));
                toList.add(item);
                i++;
                if (i >= count)
                    break;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (cursor != null)
            cursor.close();
    }

    private boolean idInList(ArrayList<Map<String, Object>> list, long id) {
        if (list == null)
            return false;
        for (int i = 0; i < list.size(); i++) {
            if (id == (Long) list.get(i).get(CalendarContract.Instances.EVENT_ID))
                return true;
        }
        return false;
    }

    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map item = defaultAgendas.get(position);
            long _id = (Long) item.get(CalendarContract.Instances.EVENT_ID);
            long begin = (Long) item.get(CalendarContract.Instances.BEGIN);
            long end = (Long) item.get(CalendarContract.Instances.END);

            int allDay = (Integer) item.get(CalendarContract.Instances.ALL_DAY);
            boolean isAllDay = allDay == 1 ? true : false;
            long calendarId = (Long) item.get(CalendarContract.Instances.CALENDAR_ID);
            String title = (String) item.get(CalendarContract.Instances.TITLE);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI, _id);

            intent.setData(eventUri);
            intent.putExtra(AllInOneActivity.BUNDLE_KEY_EVENT_ID, _id);
            intent.setClass(MySearchActivity.this, EditEventActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRA_EVENT_BEGIN_TIME, begin);
            intent.putExtra(EXTRA_EVENT_END_TIME, end);
            intent.putExtra(EXTRA_EVENT_ALL_DAY, isAllDay);
            intent.putExtra(Events.TITLE, title);
            intent.putExtra(Events.CALENDAR_ID, calendarId);
            MySearchActivity.this.startActivity(intent);

            mCurrentEventId = _id;
        }
    };
    // liubzh. Added for default results. end
}
