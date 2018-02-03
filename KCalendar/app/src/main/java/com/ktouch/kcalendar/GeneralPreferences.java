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

package com.ktouch.kcalendar;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.CalendarContract;
import android.provider.CalendarContract.CalendarCache;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.text.format.Time;
import android.widget.Toast;

import com.ktouch.kcalendar.alerts.AlertReceiver;
import com.ktouch.kcalendar.event.EventViewUtils;
//import com.ktouch.timezonepicker.TimeZoneInfo;
//import com.ktouch.timezonepicker.TimeZonePickerDialog;
//import com.ktouch.timezonepicker.TimeZonePickerDialog.OnTimeZoneSetListener;
//import com.ktouch.timezonepicker.TimeZonePickerUtils;

//tianyu add begin
import android.util.Log;
//import com.tycust.media.TYRingtoneManager;
//tianyu add end

public class GeneralPreferences extends PreferenceFragment implements
        OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
    // The name of the shared preferences file. This name must be maintained for historical
    // reasons, as it's what PreferenceManager assigned the first time the file was created.
    private static final String TAG="GeneralPreferences";

    static final String SHARED_PREFS_NAME = "com.ktouch.kcalendar_preferences";
    static final String SHARED_PREFS_NAME_NO_BACKUP = "com.ktouch.kcalendar_preferences_no_backup";

    private static final String FRAG_TAG_TIME_ZONE_PICKER = "TimeZonePicker";

    // Preference keys
    public static final String KEY_DEFAULT_START = "preferences_default_start";
    public static final String KEY_HIDE_DECLINED = "preferences_hide_declined";
    public static final String KEY_WEEK_START_DAY = "preferences_week_start_day";
    public static final String KEY_SHOW_WEEK_NUM = "preferences_show_week_num";
    public static final String KEY_DAYS_PER_WEEK = "preferences_days_per_week";
    public static final String KEY_SKIP_SETUP = "preferences_skip_setup";

    public static final String KEY_CLEAR_SEARCH_HISTORY = "preferences_clear_search_history";

    public static final String KEY_ALERTS_CATEGORY = "preferences_alerts_category";
    public static final String KEY_ALERTS = "preferences_alerts";
    public static final String KEY_ALERTS_VIBRATE = "preferences_alerts_vibrate";
    public static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";
    public static final String KEY_ALERTS_POPUP = "preferences_alerts_popup";

    public static final String KEY_SHOW_CONTROLS = "preferences_show_controls";

    public static final String KEY_DEFAULT_REMINDER = "preferences_default_reminder";
    public static final int NO_REMINDER = -1;
    public static final String NO_REMINDER_STRING = "-1";
    public static final int REMINDER_DEFAULT_TIME = 10; // in minutes

    public static final String KEY_USE_CUSTOM_SNOOZE_DELAY = "preferences_custom_snooze_delay";
    public static final String KEY_DEFAULT_SNOOZE_DELAY = "preferences_default_snooze_delay";
    public static final int SNOOZE_DELAY_DEFAULT_TIME = 5; // in minutes

    public static final String KEY_DEFAULT_CELL_HEIGHT = "preferences_default_cell_height";
    public static final String KEY_VERSION = "preferences_version";

    /** Key to SharePreference for default view (CalendarController.ViewType) */
    public static final String KEY_START_VIEW = "preferred_startView";
    /**
     *  Key to SharePreference for default detail view (CalendarController.ViewType)
     *  Typically used by widget
     */
    public static final String KEY_DETAILED_VIEW = "preferred_detailedView";
    public static final String KEY_DEFAULT_CALENDAR = "preference_defaultCalendar";

    // These must be in sync with the array preferences_week_start_day_values
    public static final String WEEK_START_DEFAULT = "-1";
    public static final String WEEK_START_SATURDAY = "7";
    public static final String WEEK_START_SUNDAY = "1";
    public static final String WEEK_START_MONDAY = "2";

    // These keys are kept to enable migrating users from previous versions
    private static final String KEY_ALERTS_TYPE = "preferences_alerts_type";
    private static final String ALERT_TYPE_ALERTS = "0";
    private static final String ALERT_TYPE_STATUS_BAR = "1";
    private static final String ALERT_TYPE_OFF = "2";
    static final String KEY_HOME_TZ_ENABLED = "preferences_home_tz_enabled";
    static final String KEY_HOME_TZ = "preferences_home_tz";

    // Default preference values
    public static final String DEFAULT_DEFAULT_START = "-2";
    //yanghong modify the default view: week->month
    /*
    public static final int DEFAULT_START_VIEW = CalendarController.ViewType.WEEK;
    */
    public static final int DEFAULT_START_VIEW = CalendarController.ViewType.MONTH;

    public static final int DEFAULT_DETAILED_VIEW = CalendarController.ViewType.DAY;
    public static final boolean DEFAULT_SHOW_WEEK_NUM = false;
    // This should match the XML file.
    public static final String DEFAULT_RINGTONE = "content://settings/system/notification_sound";

    CheckBoxPreference mAlert;
    CheckBoxPreference mVibrate;
    RingtonePreference mRingtone;
    CheckBoxPreference mPopup;
    CheckBoxPreference mUseHomeTZ;
    CheckBoxPreference mHideDeclined;
    //yanghong modify begin
    //Preference mHomeTZ;
    //TimeZonePickerUtils mTzPickerUtils;
