package com.ktouch.kcalendar.event;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktouch.calendarcommon.EventRecurrence;
import com.ktouch.kcalendar.OtherPreferences;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
import com.ktouch.wheel.CustomRecurrencePickerAlertDialog;
import com.ktouch.wheel.CustomRecurrencePickerShow;

import java.util.ArrayList;
import java.util.Arrays;

public class RecurrenceSettingsActivity extends Activity {

    // Special cases in monthlyByNthDayOfWeek
    private static final int FIFTH_WEEK_IN_A_MONTH = 5;
    private static final int LAST_NTH_DAY_OF_WEEK = -1;

    // Update android:maxLength in EditText as needed
    private static final int INTERVAL_DEFAULT = 1;
    // Update android:maxLength in EditText as needed
     private static final int COUNT_DEFAULT = 5;

    private static final int[] mFreqModelToEventRecurrence = {
            EventRecurrence.DAILY,
            EventRecurrence.WEEKLY,
            EventRecurrence.MONTHLY,
            EventRecurrence.YEARLY
    };

    private ArrayList<String> mRecurrenceLabels;
    private ArrayList<Integer> mRecurrenceTypes;
    private ArrayList<Integer> mRecurrenceCounts;

    private ListView mRecurrenceList;
    private RelativeLayout mCustomLayout;
    private RadioButton mCustomRadio;
    private TextView mCustomSelectedTextView;
    private CustomRecurrencePickerShow mCustomRecurrencePickerShow;

    private RecurrenceModel mModel = new RecurrenceModel();
    private EventRecurrence mRecurrence = new EventRecurrence();
    private String[] mCustomRecurrenceSuffixes;

    public class RecurrenceModel implements Parcelable {


        // Should match EventRecurrence.DAILY, etc
        static final int FREQ_DAILY = 0;
        static final int FREQ_WEEKLY = 1;
        static final int FREQ_MONTHLY = 2;
        static final int FREQ_YEARLY = 3;

        static final int END_NEVER = 0;
        static final int END_BY_DATE = 1;
        static final int END_BY_COUNT = 2;

        static final int MONTHLY_BY_DATE = 0;
        static final int MONTHLY_BY_NTH_DAY_OF_WEEK = 1;

        static final int STATE_NO_RECURRENCE = 0;
        static final int STATE_RECURRENCE = 1;

        int recurrenceState;

        /**
         * FREQ: Repeat pattern
         *
         * @see FREQ_DAILY
         * @see FREQ_WEEKLY
         * @see FREQ_MONTHLY
         * @see FREQ_YEARLY
         */
        int freq = FREQ_WEEKLY;

        /**
         * INTERVAL: Every n days/weeks/months/years. n >= 1
         */
        int interval = INTERVAL_DEFAULT;

        /**
         * UNTIL and COUNT: How does the the event end?
         *
         * @see END_NEVER
         * @see END_BY_DATE
         * @see END_BY_COUNT
         * @see untilDate
         * @see untilCount
         */
        int end;

        /**
         * UNTIL: Date of the last recurrence. Used when until == END_BY_DATE
         */
        Time endDate;

        /**
         * COUNT: Times to repeat. Use when until == END_BY_COUNT
         */
        int endCount = COUNT_DEFAULT;

        /**
         * BYDAY: Days of the week to be repeated. Sun = 0, Mon = 1, etc
         */
        boolean[] weeklyByDayOfWeek = new boolean[7];

        /**
         * BYDAY AND BYMONTHDAY: How to repeat monthly events? Same date of the
         * month or Same nth day of week.
         *
         * @see MONTHLY_BY_DATE
         * @see MONTHLY_BY_NTH_DAY_OF_WEEK
         */
        int monthlyRepeat;

        /**
         * Day of the month to repeat. Used when monthlyRepeat ==
         * MONTHLY_BY_DATE
         */
        int monthlyByMonthDay;

        /**
         * Day of the week to repeat. Used when monthlyRepeat ==
         * MONTHLY_BY_NTH_DAY_OF_WEEK
         */
        int monthlyByDayOfWeek;

