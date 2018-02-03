package com.ktouch.wheel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.ktouch.kcalendar.R;

@SuppressLint("SimpleDateFormat")
public class CustomRecurrencePickerShow {

    private Context context;
    private CustomRecurrenceWheelMain mCustomRecurrenceWheelMain;
    private long mSelectedDateAndTime;

    public CustomRecurrencePickerShow(Context context) {
        super();
        this.context = context;
    }

    public int getFreqType(){
        return mCustomRecurrenceWheelMain.getFreqType();
    }

    public int getFreqValues(){
        return mCustomRecurrenceWheelMain.getFreqValues();
    }

    public View customRecurrencePickerView(int freqType, int freqInterval, long start, long end, TextView numField, TextView suffixesField) {

        View customRecurrencePicker = View.inflate(context, R.layout.custom_recurrence_picker, null);
        mCustomRecurrenceWheelMain = new CustomRecurrenceWheelMain(customRecurrencePicker);
        mCustomRecurrenceWheelMain.setNumField(numField);
        mCustomRecurrenceWheelMain.setSuffixesField(suffixesField);
        mCustomRecurrenceWheelMain.initCustomRecurrencePicker(context, freqType, freqInterval, start, end);
        return customRecurrencePicker;
    }

}
