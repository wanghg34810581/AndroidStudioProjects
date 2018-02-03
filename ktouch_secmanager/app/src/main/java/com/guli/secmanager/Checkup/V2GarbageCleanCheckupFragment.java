package com.guli.secmanager.Checkup;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guli.secmanager.GarbageClean.V2GarbageCleanActivity;
import com.guli.secmanager.R;
import com.guli.secmanager.Utils.FileSizeFormatter;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.widget.V2RoundProgressBarWidthNumber;
import com.guli.secmanager.widget.HorizontalProgressBarWithNumber;

import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.RubbishEntity;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.deepclean.ScanProcessListener;


/**
 * Created by wanghg on 16-8-6.
 */
public class V2GarbageCleanCheckupFragment extends Fragment {
    public static final String TAG = "V2GarbageCleanCheckup";

    private ImageButton mGarbageClean;
    private RelativeLayout mRoundProgressLayout;
    private RelativeLayout mRoundProgressLayout_Inner;
    private V2RoundProgressBarWidthNumber mRoundProgressBar;
    private TextView mGarbageCleanInfo;

    public static int mStatusColor = R.color.green;

    private boolean mFirstRunFlag;
    private DeepcleanManager mDeepcleanManager;
    private Handler mHandler = new Handler();

    public V2GarbageCleanCheckupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate ... ");
        super.onCreate(savedInstanceState);

        mFirstRunFlag = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView enter... ");
        View v = inflater.inflate(R.layout.v2_garbageclean_checkup_fragment, container, false);

        // 功能菜单相关控件
        mGarbageClean = (ImageButton) v.findViewById(R.id.iv_garbage_clean);
        mRoundProgressLayout = (RelativeLayout) v.findViewById(R.id.round_progress_layout);
        mRoundProgressLayout_Inner = (RelativeLayout) v.findViewById(R.id.round_progress_inner_layout);
        mRoundProgressBar = (V2RoundProgressBarWidthNumber) v.findViewById(R.id.round_progress_bar);
        mGarbageClean.setOnClickListener(mOnClickListener);
        mRoundProgressBar.setOnClickListener(mOnClickListener);
        mRoundProgressLayout_Inner.setOnClickListener(mOnClickListener);
        mGarbageCleanInfo = (TextView) v.findViewById(R.id.garbage_clean_info);

        setGarbageCleanInfo(0);

        SDCardInfo sdCardInfo = getSDCardInfo();
        if (sdCardInfo != null) {
            //Log.i("wanghg", "sdCardInfo.total : " + sdCardInfo.total);
            //Log.i("wanghg", "sdCardInfo.free : " + sdCardInfo.free);
            ((TextView) v.findViewById(R.id.tv_memory_total)).setText(FileSizeFormatter.transformShortType(sdCardInfo.total) + getString(R.string.B));
            ((TextView) v.findViewById(R.id.tv_memory_used)).setText(FileSizeFormatter.transformShortType(sdCardInfo.total - sdCardInfo.free) + getString(R.string.B) + "/");
            ((HorizontalProgressBarWithNumber) v.findViewById(R.id.pb_garbage_clean)).setProgress((int)(((sdCardInfo.total - sdCardInfo.free) * 100) / sdCardInfo.total));
        }

