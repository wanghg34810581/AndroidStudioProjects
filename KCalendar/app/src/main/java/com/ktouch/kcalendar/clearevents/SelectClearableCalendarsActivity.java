/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.ktouch.kcalendar.clearevents;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ktouch.kcalendar.AbstractCalendarActivity;
import com.ktouch.kcalendar.CalendarController;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;

///M:#ClearAllEvents#
public class SelectClearableCalendarsActivity extends AbstractCalendarActivity {
    private SelectClearableCalendarsFragment mFragment;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.simple_frame_layout);

        mFragment = (SelectClearableCalendarsFragment) getFragmentManager().findFragmentById(
                R.id.main_frame);

        if (mFragment == null) {
            mFragment = new SelectClearableCalendarsFragment(R.layout.calendar_sync_item);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_frame, mFragment);
            ft.show(mFragment);
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar()
                .setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            Utils.returnToCalendarHome(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // To remove its CalendarController instance if exists
        CalendarController.removeInstance(this);
    }
}
