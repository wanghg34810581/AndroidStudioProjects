package com.guli.secmanager.Checkup;


import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.ApkInfoUtil;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.V2MainActivity;
import com.guli.secmanager.VirusScan.V2VirusScanActivity;
import com.guli.secmanager.widget.V2RoundProgressBarWidthNumber;


/**
 * Created by wanghg on 16-8-6.
 */
public class V2VirusScanCheckupFragment extends Fragment {
    public static final String TAG = "V2VirusScanCheckup";

    private ImageButton mVirusScan;
    private TextView mTvVirusScan;
    private TextView mVirusScanInfo;
    private RelativeLayout mRoundProgressLayout;
    private RelativeLayout mRoundProgressLayout_Inner;
    private V2RoundProgressBarWidthNumber mRoundProgressBar;
    private TableLayout mProtectInfo;
    private LinearLayout mPackageLayout;

    public static int mStatusColor = R.color.green;

    public V2VirusScanCheckupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate ... ");
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView enter... ");
        View v = inflater.inflate(R.layout.v2_virusscan_checkup_fragment, container, false);

        // 功能菜单相关控件
        mVirusScan = (ImageButton) v.findViewById(R.id.iv_virus_scan);
        mTvVirusScan = (TextView) v.findViewById(R.id.tv_virus_scan);
        mVirusScanInfo = (TextView) v.findViewById(R.id.virus_scan_info);
        mRoundProgressLayout = (RelativeLayout) v.findViewById(R.id.round_progress_layout);
        mRoundProgressLayout_Inner = (RelativeLayout) v.findViewById(R.id.round_progress_inner_layout);
        mRoundProgressBar = (V2RoundProgressBarWidthNumber) v.findViewById(R.id.round_progress_bar);
        mProtectInfo = (TableLayout) v.findViewById(R.id.protect_info);
        mPackageLayout = (LinearLayout) v.findViewById(R.id.package_layout);

        mVirusScan.setOnClickListener(mOnClickListener);
        mRoundProgressBar.setOnClickListener(mOnClickListener);
        mRoundProgressLayout_Inner.setOnClickListener(mOnClickListener);

        mStatusColor = R.color.green;
        switchScreenMode(null);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume Enter...");

        mVirusScan.setClickable(true);
        mRoundProgressBar.setClickable(true);
        mRoundProgressLayout_Inner.setClickable(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause Enter...");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 200:
                Log.i("wanghg", "onActivityResult : " + requestCode);
                switchScreenMode(data);

                if (mRoundProgressBar.getParent() == null) {
                    mRoundProgressLayout.addView(mRoundProgressBar);
                }
                if (mRoundProgressLayout_Inner.getParent() == null) {
                    mRoundProgressLayout.addView(mRoundProgressLayout_Inner);
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void switchScreenMode(Intent data) {
        SharedPreferences sPreferences = getActivity().getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
        int bugCount = sPreferences.getInt(ShareUtil.VIRUS_COUNT, 0);

        if(bugCount > 0) {
            mStatusColor = R.color.orange;
            mVirusScan.setImageResource(R.drawable.home_icon_virus_scan_warning);
            mTvVirusScan.setText(getString(R.string.repair));
            mTvVirusScan.setTextColor(getResources().getColor(R.color.orange));
            mVirusScanInfo.setText(getString(R.string.virus_scan_info2, bugCount));
            mVirusScanInfo.setTextColor(getResources().getColor(R.color.orange));
            mRoundProgressBar.setCircleProgressBarColor(getResources().getColor(R.color.orange));
            ((V2MainActivity)getActivity()).setBackgroundColor(R.color.orange, 1);
            mProtectInfo.setVisibility(View.GONE);
            mPackageLayout.setVisibility(View.VISIBLE);

            int n = 0;
            LinearLayout layout = null;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            mPackageLayout.removeAllViews();
            //for(int j = 0; j < 4; j++)
            for(int i = 0; i < bugCount; i++) {
                //String path = b.getString("package" + (i + 1));
                String path = sPreferences.getString(ShareUtil.VIRUS_PACKAGE_NAME + (i + 1), null);
                Log.i("wanghg", "path : " + path);
                if(path == null) {continue;}

                Drawable drawable = ApkInfoUtil.getApkIcon(getContext(), path);
                if(drawable == null) {continue;}

                if(n == 0) {
                    if(layout != null) {
                        mPackageLayout.addView(layout);
                    }
                    layout = new LinearLayout(getContext());
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.setLayoutParams(layoutParams);
                }
                n++;
                if(n >= 5) {n = 0;}

                ImageView iv = new ImageView(getContext());
                iv.setImageDrawable(drawable);
                layout.addView(iv);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
                params.setMargins(20, 10, 20, 10);
                iv.setLayoutParams(params);
            }

            if(n > 0) {
                if(layout != null) {
                    mPackageLayout.addView(layout);
                }
            }
        }
        else {
            boolean isFromFinish = false;
            if(data != null) {
                Bundle b = data.getExtras();
                isFromFinish = b.getBoolean("fromFinish", false);
            }

            mStatusColor = R.color.green;
            mVirusScan.setImageResource(R.drawable.home_icon_virus_scan);
            mTvVirusScan.setText(getString(R.string.akey_garbage_scan));
            mTvVirusScan.setTextColor(getResources().getColor(R.color.green));
            if(isFromFinish) {
                mVirusScanInfo.setText(getString(R.string.virus_scan_info3));
            }
            else {
                mVirusScanInfo.setText(getString(R.string.virus_scan_info1));
            }
            mVirusScanInfo.setTextColor(getResources().getColor(R.color.green));
            mRoundProgressBar.setCircleProgressBarColor(getResources().getColor(R.color.green));
            ((V2MainActivity)getActivity()).setBackgroundColor(R.color.green, 1);
            mProtectInfo.setVisibility(View.VISIBLE);
            mPackageLayout.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            mVirusScan.setClickable(false);
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

                startActivityForResult(new Intent(getActivity(), V2VirusScanActivity.class), 200);
            }
        };

        @Override
        public void onAnimationRepeat(Animation var1) {

        };
    };
}