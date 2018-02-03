/**
 * 
 */
package com.guli.secmanager.flowmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.guli.secmanager.Utils.ShareUtil;
import java.util.ArrayList;

import tmsdk.bg.module.network.NetworkManager;
import tmsdk.common.module.network.TrafficEntity;

/**
 * 测试代码后续删除
 * 
 * @author zouchl
 *
 */
public class AppTrafficTest {
	private static final String TAG = "flow-AppTrafficTest";
    private static final String PKGNAME = "com.tencent.mtt";

	private NetworkManager mManager;
	private Context mContext;
	private ArrayList<TrafficEntity> mDatas;
	private String[] pkgs;
	/**
	 * 开始检测
	 * @param pkg 检测的应用包名
	 * @param manager 
	 * @return 是否成功
	 */
	public boolean  start(NetworkManager manager, Context context) {
		mManager = manager;
		mContext = context;
		pkgs = new String[]{PKGNAME};
		/**
		 * clearTrafficInfo()接口用于初始化应用在TMSDK中的数据。
		 */
		manager.clearTrafficInfo(pkgs);
		/**
		 * 这个refreshTrafficInfo()接口，适用于开始检测流量数据。
		 */
        mDatas = mManager.refreshTrafficInfo(pkgs, true);
        Log.e(TAG, "mDatas="+mDatas);
		if(mDatas != null && mDatas.size() > 0) {
            Log.e(TAG, "mDatas.size="+mDatas.size());
			registerNetStateReceiver();
			return true;
		}
		return false;
	}
	
	/**
	 * 结束检测
	 */
	public void destory() {
		if(mReceiver != null) {
			mContext.unregisterReceiver(mReceiver);
		}
		mDatas = null;
	}
	/**
	 * 获取最新数据
	 * @return
	 */
	public ArrayList<TrafficEntity> getLastData() {
        mDatas = mManager.refreshTrafficInfo(pkgs, true);

        if(mDatas != null)
		Log.v(TAG, "mDatas = " + mDatas.get(0).toString());
		return mDatas;
	}
	
	/* 
	 * 手动注册网络状态变化，或在清单文件配置。 
	 */  
	private BroadcastReceiver mReceiver;
	private void registerNetStateReceiver() {
        Log.v(TAG, "registerNetStateReceiver");

		IntentFilter mfilter = new IntentFilter();
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				ConnectivityManager connectivityManager = (ConnectivityManager) mContext
		                .getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
				if(networkInfo==null){
					Log.v(TAG, "networkinfo = null");
				}else if (networkInfo.getTypeName()==null){
					Log.v(TAG, "networkInfo.getTypeName()==null");
				}else if(networkInfo!=null && networkInfo.getTypeName() != null) {
					Log.v(TAG, "网络状态改变action=" + action + "  && networkInfo =" + networkInfo != null ? networkInfo.getTypeName() : "null");
				}
				
				/**
				 * 每次网络变化需要刷新检测数据
				 */
				//mManager.refreshTrafficInfo(mDatas);
				mDatas = mManager.refreshTrafficInfo(pkgs, true);
				//Log.v(TAG, "mDatas = " + mDatas.get(0).toString());
			}
		};
	    mfilter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
	    mContext.registerReceiver(mReceiver, mfilter);
	}  
	
	
}
