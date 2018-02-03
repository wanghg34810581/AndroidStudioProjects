package com.guli.secmanager.flowmonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.guli.secmanager.R;

import java.util.ArrayList;
import java.util.HashMap;

import tmsdk.bg.module.network.CodeName;

/**
 * Created by wangqch on 16-4-13.
 */
public class OperatorSelectActivity extends Activity {
    public final static int OPERATOR_CODE = 1;
    private ArrayList<CodeName> operatorList;
    private ArrayList<HashMap<String,String>> list = null;
    private HashMap<String ,String> map = null;

    @Override
    protected void onCreate(Bundle onSavedInstanceState){
        super.onCreate(onSavedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setContentView(R.layout.activity_operator_select);
        } else {
            setContentView(R.layout.activity_operator_selector);
        }

        ListView listView = (ListView)findViewById(R.id.operator_select_list);
        list = new ArrayList<HashMap<String, String>>();
        operatorList = OperatorActivity.mTcMgr.getCarries();
        String[] operatorItem = new String[operatorList.size()];
        for(int i=0;i<operatorItem.length;i++){
            map = new HashMap<String,String>();
            map.put("operatorName",operatorList.get(i).mName);
            list.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,list,R.layout.operator_select_item,
                new String[]{"operatorName"},new int[]{R.id.operator_name});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(itemClick);

    }

    private AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String operatorName = operatorList.get(position).mName;
            String operatorId = operatorList.get(position).mCode;
            Intent intent = new Intent();
            intent.putExtra("operatorName",operatorName);
            intent.putExtra("operatorId",operatorId);
            setResult(OPERATOR_CODE, intent);
            finish();

        }
    };


}