        return v;
    }

    public void setGarbageCleanInfo(long rubbishSize) {
        if(rubbishSize > 0) {
            mGarbageCleanInfo.setText(getString(R.string.garbage_clean_info1));
        }
        else {
            SharedPreferences sPreferences = getActivity().getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
            Long total = sPreferences.getLong(ShareUtil.GARBAGE_TOTAL, 0);
            if(total > 0) {
                String totalSize = FileSizeFormatter.transformShortType(total) + getActivity().getResources().getString(R.string.B);
                mGarbageCleanInfo.setText(getString(R.string.garbage_clean_info2, totalSize));
            }
            else {
                mGarbageCleanInfo.setText(getString(R.string.garbage_clean_info1));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mFirstRunFlag) {
            mDeepcleanManager = ManagerCreatorF.getManager(DeepcleanManager.class);
            if(!mDeepcleanManager.init(new ScanProcessListener() {
                @Override
                public void onScanStarted() { Log.i(TAG, "onScanStarted : ");}
                @Override
                public void onScanProcessChange(int nowPercent, String scanPath) {Log.w(TAG, "onScanProcessChange : "+nowPercent+" %");}
                @Override
                public void onScanFinished() {Log.i(TAG, "onScanFinished : ");}
                @Override
                public void onScanCanceled() {
                    Log.i(TAG, "onScanCanceled : ");
                }
                @Override
                public void onScanError(int error) {Log.i(TAG, "onScanError : ");}
                @Override
                public void onRubbishFound(RubbishEntity aRubbish) {
                    Log.i(TAG, "onRubbishFound : ");
                    if(aRubbish.getSize() > 0) {
                        stopScanTask();
                        if(mHandler != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setGarbageCleanInfo(1);
                                }
                            });
                        }
                    }
                }
                @Override
                public void onCleanStart() {Log.i(TAG, "onCleanStart : ");}
                @Override
                public void onCleanProcessChange(long currenCleanSize, int nowPercent) {Log.i(TAG, "onCleanProcessChange : " + nowPercent + "% ::" + currenCleanSize);}
                @Override
                public void onCleanCancel() {Log.i(TAG, "onCleanCancel : ");}
                @Override
                public void onCleanFinish() {Log.i(TAG, "onCleanFinish : ");}
                @Override
                public void onCleanError(int error) {Log.i(TAG, "onCleanError : ");}
            })) {
                //初始化错误操作
                Log.e(TAG,"DeepcleanManager Init mScanProcessListener Error");
            }
            int flags = RubbishType.SCAN_FLAG_ALL;
            if(!mDeepcleanManager.startScan(flags)) {
                Log.e(TAG, "--->>> please call mDeepcleanManager.init() first!");
            }
            mFirstRunFlag = false;
        }

        mGarbageClean.setClickable(true);
        mRoundProgressBar.setClickable(true);
        mRoundProgressLayout_Inner.setClickable(true);
        Log.i(TAG, "onResume Enter...");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause Enter...");
    }

    // 从宿主Activity调用, onPause调用
    public void stopScanTask() {
        try {
            if (mDeepcleanManager != null) {
                mDeepcleanManager.cancelScan();
                mDeepcleanManager.onDestory();
                mDeepcleanManager = null;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error post mHealthScaner.stopScan msg to ui thread.", ex);
        }
    }

    @Override
    public void onDestroy() {
        stopScanTask();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                if(resultCode == Activity.RESULT_OK) {
                    setGarbageCleanInfo(0);
                }

                mRoundProgressLayout.addView(mRoundProgressBar);
                mRoundProgressLayout.addView(mRoundProgressLayout_Inner);
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            mGarbageClean.setClickable(false);
            mRoundProgressBar.setClickable(false);
            mRoundProgressLayout_Inner.setClickable(false);

            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0, 1, 0,
                                                              Animation.RELATIVE_TO_SELF, 0.5f,
                                                              Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(500);
            scaleAnimation.setAnimationListener(new ScaleAnimationListener(1));
            animationSet.addAnimation(scaleAnimation);
            mRoundProgressLayout_Inner.startAnimation(animationSet);
        }
    };

    private class ScaleAnimationListener implements Animation.AnimationListener {
        int mOrder;

        ScaleAnimationListener(int order) {
            mOrder = order;
        }

        @Override
        public void onAnimationStart(Animation var1) {

        };

        @Override
        public void onAnimationEnd(Animation var1) {
            if(mOrder == 1) {
                mRoundProgressLayout.removeView(mRoundProgressLayout_Inner);

                AnimationSet animationSet = new AnimationSet(true);
                ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0, 1, 0,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(500);
                scaleAnimation.setAnimationListener(new ScaleAnimationListener(2));
                animationSet.addAnimation(scaleAnimation);
                mRoundProgressLayout.startAnimation(animationSet);
            }
            else if(mOrder == 2) {
                mRoundProgressLayout.removeView(mRoundProgressBar);

                startActivityForResult(new Intent(getActivity(), V2GarbageCleanActivity.class), 100);
            }
        };

        @Override
        public void onAnimationRepeat(Animation var1) {

        };
    };


    public static class SDCardInfo {
        public long total;

        public long free;
    }

    public static SDCardInfo getSDCardInfo() {
        String sDcString = android.os.Environment.getExternalStorageState();

        if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
            java.io.File pathFile = android.os.Environment.getExternalStorageDirectory();

            try {
                android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());

                // 閼惧嘲褰嘢DCard娑撳たLOCK閹粯鏆?
                long nTotalBlocks = statfs.getBlockCount();

                // 閼惧嘲褰嘢DCard娑撳﹥鐦℃稉鐚檒ock閻ㄥ嚪IZE
                long nBlocSize = statfs.getBlockSize();

                // 閼惧嘲褰囬崣顖欑返缁嬪绨担璺ㄦ暏閻ㄥ嚉lock閻ㄥ嫭鏆熼柌锟?
                long nAvailaBlock = statfs.getAvailableBlocks();

                // 閼惧嘲褰囬崜鈺€绗呴惃鍕閺堝lock閻ㄥ嫭鏆熼柌锟介崠鍛妫板嫮鏆€閻ㄥ嫪绔撮懜顒傗柤鎼村繑妫ゅ▔鏇氬▏閻劎娈戦崸锟?
                long nFreeBlock = statfs.getFreeBlocks();

                SDCardInfo info = new SDCardInfo();
                // 鐠侊紕鐣籗DCard 閹顔愰柌蹇撱亣鐏忓粰B
                info.total = nTotalBlocks * nBlocSize;

                // 鐠侊紕鐣?SDCard 閸撯晙缍戞径褍鐨琈B
                info.free = nAvailaBlock * nBlocSize;

                return info;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.toString());
            }
        }

        return null;
    }
}
