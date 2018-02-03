package com.ktouch.kcalendar.agenda;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;


import com.ktouch.kcalendar.AbstractCalendarActivity;
import com.ktouch.kcalendar.CalendarController;
import com.ktouch.kcalendar.R;

public class AgendaListActivity extends AbstractCalendarActivity implements CalendarController.EventHandler {

    public static final String EXTRA_TIME_MILLIS = "extra_time_millis";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agenda_list_activity);

        Intent intent = getIntent();
        long timeMillis = intent.getLongExtra(EXTRA_TIME_MILLIS, 0);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        AgendaFragment frag = new AgendaFragment(timeMillis, false, AgendaListView.WINDOW_TYPE_YEAR);

        ft.replace(R.id.agenda_list_pane, frag).commit();
    }

        @Override
        public long getSupportedEventTypes() {
            return CalendarController.EventType.GO_TO | CalendarController.EventType.VIEW_EVENT | CalendarController.EventType.UPDATE_TITLE;
        }

        @Override
        public void handleEvent(CalendarController.EventInfo event) {
        }

        @Override
        public void eventsChanged() {
        }
}
