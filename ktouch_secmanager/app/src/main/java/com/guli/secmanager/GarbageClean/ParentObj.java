package com.guli.secmanager.GarbageClean;

import android.content.Context;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.FileSizeFormatter;

import java.util.ArrayList;

/**
 * Created by wangdsh on 16-4-12.
 */
public class ParentObj{

    private int iconID;
    private String title;
    private long dataSelectedSize = 0L;
    private long dataSize = 0L;
    private ArrayList<ChildObj> childs = new ArrayList<ChildObj>();

    public ParentObj(){

    }

    public ParentObj(int iconID, String title, long dataSize) {
        this.iconID = iconID;
        this.title = title;
        this.dataSize = dataSize;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getdataSelectedSize() {
        if(dataSelectedSize == 0L){
            if (childs != null) {
                dataSelectedSize = 0L;
                for (int i = 0; i < childs.size(); i++) {
                    if (childs.get(i).isChecked()) {
                        dataSelectedSize += childs.get(i).getDataSize();
                    }
                }
            }
        }

        return dataSelectedSize;
    }

    public long getDataSize() {
        if(childs!=null){
            dataSize = 0L;
            dataSelectedSize = 0L;
            for(int i=0;i<childs.size();i++) {
                dataSize += childs.get(i).getDataSize();
                if (childs.get(i).isChecked()) {
                    dataSelectedSize += childs.get(i).getDataSize();
                }
            }
        }
        return dataSize;
    }

    public long getSelectedDataSize(){
        return dataSelectedSize;
    }
    public void setSelectedDataSize(long dataSelectedSize){
        this.dataSelectedSize = dataSelectedSize;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }

    public long addData(long data) {
        dataSize += data;
        return dataSize;
    }
    public ArrayList<ChildObj> getChilds() {
        return childs;
    }

    public void setChilds(ArrayList<ChildObj> childs) {
        this.childs = childs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParentObj)) return false;

        ParentObj parentObj = (ParentObj) o;

        return getTitle().equals(parentObj.getTitle());

    }

    @Override
    public int hashCode() {
        return getTitle().hashCode();
    }
}
