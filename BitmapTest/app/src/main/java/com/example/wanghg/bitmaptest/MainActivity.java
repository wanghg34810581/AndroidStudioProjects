package com.example.wanghg.bitmaptest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button mButton;
    private TextView textView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext = MainActivity.this;
                Intent intent = new Intent(mContext, SubActivity.class);
                startActivity(intent);
            }
        });

        NdkJniUtil ndkJniUtil = new NdkJniUtil();
        textView = (TextView) findViewById(R.id.textView);
        textView.setText(ndkJniUtil.getCLanguageString()
                + "   "
                + ndkJniUtil.calAAddB(20, 55)
                + "   "
                + ndkJniUtil.getRandom(101));
    }
}
