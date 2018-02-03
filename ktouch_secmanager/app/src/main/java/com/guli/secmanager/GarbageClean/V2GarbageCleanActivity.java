package com.guli.secmanager.GarbageClean;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guli.secmanager.R;

import com.guli.secmanager.Utils.FileSizeFormatter;
import com.guli.secmanager.Utils.LogUtil;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.V2FinishActivity;
import com.guli.secmanager.widget.WaterWaveView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.RubbishEntity;
import tmsdk.fg.module.deepclean.RubbishEntityManager;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.deepclean.ScanProcessListener;

//import com.guli.secmanager.Utils.UnitConverter;

public class V2GarbageCleanActivity extends AppCompatActivity {
    //Debug
    private static final String TAG = "GarbageCleanUpActivity";

    //UI View
    private ExpandableListView expandableListView;
    GarbageCLeanExpandableListViewAdapter mgarbageCLeanExpandableListViewAdapter;
    private TextView tx_scan_ongoing_2;
    private TextView tx_unit_2;
    private TextView btn_scan_total;
    private Button mCompleteBtn;

    private TextView mGarbageFileSize;
    private long mGarbageFileRubbishSum = 0L;

    private TextView mInstallPKGSize;
    private long mInstallPKGRubbishSum = 0L;

    private TextView mSoftCacheSize;
    private long mSoftCacheRubbishSum = 0L;

    private ImageButton mActionBarButton;
    private LinearLayout mLayoutActionbar;
    private RelativeLayout mScanningLayout;
    private RelativeLayout mScanFinishLayout;

    //expandableList
    private ArrayList<ParentObj> listParentData = new ArrayList<ParentObj>();
    private ArrayList<ChildObj> listChildGarbageFile = new ArrayList<ChildObj>();
    private ArrayList<ChildObj> listChildInstallPkg = new ArrayList<ChildObj>();
    private ArrayList<ChildObj> listChildSftCache = new ArrayList<ChildObj>();
    public int EXPANDABLE_INDEX_GARBAGEFILE = 0;
    public int EXPANDABLE_INDEX_INSTALLPKG = 1;
    public int EXPANDABLE_INDEX_SFTCACHE = 2;

    private DeepcleanManager mDeepcleanManager;

    //Logic
    private boolean TM_WRITE_LOG = false;
    private boolean bExpandableListExpand = false;
    private long rubbishSum = 0L;

