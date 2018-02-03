package com.guli.secmanager.Checkup;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.SubscriptSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.Utils.UnitConverter;
import com.guli.secmanager.V2MainActivity;
import com.guli.secmanager.widget.V2RoundProgressBarWidthNumber;

import org.w3c.dom.Text;

import tmsdk.bg.module.network.ITrafficCorrectionListener;
import tmsdk.bg.module.network.ProfileInfo;
import tmsdk.common.ErrorCode;
import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.TMSDKContext;
import com.guli.secmanager.flowmonitor.SmsReceiver;
import com.guli.secmanager.flowmonitor.FlowCorrectWrapper;
import com.guli.secmanager.flowmonitor.FlowSettingActivity;

public class V2FlowMonitorCheckupFragment extends Fragment {
    private final String TAG ="V2FlowMonitorCheckup";

    private RelativeLayout mAutoCalibration;
    private RelativeLayout mAutoCalibrationInner;
    private TextView mAutoCalibrationText;
    private TextView mTotalLeft, mTotalLeftMB, mTotalLeftTitle, mLeftCommonSize, mLeftFreeSize;//, mNetworkManager, mTrafficRanking;
    private TextView mWebTime, mMusicTime, mVideoTime, mChatTime;
    private V2RoundProgressBarWidthNumber mRoundProgressBar;
    private SmsReceiver receiver = new SmsReceiver();
    String mquerycode = "";
    String mqueryport = "";
    private SharedPreferences sPreferences;
    private SharedPreferences.Editor editor;
    private ActionBar mActionBar;
    private ImageButton mActionBarButton, mActionbarSettings;
    private RelativeLayout actionbarLayout;
    private Context mContext;
    private boolean isCorrected;

    private final static int rateWeb = 2 * 1024;//2M/小时
    private final static int rateMusic = 30 * 1024;
    private final static int rateVideo = 120 * 1024;
    private final static int rateChat = 1 * 1024;

    public static int mStatusColor = R.color.flow_monitor_bg_color;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (Context)getActivity().getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView enter... ");
        View v = inflater.inflate(R.layout.v2_flowmonitor_checkup_fragment, container, false);

        //setStatusBarColor(true);
        //initActionBar();

        mTotalLeft = (TextView) v.findViewById(R.id.total_left_size);
        mTotalLeftMB = (TextView) v.findViewById(R.id.MB);
        mTotalLeftTitle = (TextView) v.findViewById(R.id.total_left_title);

        /*mDetail = (TextView) v.findViewById(R.id.detail);
        if(true == UnitConverter.isTosApkInstalled(mContext)){
            mDetail.setOnClickListener(mOnClickListener);
        }else{
            mDetail.setVisibility(View.GONE);
        }*/

        //mLeftCommonSize = (TextView) v.findViewById(R.id.total_common_size);
        //mLeftFreeSize = (TextView) v.findViewById(R.id.left_free_size);
        mLeftCommonSize = (TextView) v.findViewById(R.id.total_common_size);
        mLeftFreeSize = (TextView) v.findViewById(R.id.left_free_size);
        mAutoCalibration = (RelativeLayout)v.findViewById(R.id.round_progress_layout);
        mAutoCalibrationInner = (RelativeLayout)v.findViewById(R.id.round_progress_inner_layout);
        mAutoCalibrationText = (TextView) v.findViewById(R.id.tv_autocalibration);
        //UnitConverter.updateButtonBg2(mContext, mAutoCalibration);

        mWebTime = (TextView) v.findViewById(R.id.webpage_time);
        mMusicTime = (TextView) v.findViewById(R.id.music_time);
        mVideoTime = (TextView) v.findViewById(R.id.video_time);
        mChatTime = (TextView) v.findViewById(R.id.chat_time);

        mRoundProgressBar = (V2RoundProgressBarWidthNumber) v.findViewById(R.id.round_progress_bar);

        mRoundProgressBar.setOnClickListener(mOnClickListener);
        mAutoCalibrationInner.setOnClickListener(mOnClickListener);
        mAutoCalibrationText.setOnClickListener(mOnClickListener);

