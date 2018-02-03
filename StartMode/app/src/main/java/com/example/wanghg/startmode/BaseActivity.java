package com.example.wanghg.startmode;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_base);

        Log.i("whg", "------onCreate------");
        Log.i("whg", "onCreate : " + getClass().getSimpleName()
                        + "    taskId : " + getTaskId()
                        + "    hasCode : " + hashCode());
        dumpTaskAffinity();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("whg", "-------onNewIntent--------");
        Log.i("whg", "onNewIntent : " + getClass().getSimpleName()
                            + "    taskId : " + getTaskId()
                            + "    hasCode : " + hashCode());
        dumpTaskAffinity();
    }

    protected void dumpTaskAffinity() {
        try {
            ActivityInfo info = this.getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
            Log.i("whg", "taskAffinity : " + info.taskAffinity);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
