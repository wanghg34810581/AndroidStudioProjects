package com.guli.secmanager.flowmonitor;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.VirusScan.Utils;

import java.util.ArrayList;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.network.CodeName;
import tmsdk.bg.module.network.TrafficCorrectionManager;

/**
 * Created by wangqch on 16-4-12.
 */
public class OperatorActivity extends AppCompatActivity {
    private final static int REQUEST_CODE = 1;
    private final static int PROVINCE_REQUEAT_CODE = 2;
    private final static int CITY_REQUEST_CODE = 3;
    private final static int BRAND_REQUEST_CODE = 4;
    private final int MAX_NUM = 31;
    private TextView operatorName;
    private TextView provinceName;
    private TextView cityName;
    private TextView brandName;
    private EditText editText;
    private Button saveButton;
    private ImageButton actionButton;
    private RelativeLayout cityLayout;
    private RelativeLayout provinceLayout;
    private RelativeLayout operatorInfoLayout;
    private RelativeLayout brandLayout;
    private RelativeLayout accountLayout;
    private ActionBar mActionBar;
    public static TrafficCorrectionManager mTcMgr;
    private ArrayList<CodeName> cityList;
    private String provinceId;
    private String cityId;
    private String operatorId;
    private String brandId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(true);
        setContentView(R.layout.activity_operator);
        initActionBar();
        operatorName = (TextView)findViewById(R.id.operator_info_name);
        provinceName = (TextView)findViewById(R.id.province_name);
        cityName = (TextView)findViewById(R.id.city_name);
        brandName = (TextView)findViewById(R.id.brand_name);
        editText = (EditText)findViewById(R.id.account_date);
        editText.addTextChangedListener(textWatcher);
        saveButton = (Button)findViewById(R.id.save);
        provinceLayout = (RelativeLayout) findViewById(R.id.province_layout);
        provinceLayout.setOnClickListener(provinceClick);
        operatorInfoLayout = (RelativeLayout)findViewById(R.id.operator_info_layout);
        operatorInfoLayout.setOnClickListener(operatorClick);
        cityLayout = (RelativeLayout)findViewById(R.id.city_layout);
        cityLayout.setOnClickListener(cityClick);
        brandLayout = (RelativeLayout)findViewById(R.id.brand_layout);
        brandLayout.setOnClickListener(brandClick);
        accountLayout = (RelativeLayout)findViewById(R.id.account_layout);
        saveButton.setOnClickListener(saveClick);
        setClickResource();

