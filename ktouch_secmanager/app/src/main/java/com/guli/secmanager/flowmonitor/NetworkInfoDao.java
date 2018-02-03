package com.guli.secmanager.flowmonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.ShareUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import tmsdk.bg.module.network.INetworkInfoDao;
import tmsdk.bg.module.network.NetDataEntity;
import tmsdk.common.module.network.NetworkInfoEntity;

/**
 * 
 * @author boyliang
 * 作为DEMO，只是简单采用内存的方式保存数据
 * 实际项目开发当中，应该采用可持久化的数据保存方式，如读取文件或者SQLite等
 */
public final class NetworkInfoDao implements INetworkInfoDao {
	private final String TAG = "flow_NetworkInfoDao";
	private final boolean DEBUG = true;

	private static HashMap<String, INetworkInfoDao> sInstances = new HashMap<String, INetworkInfoDao>();
	private Context mContext;

	public static synchronized INetworkInfoDao getInstance(String name, Context context){
		INetworkInfoDao result = null;
		if(!sInstances.containsKey(name)){
			result = new NetworkInfoDao(context);
			sInstances.put(name, result);
		}else{
			result = sInstances.get(name);
		}
		return result;
	}
	
	private ArrayList<NetworkInfoEntity> mDatas = new ArrayList<NetworkInfoEntity>();
	private NetDataEntity mLastNetDataEntity;
	private NetworkInfoEntity mTodayNetworkInfoEntity = new NetworkInfoEntity();
	private long mUsedForMonth = 0;
	private int mClosingDay = 1;
	private long mTotalForMonth = 30 * 1024 *1024;//	//获取本月限制的流量，单位B
	
