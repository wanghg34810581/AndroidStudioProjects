package com.guli.secmanager.VirusScan;

import java.util.ArrayList;

/**
 * Created by shenyan on 2016/4/15.
 */
public class ParentData {

    private String mParName;
    private String mParResult;
    private boolean mParTitleColor;
	private int mParIcon;
    private int mParChildSize;
    private int mParMulwareCount;
    private int mParSoftWareCount;
    private int mParScanType;
	
    private ArrayList<ChildData> mParChilds = new ArrayList<ChildData>();
    private ArrayList<String> mParResultList = new ArrayList<String>();
    private ArrayList<String> mParAdviceList= new ArrayList<String>();
    private ArrayList<String> mParPackageNameList= new ArrayList<String>();

    private ArrayList<String> mParPathNameList= new ArrayList<String>();

    public String getParName() {
        return mParName;
    }
    public void setParName(String pName) {
        this.mParName = pName;
    }

    public void setParIcon(int icon) {
        this.mParIcon = icon;
    }
    public int getParIcon() {
        return mParIcon;
    }

    public void setChildSize(int childSize) {
        this.mParChildSize = childSize;
    }
    public int getChildSize() {
        return mParChildSize;
    }

    public ArrayList<ChildData> getParChilds() {
        return mParChilds;
    }

    public void setParChilds(ArrayList<ChildData> childs) {
        this.mParChilds.clear();
        this.mParChilds.addAll(childs);
    }

    public void removeParChilds(int i) {
        this.mParChilds.remove(i);
    }

    public void setParResult(String result) {
        this.mParResult = result;
    }
    public String getParResult() {
        return mParResult;
    }

    public void removeParResultList(int index) {
        if(index != -1) {
            this.mParResultList.remove(index);
        }
    }

    public void removeParAdviceList(int index) {
        this.mParAdviceList.remove(index);
    }

    public void removeParPackageList(int index) {
        this.mParPackageNameList.remove(index);
    }

    public void removeParPathList(int index) {
        this.mParPathNameList.remove(index);
    }

    public void setParTitleColor(boolean enable) {
        this.mParTitleColor = enable;
    }

    public void setParMulwareCount(int count) {
        this.mParMulwareCount = count;
    }
    public int getParMulwareCount() {
        return mParMulwareCount;
    }

    public void setParSoftWareCount(int count) {
        this.mParSoftWareCount = count;
    }
    public int getParSoftWareCount() {
        return mParSoftWareCount;
    }

    public void setParScanType(int type) {
        this.mParScanType = type;
    }
    public int getParScanType() {
        return mParScanType;
    }

    public ArrayList<String> getParResultList() {
        return mParResultList;
    }
    public void setParResultList(ArrayList<String> list) {
        this.mParResultList = list;
    }

    public ArrayList<String>  getParAdviceList() {
        return mParAdviceList;
    }
    public void setParAdviceList(ArrayList<String> list) {
        this.mParAdviceList = list;
    }

    public ArrayList<String>  getParPackageNameList() {
        return mParPackageNameList;
    }
    public void setParPackageNameList(ArrayList<String> list) {
        this.mParPackageNameList = list;
    }

    public ArrayList<String> getParPathNameList() {
        return mParPathNameList;
    }
    public void setParPathNameList(ArrayList<String> parPathNameList) {
        this.mParPathNameList = parPathNameList;
    }
}
