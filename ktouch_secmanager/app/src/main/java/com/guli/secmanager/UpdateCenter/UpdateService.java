package com.guli.secmanager.UpdateCenter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.IUpdateListener;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.UpdateRubbishDataCallback;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.LogUtil;
import com.guli.secmanager.Utils.ShareUtil;

/**
 * Created by yujie on 16-4-16.
 */
public class UpdateService extends Service implements UpdateRubbishDataCallback {
    public static final String TAG = "UpdateService";

    private static final int MSG_START_SERVICE = 5001;
    private static final int MSG_START_CHECK = 5002;
    private static final int MSG_START_UPDATE = 5003;
    private static final int MSG_STOP_SERVICE = 6001;

    //private static final int UPDATE_CHECK_INTERVAL = 1000 * 60* 3; // for test , 3 min
    private static final int UPDATE_CHECK_HOUR = 11; // 11 clock AM every day
    private static final int RANDOM_OFFSET = 59; // rangge of update check time offset is 0-58 minute

    private static final String LOGFILE = "kt_secmanager_updateservice.log";

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;

    // Wakelock
    private PowerManager.WakeLock mWakeLock;
    private PowerManager mPmMgr;

    // for updateManager
    private UpdateManager mUpdateManager;
    private CheckResult mCheckResults;

