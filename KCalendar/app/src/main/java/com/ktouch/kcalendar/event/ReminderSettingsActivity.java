package com.ktouch.kcalendar.event;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;

import java.util.ArrayList;

public class ReminderSettingsActivity extends Activity {

    private ArrayList<Integer> mReminderMinuteValues;
    private ArrayList<String> mReminderMinuteLabels;

    private ListView mReminderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_settings);

        mReminderList = (ListView)findViewById(R.id.reminder_radio_list);

        Intent intent = getIntent();
        int defaultMinutes = intent.getIntExtra(EditEventNewFragment.EXTRA_REMINDER_DEFAULT, EditEventNewFragment.REMINDER_MINITUES_INDEX_DEFAULT);

        Resources res = getResources();

        mReminderMinuteValues = Utils.loadIntegerArray(res, R.array.reminder_minutes_values);
        mReminderMinuteLabels = Utils.loadStringArray(res, R.array.reminder_minutes_labels);

        final RadioListAdapter adapter = new RadioListAdapter(this, mReminderMinuteLabels, defaultMinutes);
        if(mReminderList!=null){
            mReminderList.setAdapter(adapter);
            mReminderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.setItemChecked(position);
                    int reminderValue = mReminderMinuteValues.get(position);
                    Intent intent = new Intent();
                    intent.putExtra(EditEventNewFragment.EXTRA_REMINDER_VALUE, reminderValue);
                    setResult(EditEventNewFragment.REQUEST_CODE_REMINDER_SETTINGS, intent);
                    ReminderSettingsActivity.this.finish();
                }
            });
        }

    }
}