        mTcMgr = ManagerCreatorB.getManager(TrafficCorrectionManager.class);
        setDatas();

    }


    public void setDatas(){
        SharedPreferences sharedPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
        provinceId = sharedPreferences.getString(ShareUtil.PROVINCE, "");
        cityId = sharedPreferences.getString(ShareUtil.CITY,"");
        operatorId = sharedPreferences.getString(ShareUtil.OPERATOR,"");
        brandId = sharedPreferences.getString(ShareUtil.BRAND,"");
        String closingDate = sharedPreferences.getString(ShareUtil.ACCOUNT_DATE,"1");
        editText.setText(closingDate);
        if(provinceId.equals(""))
            return;
        ArrayList<CodeName> mProvinces = mTcMgr.getAllProvinces();
        for(int i=0;i<mProvinces.size();i++){
            if((mProvinces.get(i).mCode).equals(provinceId)){
                provinceName.setText(mProvinces.get(i).mName);
                ArrayList<CodeName> mCitys = mTcMgr.getCities(provinceId);
                if(mCitys.size() == 1){
                    cityLayout.setClickable(false);
                }
                for(int j=0;j<mCitys.size();j++){
                    if((mCitys.get(j).mCode).equals(cityId)){
                        cityName.setText(mCitys.get(j).mName);
                    }
                }
            }
        }

        ArrayList<CodeName> mOperators = mTcMgr.getCarries();
        for(int i=0;i<mOperators.size();i++){
            if((mOperators.get(i).mCode).equals(operatorId)){
                operatorName.setText(mOperators.get(i).mName);
                ArrayList<CodeName> mBrands = mTcMgr.getBrands(operatorId);
                for(int j=0;j<mBrands.size();j++){
                    if((mBrands.get(j).mCode).equals(brandId)){
                        brandName.setText(mBrands.get(j).mName);
                    }
                }
            }
        }
    }

    private View.OnClickListener provinceClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<CodeName> mProvinces = mTcMgr.getAllProvinces();
            Intent intent = new Intent();
            intent.setClass(OperatorActivity.this,ProvinceSelectActivity.class);
            startActivityForResult(intent, PROVINCE_REQUEAT_CODE);

        }
    };

    private View.OnClickListener operatorClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Intent intent = new Intent();
            intent.setClass(OperatorActivity.this,OperatorSelectActivity.class);
            startActivityForResult(intent,REQUEST_CODE);
        }

    };

    private View.OnClickListener cityClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("wangqch----","provinceId="+provinceId);
            if(provinceId == null || provinceId.equals("") || provinceId.equals("0")){
                Toast.makeText(OperatorActivity.this,R.string.input_province,Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("provinceId",provinceId);
            intent.putExtras(bundle);
            intent.setClass(OperatorActivity.this, CitySelectActivity.class);
            startActivityForResult(intent, CITY_REQUEST_CODE);

        }
    };

    private View.OnClickListener brandClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if("".equals(operatorId)){
                Toast.makeText(OperatorActivity.this,getResources().getString(R.string.select_operator),Toast.LENGTH_LONG).show();
            }else{
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("operatorId",operatorId);
                intent.putExtras(bundle);
                intent.setClass(OperatorActivity.this, BrandSelectActivity.class);
                startActivityForResult(intent, BRAND_REQUEST_CODE);
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_CODE){
            if(resultCode == OperatorSelectActivity.OPERATOR_CODE){
                operatorName.setText(data.getExtras().getString("operatorName"));
                operatorId = data.getExtras().getString("operatorId");
            }

        } else if(requestCode == PROVINCE_REQUEAT_CODE){
            if(resultCode == ProvinceSelectActivity.PROVINCE_CODE){
                provinceName.setText(data.getExtras().getString("provinceName"));
                provinceId = data.getExtras().getString("provinceId");
                cityList = mTcMgr.getCities(provinceId);
                if(cityList.size() == 1){
                    cityName.setText(cityList.get(0).mName);
                    cityId = cityList.get(0).mCode;
                    cityLayout.setClickable(false);

                }else{
                    cityLayout.setClickable(true);
                    cityName.setText("");
                }
            }

        } else if(requestCode == CITY_REQUEST_CODE){
            if(resultCode == CitySelectActivity.CITY_CODE){
                cityName.setText(data.getExtras().getString("cityNames"));
                cityId = data.getExtras().getString("cityId");
            }
        } else if(requestCode == BRAND_REQUEST_CODE){
            if(resultCode == BrandSelectActivity.BRAND_CODE){
                brandName.setText(data.getExtras().getString("brandNames"));
                brandId = data.getExtras().getString("brandId");
            }
        }
    }

    private View.OnClickListener saveClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("wangqchoperatoractivity","province="+provinceId+" city="+cityId+" operator="+operatorId+" brand="+brandId);
            if(provinceId == null || cityId == null || operatorId == null || brandId == null || editText.getText().toString().equals("")){
                AlertDialog.Builder builder = new AlertDialog.Builder(OperatorActivity.this);
                builder.setMessage(getResources().getString(R.string.improve_operator));
                builder.setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return;
            }
            SharedPreferences sharedPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(ShareUtil.PROVINCE,provinceId);
            editor.putString(ShareUtil.CITY,cityId);
            editor.putString(ShareUtil.OPERATOR,operatorId);
            editor.putString(ShareUtil.BRAND,brandId);
            editor.putString(ShareUtil.ACCOUNT_DATE,editText.getText().toString());
            editor.commit();
            finish();
        }
    };


    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(start > 1){
                int number = Integer.parseInt(s.toString());
                if(number > MAX_NUM){
                    s = String.valueOf(MAX_NUM);
                    editText.setText(s);
                }
                return;
            }

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s != null && !s.equals("")){
                int number = 0;
                try{
                    number = Integer.parseInt(s.toString());
                }catch(NumberFormatException e){
                    number = 0;
                }

                if(number > MAX_NUM){
                    editText.setText(String.valueOf(MAX_NUM));
                }
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
                finish();
                ;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            actionButton.setBackgroundResource(outValue.resourceId);
        }else{
            actionButton.setBackgroundResource(R.drawable.ic_arrow_black_selectors);
        }
        findViewById(R.id.layout_actionbar).setBackgroundColor(getResources().getColor(R.color.flow_monitor_bg_color));
    }

    private void setClickResource(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            provinceLayout.setBackgroundResource(outValue.resourceId);
            cityLayout.setBackgroundResource(outValue.resourceId);
            operatorInfoLayout.setBackgroundResource(outValue.resourceId);
            brandLayout.setBackgroundResource(outValue.resourceId);
            saveButton.setBackgroundResource(outValue.resourceId);
        } else {
            //provinceLayout.setBackgroundResource(R.drawable.complete_btn_on_selector);
            //cityLayout.setBackgroundResource(R.drawable.complete_btn_on_selector);
            //operatorInfoLayout.setBackgroundResource(R.drawable.complete_btn_on_selector);
            //brandLayout.setBackgroundResource(R.drawable.complete_btn_on_selector);
            saveButton.setBackgroundResource(R.drawable.complete_btn_on_selector);
        }
    }

}