    private DeepcleanManager mDeepcleanManager;

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            Log.i(TAG, "--->>> releaseWakeLock(): release mWakeLock...");
            LogUtil.writeToSpecialFile(LOGFILE, "--->>> releaseWakeLock(): release mWakeLock...", true);
            mWakeLock.release();
        }
    }

    private void exitService() {
        Message msg = mServiceHandler.obtainMessage();
        msg.what = MSG_STOP_SERVICE;
        mServiceHandler.sendMessageDelayed(msg, 1000);
    }

    private void startCheck() {
        Message msg = mServiceHandler.obtainMessage();
        msg.what = MSG_START_CHECK;
        mServiceHandler.sendMessageDelayed(msg, 1000);
    }

    private void startUpdate() {
        Message msg = mServiceHandler.obtainMessage();
        msg.what = MSG_START_UPDATE;
        mServiceHandler.sendMessageDelayed(msg, 2000);
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_SERVICE:
                    onHandleIntent((Intent)msg.obj);
                    break;
                case MSG_START_CHECK:
                    doCheck();
                    break;
                case MSG_START_UPDATE:
                    doUpdate();
                    break;
                case MSG_STOP_SERVICE: {
                    Log.i(TAG, "updateRubbishData   "  );
                    mDeepcleanManager.updateRubbishData(UpdateService.this);

                    //Log.i(TAG, "--->>> handleMessage():MSG_STOP_SERVICE.");
                    //LogUtil.writeToSpecialFile(LOGFILE, "--->>> handleMessage():MSG_STOP_SERVICE.", true);
                    //releaseWakeLock();
                    //stopSelf();
                }
                    break;
                default:
                    Log.i(TAG, "--->>> could not reach!");
                    break;
            }
        }
    }

    @Override
    public void updateFinished() {
        Log.i(TAG, "updateFinished   "  );
        SharedPreferences sPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putBoolean(ShareUtil.FIRST_OPEN, false);
        editor.commit();
        //Toast.makeText(UpdateService.this, getString(R.string.update_finish), Toast.LENGTH_LONG).show();

        Log.i(TAG, "--->>> handleMessage():MSG_STOP_SERVICE.");
        LogUtil.writeToSpecialFile(LOGFILE, "--->>> handleMessage():MSG_STOP_SERVICE.", true);
        releaseWakeLock();
        stopSelf();
    }

    private static long getTargetTimeForTimer() {
        long firstTime = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
        long systemTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getDefault());
        int min = getRandomOffset();
        Log.i(TAG, "--->>> setTargetTimeForTimer(): getRandomOffset() = " + min);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.HOUR_OF_DAY, UPDATE_CHECK_HOUR);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 选择的定时时间
        long selectTime = calendar.getTimeInMillis();
        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
        Log.i(TAG, "--->>> TimerInfo: Day:Hour:Min -> " + calendar.get(Calendar.DAY_OF_MONTH) + ":" +
                calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        LogUtil.writeToSpecialFile(LOGFILE, "--->>> TimerInfo: Day:Hour:Min -> " + calendar.get(Calendar.DAY_OF_MONTH) + ":" +
                calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE), true);
        // 计算现在时间到设定时间的时间差
        long time = selectTime - systemTime;
        firstTime += time;
        // 返回闹铃注册开始时间
        return firstTime;
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Log.i(TAG, "--->>> setServiceAlarm : isOn = " + isOn);
        Intent i = new Intent(context, UpdateService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTargetTimeForTimer(), AlarmManager.INTERVAL_DAY, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    // 判断定时器是否已经启动
    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, UpdateService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    private static int getRandomOffset() {
        Random r = new Random();
        return r.nextInt(RANDOM_OFFSET);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean networkState = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
        return networkState;
    }

    private void doCheck() {
        Log.i(TAG, "--->>> doCheck begin...");
        LogUtil.writeToSpecialFile(LOGFILE, "--->>> doCheck now.", true);
        mUpdateManager = ManagerCreatorC.getManager(UpdateManager.class);
        long checkFlags =
//								UpdateConfig.UPDATA_FLAG_NUM_MARK//号码标记模块
//								UpdateConfig.UPDATE_FLAG_NUMMARK_LARGE//号码标记3M
//								UpdateConfig.UPDATE_FLAG_NUMMARK50W_LARGE//号码标记3.5M
//								| UpdateConfig.UPDATE_FLAG_BLACKLIST_PROCESS //优化模块的加速功能
//								| UpdateConfig.UPDATE_FLAG_NOTKILLLIST_KILL_PROCESSES //优化模块的加速功能
                UpdateConfig.UPDATE_FLAG_SYSTEM_SCAN_CONFIG//病毒扫描模块
                        | UpdateConfig.UPDATE_FLAG_ADB_DES_LIST//病毒扫描模块
                        | UpdateConfig.UPDATE_FLAG_VIRUS_BASE//病毒扫描模块
                        | UpdateConfig.UPDATE_FLAG_STEAL_ACCOUNT_LIST//病毒扫描模块
                        | UpdateConfig.UPDATE_FLAG_PAY_LIST//病毒扫描模块
                        | UpdateConfig.UPDATE_FLAG_TRAFFIC_MONITOR_CONFIG//流量监控
//								| UpdateConfig.UPDATE_FLAG_LOCATION//归属地模块
                        | UpdateConfig.UPDATE_FLAG_PROCESSMANAGER_WHITE_LIST// 瘦身大文件模块
                        | UpdateConfig.UPDATE_FLAG_WeixinTrashCleanNew;//瘦身微信
//								| UpdateConfig.UPDATE_FLAG_POSEIDONV2;//智能拦截

        mUpdateManager.check(checkFlags, new ICheckListener() {

            //tmsdk会检查网络，如果网络失败则回调此接口
            @Override
            public void onCheckEvent(int i) {
                LogUtil.writeToSpecialFile(LOGFILE, "Network unavailable now. Report by Tmsdk CheckListener:onCheckoutEvent() callback function.", true);
                //出现网络错误，放弃本次更新
                exitService();
            }

            @Override
            public void onCheckStarted() {
                Log.i(TAG, "--->>> doCheck onCheckStarted...");
            }

            @Override
            public void onCheckCanceled() {
                Log.i(TAG, "--->>> doCheck onCheckCanceled...");
            }

            @Override
            public void onCheckFinished(CheckResult checkResult) {
                Log.i(TAG, "--->>> doCheck onCheckFinished...");
                if (checkResult != null) {
                    mCheckResults = checkResult; // 检查结果，供后续下载更新使用
                    // 如果成功获取到check结果,记录到log文件
                    Log.i(TAG, "--->>> onCheckFinished: UpdateInfoListSize = " + checkResult.mUpdateInfoList.size());
                    for (UpdateInfo info : checkResult.mUpdateInfoList) {
                        //Log.v(TAG, "--->>> onCheckFinished:updateinfo:" + info.url);
                        LogUtil.writeToSpecialFile(LOGFILE, "--->>> update url: " + info.url, true);
                    }
                    startUpdate();

                } else {
                    // 无可用更新，结束服务
                    Log.i(TAG, "--->>> onCheckFinished(): nothing require update, so exit service!");
                    LogUtil.writeToSpecialFile(LOGFILE, "--->>> onCheckFinished(): nothing require update, so exit service!", true);
                    exitService();
                }
            }
        });
    }

    private void doUpdate() {
        Log.i(TAG, "--->>> doUpdate(): Enter...");
        if (mCheckResults != null && mCheckResults.mUpdateInfoList != null
                && mCheckResults.mUpdateInfoList.size() > 0) {
            Log.i(TAG, "--->>> doUpdate(): mUpdateInfoList.size() = " + mCheckResults.mUpdateInfoList.size());
            LogUtil.writeToSpecialFile(LOGFILE, "--->>> doUpdate(): mUpdateInfoList.size() =" + mCheckResults.mUpdateInfoList.size(), true);
            mUpdateManager.update(mCheckResults.mUpdateInfoList, new IUpdateListener() {
                @Override
                //更新
                public void onProgressChanged(UpdateInfo arg0, int arg1) {
                    Log.i(TAG, "--->>> onProgressChanged()...arg0 : " + arg0 + "   arg1:  " + arg1);
                }

                @Override
                //更新中检查网络，网络出错时回调
                public void onUpdateEvent(UpdateInfo arg0, int arg1) {
                    LogUtil.writeToSpecialFile(LOGFILE, "Network unavailable now. Report by Tmsdk UpdateListener:onUpdateEvent() callback function.", true);
                    //出现网络错误，放弃本次更新
                    exitService();
                }

                @Override
                public void onUpdateFinished() {
                    Log.i(TAG, "--->>> onUpdateFinished()...");
                    exitService();
                }

                @Override
                public void onUpdateStarted() {
                    Log.i(TAG, "--->>> onUpdateStarted()...");
                }

                @SuppressWarnings("unused")
                public void onUpdateCanceled() {
                    Log.i(TAG, "--->>> onUpdateCanceled()...");
                }
            });
        } else {
            // 检查数据存在问题，放弃本次更新。
            Log.i(TAG, "--->>> doUpdate(): find some error in check results, so give up!");
            LogUtil.writeToSpecialFile(LOGFILE, "--->>> doUpdate(): find some error in check results, so give up!", true);
            exitService();
        }
    }

    public UpdateService() {
        super();
        mName = TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread(mName + "->WorkThread");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mDeepcleanManager = ManagerCreatorF.getManager(DeepcleanManager.class);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        // 如果因为系统low memery等意外情况kill, 本服务不需系统再次重启，顺延到下次timer trigger触发时会再次启动。
        return START_NOT_STICKY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.what = MSG_START_SERVICE;
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    private void onHandleIntent(Intent intent) {
        mPmMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPmMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.TAG);
        LogUtil.writeToSpecialFile(LOGFILE, "--->>> onHandleIntent():acquire mWakeLock ...", true);
        mWakeLock.acquire();
        Log.i(TAG, "--->>> Received an intent: " + intent);
        LogUtil.writeToSpecialFile(LOGFILE, "--->>> UpdateService timer triggered, onHandleIntent enter... ", true);
        //
        if (!isNetworkAvailable()) {
            // 网络不可用，本次不检查更新，压后到下次检查
            Log.i(TAG, "--->>> network unavailable now, don't check this time.");
            LogUtil.writeToSpecialFile(LOGFILE, "--->>> network unavailable now, don't check this time.", true);
            exitService();
        } else {
            // 调用tmsdk updateManager相关接口，检查服务器侧是否有可更新的数据文件
            startCheck();
        }
    }

    @Override
    public void onDestroy() {
        releaseWakeLock(); // 系统强杀时的补漏
        mServiceLooper.quit();
        if(mDeepcleanManager != null) {
            //找垃圾
            mDeepcleanManager.onDestory();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
