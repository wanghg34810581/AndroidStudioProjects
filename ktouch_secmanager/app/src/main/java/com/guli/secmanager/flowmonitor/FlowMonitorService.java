package com.guli.secmanager.flowmonitor;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.ShareUtil;

import java.lang.reflect.Method;
import java.util.Calendar;

public class FlowMonitorService extends Service {
    private final static String TAG = "FlowMonitorService";

    private NetworkMonitor mNetworkMonitor;
    private Context mContext;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor editor;

    private NetworkMonitor monitor;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate Enter ...");

        mContext = getApplicationContext();
        mSharedPref = mContext.getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
        editor = mSharedPref.edit();
        registerReceiver();

        /*月限额流量*/
        int mMonthFlowTotal = 0;
        /*月已用流量*/
        int mMonthFlowUsed = 0;
        /*统计某个月的流量*/
        int mCurrentMonth = -1;

        monitor = new NetworkMonitor(mContext);
        monitor.startNetworkMonitor();

        /*mMonthFlowTotal = mSharedPref.getInt(ShareUtil.SIM1_COMMON_TOTAL_KBYTES, 0) + mSharedPref.getInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, 0);
        mMonthFlowUsed = mSharedPref.getInt(ShareUtil.SIM1_COMMON_USED_KBYTES, 0) + mSharedPref.getInt(ShareUtil.SIM1_FREE_USED_KBYTES, 0);
        mCurrentMonth = mSharedPref.getInt(ShareUtil.CURRENT_MONTH, -1);

        Log.d(TAG, "onCreate MonthFlowUsed=" + mMonthFlowUsed + ",MonthFlowTotal=" + mMonthFlowTotal + ",mCurrentMonth="+mCurrentMonth);

        if(mMonthFlowTotal <= 0){
            int iGetSettingFlow = getFlowTotal(mContext);
            if(iGetSettingFlow > 0){
                mMonthFlowTotal = iGetSettingFlow;
            }else{
                mMonthFlowTotal = 0;
                //Toast.makeText(mContext, R.string.set_flow_total, Toast.LENGTH_LONG);
                Log.d(TAG, "onCreate mMonthTotalFlow is 0, 需要设置套餐总量");
            }
            editor.putInt(ShareUtil.FLOW_ALL_TOTAL_FOR_MONTH, mMonthFlowTotal);
            editor.commit();
        }

        if(mMonthFlowUsed <= 0){
            int iGetSettingUsed = getTotalFlowUsed(mContext);
            mMonthFlowUsed = iGetSettingUsed;
            editor.putInt(ShareUtil.FLOW_ALL_USED_FOR_MONTH, mMonthFlowUsed);
            editor.commit();
        }
        //readSharePreData();
        int iGetMonth = getCurrentMonth();//加1后为实际月份
        Log.d(TAG, "onCreate iGetMonth=" + iGetMonth);
        if(iGetMonth != mCurrentMonth){
            Log.d(TAG, "onCreate next month");
            mCurrentMonth = iGetMonth;
            mMonthFlowUsed = 0;
            editor.putInt(ShareUtil.FLOW_ALL_USED_FOR_MONTH, mMonthFlowUsed);
            editor.putInt(ShareUtil.CURRENT_MONTH, mCurrentMonth);
            editor.commit();
            //readSharePreData();
        }*/

        /*boolean bAutoBreakState = mSharedPref.getBoolean(ShareUtil.AUTO_BREAK_STATE, false);
        if (true == bAutoBreakState){
            monitor.setNetworkManagerState(true);
        }else{
            monitor.setNetworkManagerState(false);
        }*/
        monitor.setNetworkManagerState(true);
        monitor.setTotalForMonth(mMonthFlowTotal);
        monitor.setUsedForMonth(mMonthFlowUsed);

        mSharedPref.registerOnSharedPreferenceChangeListener(mSharePrefChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");

        /*editor.putInt(ShareUtil.FLOW_ALL_TOTAL_FOR_MONTH, getSettingsFlowTotal(mContext));
        editor.putInt(ShareUtil.FLOW_ALL_USED_FOR_MONTH, getSettingsFlowUsed(mContext));
        editor.putInt(ShareUtil.CURRENT_MONTH, getCurrentMonth());
        editor.commit();*/
        //readSharePreData();
        Log.d(TAG, "onDestroy() executed save end");
        unregisterReceiver(mReceiver);
    }

