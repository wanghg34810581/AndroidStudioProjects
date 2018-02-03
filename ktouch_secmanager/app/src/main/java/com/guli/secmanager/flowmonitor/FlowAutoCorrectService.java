package com.guli.secmanager.flowmonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;

import com.guli.secmanager.Utils.ShareUtil;

import java.util.Calendar;

import tmsdk.bg.module.network.ITrafficCorrectionListener;
import tmsdk.bg.module.network.ProfileInfo;
import tmsdk.common.IDualPhoneInfoFetcher;

/**
 * Created by wangqch on 16-5-9.
 */
public class FlowAutoCorrectService extends Service {

    public String mquerycode;
    public String mqueryport;
    private SmsReceiver receiver = new SmsReceiver();
    private static  String TAG="FlowAutoCorrectService";

    public static void setAlarm(Context context,boolean isOpen) {
        Log.d(TAG, "now is on setAlarm isOpen=" + isOpen);
        Intent intent = new Intent(context, FlowAutoCorrectService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOpen) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,getTargetTime(),AlarmManager.INTERVAL_DAY,pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static long getTargetTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND,0);
        long targetTime = calendar.getTimeInMillis();
        long systemTime = System.currentTimeMillis();
        if(systemTime > targetTime){
            calendar.add(Calendar.DAY_OF_MONTH,1);
            targetTime = calendar.getTimeInMillis();
        }
        return targetTime;
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent intent = new Intent(context, FlowAutoCorrectService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"now is on OnCreate");

    }

    public void init(){
        FlowCorrectWrapper.getInstance().init(getApplicationContext());

        FlowCorrectWrapper.getInstance().setTrafficCorrectionListener(new ITrafficCorrectionListener() {

            @Override
            public void onNeedSmsCorrection(int simIndex, String queryCode, String queryPort) {
                android.util.Log.v(TAG, "onNeedSmsCorrection--simIndex:[" + simIndex + "]--queryCode:[" +
                        queryCode + "]queryPort:[" + queryPort + "]");
                //receiver.setHandle(uiHandler, queryPort);
                SmsReceiver.han = uiHandler;
                SmsReceiver.mQueryPort = queryPort;
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(queryPort, null, queryCode, null, null);

                mquerycode = queryCode;
                mqueryport = queryPort;
            }

            @Override
            public void onTrafficInfoNotify(int simIndex, int trafficClass, int subClass, int kBytes) {
                android.util.Log.v(TAG, "wangqch onTrafficNotify-");
                stopSelf();

            }

            @Override
            public void onProfileNotify(int i, ProfileInfo profileInfo) {
                super.onProfileNotify(i, profileInfo);
                Log.d(TAG, "now is in onProfileNotify");
                SharedPreferences sharedPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
                String closingDay = sharedPreferences.getString(ShareUtil.ACCOUNT_DATE, "1");
                int result = FlowCorrectWrapper.getInstance().setConfig(i,
                        String.valueOf(profileInfo.province), String.valueOf(profileInfo.city), profileInfo.carry, String.valueOf(profileInfo.brand), Integer.parseInt(closingDay));
                Log.d(TAG, "result=" + String.valueOf(profileInfo.province) + " " + String.valueOf(profileInfo.city) + " " + profileInfo.carry + " " + String.valueOf(profileInfo.brand));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ShareUtil.PROVINCE, String.valueOf(profileInfo.province));
                editor.putString(ShareUtil.CITY, String.valueOf(profileInfo.city));
                editor.putString(ShareUtil.OPERATOR, profileInfo.carry);
                editor.putString(ShareUtil.BRAND, String.valueOf(profileInfo.brand));
                editor.commit();
                int retCode = FlowCorrectWrapper.getInstance().startCorrection(IDualPhoneInfoFetcher.FIRST_SIM_INDEX);

            }

            @Override
            public void onError(int simIndex, int errorCode) {

                if (IDualPhoneInfoFetcher.FIRST_SIM_INDEX == simIndex) {
                    // mTVSim1Detail.setText(strState);
                } else if (IDualPhoneInfoFetcher.SECOND_SIM_INDEX == simIndex) {
                    // mTVSim2Detail.setText(strState);
                }
                android.util.Log.v(TAG, "onError--simIndex:[" + simIndex + "]errorCode:[" + errorCode + "]");
            }
        });


    }
    public void startanalysisSMS(final String message){
        new Thread(new Runnable(){
            @Override
            public void run() {
                String strDetail = "";

                //if(IDualPhoneInfoFetcher.FIRST_SIM_INDEX == simIndexF){
                FlowCorrectWrapper.getInstance().analysisSMS(IDualPhoneInfoFetcher.FIRST_SIM_INDEX,
                        mquerycode,
                        mqueryport,
                        message);
                strDetail += "[" + mquerycode + "][" + mqueryport + "]\n[" +
                        message + "]\n";

                //}else if(IDualPhoneInfoFetcher.SECOND_SIM_INDEX == simIndexF){
                // FlowCorrectWrapper.getInstance().analysisSMS(IDualPhoneInfoFetcher.SECOND_SIM_INDEX,
                //         mquerycode,
                //         mqueryport,
                //         message);
                // strDetail += "[" + mquerycode + "][" + mqueryport + "]\n[" +
                //         message + "]\n";
                //}
                Message msg = uiHandler.obtainMessage(MSG_NEED_SEND_MSG,IDualPhoneInfoFetcher.FIRST_SIM_INDEX,0);
                msg.obj = strDetail;
                msg.sendToTarget();
            }

        }).start();
    }

    private final int MSG_NEED_SEND_MSG = 0x1b;

    Handler uiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                //case MSG_TRAFfICT_NOTIFY:
                //String logTemp = (String) msg.obj;
                // if(IDualPhoneInfoFetcher.FIRST_SIM_INDEX == msg.arg1){
//                    }else if(IDualPhoneInfoFetcher.SECOND_SIM_INDEX == msg.arg1){
                //      mTVSim2Detail.setText(mTVSim2Detail.getText() + logTemp);
                //   }
                //  break;
                case MSG_NEED_SEND_MSG:
                    //  if(IDualPhoneInfoFetcher.FIRST_SIM_INDEX == msg.arg1){
                    //      mTVSim1Detail.setText(mTVSim1Detail.getText() + (String) msg.obj);
                    //   }else if(IDualPhoneInfoFetcher.SECOND_SIM_INDEX == msg.arg1){
                    //       mTVSim2Detail.setText(mTVSim2Detail.getText() + (String) msg.obj);
                    //   }
                    break;
                case 3:
                    String message = msg.obj.toString();
                    startanalysisSMS(message);
                    Log.d(TAG,"now message content is"+message);


            }
        }
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);

        return START_NOT_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.setPriority(1000);
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver, filter);
        init();
        int resultcode = FlowCorrectWrapper.getInstance().requeatProfileInfo(IDualPhoneInfoFetcher.FIRST_SIM_INDEX);
        Log.d(TAG, "wangqch now is onStartCommang");
    }
}