        sPreferences = getActivity().getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
        sPreferences.registerOnSharedPreferenceChangeListener(mSharePrefChangeListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ShareUtil.ACTION_SET_USED_FOR_MONTH);
        mContext.registerReceiver(mReceiver, filter);

        setFlowLeftSize();

        return v;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onResume () {
        //setFlowLeftSize();

        super.onResume();
        Log.i(TAG, "onResume Enter...");
    }

    private void setFlowLeftSize(){
        int iCommonTotal = sPreferences.getInt(ShareUtil.SIM1_COMMON_TOTAL_KBYTES, 0);
        int iFreeTotal = sPreferences.getInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, 0);
        int iCommonLeft = sPreferences.getInt(ShareUtil.SIM1_COMMON_LEFT_KBYTES, 0);
        int iFreeLeft = sPreferences.getInt(ShareUtil.SIM1_FREE_LEFT_KBYTES, 0);
        Log.v(TAG, "iCommonTotal="+iCommonTotal+",iFreeTotal="+iFreeTotal);

        if(((iCommonLeft + iFreeLeft) > 0) && ((iCommonTotal + iFreeTotal) > 0)) {
            //运营商只返回剩余流量
            int mPercentage = ((iCommonLeft + iFreeLeft) * 100) / (iCommonTotal + iFreeTotal);

            mRoundProgressBar.setProgress(mPercentage);
            mRoundProgressBar.setCircleProgressBarColor(getResources().getColor(R.color.progress_gray));
            mRoundProgressBar.invalidate();

            mTotalLeft.setText(String.valueOf((iCommonLeft + iFreeLeft) / 1024));
            mTotalLeft.setTextColor(getResources().getColor(R.color.blue));
            mTotalLeftMB.setTextColor(getResources().getColor(R.color.blue));
            mTotalLeftTitle.setTextColor(getResources().getColor(R.color.blue));
            mAutoCalibrationText.setTextColor(getResources().getColor(R.color.blue));
        }
        else {
            mRoundProgressBar.setProgress(100);
            mRoundProgressBar.setCircleProgressBarColor(getResources().getColor(R.color.orange));
            mRoundProgressBar.invalidate();

            mTotalLeft.setText("0");
            mTotalLeft.setTextColor(getResources().getColor(R.color.orange));
            mTotalLeftMB.setTextColor(getResources().getColor(R.color.orange));
            mTotalLeftTitle.setTextColor(getResources().getColor(R.color.orange));
            mAutoCalibrationText.setTextColor(getResources().getColor(R.color.orange));
        }

        //不能区分当前是4G或标准流量。暂时显示相同
        if(iCommonLeft > 0){
            mLeftCommonSize.setText(String.valueOf(iCommonLeft / 1024) + "M");
            mLeftCommonSize.setTextColor(getResources().getColor(R.color.blue));
        }else{
            mLeftCommonSize.setText("0" + "M");
            mLeftCommonSize.setTextColor(getResources().getColor(R.color.orange));
        }

        if(iFreeLeft > 0){
            mLeftFreeSize.setText(String.valueOf(iFreeLeft / 1024) + "M");
            mLeftFreeSize.setTextColor(getResources().getColor(R.color.blue));
        }else{
            mLeftFreeSize.setText("0" + "M");
            mLeftFreeSize.setTextColor(getResources().getColor(R.color.orange));
        }