    private void registerReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ShareUtil.ACTION_AUTO_BREAK_FLOW);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(ShareUtil.ACTION_SET_USED_FOR_MONTH);
        registerReceiver(mReceiver, filter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.

            String action = intent.getAction();
            Log.v(TAG, "Received action=" + action);
            if (action.equals(ShareUtil.ACTION_AUTO_BREAK_FLOW)) {
                /*UED 设计：当超出月标准套餐总量，闲时套餐总量，4G套餐专用总量之和后断网*/
                Log.e(TAG, "Received break_flow_toast");
                boolean bAutoBreak = mSharedPref.getBoolean(ShareUtil.AUTO_BREAK_STATE, false);

                if(false == bAutoBreak){return;}

                //Toast.makeText(context, R.string.break_flow_toast, Toast.LENGTH_LONG);
                Log.e(TAG, "break_flow_toast");

                //editor.putBoolean(ShareUtil.AUTO_BREAK_STATE, false);
                //editor.commit();

                /*Intent mIntent = new Intent(mContext, AlertDialog.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(mIntent);*/
                showToast();
                setMobileData(mContext, false);//android 5.0 以上不起作用

                    /*android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
                    builder.setMessage("Are you sure you want to exit?")
                            .setCancelable(false)
                            .setNegativeButton("yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    android.app.AlertDialog alert = builder.create();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
                    //alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();*/
                // }
            } else if (action.equals(Intent.ACTION_DATE_CHANGED)) {
                Log.e(TAG, "ACTION_DATE_CHANGED");
                int iGetMonth = getCurrentMonth();//加1后为实际月份
                int mCurrentMonth = mSharedPref.getInt(ShareUtil.CURRENT_MONTH, -1);
                Log.d(TAG, "iGetMonth=" + iGetMonth+",mCurrentMonth=" + mCurrentMonth);
                if(iGetMonth == mCurrentMonth){
                    int mUsedForMonth = mSharedPref.getInt(ShareUtil.SIM1_COMMON_USED_KBYTES, 0) + mSharedPref.getInt(ShareUtil.SIM1_FREE_USED_KBYTES, 0);
                    int mUsedForDay = monitor.getUsedForDay();
                    editor.putInt(ShareUtil.CURRENT_MONTH, iGetMonth);
                    editor.commit();

                    monitor.setUsedForMonth(mUsedForMonth + mUsedForDay);
                }else{
                    //editor.putInt(ShareUtil.SIM1_4G_USED_KBYTES, 0);
                    editor.putInt(ShareUtil.SIM1_COMMON_USED_KBYTES, 0);
                    editor.putInt(ShareUtil.SIM1_FREE_USED_KBYTES, 0);
                    editor.commit();

                    monitor.setUsedForMonth(0);
                }
            } else if(action.equals(ShareUtil.ACTION_SET_USED_FOR_MONTH)){
                monitor.resetTodayNetworkInfo();
                monitor.setTotalForMonth(mSharedPref.getInt(ShareUtil.SIM1_COMMON_TOTAL_KBYTES, 0) + mSharedPref.getInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, 0));
                monitor.setUsedForMonth(mSharedPref.getInt(ShareUtil.SIM1_COMMON_USED_KBYTES, 0) + mSharedPref.getInt(ShareUtil.SIM1_FREE_USED_KBYTES, 0));
            }
        }
    };
    /**
     * 设置手机的移动数据
     */
    public static void setMobileData(Context cxt, boolean state) {
        ConnectivityManager connectivityManager = null;
        Class connectivityManagerClz = null;
        try {
            connectivityManager = (ConnectivityManager) cxt
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManagerClz = connectivityManager.getClass();
            Method method = connectivityManagerClz.getMethod(
                    "setMobileDataEnabled", new Class[] { boolean.class });
            method.invoke(connectivityManager, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getCurrentMonth(){
        final Calendar c = Calendar.getInstance();
        //mYear = c.get(Calendar.YEAR); //获取当前年份
        int mMonth = c.get(Calendar.MONTH);//获取当前月份
        return mMonth;
    }

    public SharedPreferences.OnSharedPreferenceChangeListener mSharePrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.e(TAG, "key=" + key);
            if(key.equals(ShareUtil.SIM1_COMMON_TOTAL_KBYTES)
                    || key.equals(ShareUtil.SIM1_FREE_TOTAL_KBYTES)){
                Log.d(TAG, "OnSharedPreferenceChangeListener flow_total_month changed");
                //int iTotal = getFlowTotal(mContext);
                monitor.setTotalForMonth(mSharedPref.getInt(ShareUtil.SIM1_COMMON_TOTAL_KBYTES, 0) + mSharedPref.getInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, 0));
            }
            /*else if(key.equals(ShareUtil.SIM1_COMMON_LEFT_KBYTES)
                    || key.equals(ShareUtil.SIM1_FREE_LEFT_KBYTES)){
                Log.d(TAG, "OnSharedPreferenceChangeListener SIM1_COMMON changed");
                saveTotalUsedFormLeft(mContext);
            }*/
        }
    };

    public void showToast(){
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                Toast.makeText(FlowMonitorService.this, R.string.break_flow_toast, Toast.LENGTH_LONG).show();
                Log.d(TAG, "break_flow_toast break_flow_toast");
                Looper.loop();
            }
        }).start();
    }
}
