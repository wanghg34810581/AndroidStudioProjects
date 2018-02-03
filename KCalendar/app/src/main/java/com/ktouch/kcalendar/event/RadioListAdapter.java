package com.ktouch.kcalendar.event;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktouch.kcalendar.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;


public class RadioListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> mItemList;
    HashMap<String, Boolean> mStates = new HashMap<String, Boolean>();

    private int mSelection;

    public RadioListAdapter(Context context, ArrayList<String> itemList, int defaultPosition) {
        mContext = context;
        mItemList = itemList;
        mSelection = defaultPosition;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void setItemChecked(int index) {
        mSelection = index;
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.radio_list_item, null);
            holder = new ViewHolder();
            holder.mItemText = (TextView) convertView.findViewById(R.id.radio_item_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final RadioButton radio = (RadioButton) convertView.findViewById(R.id.radio_btn);

        for (String key : mStates.keySet()) {
            mStates.put(key, false);

        }
        mStates.put(String.valueOf(mSelection), true);
        RadioListAdapter.this.notifyDataSetChanged();

        holder.mRadioButton = radio;
        holder.mItemRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.item_relative_layout);

        holder.mRadioButton.setFocusable(false);
        holder.mItemText.setFocusable(false);
        holder.mRadioButton.setClickable(false);
        holder.mItemText.setClickable(false);

        holder.mItemText.setText(mItemList.get(position));

        boolean res = false;
        if (mStates.get(String.valueOf(position)) == null || mStates.get(String.valueOf(position)) == false) {
            res = false;
            mStates.put(String.valueOf(position), false);
        } else
            res = true;

        holder.mRadioButton.setChecked(res);
        return convertView;
    }

    static class ViewHolder {
        RelativeLayout mItemRelativeLayout;
        TextView mItemText;
        RadioButton mRadioButton;
    }
}