package com.guli.secmanager.GarbageClean;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.FileSizeFormatter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wangdsh on 16-4-7.
 */

public class GarbageCLeanExpandableListViewAdapter extends BaseExpandableListAdapter {
    private String TAG = "GarbageCLeanExpandableListViewAdapter";

    private Context context;
    private ArrayList<ParentObj> listParentData;
    private LayoutInflater mLayoutInflater;
    private boolean mParentDateChildTitleVisible = false;

    private final int CHILD_VIEW_TYPE = 2;
    private final int TYPE_1 = 0x01;
    private final int TYPE_2 = 0x00;

    private HashMap<Integer, ArrayList<Boolean>> mChildCheckStates;

    public GarbageCLeanExpandableListViewAdapter(Context context) {
        this.context = context.getApplicationContext();
        mLayoutInflater = LayoutInflater.from(context);
        mChildCheckStates = new HashMap<Integer, ArrayList<Boolean>>();
    }
    public GarbageCLeanExpandableListViewAdapter(Context context, ArrayList<ParentObj> listParentData){
        this(context);
        this.listParentData = listParentData;
    }

    public ArrayList<Boolean> getmChildChkBoxStates(int num) {
        Log.d(TAG,"listParentData.size()="+listParentData.size()+"|num="+num);
        if(num<listParentData.size()) {
            Log.d(TAG,"listParentData.size()="+listParentData.size());
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
        return listParentData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listParentData.get(groupPosition).getChilds().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listParentData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listParentData.get(groupPosition).getChilds().get(childPosition);
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
        return true;
    }

    public void setParentDataChildTitleVisible(boolean bVisible){
        mParentDateChildTitleVisible = bVisible;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        if(childPosition == 0){
            return TYPE_2;
        }else{
            return TYPE_1;
        }
    }

    @Override
    public int getChildTypeCount() {
        return CHILD_VIEW_TYPE;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.garbage_cleanup_expendlist_parent, null);
            groupHolder = new GroupHolder();
            //groupHolder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);
            groupHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            //groupHolder.tv_childtitle = (TextView) convertView.findViewById(R.id.tv_childtitle);
            groupHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            //groupHolder.rl = (RelativeLayout)convertView.findViewById(R.id.rl2);
            groupHolder.v_line = (View) convertView.findViewById(R.id.v_line);
            groupHolder.iv_indicator = (ImageView)convertView.findViewById(R.id.indicator);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        //groupHolder.iv_image.setImageResource(listParentData.get(groupPosition).getIconID());
        groupHolder.tv_title.setText(listParentData.get(groupPosition).getTitle());
        //groupHolder.tv_childtitle.setText(listParentData.get(groupPosition).getChildTitle());
        if(mParentDateChildTitleVisible) {
            //groupHolder.rl.setVisibility(View.VISIBLE);
            //groupHolder.tv_childtitle.setVisibility(View.VISIBLE);
        }
        groupHolder.tv_content.setText(FileSizeFormatter.transformShortType(
                listParentData.get(groupPosition).getDataSize()) + context.getString(R.string.B));

        if(isExpanded){
            groupHolder.iv_indicator.setBackgroundResource(R.drawable.expandable_listview_close);
            groupHolder.v_line.setVisibility(View.INVISIBLE);
        }else{
            groupHolder.iv_indicator.setBackgroundResource(R.drawable.expandable_listview_open);
            groupHolder.v_line.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
    private View v = null;
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        int type = getChildType(groupPosition,childPosition);

        if (type == TYPE_2) {
            ItemHolder2 itemHolder = null;
            if (convertView == null) {
                if(groupPosition == 0){
                    convertView = mLayoutInflater.inflate(R.layout.garbage_cleanup_expendlist_child3, null);
                }else {
                    convertView = mLayoutInflater.inflate(R.layout.garbage_cleanup_expendlist_child2, null);
                }
                itemHolder = new ItemHolder2();
                itemHolder.tv_childtitle = (TextView)convertView.findViewById(R.id.tv_childtitle);
                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ItemHolder2) convertView.getTag();
            }
            v = convertView;
            String contentScanState = context.getString(R.string.selected);
            String B = context.getString(R.string.B);
            String childTitle = contentScanState+ FileSizeFormatter.transformShortType(listParentData.get(groupPosition).getdataSelectedSize())+B;
            itemHolder.tv_childtitle.setText(childTitle);
        } else if(type == TYPE_1){
            ItemHolder1 itemHolder = null;
            if ((convertView == null)||((ItemHolder1)convertView.getTag()).needInflate) {
                convertView = mLayoutInflater.inflate(R.layout.garbage_cleanup_expendlist_child1, null);
                itemHolder = new ItemHolder1();
                itemHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
                itemHolder.tv_flowdata = (TextView) convertView.findViewById(R.id.tv_flowdata);
                itemHolder.chkbox = (CheckBox) convertView.findViewById(R.id.chkbox);
                itemHolder.img = (ImageView) convertView.findViewById(R.id.img);
                itemHolder.v_line = convertView.findViewById(R.id.v_line);
                itemHolder.needInflate = false;
                convertView.setTag(itemHolder);
            }else {
                itemHolder = (ItemHolder1) convertView.getTag();
            }
            itemHolder.tv_content.setText(listParentData.get(groupPosition).getChilds().get(
                    childPosition).getTitle());
            itemHolder.tv_flowdata.setText(
                    FileSizeFormatter.transformShortType(listParentData.get(groupPosition).getChilds().get(
                            childPosition).getDataSize()) + context.getString(R.string.B)
            );
            itemHolder.img.setImageDrawable(listParentData.get(groupPosition).getChilds().get(
                    childPosition).getDrawable());
            if (groupPosition == 0) {
                itemHolder.img.setVisibility(View.GONE);
            }else{
                itemHolder.img.setVisibility(View.VISIBLE);
            }

            if(childPosition >= (getChildrenCount(groupPosition) - 1)) {
                itemHolder.v_line.setVisibility(View.INVISIBLE);
            }
            else {
                itemHolder.v_line.setVisibility(View.VISIBLE);
            }

            /*RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) itemHolder.v_line.getLayoutParams();
            if(isLastChild && (listParentData.get(groupPosition).getChilds().size()-1 == childPosition)){
                param.leftMargin = 40;//for dp,need*2
            }else{
                param.leftMargin = 60;//for dp,need*2
            }
            itemHolder.v_line.setLayoutParams(param);*/

            v = convertView;
            //wangdsh Expandablelist Checkbox Bug Solution
            itemHolder.chkbox.setOnCheckedChangeListener(null);
            if (mChildCheckStates.containsKey(groupPosition)) {
                ArrayList<Boolean> getChecked = mChildCheckStates.get(groupPosition);
                if(childPosition<=(getChecked.size()-1)){
                    itemHolder.chkbox.setChecked(getChecked.get(childPosition));
                }
            } else {
                ArrayList<Boolean> getChecked = new ArrayList<Boolean>();
                for (int i = 0; i < getChildrenCount(groupPosition); i++) {
                    getChecked.add(true);
                }
                mChildCheckStates.put(groupPosition, getChecked);
                itemHolder.chkbox.setChecked(true);
            }
        }
        v.setBackgroundColor(context.getResources().getColor(R.color.expandable_list_child));

        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

class GroupHolder {
    public ImageView iv_indicator;
    public TextView tv_title;
    public TextView tv_content;
    public View v_line;
}

class ItemHolder1 {
    public ImageView img;
    public TextView tv_content;
    public TextView tv_flowdata;
    public CheckBox chkbox;
    public boolean needInflate;
    public View v_line;
}

class ItemHolder2 {
    public TextView tv_childtitle;
}