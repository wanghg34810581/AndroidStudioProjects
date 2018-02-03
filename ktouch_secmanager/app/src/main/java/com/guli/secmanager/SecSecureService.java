package com.guli.secmanager;

import android.content.Intent;
import android.util.Log;

import tmsdk.common.TMSService;

/**
 * Created by yujie on 16-4-14.
 */
public class SecSecureService extends TMSService {
    public static final String TAG = "SecSecureService";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate Enter ...");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG, "onStart Enter ...");
    }
}
