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
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgendaRecentAdapter extends SimpleAdapter {

    public static final String TAG = "AgendaRecentAdapter";

    private Context mContext;
    private int mResource;
    private LayoutInflater mInflater;
    private int[] mMarkAsIcons;
    private final Formatter mFormatter;
    private final StringBuilder mStringBuilder;
    private java.util.List<? extends Map<String, ?>> mData;

    public AgendaRecentAdapter(Context context,
                           List<? extends Map<String, ?>> data, int resource) {
        super(context, data, resource, null, null);
        mContext = context;
        this.mResource = resource;
        this.mData = data;
        mInflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        mStringBuilder = new StringBuilder(50);
        mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
        mMarkAsIcons = Utils.loadDrawableArray(context.getResources(), R.array.agenda_mark_as_icons);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(mResource, null);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.icon = (ImageView) convertView.findViewById(R.id.agenda_item_icon);
            holder.theDay = (TextView) convertView.findViewById(R.id.theDay);
            holder.when = (TextView) convertView.findViewById(R.id.when);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(mData.get(position).get(CalendarContract.Instances.TITLE).toString());
        int isAllDay = (Integer) mData.get(position).get(CalendarContract.Instances.ALL_DAY);
        boolean allDay = isAllDay == 1 ? true : false;

        Object obj = mData.get(position).get(CalendarContract.Instances.DESCRIPTION);
        String description = obj == null ? null : obj.toString();
        int markAs = 0;
        if (description != null && description.length() == 1) {
            try {
                markAs = Integer.parseInt(description);
            } catch (Exception e) {

            }
        }

        // When
        long begin = (Long) mData.get(position).get(CalendarContract.Instances.BEGIN);
        long end = (Long) mData.get(position).get(CalendarContract.Instances.END);
        //String eventTz = mData.get(position).get(CalendarContract.Instances.EVENT_TIMEZONE).toString();
        int flags = 0;
        String whenString;
        // It's difficult to update all the adapters so just query this each
        // time we need to build the view.
        String tzString = Utils.getTimeZone(mContext, mTZUpdater);
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

        if (DateFormat.is24HourFormat(mContext)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        mStringBuilder.setLength(0);
        whenString = DateUtils.formatDateRange(mContext, mFormatter, begin, end, flags, tzString)
                .toString();
        /*if (!allDay && !TextUtils.equals(tzString, eventTz)) {
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
        }*/
        holder.when.setText(whenString);

        if (allDay) {
            holder.when.setText(R.string.all_day_title);
        }

        holder.theDay.setText(DateUtils.formatDateTime(mContext, begin, DateUtils.FORMAT_SHOW_DATE));

        return convertView;
    }

    class ViewHolder {
        public TextView title;
        public TextView when;
        public ImageView icon;
        public TextView theDay;
    }

    private final Runnable mTZUpdater = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };
}