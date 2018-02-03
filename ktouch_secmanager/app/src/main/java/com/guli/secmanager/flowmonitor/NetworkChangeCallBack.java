package com.guli.secmanager.flowmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.guli.secmanager.Utils.ShareUtil;

import java.util.Calendar;

import tmsdk.bg.module.network.INetworkChangeCallBack;
import tmsdk.common.module.network.NetworkInfoEntity;

public final class NetworkChangeCallBack implements INetworkChangeCallBack {
	private static final String TAG = "flow-NetworkChangeCB";
	private String mName;
	private Context mContext;
    //当存在闲时流量时，是否已经设定的闲时时间段
    private static boolean IsFreeTime = false;
    private static long mLastUsedForDay = 0L;

	public NetworkChangeCallBack(String name, Context context) {
		mName = name;
		mContext = context;
	}
	
	//当到达月结日时回调
	@Override
	public void onClosingDateReached() {
		Log.e(TAG, "onClosingDateReached");
		//Toast.makeText(mContext, "当到达月结日时回调", Toast.LENGTH_LONG);
        Log.d(TAG, "onClosingDateReached 当到达月结日时回调");
	}
	
	// 当Day发生变化时回调
	@Override
	public void onDayChanged() {
		Log.e(TAG, "onDayChanged");
		//Toast.makeText(mContext, "当Day发生变化时回调", Toast.LENGTH_LONG);
        Log.d(TAG, "onDayChanged 当Day发生变化时回调");
	}
	
	//当流量有发生变化时 
	@Override
	public void onNormalChanged(final NetworkInfoEntity arg0) {
        Log.i("wanghg", "onNormalChanged123");
        Log.i("wanghg", "mName=" + mName + ",dayused=" + arg0.mUsedForDay + ",retail=" + arg0.mRetialForMonth);
        if(mLastUsedForDay == arg0.mUsedForDay) {
            return;
        }
        mLastUsedForDay = arg0.mUsedForDay;

        SharedPreferences mSharedPref = mContext.getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        //boolean state = mSharedPref.getBoolean(ShareUtil.AUTO_BREAK_STATE, false);
        //校准过程中，不统计数据
        boolean state = mSharedPref.getBoolean(ShareUtil.REQUEST_FLOW_AUTO_CORRECTING, false);
        boolean FreeFlowExist = (mSharedPref.getInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, 0) > 0) ? true : false;
        Log.i("wanghg", "state=" + state + ",mUsedForMonth=" + arg0.mUsedForMonth + ",mTotalForMonth=" + arg0.mTotalForMonth);
        boolean oldFreeState = IsFreeTime;

        judgeFreeTime();

        int mFreeBaseKB = mSharedPref.getInt(ShareUtil.FREE_BASE_VALUE_KBYTES, 0);//KB

        Log.i("wanghg", "IsFreeTime=" + IsFreeTime + ",oldFreeState=" + oldFreeState);

        if((false == oldFreeState) && (true == IsFreeTime) && mName.equals("mobile")){
            mFreeBaseKB = (int)(arg0.mUsedForMonth + arg0.mUsedForDay) / 1024;
            editor.putInt(ShareUtil.FREE_BASE_VALUE_KBYTES, mFreeBaseKB);
            editor.commit();
        }

        if((false == state) && mName.equals("mobile") && (arg0.mUsedForDay > 0)){
            //if(arg0.mUsedForMonth <= arg0.mTotalForMonth){
                int mUsedForMonthKB = (int)((arg0.mUsedForDay + arg0.mUsedForMonth)/1024);
                Log.d(TAG, "onNormalChanged arg0.mUsedForDay =" + arg0.mUsedForDay
                    + ",arg0.mUsedForMonth =" + arg0.mUsedForMonth +",mUsedForMonthKB="+mUsedForMonthKB);
                if((FreeFlowExist == true) && (IsFreeTime == true)) {
                    int mFreeUsedKB = mUsedForMonthKB - mFreeBaseKB;
                    Log.d(TAG, "onNormalChanged mFreeUsedKB=" + mFreeUsedKB);
                    editor.putInt(ShareUtil.SIM1_FREE_USED_KBYTES, mFreeUsedKB);

                    int mFreeTotalKB = mSharedPref.getInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, 0);

                    if(mFreeTotalKB > mFreeUsedKB) {
                        editor.putInt(ShareUtil.SIM1_FREE_LEFT_KBYTES, mFreeTotalKB-mFreeUsedKB );
                        editor.commit();
                        return;
                    }
                    else {
                        editor.putInt(ShareUtil.SIM1_FREE_LEFT_KBYTES, 0);
                        editor.putInt(ShareUtil.SIM1_FREE_USED_KBYTES, mFreeTotalKB);
                        //editor.putInt(ShareUtil.SIM1_COMMON_LEFT_KBYTES, 0);
                        //editor.putInt(ShareUtil.SIM1_4G_LEFT_KBYTES, 0);
                        //editor.putInt(ShareUtil.FLOW_ALL_USED_FOR_MONTH, (int) arg0.mUsedForMonth/1024);
                        //mContext.sendBroadcast(new Intent(ShareUtil.ACTION_AUTO_BREAK_FLOW));
                        //Log.d(TAG, "onNormalChanged send_auto_break_flow  free overflow");
                    }
                }//else{

