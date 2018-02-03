package com.ktouch.wheel;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;


public class CustomRecurrenceWheelMain {

    private static final String TAG = "RecurrenceWheelMain";
    private static final int DEFAULT_TYPE = 0;

    private Context mContext;
    private View mView;
    private WheelView mWVNum;

    private WheelView mWVSuffixes;

    private TextView mNumField;
    private TextView mSuffixesField;
    private int mMinValues = 2;
    private int mMaxValues;
    private long mMaxDays;
    private int mMaxMonths;
    private int mMaxYears;
    private String[] mSuffixesArray;
    private int mFreqValues;
    private int mFreqType;


    public View getView() {
        return mView;
    }

    public void setView(View view) {
        mView = view;
    }

    public void setNumField(TextView textView) {
        mNumField = textView;
    }

    public void setSuffixesField(TextView textView) {
        mSuffixesField = textView;
    }


    public int getFreqValues(){
        return mFreqValues;
    }

    public int getFreqType(){
        return mFreqType;
    }

    public CustomRecurrenceWheelMain(View view) {
        super();
        mView = view;
        setView(view);
    }

    private void refreshNumWheel(int type){
        NumericWheelAdapter adapter = null;
        mFreqType = type;
        switch (type){
            case 0:
                mMinValues = 2;
                mMaxValues = (int)mMaxDays;
                break;
            case 1:
                mMinValues = 3;
                mMaxValues = (int)(mMaxDays/7);
                break;
            case 2:
                mMinValues = 2;
                mMaxValues = mMaxMonths;
                break;
            case 3:
                mMinValues = 2;
                mMaxValues = mMaxYears;
                break;
        }
        adapter = new NumericWheelAdapter(mMinValues, mMaxValues);
        setNumField(mFreqValues);
        mWVNum.setAdapter(adapter);
        mWVNum.setCurrentItem(mFreqValues-mMinValues);
        mWVNum.setCyclic(false);
    }

    private void setNumField(int freqValues){
        String numString = mContext.getResources().getString(R.string.num_field_text, freqValues);
        mNumField.setText(numString);
    }

    public void initCustomRecurrencePicker(final Context context, int freqType, int freqInterval, long start, long end) {
        mContext = context;
        mSuffixesArray = mContext.getResources().getStringArray(R.array.custom_recurrence_suffixes_array);

        mWVNum = (WheelView) mView.findViewById(R.id.recurrence_num_wheel);
        mWVSuffixes = (WheelView) mView.findViewById(R.id.recurrence_suffixes_wheel);
        mWVSuffixes.setCyclic(false);

        long diffDays = (end - start)/Utils.DAY_IN_MILLIS;
        mMaxDays = Utils.calculateDays(end)-diffDays;
        mMaxMonths = Utils.calculateMonths(end);
        mMaxYears = Utils.calculateYears(end);

        mWVSuffixes.setAdapter(new ArrayWheelAdapter<String>(context.getResources().getStringArray(R.array.custom_recurrence_suffixes_array)));
        OnWheelChangedListener suffixesWheelListener = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                refreshNumWheel(newValue);
                mSuffixesField.setText(mSuffixesArray[wheel.getCurrentItem()]);
            }
        };
        mWVSuffixes.addChangingListener(suffixesWheelListener);

        if(freqType>=4) {
            mFreqType = freqType - 4;
        }

        if(freqInterval<=0){
            freqInterval = mMinValues;
        }

        refreshNumWheel(mFreqType);

        OnWheelChangedListener numWheelListener = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mFreqValues= newValue+mMinValues;
                setNumField(mFreqValues);
            }
        };
        mWVNum.addChangingListener(numWheelListener);

        mFreqValues = freqInterval;
        setNumField(mFreqValues);

        mSuffixesField.setText(mSuffixesArray[mFreqType]);
        mWVNum.setCurrentItem(freqInterval-mMinValues);
        mWVSuffixes.setCurrentItem(mFreqType);
    }

}