	private NetworkInfoDao(Context context){
		mContext = context;
		mTotalForMonth = (long)NetworkMonitor.mTotalForMonth;
		mUsedForMonth = (long)NetworkMonitor.mUsedForMonth;
		mClosingDay = NetworkMonitor.mClosingDay;

		Log.d(TAG, "NetworkInfoDao mTotalForMonth="+mTotalForMonth+",mUsedForMonth="+mUsedForMonth+",mClosingDay="+mClosingDay);
		mTodayNetworkInfoEntity.mTotalForMonth = mTotalForMonth;//每月限额流量
		mTodayNetworkInfoEntity.mUsedForMonth = mUsedForMonth;//该月已用的流量
		mTodayNetworkInfoEntity.mUsedForDay = 0l;//本日已经用流量
		mTodayNetworkInfoEntity.mRetialForMonth = mTotalForMonth;//该月剩余流量
	}
	//清空当月所有流量监控日志
	@Override
	public void clearAll() {
		mDatas.clear();
		if(DEBUG)
		Log.v(TAG, "clearAll");

	}
	//获取当前所有流量监控日志,并返回流量日志列表
	@Override
	public ArrayList<NetworkInfoEntity> getAll() {
		if(DEBUG)
		Log.v(TAG, "getAll");
		return (ArrayList<NetworkInfoEntity>) mDatas.clone();
	}
	//获取月结日，并返回月结日 [1~31]
	@Override
	public int getClosingDayForMonth() {
		if(DEBUG)
		Log.v(TAG, "getClosingDayForMonth mClosingDay=" + mClosingDay);
		return mClosingDay;
	}
	//获取上一次网络情况
	@Override
	public NetDataEntity getLastNetDataEntity()
	{
		if(DEBUG)
		if (mLastNetDataEntity != null)
		Log.v(TAG, "getLastNetDataEntity mReceiver=" + mLastNetDataEntity.mReceiver
				+ ",mTotalForMonth="+mLastNetDataEntity.mTranslate
				+ ",mReceiverPks="+ mLastNetDataEntity.mReceiverPks
				+ ",mTranslatePks="+ mLastNetDataEntity.mTranslatePks);
		return mLastNetDataEntity;
	}
	//获取当天网络流量监控情况
	//get 返回给SDK曾经保存的当天流量，可以累积计算当月流量。初始化SDK时，sdk调用这个接口获取基础数
	//set 保存SDK发送过来的当天流量，可以累积计算当天流量 检测流量时，SDK调用这个接口发送流量信息
	@Override
	public NetworkInfoEntity getTodayNetworkInfoEntity() {
		if(DEBUG)
		Log.v(TAG, "getTodayNetworkInfoEntity mUsedForMonth=" + mTodayNetworkInfoEntity.mUsedForMonth
				+ ",mTotalForMonth="+mTodayNetworkInfoEntity.mTotalForMonth
				+ ",mUsedTranslateForMonth="+ mTodayNetworkInfoEntity.mUsedTranslateForMonth
				+ ",mUsedReceiveForMonth="+ mTodayNetworkInfoEntity.mUsedReceiveForMonth
				+ ",mRetialForMonth="+ mTodayNetworkInfoEntity.mRetialForMonth
				+ ",mUsedForDay="+ mTodayNetworkInfoEntity.mUsedForDay
				+ ",mUsedTranslateForDay="+ mTodayNetworkInfoEntity.mUsedTranslateForDay
				+ ",mUsedReceiveForDay="+ mTodayNetworkInfoEntity.mUsedReceiveForDay);
		return mTodayNetworkInfoEntity;
	}
	//获取本月限制的流量，单位B
	@Override
	public long getTotalForMonth() {
		if(DEBUG)
		Log.v(TAG, "getTotalForMonth mTotalForMonth=" + mTotalForMonth);
		return mTotalForMonth;
	}
	//获取本月已用流量,单位B,并返回本于剩余流量
	@Override
	public long getUsedForMonth() {
		if(DEBUG)
		Log.v(TAG, "getUsedForMonth mUsedForMonth=" + mUsedForMonth);
		return mUsedForMonth;
	}
	//插入一条流量监控日志
	@Override
	public void insert(NetworkInfoEntity arg0) {
		if(DEBUG)
		Log.v(TAG, "NetworkInfoEntity mUsedForMonth=" + arg0.mUsedForMonth
				+ ",mTotalForMonth="+arg0.mTotalForMonth
				+ ",mUsedTranslateForMonth="+ arg0.mUsedTranslateForMonth
				+ ",mUsedReceiveForMonth="+ arg0.mUsedReceiveForMonth
				+ ",mRetialForMonth="+ arg0.mRetialForMonth
				+ ",mUsedForDay="+ arg0.mUsedForDay
				+ ",mUsedTranslateForDay="+ arg0.mUsedTranslateForDay
				+ ",mUsedReceiveForDay="+ arg0.mUsedReceiveForDay);
		mDatas.add(arg0);
	}
	//清空当天流量监控情况
	@Override
	public void resetToDayNetworkInfoEntity() {
		if(DEBUG)
		Log.v(TAG, "resetToDayNetworkInfoEntity");
		mTodayNetworkInfoEntity = new NetworkInfoEntity();

	}
	//设置月结日，只对Mobile有效，WIFI无效
	@Override
	public void setClosingDayForMonth(int arg0) {//没有调用
		if(DEBUG)
		Log.v(TAG, "setClosingDayForMonth mClosingDay=" + arg0);
		mClosingDay = arg0;
	}
	//保存上一次网络情况
	@Override
	public void setLastNetDataEntity(NetDataEntity arg0)
	{
		if(DEBUG)
		Log.v(TAG, "setLastNetDataEntity mReceiver=" + arg0.mReceiver
				+ ",mTotalForMonth="+arg0.mTranslate
				+ ",mReceiverPks="+ arg0.mReceiverPks
				+ ",mTranslatePks="+ arg0.mTranslatePks);
		mLastNetDataEntity = arg0;
	}
	//设置当天的网络流量监控情况
	@Override
	public void setTodayNetworkInfoEntity(NetworkInfoEntity arg0) {
		if(DEBUG)
		Log.v(TAG, "setTodayNetworkInfoEntity mUsedForMonth=" + arg0.mUsedForMonth
				+ ",mTotalForMonth="+arg0.mTotalForMonth
				+ ",mUsedTranslateForMonth="+ arg0.mUsedTranslateForMonth
				+ ",mUsedReceiveForMonth="+ arg0.mUsedReceiveForMonth
				+ ",mRetialForMonth="+ arg0.mRetialForMonth
				+ ",mUsedForDay="+ arg0.mUsedForDay
				+ ",mUsedTranslateForDay="+ arg0.mUsedTranslateForDay
				+ ",mUsedReceiveForDay="+ arg0.mUsedReceiveForDay);

		/*ty zouchl add start*/
		/*long mUsedForMonth = arg0.mUsedTranslateForMonth + arg0.mUsedReceiveForMonth;
		setUsedForMonth(mUsedForMonth);
		if ((mUsedForMonth > 0) && (getTotalForMonth() > 0) && (mUsedForMonth >= getTotalForMonth())){
			mContext.sendBroadcast(new Intent(ShareUtil.ACTION_AUTO_BREAK_FLOW));
			Log.d(TAG, "setTodayNetworkInfoEntity send_auto_break_flow");
		}*/
		/*ty zouchl add end*/

		mTodayNetworkInfoEntity = arg0;
	}
	//设置本月限制的流量，单位B
	@Override
	public void setTotalForMonth(long arg0) {//没有调用
		if(DEBUG)
		Log.v(TAG, "setTotalForMonth mTotalForMonth="+arg0);
		mTotalForMonth = arg0;
	}
	//设置本月已用流量,单位B
	@Override
	public void setUsedForMonth(long arg0) {
		if(DEBUG)
		Log.v(TAG, "setUsedForMonth mUsedForMonth=" + arg0);
		mUsedForMonth = arg0;
	}
	/*
	 * 系统时间改变后获取最新数据。
	 * @param mStartDate,调整系统前一次的刷新时间
	 * @return 新的数据，包括每月限额，本月已用，今日已用。
	 */
	@Override
	public NetworkInfoEntity getSystemTimeChange(Date mStartDate) {
		// TODO Auto-generated method stub
		if(DEBUG)
		Log.v(TAG, "getSystemTimeChange mStartDate=" + mStartDate);
		return null;
	}

	/* (non-Javadoc)  add by gabeli 不解其中含义，先当成清空当月的，保留当天的记录。
	 * @see tmsdk.bg.module.network.INetworkInfoDao#resetMonthNetworkinfoEntity()
	 */
	@Override
	public void resetMonthNetworkinfoEntity() {
		// TODO Auto-generated method stub
		if(DEBUG)
		Log.v(TAG, "resetMonthNetworkinfoEntity");
		mTodayNetworkInfoEntity.mUsedForMonth = 0l;
	}

}
