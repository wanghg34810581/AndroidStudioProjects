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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity
        implements Switch.OnCheckedChangeListener {

    public static final String TAG = "SettingsActivity";

    static final String SHARED_PREFS_NAME = "com.ktouch.kcalendar_preferences";

    public static final String KEY_WEEK_START_DAY = "preferences_week_start_day";
    public static final String KEY_DISPLAY_LUNAR = "preferences_display_lunar";
    public static final String KEY_SHOW_CHINESE_FESTIVALS = "preferences_show_chinese_festivals";
    public static final String KEY_SHOW_WESTERN_FESTIVALS = "preferences_show_western_festivals";

    public static final String WEEK_START_SUNDAY = "1";
    public static final String WEEK_START_MONDAY = "2";

    private SharedPreferences mSharedPreferences;

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        mSharedPreferences = this.getSharedPreferences(this);

        initSwitches();
        /*
        SettingPreferences preferences = new SettingPreferences();
        preferences.setArguments(savedInstanceState);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.setting_preferences, preferences);
        ft.commit();
        */
    }

    public void onBackPressed(View view) {
        super.onBackPressed();
    }

    public void onItemPressed(View view) {
        Switch mSwitch = null;
        switch (view.getId()) {
            case R.id.setting_item_week_start_day:
                mSwitch = (Switch) view.findViewById(R.id.setting_switch_week_start_day);
                break;
            case R.id.setting_item_display_lunar:
                mSwitch = (Switch) view.findViewById(R.id.setting_switch_display_lunar);
                break;
            case R.id.setting_item_chinese_festival:
                mSwitch = (Switch) view.findViewById(R.id.setting_switch_chinese_festival);
                break;
            case R.id.setting_item_western_festival:
                mSwitch = (Switch) view.findViewById(R.id.setting_switch_western_festival);
                break;
            case R.id.setting_item_read_birthday:
                mSwitch = (Switch) view.findViewById(R.id.setting_switch_read_birthday);
                break;
        }
        if (mSwitch != null) {
            mSwitch.toggle();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initSwitch(int id, boolean checked) {
        Switch mSwitch = (Switch) findViewById(id);
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setChecked(checked);
    }

    private void initSwitches() {
        String start = mSharedPreferences.getString(KEY_WEEK_START_DAY, WEEK_START_SUNDAY);
        boolean checked = false;
        if (start != null && start.equals(WEEK_START_SUNDAY))
            checked = true;
        initSwitch(R.id.setting_switch_week_start_day, checked);

        checked = mSharedPreferences.getBoolean(KEY_DISPLAY_LUNAR, true);
        initSwitch(R.id.setting_switch_display_lunar, checked);

        checked = mSharedPreferences.getBoolean(KEY_SHOW_CHINESE_FESTIVALS, true);
        initSwitch(R.id.setting_switch_chinese_festival, checked);

        checked = mSharedPreferences.getBoolean(KEY_SHOW_WESTERN_FESTIVALS, true);
        initSwitch(R.id.setting_switch_western_festival, checked);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.setting_switch_week_start_day:
                String week_start_day = WEEK_START_MONDAY;
                if (isChecked) {
                    week_start_day = WEEK_START_SUNDAY;
                }
                mSharedPreferences.edit()
                        .putString(KEY_WEEK_START_DAY, week_start_day)
                        .commit();
                break;
            case R.id.setting_switch_display_lunar:
                mSharedPreferences.edit()
                        .putBoolean(KEY_DISPLAY_LUNAR, isChecked)
                        .commit();
                break;
            case R.id.setting_switch_chinese_festival:
                mSharedPreferences.edit()
                        .putBoolean(KEY_SHOW_CHINESE_FESTIVALS, isChecked)
                        .commit();
                break;
            case R.id.setting_switch_western_festival:
                mSharedPreferences.edit()
                        .putBoolean(KEY_SHOW_WESTERN_FESTIVALS, isChecked)
                        .commit();
                break;
        }
    }
}
