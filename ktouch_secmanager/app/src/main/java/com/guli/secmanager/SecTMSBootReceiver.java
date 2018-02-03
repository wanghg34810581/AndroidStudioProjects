package com.guli.secmanager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import tmsdk.common.TMSBootReceiver;
import com.guli.secmanager.UpdateCenter.UpdateService;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.flowmonitor.FlowAutoCorrectService;
import com.guli.secmanager.flowmonitor.FlowMonitorService;

/**
 * Created by yujie on 16-4-14.
 */
public class SecTMSBootReceiver extends TMSBootReceiver {
    public static final String TAG = "SecTMSBootReceiver";
    @Override
    public void doOnRecv(final Context context, Intent intent) {
        Log.i(TAG, "doOnRecv called ...");
        super.doOnRecv(context, intent);

        // start updateService
        if (!UpdateService.isServiceAlarmOn(context)) {
            Log.i(TAG, "--->>> Start UpdateService Timer!");
            UpdateService.setServiceAlarm(context, true);
        }
        if(!FlowAutoCorrectService.isServiceAlarmOn(context)){
            FlowAutoCorrectService.setAlarm(context,true);
        }

        context.startService(new Intent(context, FlowMonitorService.class));
    }
}
