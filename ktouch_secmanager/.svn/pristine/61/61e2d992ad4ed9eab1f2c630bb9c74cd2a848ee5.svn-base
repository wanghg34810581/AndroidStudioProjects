package com.guli.secmanager.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.text.format.Time;

// from tencent tmsdk demo code
public class LogUtil {
    public static final String TAG = "LogUtil";
    private static final String DEFAULT_FILE = "tencnet_sdk_demo_default.log";

    public static boolean writeToDefaultFile(String aLog) {
        File SDfiles = Environment.getExternalStorageDirectory(); // SDCards
        if (!SDfiles.canWrite()) {
            return false;
        }
        return writeLog(SDfiles.getAbsolutePath() + File.separator + DEFAULT_FILE, aLog, false);
    }

    public static boolean writeToSpecialFile(String aFileName, String aLog, boolean timestamp) {
        File SDfiles = Environment.getExternalStorageDirectory(); // SDCards
        if (!SDfiles.canWrite()) {
            return false;
        }
        return writeLog(SDfiles.getAbsolutePath() + File.separator + aFileName, aLog, timestamp);
    }

    @SuppressWarnings("deprecation")
    public static boolean writeLog(String aLogPath, String aLog, boolean tt) {
        FileWriter fw = null;
        try {
            File logFile = new File(aLogPath);
            try {
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                fw = new FileWriter(logFile, true);
            } catch (Exception e) {
            }
            if (tt) {
                Time time = new Time();
                time.setToNow();
                String now = "[" + time.year+"-"+(time.month+1)+"-"+time.monthDay+" "+time.hour+":"+time.minute+":"+time.second + "]";
                fw.write(now);
            }
            fw.write(aLog + "\n");
            fw.flush();
        } catch (Exception e) {
            return false;
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}