package com.guli.secmanager.flowmonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.guli.secmanager.R;

import java.util.ArrayList;
import java.util.HashMap;

import tmsdk.bg.module.network.CodeName;

/**
 * Created by wanqqch on 16-4-14.
 */
public class BrandSelectActivity extends Activity{

    private ListView listView;
    private ArrayList<HashMap<String,String>> list = null;
    private HashMap<String ,String> map = null;
    private ArrayList<CodeName> brandList;
    public final static int BRAND_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setContentView(R.layout.activity_brand_select);
        } else {
            setContentView(R.layout.activity_brand_selector);
        }
        Bundle bundle = getIntent().getExtras();
        String operator = bundle.getString("operatorId");

        listView = (ListView)findViewById(R.id.brand_list);
        list = new ArrayList<HashMap<String, String>>();
        brandList = OperatorActivity.mTcMgr.getBrands(operator);
        String[] brandItem = new String[brandList.size()];
        for(int i=0;i<brandItem.length;i++){
            map = new HashMap<String,String>();
            map.put("brandName",brandList.get(i).mName);
            list.add(map);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this,list,R.layout.brand_select_item,
                new String[]{"brandName"},new int[]{R.id.brand_names});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(itemClick);
    }

    private AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String brandName = brandList.get(position).mName;
            String brandId = brandList.get(position).mCode;
            Intent intent = new Intent();
            intent.putExtra("brandNames",brandName);
            intent.putExtra("brandId",brandId);
            setResult(BRAND_CODE, intent);
            finish();
        }
    };
}
