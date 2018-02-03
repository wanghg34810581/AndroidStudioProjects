package com.ktouch.kcalendar.contentprovider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.format.Time;
import android.util.Log;

import com.ktouch.kcalendar.VacationUtils;
import com.ktouch.kcalendar.vacation.Vacation;

import java.util.ArrayList;
import java.util.Calendar;

public class KCalendarProviderHelper {
    Context mContext;
    private static final String TAG = "KCalendarProviderHelper";
    ArrayList<Vacation> mVacationArray = new ArrayList<Vacation>();

    public KCalendarProviderHelper(Context context) {
        mContext = context;
    }

    public void initDatabase() {
        readFromDatabase();
        if (mVacationArray.size() == 0) {
            vacationFactory(2016, 1, 1, 1);
            vacationFactory(2016, 1, 2, 1);
            vacationFactory(2016, 1, 3, 1);
            vacationFactory(2016, 2, 6, 2);
            vacationFactory(2016, 2, 7, 1);
            vacationFactory(2016, 2, 8, 1);
            vacationFactory(2016, 2, 9, 1);
            vacationFactory(2016, 2, 10, 1);
            vacationFactory(2016, 2, 11, 1);
            vacationFactory(2016, 2, 12, 1);
            vacationFactory(2016, 2, 13, 1);
            vacationFactory(2016, 2, 14, 2);
            vacationFactory(2016, 4, 2, 1);
            vacationFactory(2016, 4, 3, 1);
            vacationFactory(2016, 4, 4, 1);
            vacationFactory(2016, 4, 30, 1);
            vacationFactory(2016, 5, 1, 1);
            vacationFactory(2016, 5, 2, 1);
            vacationFactory(2016, 6, 9, 1);
            vacationFactory(2016, 6, 10, 1);
            vacationFactory(2016, 6, 11, 1);
            vacationFactory(2016, 6, 12, 2);
            vacationFactory(2016, 9, 15, 1);
            vacationFactory(2016, 9, 16, 1);
            vacationFactory(2016, 9, 17, 1);
            vacationFactory(2016, 9, 18, 2);
            vacationFactory(2016, 10, 1, 1);
            vacationFactory(2016, 10, 2, 1);
            vacationFactory(2016, 10, 3, 1);
            vacationFactory(2016, 10, 4, 1);
            vacationFactory(2016, 10, 5, 1);
            vacationFactory(2016, 10, 6, 1);
            vacationFactory(2016, 10, 7, 1);
            vacationFactory(2016, 10, 8, 2);
            vacationFactory(2016, 10, 9, 2);
            vacationFactory(2016, 4, 2, 3);
            vacationFactory(2016, 4, 3, 3);
            vacationFactory(2016, 4, 4, 3);
            vacationFactory(2016, 4, 30, 3);
            vacationFactory(2016, 5, 1, 3);
            vacationFactory(2016, 5, 2, 3);
            vacationFactory(2016, 6, 9, 3);
            vacationFactory(2016, 6, 10, 3);
            vacationFactory(2016, 6, 11, 3);
            vacationFactory(2016, 6, 12, 3);
            vacationFactory(2016, 6, 30, 3);
            vacationFactory(2016, 8, 31, 3);
            vacationFactory(2016, 12, 31, 3);
        }
        mVacationArray.clear();
        readFromDatabase();
        for (int i = 0; i< mVacationArray.size(); i++){
            Vacation v = mVacationArray.get(i);
            Log.e(TAG, "---->>> i "+i+" <<<---"+v.mYear+"-"+v.mMonth+"-"+v.mDay+", date = "+v.mDate+", state = "+v.mState);
        }
    }

    public void vacationFactory(int year, int month, int day, int state) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        saveKCalendarVacation(calendar.getTimeInMillis(), state, -1, false);
    }

    public boolean readFromDatabase() {
        Cursor c = mContext.getContentResolver().query(KCalendarProvider.Columns.CONTENT_URI, null, null, null, null);
        try {
//            c.moveToFirst();
            while (c.moveToNext()) {
                int year = c.getInt(c.getColumnIndex(KCalendarProvider.Columns.YEAR));
                int month = c.getInt(c.getColumnIndex(KCalendarProvider.Columns.MONTH));
                int day = c.getInt(c.getColumnIndex(KCalendarProvider.Columns.DAY));
                long date = c.getLong(c.getColumnIndex(KCalendarProvider.Columns.DATE));
                int state = c.getInt(c.getColumnIndex(KCalendarProvider.Columns.STATE));
                mVacationArray.add(new Vacation(year, month, day, date, state));
            }
        } finally {
            c.close();
        }
        return true;
    }

    public Vacation readFromDatabaseByTime(Time time) {
        String where = KCalendarProvider.Columns.YEAR + "=" + time.year
                + " AND " + KCalendarProvider.Columns.MONTH + "=" + time.month
                + " AND " + KCalendarProvider.Columns.DAY + "=" + time.monthDay;
        if (VacationUtils.CONTENT_MODE == VacationUtils.EVENTS_MODE) {
            where += " AND " + KCalendarProvider.Columns.STATE + " in (1,2)";
        } else {
            where += " AND " + KCalendarProvider.Columns.STATE + "=3";
        }
        Cursor c = mContext.getContentResolver().query(KCalendarProvider.Columns.CONTENT_URI, null, where, null, null);
        Vacation vacation = null;
        try {
            if (c.moveToFirst()) {
                int year = c.getInt(c.getColumnIndex(KCalendarProvider.Columns.YEAR));
                int month = c.getInt(c.getColumnIndex(KCalendarProvider.Columns.MONTH));
                int day = c.getInt(c.getColumnIndex(KCalendarProvider.Columns.DAY));
                long date = c.getLong(c.getColumnIndex(KCalendarProvider.Columns.DATE));
                int state = c.getInt(c.getColumnIndex(KCalendarProvider.Columns.STATE));
                vacation = new Vacation(year, month, day, date, state);
            }
        } finally {
            c.close();
        }
        return vacation;
    }

    public boolean saveKCalendarVacation(long date, int state, long vacationId, boolean newVacation) {

        ContentValues values = new ContentValues();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        values.put(KCalendarProvider.Columns.YEAR, year);
        values.put(KCalendarProvider.Columns.MONTH, month);
        values.put(KCalendarProvider.Columns.DAY, day);
        values.put(KCalendarProvider.Columns.DATE, date);
        values.put(KCalendarProvider.Columns.STATE, state);

        mContext.getContentResolver().insert(KCalendarProvider.Columns.CONTENT_URI, values);

        return true;
    }
}
