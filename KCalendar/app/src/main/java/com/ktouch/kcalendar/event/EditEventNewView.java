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
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ktouch.kcalendar.CalendarEventModel;
import com.ktouch.kcalendar.CalendarEventModel.Attendee;
import com.ktouch.kcalendar.CalendarEventModel.ReminderEntry;
import com.ktouch.kcalendar.DeleteEventHelper;
import com.ktouch.kcalendar.EventRecurrenceFormatter;
import com.ktouch.kcalendar.GeneralPreferences;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
import com.ktouch.kcalendar.event.EditEventHelper.EditDoneRunnable;
import com.ktouch.kcalendar.recurrencepicker.RecurrencePickerDialog;
import com.ktouch.calendarcommon.EventRecurrence;
//import com.ktouch.common.Rfc822InputFilter;
//import com.ktouch.common.Rfc822Validator;
import com.ktouch.datetimepicker.date.DatePickerDialog;
import com.ktouch.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.ktouch.datetimepicker.time.RadialPickerLayout;
import com.ktouch.datetimepicker.time.TimePickerDialog;
import com.ktouch.datetimepicker.time.TimePickerDialog.OnTimeSetListener;
//import com.ktouch.chips.AccountSpecifier;
//import com.ktouch.chips.BaseRecipientAdapter;
//import com.ktouch.chips.ChipsUtil;
//import com.ktouch.chips.RecipientEditTextView;
//import com.ktouch.timezonepicker.TimeZoneInfo;
//import com.ktouch.timezonepicker.TimeZonePickerDialog;
//import com.ktouch.timezonepicker.TimeZonePickerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import com.ktouch.wheel.DateAndTimePickerAlertDialog;
import com.ktouch.wheel.TimePickerShow;