//    ListPreference mHomeTZ;
    //yanghong modify end
    ListPreference mWeekStart;
    ListPreference mDefaultReminder;
    ListPreference mSnoozeDelay;
    ListPreference mDefaultStart;

    private String mTimeZoneId;

//    private CharSequence[][] mTimezones;

    /** Return a properly configured SharedPreferences instance */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Set the default shared preferences in the proper context */
    public static void setDefaultValues(Context context) {
        PreferenceManager.setDefaultValues(context, SHARED_PREFS_NAME, Context.MODE_PRIVATE,
                R.xml.general_preferences, false);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Activity activity = getActivity();

        // Make sure to always use the same preferences file regardless of the package name
        // we're running under
        final PreferenceManager preferenceManager = getPreferenceManager();
        final SharedPreferences sharedPreferences = getSharedPreferences(activity);
        preferenceManager.setSharedPreferencesName(SHARED_PREFS_NAME);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.general_preferences);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        mAlert = (CheckBoxPreference) preferenceScreen.findPreference(KEY_ALERTS);
        mVibrate = (CheckBoxPreference) preferenceScreen.findPreference(KEY_ALERTS_VIBRATE);
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            PreferenceCategory mAlertGroup = (PreferenceCategory) preferenceScreen
                    .findPreference(KEY_ALERTS_CATEGORY);
            mAlertGroup.removePreference(mVibrate);
        }

        mRingtone = (RingtonePreference) preferenceScreen.findPreference(KEY_ALERTS_RINGTONE);
        String ringToneUri = Utils.getRingTonePreference(activity);

        // Set the ringToneUri to the backup-able shared pref only so that
        // the Ringtone dialog will open up with the correct value.
        final Editor editor = preferenceScreen.getEditor();
        editor.putString(GeneralPreferences.KEY_ALERTS_RINGTONE, ringToneUri).apply();

        //yanghong add modify on Feb.5,2015
        //String ringtoneDisplayString = getRingtoneTitleFromUri(activity, ringToneUri);
        //mRingtone.setSummary(ringtoneDisplayString == null ? "" : ringtoneDisplayString);
        setRingtoneSummary(ringToneUri);
        //yanghong add end

        mPopup = (CheckBoxPreference) preferenceScreen.findPreference(KEY_ALERTS_POPUP);
        mUseHomeTZ = (CheckBoxPreference) preferenceScreen.findPreference(KEY_HOME_TZ_ENABLED);
        mHideDeclined = (CheckBoxPreference) preferenceScreen.findPreference(KEY_HIDE_DECLINED);
        mWeekStart = (ListPreference) preferenceScreen.findPreference(KEY_WEEK_START_DAY);
        mDefaultReminder = (ListPreference) preferenceScreen.findPreference(KEY_DEFAULT_REMINDER);
        //yanghong modify begin
        //mHomeTZ = preferenceScreen.findPreference(KEY_HOME_TZ);
