package com.example.wanghg.bitmaptest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SubActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = SubActivity.this;
    }
}