public class EditEventNewView implements View.OnClickListener, DialogInterface.OnCancelListener,
        DialogInterface.OnClickListener, OnItemSelectedListener {

    private static final String TAG = "EditEventView";
    private static final String GOOGLE_SECONDARY_CALENDAR = "calendar.google.com";
    private static final String PERIOD_SPACE = ". ";

    private static final String FRAG_TAG_DATE_PICKER = "datePickerDialogFragment";
    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";
    private static final String FRAG_TAG_TIME_ZONE_PICKER = "timeZonePickerDialogFragment";
    private static final String FRAG_TAG_RECUR_PICKER = "recurrencePickerDialogFragment";

    RelativeLayout mAllDayLayout;
    RelativeLayout mStartDateAndTimeLayout;
    RelativeLayout mEndDateAndTimeLayout;
    RelativeLayout mRemindersLayout;
    RelativeLayout mRecurrenceLayout;
    RelativeLayout mMarkLayout;

    EditText mTitleEdit;
    Switch mAllDaySwitch;
    TextView mStartDateAndTimeTextView;
    TextView mEndDateAndTimeTextView;
    TextView mRemindersTextView;
    TextView mRecurrenceTextView;
    TextView mMarkTextView;

    ArrayList<TextView> mTextViewList = new ArrayList<TextView>();
    ArrayList<RelativeLayout> mLayoutList = new ArrayList<RelativeLayout>();

    private TimePickerShow mTimePickerShow;

    ArrayList<View> mEditOnlyList = new ArrayList<View>();
    ArrayList<View> mEditViewList = new ArrayList<View>();
    ArrayList<View> mViewOnlyList = new ArrayList<View>();
    TextView mLoadingMessage;
    ScrollView mScrollView;

    TextView mStartTimeHome;
    TextView mStartDateHome;
    TextView mEndTimeHome;
    TextView mEndDateHome;

    TextView mTitleTextView;

    TextView mDescriptionTextView;
    TextView mWhenView;

    LinearLayout mRemindersContainer;
    View mDescriptionGroup;
    View mRemindersGroup;
    View mStartHomeGroup;
    View mEndHomeGroup;

    private int[] mOriginalPadding = new int[4];

    public boolean mIsMultipane;
    private ProgressDialog mLoadingCalendarsDialog;
    private AlertDialog mNoCalendarsDialog;
    private DialogFragment mTimezoneDialog;
    private Activity mActivity;
    private EditDoneRunnable mDone;
    private View mView;
    private CalendarEventModel mModel;
    private Cursor mCalendarsCursor;

    /**
     * Contents of the "minutes" spinner.  This has default values from the XML file, augmented
     * with any additional values that were already associated with the event.
     */
    private ArrayList<Integer> mReminderMinuteValues;
    private ArrayList<String> mReminderMinuteLabels;

    /**
     * Contents of the "methods" spinner.  The "values" list specifies the method constant
     * (e.g. {@link Reminders#METHOD_ALERT}) associated with the labels.  Any methods that
     * aren't allowed by the Calendar will be removed.
     */
    private ArrayList<Integer> mReminderMethodValues;
    private ArrayList<String> mReminderMethodLabels;

    private ArrayList<String> mRecurrenceLabels;

    private ArrayList<String> mMarkAsTexts;

    /**
     * Contents of the "availability" spinner. The "values" list specifies the
     * type constant (e.g. {@link Events#AVAILABILITY_BUSY}) associated with the
     * labels. Any types that aren't allowed by the Calendar will be removed.
     */

    private boolean mAvailabilityExplicitlySet;
    private boolean mAllDayChangingAvailability;
    private int mAvailabilityCurrentlySelected;

    private boolean mSaveAfterQueryComplete = false;

    private Time mStartTime;
    private Time mEndTime;
    private String mTimezone;
    private boolean mAllDay = false;
    private int mModification = EditEventHelper.MODIFY_UNINITIALIZED;

    private EventRecurrence mEventRecurrence = new EventRecurrence();

    private ArrayList<LinearLayout> mReminderItems = new ArrayList<LinearLayout>(0);
    private ArrayList<ReminderEntry> mUnsupportedReminders = new ArrayList<ReminderEntry>();
    private String mRrule;

    private static StringBuilder mSB = new StringBuilder(50);
    private static Formatter mF = new Formatter(mSB, Locale.getDefault());

    private boolean mReadOnly;

    // Fills in the date and time fields
    private void populateWhen() {
        long startMillis = mStartTime.toMillis(false /* use isDst */);
        long endMillis = mEndTime.toMillis(false /* use isDst */);

        Utils.setDateAndTime(mActivity, mStartDateAndTimeTextView, startMillis);
        Utils.setDateAndTime(mActivity, mEndDateAndTimeTextView, endMillis);
    }

    private void populateRepeats() {
        int freq = mEventRecurrence.freq;
        int interval = mEventRecurrence.interval <= 1 ? 1 : mEventRecurrence.interval;

        if(freq==0){
            mModel.mRecurrenceIndex = 0;
        } else if(freq==EventRecurrence.DAILY && interval==1){
            mModel.mRecurrenceIndex = 1;
        }else if(freq==EventRecurrence.WEEKLY && interval==1){
            mModel.mRecurrenceIndex = 2;
        }else if(freq==EventRecurrence.WEEKLY && interval==2){
            mModel.mRecurrenceIndex = 3;
        }else if(freq==EventRecurrence.MONTHLY && interval==1){
            mModel.mRecurrenceIndex = 4;
        }else if(freq==EventRecurrence.YEARLY && interval==1){
            mModel.mRecurrenceIndex = 5;
        }else {
            mModel.mRecurrenceIndex = 100;
        }

        if(mRecurrenceTextView!=null){
            if(mModel.mRecurrenceIndex<6) {
                mRecurrenceTextView.setText(mRecurrenceLabels.get(mModel.mRecurrenceIndex));
            }else {
                mRecurrenceTextView.setText(mActivity.getResources().getString(R.string.event_recurrence_custom));
            }
        }
    }

    public static class CalendarsAdapter extends ResourceCursorAdapter {
        public CalendarsAdapter(Context context, int resourceId, Cursor c) {
            super(context, resourceId, c);
            setDropDownViewResource(R.layout.calendars_dropdown_item);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            //View colorBar = view.findViewById(R.id.color);
            int colorColumn = cursor.getColumnIndexOrThrow(Calendars.CALENDAR_COLOR);
            int nameColumn = cursor.getColumnIndexOrThrow(Calendars.CALENDAR_DISPLAY_NAME);
            int ownerColumn = cursor.getColumnIndexOrThrow(Calendars.OWNER_ACCOUNT);
            //if (colorBar != null) {
            // colorBar.setBackgroundColor(Utils.getDisplayColorFromColor(cursor
            //  .getInt(colorColumn)));
            // }

            TextView name = (TextView) view.findViewById(R.id.calendar_name);
            if (name != null) {
                String displayName = cursor.getString(nameColumn);
                name.setText(displayName);

                TextView accountName = (TextView) view.findViewById(R.id.account_name);
                if (accountName != null) {
                    accountName.setText(cursor.getString(ownerColumn));
                    accountName.setVisibility(TextView.VISIBLE);
                }
            }
        }
    }

    /**
     * Does prep steps for saving a calendar event.
     * <p/>
     * This triggers a parse of the attendees list and checks if the event is
     * ready to be saved. An event is ready to be saved so long as a model
     * exists and has a calendar it can be associated with, either because it's
     * an existing event or we've finished querying.
     *
     * @return false if there is no model or no calendar had been loaded yet,
     * true otherwise.
     */
    public boolean prepareForSave() {
        if (mModel == null || (mCalendarsCursor == null && mModel.mUri == null)) {
            return false;
        }
        return fillModelFromUI();
    }

    public CalendarEventModel getModel() {
        return mModel;
    }

    // This is called if the user clicks on one of the buttons: "Save",
    // "Discard", or "Delete". This is also called if the user clicks
    // on the "remove reminder" button.
    @Override
    public void onClick(View view) {
        // This must be a click on one of the "remove reminder" buttons
        LinearLayout reminderItem = (LinearLayout) view.getParent();
        LinearLayout parent = (LinearLayout) reminderItem.getParent();
        parent.removeView(reminderItem);
        mReminderItems.remove(reminderItem);
        updateRemindersVisibility(mReminderItems.size());
        EventViewUtils.updateAddReminderButton(mView, mReminderItems, mModel.mCalendarMaxReminders);
    }

    public void setRecurrenceRule(String rrule){
        Log.d(TAG, "Old rrule:" + mRrule);
        Log.d(TAG, "New rrule:" + rrule);
        mRrule = rrule;
        if (mRrule != null) {
            mEventRecurrence.parse(mRrule);
        }
        populateRepeats();
    }

    // This is called if the user cancels the "No calendars" dialog.
    // The "No calendars" dialog is shown if there are no syncable calendars.
    @Override
    public void onCancel(DialogInterface dialog) {
        if (dialog == mLoadingCalendarsDialog) {
            mLoadingCalendarsDialog = null;
            mSaveAfterQueryComplete = false;
        } else if (dialog == mNoCalendarsDialog) {
            mDone.setDoneCode(Utils.DONE_REVERT);
            mDone.run();
            return;
        }
    }

    // This is called if the user clicks on a dialog button.
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mNoCalendarsDialog) {
            mDone.setDoneCode(Utils.DONE_REVERT);
            mDone.run();
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Intent nextIntent = new Intent(Settings.ACTION_ADD_ACCOUNT);
                final String[] array = {"com.ktouch.kcalendar"};
                nextIntent.putExtra(Settings.EXTRA_AUTHORITIES, array);
                nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(nextIntent);
            }
        }
    }

    // Goes through the UI elements and updates the model as necessary
    private boolean fillModelFromUI() {
        if (mModel == null) {
            return false;
        }

        mModel.normalizeReminders();
        mModel.mHasAlarm = mModel.mReminders.size() > 0;

        if (mAllDaySwitch != null) {
            mModel.mAllDay = mAllDaySwitch.isChecked();
        }

        if (mTitleEdit != null) {
            mModel.mTitle = mTitleEdit.getText().toString();
        }
        if (TextUtils.isEmpty(mModel.mLocation)) {
            mModel.mLocation = null;
        }
        if (TextUtils.isEmpty(mModel.mDescription)) {
            mModel.mDescription = null;
        }
        // If this was a new event we need to fill in the Calendar information
        //if (mModel.mUri == null && mCalendarsSpinner != null) {
        if (mModel.mUri == null) {
            mModel.mCalendarId = 0;
            int calendarCursorPosition = 0;

            if (mCalendarsCursor.moveToPosition(calendarCursorPosition)) {
                String defaultCalendar = mCalendarsCursor.getString(
                        EditEventHelper.CALENDARS_INDEX_OWNER_ACCOUNT);
                Utils.setSharedPreference(
                        mActivity, GeneralPreferences.KEY_DEFAULT_CALENDAR, defaultCalendar);
                mModel.mOwnerAccount = defaultCalendar;
                mModel.mOrganizer = defaultCalendar;
                mModel.mCalendarId = mCalendarsCursor.getLong(EditEventHelper.CALENDARS_INDEX_ID);
            }
        }

        if (mModel.mAllDay) {
            // Reset start and end time, increment the monthDay by 1, and set
            // the timezone to UTC, as required for all-day events.
            mTimezone = Time.TIMEZONE_UTC;
            mStartTime.hour = 0;
            mStartTime.minute = 0;
            mStartTime.second = 0;
            mStartTime.timezone = mTimezone;
            mModel.mStart = mStartTime.normalize(true);

            mEndTime.hour = 0;
            mEndTime.minute = 0;
            mEndTime.second = 0;
            mEndTime.timezone = mTimezone;
            // When a user see the event duration as "X - Y" (e.g. Oct. 28 - Oct. 29), end time
            // should be Y + 1 (Oct.30).
            final long normalizedEndTimeMillis =
                    mEndTime.normalize(true) + DateUtils.DAY_IN_MILLIS;
            if (normalizedEndTimeMillis < mModel.mStart) {
                // mEnd should be midnight of the next day of mStart.
                mModel.mEnd = mModel.mStart + DateUtils.DAY_IN_MILLIS;
            } else {
                mModel.mEnd = normalizedEndTimeMillis;
            }
        } else {
            mStartTime.timezone = mTimezone;
            mEndTime.timezone = mTimezone;
            mModel.mStart = mStartTime.toMillis(true);
            mModel.mEnd = mEndTime.toMillis(true);
        }
        mModel.mTimezone = mTimezone;

        mModel.mAvailability = 1;

        // rrrule
        // If we're making an exception we don't want it to be a repeating
        // event.
        if (mModification == EditEventHelper.MODIFY_SELECTED) {
            mModel.mRrule = null;
        } else {
            mModel.mRrule = mRrule;
        }

        return true;
    }

    public void setReadOnly(boolean readOnly){
        mReadOnly = readOnly;
        setAllLayoutEnable(!readOnly);
        updateView();
    }

    private void setEditTextEnable(EditText edit, boolean enable){
        if(edit!=null) {
            edit.setClickable(enable);
            edit.setFocusableInTouchMode(enable);

            if(enable){
                edit.setTextColor(mActivity.getResources().getColor(R.color.primary_text_black_color));
            } else {
                edit.setTextColor(mActivity.getResources().getColor(R.color.primary_text_gray_color));
            }
        }
    }

    private void setAllLayoutEnable(boolean enable) {
        setEditTextEnable(mTitleEdit, enable);
        for (RelativeLayout layout : mLayoutList) {
            layout.setClickable(enable);
        }

        for (TextView text : mTextViewList) {
            if (enable) {
                text.setTextColor(mActivity.getResources().getColor(R.color.primary_text_black_color));
            } else {
                text.setTextColor(mActivity.getResources().getColor(R.color.primary_text_gray_color));
            }
            text.setClickable(enable);
        }
    }

    class EditChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(mTitleEdit.getText()) && !mReadOnly) {
                setAllLayoutEnable(true);
                setAllDayViewsVisibility(mModel.mAllDay);
            } else {
                setAllLayoutEnable(false);
                if(!mReadOnly) {
                    setEditTextEnable(mTitleEdit, true);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public void showDateAndTimePicker(final boolean isStart, final Time time) {
        if (mTimePickerShow == null) {
            mTimePickerShow = new TimePickerShow(mActivity);
        }

        if (mTimePickerShow != null) {
            long millis = time.toMillis(false);
            final DateAndTimePickerAlertDialog dialog = new DateAndTimePickerAlertDialog(mActivity);
            dialog.builder();
            dialog.getDateField();
            dialog.setView(mTimePickerShow.timePickerView(millis, dialog.getDateField(), dialog.getTimeField()));
            dialog.setNegativeButton(mActivity.getText(R.string.negative_button).toString(), new OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            dialog.setPositiveButton(mActivity.getText(R.string.positive_button).toString(), new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Time newTime = new Time();
                    newTime.set(mTimePickerShow.getSelectedDateAndTime());
                    newTime.second = 0;
                    if(isStart) {
                        mStartTime.set(newTime);
                        Utils.setDateAndTime(mActivity, mStartDateAndTimeTextView, mTimePickerShow.getSelectedDateAndTime());
                        if(mEndTime.before(mStartTime)) {
                            newTime.hour++;
                            long newMillis = newTime.toMillis(false);
                            mEndTime.set(newMillis);
                            Utils.setDateAndTime(mActivity, mEndDateAndTimeTextView, mEndTime.toMillis(true));
                        }
                    } else {
                        mEndTime.set(newTime);
                        Utils.setDateAndTime(mActivity, mEndDateAndTimeTextView, mTimePickerShow.getSelectedDateAndTime());
                        if(mEndTime.before(mStartTime)) {
                            newTime.hour--;
                            long newMillis = newTime.toMillis(false);
                            mStartTime.set(newMillis);
                            Utils.setDateAndTime(mActivity, mStartDateAndTimeTextView, mStartTime.toMillis(true));
                        }
                    }
                }
            });
            dialog.show();

        }
    }

    public EditEventNewView(Activity activity, View view, EditDoneRunnable done,
                            boolean timeSelectedWasStartTime, boolean dateSelectedWasStartDate, boolean readOnly) {

        mActivity = activity;
        mView = view;
        mDone = done;
        mReadOnly = readOnly;

        mAllDayLayout = (RelativeLayout) view.findViewById(R.id.edit_event_detail_all_day);
        mStartDateAndTimeLayout = (RelativeLayout) view.findViewById(R.id.edit_event_detail_start_time);
        mEndDateAndTimeLayout = (RelativeLayout) view.findViewById(R.id.edit_event_detail_end_time);
        mRemindersLayout = (RelativeLayout) view.findViewById(R.id.edit_event_detail_reminders);
        mRecurrenceLayout = (RelativeLayout) view.findViewById(R.id.edit_event_detail_repetition);
        mMarkLayout = (RelativeLayout) view.findViewById(R.id.edit_event_detail_mark);

        mTitleEdit = (EditText) view.findViewById(R.id.edit_event_description_et);
        mAllDaySwitch = (Switch) view.findViewById(R.id.all_day_switch);
        mStartDateAndTimeTextView = (TextView) view.findViewById(R.id.start_time_tv);
        mEndDateAndTimeTextView = (TextView) view.findViewById(R.id.end_time_tv);
        mRemindersTextView = (TextView) view.findViewById(R.id.reminders_tv);
        mRecurrenceTextView = (TextView) view.findViewById(R.id.repetition_tv);
        mMarkTextView = (TextView) view.findViewById(R.id.mark_tv);


        if (mAllDayLayout != null) {
            mAllDayLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAllDaySwitch != null) {
                        mAllDaySwitch.toggle();
                    }
                }
            });
        }

        if (mStartDateAndTimeLayout != null) {
            mStartDateAndTimeLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDateAndTimePicker(true, mStartTime);
                }
            });
        }

        if (mEndDateAndTimeLayout != null) {
            mEndDateAndTimeLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDateAndTimePicker(false, mEndTime);
                }
            });
        }

        if (mRemindersLayout != null) {
            mRemindersLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditEventNewFragment.mDone = false;
                    Intent intent = new Intent();
                    intent.setClass(mActivity, ReminderSettingsActivity.class);
                    int index = EditEventNewFragment.REMINDER_MINITUES_INDEX_DEFAULT;
                    for (ReminderEntry re : mModel.mReminders) {
                        index = mReminderMinuteValues.indexOf(re.getMinutes());
                    }
                    intent.putExtra(EditEventNewFragment.EXTRA_REMINDER_DEFAULT, index);
                    mActivity.startActivityForResult(intent, EditEventNewFragment.REQUEST_CODE_REMINDER_SETTINGS);
                }
            });
        }

        if (mRecurrenceLayout != null) {
            mRecurrenceLabels = Utils.loadStringArray(mActivity.getResources(), R.array.recurrency_label_array);
            mRecurrenceLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditEventNewFragment.mDone = false;
                    Intent intent = new Intent();
                    intent.setClass(mActivity, RecurrenceSettingsActivity.class);
                    int index = mModel.mRecurrenceIndex;
                    intent.putExtra(EditEventNewFragment.EXTRA_RECURRENCE_DEFAULT, index);
                    intent.putExtra(EditEventNewFragment.EXTRA_FREQ_TYPE, mEventRecurrence.freq);
                    intent.putExtra(EditEventNewFragment.EXTRA_FREQ_INTERVAL, mEventRecurrence.interval);
                    intent.putExtra(EditEventNewFragment.EXTRA_START_TIME, mStartTime.toMillis(false));
                    intent.putExtra(EditEventNewFragment.EXTRA_END_TIME, mEndTime.toMillis(false));
                    mActivity.startActivityForResult(intent, EditEventNewFragment.REQUEST_CODE_RECURRENCE_SETTINGS);
                }
            });
        }

        if (mMarkLayout != null) {
            mMarkAsTexts = Utils.loadStringArray(mActivity.getResources(), R.array.mark_as_texts);
            mMarkLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditEventNewFragment.mDone = false;
                    Intent intent = new Intent();
                    intent.setClass(mActivity, MarkAsSettingsActivity.class);
//                    int index = mModel.mMarkAs;
                    int value = 0;
                    if(mModel.mDescription!=null && mModel.mDescription.length()==1){
                        try {
                            value = Integer.parseInt(mModel.mDescription);
                        }catch (Exception e){

                        }
                    }
                    intent.putExtra(EditEventNewFragment.EXTRA_MARK_AS_DEFAULT, value);
                    mActivity.startActivityForResult(intent, EditEventNewFragment.REQUEST_CODE_MARK_AS_SETTINGS);
                }
            });
        }

        if (mLayoutList != null) {
            mLayoutList.add(mAllDayLayout);
            mLayoutList.add(mRemindersLayout);
            mLayoutList.add(mRecurrenceLayout);
            mLayoutList.add(mMarkLayout);
        }

        if (mTextViewList != null) {
            mTextViewList.add(mRemindersTextView);
            mTextViewList.add(mRecurrenceTextView);
            mTextViewList.add(mMarkTextView);
        }


        if (mTitleEdit != null) {
            if (TextUtils.isEmpty(mTitleEdit.getText())) {
                setAllLayoutEnable(false);
            }

            mTitleEdit.addTextChangedListener(new EditChangedListener());
        }

        setAllLayoutEnable(!mReadOnly);

        // cache top level view elements
        mLoadingMessage = (TextView) view.findViewById(R.id.loading_message);
        mScrollView = (ScrollView) view.findViewById(R.id.scroll_view);

        mTitleTextView = (TextView) view.findViewById(R.id.title);
        mDescriptionTextView = (TextView) view.findViewById(R.id.description);
        mWhenView = (TextView) mView.findViewById(R.id.when);

        mStartTimeHome = (TextView) view.findViewById(R.id.start_time_home_tz);
        mStartDateHome = (TextView) view.findViewById(R.id.start_date_home_tz);
        mEndTimeHome = (TextView) view.findViewById(R.id.end_time_home_tz);
        mEndDateHome = (TextView) view.findViewById(R.id.end_date_home_tz);
        mRemindersGroup = view.findViewById(R.id.reminders_row);
        mDescriptionGroup = view.findViewById(R.id.description_row);
        mStartHomeGroup = view.findViewById(R.id.from_row_home_tz);
        mEndHomeGroup = view.findViewById(R.id.to_row_home_tz);

        //yanghong remove these views
        if (mStartHomeGroup != null) mStartHomeGroup.setVisibility(View.GONE);
        if (mEndHomeGroup != null) mEndHomeGroup.setVisibility(View.GONE);

        if (mTitleTextView != null) {
            mTitleTextView.setTag(mTitleTextView.getBackground());
        }

        mAvailabilityExplicitlySet = false;
        mAllDayChangingAvailability = false;
        mAvailabilityCurrentlySelected = -1;

        if (mDescriptionTextView != null) {
            mDescriptionTextView.setTag(mDescriptionTextView.getBackground());
        }

        if (mEditViewList != null) {
            mEditViewList.add(mTitleTextView);
            mEditViewList.add(mDescriptionTextView);
        }

        if (mViewOnlyList != null) {
            mViewOnlyList.add(view.findViewById(R.id.when_row));
        }

        if (mEditOnlyList != null) {
            mEditOnlyList.add(view.findViewById(R.id.all_day_row));
            mEditOnlyList.add(view.findViewById(R.id.availability_row));
            mEditOnlyList.add(view.findViewById(R.id.visibility_row));
            mEditOnlyList.add(view.findViewById(R.id.from_row));
            mEditOnlyList.add(view.findViewById(R.id.to_row));
            mEditOnlyList.add(mStartHomeGroup);
            mEditOnlyList.add(mEndHomeGroup);
        }

        mRemindersContainer = (LinearLayout) view.findViewById(R.id.reminder_items_container);

        mTimezone = Utils.getTimeZone(activity, null);
        mIsMultipane = activity.getResources().getBoolean(R.bool.tablet_config);
        mStartTime = new Time(mTimezone);
        mEndTime = new Time(mTimezone);

        // Display loading screen
        setModel(null);
    }


    /**
     * Loads an integer array asset into a list.
     */
    private static ArrayList<Integer> loadIntegerArray(Resources r, int resNum) {
        int[] vals = r.getIntArray(resNum);
        int size = vals.length;
        ArrayList<Integer> list = new ArrayList<Integer>(size);

        for (int i = 0; i < size; i++) {
            list.add(vals[i]);
        }

        return list;
    }

    /**
     * Loads a String array asset into a list.
     */
    private static ArrayList<String> loadStringArray(Resources r, int resNum) {
        String[] labels = r.getStringArray(resNum);
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(labels));
        return list;
    }

    /**
     * Prepares the reminder UI elements.
     * <p/>
     * (Re-)loads the minutes / methods lists from the XML assets, adds/removes items as
     * needed for the current set of reminders and calendar properties, and then creates UI
     * elements.
     */
    private void prepareReminders() {
        CalendarEventModel model = mModel;
        Resources r = mActivity.getResources();


        // Load the labels and corresponding numeric values for the minutes and methods lists
        // from the assets.  If we're switching calendars, we need to clear and re-populate the
        // lists (which may have elements added and removed based on calendar properties).  This
        // is mostly relevant for "methods", since we shouldn't have any "minutes" values in a
        // new event that aren't in the default set.
        mReminderMinuteValues = loadIntegerArray(r, R.array.reminder_minutes_values);
        mReminderMinuteLabels = loadStringArray(r, R.array.reminder_minutes_labels);
        mReminderMethodValues = loadIntegerArray(r, R.array.reminder_methods_values);
        mReminderMethodLabels = loadStringArray(r, R.array.reminder_methods_labels);

        // Remove any reminder methods that aren't allowed for this calendar.  If this is
        // a new event, mCalendarAllowedReminders may not be set the first time we're called.
        if (mModel.mCalendarAllowedReminders != null) {
            EventViewUtils.reduceMethodList(mReminderMethodValues, mReminderMethodLabels,
                    mModel.mCalendarAllowedReminders);
        }

        int numReminders = 0;
        if (model.mHasAlarm) {
            ArrayList<ReminderEntry> reminders = model.mReminders;
            numReminders = reminders.size();
            // Insert any minute values that aren't represented in the minutes list.
            for (ReminderEntry re : reminders) {
                if (mReminderMethodValues.contains(re.getMethod())) {
                    EventViewUtils.addMinutesToList(mActivity, mReminderMinuteValues,
                            mReminderMinuteLabels, re.getMinutes());
                }
            }

            // Create a UI element for each reminder.  We display all of the reminders we get
            // from the provider, even if the count exceeds the calendar maximum.  (Also, for
            // a new event, we won't have a maxReminders value available.)
            mUnsupportedReminders.clear();
            for (ReminderEntry re : reminders) {
                if (!mReminderMethodValues.contains(re.getMethod())
                        && re.getMethod() != Reminders.METHOD_DEFAULT) {
                    mUnsupportedReminders.add(re);
                }
            }
        }
        updateRemindersVisibility(numReminders);
        EventViewUtils.updateAddReminderButton(mView, mReminderItems, mModel.mCalendarMaxReminders);
    }


    /**
     * Fill in the view with the contents of the given event model. This allows
     * an edit view to be initialized before the event has been loaded. Passing
     * in null for the model will display a loading screen. A non-null model
     * will fill in the view's fields with the data contained in the model.
     *
     * @param model The event model to pull the data from
     */
    public void setModel(CalendarEventModel model) {
        mModel = model;

        if (model == null) {
            // Display loading screen
            if (mLoadingMessage != null) {
                mLoadingMessage.setVisibility(View.VISIBLE);
            }
            if (mScrollView != null) {
                mScrollView.setVisibility(View.GONE);
            }
            return;
        }

        long begin = model.mStart;
        long end = model.mEnd;
        mTimezone = model.mTimezone; // this will be UTC for all day events

        // Set up the starting times
        if (begin > 0) {
            mStartTime.timezone = mTimezone;
            mStartTime.set(begin);
            mStartTime.normalize(true);
        }
        if (end > 0) {
            mEndTime.timezone = mTimezone;
            mEndTime.set(end);
            mEndTime.normalize(true);
        }

        mRrule = model.mRrule;
        if (!TextUtils.isEmpty(mRrule)) {
            mEventRecurrence.parse(mRrule);
        }

        if (mEventRecurrence.startDate == null) {
            mEventRecurrence.startDate = mStartTime;
        }

        if (mAllDaySwitch != null) {
            mAllDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setAllDayViewsVisibility(isChecked);
                }
            });

            boolean prevAllDay = mAllDaySwitch.isChecked();
            mAllDay = false; // default to false. Let setAllDayViewsVisibility update it as needed
            if (model.mAllDay) {
                mAllDaySwitch.setChecked(true);
                // put things back in local time for all day events
                mTimezone = Utils.getTimeZone(mActivity, null);
                mStartTime.timezone = mTimezone;
                mEndTime.timezone = mTimezone;
                mEndTime.normalize(true);
            } else {
                mAllDaySwitch.setChecked(false);
            }

            // On a rotation we need to update the views but onCheckedChanged
            // doesn't get called
            if (prevAllDay == mAllDaySwitch.isChecked()) {
                setAllDayViewsVisibility(prevAllDay);
            }
        }

        prepareReminders();

        View reminderAddButton = mView.findViewById(R.id.reminder_add);
        View.OnClickListener addReminderOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminder();
            }
        };
        if (reminderAddButton != null) {
            reminderAddButton.setOnClickListener(addReminderOnClickListener);
        }

        if (model.mTitle != null && mTitleTextView != null) {
            mTitleTextView.setTextKeepState(model.mTitle);
        }

        if (model.mDescription != null && mDescriptionTextView != null) {
            mDescriptionTextView.setTextKeepState(model.mDescription);
        }

        if (model.mUri != null) {
            // This is an existing event so hide the calendar spinner
            // since we can't change the calendar.
            View calendarGroup = mView.findViewById(R.id.calendar_selector_group);
            if (calendarGroup != null) {
                calendarGroup.setVisibility(View.GONE);
            }
            TextView tv = (TextView) mView.findViewById(R.id.calendar_textview);
            if (tv != null) {
                tv.setText(model.mCalendarDisplayName);
            }
            tv = (TextView) mView.findViewById(R.id.calendar_textview_secondary);
            if (tv != null) {
                tv.setText(model.mOwnerAccount);
            }
        } else {
            View calendarGroup = mView.findViewById(R.id.calendar_group);
            if (calendarGroup != null) {
                calendarGroup.setVisibility(View.GONE);
            }
        }

        populateWhen();

        populateRepeats();

        updateView();

        if (mScrollView != null) {
            mScrollView.setVisibility(View.VISIBLE);
        }
        if (mLoadingMessage != null) {
            mLoadingMessage.setVisibility(View.GONE);
        }

        if(mTitleEdit!=null){
            mTitleEdit.setText(mModel.mTitle);
        }

        if (mRemindersTextView != null) {
            for (ReminderEntry re : mModel.mReminders) {
                int index = mReminderMinuteValues.indexOf(re.getMinutes());
                mRemindersTextView.setText(mReminderMinuteLabels.get(index));
            }
        }

        if(mMarkTextView!=null){
            int value = 0;
            if(mModel.mDescription!=null && mModel.mDescription.length()==1){
                try {
                    value = Integer.parseInt(mModel.mDescription);
                }catch (Exception e){

                }
            }

            mMarkTextView.setText(mMarkAsTexts.get(value));
        }

    }

    /**
     * Creates a single line string for the time/duration
     */
    protected void setWhenString() {
        String when;
        int flags = DateUtils.FORMAT_SHOW_DATE;
        String tz = mTimezone;
        if (mModel.mAllDay) {
            flags |= DateUtils.FORMAT_SHOW_WEEKDAY;
            tz = Time.TIMEZONE_UTC;
        } else {
            flags |= DateUtils.FORMAT_SHOW_TIME;
            if (DateFormat.is24HourFormat(mActivity)) {
                flags |= DateUtils.FORMAT_24HOUR;
            }
        }
        long startMillis = mStartTime.normalize(true);
        long endMillis = mEndTime.normalize(true);
        mSB.setLength(0);
        if(mModel.mAllDay) {
            Utils.setDate(mActivity, mStartDateAndTimeTextView, startMillis);
            Utils.setDate(mActivity, mEndDateAndTimeTextView, endMillis);
        } else {
            Utils.setDateAndTime(mActivity, mStartDateAndTimeTextView, startMillis);
            Utils.setDateAndTime(mActivity, mEndDateAndTimeTextView, endMillis);
        }
        setAllDayViewsVisibility(mModel.mAllDay);
    }

    /**
     * Configures the Calendars spinner.  This is only done for new events, because only new
     * events allow you to select a calendar while editing an event.
     * <p/>
     * We tuck a reference to a Cursor with calendar database data into the spinner, so that
     * we can easily extract calendar-specific values when the value changes (the spinner's
     * onItemSelected callback is configured).
     */
    public void setCalendarsCursor(Cursor cursor, boolean userVisible, long selectedCalendarId) {
        // If there are no syncable calendars, then we cannot allow
        // creating a new event.
        mCalendarsCursor = cursor;
        if (cursor == null || cursor.getCount() == 0) {
            // Cancel the "loading calendars" dialog if it exists
            if (mSaveAfterQueryComplete) {
                mLoadingCalendarsDialog.cancel();
            }
            if (!userVisible) {
                return;
            }
            // Create an error message for the user that, when clicked,
            // will exit this activity without saving the event.
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.no_syncable_calendars).setIconAttribute(
                    android.R.attr.alertDialogIcon).setMessage(R.string.no_calendars_found)
                    .setPositiveButton(R.string.add_account, this)
                    .setNegativeButton(android.R.string.no, this).setOnCancelListener(this);
            mNoCalendarsDialog = builder.show();
            return;
        }

        int selection;
        if (selectedCalendarId != -1) {
            selection = findSelectedCalendarPosition(cursor, selectedCalendarId);
        } else {
            selection = findDefaultCalendarPosition(cursor);
        }

        // populate the calendars spinner
        CalendarsAdapter adapter = new CalendarsAdapter(mActivity,
                R.layout.calendars_spinner_item, cursor);

        if (mSaveAfterQueryComplete) {
            mLoadingCalendarsDialog.cancel();
            if (prepareForSave() && fillModelFromUI()) {
                int exit = userVisible ? Utils.DONE_EXIT : 0;
                mDone.setDoneCode(Utils.DONE_SAVE | exit);
                mDone.run();
            } else if (userVisible) {
                mDone.setDoneCode(Utils.DONE_EXIT);
                mDone.run();
            } else if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "SetCalendarsCursor:Save failed and unable to exit view");
            }
            return;
        }
    }

    /**
     * Updates the view based on {@link #mModification} and {@link #mModel}
     */
    public void updateView() {
        if (mModel == null) {
            return;
        }
        if (EditEventHelper.canModifyEvent(mModel)) {
            setViewStates(mModification);
        } else {
            setViewStates(Utils.MODIFY_UNINITIALIZED);
        }
    }

    private void setViewStates(int mode) {
        // Extra canModify check just in case
        if (mode == Utils.MODIFY_UNINITIALIZED || !EditEventHelper.canModifyEvent(mModel)) {
            setWhenString();

            for (View v : mViewOnlyList) {
                if(v!=null) {
                    v.setVisibility(View.VISIBLE);
                }
            }
            for (View v : mEditOnlyList) {
                if(v!=null) {
                    v.setVisibility(View.GONE);
                }
            }
            for (View v : mEditViewList) {
                if(v!=null) {
                    v.setEnabled(false);
                    v.setBackgroundDrawable(null);
                }
            }

            if(mRemindersGroup!=null) {
                if (EditEventHelper.canAddReminders(mModel)) {
                    mRemindersGroup.setVisibility(View.VISIBLE);
                } else {
                    mRemindersGroup.setVisibility(View.GONE);
                }
            }

            if (mDescriptionTextView!=null && TextUtils.isEmpty(mDescriptionTextView.getText()) && mDescriptionGroup!=null) {
                mDescriptionGroup.setVisibility(View.GONE);
            }
        } else {
            for (View v : mViewOnlyList) {
                if (v != null) {
                    v.setVisibility(View.GONE);
                }
            }
            for (View v : mEditOnlyList) {
                if (v != null) {
                    v.setVisibility(View.VISIBLE);
                }
            }
            for (View v : mEditViewList) {
                if (v != null) {
                    v.setEnabled(true);
                    if (v.getTag() != null) {
                        v.setBackgroundDrawable((Drawable) v.getTag());
                        v.setPadding(mOriginalPadding[0], mOriginalPadding[1], mOriginalPadding[2],
                                mOriginalPadding[3]);
                    }
                }
            }

            if (mRemindersGroup != null) {
                mRemindersGroup.setVisibility(View.VISIBLE);
            }

            if (mDescriptionGroup != null) {
                mDescriptionGroup.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setModification(int modifyWhich) {
        mModification = modifyWhich;
        updateView();
    }

    private int findSelectedCalendarPosition(Cursor calendarsCursor, long calendarId) {
        if (calendarsCursor.getCount() <= 0) {
            return -1;
        }
        int calendarIdColumn = calendarsCursor.getColumnIndexOrThrow(Calendars._ID);
        int position = 0;
        calendarsCursor.moveToPosition(-1);
        while (calendarsCursor.moveToNext()) {
            if (calendarsCursor.getLong(calendarIdColumn) == calendarId) {
                return position;
            }
            position++;
        }
        return 0;
    }

    // Find the calendar position in the cursor that matches calendar in
    // preference
    private int findDefaultCalendarPosition(Cursor calendarsCursor) {
        if (calendarsCursor.getCount() <= 0) {
            return -1;
        }

        String defaultCalendar = Utils.getSharedPreference(
                mActivity, GeneralPreferences.KEY_DEFAULT_CALENDAR, (String) null);

        int calendarsOwnerIndex = calendarsCursor.getColumnIndexOrThrow(Calendars.OWNER_ACCOUNT);
        int accountNameIndex = calendarsCursor.getColumnIndexOrThrow(Calendars.ACCOUNT_NAME);
        int accountTypeIndex = calendarsCursor.getColumnIndexOrThrow(Calendars.ACCOUNT_TYPE);
        int position = 0;
        calendarsCursor.moveToPosition(-1);
        while (calendarsCursor.moveToNext()) {
            String calendarOwner = calendarsCursor.getString(calendarsOwnerIndex);
            if (defaultCalendar == null) {
                // There is no stored default upon the first time running.  Use a primary
                // calendar in this case.
                if (calendarOwner != null &&
                        calendarOwner.equals(calendarsCursor.getString(accountNameIndex)) &&
                        !CalendarContract.ACCOUNT_TYPE_LOCAL.equals(
                                calendarsCursor.getString(accountTypeIndex))) {
                    return position;
                }
            } else if (defaultCalendar.equals(calendarOwner)) {
                // Found the default calendar.
                return position;
            }
            position++;
        }
        return 0;
    }

    private void updateRemindersVisibility(int numReminders) {
        if (mRemindersContainer == null) {
            return;
        }
        if (numReminders == 0) {
            mRemindersContainer.setVisibility(View.GONE);
        } else {
            mRemindersContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Add a new reminder when the user hits the "add reminder" button.  We use the default
     * reminder time and method.
     */
    private void addReminder() {
        // TODO: when adding a new reminder, make it different from the
        // last one in the list (if any).
//        if (mDefaultReminderMinutes == GeneralPreferences.NO_REMINDER) {
//            EventViewUtils.addReminder(mActivity, mScrollView, this, mReminderItems,
//                    mReminderMinuteValues, mReminderMinuteLabels,
//                    mReminderMethodValues, mReminderMethodLabels,
//                    ReminderEntry.valueOf(GeneralPreferences.REMINDER_DEFAULT_TIME),
//                    mModel.mCalendarMaxReminders, null);
//        } else {
//            EventViewUtils.addReminder(mActivity, mScrollView, this, mReminderItems,
//                    mReminderMinuteValues, mReminderMinuteLabels,
//                    mReminderMethodValues, mReminderMethodLabels,
//                    ReminderEntry.valueOf(mDefaultReminderMinutes),
//                    mModel.mCalendarMaxReminders, null);
//        }
        updateRemindersVisibility(mReminderItems.size());
        EventViewUtils.updateAddReminderButton(mView, mReminderItems, mModel.mCalendarMaxReminders);
    }

    // From com.google.android.gm.ComposeActivity
//    private MultiAutoCompleteTextView initMultiAutoCompleteTextView(RecipientEditTextView list) {
//        if (list == null) {
//            return null;
//        }
//        if (ChipsUtil.supportsChipsUi()) {
//            mAddressAdapter = new RecipientAdapter(mActivity);
//            list.setAdapter((BaseRecipientAdapter) mAddressAdapter);
//            list.setOnFocusListShrinkRecipients(false);
//        } else {
//            mAddressAdapter = new EmailAddressAdapter(mActivity);
//            list.setAdapter((EmailAddressAdapter) mAddressAdapter);
//        }
//        list.setTokenizer(new Rfc822Tokenizer());
//        list.setValidator(mEmailValidator);
//
//        // NOTE: assumes no other filters are set
//        list.setFilters(sRecipientFilters);
//
//        return list;
//    }

    /**
     * From com.google.android.gm.ComposeActivity Implements special address
     * cleanup rules: The first space key entry following an "@" symbol that is
     * followed by any combination of letters and symbols, including one+ dots
     * and zero commas, should insert an extra comma (followed by the space).
     */
//    private static InputFilter[] sRecipientFilters = new InputFilter[]{new Rfc822InputFilter()};

    private void setStartAndEndTimeEnable(boolean enable) {
        if (mStartDateAndTimeLayout != null) {
            mStartDateAndTimeLayout.setClickable(enable);
        }

        if (mEndDateAndTimeLayout != null) {
            mEndDateAndTimeLayout.setClickable(enable);
        }

        if (enable) {
            if (mStartDateAndTimeTextView != null) {
                if (TextUtils.isEmpty(mTitleEdit.getText())) {
                    mStartDateAndTimeTextView.setTextColor(mActivity.getResources().getColor(R.color.primary_text_gray_color));
                } else {
                    mStartDateAndTimeTextView.setTextColor(mActivity.getResources().getColor(R.color.primary_text_black_color));

                }
            }
            if (mEndDateAndTimeTextView != null) {
                if (TextUtils.isEmpty(mTitleEdit.getText())) {
                    mEndDateAndTimeTextView.setTextColor(mActivity.getResources().getColor(R.color.primary_text_gray_color));
                } else {
                    mEndDateAndTimeTextView.setTextColor(mActivity.getResources().getColor(R.color.primary_text_black_color));
                }
            }
        } else {
            if (mStartDateAndTimeTextView != null) {
                mStartDateAndTimeTextView.setTextColor(mActivity.getResources().getColor(R.color.primary_text_gray_color));
            }
            if (mEndDateAndTimeTextView != null) {
                mEndDateAndTimeTextView.setTextColor(mActivity.getResources().getColor(R.color.primary_text_gray_color));
            }
        }
    }

    /**
     * @param isChecked
     */
    protected void setAllDayViewsVisibility(boolean isChecked) {
        if (isChecked) {
            if (mEndTime.hour == 0 && mEndTime.minute == 0) {
                if (mAllDay != isChecked) {
                    mEndTime.monthDay--;
                }

                long endMillis = mEndTime.normalize(true);

                // Do not allow an event to have an end time
                // before the
                // start time.
                if (mEndTime.before(mStartTime)) {
                    mEndTime.set(mStartTime);
                    endMillis = mEndTime.normalize(true);
                }

                Utils.setDateAndTime(mActivity, mEndDateAndTimeTextView, endMillis);
            }
            Utils.setDate(mActivity, mStartDateAndTimeTextView, mStartTime.normalize(true));
            Utils.setDate(mActivity, mEndDateAndTimeTextView, mEndTime.normalize(true));

        } else {
            if (mEndTime.hour == 0 && mEndTime.minute == 0) {
                if (mAllDay != isChecked) {
                    mEndTime.monthDay++;
                }

                long endMillis = mEndTime.normalize(true);
                Utils.setDateAndTime(mActivity, mEndDateAndTimeTextView, endMillis);
            }

            Utils.setDateAndTime(mActivity, mStartDateAndTimeTextView, mStartTime.normalize(true));
            Utils.setDateAndTime(mActivity, mEndDateAndTimeTextView, mEndTime.normalize(true));
        }
        if(mReadOnly){
            setStartAndEndTimeEnable(false);
        } else {
            setStartAndEndTimeEnable(!isChecked);
        }
        mAllDay = isChecked;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // This is only used for the Calendar spinner in new events, and only fires when the
        // calendar selection changes or on screen rotation
        Cursor c = (Cursor) parent.getItemAtPosition(position);
        if (c == null) {
            // TODO: can this happen? should we drop this check?
            Log.w(TAG, "Cursor not set on calendar item");
            return;
        }

        // Do nothing if the selection didn't change so that reminders will not get lost
        int idColumn = c.getColumnIndexOrThrow(Calendars._ID);
        long calendarId = c.getLong(idColumn);
        int colorColumn = c.getColumnIndexOrThrow(Calendars.CALENDAR_COLOR);
        int color = c.getInt(colorColumn);
        int displayColor = Utils.getDisplayColorFromColor(color);

        // Prevents resetting of data (reminders, etc.) on orientation change.
        if (calendarId == mModel.mCalendarId && mModel.isCalendarColorInitialized() &&
                displayColor == mModel.getCalendarColor()) {
            return;
        }

//        setSpinnerBackgroundColor(displayColor);

        mModel.mCalendarId = calendarId;
        mModel.setCalendarColor(displayColor);
        mModel.mCalendarAccountName = c.getString(EditEventHelper.CALENDARS_INDEX_ACCOUNT_NAME);
        mModel.mCalendarAccountType = c.getString(EditEventHelper.CALENDARS_INDEX_ACCOUNT_TYPE);
        mModel.setEventColor(mModel.getCalendarColor());

//        setColorPickerButtonStates(mModel.getCalendarEventColors());

        // Update the max/allowed reminders with the new calendar properties.
        int maxRemindersColumn = c.getColumnIndexOrThrow(Calendars.MAX_REMINDERS);
        mModel.mCalendarMaxReminders = c.getInt(maxRemindersColumn);
        int allowedRemindersColumn = c.getColumnIndexOrThrow(Calendars.ALLOWED_REMINDERS);
        mModel.mCalendarAllowedReminders = c.getString(allowedRemindersColumn);
        int allowedAttendeeTypesColumn = c.getColumnIndexOrThrow(Calendars.ALLOWED_ATTENDEE_TYPES);
        mModel.mCalendarAllowedAttendeeTypes = c.getString(allowedAttendeeTypesColumn);
        int allowedAvailabilityColumn = c.getColumnIndexOrThrow(Calendars.ALLOWED_AVAILABILITY);
        mModel.mCalendarAllowedAvailability = c.getString(allowedAvailabilityColumn);

        // Discard the current reminders and replace them with the model's default reminder set.
        // We could attempt to save & restore the reminders that have been added, but that's
        // probably more trouble than it's worth.
        mModel.mReminders.clear();
        mModel.mReminders.addAll(mModel.mDefaultReminders);
        mModel.mHasAlarm = mModel.mReminders.size() != 0;

        // Update the UI elements.
        mReminderItems.clear();
        LinearLayout reminderLayout =
                (LinearLayout) mScrollView.findViewById(R.id.reminder_items_container);
        reminderLayout.removeAllViews();
        prepareReminders();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