    //Handler
    private Handler mUIHandler = new Handler() {
        String displayData;
        String displayDataUnit;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UIHandlerMsg.MSG_SCAN_START:
                    break;
                case UIHandlerMsg.MSG_SCAN_PROGRESS_FINISH:
                    doScanFinish();
                    break;
                case UIHandlerMsg.MSG_SCAN_REFRESH_PROGRESS:
                    doScanRefreshProgress(msg);
                    break;
                case UIHandlerMsg.MSG_CLEAN_START:
                    //doStartBackgroundAnim();
                    break;
                case UIHandlerMsg.MSG_CLEAN_REFRESH_PROGRESS:
                    doCleanRefreshProgress(msg);
                    break;
                case UIHandlerMsg.MSG_CLEAN_PROGRESS_FINISH:
                    doCleanFinish();
                    //doCancelBackgroundAnim();
                    break;
                case UIHandlerMsg.MSG_UPDATE_TEXT:
                    doUpdateText();
                    break;
                default:
                    break;
            }
        }

        private void doUpdateText() {
            rubbishSum = 0L;
            for(int i=0;i<listParentData.size();i++){
                rubbishSum += listParentData.get(i).getSelectedDataSize();
            }

            displayData = FileSizeFormatter.transformShortType(rubbishSum);
            displayDataUnit = displayData.substring(displayData.length()-1,displayData.length())+getString(R.string.B);
            displayData = displayData.substring(0, displayData.length() - 1);
            if(Float.valueOf(displayData) == 0){
                displayDataUnit ="KB";
            }
            tx_scan_ongoing_2.setText(displayData);
            tx_unit_2.setText(displayDataUnit);
        }

        private void doCleanRefreshProgress(Message msg) {
            long currenCleanSize = (long)msg.obj;
            Log.w(TAG,"rubbishSum = "+rubbishSum +"|currenCleanSize = "+currenCleanSize);
            displayData = FileSizeFormatter.transformShortType(rubbishSum - currenCleanSize);
            displayDataUnit = displayData.substring(displayData.length()-1,displayData.length())+getString(R.string.B);
            displayData = displayData.substring(0, displayData.length() - 1);
            if(Float.valueOf(displayData) == 0){
                displayDataUnit ="KB";
            }
            tx_scan_ongoing_2.setText(displayData);
            tx_unit_2.setText(displayDataUnit);
        }

        private void doCleanFinish() {
            SharedPreferences sPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
            Long total = sPreferences.getLong(ShareUtil.GARBAGE_TOTAL, 0);
            SharedPreferences.Editor editor = sPreferences.edit();
            editor.putLong(ShareUtil.GARBAGE_TOTAL, rubbishSum + total);
            editor.commit();

            //rubbishSum = 0L;
            displayData = FileSizeFormatter.transformShortType(0L);
            displayDataUnit = displayData.substring(displayData.length()-1,displayData.length())+getString(R.string.B);
            displayData = displayData.substring(0, displayData.length() - 1);
            if(Float.valueOf(displayData) == 0){
                displayDataUnit ="KB";
            }
            tx_scan_ongoing_2.setText(displayData);
            tx_unit_2.setText(displayDataUnit);
            removeSelectedItems();
        }

        private void doScanRefreshProgress(Message msg) {
            if(msg.arg1 == UIHandlerMsg.MSG_SCANNING) {
                int progress = msg.arg2;
                mWaterWaveProgress.setProgress((float) ((float)progress / (float)100));
            }else if(msg.arg1 == UIHandlerMsg.MSG_SCAN_RUBBISH_FOUND){
                long size = (long)msg.obj;
                rubbishSum += size;
                displayData = FileSizeFormatter.transformShortType(rubbishSum);
                displayDataUnit = displayData.substring(displayData.length()-1,displayData.length())+getString(R.string.B);
                displayData = displayData.substring(0,displayData.length()-1);
                mWaterWaveProgress.setScanOngoing(displayData);
                tx_scan_ongoing_2.setText(displayData);
                if(Float.valueOf(displayData) == 0.0f){
                    displayDataUnit ="KB";
                }
                mWaterWaveProgress.setUnit(displayDataUnit);
                tx_unit_2.setText(displayDataUnit);

                mInstallPKGSize.setText(FileSizeFormatter.transformShortType(mInstallPKGRubbishSum) + getString(R.string.B));
                mSoftCacheSize.setText(FileSizeFormatter.transformShortType(mSoftCacheRubbishSum) + getString(R.string.B));
                mGarbageFileSize.setText(FileSizeFormatter.transformShortType(mGarbageFileRubbishSum) + getString(R.string.B));

                mgarbageCLeanExpandableListViewAdapter.notifyDataSetChanged();
            }
        }

        private void doScanFinish() {
            if(listChildGarbageFile == null) {return;}

            Comparator<ChildObj> comparator = new Comparator<ChildObj>() {
                @Override
                public int compare(ChildObj lhs, ChildObj rhs) {
                    long result = lhs.getDataSize() - rhs.getDataSize();
                    //Log.d(TAG,"lhs.getDataSize()="+lhs.getDataSize()+"|rhs.getDataSize()"+rhs.getDataSize());
                    if(lhs.isFirst()){
                        return -1;
                    }
                    if(rhs.isFirst()){
                        return 1;
                    }
                    if (result > 0L) {
                        return -1;
                    } else if (result < 0L) {
                        return 1;
                    }
                    return 0;
                }
            };

            mScanningLayout = (RelativeLayout) findViewById(R.id.scanning_layout);
            mScanningLayout.setVisibility(View.GONE);
            mWaterWaveProgress.stopWave();

            mScanFinishLayout = (RelativeLayout) findViewById(R.id.scan_finish_layout);
            mScanFinishLayout.setVisibility(View.VISIBLE);

            displayData = FileSizeFormatter.transformShortType(rubbishSum);
            displayDataUnit = displayData.substring(displayData.length()-1,displayData.length())+getString(R.string.B);
            displayData = displayData.substring(0,displayData.length()-1);
            if(Float.valueOf(displayData) == 0.0f){
                displayDataUnit ="KB";
            }
            tx_scan_ongoing_2.setText(displayData);
            tx_unit_2.setText(displayDataUnit);
            btn_scan_total.setText(getString(R.string.garbage_clean_scan_total, displayData, displayDataUnit));

            Collections.sort(listChildGarbageFile, comparator);
            Collections.sort(listChildInstallPkg, comparator);
            Collections.sort(listChildSftCache, comparator);
            bExpandableListExpand = true;
            mgarbageCLeanExpandableListViewAdapter.setParentDataChildTitleVisible(true);
            mgarbageCLeanExpandableListViewAdapter.notifyDataSetChanged();
            expandableListView.setGroupIndicator(null);
            for(int i=0;i<listParentData.size();i++){
                if(!(listParentData.get(i).getDataSize() == 0L)){
                    expandableListView.expandGroup(i, false);
                }
            }
        }
    };

    //android Activity Structure start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitView();
        InitActionBar();

        mDeepcleanManager = ManagerCreatorF.getManager(DeepcleanManager.class);
        if (!mDeepcleanManager.init(initProcessListener())) {
            //初始化错误操作
            Log.e(TAG,"DeepcleanManager Init mScanProcessListener Error");
        }
        mDeepcleanManager.startScan(RubbishType.SCAN_FLAG_ALL);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(mDeepcleanManager != null) {
            mDeepcleanManager.cancelScan();
            mDeepcleanManager.cancelClean();
            mDeepcleanManager.onDestory();
            mDeepcleanManager = null;
        }

        mWaterWaveProgress.stopWave();

        if(mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
            //mUIHandler = null;
        }

        listChildGarbageFile.removeAll(listChildGarbageFile);
        listChildSftCache.removeAll(listChildSftCache);
        listChildInstallPkg.removeAll(listChildInstallPkg);
        listChildGarbageFile = null;
        listChildSftCache = null;
        listChildInstallPkg = null;
        //listParentData.get(EXPANDABLE_INDEX_GARBAGEFILE).setChilds(listChildGarbageFile);
        //listParentData.get(EXPANDABLE_INDEX_INSTALLPKG).setChilds(listChildInstallPkg);
        //listParentData.get(EXPANDABLE_INDEX_SFTCACHE).setChilds(listChildSftCache);
        listParentData.removeAll(listParentData);
        listParentData = null;
        mgarbageCLeanExpandableListViewAdapter = null;

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 300:
                setResult(RESULT_OK);
                finish();
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //android Activity Structure  end

    //just for remove Items When necessary start
    private void removeUnVisibleItems(){
        int packedPositionType;
        int groupPosition;
        int childPosition;
        int firstVis = expandableListView.getFirstVisiblePosition();
        int lastVis = expandableListView.getLastVisiblePosition();
        int lastPos = 0;
        int firstPos = 0;
        int count;
        long packedPosition;

        for(int i=0;i<mgarbageCLeanExpandableListViewAdapter.getGroupCount();i++){
            lastPos+= (mgarbageCLeanExpandableListViewAdapter.getChildrenCount(i)+1);
        }
        lastPos--;
        Log.d(TAG,"lastPos="+lastPos+"firstPos="+firstPos+" | lastVis="+lastVis+"firstVis="+firstVis);
        count = lastPos - firstPos;
        while(count>=0){//lastPos Question
            if((count>lastVis && count<=lastPos)||(count>=firstPos && count<firstVis)){
                Log.d(TAG,"1::CHILD Effect Count1 = "+count);
                packedPosition = expandableListView.getExpandableListPosition(count);
                packedPositionType = ExpandableListView.getPackedPositionType(packedPosition);
                if (packedPositionType != ExpandableListView.PACKED_POSITION_TYPE_NULL) {
                    groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                    if (packedPositionType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
                        Log.d(TAG, "1::Child PACKED_POSITION_TYPE_CHILD,Do Sth");
                        if (childPosition != 0) {
                            Log.d(TAG, "1::Effect Position{" + groupPosition + "," + childPosition + "}");
                            ArrayList<Boolean> arrayListChkBox = mgarbageCLeanExpandableListViewAdapter.getmChildChkBoxStates(groupPosition);
                            if (listParentData.get(groupPosition).getChilds().get(childPosition).isChecked()) {
                                listParentData.get(groupPosition).getChilds().remove(childPosition);
                                if((arrayListChkBox!=null)&&(arrayListChkBox.size()>childPosition)) {
                                    arrayListChkBox.remove(childPosition);
                                }
                                //mgarbageCLeanExpandableListViewAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        Log.d(TAG, "1::Group PACKED_POSITION_TYPE_GROUP,Do Nothing");
                    }
                }
            }
            Log.d(TAG,"1::CHILD EXE Count = "+count);
            count--;
        }
        mgarbageCLeanExpandableListViewAdapter.notifyDataSetChanged();
    }
    private synchronized void removeItem(int count) {
        long packedPosition;
        int packedPositionType;
        int groupPosition;
        int childPosition;
        boolean flag = true;
//        int firstVisTest = expandableListView.getFirstVisiblePosition();
//        int lastVisTest = expandableListView.getLastVisiblePosition();
//        Log.d(TAG, "2::delete Cell Data,DataSync,firstVisTest="+firstVisTest+"| lastVisTest="+lastVisTest);
        final View view = expandableListView.getChildAt(count);
        //final int firstVis = expandableListView.getFirstVisiblePosition();

        if (view != null && listParentData.size() > 0) {
            Log.d(TAG, "2::delete Cell Data,DataSync,count="+count);
            packedPosition = expandableListView.getExpandableListPosition(count);
            packedPositionType = ExpandableListView.getPackedPositionType(packedPosition);
            if (packedPositionType != ExpandableListView.PACKED_POSITION_TYPE_NULL) {
                groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                if (packedPositionType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
                    Log.d(TAG, "2::Child PACKED_POSITION_TYPE_CHILD,Do Sth");
                    if(childPosition!=0) {
                        Log.d(TAG, "2::Effect Position{" + groupPosition + "," + childPosition + "}");
                        if(listParentData.get(groupPosition).getChilds().get(childPosition).isChecked()) {
                            AnimationSet animationSet = collapse(300);
                            final int s_groupPosition = groupPosition;
                            final int s_childPosition = childPosition;
                            final int s_count = count-1;
                            flag = false;
                            Log.d(TAG, "2::Animation End stage 0");
                            mUIHandler.postDelayed(new Runnable() {
                                public void run() {
                                    //view.clearAnimation();
                                    if(listParentData.size() <= s_groupPosition) {return;}
                                    if(listParentData.get(s_groupPosition).getChilds().size() <= s_childPosition) {return;}
                                    listParentData.get(s_groupPosition).getChilds().remove(s_childPosition);
                                    ArrayList<Boolean> arrayListChkBox = mgarbageCLeanExpandableListViewAdapter.getmChildChkBoxStates(s_groupPosition);
                                    arrayListChkBox.remove(s_childPosition);
                                    mgarbageCLeanExpandableListViewAdapter.notifyDataSetChanged();
                                    if (s_count >= 0) {
                                        Log.d(TAG, "2::Animation End stage 1");
                                        removeItem(s_count);
                                        Log.d(TAG, "2::Animation End stage 2");
                                    } else if (s_count == -1) {
                                        Log.d(TAG, "2::doAfterRemoveSelectedItems()_1");
                                        doAfterRemoveSelectedItems();
                                    }
                                    Log.d(TAG, "2::Animation End stage 3");
                                }
                            }, animationSet.getDuration());
                            view.startAnimation(animationSet);
                        }
                    }else{
                        Log.d(TAG,"2::UnEffect Position{" + groupPosition + ",0}");
                    }
                }else if(packedPositionType == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
                    Log.d(TAG,"2::Packed POS GROUP,Do Nothing");
                }else {
                    Log.e(TAG,"2::Packed POS NOT GROUP && NOT CHILD && NOT NULL");
                }
            } else {
                Log.d(TAG,"2::Packed POS == NULL");
            }
        } else {
            Log.d(TAG, "2::View == NULL");
        }
        if(flag) {
            if (count>0) {
                removeItem(count-1);
            } else if (count == 0) {
                Log.d(TAG, "2::doAfterRemoveSelectedItems()_2");
                doAfterRemoveSelectedItems();
            }
        }
    }

    private void removeVisibleItems(){
        int firstVis = expandableListView.getFirstVisiblePosition();
        int lastVis = expandableListView.getLastVisiblePosition();
        int firstPos=0;
        int lastPos=0;
        for(int i=0;i<mgarbageCLeanExpandableListViewAdapter.getGroupCount();i++){
            lastPos+= (mgarbageCLeanExpandableListViewAdapter.getChildrenCount(i)+1);
        }
        lastPos--;
        int count = lastVis - firstVis;
        Log.d(TAG, "removeVisibleItems:lastPos=" + lastPos + " | firstPos=" + firstPos + " | lastVis=" + lastVis + " | firstVis=" + firstVis);
        removeItem(count);
        //Log.d(TAG,"removeVisibleItems:lastPos="+lastPos+"| firstPos="+firstPos);
        //removeItem(lastVis);
    }

    private void doAfterRemoveSelectedItems(){
        /*for(int i=0;i<listParentData.size();i++){
            for(int j=listParentData.get(i).getChilds().size()-1;j>0;j--){
                if(listParentData.get(i).getChilds().get(j).isChecked()){
                    listParentData.get(i).getChilds().remove(j);
                }
            }
        }
        mgarbageCLeanExpandableListViewAdapter.notifyDataSetChanged();
        for(int i=0;i<listParentData.size();i++){
            if((listParentData.get(i).getDataSize() == 0L)){
                expandableListView.collapseGroup(i);
            }
        }
        mCompleteBtn.setClickable(true);*/

        mUIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClass(V2GarbageCleanActivity.this, V2FinishActivity.class);
                intent.putExtra("from", "V2GarbageCleanActivity");
                intent.putExtra("totalSize", rubbishSum);
                startActivityForResult(intent, 300);
            }
        }, 300);
    }

    private void removeSelectedItems() {
        //removeUnVisibleItems();
        removeVisibleItems();
    }

    private int getGarbageCleanActivityWindowWidth(){
        DisplayMetrics dm = new DisplayMetrics();
        //获取屏幕信息为后续动画做基础
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        //int screenHeigh = dm.heightPixels;
        return screenWidth;
    }
    private AnimationSet collapse(int duration){
        final int initialWidth = getGarbageCleanActivityWindowWidth();
        AnimationSet animationSet = new AnimationSet(true);

        TranslateAnimation translateAnimation = new TranslateAnimation(0f,initialWidth*(-1),0f,0f);
        translateAnimation.setDuration(duration);
        animationSet.addAnimation(translateAnimation);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(duration);
        animationSet.addAnimation(alphaAnimation);
        return animationSet;
    }
    //just for remove Items When necessary  end

    private ScanProcessListener initProcessListener() {

        return new ScanProcessListener() {

            boolean []b ={true,true,true,true,true};

            @Override
            public void onScanStarted() {
                if(mUIHandler != null) {
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_SCAN_START);
                    msg.sendToTarget();
                }
                Log.i(TAG, "onScanStarted : ");
            }

            @Override
            public void onScanProcessChange(int nowPercent, String scanPath) {
                Log.w(TAG, "onScanProcessChange : "+nowPercent+" %");
                if(listParentData == null || listParentData.size() == 0) {return;}

                if(mUIHandler != null) {
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_SCAN_REFRESH_PROGRESS);
                    msg.obj = scanPath;
                    msg.arg1 = UIHandlerMsg.MSG_SCANNING;
                    //nowPercent %= 100;
                    msg.arg2 = nowPercent;
                    msg.sendToTarget();
                }
            }

            @Override
            public void onScanFinished() {
                Log.i(TAG, "onScanFinished : ");
                if(mUIHandler != null) {
                    mUIHandler.sendEmptyMessage(UIHandlerMsg.MSG_SCAN_PROGRESS_FINISH);
                }

                if(TM_WRITE_LOG){
                    File _tmpFile = new File("/sdcard/demo_all_rubbish.txt");
                    _tmpFile.deleteOnExit();
                    int _currentRubbishType = -1;
                    List<RubbishEntity> _Rubbishes = mDeepcleanManager.getmRubbishEntityManager().getRubbishes();
                    for(RubbishEntity aRubbish:_Rubbishes){

                        StringBuffer sbtips = new StringBuffer();
                        if(_currentRubbishType != aRubbish.getRubbishType() ){
                            sbtips.append("【垃圾类型】 " );
                            switch(aRubbish.getRubbishType()){
                                case RubbishType.INDEX_APK:
                                    sbtips.append("——————————————————————【APK】——————————————————————\n" );
                                    break;
                                case RubbishType.INDEX_SOFT_RUNTIMG_RUBBISH:
                                    sbtips.append("——————————————————————【系统垃圾】——————————————————————\n " );
                                    break;
                                case RubbishType.INDEX_SOFTWARE_CACHE:
                                    sbtips.append("——————————————————————【软件缓存】——————————————————————\n " );
                                    break;
                                case RubbishType.INDEX_UNINSTALL_RETAIL:
                                    sbtips.append("——————————————————————【卸载残余】 ——————————————————————\n " );
                                    break;
                            }

                            _currentRubbishType = aRubbish.getRubbishType() ;
                        }
                        sbtips.append("垃圾大小：").append(FileSizeFormatter.transformShortType(aRubbish.getSize())).append("\n");
                        sbtips.append("垃圾描述：").append(aRubbish.getDescription()).append(" ");
                        if(aRubbish.isSuggest()){
                            sbtips.append("是否建议：").append(aRubbish.isSuggest()).append(" XXX_DEL");
                        }else{
                            sbtips.append("是否建议：").append(aRubbish.isSuggest()).append(" XXX_NOT_DEL");
                        }

                        sbtips.append("所属应用：").append(aRubbish.getAppName()).append(" ");
                        sbtips.append("应用包名：").append(aRubbish.getPackageName()).append("\n");
                        List<String>  _rubbishkeys = aRubbish.getRubbishKey();
                        if(null!=_rubbishkeys){
                            for(String _p : _rubbishkeys){
                                sbtips.append( _p +" \n ");
                            }
                        }
                        LogUtil.writeLog("/sdcard/demo_all_rubbish.txt", sbtips.toString(), true);
                    }
                }
            }

            @Override
            public void onScanCanceled() {
                Log.i(TAG, "onScanCanceled : ");
            }

            @Override
            public void onScanError(int error) {
                // TODO Auto-generated method stub
                if (DeepcleanManager.ERROR_CODE_SCAN_LOAD_ERROR == error)
                    Log.e("onScanError", "load  do error!!   report in ui ...");
                else if (DeepcleanManager.ERROR_CODE_PROCESS_ERROR == error)
                    Log.e("onScanError", "service error!!   report in ui ...");
                Log.i(TAG, "onScanError : ");
            }

            public void onRubbishFound(RubbishEntity aRubbish) {
                Log.i(TAG, "onRubbishFound : ");
                if(listParentData == null || listParentData.size() == 0) {return;}

                ChildDataInit(aRubbish);

                if(mUIHandler != null) {
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_SCAN_REFRESH_PROGRESS);
                    msg.arg1 = UIHandlerMsg.MSG_SCAN_RUBBISH_FOUND;
                    msg.obj = aRubbish.getSize();// type long
                    msg.sendToTarget();
                }

                if(TM_WRITE_LOG) {
                    Log.i(TAG,"onRubbishFound : "  );

                    StringBuffer sbtips = new StringBuffer();
                    sbtips.append("垃圾大小：").append(FileSizeFormatter.transformShortType(aRubbish.getSize())).append("\n");
                    sbtips.append("所属应用：").append(aRubbish.getAppName()).append("\n");
                    sbtips.append("应用包名：").append(aRubbish.getPackageName()).append("\n");
                    if(RubbishType.INDEX_APK==aRubbish.getRubbishType()){
                        sbtips.append("垃圾描述：").append("APK　安装包\n");
                    }else{
                        sbtips.append("垃圾描述：").append(aRubbish.getDescription()).append("\n");
                    }
                    List<String>  _rubbishkeys = aRubbish.getRubbishKey();
                    if(null!=_rubbishkeys){
                        for(String _p : _rubbishkeys){
                            sbtips.append( _p +" :: ");
                        }
                        sbtips.append("\n");
                    }
                    Log.e("onRubbishFound"," "+sbtips.toString());

                    StringBuffer sb = new StringBuffer();
                    sb.append("---找到垃圾---").append("\n");
                    sb.append("垃圾类型：").append(aRubbish.getRubbishType()).append("\n");
                    sb.append("垃圾大小：").append(FileSizeFormatter.transformShortType(aRubbish.getSize())).append("\n");
                    sb.append("是否建议：").append(aRubbish.isSuggest()).append("\n");
                    sb.append("所属应用：").append(aRubbish.getAppName()).append("\n");
                    sb.append("应用包名：").append(aRubbish.getPackageName()).append("\n");
                    sb.append("垃圾描述：").append(aRubbish.getDescription()).append("\n");
                    _rubbishkeys = aRubbish.getRubbishKey();
                    if(null!=_rubbishkeys){
                        for(String _p : _rubbishkeys){
                            sb.append( _p +" :: ");
                        }
                        sb.append("\n");
                    }
                    Log.e("onRubbishFound"," "+sb.toString());

                    StringBuffer sbx = new StringBuffer();
                    sbx.append("---找到垃圾---").append(" || ");
                    sbx.append("垃圾大小：").append(FileSizeFormatter.transformShortType(aRubbish.getSize())).append(" || ");
                    sbx.append("是否建议：").append(aRubbish.isSuggest()).append("\n");
                    sbx.append("所属应用：").append(aRubbish.getAppName()).append(" || ");
                    sbx.append("应用包名：").append(aRubbish.getPackageName()).append(" || ");
                    sbx.append("垃圾描述：").append(aRubbish.getDescription()).append("\n");
                    if(null!=_rubbishkeys){
                        for(String _p : _rubbishkeys){
                            sbx.append( _p +" :: ");
                        }
                        sbx.append("\n");
                    }
                    switch(aRubbish.getRubbishType()){
                        case RubbishType.INDEX_APK:
                            LogUtil.writeLog("/sdcard/demo_apk_onRubbishFound.txt", sbx.toString(),true);
                            break;
                        case RubbishType.INDEX_SOFT_RUNTIMG_RUBBISH:
                            LogUtil.writeLog("/sdcard/demo_general_onRubbishFound.txt", sbx.toString(),true);
                            break;
                        case RubbishType.INDEX_SOFTWARE_CACHE:
                            LogUtil.writeLog("/sdcard/demo_install_onRubbishFound.txt", sbx.toString(),true);
                            break;
                        case RubbishType.INDEX_UNINSTALL_RETAIL:
                            LogUtil.writeLog("/sdcard/demo_uninstall_onRubbishFound.txt", sbx.toString(),true);
                            break;
                    }
                }
            }

            public void onCleanStart() {
                if(mUIHandler != null) {
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_CLEAN_START);
                    msg.sendToTarget();
                }
                Log.i(TAG, "onCleanStart : ");
            }

            @Override
            public void onCleanProcessChange(long currenCleanSize, int nowPercent) {
                Log.i(TAG, "onCleanProcessChange : " + nowPercent + "% ::" + currenCleanSize);

                if(mUIHandler == null) {return;}
                if(listParentData == null || listParentData.size() == 0) {return;}

                if(nowPercent>0 && b[0]) {
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_CLEAN_REFRESH_PROGRESS);
                    msg.arg1 = 0;
                    msg.obj = currenCleanSize;
                    msg.sendToTarget();
                    b[0] = false;
                }else if(nowPercent>25 && b[1]){
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_CLEAN_REFRESH_PROGRESS);
                    msg.arg1 = 25;
                    msg.obj = currenCleanSize;
                    msg.sendToTarget();
                    b[1] = false;
                }else if(nowPercent>50 && b[2]){
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_CLEAN_REFRESH_PROGRESS);
                    msg.arg1 = 50;
                    msg.obj = currenCleanSize;
                    msg.sendToTarget();
                    b[2] = false;
                }else if(nowPercent>75 && b[3]){
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_CLEAN_REFRESH_PROGRESS);
                    msg.arg1 = 75;
                    msg.obj = currenCleanSize;
                    msg.sendToTarget();
                    b[3] = false;
                }else if(nowPercent>90 && b[4]){
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_CLEAN_REFRESH_PROGRESS);
                    msg.arg1 = 100;
                    msg.obj = currenCleanSize;
                    msg.sendToTarget();
                    b[4] = false;
                }
            }

            @Override
            public void onCleanCancel() {
                Log.i(TAG, "onCleanCancel : ");
                //清理出错，自动退出清理垃圾。
            }

            /***
             * 完全清理完毕
             */
            public void onCleanFinish() {
                if(mUIHandler != null) {
                    Message msg = mUIHandler.obtainMessage(UIHandlerMsg.MSG_CLEAN_PROGRESS_FINISH);
                    msg.sendToTarget();
                }
                Log.i(TAG, "onCleanFinish : ");
            }

            @Override
            public void onCleanError(int error) {
                if (DeepcleanManager.ERROR_CODE_SCAN_LOAD_ERROR == error)
                    Log.e("onCleanError", "load  do error!!   report in ui ...");
                else if (DeepcleanManager.ERROR_CODE_PROCESS_ERROR == error)
                    Log.e("onCleanError", "service error!!   report in ui ...");
                Log.i(TAG, "onCleanError : ");
            }
        };
    }
    //Sth about GarbageClean Service  end

    //Init Expandable ListView Data start
    private List<ParentObj> ParentDataInit() {
        String[] title = {getString(R.string.GarbageFile), getString(R.string.InstallPackage), getString(R.string.SoftwareCache)};
        String[] childtitle = new String[title.length];
        int[] iconID = {R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher};

        for (int i = 0; i < childtitle.length; i++) {
            childtitle[i] = getString(R.string.selected)+String.valueOf(0)+getString(R.string.MB);
        }

        for (int i = 0; i < 3; i++) {
            ParentObj p = new ParentObj(iconID[i], title[i], 0L);
            listParentData.add(p);
        }
        listParentData.get(EXPANDABLE_INDEX_GARBAGEFILE).setChilds(listChildGarbageFile);
        listParentData.get(EXPANDABLE_INDEX_INSTALLPKG).setChilds(listChildInstallPkg);
        listParentData.get(EXPANDABLE_INDEX_SFTCACHE).setChilds(listChildSftCache);
        ChildObj childObj;// = new ChildObj();
        for(int i=0;i<listParentData.size();i++){
            childObj = new ChildObj();
            childObj.setIsFirst(true);
            listParentData.get(i).getChilds().add(childObj);
        }
        return listParentData;
    }
    private void  ChildDataInit(RubbishEntity aRubbish) {
        Drawable drawable = null;//ContextCompat.getDrawable(V2GarbageCleanActivity.this, R.drawable.uninstall_app);
        long rubbishSize = aRubbish.getSize();
        ChildObj childObj;
        int index;
        PackageManager packageManager = getPackageManager();

        switch(aRubbish.getRubbishType()) {
            case RubbishType.INDEX_APK:
                Log.w(TAG,"RubbishType.INDEX_APK");
                Log.w(TAG,"APK-Name:"+aRubbish.getAppName()+"|Desc:"+aRubbish.getDescription()+"|Pkg:"+aRubbish.getDescription()+"||E");
                String preStr = "";
                for(String str:aRubbish.path){
                    preStr += str+";";
                }
                Log.e(TAG,"Path = "+preStr+";Path.length="+aRubbish.path.size());
                if(aRubbish.getPackageName()!=null) {
                    //drawable = getApkIcon(V2GarbageCleanActivity.this, aRubbish.getPackageName());
                    try {
                        drawable = packageManager.getApplicationIcon(aRubbish.getPackageName());
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    childObj = new ChildObj(drawable, aRubbish.getAppName(), rubbishSize, true);
                    listChildInstallPkg.add(childObj);
                    listParentData.get(EXPANDABLE_INDEX_INSTALLPKG).addData(rubbishSize);

                    mInstallPKGRubbishSum += rubbishSize;
                }
                break;
            case RubbishType.INDEX_SOFTWARE_CACHE:
                if(aRubbish.getPackageName()!=null) {
                    Log.i(TAG, "RubbishType.INDEX_SOFTWARE_CACHE");
                    try {
                        drawable = packageManager.getApplicationIcon(aRubbish.getPackageName());
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    childObj = new ChildObj(drawable, aRubbish.getAppName(), rubbishSize, true);
                    if (!listChildSftCache.contains(childObj)) {
                        listChildSftCache.add(childObj);
                    } else {
                        index = listChildSftCache.indexOf(childObj);
                        childObj = listChildSftCache.get(index);
                        childObj.addData(rubbishSize);
                    }
                    listParentData.get(EXPANDABLE_INDEX_SFTCACHE).addData(rubbishSize);

                    mSoftCacheRubbishSum += rubbishSize;
                }
                break;
            case RubbishType.INDEX_SOFT_RUNTIMG_RUBBISH:
            case RubbishType.INDEX_UNINSTALL_RETAIL:
                Log.i(TAG,"RubbishType.INDEX_SOFT_RUNTIMG_RUBBISH && RubbishType.INDEX_UNINSTALL_RETAIL");
                drawable = null;//ContextCompat.getDrawable(V2GarbageCleanActivity.this, R.mipmap.ic_launcher);
                childObj = new ChildObj(drawable,aRubbish.getDescription(),rubbishSize,true);
                if(!listChildGarbageFile.contains(childObj)) {
                    listChildGarbageFile.add(childObj);
                }else{
                    index = listChildGarbageFile.indexOf(childObj);
                    childObj = listChildGarbageFile.get(index);
                    childObj.addData(rubbishSize);
                }
                listParentData.get(EXPANDABLE_INDEX_GARBAGEFILE).addData(rubbishSize);

                mGarbageFileRubbishSum += rubbishSize;
                break;
            default:
                break;
        }
    }
    //Init Expandable ListView Data  end

    //chkbox status Modifier start
    private boolean ChkboxStatusInit(List<RubbishEntity> rubbish){
        for(RubbishEntity aRubbish:rubbish) {
            switch (aRubbish.getRubbishType()) {
                case RubbishType.INDEX_APK:
                    ChildListChkboxStatusInit(aRubbish, aRubbish.getAppName(), listChildInstallPkg);
                    break;
                case RubbishType.INDEX_SOFTWARE_CACHE:
                    ChildListChkboxStatusInit(aRubbish, aRubbish.getAppName(), listChildSftCache);
                    break;
                case RubbishType.INDEX_SOFT_RUNTIMG_RUBBISH:
                case RubbishType.INDEX_UNINSTALL_RETAIL:
                    ChildListChkboxStatusInit(aRubbish, aRubbish.getDescription(), listChildGarbageFile);
                    break;
                default:
                    break;
            }
        }
        return true;
    }
    private void ChildListChkboxStatusInit(RubbishEntity aRubbish, String title,ArrayList<ChildObj> arraylist) {
        for(ChildObj childObj:arraylist){
            if(childObj.getTitle() == title){
                if(childObj.isChecked()){
                    aRubbish.setStatus(RubbishType.MODEL_TYPE_SELECTED);
                }else {
                    aRubbish.setStatus(RubbishType.MODEL_TYPE_UNSELECTED);
                }
                break;
            }
        }
    }
    //chkbox status Modifier  end

    private WaterWaveView mWaterWaveProgress;
    //Init UI start
    private void InitView() {
        //Layout Init
        setContentView(R.layout.v2_garbage_cleanup_main);

        tx_scan_ongoing_2 = (TextView) findViewById(R.id.scan_ongoing_2);
        tx_unit_2 = (TextView) findViewById(R.id.unit_2);
        btn_scan_total = (TextView) findViewById(R.id.scan_total);
        mCompleteBtn = (Button)findViewById(R.id.complete_button);

        mGarbageFileSize = (TextView) findViewById(R.id.tv_garbage_file_size);
        mInstallPKGSize = (TextView) findViewById(R.id.tv_install_pkg_size);
        mSoftCacheSize = (TextView) findViewById(R.id.tv_sft_cache_size);

        //ExpanableListView init
        bExpandableListExpand = false;
        ParentDataInit();
        expandableListView = (ExpandableListView) findViewById(R.id.expendlist);
        mgarbageCLeanExpandableListViewAdapter = new GarbageCLeanExpandableListViewAdapter(this, listParentData);
        expandableListView.setAdapter(mgarbageCLeanExpandableListViewAdapter);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return !bExpandableListExpand;
            }
        });
        
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                CheckBox chkbox = (CheckBox) v.findViewById(R.id.chkbox);

                if(chkbox != null && mgarbageCLeanExpandableListViewAdapter.getmChildChkBoxStates(groupPosition) != null) {
                    boolean isChecked = mgarbageCLeanExpandableListViewAdapter.getmChildChkBoxStates(groupPosition).get(childPosition);
                    chkbox.setChecked(!isChecked);
                    mgarbageCLeanExpandableListViewAdapter.getmChildChkBoxStates(groupPosition).set(childPosition, !isChecked);
                    listParentData.get(groupPosition).getChilds().get(childPosition).setIschecked(!isChecked);

                    long pd = listParentData.get(groupPosition).getSelectedDataSize();
                    long cd = listParentData.get(groupPosition).getChilds().get(childPosition).getDataSize();

                    if (!isChecked) {
                        Log.d(TAG, "ischecked:pd=" + pd + " | cd=" + cd);
                        listParentData.get(groupPosition).setSelectedDataSize(pd + cd);
                    } else {
                        if (pd - cd >= 0L) {
                            Log.d(TAG, "ischecked:pd=" + pd + " | cd=" + cd);
                            listParentData.get(groupPosition).setSelectedDataSize(pd - cd);
                            Log.d(TAG, "mDeleteViewList:del:pos{" + groupPosition + "," + childPosition + "}");
                        }

                    }
                    mgarbageCLeanExpandableListViewAdapter.notifyDataSetChanged();

                    Log.i("wanghg", "setOnChildClickListener");
                    Message msg = mUIHandler.obtainMessage();
                    msg.what = UIHandlerMsg.MSG_UPDATE_TEXT;
                    mUIHandler.sendMessage(msg);
                }

                return true;
            }
        });

        //set expandableListView,from left to right
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        expandableListView.setIndicatorBounds(width - 60, width - 30);
        expandableListView.setGroupIndicator(null);

        mCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAKeyClean();
            }
        });

        mScanningLayout = (RelativeLayout) findViewById(R.id.scanning_layout);
        mScanningLayout.setVisibility(View.VISIBLE);

        mScanFinishLayout = (RelativeLayout) findViewById(R.id.scan_finish_layout);
        mScanFinishLayout.setVisibility(View.GONE);

        mWaterWaveProgress = (WaterWaveView) findViewById(R.id.water_wave);
        mWaterWaveProgress.setProgress(0.01F);
        mWaterWaveProgress.startWave();
        mWaterWaveProgress.setScanOngoing(getString(R.string.zero_value));
        Log.i("wanghg", "mWaterWaveProgress.startWave()");
    }

    private void doAKeyClean() {
        if(listChildGarbageFile == null) {return;}
        mCompleteBtn.setClickable(false);
        RubbishEntityManager rubbishManager = mDeepcleanManager.getmRubbishEntityManager();
        List<RubbishEntity> _rubbish =  rubbishManager.getRubbishes();
        ChkboxStatusInit(_rubbish);
        mDeepcleanManager.startClean();
    }

    private void InitActionBar() {
        mActionBarButton = (ImageButton) findViewById(R.id.actionbar_icon);
        mLayoutActionbar = (LinearLayout) findViewById(R.id.layout_actionbar);
        mActionBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            mActionBarButton.setBackgroundResource(outValue.resourceId);
            //noinspection deprecation
            getWindow().setStatusBarColor(getResources().getColor(R.color.green));
        } else {
            mActionBarButton.setBackgroundResource(R.drawable.actionbar_btn_on_selector);
        }
        //noinspection deprecation
        mLayoutActionbar.setBackgroundColor(getResources().getColor(R.color.green));
    }
    //Init UI end

    @Override
    public void onBackPressed() {
        if(mScanningLayout.getVisibility() == View.VISIBLE) {
            mDeepcleanManager.cancelScan();

            mScanningLayout = (RelativeLayout) findViewById(R.id.scanning_layout);
            mScanningLayout.setVisibility(View.GONE);
            mWaterWaveProgress.stopWave();

            mScanFinishLayout = (RelativeLayout) findViewById(R.id.scan_finish_layout);
            mScanFinishLayout.setVisibility(View.VISIBLE);

            return;
        }

        setResult(RESULT_OK);
        super.onBackPressed();
    }
}