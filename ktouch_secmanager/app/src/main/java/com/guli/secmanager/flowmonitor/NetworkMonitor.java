package com.guli.secmanager.flowmonitor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.guli.secmanager.Utils.ShareUtil;

import java.util.ArrayList;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.network.INetworkChangeCallBack;
import tmsdk.bg.module.network.INetworkInfoDao;
import tmsdk.bg.module.network.NetworkManager;
import tmsdk.common.module.network.NetworkInfoEntity;
import tmsdk.common.module.network.TrafficEntity;

/**
 * Created by zouchl on 4/20/16.
 */
public class NetworkMonitor {

    private final static String TAG = "flow_NetworkMonitor";
    private NetworkManager mNetworkManager;
    private INetworkChangeCallBack mCallbackForMobile;
    private INetworkChangeCallBack mCallbackForWIFI;
    private AppTrafficTest mAppTrafficTest;
    private Context mContext;
    private INetworkInfoDao mNetworkInfoDaoMobile, mNetworkInfoDaoWifi;

    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor editor;

    public static int mTotalForMonth, mUsedForMonth;
    public static int mClosingDay;

    public NetworkMonitor(Context context) {

        mContext = context;
        mSharedPref = mContext.getSharedPreferences(ShareUtil.DATABASE_NAME, Activity.MODE_PRIVATE);
        editor = mSharedPref.edit();
        mTotalForMonth = mSharedPref.getInt(ShareUtil.SIM1_COMMON_TOTAL_KBYTES, 0) + mSharedPref.getInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, 0);
        mUsedForMonth = mSharedPref.getInt(ShareUtil.SIM1_COMMON_USED_KBYTES, 0) + mSharedPref.getInt(ShareUtil.SIM1_FREE_USED_KBYTES, 0);
        Log.d(TAG, "NetworkMonitor mTotalForMonth="+mTotalForMonth+",mUsedForMonth="+mUsedForMonth+",mClosingDay="+mClosingDay);

        //现在还没有用到，不明白什么意思
        String strClosingDay = mSharedPref.getString(ShareUtil.ACCOUNT_DATE, "1");
        if(!(strClosingDay.equals("0") || strClosingDay.equals(""))){
            mClosingDay = Integer.parseInt(strClosingDay);
        }

        mNetworkManager = ManagerCreatorB.getManager(NetworkManager.class);
        mCallbackForMobile = new NetworkChangeCallBack("mobile", mContext);
        //mCallbackForWIFI = new NetworkChangeCallBack("WIFI", mContext);
        mNetworkInfoDaoMobile = NetworkInfoDao.getInstance("mobile", mContext);
        mNetworkInfoDaoWifi = NetworkInfoDao.getInstance("wifi", mContext);
        mAppTrafficTest = new AppTrafficTest();

    }

    public void startNetworkMonitor(){
        Log.i("wanghg", "startNetworkMonitor");
        //INTERVAL_FOR_REALTIME = 2;最短时间的刷新，适配一些极端情况
        mNetworkManager.setInterval(30 * 1000);

        //添加默认的Mobile监控器和WIFI监控器
        mNetworkManager.addDefaultMobileMonitor("mobile", mNetworkInfoDaoMobile);
        mNetworkManager.addDefaultWifiMonitor("WIFI", mNetworkInfoDaoWifi);

        //寻找流量监控器,添加毁掉
        mNetworkManager.findMonitor("mobile").addCallback(mCallbackForMobile);
        //mNetworkManager.findMonitor("WIFI").addCallback(mCallbackForWIFI);


        //mAppTrafficTest.start(mNetworkManager, mContext);

        //setThresholdForMonth();
        /*int iTotal = getFlowTotal();
        int iUsed = getFlowUsed();
        mNetworkInfoDaoMobile.setTotalForMonth(iTotal);
        mNetworkInfoDaoMobile.setUsedForMonth(iUsed);
        mNetworkInfoDaoMobile.setClosingDayForMonth(26);
        mNetworkManager.notifyConfigChange();*/

    }

    /*protected void getLastDate(){
        ArrayList<TrafficEntity> mDatas = mAppTrafficTest.getLastData();
        if(mDatas != null){
            Log.d(TAG, "stopNetworkMonitor mData.size=" + mDatas.size());

            for(int i=0; i < mDatas.size(); i++){
                Log.e(TAG, "data-"+i+",MobileDown="+mDatas.get(i).mMobileDownValue+"MobileUp="+mDatas.get(i).mMobileUpValue
                        +",WIfiDown="+mDatas.get(i).mWIFIDownValue+"wifiUp="+mDatas.get(i).mWIFIUpValue
                        +",LastDown="+mDatas.get(i).mLastDownValue+"LastUp="+mDatas.get(i).mLastUpValue);
            }
        }
    }*/
    protected void stopNetworkMonitor() {
        Log.d(TAG, "stopNetworkMonitor");
        //寻找流量监控器,删除毁掉
        /*ArrayList<TrafficEntity> mDatas = mAppTrafficTest.getLastData();

        Log.d(TAG, "stopNetworkMonitor mData.size="+ mDatas.size());
        for(int i=0; i < mDatas.size(); i++){
            Log.e(TAG, "data-"+i+",MobileDown="+mDatas.get(i).mMobileDownValue+"MobileUp="+mDatas.get(i).mMobileUpValue
                    +",WIfiDown="+mDatas.get(i).mWIFIDownValue+"wifiUp="+mDatas.get(i).mWIFIUpValue
                    +",LastDown="+mDatas.get(i).mLastDownValue+"LastUp="+mDatas.get(i).mLastUpValue);
        }*/
        mNetworkManager.findMonitor("mobile").removeCallback(mCallbackForMobile);
        //mNetworkManager.findMonitor("WIFI").removeCallback(mCallbackForWIFI);
        //mAppTrafficTest.destory();
    }

    protected boolean getNetworkManagerState(){
        Log.e(TAG, "getNetworkManagerState=" + mNetworkManager.isEnable());
        return mNetworkManager.isEnable();
    }

    protected void setNetworkManagerState(boolean state){
        Log.e(TAG, "setNetworkManagerState=" + state);
        if(state != getNetworkManagerState()){
            mNetworkManager.setEnable(state);
        }
    }

    public void setTotalForMonth(long iSize){
        Log.v(TAG, "setTotalForMonth size="+iSize);
        mNetworkInfoDaoMobile.setTotalForMonth(iSize * 1024);
    }

    public void setUsedForMonth(long iSize){
        Log.v(TAG, "setUsedForMonth size="+iSize);
        mNetworkInfoDaoMobile.setUsedForMonth(iSize * 1024);
    }

    public void resetTodayNetworkInfo(){
        Log.v(TAG, "resetTodayNetworkInfo");
        mNetworkInfoDaoMobile.resetToDayNetworkInfoEntity();
    }

    public int getUsedForDay(){
        NetworkInfoEntity mEntity = mNetworkInfoDaoMobile.getTodayNetworkInfoEntity();
        Log.v(TAG, "getUsedForDay ="+mEntity.mUsedForDay);
        return (int)(mEntity.mUsedForDay/1024);
    }
}