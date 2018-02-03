package com.android.gnotes;

import android.net.Uri;
import android.provider.BaseColumns;

class Notes implements BaseColumns {

    private Notes() {
    }

    public static final String VIEW_NAME_INDEX = "view_name_index";
    public final static int MAIN_VIEW = 0;
    public final static int DELETE_VIEW = 1;
    public final static int ADD_VIEW = 2;
    public final static int EDIT_VIEW = 3;
    //TY zhencc add begin
    public final static int NOTEREADING_DELETE_TOKEN = 4;
	//TY zhencc add end
    
    //TY zhencc 20160829 add for PROD104174151 begin
    public final static int NOTEEDITING_DELETE_TOKEN = 5;
    //TY zhencc 20160829 add for PROD104174151 end
    
    public static final String AUTHORITY = "com.android.gnotes.NotePad";
    public static final String TABLE_NAME = "notes";

    private static final String SCHEME = "content://";

    private static final String PATH_NOTES = "/notes";

    private static final String PATH_NOTE_ID = "/notes/";

    private static final String NOTE_URI = SCHEME + AUTHORITY;

    public static final int NOTE_ID_PATH_POSITION = 1;

    public static final Uri CONTENT_URI = Uri.parse(NOTE_URI + PATH_NOTES);

    public static final Uri CONTENT_ID_URI_BASE = Uri.parse(NOTE_URI + PATH_NOTE_ID);

    public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(NOTE_URI + PATH_NOTE_ID + "/#");

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

    public static String sSortOrder = "modified DESC";

    public static final String COLUMN_NAME_TITLE = "title";

    public static final String COLUMN_NAME_NOTE = "note";
    
    //TY zhencc add for search begin
    public static final String COLUMN_NAME_SEARCH_NOTE = "search_note";
    //TY zhencc add for search end

    public static final String COLUMN_NAME_CREATE_DATE = "created";

    public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";

    //chendy 20131108 add for PROD102320952 start
    public static final String COLUMN_NAME_GROUP = "notegroup";
    //chendy 20131108 add for PROD102320952 end

    //yuhf add for change skin function begin
    public static final String COLUMN_NAME_SKIN_INDEX = "skinindex";
    //yuhf add for change skin function end

    //TY zhencc delete begin
    //public static int NOTESLIST_DELETE_TOKEN = 0;
    //public static int NOTEREADING_DELETE_TOKEN = 1;
    //public static int NOTESLIST_DELETE_ALL_TOKEN = 2;
	//TY zhencc delete end

    public static final String[] PROJECTION = new String[] {
        Notes._ID,
        Notes.COLUMN_NAME_TITLE,
        Notes.COLUMN_NAME_NOTE, 
        Notes.COLUMN_NAME_SEARCH_NOTE, //TY zhencc add for search
        Notes.COLUMN_NAME_MODIFICATION_DATE,
        Notes.COLUMN_NAME_CREATE_DATE,
        Notes.COLUMN_NAME_SKIN_INDEX //yuhf add for change skin function end		
    };
}