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

package com.ktouch.kcalendar.event;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Colors;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ktouch.kcalendar.AsyncQueryService;
import com.ktouch.kcalendar.CalendarController;
import com.ktouch.kcalendar.CalendarController.EventHandler;
import com.ktouch.kcalendar.CalendarController.EventInfo;
import com.ktouch.kcalendar.CalendarController.EventType;
import com.ktouch.kcalendar.CalendarEventModel;
import com.ktouch.kcalendar.CalendarEventModel.Attendee;
import com.ktouch.kcalendar.CalendarEventModel.ReminderEntry;
import com.ktouch.kcalendar.DeleteEventHelper;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
import com.ktouch.kcalendar.agenda.AgendaListView;
import com.ktouch.kcalendar.contentprovider.KCalendarProvider;
//import com.ktouch.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
//import com.ktouch.colorpicker.HsvColorComparator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import android.widget.Button;

public class EditEventNewFragment extends Fragment implements DeleteEventHelper.DeleteNotifyListener,
        EventHandler {
    private static final String TAG = "EditEventActivity";
    private static final String COLOR_PICKER_DIALOG_TAG = "ColorPickerDialog";

    private static final int REQUEST_CODE_COLOR_PICKER = 0;
    public static final int REQUEST_CODE_REMINDER_SETTINGS = 1;
    public static final int REQUEST_CODE_RECURRENCE_SETTINGS = 2;
    public static final int REQUEST_CODE_MARK_AS_SETTINGS = 3;

    public static final String BUNDLE_KEY_MODEL = "key_model";
    private static final String BUNDLE_KEY_EDIT_STATE = "key_edit_state";
    private static final String BUNDLE_KEY_EVENT = "key_event";
    private static final String BUNDLE_KEY_READ_ONLY = "key_read_only";
    private static final String BUNDLE_KEY_EDIT_ON_LAUNCH = "key_edit_on_launch";
    private static final String BUNDLE_KEY_SHOW_COLOR_PALETTE = "show_color_palette";

    private static final String BUNDLE_KEY_DATE_BUTTON_CLICKED = "date_button_clicked";

    public static final String EXTRA_REMINDER_VALUE = "extra_reminder_value";
    public static final String EXTRA_REMINDER_DEFAULT = "extra_reminder_default";
    public static final String EXTRA_RECURRENCE_VALUE = "extra_recurrence_value";
    public static final String EXTRA_RECURRENCE_DEFAULT = "extra_recurrence_default";
    public static final String EXTRA_RECURRENCE_INDEX = "extra_recurrence_index";
    public static final String EXTRA_MARK_AS_DEFAULT = "extra_mark_as_default";
    public static final String EXTRA_MARK_AS_INDEX = "extra_mark_as_index";
    public static final String EXTRA_START_TIME = "extra_start_time";
    public static final String EXTRA_END_TIME = "extra_end_time";
    public static final String EXTRA_FREQ_TYPE = "extra_freq_type";
    public static final String EXTRA_FREQ_INTERVAL = "extra_freq_interval";

    public static final int REMINDER_MINITUES_DEFAULT = 0;

    public static final int REMINDER_MINITUES_INDEX_DEFAULT = 1;
    public static final int RECURRENCE_INDEX_DEFAULT = 0;
    public static final int RECURRENCE_CUSTOM_INDEX = 100;
    public static final int MARK_AS_INDEX_DEFAULT = 0;

    private static final boolean DEBUG = false;

    private static final int TOKEN_EVENT = 1;
    private static final int TOKEN_REMINDERS = 1 << 2;
    private static final int TOKEN_CALENDARS = 1 << 3;

    private static final int TOKEN_ALL = TOKEN_EVENT | TOKEN_REMINDERS
            | TOKEN_CALENDARS;
    private static final int TOKEN_UNITIALIZED = 1 << 31;

    /**
     * A bitfield of TOKEN_* to keep track which query hasn't been completed
     * yet. Once all queries have returned, the model can be applied to the
     * view.
     */
    private int mOutstandingQueries = TOKEN_UNITIALIZED;

    EditEventHelper mHelper;
    CalendarEventModel mModel;
    CalendarEventModel mOriginalModel;
    CalendarEventModel mRestoreModel;
    EditEventNewView mView;
    QueryHandler mHandler;

    private AlertDialog mModifyDialog;
    int mModification = Utils.MODIFY_UNINITIALIZED;

    private final EventInfo mEvent;
    private EventBundle mEventBundle;
    private ArrayList<ReminderEntry> mReminders;
    private int mEventColor;
    private boolean mEventColorInitialized = false;
    private Uri mUri;
    private long mBegin;
    private long mEnd;
    private long mCalendarId = -1;

    private Activity mActivity;
    private final Done mOnDone = new Done();

    private boolean mSaveOnDetach = true;
    private boolean mIsReadOnly = false;
    public boolean mShowModifyDialogOnLaunch = true;

    private boolean mTimeSelectedWasStartTime;
    private boolean mDateSelectedWasStartDate;

    private InputMethodManager mInputMethodManager;

    private final Intent mIntent;

    private TextView mSaveOrEditButton;

    public static boolean mDone = false;

    private ImageView mBackButton;
    private TextView mTitle;
    private LinearLayout mDeleteButton;

    private DeleteEventHelper mDeleteHelper;
    // Used to prevent saving changes in event if it is being deleted.
    private boolean mEventDeletionStarted = false;

    private static int mDialogWidth = 500;
    private static int mDialogHeight = 600;
    private static int DIALOG_TOP_MARGIN = 8;
    private boolean mIsDialog = false;
    private boolean mIsPaused = true;
    private boolean mDismissOnResume = false;
    private boolean mDeleteDialogVisible = false;
    private int mDeleteDialogChoice = -1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDone = true;
        if (requestCode == REQUEST_CODE_REMINDER_SETTINGS && data != null) {
            int value = data.getIntExtra(EXTRA_REMINDER_VALUE, 0);
            CalendarEventModel model = mView.getModel();
//            mOriginalModel = model;
            ArrayList<ReminderEntry> reminders = new ArrayList<ReminderEntry>();
            reminders.add(ReminderEntry.valueOf(value, 1));
            model.mReminders = reminders;
            model.normalizeReminders();
            mView.setModel(model);
            mModel = model;
            mReminders = reminders;
        } else if (requestCode == REQUEST_CODE_RECURRENCE_SETTINGS && data != null) {
            String rrule = data.getStringExtra(EXTRA_RECURRENCE_VALUE);
            int value = data.getIntExtra(EXTRA_RECURRENCE_INDEX, RECURRENCE_INDEX_DEFAULT);
            CalendarEventModel model = mView.getModel();
//            mOriginalModel = model;
            model.mRecurrenceIndex = value;
            mView.setModel(model);
            mView.setRecurrenceRule(rrule);
            mModel = model;
        } else if (requestCode == REQUEST_CODE_MARK_AS_SETTINGS && data != null) {
            int value = data.getIntExtra(EXTRA_MARK_AS_INDEX, RECURRENCE_INDEX_DEFAULT);
            CalendarEventModel model = mView.getModel();
//            mOriginalModel = model;
            model.mMarkAs = value;
            model.mDescription = "" + value;
            mView.setModel(model);
            mModel = model;
        }
    }

    // TODO turn this into a helper function in EditEventHelper for building the
    // model
    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            // If the query didn't return a cursor for some reason return
            if (cursor == null) {
                return;
            }

            // If the Activity is finishing, then close the cursor.
            // Otherwise, use the new cursor in the adapter.
            final Activity activity = EditEventNewFragment.this.getActivity();
            if (activity == null || activity.isFinishing()) {
                cursor.close();
                return;
            }
            long eventId;
            switch (token) {
                case TOKEN_EVENT:
                    if (cursor.getCount() == 0) {
                        // The cursor is empty. This can happen if the event
                        // was deleted.
                        cursor.close();
                        mOnDone.setDoneCode(Utils.DONE_EXIT);
                        mSaveOnDetach = false;
                        mOnDone.run();
                        return;
                    }
                    mOriginalModel = new CalendarEventModel();
                    EditEventHelper.setModelFromCursor(mOriginalModel, cursor);
                    EditEventHelper.setModelFromCursor(mModel, cursor);
                    cursor.close();

                    mOriginalModel.mUri = mUri.toString();

                    mModel.mUri = mUri.toString();
                    mModel.mOriginalStart = mBegin;
                    mModel.mOriginalEnd = mEnd;
                    mModel.mIsFirstEventInSeries = mBegin == mOriginalModel.mStart;
                    mModel.mStart = mBegin;
                    mModel.mEnd = mEnd;
                    if (mEventColorInitialized) {
                        mModel.setEventColor(mEventColor);
                    }
                    eventId = mModel.mId;

                    // TOKEN_REMINDERS
                    if (mModel.mHasAlarm && mReminders == null) {
                        Uri rUri = Reminders.CONTENT_URI;
                        String[] remArgs = {
                                Long.toString(eventId)
                        };
                        mHandler.startQuery(TOKEN_REMINDERS, null, rUri,
                                EditEventHelper.REMINDERS_PROJECTION,
                                EditEventHelper.REMINDERS_WHERE /* selection */,
                                remArgs /* selection args */, null /* sort order */);
                    } else {
                        if (mReminders == null) {
                            // mReminders should not be null.
                            mReminders = new ArrayList<ReminderEntry>();
                        } else {
                            Collections.sort(mReminders);
                        }
                        mOriginalModel.mReminders = mReminders;
                        mModel.mReminders =
                                (ArrayList<ReminderEntry>) mReminders.clone();
                        setModelIfDone(TOKEN_REMINDERS);
                    }

                    // TOKEN_CALENDARS
                    String[] selArgs = {
                            Long.toString(mModel.mCalendarId)
                    };
                    mHandler.startQuery(TOKEN_CALENDARS, null, Calendars.CONTENT_URI,
                            EditEventHelper.CALENDARS_PROJECTION, EditEventHelper.CALENDARS_WHERE,
                            selArgs /* selection args */, null /* sort order */);


                    setModelIfDone(TOKEN_EVENT);
                    break;

                case TOKEN_REMINDERS:
                    try {
                        // Add all reminders to the models
                        while (cursor.moveToNext()) {
                            int minutes = cursor.getInt(EditEventHelper.REMINDERS_INDEX_MINUTES);
                            int method = cursor.getInt(EditEventHelper.REMINDERS_INDEX_METHOD);
                            ReminderEntry re = ReminderEntry.valueOf(minutes, method);
                            mModel.mReminders.add(re);
                            mOriginalModel.mReminders.add(re);
                        }

                        // Sort appropriately for display
                        Collections.sort(mModel.mReminders);
                        Collections.sort(mOriginalModel.mReminders);
                    } finally {
                        cursor.close();
                    }

                    setModelIfDone(TOKEN_REMINDERS);
                    break;
                case TOKEN_CALENDARS:
                    try {
                        if (mModel.mId == -1) {
                            // Populate Calendar spinner only if no event id is set.
                            MatrixCursor matrixCursor = Utils.matrixCursorFromCursor(cursor);
                            if (DEBUG) {
                                Log.d(TAG, "onQueryComplete: setting cursor with "
                                        + matrixCursor.getCount() + " calendars");
                            }
                            mView.setCalendarsCursor(matrixCursor, isAdded() && isResumed(),
                                    mCalendarId);
                        } else {
                            // Populate model for an existing event
                            EditEventHelper.setModelFromCalendarCursor(mModel, cursor);
                            EditEventHelper.setModelFromCalendarCursor(mOriginalModel, cursor);
                        }
                    } finally {
                        cursor.close();
                    }
                    setModelIfDone(TOKEN_CALENDARS);
                    break;

                default:
                    cursor.close();
                    break;
            }


        }
    }

    private void setModelIfDone(int queryType) {
        synchronized (this) {
            if (mEvent.id != -1) {
                int[] value = EditEventHelper.queryKCalendar(mActivity, AgendaListView.mInstanceId);
                mModel.mRecurrenceIndex = value[KCalendarProvider.KCALENDAR_PROVIDER_INDEX_RECURRENCE];
                mModel.mMarkAs = value[KCalendarProvider.KCALENDAR_PROVIDER_INDEX_MARK_AS];
            }
            mOutstandingQueries &= ~queryType;
            if (mOutstandingQueries == 0) {
                if (mRestoreModel != null) {
//                    mModel = mRestoreModel;
                }
//                if (mShowModifyDialogOnLaunch && mModification == Utils.MODIFY_UNINITIALIZED) {
//                    if (!TextUtils.isEmpty(mModel.mRrule)) {
//                        displayEditWhichDialog();
//                    } else {
//                        mModification = Utils.MODIFY_ALL;
//                    }
//
//                }
                if (mModel.mReminders.size() == 0) {
                    ArrayList<ReminderEntry> reminders = new ArrayList<ReminderEntry>(1);
                    reminders.add(ReminderEntry.valueOf(REMINDER_MINITUES_DEFAULT));
                    mModel.mReminders = reminders;
                }

                mView.setModel(mModel);
                mView.setModification(mModification);
            }

        }
    }

    public EditEventNewFragment() {
        this(null, null, false, -1, false, null);
    }

    public EditEventNewFragment(EventInfo event, ArrayList<ReminderEntry> reminders,
                                boolean eventColorInitialized, int eventColor, boolean readOnly, Intent intent) {
        mEvent = event;
        mIsReadOnly = readOnly;
        mIntent = intent;

        mReminders = reminders;
        mEventColorInitialized = eventColorInitialized;
        if (eventColorInitialized) {
            mEventColor = eventColor;
        }
        setHasOptionsMenu(true);
    }


    private void startQuery() {
        mUri = null;
        mBegin = -1;
        mEnd = -1;
        if (mEvent != null) {
            if (mEvent.id != -1) {
                mModel.mId = mEvent.id;
                mUri = ContentUris.withAppendedId(Events.CONTENT_URI, mEvent.id);
            } else {
                // New event. All day?
                mModel.mAllDay = mEvent.extraLong == CalendarController.EXTRA_CREATE_ALL_DAY;
            }
            if (mEvent.startTime != null) {
                mBegin = mEvent.startTime.toMillis(true);
            }
            if (mEvent.endTime != null) {
                mEnd = mEvent.endTime.toMillis(true);
            }
            if (mEvent.calendarId != -1) {
                mCalendarId = mEvent.calendarId;
            }
        } else if (mEventBundle != null) {
            if (mEventBundle.id != -1) {
                mModel.mId = mEventBundle.id;
                mUri = ContentUris.withAppendedId(Events.CONTENT_URI, mEventBundle.id);
            }
            mBegin = mEventBundle.start;
            mEnd = mEventBundle.end;
        }

        if (mReminders != null) {
            mModel.mReminders = mReminders;
        }

        if (mEventColorInitialized) {
            mModel.setEventColor(mEventColor);
        }

        if (mBegin <= 0) {
            // use a default value instead
            mBegin = mHelper.constructDefaultStartTime(System.currentTimeMillis());
        }
        if (mEnd < mBegin) {
            // use a default value instead
            mEnd = mHelper.constructDefaultEndTime(mBegin);
        }

        // Kick off the query for the event
        boolean newEvent = mUri == null;
        if (!newEvent) {
            mModel.mCalendarAccessLevel = Calendars.CAL_ACCESS_NONE;
            mOutstandingQueries = TOKEN_ALL;
            if (DEBUG) {
                Log.d(TAG, "startQuery: uri for event is " + mUri.toString());
            }
            mHandler.startQuery(TOKEN_EVENT, null, mUri, EditEventHelper.EVENT_PROJECTION,
                    null /* selection */, null /* selection args */, null /* sort order */);
        } else {
            mOutstandingQueries = TOKEN_CALENDARS;
            if (DEBUG) {
                Log.d(TAG, "startQuery: Editing a new event.");
            }
            mModel.mOriginalStart = mBegin;
            mModel.mOriginalEnd = mEnd;
            mModel.mStart = mBegin;
            mModel.mEnd = mEnd;
            mModel.mCalendarId = mCalendarId;
            mModel.mSelfAttendeeStatus = Attendees.ATTENDEE_STATUS_ACCEPTED;

            // Start a query in the background to read the list of calendars and colors
            mHandler.startQuery(TOKEN_CALENDARS, null, Calendars.CONTENT_URI,
                    EditEventHelper.CALENDARS_PROJECTION,
                    EditEventHelper.CALENDARS_WHERE_WRITEABLE_VISIBLE, null /* selection args */,
                    null /* sort order */);


            mModification = Utils.MODIFY_ALL;
            mView.setModification(mModification);
        }

        mModel.mRecurrenceIndex = RECURRENCE_INDEX_DEFAULT;
        mModel.mMarkAs = MARK_AS_INDEX_DEFAULT;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

        mHelper = new EditEventHelper(activity, null);
        mHandler = new QueryHandler(activity.getContentResolver());
        mModel = new CalendarEventModel(activity, mIntent);
        mInputMethodManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;

//        if (mIsReadOnly) {
//            view = inflater.inflate(R.layout.edit_event_single_column, null);
//        } else {
        view = inflater.inflate(R.layout.edit_event_new, null);
//        }
        mView = new EditEventNewView(mActivity, view, mOnDone, mTimeSelectedWasStartTime,
                mDateSelectedWasStartDate, mIsReadOnly);

        mTitle = (TextView) view.findViewById(R.id.edit_event_toolbar_title);

        mBackButton = (ImageView) view.findViewById(R.id.edit_event_toolbar_back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revertView();
            }
        });

        mSaveOrEditButton = (TextView) view.findViewById(R.id.edit_event_toolbar_save);
        mSaveOrEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveEditButtonClick();
            }
        });

        mDeleteButton = (LinearLayout) view.findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsReadOnly) {
                    mDeleteHelper =
                            new DeleteEventHelper(mActivity, mActivity, true);
                    mDeleteHelper.setDeleteNotificationListener(EditEventNewFragment.this);
                    mDeleteHelper.setOnDismissListener(createDeleteOnDismissListener());
                    mDeleteDialogVisible = true;
                    mDeleteHelper.delete(mEvent.startTime.toMillis(true), mEvent.endTime.toMillis(true), mEvent.id, -1, onDeleteRunnable);
                }
            }
        });

        if (mIsReadOnly) {
            mTitle.setText(getResources().getString(R.string.review_event));
            mSaveOrEditButton.setText(getResources().getString(R.string.edit));
        } else {
            mDeleteButton.setVisibility(View.GONE);
        }


        startQuery();

        return view;
    }

    public void revertView() {
//        if (!mIsReadOnly) {
            mOnDone.setDoneCode(Utils.DONE_REVERT);
            mOnDone.run();
//        }
    }

    public void onSaveEditButtonClick() {
        if (mIsReadOnly) {
            if (mShowModifyDialogOnLaunch && mModification == Utils.MODIFY_UNINITIALIZED) {
                if (!TextUtils.isEmpty(mModel.mRrule)) {
                    displayEditWhichDialog();
                } else {
                    mModification = Utils.MODIFY_ALL;
                }

            }
            mIsReadOnly = false;
            mView.setReadOnly(mIsReadOnly);
            mTitle.setText(getResources().getString(R.string.edit_event));
            mSaveOrEditButton.setText(getResources().getString(R.string.save));
            mDeleteButton.setVisibility(View.GONE);

        } else {

            if (EditEventHelper.canModifyEvent(mModel) || EditEventHelper.canRespond(mModel)) {
                if (mView != null && mView.prepareForSave()) {
                    if (mModification == Utils.MODIFY_UNINITIALIZED) {
                        mModification = Utils.MODIFY_ALL;
                    }
                    mOnDone.setDoneCode(Utils.DONE_SAVE | Utils.DONE_EXIT);
                    mOnDone.run();
                } else {
                    mOnDone.setDoneCode(Utils.DONE_REVERT);
                    mOnDone.run();
                }
            } else if (EditEventHelper.canAddReminders(mModel) && mModel.mId != -1
                    && mOriginalModel != null && mView.prepareForSave()) {
                saveReminders();
                mOnDone.setDoneCode(Utils.DONE_EXIT);
                mOnDone.run();
            } else {
                mOnDone.setDoneCode(Utils.DONE_REVERT);
                mOnDone.run();
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BUNDLE_KEY_MODEL)) {
                mRestoreModel = (CalendarEventModel) savedInstanceState.getSerializable(
                        BUNDLE_KEY_MODEL);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_EDIT_STATE)) {
                mModification = savedInstanceState.getInt(BUNDLE_KEY_EDIT_STATE);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_EDIT_ON_LAUNCH)) {
                mShowModifyDialogOnLaunch = savedInstanceState
                        .getBoolean(BUNDLE_KEY_EDIT_ON_LAUNCH);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_EVENT)) {
                mEventBundle = (EventBundle) savedInstanceState.getSerializable(BUNDLE_KEY_EVENT);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_READ_ONLY)) {
                mIsReadOnly = savedInstanceState.getBoolean(BUNDLE_KEY_READ_ONLY);
            }
            if (savedInstanceState.containsKey("EditEventView_timebuttonclicked")) {
                mTimeSelectedWasStartTime = savedInstanceState.getBoolean(
                        "EditEventView_timebuttonclicked");
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_DATE_BUTTON_CLICKED)) {
                mDateSelectedWasStartDate = savedInstanceState.getBoolean(
                        BUNDLE_KEY_DATE_BUTTON_CLICKED);
            }

        }
    }

    private void saveReminders() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(3);
        boolean changed = EditEventHelper.saveReminders(ops, mModel.mId, mModel.mReminders,
                mOriginalModel.mReminders, false /* no force save */);

        if (!changed) {
            return;
        }

        AsyncQueryService service = new AsyncQueryService(getActivity());
        service.startBatch(0, null, Calendars.CONTENT_URI.getAuthority(), ops, 0);
        // Update the "hasAlarm" field for the event
        Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, mModel.mId);
        int len = mModel.mReminders.size();
        boolean hasAlarm = len > 0;
        if (hasAlarm != mOriginalModel.mHasAlarm) {
            ContentValues values = new ContentValues();
            values.put(Events.HAS_ALARM, hasAlarm ? 1 : 0);
            service.startUpdate(0, null, uri, values, null, null, 0);
        }

        Toast.makeText(mActivity, R.string.saving_event, Toast.LENGTH_SHORT).show();
    }

    protected void displayEditWhichDialog() {
        if (mModification == Utils.MODIFY_UNINITIALIZED) {
            final boolean notSynced = TextUtils.isEmpty(mModel.mSyncId);
            boolean isFirstEventInSeries = mModel.mIsFirstEventInSeries;
            int itemIndex = 0;
            CharSequence[] items;

            if (notSynced) {
                // If this event has not been synced, then don't allow deleting
                // or changing a single instance.
                if (isFirstEventInSeries) {
                    // Still display the option so the user knows all events are
                    // changing
                    items = new CharSequence[1];
                } else {
                    items = new CharSequence[2];
                }
            } else {
                if (isFirstEventInSeries) {
                    items = new CharSequence[2];
                } else {
                    items = new CharSequence[3];
                }
                items[itemIndex++] = mActivity.getText(R.string.modify_event);
            }
            items[itemIndex++] = mActivity.getText(R.string.modify_all);

            // Do one more check to make sure this remains at the end of the list
            if (!isFirstEventInSeries) {
                items[itemIndex++] = mActivity.getText(R.string.modify_all_following);
            }

            // Display the modification dialog.
            if (mModifyDialog != null) {
                mModifyDialog.dismiss();
                mModifyDialog = null;
            }
            mModifyDialog = new AlertDialog.Builder(mActivity).setTitle(R.string.edit_event_label)
                    .setItems(items, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                // Update this if we start allowing exceptions
                                // to unsynced events in the app
                                mModification = notSynced ? Utils.MODIFY_ALL
                                        : Utils.MODIFY_SELECTED;
                                if (mModification == Utils.MODIFY_SELECTED) {
                                    mModel.mOriginalSyncId = notSynced ? null : mModel.mSyncId;
                                    mModel.mOriginalId = mModel.mId;
                                }
                            } else if (which == 1) {
                                mModification = notSynced ? Utils.MODIFY_ALL_FOLLOWING
                                        : Utils.MODIFY_ALL;
                            } else if (which == 2) {
                                mModification = Utils.MODIFY_ALL_FOLLOWING;
                            }

                            mView.setModification(mModification);
                        }
                    }).show();

            mModifyDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Activity a = EditEventNewFragment.this.getActivity();
                    if (a != null) {
                        a.finish();
                    }
                }
            });
        }
    }

    class Done implements EditEventHelper.EditDoneRunnable {
        private int mCode = -1;

        @Override
        public void setDoneCode(int code) {
            mCode = code;
        }

        @Override
        public void run() {
            // We only want this to get called once, either because the user
            // pressed back/home or one of the buttons on screen
            mSaveOnDetach = false;
            if (mModification == Utils.MODIFY_UNINITIALIZED) {
                // If this is uninitialized the user hit back, the only
                // changeable item is response to default to all events.
                mModification = Utils.MODIFY_ALL;
            }

            mModel = mView.getModel();
            if ((mCode & Utils.DONE_SAVE) != 0 && mModel != null
                    && (EditEventHelper.canRespond(mModel)
                    || EditEventHelper.canModifyEvent(mModel))
                    && mView.prepareForSave()
                    && !isEmptyNewEvent()
                    && mModel.normalizeReminders()
                    && mHelper.saveEvent(mModel, mOriginalModel, mModification)) {
                int stringResource;
                if (!mModel.mAttendeesList.isEmpty()) {
                    if (mModel.mUri != null) {
                        stringResource = R.string.saving_event_with_guest;
                    } else {
                        stringResource = R.string.creating_event_with_guest;
                    }
                } else {
                    if (mModel.mUri != null) {
                        stringResource = R.string.saving_event;
                    } else {
                        stringResource = R.string.creating_event;
                    }
                }
                Toast.makeText(mActivity, stringResource, Toast.LENGTH_SHORT).show();
            } else if ((mCode & Utils.DONE_SAVE) != 0 && mModel != null && isEmptyNewEvent()) {
                Toast.makeText(mActivity, R.string.empty_event, Toast.LENGTH_SHORT).show();
            }

            if ((mCode & Utils.DONE_DELETE) != 0 && mOriginalModel != null
                    && EditEventHelper.canModifyCalendar(mOriginalModel)) {
                long begin = mModel.mStart;
                long end = mModel.mEnd;
                int which = -1;
                switch (mModification) {
                    case Utils.MODIFY_SELECTED:
                        which = DeleteEventHelper.DELETE_SELECTED;
                        break;
                    case Utils.MODIFY_ALL_FOLLOWING:
                        which = DeleteEventHelper.DELETE_ALL_FOLLOWING;
                        break;
                    case Utils.MODIFY_ALL:
                        which = DeleteEventHelper.DELETE_ALL;
                        break;
                }
                DeleteEventHelper deleteHelper = new DeleteEventHelper(
                        mActivity, mActivity, !mIsReadOnly /* exitWhenDone */);
                deleteHelper.delete(begin, end, mOriginalModel, which);
            }

            if ((mCode & Utils.DONE_EXIT) != 0) {
                // This will exit the edit event screen, should be called
                // when we want to return to the main calendar views
                if ((mCode & Utils.DONE_SAVE) != 0) {
                    if (mActivity != null) {
                        long start = mModel.mStart;
                        long end = mModel.mEnd;
//                        if (mModel.mAllDay) {
                            // For allday events we want to go to the day in the
                            // user's current tz
                            String tz = Utils.getTimeZone(mActivity, null);
                            Time t = new Time(Time.TIMEZONE_UTC);
                            t.set(start);
                            t.timezone = tz;
                            start = t.toMillis(true);

                            t.timezone = Time.TIMEZONE_UTC;
                            t.set(end);
                            t.timezone = tz;
                            end = t.toMillis(true);
//                        }
                        CalendarController.getInstance(mActivity).launchViewEvent(-1, start, end,
                                Attendees.ATTENDEE_STATUS_NONE);
                    }
                }
                Activity a = EditEventNewFragment.this.getActivity();
                if (a != null) {
                    a.finish();
                }
            }

            // Hide a software keyboard so that user won't see it even after this Fragment's
            // disappearing.
            final View focusedView = mActivity.getCurrentFocus();
            if (focusedView != null) {
                mInputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
    }

    boolean isEmptyNewEvent() {
        if (mOriginalModel != null) {
            // Not new
            return false;
        }

        if (mModel.mOriginalStart != mModel.mStart || mModel.mOriginalEnd != mModel.mEnd) {
            return false;
        }

        if (!mModel.mAttendeesList.isEmpty()) {
            return false;
        }

        return mModel.isEmpty();
    }

    @Override
    public void onPause() {
        mIsPaused = true;
        Activity act = getActivity();
        if (mSaveOnDetach && act != null && !mIsReadOnly && !act.isChangingConfigurations()
                && mView.prepareForSave() && mDone) {
            mOnDone.setDoneCode(Utils.DONE_SAVE);
            mOnDone.run();
        }
        super.onPause();
        if (mDeleteDialogVisible && mDeleteHelper != null) {
            mDeleteDialogChoice = mDeleteHelper.getWhichDelete();
            mDeleteHelper.dismissAlertDialog();
            mDeleteHelper = null;
        }
    }

    @Override
    public void onDestroy() {
        if (mView != null) {
            mView.setModel(null);
        }
        if (mModifyDialog != null) {
            mModifyDialog.dismiss();
            mModifyDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public void eventsChanged() {
        // TODO Requery to see if event has changed
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mView.prepareForSave();
//        outState.putSerializable(BUNDLE_KEY_MODEL, mModel);
        outState.putInt(BUNDLE_KEY_EDIT_STATE, mModification);
        if (mEventBundle == null && mEvent != null) {
            mEventBundle = new EventBundle();
            mEventBundle.id = mEvent.id;
            if (mEvent.startTime != null) {
                mEventBundle.start = mEvent.startTime.toMillis(true);
            }
            if (mEvent.endTime != null) {
                mEventBundle.end = mEvent.startTime.toMillis(true);
            }
        }
        outState.putBoolean(BUNDLE_KEY_EDIT_ON_LAUNCH, mShowModifyDialogOnLaunch);
        outState.putSerializable(BUNDLE_KEY_EVENT, mEventBundle);
        outState.putBoolean(BUNDLE_KEY_READ_ONLY, mIsReadOnly);
    }

    @Override
    public long getSupportedEventTypes() {
        return EventType.USER_HOME;
    }

    @Override
    public void handleEvent(EventInfo event) {
        // It's currently unclear if we want to save the event or not when home
        // is pressed. When creating a new event we shouldn't save since we
        // can't get the id of the new event easily.
        Log.i("wanghg", "handleEvent  4");
        if ((false && event.eventType == EventType.USER_HOME) || (event.eventType == EventType.GO_TO
                && mSaveOnDetach)) {
            if (mView != null && mView.prepareForSave()) {
                mOnDone.setDoneCode(Utils.DONE_SAVE);
                mOnDone.run();
            }
        }
    }

    private static class EventBundle implements Serializable {
        private static final long serialVersionUID = 1L;
        long id = -1;
        long start = -1;
        long end = -1;
    }

    @Override
    public void onDeleteStarted() {
        mEventDeletionStarted = true;
    }

    private Dialog.OnDismissListener createDeleteOnDismissListener() {
        return new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Since OnPause will force the dialog to dismiss , do
                // not change the dialog status
                if (!mIsPaused) {
                    mDeleteDialogVisible = false;
                }
            }
        };
    }

    private final Runnable onDeleteRunnable = new Runnable() {
        @Override
        public void run() {
            if (EditEventNewFragment.this.mIsPaused) {
                mDismissOnResume = true;
                return;
            }
            if (EditEventNewFragment.this.isVisible()) {
//                EditEventNewFragment.this.dismiss();
            }
        }
    };


}
