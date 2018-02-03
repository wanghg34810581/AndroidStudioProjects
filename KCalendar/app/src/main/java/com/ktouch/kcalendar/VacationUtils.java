package com.ktouch.kcalendar;

import android.database.Cursor;
import android.util.Log;

import com.ktouch.kcalendar.contentprovider.KCalendarProvider;
import com.ktouch.kcalendar.vacation.Vacation;

import java.util.HashMap;

/**
 * Created by zhang-yi on 2016/6/16 0016.
 */
public class VacationUtils {
    private static final String TAG = "VacationUtils";

    private static KCalendarProvider sProvider = new KCalendarProvider();

    public static final int EVENTS_MODE = 0;
    public static final int VACATION_MODE = 1;
    public static int CONTENT_MODE = EVENTS_MODE;
    public static HashMap<String, Vacation> mVacationMap;

    public static HashMap<String, Vacation> initVacationMap(Cursor c) {
        Log.d(TAG, "-=-=-= initVacationMap c = " + c);
        HashMap<String, Vacation> vacationMap = new HashMap<String, Vacation>();
        if (c != null && c.getCount() > 0) {

            while (c.moveToNext()) {
                Vacation vacation = new Vacation(
                        c.getInt(c.getColumnIndex(KCalendarProvider.Columns.YEAR)),
                        c.getInt(c.getColumnIndex(KCalendarProvider.Columns.MONTH)),
                        c.getInt(c.getColumnIndex(KCalendarProvider.Columns.DAY)),
                        c.getInt(c.getColumnIndex(KCalendarProvider.Columns.DATE)),
                        c.getInt(c.getColumnIndex(KCalendarProvider.Columns.STATE)));
                String key = vacation.mYear+ "-" + vacation.mMonth + "-" + vacation.mDay;
                Log.d(TAG, "-=-=-= initVacationMap key = " + key);
                Log.d(TAG, "-=-=-= initVacationMap day = " + vacation.mDay);
                vacationMap.put(key, vacation);
            }
        }
        return vacationMap;
    }
}
