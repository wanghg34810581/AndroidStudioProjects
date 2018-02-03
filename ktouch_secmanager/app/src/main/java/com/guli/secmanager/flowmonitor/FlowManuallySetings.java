package com.guli.secmanager.flowmonitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.Utils.UnitConverter;
import com.guli.secmanager.Utils.Util;
import com.guli.secmanager.VirusScan.Utils;

import tmsdk.bg.module.network.INetworkInfoDao;

/**
 * Created by zouchl on 4/11/16.
 */

public class FlowManuallySetings extends AppCompatActivity {

    public static final String TAG = "FlowManuallySetings";

    public static final int TIME_PICKER_CODE = 1;
    private int mStatusBarHeight = Utils.STATUS_BAR_HEIGHT;

    private RelativeLayout mTimePick;
    private TextView mFreeTime;
    private Button mBtnSave;
    private EditText mMonthCommonTotal, mMonthCommonUsed, mFreeFlowTotal, mFreeFlowUsed;//, m4GTotal, m4GUsed;

    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor editor;
    public static int iHourStart, iHourEnd;//, iMinStart, iMinEnd;//TimePicker返回
    private static int iMonthTotal, iMonthUsed, iFreeTotal, iFreeUsed, i4GTotal, i4GUsed;
    private ImageButton mActionBarButton;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flow_manual_set);
        mSharedPref = getSharedPreferences(ShareUtil.DATABASE_NAME, Activity.MODE_PRIVATE);
        editor = mSharedPref.edit();

        //iniTranStatusBar();
        setStatusBarColor(true);
        initActionBar();

        /*ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setElevation(0);
        actionbar.setHomeAsUpIndicator(R.drawable.sec_actionbar_back);
        Resources r = getResources();
        Drawable myDrawable = r.getDrawable(R.drawable.titlegray);
        actionbar.setBackgroundDrawable(myDrawable);*/

        mTimePick = (RelativeLayout) findViewById(R.id.time_picker);
        mFreeTime = (TextView) findViewById(R.id.free_time);
        mBtnSave = (Button) findViewById(R.id.save);
        UnitConverter.updateButtonBg2(getApplicationContext(), mBtnSave);
        mMonthCommonTotal = (EditText) findViewById(R.id.month_total_input);
        mMonthCommonUsed = (EditText) findViewById(R.id.month_used__input);
        mFreeFlowTotal = (EditText) findViewById(R.id.free_total_input);
        mFreeFlowUsed = (EditText) findViewById(R.id.free_used_input);
        //m4GTotal = (EditText) findViewById(R.id.total_4G_input);
        //m4GUsed = (EditText) findViewById(R.id.used_4G_input);

        mBtnSave.setOnClickListener(mOnClickListener);

        mTimePick.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (Util.isFastClick()) {
                    return ;
                }

                //getEditTextData();
                iMonthTotal = getEditValue(mMonthCommonTotal);
                iMonthUsed = getEditValue(mMonthCommonUsed);
                iFreeTotal = getEditValue(mFreeFlowTotal);
                iFreeUsed = getEditValue(mFreeFlowUsed);

                Intent intent= new Intent(getApplicationContext(), TimePickerActivity.class);
                startActivityForResult(intent, TIME_PICKER_CODE);
            }
        });

        readSharePreData();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.e(TAG, "onResume");

        showFlowSetting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*private void iniTranStatusBar(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            int result;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId);
                final float scale = this.getResources().getDisplayMetrics().density;
                mStatusBarHeight = (int) (result / scale + 0.5f);
            }
        }
    }*/


    private void setStatusBarColor(boolean flag) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int mStatusColor = this.getResources().getColor(R.color.flow_monitor_bg_color);
            this.getWindow().setStatusBarColor(mStatusColor);
        }
    }

    /*private void initActionBar() {
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setBackgroundDrawable(this.getResources().getDrawable(R.color.flow_gray_color));
        mActionBar.setElevation(0);

        String str = getString(R.string.flow_manually_set);
        SpannableStringBuilder mSpanStrBuilder = new SpannableStringBuilder(str);
        mSpanStrBuilder.setSpan(new AbsoluteSizeSpan(17, true), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSpanStrBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mActionBar.setTitle(mSpanStrBuilder);
    }*/
    private void initActionBar() {
        mActionBarButton = (ImageButton) findViewById(R.id.actionbar_icon);
        mActionBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            mActionBarButton.setBackgroundResource(outValue.resourceId);
        }else{
            mActionBarButton.setBackgroundResource(R.drawable.ic_arrow_black_selectors);
        }
        findViewById(R.id.layout_actionbar).setBackgroundColor(getResources().getColor(R.color.flow_monitor_bg_color));
    }

    private void readSharePreData(){

        iMonthTotal = mSharedPref.getInt(ShareUtil.SIM1_COMMON_TOTAL_KBYTES, 0);
        iMonthUsed = mSharedPref.getInt(ShareUtil.SIM1_COMMON_USED_KBYTES, 0);
        iFreeTotal = mSharedPref.getInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, 0);
        iFreeUsed = mSharedPref.getInt(ShareUtil.SIM1_FREE_USED_KBYTES, 0);
        //i4GTotal = mSharedPref.getInt(ShareUtil.SIM1_4G_TOTAL_KBYTES, 0);
        //i4GUsed = mSharedPref.getInt(ShareUtil.SIM1_4G_USED_KBYTES, 0);
        Log.d(TAG, "readSharePreData iMonthTotal=" + iMonthTotal+",iMonthUsed="+iMonthUsed);
        Log.d(TAG, "iFreeTotal="+iFreeTotal+",iFreeUsed="+iFreeUsed);
        //Log.e(TAG, "i4Gtotal="+i4GTotal+",i4GUsed="+i4GUsed);

        iHourStart = mSharedPref.getInt(ShareUtil.START_TIME_HOUR, 23);
        iHourEnd = mSharedPref.getInt(ShareUtil.END_TIME_HOUR, 00);
        //iMinStart = mSharedPref.getInt(ShareUtil.START_TIME_MIN, 07);
        //iMinEnd = mSharedPref.getInt(ShareUtil.END_TIME_MIN, 00);
        Log.d(TAG, "showFlowSetting iHourStart="+iHourStart
                +",iHourEnd="+iHourEnd);
    }

    private void showFlowSetting(){

        mMonthCommonTotal.setText(String.valueOf(iMonthTotal/1024));
        mMonthCommonUsed.setText(String.valueOf(iMonthUsed/1024));
        mFreeFlowTotal.setText(String.valueOf(iFreeTotal/1024));
        mFreeFlowUsed.setText(String.valueOf(iFreeUsed/1024));
        //m4GTotal.setText(String.valueOf(i4GTotal/1024));
        //m4GUsed.setText(String.valueOf(i4GUsed/1024));

        String strHourStart = String.valueOf(iHourStart);
        String strHourEnd = String.valueOf(iHourEnd);
        //String strMinStart = String.valueOf(iMinStart);
        //String strMinEnd = String.valueOf(iMinEnd);

        if(iHourStart < 10){
            strHourStart = "0"+strHourStart + ":00";
        }else{
            strHourStart = strHourStart + ":00";
        }

        if(iHourEnd < 10){
            strHourEnd = "0"+strHourEnd + ":00";
        }else{
            strHourEnd = strHourEnd + ":00";
        }

        /*if(iMinStart < 10){
            strMinStart = "0"+strMinStart;
        }

        if(iMinEnd < 10){
            strMinEnd = "0"+strMinEnd;
        }*/

        mFreeTime.setText(strHourStart + "-" + strHourEnd);
    }

    private int getEditValue(EditText mText){

        String mStr;
        int mValue = 0;

        mStr = mText.getText().toString();

        Log.v(TAG, "str="+mStr);

        if(!(mStr.equals("0") || mStr.equals(""))){
            mValue = Integer.valueOf(mStr)*1024;
        }
        return mValue;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.save){

                int iCommTota = 0, iCommUsed = 0, iFreeTotal = 0, iFreeUsed = 0;

                iCommTota = getEditValue(mMonthCommonTotal);
                iCommUsed = getEditValue(mMonthCommonUsed);
                iFreeTotal = getEditValue(mFreeFlowTotal);
                iFreeUsed = getEditValue(mFreeFlowUsed);

                if(iCommUsed > iCommTota || iFreeUsed > iFreeTotal) {
                    Toast.makeText(FlowManuallySetings.this, R.string.manual_break_flow_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                editor.putInt(ShareUtil.SIM1_COMMON_TOTAL_KBYTES,iCommTota);
                editor.putInt(ShareUtil.SIM1_COMMON_USED_KBYTES, iCommUsed);
                editor.putInt(ShareUtil.SIM1_FREE_TOTAL_KBYTES, iFreeTotal);
                editor.putInt(ShareUtil.SIM1_FREE_USED_KBYTES, iFreeUsed);
                editor.putInt(ShareUtil.SIM1_COMMON_LEFT_KBYTES, iCommTota-iCommUsed);
                editor.putInt(ShareUtil.SIM1_FREE_LEFT_KBYTES, iFreeTotal-iFreeUsed);

                Log.v(TAG, "iCommTota="+iCommTota+",iCommUsed="+iCommUsed+",iFreeTotal="+iFreeTotal+",iFreeUsed="+iFreeUsed);

                //editor.putInt(ShareUtil.SIM1_4G_TOTAL_KBYTES, Integer.valueOf(m4GTotal.getText().toString())*1024);
                //editor.putInt(ShareUtil.SIM1_4G_USED_KBYTES, Integer.valueOf(m4GUsed.getText().toString())*1024);

                editor.putInt(ShareUtil.START_TIME_HOUR, iHourStart);
                editor.putInt(ShareUtil.END_TIME_HOUR, iHourEnd);
                //editor.putInt(ShareUtil.START_TIME_MIN, iMinStart);
                //editor.putInt(ShareUtil.END_TIME_MIN, iMinEnd);

                Log.v(TAG, "saveListener monthtotal=" + iCommTota + ",iMonthUsed=" + iCommUsed);
                Log.v(TAG, "strFreeTotal=" + iFreeTotal + ",iFreeUsed=" + iFreeUsed);
                //Log.v(TAG, "str4Gtotal=" + i4GTotal + ",i4GUsed=" + i4GUsed);

                editor.commit();
                //readSharePreData();

                sendBroadcast(new Intent(ShareUtil.ACTION_SET_USED_FOR_MONTH));

                finish();
            }/*else if(v.getId() == R.id.actionbar_icon){
                finish();
            }*/

        }
    };

    // iHourStart, iHourEnd, iMinStart, iMinEnd;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == TIME_PICKER_CODE){
            if(resultCode == RESULT_OK){
                iHourStart = data.getExtras().getInt("hourStart");
                iHourEnd = data.getExtras().getInt("hourEnd");
                //iMinStart = data.getExtras().getInt("minStart");
                //iMinEnd = data.getExtras().getInt("minEnd");
            }

        }
    }
}