        /**
         * Nth day of the week to repeat. Used when monthlyRepeat ==
         * MONTHLY_BY_NTH_DAY_OF_WEEK 0=undefined, -1=Last, 1=1st, 2=2nd, ..., 5=5th
         * <p/>
         * We support 5th, just to handle backwards capabilities with old bug, but it
         * gets converted to -1 once edited.
         */
        int monthlyByNthDayOfWeek;

        /*
         * (generated method)
         */
        @Override
        public String toString() {
            return "Model [freq=" + freq + ", interval=" + interval + ", end=" + end + ", endDate="
                    + endDate + ", endCount=" + endCount + ", weeklyByDayOfWeek="
                    + Arrays.toString(weeklyByDayOfWeek) + ", monthlyRepeat=" + monthlyRepeat
                    + ", monthlyByMonthDay=" + monthlyByMonthDay + ", monthlyByDayOfWeek="
                    + monthlyByDayOfWeek + ", monthlyByNthDayOfWeek=" + monthlyByNthDayOfWeek + "]";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public RecurrenceModel() {
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(freq);
            dest.writeInt(interval);
            dest.writeInt(end);
            dest.writeInt(endDate.year);
            dest.writeInt(endDate.month);
            dest.writeInt(endDate.monthDay);
            dest.writeInt(endCount);
            dest.writeBooleanArray(weeklyByDayOfWeek);
            dest.writeInt(monthlyRepeat);
            dest.writeInt(monthlyByMonthDay);
            dest.writeInt(monthlyByDayOfWeek);
            dest.writeInt(monthlyByNthDayOfWeek);
            dest.writeInt(recurrenceState);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recurrence_settings);

        mRecurrenceList = (ListView) findViewById(R.id.recurrence_radio_list);
        mCustomLayout = (RelativeLayout) findViewById(R.id.recurrence_custom_item_layout);
        mCustomRadio = (RadioButton) findViewById(R.id.custom_radio_btn);
        mCustomSelectedTextView = (TextView) findViewById(R.id.custom_selected_text) ;

        Intent intent = getIntent();

        int defaultSelection = intent.getIntExtra(EditEventNewFragment.EXTRA_RECURRENCE_DEFAULT, EditEventNewFragment.RECURRENCE_INDEX_DEFAULT);
        final int freqType = intent.getIntExtra(EditEventNewFragment.EXTRA_FREQ_TYPE, -1);
        final int freqInterval = intent.getIntExtra(EditEventNewFragment.EXTRA_FREQ_INTERVAL, 0);

        final long startTime = intent.getLongExtra(EditEventNewFragment.EXTRA_START_TIME, 0);
        final long endTime = intent.getLongExtra(EditEventNewFragment.EXTRA_END_TIME, 0);

        Resources res = getResources();
        mRecurrenceLabels = Utils.loadStringArray(res, R.array.recurrency_label_array);
        mRecurrenceTypes = Utils.loadIntegerArray(res, R.array.recurrency_freq_type);
        mRecurrenceCounts = Utils.loadIntegerArray(res, R.array.recurrency_freq_count);

        final RadioListAdapter adapter = new RadioListAdapter(this, mRecurrenceLabels, defaultSelection);
        if (defaultSelection == EditEventNewFragment.RECURRENCE_CUSTOM_INDEX) {
            mCustomRadio.setChecked(true);
        } else {
            mCustomRadio.setChecked(false);
        }

        if (mRecurrenceList != null) {
            mRecurrenceList.setAdapter(adapter);
            mRecurrenceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.setItemChecked(position);
                    mRecurrence.wkst = EventRecurrence.timeDay2Day(Utils.getFirstDayOfWeek(RecurrenceSettingsActivity.this));
                    String rrule = null;
                    mModel.recurrenceState = (mRecurrenceTypes.get(position) >= 0) ? RecurrenceModel.STATE_RECURRENCE
                            : RecurrenceModel.STATE_NO_RECURRENCE;
                    if (mModel.recurrenceState == RecurrenceModel.STATE_RECURRENCE) {
                        mModel.freq = mRecurrenceTypes.get(position);
                        mModel.interval = mRecurrenceCounts.get(position);
                        copyModelToEventRecurrence(mModel, mRecurrence);
                        rrule = mRecurrence.toString();
                    }
                    Intent intent = new Intent();
                    intent.putExtra(EditEventNewFragment.EXTRA_RECURRENCE_VALUE, rrule);
                    intent.putExtra(EditEventNewFragment.EXTRA_RECURRENCE_INDEX, position);
                    setResult(EditEventNewFragment.REQUEST_CODE_RECURRENCE_SETTINGS, intent);
                    RecurrenceSettingsActivity.this.finish();

                }
            });
        }

        if (mCustomLayout != null) {
            mCustomLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCustomRecurrencePicker(freqType, freqInterval, startTime, endTime);
                }
            });
        }

        mCustomRecurrenceSuffixes = getResources().getStringArray(R.array.custom_recurrence_suffixes_array);

        int index = freqType;
        if(freqType>=4){
            index = freqType-4;
        }
        refreshCustomText(mCustomRecurrenceSuffixes[index], freqInterval);

    }

    private void refreshCustomText(String type, int value){
        if(mCustomRadio.isChecked()){
            String text = getString(R.string.custom_selected_text, value, type);
            mCustomSelectedTextView.setText(text);
            mCustomSelectedTextView.setVisibility(View.VISIBLE);
        } else {
            mCustomSelectedTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void showCustomRecurrencePicker(int freqType, int freqInterval, long start, long end) {
        if (mCustomRecurrencePickerShow == null) {
            mCustomRecurrencePickerShow = new CustomRecurrencePickerShow(this);
        }

        final CustomRecurrencePickerAlertDialog dialog = new CustomRecurrencePickerAlertDialog(this);
        dialog.builder();
        dialog.setView(mCustomRecurrencePickerShow.customRecurrencePickerView(freqType, freqInterval, start, end, dialog.getNumField(), dialog.getSuffixesField()));
        dialog.setNegativeButton(getText(R.string.negative_button).toString(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        dialog.setPositiveButton(getText(R.string.positive_button).toString(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecurrence.wkst = EventRecurrence.timeDay2Day(Utils.getFirstDayOfWeek(RecurrenceSettingsActivity.this));
                String rrule = null;
                mModel.recurrenceState = (mCustomRecurrencePickerShow.getFreqType() >= 0) ? RecurrenceModel.STATE_RECURRENCE
                        : RecurrenceModel.STATE_NO_RECURRENCE;
                if (mModel.recurrenceState == RecurrenceModel.STATE_RECURRENCE) {
                    mModel.freq = mCustomRecurrencePickerShow.getFreqType();
                    mModel.interval = mCustomRecurrencePickerShow.getFreqValues();
                    copyModelToEventRecurrence(mModel, mRecurrence);
                    rrule = mRecurrence.toString();
                }
                refreshCustomText(mCustomRecurrenceSuffixes[mModel.freq], mModel.interval);
                Intent intent = new Intent();
                intent.putExtra(EditEventNewFragment.EXTRA_RECURRENCE_VALUE, rrule);
                intent.putExtra(EditEventNewFragment.EXTRA_RECURRENCE_INDEX, EditEventNewFragment.RECURRENCE_CUSTOM_INDEX);
                setResult(EditEventNewFragment.REQUEST_CODE_RECURRENCE_SETTINGS, intent);
                RecurrenceSettingsActivity.this.finish();
            }
        });
        dialog.show();
    }

    static public boolean canHandleRecurrenceRule(EventRecurrence er) {
        switch (er.freq) {
            case EventRecurrence.DAILY:
            case EventRecurrence.MONTHLY:
            case EventRecurrence.YEARLY:
            case EventRecurrence.WEEKLY:
                break;
            default:
                return false;
        }

        if (er.count > 0 && !TextUtils.isEmpty(er.until)) {
            return false;
        }

        // Weekly: For "repeat by day of week", the day of week to repeat is in
        // er.byday[]

        /*
         * Monthly: For "repeat by nth day of week" the day of week to repeat is
         * in er.byday[] and the "nth" is stored in er.bydayNum[]. Currently we
         * can handle only one and only in monthly
         */
        int numOfByDayNum = 0;
        for (int i = 0; i < er.bydayCount; i++) {
            if (isSupportedMonthlyByNthDayOfWeek(er.bydayNum[i])) {
                ++numOfByDayNum;
            }
        }

        if (numOfByDayNum > 1) {
            return false;
        }

        if (numOfByDayNum > 0 && er.freq != EventRecurrence.MONTHLY) {
            return false;
        }

        // The UI only handle repeat by one day of month i.e. not 9th and 10th
        // of every month
        if (er.bymonthdayCount > 1) {
            return false;
        }

        if (er.freq == EventRecurrence.MONTHLY) {
            if (er.bydayCount > 1) {
                return false;
            }
            if (er.bydayCount > 0 && er.bymonthdayCount > 0) {
                return false;
            }
        }

        return true;
    }

    static public boolean isSupportedMonthlyByNthDayOfWeek(int num) {
        // We only support monthlyByNthDayOfWeek when it is greater then 0 but less then 5.
        // Or if -1 when it is the last monthly day of the week.
        return (num > 0 && num <= FIFTH_WEEK_IN_A_MONTH) || num == LAST_NTH_DAY_OF_WEEK;
    }

    static private void copyModelToEventRecurrence(final RecurrenceModel model,
                                                   EventRecurrence er) {
        if (model.recurrenceState == RecurrenceModel.STATE_NO_RECURRENCE) {
            throw new IllegalStateException("There's no recurrence");
        }

        // Freq
        er.freq = mFreqModelToEventRecurrence[model.freq];

        // Interval
        if (model.interval <= 1) {
            er.interval = 0;
        } else {
            er.interval = model.interval;
        }

        // End
        switch (model.end) {
            case RecurrenceModel.END_BY_DATE:
                if (model.endDate != null) {
                    model.endDate.switchTimezone(Time.TIMEZONE_UTC);
                    model.endDate.normalize(false);
                    er.until = model.endDate.format2445();
                    er.count = 0;
                } else {
                    throw new IllegalStateException("end = END_BY_DATE but endDate is null");
                }
                break;
            case RecurrenceModel.END_BY_COUNT:
                er.count = model.endCount;
                er.until = null;
                if (er.count <= 0) {
                    throw new IllegalStateException("count is " + er.count);
                }
                break;
            default:
                er.count = 0;
                er.until = null;
                break;
        }

        // Weekly && monthly repeat patterns
        er.bydayCount = 0;
        er.bymonthdayCount = 0;

        switch (model.freq) {
            case RecurrenceModel.FREQ_MONTHLY:
                if (model.monthlyRepeat == RecurrenceModel.MONTHLY_BY_DATE) {
                    if (model.monthlyByMonthDay > 0) {
                        if (er.bymonthday == null || er.bymonthdayCount < 1) {
                            er.bymonthday = new int[1];
                        }
                        er.bymonthday[0] = model.monthlyByMonthDay;
                        er.bymonthdayCount = 1;
                    }
                } else if (model.monthlyRepeat == RecurrenceModel.MONTHLY_BY_NTH_DAY_OF_WEEK) {
                    if (!isSupportedMonthlyByNthDayOfWeek(model.monthlyByNthDayOfWeek)) {
                        throw new IllegalStateException("month repeat by nth week but n is "
                                + model.monthlyByNthDayOfWeek);
                    }
                    int count = 1;
                    if (er.bydayCount < count || er.byday == null || er.bydayNum == null) {
                        er.byday = new int[count];
                        er.bydayNum = new int[count];
                    }
                    er.bydayCount = count;
                    er.byday[0] = EventRecurrence.timeDay2Day(model.monthlyByDayOfWeek);
                    er.bydayNum[0] = model.monthlyByNthDayOfWeek;
                }
                break;
            case RecurrenceModel.FREQ_WEEKLY:
                int count = 0;
                for (int i = 0; i < 7; i++) {
                    if (model.weeklyByDayOfWeek[i]) {
                        count++;
                    }
                }

                if (er.bydayCount < count || er.byday == null || er.bydayNum == null) {
                    er.byday = new int[count];
                    er.bydayNum = new int[count];
                }
                er.bydayCount = count;

                for (int i = 6; i >= 0; i--) {
                    if (model.weeklyByDayOfWeek[i]) {
                        er.bydayNum[--count] = 0;
                        er.byday[count] = EventRecurrence.timeDay2Day(i);
                    }
                }
                break;
        }

        if (!canHandleRecurrenceRule(er)) {
            throw new IllegalStateException("UI generated recurrence that it can't handle. ER:"
                    + er.toString() + " Model: " + model.toString());
        }
    }
}