                int mFreeUsedKB = mSharedPref.getInt(ShareUtil.SIM1_FREE_USED_KBYTES, 0);
                int mCommonUsedKB = mUsedForMonthKB - mFreeUsedKB;

                Log.d(TAG, "onNormalChanged mFreeUsedKB="+mFreeUsedKB+",mCommonUsedKB="+mCommonUsedKB);

                if(mCommonUsedKB < 0){
                    mCommonUsedKB = 0;
                }

                editor.putInt(ShareUtil.SIM1_COMMON_USED_KBYTES, mCommonUsedKB );

                int mCommonTotalKB = mSharedPref.getInt(ShareUtil.SIM1_COMMON_TOTAL_KBYTES, 0);
                Log.i("wanghg", "mCommonTotalKB : " + mCommonTotalKB + "   mCommonUsedKB : " + mCommonUsedKB);
                Log.e(TAG, "onNormalChanged SIM1_COMMON_LEFT M=" + mSharedPref.getInt(ShareUtil.SIM1_COMMON_LEFT_KBYTES, 0)/1024);

                if(mCommonTotalKB > mCommonUsedKB) {
                    editor.putInt(ShareUtil.SIM1_COMMON_LEFT_KBYTES, mCommonTotalKB-mCommonUsedKB );
                }
                else {
                    //editor.putInt(ShareUtil.SIM1_FREE_LEFT_KBYTES, 0);
                    editor.putInt(ShareUtil.SIM1_COMMON_LEFT_KBYTES, 0);
                    //editor.putInt(ShareUtil.SIM1_4G_LEFT_KBYTES, 0);
                    //editor.putInt(ShareUtil.FLOW_ALL_USED_FOR_MONTH, (int) arg0.mUsedForMonth/1024);
                    mContext.sendBroadcast(new Intent(ShareUtil.ACTION_AUTO_BREAK_FLOW));
                    Log.d(TAG, "onNormalChanged send_auto_break_flow  common overflow");
                }
                //}
            /*}else{
                editor.putInt(ShareUtil.SIM1_FREE_LEFT_KBYTES, 0);
                editor.putInt(ShareUtil.SIM1_COMMON_LEFT_KBYTES, 0);
                //editor.putInt(ShareUtil.SIM1_4G_LEFT_KBYTES, 0);
                editor.putInt(ShareUtil.FLOW_ALL_USED_FOR_MONTH, (int) arg0.mUsedForMonth/1024);
                mContext.sendBroadcast(new Intent(ShareUtil.ACTION_AUTO_BREAK_FLOW));
                Log.d(TAG, "onNormalChanged send_auto_break_flow");
            }*/
        }
        editor.commit();
	}

	private void judgeFreeTime(){
		SharedPreferences mSharedPref = mContext.getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mSharedPref.edit();
		Calendar cal = Calendar.getInstance();// 当前日期
		int curHour = cal.get(Calendar.HOUR_OF_DAY);// 获取小时
		//int minute = cal.get(Calendar.MINUTE);// 获取分钟
		//int minuteOfDay = hour * 60 + minute;// 从0:00分开是到目前为止的分钟数
		int start = mSharedPref.getInt(ShareUtil.START_TIME_HOUR, 0);// 起始时间 17:00的分钟数
		int end = mSharedPref.getInt(ShareUtil.END_TIME_HOUR, 0);;// 结束时间 19:00的分钟数

        Log.d(TAG, "curHour="+curHour+",start="+start+",end="+end);

        int Hour24 = 24;

        if(start > end){
            if ((curHour >= start && curHour <= Hour24)
            || (curHour >= 0 && curHour <= end)){
                //System.out.println("在外围内");
                IsFreeTime = true;
            } else {
                //System.out.println("在外围外");
                IsFreeTime = false;
            }
        }else{
            if (curHour >= start && curHour <= end) {
                //System.out.println("在外围内");
                IsFreeTime = true;
            } else {
                //System.out.println("在外围外");
                IsFreeTime = false;
            }
        }
	}
}
