package com.guli.secmanager.GarbageClean;

import android.graphics.drawable.Drawable;

/**
 * Created by wangdsh on 16-4-12.
 */
public class ChildObj {
    private Drawable drawable;
    private String title;
    private long dataSize;
    private boolean isChecked;
    private boolean isFirst;
    //private boolean isDeleted;

    public ChildObj() {

    }

    public ChildObj(Drawable drawable, String title, long dataSize, boolean isChecked) {
        this.drawable = drawable;
        this.title = title;
        this.dataSize = dataSize;
        this.isChecked = isChecked;
        this.isFirst = false;
//        this.isDeleted = false;
    }


    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDataSize() {
        return dataSize;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }

    public long addData(long data) {
        dataSize += data;
        return this.dataSize;
    }
    public boolean isChecked() {
        return isChecked;
    }

    public void setIschecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setIsFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }
//    public boolean isDeleted() {
//        return isDeleted;
//    }
//
//    public void setIsDeleted(boolean isDeleted) {
//        this.isDeleted = isDeleted;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChildObj)) return false;

        ChildObj childObj = (ChildObj) o;

        return getTitle().equals(childObj.getTitle());

    }

    @Override
    public int hashCode() {
        return getTitle().hashCode();
    }
}