//        mHomeTZ = (ListPreference) preferenceScreen.findPreference(KEY_HOME_TZ);
//        String tz = mHomeTZ.getValue();
//        if (mTimezones == null) {
//            mTimezones = (new TimezoneAdapter(activity, tz, System.currentTimeMillis()))
//                    .getAllTimezones();
//        }
//        mHomeTZ.setEntryValues(mTimezones[0]);
//        mHomeTZ.setEntries(mTimezones[1]);
//        CharSequence tzName = mHomeTZ.getEntry();
//        ///M: Should not set TimeZone value to summary.It will restore the
//        /// systemTZ as homeTZ at the first time launch the calendar setting.@{
//        if (TextUtils.isEmpty(tzName)) {
//            mHomeTZ.setValue(Utils.getTimeZone(activity, null));
//            mHomeTZ.setSummary(mHomeTZ.getEntry());
//        } else {
//            mHomeTZ.setSummary(tzName);
//        }
        //yanghong modify end

        mDefaultStart = (ListPreference) preferenceScreen.findPreference(KEY_DEFAULT_START);
        mDefaultStart.setOnPreferenceChangeListener(this);

        mSnoozeDelay = (ListPreference) preferenceScreen.findPreference(KEY_DEFAULT_SNOOZE_DELAY);
        buildSnoozeDelayEntries();

        mWeekStart.setSummary(mWeekStart.getEntry());
        mDefaultReminder.setSummary(mDefaultReminder.getEntry());
        mSnoozeDelay.setSummary(mSnoozeDelay.getEntry());
        mDefaultStart.setSummary(mDefaultStart.getEntry());

        // This triggers an asynchronous call to the provider to refresh the data in shared pref
        mTimeZoneId = Utils.getTimeZone(activity, null);

        SharedPreferences prefs = CalendarUtils.getSharedPreferences(activity,
                Utils.SHARED_PREFS_NAME);

        // Utils.getTimeZone will return the currentTimeZone instead of the one
        // in the shared_pref if home time zone is disabled. So if home tz is
        // off, we will explicitly read it.
        if (!prefs.getBoolean(KEY_HOME_TZ_ENABLED, false)) {
            mTimeZoneId = prefs.getString(KEY_HOME_TZ, Time.getCurrentTimezone());
        }

        /* //yanghong remove it
        mHomeTZ.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimezoneDialog();
                return true;
            }
        });

        if (mTzPickerUtils == null) {
            mTzPickerUtils = new TimeZonePickerUtils(getActivity());
        }
        CharSequence timezoneName = mTzPickerUtils.getGmtDisplayName(getActivity(), mTimeZoneId,
                System.currentTimeMillis(), false);
        mHomeTZ.setSummary(timezoneName != null ? timezoneName : mTimeZoneId);
        TimeZonePickerDialog tzpd = (TimeZonePickerDialog) activity.getFragmentManager()
                .findFragmentByTag(FRAG_TAG_TIME_ZONE_PICKER);
        if (tzpd != null) {
            tzpd.setOnTimeZoneSetListener(this);
        }
        */
        //yanghong remove end

        migrateOldPreferences(sharedPreferences);

        updateChildPreferences();
    }

    //yanghong add for ring tone title display on Feb.5,2015
    private String getRingTitle(Uri uri){
        String title = null;
        if(uri == null){
            //return getString(com.android.internal.R.string.ringtone_default);
            return null;
        }
        Ringtone tone = RingtoneManager.getRingtone(getActivity(), uri);
        if(tone != null){
            title = tone.getTitle(getActivity());
            //title = RingtoneManager.getRingToneTitle(getActivity(), title);

        }else{
            title = getResources().getString(R.string.silent_ringtone);
        }

        return title;
    }

    private void setRingtoneSummary(String soundValue) {
        Uri soundUri = TextUtils.isEmpty(soundValue) ? null : Uri.parse(soundValue);
        Ringtone tone = soundUri != null ? RingtoneManager.getRingtone(getActivity(), soundUri) : null;
        //yanghong 20140107 modify begin
        /*
        ringPre.setSummary(tone != null ? tone.getTitle(this)
                : getResources().getString(R.string.silent_ringtone));
        */
        String title = null;
        try{
            if(tone != null){
                if(soundValue.equals(DEFAULT_RINGTONE)){
                    //title = getString(com.android.internal.R.string.ringtone_default);
                    Log.d(TAG,"setRingtoneSummary(A)title="+title);
                }else{
//                    if(TYRingtoneManager.isRingtoneExist(getActivity(),soundUri)){
//                        title = getRingTitle(soundUri);
//                    }else{
//                        title = getString(com.android.internal.R.string.ringtone_default);
//                        Uri temp = TYRingtoneManager.getDefaultRingtoneUri(getActivity(),RingtoneManager.TYPE_NOTIFICATION);
//                        title = getRingTitle(temp);
//                        Log.d(TAG,"setRingtoneSummary(C)title="+title);
//                    }
                }
            }else{
                title = getActivity().getResources().getString(R.string.silent_ringtone);
            }
        }catch(Exception e){
            Log.e(TAG,"setRingtoneSummary()Exception="+e.toString());
        }

        if(title != null){
            mRingtone.setSummary(title);
        }
    }
    //yanghong add end