        mWebTime.setText(String.valueOf((iCommonLeft + iFreeLeft) > 0 ? (iCommonLeft + iFreeLeft) / rateWeb : 0));
        mMusicTime.setText(String.valueOf((iCommonLeft + iFreeLeft) > 0 ? (iCommonLeft + iFreeLeft) / rateMusic : 0));
        mVideoTime.setText(String.valueOf((iCommonLeft + iFreeLeft) > 0 ? (iCommonLeft + iFreeLeft) / rateVideo : 0));
        mChatTime.setText(String.valueOf((iCommonLeft + iFreeLeft) > 0 ? (iCommonLeft + iFreeLeft) / rateChat : 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FlowCorrectWrapper.getInstance().setTrafficCorrectionListener(null);
        mContext.unregisterReceiver(mReceiver);
        sPreferences.unregisterOnSharedPreferenceChangeListener(mSharePrefChangeListener);
        uiHandler.removeCallbacksAndMessages(null);
        //unregisterReceiver(receiver);
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.round_progress_bar || v.getId() == R.id.round_progress_layout
                    || v.getId() == R.id.round_progress_inner_layout || v.getId() == R.id.tv_autocalibration){
                TelephonyManager telephonyManager = (TelephonyManager)getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
                init();
                int resultcode = FlowCorrectWrapper.getInstance().requeatProfileInfo(IDualPhoneInfoFetcher.FIRST_SIM_INDEX);
                Log.d("FlowMonitorActivity", "now resultcode is =" + String.valueOf(resultcode) + "errorcode=" + ErrorCode.ERR_NONE);
                if(resultcode == ErrorCode.ERR_NONE){
                    mAutoCalibrationText.setText(R.string.flow_correcting);
                    mRoundProgressBar.setClickable(false);
                    mAutoCalibrationInner.setClickable(false);
                    mAutoCalibrationText.setClickable(false);
                }else{
                    Toast.makeText(mContext, R.string.correct_failed, Toast.LENGTH_LONG).show();
                }
                /** SharedPreferences sharedPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
                 String imsi = sharedPreferences.getString(ShareUtil.SIMI_IMSI, "-1");
                 String currentImsi = telephonyManager.getSubscriberId();
                 SharedPreferences.Editor editor = sharedPreferences.edit();
                 editor.putString(ShareUtil.SIMI_IMSI, currentImsi);
                 editor.commit();
                 Log.d("TrafficCorrection", "wanqch imsi=" + imsi + "   " + "currentImsi=" + currentImsi);
                 if(!imsi.equals("-1") && !currentImsi.equals(imsi)){*/
                Log.d("TrafficCorrection","now is TMSDKContext.onImsiChanged();");
                isCorrected = false;

                Message message = new Message();
                message.what = 5;
                uiHandler.sendMessageDelayed(message, 20000);

                // }

            }else if(v.getId() == R.id.actionbar_icon){
                getActivity().finish();
            }else if(v.getId() == R.id.actionbar_settings){
                Intent intent = new Intent();
                intent.setClass(getActivity(), FlowSettingActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
            }
        }
    };

