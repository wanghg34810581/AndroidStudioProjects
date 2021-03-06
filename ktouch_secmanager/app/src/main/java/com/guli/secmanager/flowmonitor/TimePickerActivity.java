package com.guli.secmanager.flowmonitor;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.UnitConverter;

import java.lang.reflect.Field;


public class TimePickerActivity extends Activity {

    private static final String TAG = "TimePickerActivity";

    private int SHOW_TIME_PICKER_DIALOG = 0;
    private NumberPicker mNumPic1, mNumPic2, mNumPic3, mNumPic4;

    private LinearLayout mBtnOK, mBtnCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_picker);

        Log.e(TAG, "onCreate");

        mNumPic1=(NumberPicker)findViewById(R.id.NumPic1);
        mNumPic2=(NumberPicker)findViewById(R.id.NumPic2);
       /*mNumPic3=(NumberPicker)findViewById(R.id.NumPic3);
        mNumPic4=(NumberPicker)findViewById(R.id.NumPic4);*/

        NumberPicker.Formatter formatter = new NumberPicker.Formatter(){
            @Override
            public String format(int value){
                String tmpStr = String.valueOf(value);
                if(value < 10){
                    tmpStr = "0" + tmpStr + ":00";
                }else{
                    tmpStr = tmpStr + ":00";
                }
                return tmpStr;
            }
        };
        mNumPic1.setFormatter(formatter);
        mNumPic1.setMaxValue(23);
        mNumPic1.setMinValue(00);
        mNumPic1.setValue(FlowManuallySetings.iHourStart);


        mNumPic2.setFormatter(formatter);
        mNumPic2.setMaxValue(23);
        mNumPic2.setMinValue(00);
        mNumPic2.setValue(FlowManuallySetings.iHourEnd);


        /*
        mNumPic2.setFormatter(formatter);
        mNumPic3.setFormatter(formatter);
        mNumPic4.setFormatter(formatter);

        setNumberPickerDividerColor(mNumPic1);
        setNumberPickerDividerColor(mNumPic2);
        setNumberPickerDividerColor(mNumPic3);
        setNumberPickerDividerColor(mNumPic4);

        mNumPic1.setMaxValue(23);
        mNumPic1.setMinValue(00);
        mNumPic1.setValue(FlowManuallySetings.iHourStart);
        mNumPic1.setFocusable(true);
        mNumPic1.setFocusableInTouchMode(true);

        mNumPic2.setMaxValue(59);
        mNumPic2.setMinValue(00);
        mNumPic2.setValue(FlowManuallySetings.iHourEnd);
        mNumPic2.setFocusable(true);
        mNumPic2.setFocusableInTouchMode(true);

        mNumPic3.setMaxValue(23);
        mNumPic3.setMinValue(00);
        mNumPic3.setValue(FlowManuallySetings.iMinStart);
        mNumPic3.setFocusable(true);
        mNumPic3.setFocusableInTouchMode(true);

        mNumPic4.setMaxValue(59);
        mNumPic4.setMinValue(00);
        mNumPic4.setValue(FlowManuallySetings.iMinEnd);
        mNumPic4.setFocusable(true);
        mNumPic4.setFocusableInTouchMode(true);*/

        mBtnOK = (LinearLayout) findViewById(R.id.ok_panel);
        UnitConverter.updateButtonBg2(getApplicationContext(), mBtnOK);
        mBtnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra("hourStart", mNumPic1.getValue());
                intent.putExtra("hourEnd", mNumPic2.getValue());
                setResult(RESULT_OK, intent);
				
                finish();
                //Log.e("TimePickerActivity", "iSHour="+FlowManuallySetings.iHourStart+",iSMin="+FlowManuallySetings.iMinStart
                // +",iEHour="+FlowManuallySetings.iHourEnd+",iEMin="+FlowManuallySetings.iMinEnd);
            }
        });

        mBtnCancel = (LinearLayout) findViewById(R.id.cancel_panel);
        UnitConverter.updateButtonBg2(getApplicationContext(), mBtnCancel);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setNumberPickerDividerColor(NumberPicker numberPicker) {
        NumberPicker picker = numberPicker;
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值
                    pf.set(picker, new ColorDrawable(this.getResources().getColor(R.color.green)));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}