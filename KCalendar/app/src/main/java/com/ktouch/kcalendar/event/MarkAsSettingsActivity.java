package com.ktouch.kcalendar.event;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;

import java.util.ArrayList;

public class MarkAsSettingsActivity extends Activity {

    private int[] mMarkAsIcons;
    private ArrayList<String> mMarkAsTexts;

    private GridView mMarkAsGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mark_as_settings);

        mMarkAsGrid = (GridView) findViewById(R.id.mark_as_grid);

        Intent intent = getIntent();
        int defaultValue = intent.getIntExtra(EditEventNewFragment.EXTRA_MARK_AS_DEFAULT, EditEventNewFragment.MARK_AS_INDEX_DEFAULT);

        Resources res = getResources();

        mMarkAsIcons = Utils.loadDrawableArray(res, R.array.mark_as_icons);
        mMarkAsTexts = Utils.loadStringArray(res, R.array.mark_as_texts);

        final MarkGridAdapter adapter = new MarkGridAdapter(this, mMarkAsIcons, mMarkAsTexts,  defaultValue);
        if(mMarkAsGrid!=null){
            mMarkAsGrid.setAdapter(adapter);
            mMarkAsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.setItemChecked(position);
                    Intent intent = new Intent();
                    intent.putExtra(EditEventNewFragment.EXTRA_MARK_AS_INDEX, position);
                    setResult(EditEventNewFragment.REQUEST_CODE_MARK_AS_SETTINGS, intent);
                    MarkAsSettingsActivity.this.finish();
                }
            });
        }

    }
}