    private void setStatusBarColor(boolean flag) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int mStatusColor = this.getResources().getColor(R.color.flow_monitor_bg_color);
            getActivity().getWindow().setStatusBarColor(mStatusColor);
        }
    }


    public void init(){
        FlowCorrectWrapper.getInstance().init(mContext);

        FlowCorrectWrapper.getInstance().setTrafficCorrectionListener(new ITrafficCorrectionListener() {

            @Override
            public void onNeedSmsCorrection(int simIndex, String queryCode, String queryPort) {
                android.util.Log.v("FlowMonitorActivity", "onNeedSmsCorrection--simIndex:[" + simIndex + "]--queryCode:[" +
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
                Toast.makeText(mContext, R.string.correct_finish, Toast.LENGTH_LONG).show();
                android.util.Log.v("FlowMonitorActivity", "wangqch onTrafficNotify-");
                //mAutoCalibration.setClickable(true);
                Message message = new Message();
                message.what = 4;
                uiHandler.sendMessage(message);
                //setFlowLeftSize();
            }

            @Override
            public void onProfileNotify(int i, ProfileInfo profileInfo) {
                super.onProfileNotify(i, profileInfo);
                Log.d("wangqch", "now is in onProfileNotify");
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
                String closingDay = sharedPreferences.getString(ShareUtil.ACCOUNT_DATE,"1");
                int result = FlowCorrectWrapper.getInstance().setConfig(i,
                        String.valueOf(profileInfo.province), String.valueOf(profileInfo.city), profileInfo.carry, String.valueOf(profileInfo.brand), Integer.parseInt(closingDay));
                if(result != ErrorCode.ERR_NONE){
                    Toast.makeText(mContext, R.string.correct_failed, Toast.LENGTH_LONG).show();
                    Message message = new Message();
                    message.what = 4;
                    uiHandler.sendMessage(message);
                    return;
                }
                Log.d("wangqch", "result=" + String.valueOf(profileInfo.province) + " " + String.valueOf(profileInfo.city) + " " + profileInfo.carry + " " + String.valueOf(profileInfo.brand));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ShareUtil.PROVINCE,String.valueOf(profileInfo.province));
                editor.putString(ShareUtil.CITY,String.valueOf(profileInfo.city));
                editor.putString(ShareUtil.OPERATOR,profileInfo.carry);
                editor.putString(ShareUtil.BRAND,String.valueOf(profileInfo.brand));
                editor.commit();

                int retCode = FlowCorrectWrapper.getInstance().startCorrection(IDualPhoneInfoFetcher.FIRST_SIM_INDEX);
                if(retCode != ErrorCode.ERR_NONE){
                    Toast.makeText(mContext, R.string.correct_failed, Toast.LENGTH_LONG).show();
                    Message message = new Message();
                    message.what = 4;
                    uiHandler.sendMessage(message);
                }

            }

            @Override
            public void onError(int simIndex, int errorCode) {
                Toast.makeText(mContext, R.string.correct_failed, Toast.LENGTH_LONG).show();
                Message message = new Message();
                message.what = 4;
                uiHandler.sendMessage(message);

                if (IDualPhoneInfoFetcher.FIRST_SIM_INDEX == simIndex) {
                    // mTVSim1Detail.setText(strState);
                } else if (IDualPhoneInfoFetcher.SECOND_SIM_INDEX == simIndex) {
                    // mTVSim2Detail.setText(strState);
                }
                android.util.Log.v("FlowMonitorActivity", "onError--simIndex:[" + simIndex + "]errorCode:[" + errorCode + "]");
            }
        });



    }

    /**
     * 校正成功提示
     */
    //private final int MSG_TRAFfICT_NOTIFY = 0x1a;

    /***
     * 需要发送短信
     */
    //private final int MSG_NEED_SEND_MSG = 0x1b;


    Handler uiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    String message = msg.obj.toString();
                    startanalysisSMS(message);
                    Log.d("wangqch","now message content is"+message);
                    break;
                case 4:
                    mAutoCalibrationText.setText(R.string.flow_click_correct);
                    mRoundProgressBar.setOnClickListener(mOnClickListener);
                    mAutoCalibrationInner.setOnClickListener(mOnClickListener);
                    mAutoCalibrationText.setOnClickListener(mOnClickListener);
                    isCorrected = true;
                    this.removeMessages(5);
                    break;
                case 5:
                    if(!isCorrected){
                        mAutoCalibrationText.setText(R.string.flow_click_correct);
                        mRoundProgressBar.setOnClickListener(mOnClickListener);
                        mAutoCalibrationInner.setOnClickListener(mOnClickListener);
                        mAutoCalibrationText.setOnClickListener(mOnClickListener);
                        Toast.makeText(mContext, R.string.try_later, Toast.LENGTH_LONG).show();
                    }
            }
        }
    };

    public void startanalysisSMS(final String message){
        new Thread(new Runnable(){
            @Override
            public void run() {
                String strDetail = "";

                FlowCorrectWrapper.getInstance().analysisSMS(IDualPhoneInfoFetcher.FIRST_SIM_INDEX,
                        mquerycode,
                        mqueryport,
                        message);
                strDetail += "[" + mquerycode + "][" + mqueryport + "]\n[" +
                        message + "]\n";
            }

        }).start();
    }

    public SharedPreferences.OnSharedPreferenceChangeListener mSharePrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals(ShareUtil.SIM1_COMMON_LEFT_KBYTES)
                    || key.equals(ShareUtil.SIM1_FREE_LEFT_KBYTES)){
                Log.e("wanghg", "onSharedPreferenceChanged key=" + key + " , value=" + sPreferences.getInt(key, 0));
                setFlowLeftSize();
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            String action = intent.getAction();
            Log.v(TAG, "Received action=" + action);
            if(action.equals(ShareUtil.ACTION_SET_USED_FOR_MONTH)){
                setFlowLeftSize();
            }
        }
    };
}