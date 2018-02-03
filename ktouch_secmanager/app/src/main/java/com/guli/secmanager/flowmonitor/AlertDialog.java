package com.guli.secmanager.flowmonitor;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.ShareUtil;

public class AlertDialog extends Activity {

    private Button mBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_dialog);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(onClickListener);
        Toast.makeText(getApplicationContext(), R.string.break_flow_toast, Toast.LENGTH_LONG);
        Toast.makeText(getApplicationContext(), "bbbbbbbbb", Toast.LENGTH_LONG);
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
