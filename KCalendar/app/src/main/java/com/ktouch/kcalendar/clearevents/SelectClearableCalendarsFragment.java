/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ktouch.kcalendar.clearevents;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
//TY hanjq 20140910 add for PROD103124493 begin
import android.widget.LinearLayout;
//TY hanjq 20140910 add for PROD103124493 end
import android.widget.ListView;
import android.widget.Toast;

import com.ktouch.kcalendar.AllInOneActivity;
import com.ktouch.kcalendar.AsyncQueryService;
import com.ktouch.kcalendar.R;
import com.ktouch.kcalendar.Utils;
import com.ktouch.kcalendar.recurrencepicker.LinearLayoutWithMaxWidth;
import com.ktouch.kcalendar.selectcalendars.SelectCalendarsSimpleAdapter;

import java.util.ArrayList;
import android.graphics.Color;
import android.os.Handler;

///M:#ClearAllEvents#
public class SelectClearableCalendarsFragment extends Fragment
        implements AdapterView.OnItemClickListener/*, CalendarController.EventHandler*/ {

    private static final String TAG = "Calendar";
    private static final String IS_PRIMARY = "\"primary\"";
    private static final String SELECTION = Calendars.SYNC_EVENTS + "=?";
    private static final String[] SELECTION_ARGS = new String[] {"1"};

    private static final String[] PROJECTION = new String[] {
        Calendars._ID,
        Calendars.CALENDAR_DISPLAY_NAME,
        Calendars.CALENDAR_COLOR,
        Calendars.VISIBLE,
        Calendars.OWNER_ACCOUNT,
        Calendars.ACCOUNT_NAME,
        Calendars.ACCOUNT_TYPE,
        Calendars.SYNC_EVENTS,
        "(" + Calendars.ACCOUNT_NAME + "=" + Calendars.OWNER_ACCOUNT + ") AS " + IS_PRIMARY,
    };
    private static int sDeleteToken;
    private static int sQueryToken;
    private static int mCalendarItemLayout = R.layout.mini_calendar_item;

    private View mView;
    private ListView mList;
    private SelectCalendarsSimpleAdapter mAdapter;
    private Activity mContext;
    private AsyncQueryService mService;
    private Cursor mCursor;
    private Toast mToast;
    private Button mBtnDelete; //yanghong modify on Jan.15,2015
    private AlertDialog mAlertDialog;
    private ArrayList<Long> mCalendarIds = new ArrayList<Long>();
    
    ///M:The flag that set the account check status diable.
    private static final int FLAG_ACCOUNT_CHECK_DISABLE = 0;

    public SelectClearableCalendarsFragment() {
    }

    public SelectClearableCalendarsFragment(int itemLayout) {
        mCalendarItemLayout = itemLayout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mService = new AsyncQueryService(activity) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                ///M:set the account check status disable.
                Cursor newCursor = disableAccountCheckStatus(cursor);
                ///@}
                mAdapter.changeCursor(newCursor);
                mCursor = newCursor;
                if (!mCalendarIds.isEmpty()) {
                    mCalendarIds.clear();
                    mBtnDelete.setEnabled(false);
                    /*wangqin fixed for white background TOS3.0*/
                    //mBtnDelete.setTextColor(Color.GRAY); //yanghong add on Jan.15,2015
                }
            }
            
            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                Log.i(TAG, "Clear all events,onDeleteComplete.  result(delete number)=" + result);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                    Log.i(TAG, "Cancel Progress dialog.");
                }

                if (mToast == null) {
                    mToast = Toast.makeText(mContext, R.string.delete_completed, Toast.LENGTH_SHORT);
                }
                mToast.show();
                /// M: need to set flag true to make sure "event_change" is sent to update UI about
                // events in month view @{
                AllInOneActivity.setClearEventsCompletedStatus(true);
                /// @}

                super.onDeleteComplete(token, cookie, result);
                //yanghong add to exit the current screent on Jan.22,2015
                new Handler().postDelayed(
                    new Runnable(){
                        public void run(){
                            mContext.finish();
                        }
                    }, 1000);
                //yanghong add end
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.select_calendars_to_clear_fragment, null);
        mList = (ListView)mView.findViewById(R.id.list);

        // Hide the Calendars to Sync button on tablets for now.
        // Long terms stick it in the list of calendars
        if (Utils.getConfigBool(getActivity(), R.bool.multiple_pane_config)) {
            // Don't show dividers on tablets
            mList.setDivider(null);
            View v = mView.findViewById(R.id.manage_sync_set);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }
        mBtnDelete = (Button) mView.findViewById(R.id.btn_ok);
        if (mBtnDelete != null) {
            mBtnDelete.setOnClickListener(mClickListener);
            mBtnDelete.setEnabled(false);
            /*wangqin fixed for white background TOS3.0*/
            //mBtnDelete.setTextColor(Color.GRAY); //yanghong add on Jan.15,2015
        }
        Button cancel = (Button) mView.findViewById(R.id.btn_cancel);
        if (cancel != null) {
            cancel.setOnClickListener(mClickListener);
        }
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new SelectCalendarsSimpleAdapter(mContext, mCalendarItemLayout, null,getFragmentManager());
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
    }
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = createProgressDialog();
        sQueryToken = mService.getNextToken();
        mService.startQuery(sQueryToken, null, Calendars.CONTENT_URI, PROJECTION, SELECTION,
                SELECTION_ARGS, Calendars.ACCOUNT_NAME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCalendarIds != null && !mCalendarIds.isEmpty()) {
            mCalendarIds.clear();
        }
        dismissAlertDialog();
        
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        if (mCursor != null) {
            mAdapter.changeCursor(null);
            mCursor.close();
            mCursor = null;
        }
    }

    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.btn_ok:
                Log.d(TAG, "Clear all events, ok");
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.delete_label)
                .setMessage(R.string.clear_all_selected_events_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setNegativeButton(android.R.string.cancel, null).create();
                
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        mContext.getText(R.string.delete_label),
                        mClearEventsDialogListener);
                dialog.show();
                mAlertDialog = dialog;
                break;
            case R.id.btn_cancel:
                Log.d(TAG, "Clear all events, cancel");
                mContext.finish();
                break;
            default:
                Log.e(TAG, "Unexpected view called: " + view);
                break;
            }
        }
    };

    private DialogInterface.OnClickListener mClearEventsDialogListener =
            new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            Log.d(TAG, "Clear all events, to delete.");
            dismissAlertDialog();
            sDeleteToken = mService.getNextToken();
            //mContext.finish();
            if (mProgressDialog != null) {
                mProgressDialog.show();
            }

            ///M: delete all events whose id > 0 && belong to selected Accounts
            String selection = Events._ID + ">0";
            selection = getSelection(selection);
            Log.i(TAG, "Clear all events, start delete, selection=" + selection);
            mService.startDelete(sDeleteToken, null, CalendarContract.Events.CONTENT_URI, selection, null, 0);
        }
    };
    
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)  {
        if (mAdapter == null || mAdapter.getCount() <= position) {
            return;
        }
        saveCalendarId(position);
    }

    public void saveCalendarId(int position) {
        Log.d(TAG, "Toggling calendar at " + position);
        long calId = mAdapter.getItemId(position);
        int selected = mAdapter.getVisible(position) ^ 1;
        
        mAdapter.setVisible(position, selected);
        if (selected != 0) {
            mCalendarIds.add(calId);
        } else {
            if (mCalendarIds.contains(calId)) {
                mCalendarIds.remove(calId);
            }
        }
        
        if (!mCalendarIds.isEmpty()) {
            mBtnDelete.setEnabled(true);
            /*wangqin fixed for white background TOS3.0*/
            //mBtnDelete.setTextColor(Color.WHITE); //yanghong add on Jan.15,2015
        } else {
            mBtnDelete.setEnabled(false);
            /*wangqin fixed for white background TOS3.0*/
            //mBtnDelete.setTextColor(Color.GRAY); //yanghong add on Jan.15,2015
        }
    }
    
    private String getSelection(String selection) {
        String tmpSelection = "";
        for (Long calId : mCalendarIds) {
            tmpSelection += " OR " + Events.CALENDAR_ID + "=" + String.valueOf(calId);
        }
        if (!TextUtils.isEmpty(tmpSelection)) {
            tmpSelection = tmpSelection.replaceFirst(" OR ", "");
            return selection + " AND (" + tmpSelection + ")";
        } else {
            return selection;
        }
    }
    
    private void dismissAlertDialog() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }
    
    ProgressDialog mProgressDialog = null ;
    private ProgressDialog createProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setMessage(getString(R.string.wait_deleting_tip));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    public boolean isProgressDialogShowing() {
        return mProgressDialog != null ? mProgressDialog.isShowing() : false;
    }
    
    /**
     * M:set the account check status disable
     * @param cursor 
     * @return 
     */
     public Cursor disableAccountCheckStatus(Cursor cursor) {
         MatrixCursor newCursor = new MatrixCursor(cursor.getColumnNames());
         int numColumns = cursor.getColumnCount();
         String data[] = new String[numColumns];
         while (cursor.moveToNext()) {
             for (int i = 0; i < numColumns; i++) {
                 data[i] = cursor.getString(i);
             }

             int index = cursor.getColumnIndex(Calendars.VISIBLE);
             data[index] = String.valueOf(FLAG_ACCOUNT_CHECK_DISABLE);
             newCursor.addRow(data);
         }
         cursor.close();
         return newCursor;
     }
}
