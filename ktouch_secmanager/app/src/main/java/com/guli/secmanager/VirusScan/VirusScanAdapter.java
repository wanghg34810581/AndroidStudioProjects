package com.guli.secmanager.VirusScan;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.guli.secmanager.R;
import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;
/**
 * Created by shenyan on 2016/4/18.
 */
public class VirusScanAdapter extends BaseExpandableListAdapter{

    public static final String TAG = "VirusScanAdapter";
    private ArrayList<ParentData> dataList;
    private Context context;
    private boolean intentFlag;
    private HashMap<Integer, ArrayList<Boolean>> mChildCheckStates;

    public VirusScanAdapter(ArrayList<ParentData> datas, Context context, boolean intentFlag) {
        super();
        this.dataList = datas;
        this.context = context;
        this.intentFlag = intentFlag;
        mChildCheckStates = new HashMap<Integer, ArrayList<Boolean>>();
    }

    public ArrayList<Boolean> getmChildChkBoxStates(int num) {
        Log.d(TAG,"listParentData.size()="+dataList.size()+"|num="+num);
        if(num<dataList.size()) {
            Log.d(TAG,"listParentData.size()="+dataList.size());
            return mChildCheckStates.get(num);
        }else{
            Log.d(TAG,"return NULL");
            return null;
        }
    }

    public void clearChildCheckStates(){
        mChildCheckStates.clear();
    }

    @Override
    public int getGroupCount() {
        return dataList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return dataList.get(groupPosition).getParChilds().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dataList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dataList.get(groupPosition).getParChilds().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ParentViewHolder pViewHolder;

        if (convertView == null) {
            //Log.v(TAG, "getGroupView() convertView == null");
            convertView = LayoutInflater.from(context).inflate(R.layout.virus_scan_parent_item, parent, false);
            pViewHolder = new ParentViewHolder();
            pViewHolder.pTextView = (TextView) convertView.findViewById(R.id.parent_item_text);
            pViewHolder.pImageView = (ImageView) convertView.findViewById(R.id.parent_item_image_view);
            pViewHolder.pResultView = (TextView) convertView.findViewById(R.id.parent_scan_num_text);
            pViewHolder.pDividerView  = (View) convertView.findViewById(R.id.parent_divider);
            convertView.setTag(pViewHolder);
        }

        pViewHolder = (ParentViewHolder)convertView.getTag();
        if(dataList.get(groupPosition).getChildSize() > 0){
            pViewHolder.pDividerView.setVisibility(View.GONE);
        }else{
            pViewHolder.pDividerView.setVisibility(View.VISIBLE);
        }

        pViewHolder.pTextView.setText(dataList.get(groupPosition).getParName());
        pViewHolder.pResultView.setText(dataList.get(groupPosition).getParResult());

        if( dataList.get(groupPosition).getParIcon() == Utils.CHECKED_ICON){
            pViewHolder.pImageView.setImageResource(R.drawable.home_icon_intercept);
        }else if(dataList.get(groupPosition).getParIcon() == Utils.WARING_ICON){
            pViewHolder.pImageView.setImageResource(R.drawable.home_icon_intercept_02);
        }

        if(intentFlag) {
            pViewHolder.pTextView.setTextColor(context.getResources().getColor(R.color.black));
        }
        //Log.v(TAG, "getGroupView() groupPosition=="+groupPosition);
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        ChildViewHolder cViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.virus_scan_child_item, parent, false);
            cViewHolder = new ChildViewHolder();
            cViewHolder.cTitleView = (TextView)convertView.findViewById(R.id.children_item_text);
            cViewHolder.cResultView = (TextView)convertView.findViewById(R.id.child_scan_result_text);
            cViewHolder.chkbox = (CheckBox) convertView.findViewById(R.id.chkbox);
            cViewHolder.cDividerView = (View) convertView.findViewById(R.id.child_divider);
            convertView.setTag(cViewHolder);
        }

        cViewHolder = (ChildViewHolder)convertView.getTag();
        cViewHolder.cTitleView.setText(dataList.get(groupPosition).getParChilds().get(childPosition).getChiName());
        cViewHolder.cResultView.setText(dataList.get(groupPosition).getParChilds().get(childPosition).getChiResult());
        if(childPosition == dataList.get(groupPosition).getParChilds().size() - 1){
            cViewHolder.cDividerView.setVisibility(View.VISIBLE);
        }else{
            cViewHolder.cDividerView.setVisibility(View.GONE);
        }

        //wangdsh Expandablelist Checkbox Bug Solution
        cViewHolder.chkbox.setOnCheckedChangeListener(null);
        if (mChildCheckStates.containsKey(groupPosition)) {
            ArrayList<Boolean> getChecked = mChildCheckStates.get(groupPosition);
            if(childPosition<=(getChecked.size()-1)){
                cViewHolder.chkbox.setChecked(getChecked.get(childPosition));
            }
        } else {
            ArrayList<Boolean> getChecked = new ArrayList<Boolean>();
            for (int i = 0; i < getChildrenCount(groupPosition); i++) {
                getChecked.add(true);
            }
            mChildCheckStates.put(groupPosition, getChecked);
            cViewHolder.chkbox.setChecked(true);
        }

        cViewHolder.chkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ArrayList<Boolean> getChecked = mChildCheckStates.get(groupPosition);
                getChecked.set(childPosition, isChecked);
                mChildCheckStates.put(groupPosition, getChecked);

                dataList.get(groupPosition).getParChilds().get(childPosition).setIschecked(
                        mChildCheckStates.get(groupPosition).get(childPosition));
                //Message msg = handler.obtainMessage();
                //msg.what = UIHandlerMsg.MSG_UPDATE_TEXT;
                //handler.sendMessage(msg);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class ParentViewHolder {
        TextView pTextView;
        ImageView pImageView;
        TextView pResultView;
        View pDividerView;
    }

    static class ChildViewHolder {
        TextView cTitleView;
        TextView cResultView;
        CheckBox chkbox;
        View cDividerView;
    }
}
