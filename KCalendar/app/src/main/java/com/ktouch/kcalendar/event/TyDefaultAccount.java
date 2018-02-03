/*
 * TIANYURD kongbb add for PROD100649786, 
 * this is used to check calendar account and write default account
 * 
 */
package com.ktouch.kcalendar.event;

import java.util.TimeZone;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

public class TyDefaultAccount {
    private static final String TAG = "TyDefaultAccount";
    //TIANYURD:weihl 20131210 modify for CQWB00008670 begin
    /*
    public static final String LOCAL_CALENDER_ACCOUNT_NAME = "default@k-touch.cn";
    public static final String LOCAL_CALENDER_ACCOUNT_TYPE = "com.k-touch.default";
    */
    public static final String LOCAL_CALENDER_ACCOUNT_NAME = "PC Sync";
    public static final String LOCAL_CALENDER_ACCOUNT_TYPE = "com.ktouch.kcalendar";
    //TIANYURD:weihl 20131210 modify end
    private static final int MAX_TIME_FOR_SYNC_IN_MINS = 20;

    public static void maybeCreateLocalCalendar(Context context) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, "-9215145");
        //TIANYURD:weihl 20131210 modify for CQWB00008670 begin
        /*
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Default");
        */
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "PC Sync");
        //TIANYURD:weihl 20131210 modify end
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, LOCAL_CALENDER_ACCOUNT_NAME);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.NAME, "Local");
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, LOCAL_CALENDER_ACCOUNT_NAME);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, LOCAL_CALENDER_ACCOUNT_TYPE);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());

        Uri resultUri = context.getContentResolver().insert(
            asSyncAdapter(CalendarContract.Calendars.CONTENT_URI, LOCAL_CALENDER_ACCOUNT_NAME,
            LOCAL_CALENDER_ACCOUNT_TYPE), values);
    }

    static Uri asSyncAdapter(Uri uri, String account, String accountType) {
        Uri accountUri = uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,
                        "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
        Log.e(TAG,"kbb--accountUri is: " + accountUri);
        return accountUri;
    }

    public static void initCalendar(Context context) {
        Uri uri_Calendars = Calendars.CONTENT_URI;
        //hunan 2016-01-14 PROD104140014 has two PC sync
        String selection = Calendars.CALENDAR_DISPLAY_NAME + "= 'PC Sync' ";
        //String selection = Calendars.NAME + "= 'Local' " + "AND "
                //TIANYURD:weihl 20131210 modify for CQWB00008670 begin
                //+ Calendars.CALENDAR_DISPLAY_NAME + "= 'Default' ";
       //         + Calendars.CALENDAR_DISPLAY_NAME + "= 'PC Sync' ";
                //TIANYURD:weihl 20131210 modify end
        String[] selectionArgs = null;
        String sortOrder = null;
        String[] projection = new String[] { Calendars.NAME };
        Cursor cursor = context.getContentResolver().query(uri_Calendars,
                                projection, selection, selectionArgs, sortOrder);
        try {
            if (cursor != null) {
                if (cursor.getCount() == 0) {
                    Log.v(TAG, "local account is zero, so start to create it");
                    maybeCreateLocalCalendar(context);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

}
