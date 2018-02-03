package com.ktouch.kcalendar.event;

import android.content.Context;
import android.content.res.Resources;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktouch.kcalendar.R;

import java.util.ArrayList;
import java.util.HashMap;

public class MarkGridAdapter extends BaseAdapter {

    private Context mContext;
    private int[] mIcons;
    private ArrayList<String> mTexts;
    private int mWindowWidth;
    HashMap<String, Boolean> mStates = new HashMap<String, Boolean>();

    private int mSelection;

    public MarkGridAdapter(Context context, int[] icons, ArrayList<String> texts, int defaultPosition) {
        mContext = context;
        mIcons = icons;
        mTexts = texts;
        mSelection = defaultPosition;
        Resources res = context.getResources();
        mWindowWidth = res.getDisplayMetrics().widthPixels;
    }

    @Override
    public int getCount() {
        return mTexts.size();
    }

    @Override
    public Object getItem(int position) {
        return mTexts.get(position);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_view_item, null);
            holder = new ViewHolder();
            holder.mItemLinearLayout = (LinearLayout) convertView.findViewById(R.id.grid_view_item_layout);
            holder.mItemIcon = (ImageView) convertView.findViewById(R.id.grid_view_icon);
            holder.mItemText = (TextView) convertView.findViewById(R.id.grid_view_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(mSelection==position) {
            holder.mItemLinearLayout.setBackgroundResource(R.drawable.grid_view_selector);
        } else {
            holder.mItemLinearLayout.setBackgroundResource(R.drawable.grid_selector);
        }

        holder.mItemText.setFocusable(false);
        holder.mItemText.setClickable(false);
        holder.mItemIcon.setFocusable(false);
        holder.mItemIcon.setClickable(false);

        if (mIcons[position] == 0) {
            holder.mItemText.getLayoutParams().height = mWindowWidth / 3 - (int) (2 * mContext.getResources().getDimension(R.dimen.normal_gap) - 2);
        } else {
            holder.mItemIcon.setBackgroundResource(mIcons[position]);
        }

        holder.mItemText.setText(mTexts.get(position));
        return convertView;
    }

    static class ViewHolder {
        LinearLayout mItemLinearLayout;
        ImageView mItemIcon;
        TextView mItemText;
    }
}
