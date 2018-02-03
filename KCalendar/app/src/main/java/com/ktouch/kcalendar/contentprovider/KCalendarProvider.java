package com.ktouch.kcalendar.contentprovider;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class KCalendarProvider extends ContentProvider {
    private SQLiteDatabase sqlDB;
    private DatabaseHelper    dbHelper;
    private static final String  DATABASE_NAME = "KCalendar.db";
    private static final int  DATABASE_VERSION= 1;
    private static final String TABLE_NAME= "KCalendar";
    private static final String TAG = "KCalendarProvider";

    public static final String AUTHORITY  = "com.ktouch.kcalendar.KCalendarProvider";

    public static final int KCALENDAR_PROVIDER_INDEX_RECURRENCE = 0;
    public static final int KCALENDAR_PROVIDER_INDEX_MARK_AS = 1;

    // BaseColumn类中已经包含了 _id字段
    public static final class Columns implements BaseColumns {
        public static final Uri CONTENT_URI  = Uri.parse("content://com.ktouch.kcalendar.KCalendarProvider");

        public static final String  _ID  = "_id";
        public static final String  YEAR  = "year";
        public static final String  MONTH  = "month";
        public static final String  DAY  = "day";
        public static final String  DATE  = "date";
        public static final String  STATE  = "state";   // 0,normal; 1,vacation; 2,work; 3,custom;

        public static final String[] allColumns = {_ID, YEAR, MONTH, DAY, DATE, STATE};
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("Create table " + TABLE_NAME + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, year INTEGER, month INTEGER, day INTEGER, date LONG, state int);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public int delete(Uri uri, String s, String[] as) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        if(dbHelper==null){
            dbHelper = new DatabaseHelper(getContext());
        }
        sqlDB = dbHelper.getWritableDatabase();
        long rowId = sqlDB.insert(TABLE_NAME, "", contentvalues);
        if (rowId > 0) {
            Uri rowUri = ContentUris.appendId(Columns.CONTENT_URI.buildUpon(), rowId).build();
            getContext().getContentResolver().notifyChange(rowUri, null);
            return rowUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return (dbHelper == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        qb.setTables(TABLE_NAME);
        Cursor c = qb.query(db, projection, selection, null, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
        return 0;
    }
}
