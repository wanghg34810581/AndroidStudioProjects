
package com.android.gnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class NotesDatabase {
    public static class NotesDatabaseHelper extends SQLiteOpenHelper {
        //private static final String DATABASE_NAME = "historydata.db";

        private static final String DATABASE_NAME = "note_pad.db";
        private static final String DATABASE_TABLE = "note";
        private static final int DATABASE_VERSION = 2;
        private static final String TAG = "NotesDatabaseHelper";

        public NotesDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE + ";");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " ("
                       + Notes._ID + " INTEGER PRIMARY KEY,"
                       + Notes.COLUMN_NAME_TITLE + " TEXT,"
                       + Notes.COLUMN_NAME_NOTE + " TEXT,"
                       + Notes.COLUMN_NAME_SEARCH_NOTE + " TEXT," //TY zhencc add for search
                       + Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                       + Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
                       + Notes.COLUMN_NAME_SKIN_INDEX + " INTEGER" //yuhf add for change skin function
                       +  ");");
        }

        @Override
        public void onOpen(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < newVersion) {
                Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

                db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE + ";");
                onCreate(db);
            }
        }
    }

    private NotesDatabaseHelper mHelper;
    private SQLiteDatabase	mDb;
    private static final String TAG = "NotesDatabase";
    public NotesDatabase(Context context) {
        // TODO Auto-generated constructor stub
        mHelper = new NotesDatabaseHelper(context);
        mDb = mHelper.getWritableDatabase();
    }

    public void close() {
        mDb.close();
        mHelper.close();
    }

    public Cursor query(String selection) {
        try {
            String table = NotesDatabaseHelper.DATABASE_TABLE;
            //SQLiteDatabase db = mHelper.getReadableDatabase();
            Cursor cursor=mDb.query(table, Notes.PROJECTION, selection, null,
                null, null, Notes.sSortOrder);
            //cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        } catch(SQLException e) {
            Log.d(TAG, "query exception:" + e.toString());
            return null;
        }
    }

    public long insert(ContentValues values) {
        try {
            Log.d(TAG, "insert");
            String table = NotesDatabaseHelper.DATABASE_TABLE;
            //SQLiteDatabase db = mHelper.getWritableDatabase();
            return mDb.insert(table, null, values);
        } catch(SQLException e) {
            Log.d(TAG, "insert exception:" + e.toString());
            return 0;
        }
    }

    public int delete(String selection) {
        try {
            String table = NotesDatabaseHelper.DATABASE_TABLE;
            //SQLiteDatabase db = mHelper.getWritableDatabase();
            return mDb.delete(table, selection, null);
        } catch(SQLException e) {
            Log.d(TAG, "delete exception:" + e.toString());
            return 0;
        }
    }

    public int update(ContentValues values, String selection) {
        try {
            String table = NotesDatabaseHelper.DATABASE_TABLE;
            //SQLiteDatabase db = mHelper.getWritableDatabase();
            return mDb.update(table, values, selection, null);
        } catch(SQLException e) {
            Log.d(TAG, "update exception:" + e.toString());
            return 0;
        }
    }
}
