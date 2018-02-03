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

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.provider.CalendarContract.Attendees;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
import com.ktouch.kcalendar.contentprovider.KCalendarProvider;
import com.ktouch.kcalendar.event.EditEventHelper;

import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

public class AgendaAdapter extends ResourceCursorAdapter {
    private final String mNoTitleLabel;
    private final Resources mResources;
    // Note: Formatter is not thread safe. Fine for now as it is only used by the main thread.
    private final Formatter mFormatter;
    private final StringBuilder mStringBuilder;
    private int[] mMarkAsIcons;


    private final Runnable mTZUpdater = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    static class ViewHolder {
        /* Event */
        TextView theDay;
        TextView title;
        TextView when;
        View selectedMarker;
        LinearLayout textContainer;
        long instanceId;
        ImageView icon;
        long startTimeMilli;
        boolean allDay;
        boolean grayed;
        int julianDay;
        CheckBox selectedForSharing;
    }

    public AgendaAdapter(Context context, int resource) {
        super(context, resource, null);

        mResources = context.getResources();
        mNoTitleLabel = mResources.getString(R.string.no_title_label);
        mStringBuilder = new StringBuilder(50);
        mFormatter = new Formatter(mStringBuilder, Locale.getDefault());

        mMarkAsIcons = Utils.loadDrawableArray(mResources, R.array.agenda_mark_as_icons);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = null;

        // Listview may get confused and pass in a different type of view since
        // we keep shifting data around. Not a big problem.
        Object tag = view.getTag();
        if (tag instanceof ViewHolder) {
            holder = (ViewHolder) view.getTag();
        }

        if (holder == null) {
            holder = new ViewHolder();
            view.setTag(holder);
            holder.theDay = (TextView) view.findViewById(R.id.theDay);
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.when = (TextView) view.findViewById(R.id.when);
            holder.textContainer = (LinearLayout)
                    view.findViewById(R.id.agenda_item_text_container);
            holder.selectedMarker = view.findViewById(R.id.selected_marker);
            holder.icon = (ImageView) view.findViewById(R.id.agenda_item_icon);
            holder.selectedForSharing = (CheckBox) view.findViewById(R.id.shareCheckbox);
        }

        holder.startTimeMilli = cursor.getLong(AgendaWindowAdapter.INDEX_BEGIN);
        // Fade text if event was declined and set the color chip mode (response
        boolean allDay = cursor.getInt(AgendaWindowAdapter.INDEX_ALL_DAY) != 0;
        holder.allDay = allDay;

        TextView title = holder.title;
        TextView when = holder.when;
        TextView theDay = holder.theDay;

        holder.instanceId = cursor.getLong(AgendaWindowAdapter.INDEX_INSTANCE_ID);

        String description = cursor.getString(AgendaWindowAdapter.INDEX_DESCRIPTION);
        int markAs = 0;
        if (description != null && description.length() == 1) {
            try {
                markAs = Integer.parseInt(description);
            } catch (Exception e) {

            }
        }

//        int markAs = EditEventHelper.queryKCalendar(context, holder.instanceId)[KCalendarProvider.KCALENDAR_PROVIDER_INDEX_MARK_AS];
//        Cursor c = context.getContentResolver().query(KCalendarProvider.Columns.CONTENT_URI , null, null,  null, null);
//        int markAs = 0;
//        try {
//            c.moveToFirst();
//            while (c.moveToNext()){
//                int id = c.getColumnIndex(KCalendarProvider.Columns._ID);
//                int markAsColumn = c.getColumnIndex(KCalendarProvider.Columns.MARK_AS);
//                 if(c.getLong(id) == holder.instanceId) {
//                    markAs = c.getInt(markAsColumn);
//                    break;
//                }
//            }
//        } finally {
//            c.close();
//        }

        // What
        String titleString = cursor.getString(AgendaWindowAdapter.INDEX_TITLE);
        if (titleString == null || titleString.length() == 0) {
            titleString = mNoTitleLabel;
        }
        title.setText(titleString);

        // When
        long begin = cursor.getLong(AgendaWindowAdapter.INDEX_BEGIN);
        long end = cursor.getLong(AgendaWindowAdapter.INDEX_END);
        String eventTz = cursor.getString(AgendaWindowAdapter.INDEX_TIME_ZONE);
        int flags = 0;
        String whenString;
        // It's difficult to update all the adapters so just query this each
        // time we need to build the view.
        String tzString = Utils.getTimeZone(context, mTZUpdater);
        if (allDay) {
            tzString = Time.TIMEZONE_UTC;
            if (markAs == 0) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageResource(mMarkAsIcons[0]);
            } else {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageResource(mMarkAsIcons[markAs]);
            }
        } else {
            flags = DateUtils.FORMAT_SHOW_TIME;

        }

        if (markAs == 0 && !allDay) {
            holder.icon.setVisibility(View.GONE);
        } else {
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageResource(mMarkAsIcons[markAs]);
        }

        if (DateFormat.is24HourFormat(context)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        mStringBuilder.setLength(0);
        whenString = DateUtils.formatDateRange(context, mFormatter, begin, end, flags, tzString)
                .toString();
        if (!allDay && !TextUtils.equals(tzString, eventTz)) {
            String displayName;
            // Figure out if this is in DST
            Time date = new Time(tzString);
            date.set(begin);

            TimeZone tz = TimeZone.getTimeZone(tzString);
            if (tz == null || tz.getID().equals("GMT")) {
                displayName = tzString;
            } else {
                displayName = tz.getDisplayName(date.isDst != 0, TimeZone.SHORT);
            }
            whenString += " (" + displayName + ")";
        }
        when.setText(whenString);

        if (theDay != null) {
            String day = DateUtils.formatDateTime(context, begin, DateUtils.FORMAT_SHOW_DATE);
            theDay.setText(day);
            if (allDay) {
                when.setText(R.string.all_day_title);
            }
        }

    }
}

