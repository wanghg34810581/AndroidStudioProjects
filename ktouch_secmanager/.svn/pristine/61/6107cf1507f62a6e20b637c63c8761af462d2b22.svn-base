package com.guli.secmanager.GarbageClean;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.RubbishEntityManager;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.deepclean.ScanProcessListener;

/**
 * Created by wangdsh on 16-4-29.
 */

public class GarbageCleanService extends Service {
    //Debug
    private static final String TAG = "GarbageCleanUpService";

    private DeepcleanManager mDeepcleanManager;
    private IBinder mBinder = new ServiceBinder();

    public class ServiceBinder extends Binder {
        GarbageCleanService getService()
        {
            return GarbageCleanService.this;
        }
    }

    public void init(ScanProcessListener scanProcessListener){
        //mDeepcleanManager = ManagerCreatorF.getManager(DeepcleanManager.class);
        //mDeepcleanManager.appendWhitePath("/tencent/mobileqq");
        //mDeepcleanManager.appendWhitePath("/tencent/MicroMsg");

        if (!mDeepcleanManager.init(scanProcessListener)) {
            //初始化错误操作
            Log.e(TAG,"DeepcleanManager Init mScanProcessListener Error");
        }
    }

    public void reinit(ScanProcessListener scanProcessListener){
        uinit();
        init(scanProcessListener);
    }

    private void uinit(){
        if(mDeepcleanManager != null) {
            //找垃圾
            mDeepcleanManager.onDestory();
        }
    }
    public void startScan(int flags) {
        mDeepcleanManager.startScan(flags);
    }

    public void startScan() {
        mDeepcleanManager.startScan(RubbishType.SCAN_FLAG_ALL);
    }
    public void cancelScan(){
        mDeepcleanManager.cancelScan();
    }

    public void startClean() {
        mDeepcleanManager.startClean();
    }
    public void cancelClean(){
        mDeepcleanManager.cancelClean();
    }

    public RubbishEntityManager getmRubbishEntityManager(){
        return mDeepcleanManager.getmRubbishEntityManager();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mDeepcleanManager = ManagerCreatorF.getManager(DeepcleanManager.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        if(mDeepcleanManager != null) {
            //找垃圾
            mDeepcleanManager.onDestory();
        }
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