//    private void showTimezoneDialog() {
//        final Activity activity = getActivity();
//        if (activity == null) {
//            return;
//        }
//
//        Bundle b = new Bundle();
//        b.putLong(TimeZonePickerDialog.BUNDLE_START_TIME_MILLIS, System.currentTimeMillis());
//        b.putString(TimeZonePickerDialog.BUNDLE_TIME_ZONE, Utils.getTimeZone(activity, null));
//
//        FragmentManager fm = getActivity().getFragmentManager();
//        TimeZonePickerDialog tzpd = (TimeZonePickerDialog) fm
//                .findFragmentByTag(FRAG_TAG_TIME_ZONE_PICKER);
//        if (tzpd != null) {
//            tzpd.dismiss();
//        }
//        tzpd = new TimeZonePickerDialog();
//        tzpd.setArguments(b);
//        tzpd.setOnTimeZoneSetListener(this);
//        tzpd.show(fm, FRAG_TAG_TIME_ZONE_PICKER);
//    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        setPreferenceListeners(this);
        mRingtone.setOnPreferenceChangeListener(this);//yanghong add to avoid the unregister when call in onStop()
    }

    /**
     * Sets up all the preference change listeners to use the specified
     * listener.
     */
    private void setPreferenceListeners(OnPreferenceChangeListener listener) {
        mUseHomeTZ.setOnPreferenceChangeListener(listener);
//        mHomeTZ.setOnPreferenceChangeListener(listener);
        mWeekStart.setOnPreferenceChangeListener(listener);
        mDefaultReminder.setOnPreferenceChangeListener(listener);
        mSnoozeDelay.setOnPreferenceChangeListener(listener);
        //mRingtone.setOnPreferenceChangeListener(listener);//yanghong move it to onStart
        mHideDeclined.setOnPreferenceChangeListener(listener);
        mVibrate.setOnPreferenceChangeListener(listener);
    }

    @Override
    public void onStop() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        setPreferenceListeners(null);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity a = getActivity();
        if (key.equals(KEY_ALERTS)) {
            updateChildPreferences();
            if (a != null) {
                Intent intent = new Intent();
                intent.setClass(a, AlertReceiver.class);
                if (mAlert.isChecked()) {
                    intent.setAction(AlertReceiver.ACTION_DISMISS_OLD_REMINDERS);
                } else {
                    intent.setAction(AlertReceiver.EVENT_REMINDER_APP_ACTION);
                }
                a.sendBroadcast(intent);
            }
        }
        if (a != null) {
            BackupManager.dataChanged(a.getPackageName());
        }
    }

    /**
     * Handles time zone preference changes
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String tz;
        final Activity activity = getActivity();
        if (preference == mUseHomeTZ) {
            if ((Boolean)newValue) {
                //yanghong modify begin
                //tz = mTimeZoneId;
//                tz = mHomeTZ.getValue();
                //yanghong modify end
            } else {
                tz = CalendarCache.TIMEZONE_TYPE_AUTO;
            }
//            Utils.setTimeZone(activity, tz);
            return true;
        } else if (preference == mHideDeclined) {
            mHideDeclined.setChecked((Boolean) newValue);
            Intent intent = new Intent(Utils.getWidgetScheduledUpdateAction(activity));
            intent.setDataAndType(CalendarContract.CONTENT_URI, Utils.APPWIDGET_DATA_TYPE);
            activity.sendBroadcast(intent);
            return true;
//        //yanghong modify begin
//        } else if (preference == mHomeTZ) {
//            tz = (String) newValue;
//            // We set the value here so we can read back the entry
//            mHomeTZ.setValue(tz);
//            mHomeTZ.setSummary(mHomeTZ.getEntry());
//            Utils.setTimeZone(getActivity(), tz);
//        //yanghong modify end
        } else if (preference == mWeekStart) {
            mWeekStart.setValue((String) newValue);
            mWeekStart.setSummary(mWeekStart.getEntry());
        } else if (preference == mDefaultReminder) {
            mDefaultReminder.setValue((String) newValue);
            mDefaultReminder.setSummary(mDefaultReminder.getEntry());
        } else if (preference == mSnoozeDelay) {
            mSnoozeDelay.setValue((String) newValue);
            mSnoozeDelay.setSummary(mSnoozeDelay.getEntry());
        } else if (preference == mRingtone) {
            if (newValue instanceof String) {
                Utils.setRingTonePreference(activity, (String) newValue);
                //yanghong modify on Feb.5,2015
                //String ringtone = getRingtoneTitleFromUri(activity, (String) newValue);
                //mRingtone.setSummary(ringtone == null ? "" : ringtone);
                setRingtoneSummary((String) newValue);
                //yanghong modify end
            }
            return true;
        } else if (preference == mVibrate) {
            mVibrate.setChecked((Boolean) newValue);
            return true;
        } else if (preference == mDefaultStart) {
            int i = mDefaultStart.findIndexOfValue((String) newValue);
            mDefaultStart.setSummary(mDefaultStart.getEntries()[i]);
            return true;
        } else {
            return true;
        }
        return false;
    }

    public String getRingtoneTitleFromUri(Context context, String uri) {
        if (TextUtils.isEmpty(uri)) {
            return null;
        }

        Ringtone ring = RingtoneManager.getRingtone(getActivity(), Uri.parse(uri));
        if (ring != null) {
            return ring.getTitle(context);
        }
        return null;
    }

    /**
     * If necessary, upgrades previous versions of preferences to the current
     * set of keys and values.
     * @param prefs the preferences to upgrade
     */
    private void migrateOldPreferences(SharedPreferences prefs) {
        // If needed, migrate vibration setting from a previous version

        mVibrate.setChecked(Utils.getDefaultVibrate(getActivity(), prefs));

        // If needed, migrate the old alerts type settin
        if (!prefs.contains(KEY_ALERTS) && prefs.contains(KEY_ALERTS_TYPE)) {
            String type = prefs.getString(KEY_ALERTS_TYPE, ALERT_TYPE_STATUS_BAR);
            if (type.equals(ALERT_TYPE_OFF)) {
                mAlert.setChecked(false);
                mPopup.setChecked(false);
                mPopup.setEnabled(false);
            } else if (type.equals(ALERT_TYPE_STATUS_BAR)) {
                mAlert.setChecked(true);
                mPopup.setChecked(false);
                mPopup.setEnabled(true);
            } else if (type.equals(ALERT_TYPE_ALERTS)) {
                mAlert.setChecked(true);
                mPopup.setChecked(true);
                mPopup.setEnabled(true);
            }
            // clear out the old setting
            prefs.edit().remove(KEY_ALERTS_TYPE).commit();
        }
    }

    /**
     * Keeps the dependent settings in sync with the parent preference, so for
     * example, when notifications are turned off, we disable the preferences
     * for configuring the exact notification behavior.
     */
    private void updateChildPreferences() {
        if (mAlert.isChecked()) {
            mVibrate.setEnabled(true);
            mRingtone.setEnabled(true);
            mPopup.setEnabled(true);
        } else {
            mVibrate.setEnabled(false);
            mRingtone.setEnabled(false);
            mPopup.setEnabled(false);
        }
    }

    private void buildSnoozeDelayEntries() {
        final CharSequence[] values = mSnoozeDelay.getEntryValues();
        final int count = values.length;
        final CharSequence[] entries = new CharSequence[count];

        for (int i = 0; i < count; i++) {
            int value = Integer.parseInt(values[i].toString());
            entries[i] = EventViewUtils.constructReminderLabel(getActivity(), value, false);
        }

        mSnoozeDelay.setEntries(entries);
    }

    @Override
    public boolean onPreferenceTreeClick(
            PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();
        if (KEY_CLEAR_SEARCH_HISTORY.equals(key)) {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                    Utils.getSearchAuthority(getActivity()),
                    CalendarRecentSuggestionsProvider.MODE);
            suggestions.clearHistory();
            Toast.makeText(getActivity(), R.string.search_history_cleared,
                    Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }


}
