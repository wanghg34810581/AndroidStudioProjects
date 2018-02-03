package com.guli.secmanager.VirusScan;

import android.widget.ImageView;
import android.widget.TextView;

import com.guli.secmanager.GarbageClean.ParentObj;
import com.guli.secmanager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shenyan on 2016/4/15.
 */
public class Utils {
    public static final int  PARENT_MAX = 3;

    public static final int CHECKED_ICON = 1;
    public static final int UNCHECKED_ICON = 2;
    public static final int WARING_ICON = 3;

    public static final int SCAN_RESTART_ONE_MSG = 1;
    public static final int SCAN_COMPLETE_ALL_MSG = 2;
    public static final int START_ALL_SAFE_MSG = 3;
    public static final int START_DANGER_INFO_MSG = 4;
    public static final int UPDATE_PROGRESS_VIEW_MSG = 5;
    public static final int UPDATE_SDCARD_SCAN_PRO_MSG = 6;
    public static final int UPDATE_PARENT_VIEW_MSG = 7;
    public static final int UPDATE_SDCARD_PATH_INFO_MSG = 8;
    public static final int SCAN_CONTINUE_MSG = 9;
    public static final int SCAN_PAUSE_MSG = 10;
    public static final int SCAN_START_ROTATE_MSG = 11;
    public static final int SCAN_STOP_ROTATE_MSG = 12;
    public static final int SCAN_THREAD_EXIT_MSG = 13;
    public static final int SCAN_DELETE_PACKAGE_MSG = 14;

    public static final int BUG_SCAN = 0;
    public static final int SOFT_SCAN = 1;
    //public static final int CLOUD_SCAN = 2;
    public static final int SDCARD_SCAN =2;

    public static final int PROG_BAR_MAX = 210;
    public static final int SD_BAR_MAX = 200;
    public static final int SCAN_COMPLETE_MAX = 100;
    public static final int STATUS_BAR_HEIGHT = 25;
    public static final int BUG_SCAN_BASE = 30;

    public static final int BUG_SCAN_MAX = 5;
    public static final int SOFT_SCAN_MAX = 94;
    //public static final int CLOUD_SCAN_MAX = 2;
    public static final int SDCARD_SCAN_MAX = 1;
}
