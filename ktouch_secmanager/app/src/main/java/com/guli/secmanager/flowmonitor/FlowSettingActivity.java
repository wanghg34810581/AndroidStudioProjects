package com.guli.secmanager.flowmonitor;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.guli.secmanager.*;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.VirusScan.Utils;


/**
 * Created by wangqch on 16-4-12.
 */
public class FlowSettingActivity extends AppCompatActivity {
    private final String TAG = "FlowSettingActivity";

    private TYAplaSwitch autoCorrectFlow;
    private TYAplaSwitch autoBreakFlow;
    private NetworkMonitor mNetworkMonitor;
    private RelativeLayout manualSetFlow;
    private RelativeLayout settingInformation;
    private RelativeLayout everyCheckoutLayout;
    private RelativeLayout autoBreakLayout;
    private Button saveButton;
    private ImageButton actionButton;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(true);
        setContentView(R.layout.activity_flowsetting);
        initActionBar();
        manualSetFlow = (RelativeLayout) findViewById(R.id.flow_manual_set);
        settingInformation = (RelativeLayout) findViewById(R.id.write_operator_layout);
        everyCheckoutLayout = (RelativeLayout)findViewById(R.id.everycheck_layout);
        autoBreakLayout = (RelativeLayout)findViewById(R.id.net_over_layout);
        saveButton = (Button)findViewById(R.id.save);
        setClickResource();
        autoBreakFlow = (TYAplaSwitch)findViewById(R.id.auto_break_flow);
        autoCorrectFlow = (TYAplaSwitch)findViewById(R.id.auto_correct_flow);
        autoBreakFlow.setOnCheckedChangeListener(autoBreakListener);
        autoCorrectFlow.setOnCheckedChangeListener(autoCorrectListener);
        manualSetFlow.setOnClickListener(manualSetFlowListener);
        settingInformation.setOnClickListener(settingListener);
        saveButton.setOnClickListener(saveListener);

        SharedPreferences sPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Activity.MODE_PRIVATE);
        Boolean isCorrect = sPreferences.getBoolean(ShareUtil.AUTO_CORRECT_STATE, true);
        Boolean isBreak = sPreferences.getBoolean(ShareUtil.AUTO_BREAK_STATE,false);
        autoCorrectFlow.setChecked(isCorrect);
       // if(isCorrect){
       //     FlowAutoCorrectService.setAlarm(this,true);
       // }else{
       ///     FlowAutoCorrectService.setAlarm(this,false);
       // }
        Log.d(TAG, "isBreak=" + isBreak);
        autoBreakFlow.setChecked(isBreak);

    }


    private View.OnClickListener manualSetFlowListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(FlowSettingActivity.this, FlowManuallySetings.class);
            startActivity(intent);

        }
    };

    private View.OnClickListener settingListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(FlowSettingActivity.this, OperatorActivity.class);
            startActivity(intent);

        }
    };


    private View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Boolean isAutoCorrect = autoCorrectFlow.isChecked();
            Boolean isAutoBreak = autoBreakFlow.isChecked();
            SharedPreferences sharedPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ShareUtil.AUTO_CORRECT_STATE,isAutoCorrect);
            editor.putBoolean(ShareUtil.AUTO_BREAK_STATE,isAutoBreak);
            editor.commit();

            mNetworkMonitor = new NetworkMonitor(getApplicationContext());
            //test
            //mNetworkMonitor.getLastDate();
            //test
            //if(true == isAutoBreak){
                mNetworkMonitor.setNetworkManagerState(true);
            //}else{
                //mNetworkMonitor.setNetworkManagerState(false);
            //}
            finish();

        }
    };

    private OnCheckedChangeListener autoBreakListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "autoBreakButton isChecked=" + isChecked);
                if(true == isChecked){
                    SharedPreferences sPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, MODE_PRIVATE);
                    int mTotalFlow = sPreferences.getInt(ShareUtil.SIM1_COMMON_TOTAL_KBYTES, 0) + sPreferences.getInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, 0);
                    if(mTotalFlow <= 0){
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(FlowSettingActivity.this);
                        builder.setMessage(R.string.set_flow_total);
                        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();;
                       // buttonView.toggle();
                        autoBreakFlow.setChecked(false);
                    }
                }
        }
    };

    private OnCheckedChangeListener autoCorrectListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(true == isChecked){
                Log.d("FlowSettings", "wangqch now is on OnCheckedChangeListener");
                if(!FlowAutoCorrectService.isServiceAlarmOn(FlowSettingActivity.this)){
                    FlowAutoCorrectService.setAlarm(FlowSettingActivity.this,true);
                }
            }else{
                FlowAutoCorrectService.setAlarm(FlowSettingActivity.this, false);
            }

        }
    };


    private void setStatusBarColor(boolean flag) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int mStatusColor;
            if(flag) {
                mStatusColor = this.getResources().getColor(R.color.flow_monitor_bg_color);
            } else {
                mStatusColor = this.getResources().getColor(R.color.orange);
            }
            this.getWindow().setStatusBarColor(mStatusColor);
        }
    }

    private void initActionBar() {
        actionButton = (ImageButton)findViewById(R.id.actionbar_icon);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            actionButton.setBackgroundResource(outValue.resourceId);
        }else{
            actionButton.setBackgroundResource(R.drawable.ic_arrow_black_selectors);
        }

    }

    private void setClickResource(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            manualSetFlow.setBackgroundResource(outValue.resourceId);
            settingInformation.setBackgroundResource(outValue.resourceId);
            everyCheckoutLayout.setBackgroundResource(outValue.resourceId);
            autoBreakLayout.setBackgroundResource(outValue.resourceId);
            saveButton.setBackgroundResource(outValue.resourceId);
        } else {
            //manualSetFlow.setBackgroundResource(R.drawable.complete_btn_on_selector);
            //settingInformation.setBackgroundResource(R.drawable.complete_btn_on_selector);
            //everyCheckoutLayout.setBackgroundResource(R.drawable.complete_btn_on_selector);
            //autoBreakLayout.setBackgroundResource(R.drawable.complete_btn_on_selector);
            saveButton.setBackgroundResource(R.drawable.complete_btn_on_selector);
        }
    }
}

