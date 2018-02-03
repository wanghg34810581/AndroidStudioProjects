package com.guli.secmanager.VirusScan;

/**
 * Created by shenyan on 2016/4/15.
 */
public class ChildData {
    private String mChiName;
    private int mChiIcon;
    private String mChiResult;
    private boolean isChecked;

    public String getChiName() {
        return mChiName;
    }
    public void setChiName(String cName) {
        this.mChiName = cName;
    }

    public void setChiIcon(int icon) {
        this.mChiIcon = icon;
    }
    public int getChiIcon() {
        return mChiIcon;
    }

    public void setChiResult(String result) {
        this.mChiResult = result;
    }
    public String getChiResult() {
        return mChiResult;
    }

    public boolean isChecked() {
        return isChecked;
    }
    public void setIschecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
