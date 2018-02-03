package com.guli.secmanager.flowmonitor;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.guli.secmanager.R;
import com.guli.secmanager.VirusScan.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import tmsdk.bg.module.network.CodeName;

/**
 * Created by wangqch on 16-4-13.
 */
public class ProvinceSelectActivity extends AppCompatActivity {

    private ListView listView;
    private ImageButton actionButton;
    private ArrayList<HashMap<String,String>> list = null;
    private ArrayList<CodeName> mProvinces;
    private HashMap<String ,String> map = null;
    public final static int PROVINCE_CODE=1;
    private android.support.v7.app.ActionBar mActionBar;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStatusBarColor(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setContentView(R.layout.activity_province_select);
        } else {
            setContentView(R.layout.activity_province_selectors);
        }
        initActionBar();
        listView = (ListView)findViewById(R.id.province_list);
        list = new ArrayList<HashMap<String, String>>();
        mProvinces = OperatorActivity.mTcMgr.getAllProvinces();
        String[] provinceItem = new String[mProvinces.size()];
        for(int i=0;i<provinceItem.length;i++){
            map = new HashMap<String,String>();
            map.put("provinceName",mProvinces.get(i).mName);
            list.add(map);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this,list,R.layout.province_select_item,
                new String[]{"provinceName"},new int[]{R.id.province_names});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(itemClick);
    }


    private AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String provinceId = mProvinces.get(position).mCode;
            String provinceName = mProvinces.get(position).mName;
            Intent intent = new Intent();
            intent.putExtra("provinceId",provinceId);
            intent.putExtra("provinceName",provinceName);
            setResult(PROVINCE_CODE, intent);
            finish();
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

}
