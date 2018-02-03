package com.guli.secmanager.VirusScan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guli.secmanager.R;
import com.guli.secmanager.Utils.ApkInfoUtil;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.V2FinishActivity;
import com.guli.secmanager.widget.RadarView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.qscanner.QScanListenerV2;
import tmsdk.fg.module.qscanner.QScannerManagerV2;

//import android.widget.Toast;
/**
 * Created by shenyan on 2016/4/18.
 */
public class V2VirusScanActivity extends AppCompatActivity {

    public static final String TAG = "V2VirusScanActivity";

    private QScannerManagerV2 mQScannerMananger;
    private Thread mScanThread;
    private boolean isContinue;

    private VirusScanAdapter mGroupAdapter = null;
    private ExpandableListView mVirusScanListView = null;
    private TextView mScanRecTitleView;
    private TextView mScanPercentView;
    private Button mCompleteButtonView;
    private RelativeLayout mCompleteButtonLayout;
    private LinearLayout mLayoutInfo, mLayoutActionbar;
    private ImageButton mActionBarButton;
    private RelativeLayout mScanningLayout;
    private RelativeLayout mScanFinishLayout;
    private TextView mSystemBugCount;
    private TextView mSoftInstalledCount;
    private TextView mSoftUninstalledCount;

    private int mGroupPosition = 0;
    private int mChildPosition = 0;

    private ArrayList<ParentData> mListData = new ArrayList<ParentData>();
    private ArrayList<QScanResultEntity> mIntentArrayList = new ArrayList<QScanResultEntity>();

    private boolean mGetIntentFlag;

    private int mGetIntentScanCount;
    private int mScanType = Utils.BUG_SCAN;

    private RadarView mRadarProgress;

    private UninstallPackageReceiver mUninstallReceiver;

    private class UninstallPackageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                Log.d(TAG,"UninstallPackageReceivernyan onReceive packageName==" + packageName);

                int index = -1;
                for (int i = 0; i < mListData.get(mScanType).getParMulwareCount(); i++) {
                    if(packageName.equals(mListData.get(mScanType).getParPackageNameList().get(i))) {
                        index = i;
                        Log.d(TAG, "packageName index int==" + index);
                        break;
                    }
                }
                if(index != -1) {
                    mListData.get(mScanType).removeParResultList(index);
                    mListData.get(mScanType).removeParAdviceList(index);
                    mListData.get(mScanType).removeParPackageList(index);
                    mListData.get(mScanType).removeParPathList(index);
                    mListData.get(mScanType).removeParChilds(index);
                    mListData.get(mScanType).setParMulwareCount(mListData.get(mScanType).getParMulwareCount() - 1);
                }
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage" + msg);
            switch (msg.what) {
                case Utils.UPDATE_PROGRESS_VIEW_MSG:
                    //update progressbar
                    Log.v(TAG, "UPDATE_PROGRESS_VIEW_MSG progress == " + msg.arg1);

                    mRadarProgress.setProgress(msg.arg1);

                    int bugCount = mListData.get(mScanType).getParSoftWareCount();
                    if (mScanType == Utils.BUG_SCAN) {
                        mSystemBugCount.setText(bugCount + getString(R.string.item));
                    }
                    else if (mScanType == Utils.SOFT_SCAN) {
                        mSoftInstalledCount.setText(bugCount + getString(R.string.item));
                    }
                    else {
                        mSoftUninstalledCount.setText(bugCount + getString(R.string.item));
                    }
                    break;

                case Utils.SCAN_COMPLETE_ALL_MSG:
                    Log.v(TAG, "SCAN_COMPLETE_ALL_MSG begin ");
                    createAllChildData();
                    int count = saveVirusStatus();
                    mScanPercentView.setText("发现" + count + "个问题");
                    if (count > 0) {
                        updateUiState(false);

                        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
                        filter.addDataScheme("package");
                        mUninstallReceiver = new UninstallPackageReceiver();
                        V2VirusScanActivity.this.registerReceiver(mUninstallReceiver, filter);
                    } else {
                        updateUiState(true);
                    }
                    mGroupAdapter.notifyDataSetChanged();

                    mScanningLayout.setVisibility(View.GONE);
                    mScanFinishLayout.setVisibility(View.VISIBLE);
                    mRadarProgress.stopWave();
                    stopScanVirus();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v2_virus_scan_main);

        Log.v(TAG, "V2VirusScanActivity oncreate() begin");

        mQScannerMananger = ManagerCreatorF.getManager(QScannerManagerV2.class);
        mUninstallReceiver = null;

        mCompleteButtonView = (Button) findViewById(R.id.complete_button);
        mCompleteButtonView.setOnClickListener(mCompleteBtnListener);
        mCompleteButtonLayout = (RelativeLayout) findViewById(R.id.layout_button);

        mScanRecTitleView = (TextView) findViewById(R.id.scan_title_info);
        mLayoutInfo = (LinearLayout) findViewById(R.id.layout_info);
        mScanPercentView = (TextView) findViewById(R.id.scan_result_percentage);
        mVirusScanListView = (ExpandableListView) findViewById(R.id.virus_scan_recover_group_list_view);

        mLayoutActionbar = (LinearLayout) findViewById(R.id.layout_actionbar);
        initActionBar();

        mScanningLayout = (RelativeLayout) findViewById(R.id.scanning_layout);
        mScanningLayout.setVisibility(View.VISIBLE);
        mScanFinishLayout = (RelativeLayout) findViewById(R.id.scan_finish_layout);
        mScanFinishLayout.setVisibility(View.INVISIBLE);

        mSystemBugCount = (TextView) findViewById(R.id.virus_system_bug_count);
        mSoftInstalledCount = (TextView) findViewById(R.id.virus_soft_installed_count);
        mSoftUninstalledCount = (TextView) findViewById(R.id.virus_soft_uninstalled_count);

        mRadarProgress = (RadarView) findViewById(R.id.radar);
        mRadarProgress.setProgress(0);
        mRadarProgress.startWave();

        getScanVirusResultIntent();
        if (mGetIntentFlag) {
            mLayoutActionbar.setBackgroundColor(getResources().getColor(R.color.orange));
            mLayoutInfo.setBackgroundColor(getResources().getColor(R.color.orange));
            iniScanVirusIntentView();
            initGroupListView();
        } else {
            setStatusBarColor(true);
            mLayoutActionbar.setBackgroundColor(getResources().getColor(R.color.green));
            mLayoutInfo.setBackgroundColor(getResources().getColor(R.color.green));
            initGroupListView();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mQScannerMananger.initScanner() == 0) {
                        Log.v(TAG, "initScanner return true");
                    } else {
                        Log.v(TAG, "initScanner return false");
                    }
                    Log.v(TAG, "virus database version:" + mQScannerMananger.getVirusBaseVersion(V2VirusScanActivity.this));
                    startScanVirus();
                }
            }, 200);
        }

        Log.v(TAG, "V2VirusScanActivity oncreate() end");
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart() begin.");
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() begin.");

        stopScanVirus();
        mQScannerMananger.freeScanner();
        mListData.removeAll(mListData);
        //mListData = null;
        mMyQScanListener = null;

        if(mUninstallReceiver != null) {
            this.unregisterReceiver(mUninstallReceiver);
            mUninstallReceiver = null;
        }
        mRadarProgress.stopWave();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;

        super.onDestroy();
        Log.d(TAG, "onDestroy() end.");
    }

    @Override
    public void onBackPressed() {
        Log.i("wanghg", "onBackPressed");
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private int saveVirusStatus() {
        SharedPreferences sPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPreferences.edit();
        int count = 0;
        int cnt = 1;
        for (int i = 0; i < Utils.PARENT_MAX; i++) {
            count += mListData.get(i).getParMulwareCount();
            Log.i("wanghg", "saveVirusStatus  mListData.get(i).getParMulwareCount() : " + mListData.get(i).getParMulwareCount());
            if(count > 0) {
                for(int j = 0; j < mListData.get(i).getParMulwareCount(); j++) {
                    String curPathName = mListData.get(i).getParPathNameList().get(j);
                    Log.i("wanghg", "saveVirusStatus  curPathName : " + curPathName);
                    editor.putString(ShareUtil.VIRUS_PACKAGE_NAME + cnt, curPathName);
                    cnt++;
                }
            }
        }

        editor.putInt(ShareUtil.VIRUS_COUNT, count);
        Log.i("wanghg", "saveVirusStatus  count : " + count);
        editor.commit();

        return count;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i(TAG, "android.R.id.home  : ");
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    View.OnClickListener mCompleteBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            mChildPosition = 0;
            doNext();
        }
    };

    public void doNext() {
        boolean flag = false;

        for (int i = mGroupPosition; i < Utils.PARENT_MAX; i++) {
            mGroupPosition = i;
            int bugCount = mListData.get(i).getParMulwareCount();

            if (bugCount <= 0) {
                continue;
            }
            else if (mChildPosition >= bugCount) {
                mChildPosition = 0;
                continue;
            }
            else {
                if(mListData.get(i).getParChilds().get(mChildPosition).isChecked() == true) {
                    flag = true;
                    break;
                }
                else {
                    mChildPosition++;
                    doNext();
                    return;
                }
            }
        }

        if(flag == false) {
            if(saveVirusStatus() > 0) {
                setResult(RESULT_OK);
                finish();
            }
            else {
                Intent intent = new Intent();
                intent.setClass(V2VirusScanActivity.this, V2FinishActivity.class);
                intent.putExtra("from", "V2VirusScanActivity");
                startActivityForResult(intent, 400);
            }
            return;
        }

        Log.w(TAG,"GroupPos = "+mGroupPosition+" | ChildPos"+mChildPosition);
        if (mGroupPosition == Utils.SOFT_SCAN) {
            mScanType = Utils.SOFT_SCAN;
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DELETE);
            String curPackageName = mListData.get(mGroupPosition).getParPackageNameList().get(mChildPosition);
            String packageName = "package:" + curPackageName;
            intent.setData(Uri.parse(packageName));

            Bundle b = new Bundle();
            b.putString("from", "V2VirusScanActivity");
            intent.putExtras(b);

            startActivityForResult(intent, 1000);
        } else if (mGroupPosition == Utils.SDCARD_SCAN) {
            mScanType = Utils.SDCARD_SCAN;
            final String curPathName = mListData.get(mGroupPosition).getParPathNameList().get(mChildPosition);
            Drawable drawable = ApkInfoUtil.getApkIcon(V2VirusScanActivity.this,curPathName);
            String displayName = ApkInfoUtil.getAPKlabel(V2VirusScanActivity.this, curPathName);
            Log.e(TAG,"displayName="+displayName);

            new AlertDialog.Builder(V2VirusScanActivity.this).setTitle(displayName)//设置对话框标题
                    .setMessage("要删除此应用吗？")//设置显示的内容
                    .setIcon(drawable)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                            File file = new File(curPathName);
                            if (file.exists()) { // 判断文件是否存在
                                if (file.isFile()) { // 判断是否是文件
                                    file.delete();
                                }
                            }

                            Log.d(TAG, "pathName==" + curPathName);
                            int index = -1;
                            for (int i = 0; i < mListData.get(mScanType).getParMulwareCount(); i++) {
                                if(curPathName.equals(mListData.get(mScanType).getParPathNameList().get(i))) {
                                    index = i;
                                    Log.d(TAG, "pathName index ==" + index);
                                    break;
                                }
                            }
                            if(index != -1) {
                                mListData.get(mScanType).removeParResultList(index);
                                mListData.get(mScanType).removeParAdviceList(index);
                                mListData.get(mScanType).removeParPackageList(index);
                                mListData.get(mScanType).removeParPathList(index);
                                mListData.get(mScanType).removeParChilds(index);
                                mListData.get(mScanType).setParMulwareCount(mListData.get(mScanType).getParMulwareCount() - 1);
                            }
                            mChildPosition++;
                            doNext();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加返回按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//响应事件
                            dialog.dismiss();
                            dialog.cancel();
                            mChildPosition++;
                            doNext();
                        }
                    }).show();//在按键响应事件中显示此对话框
        }
    }

    private void initActionBar() {
        mActionBarButton = (ImageButton) findViewById(R.id.actionbar_icon);
        mActionBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {;
                setResult(RESULT_OK);
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            mActionBarButton.setBackgroundResource(outValue.resourceId);
        }else{
            mActionBarButton.setBackgroundResource(R.drawable.actionbar_btn_on_selector);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 400:
                SharedPreferences sPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sPreferences.edit();
                editor.putInt(ShareUtil.VIRUS_COUNT, 0);
                editor.commit();

                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putBoolean("fromFinish", true);
                intent.putExtras(b);

                setResult(RESULT_OK, intent);
                finish();
                break;
            case 1000:
                mChildPosition++;
                doNext();
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initGroupListView() {
        mGroupAdapter = new VirusScanAdapter(mListData, this, mGetIntentFlag);
        mVirusScanListView.setAdapter(mGroupAdapter);
        mVirusScanListView.setGroupIndicator(null);
        mVirusScanListView.setClickable(false);

        final int groupCount = mVirusScanListView.getCount();
        for (int i = 0; i < groupCount; i++) {
            mVirusScanListView.expandGroup(i);
        }
        mVirusScanListView.setOnGroupClickListener(
                new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v,
                                                int groupPosition, long id) {
                        // TODO Auto-generated method stub
                        return true;
                    }
                }
        );

        mVirusScanListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                CheckBox chkbox = (CheckBox) v.findViewById(R.id.chkbox);
                boolean isChecked = mGroupAdapter.getmChildChkBoxStates(groupPosition).get(childPosition);

                chkbox.setChecked(!isChecked);
                return true;
            }
        });
    }

    private void getScanVirusResultIntent() {
        mGetIntentFlag = false;
        createGroupData();
    }

    public void iniScanVirusIntentView() {
        if (mIntentArrayList.size() > 0) {
            createGroupData();
            mListData.get(Utils.SOFT_SCAN).setParMulwareCount(mIntentArrayList.size());

            ArrayList mulwareList = new ArrayList();
            ArrayList mulwareAdviceList = new ArrayList();
            for (QScanResultEntity entity : mIntentArrayList) {
                mulwareList.add(entity.softName);
                mulwareAdviceList.add(getString(R.string.advice_install_soft_delete));
            }
            mListData.get(Utils.SOFT_SCAN).setParResultList(mulwareList);
            mListData.get(Utils.SOFT_SCAN).setParAdviceList(mulwareAdviceList);

            if (mListData.get(Utils.SOFT_SCAN).getParMulwareCount() != 0) {
                updateUiState(false);
                createAllChildData();
                mListData.get(Utils.SOFT_SCAN).setParResult(String.valueOf(mGetIntentScanCount)
                        + getString(R.string.list_scan_item_count));

                for (int i = 0; i < Utils.PARENT_MAX; i++) {
                    mListData.get(i).setParTitleColor(true);
                    if(i == Utils.SOFT_SCAN){
                        mListData.get(i).setParIcon(Utils.WARING_ICON);
                    }else{
                        mListData.get(i).setParIcon(Utils.CHECKED_ICON);
                    }
                }
            }
        }
    }

    /**
     * @param flag 根据实际情况，黄色警告就是true，绿色正常状态就是false
     */
    private void setStatusBarColor(boolean flag) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int mStatusColor;
            if(flag) {
                mStatusColor = this.getResources().getColor(R.color.green);
            } else {
                mStatusColor = this.getResources().getColor(R.color.orange);
            }
            this.getWindow().setStatusBarColor(mStatusColor);
        }
    }

    /**
     * 发现危险与安全隐患时更新UI的状态
     */
    private void updateUiState(boolean isSafe) {
        if(isSafe) {
            mLayoutActionbar.setBackgroundColor(getResources().getColor(R.color.green));
            mLayoutInfo.setBackgroundColor(getResources().getColor(R.color.green));
            setStatusBarColor(true);
            updateScanButtonState(isSafe);
            mScanRecTitleView.setText(getString(R.string.result_big_safe_info));
        }
        else {
            mLayoutActionbar.setBackgroundColor(getResources().getColor(R.color.orange));
            mLayoutInfo.setBackgroundColor(getResources().getColor(R.color.orange));
            setStatusBarColor(false);
            updateScanButtonState(isSafe);
            mScanRecTitleView.setText(getString(R.string.result_big_danger_info));
        }
    }

    /**
     * 更新浏览按键的状态
     */
    private void updateScanButtonState(boolean isSafe) {
        updateCompleteButtonBg(isSafe);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if(isSafe) {
                mActionBarButton.setBackgroundResource(R.drawable.actionbar_btn_on_selector);
            }
            else {
                mActionBarButton.setBackgroundResource(R.drawable.actionbar_yellow_btn_on_selector);
            }
        }
    }

    /**
     * 更新完成按键的背景效果
     */
    private void updateCompleteButtonBg(boolean isSafe) {
        if(isSafe) {
            mCompleteButtonLayout.setBackgroundColor(getResources().getColor(R.color.green));
            mCompleteButtonView.setText(getString(R.string.optimize_complete));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                TypedValue outValue = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                mCompleteButtonView.setBackgroundResource(outValue.resourceId);
            } else {
                mCompleteButtonView.setBackgroundColor(getResources().getColor(R.color.green));
                mCompleteButtonView.setBackgroundResource(R.drawable.actionbar_btn_on_selector);
            }
        }
        else {
            mCompleteButtonLayout.setBackgroundColor(getResources().getColor(R.color.orange));
            mCompleteButtonView.setText(getString(R.string.all_repair));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                TypedValue outValue = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                mCompleteButtonView.setBackgroundResource(outValue.resourceId);
            } else {
                mCompleteButtonView.setBackgroundColor(getResources().getColor(R.color.orange));
                mCompleteButtonView.setBackgroundResource(R.drawable.actionbar_yellow_btn_on_selector);
            }
        }

    }

    /**
     * 初始化父类ExpandableListView的数据
     */
    private void createGroupData() {
        for(int i = 0; i < Utils.PARENT_MAX; i++){
            ParentData p = new ParentData();
            if(i == Utils.BUG_SCAN){
                p.setParName(getString(R.string.list_system_bug_title));
            }else if(i == Utils.SOFT_SCAN){
                p.setParName(getString(R.string.list_soft_installed_title));
            }else if(i == Utils.SDCARD_SCAN){
                p.setParName(getString(R.string.list_sdcard_soft_title));
            }
            p.setParIcon(Utils.UNCHECKED_ICON);
            p.setParTitleColor(false);
            p.setChildSize(0);
            mListData.add(p);
        }
    }

    private void createChildData(int scanType) {
        int childSize = mListData.get(scanType).getParMulwareCount();
        mListData.get(scanType).setChildSize(childSize);

        if(childSize > 0) {
            ArrayList<ChildData> cLists = new ArrayList<ChildData>();
            for (int i = 0; i < childSize; i++) {
                ChildData cObj = new ChildData();
                cObj.setChiName(mListData.get(scanType).getParResultList().get(i));
                if(mListData.get(scanType).getParAdviceList().size() > 0) {
                    cObj.setChiResult(mListData.get(scanType).getParAdviceList().get(i));
                }
                cObj.setIschecked(true);
                cLists.add(cObj);
            }
            mListData.get(scanType).setParChilds(cLists);
        }
    }

    private void createAllChildData(){
        Log.d(TAG, "createAllChildData()");
        for (int i = 0; i < Utils.PARENT_MAX; i++) {
            if(mListData.get(i).getParMulwareCount() > 0){
                int bugCount = mListData.get(i).getParSoftWareCount();
                mListData.get(i).setParResult(String.valueOf(bugCount)
                        + getString(R.string.list_scan_item_count));
                mListData.get(i).setParIcon(Utils.WARING_ICON);
            }else{
                mListData.get(i).setParResult(getString(R.string.result_big_safe_info));
                mListData.get(i).setParIcon(Utils.CHECKED_ICON);
            }
            createChildData(i);
        }
    }

    public void startScanVirus() {
        Log.d(TAG, "startScanVirus() mScanType ==" + mScanType);
        isContinue = true;
        if (mScanThread == null || !mScanThread.isAlive()) {
            mScanThread = new Thread() {
                @Override
                public void run() {
                    for(int i = 0; i <= Utils.SDCARD_SCAN; i++) {
                        if(!isContinue) {break;}

                        mScanType = i;
                        if (mScanType == Utils.BUG_SCAN) {
                            Log.d(TAG, "vs nativeScanSystemFlaws begin");
                            //本地扫描:参数false  云扫描:参数 true
                            //mQScannerMananger.scanInstalledPackages(new MyQScanListener(), false);
                            mQScannerMananger.nativeScanSystemFlaws(mMyQScanListener);
                            Log.d(TAG, "vs nativeScanSystemFlaws end");
                        }
                        else if (mScanType == Utils.SOFT_SCAN) {
                            Log.d(TAG, "vs scanInstalledPackages begin");
                            //本地扫描:参数false  云扫描:参数 true
                            //mQScannerMananger.scanInstalledPackages(new MyQScanListener(), false);
                            mQScannerMananger.scanInstalledPackages(mMyQScanListener, true);
                            Log.d(TAG, "vs scanInstalledPackages end");
                        }
                        else if(mScanType == Utils.SDCARD_SCAN) {
                            Log.d(TAG, "scanUninstalledApks begin");
                            mQScannerMananger.scanUninstalledApks(mMyQScanListener, true);
                            Log.d(TAG, "scanUninstalledApks end");
                        }
                    }
                }
            };
            mScanThread.start();
        }
    }

    public void stopScanVirus(){
        Log.d(TAG, "stopScanVirus begin");
        isContinue = false;
        if (mScanThread != null && mScanThread.isAlive()) {
            Log.d(TAG, "stopScanVirus cancelScan()");
            mQScannerMananger.cancelScan();
            try {
                mScanThread.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            mScanThread = null;
        }
    }

    public void continueScanVirus() {
        Log.v(TAG, "continueScanVirus begin");
        if (mScanThread != null && mScanThread.isAlive()) {
            Log.v(TAG, "pauseScanVirus continueScan()");
            mQScannerMananger.continueScan();
        }
    }

    private MyQScanListener mMyQScanListener = new MyQScanListener();
    private class MyQScanListener extends QScanListenerV2 {

        @Override
        public void onScanStarted(int scanType) {
            android.util.Log.v(TAG, "onScanStarted:[" + scanType + "]");
        }

        /**
         * 安装包扫描进度回调
         *
         * @param scanType 扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
         *                 {@link QScanConstants#SCAN_SPECIALS}
         * @param progress 扫描进度 像未安装apk扫描，progress无法计算，这里会返回-1的值，标识未知
         * @param result   扫描项信息
         */
        @Override
        public void onScanProgress(int scanType, int progress,
                                   QScanResultEntity result) {
            Log.v(TAG, "onScanProgress:" + progress);
            if(isContinue) {
                updateProgressTip(result, progress);
            }
        }

        /**
         * 搜索到不扫描的文件的回调
         */
        @Override
        public void onFoundElseFile(int scanType, File file) {

            Log.v(TAG, "onFoundElseFile:[" + scanType + "]" + "file==" + file);
        }

        /**
         * 云扫描出现网络错误
         *
         * @param scanType 扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
         *                 {@link QScanConstants#SCAN_SPECIALS}
         * @param errCode  错误码
         */
        @Override
        public void onScanError(int scanType, int errCode) {
            android.util.Log.v(TAG, "onScanError--scanType[" + scanType + "]errCode[" + errCode + "]");

            //updateTip("查杀出错，出错码：" + errCode + " " + "查杀类型-" + getScanTypeString(scanType), 0);
            //mHandle2.sendEmptyMessage(MSG_RESET_PAUSE);
            continueScanVirus();
        }

        /**
         * 扫描被暂停时回调
         */
        @Override
        public void onScanPaused(int scanType) {
            android.util.Log.v(TAG, "onScanPaused--scanType[" + scanType + "]");
        }

        /**
         * 扫描继续时回调
         */
        @Override
        public void onScanContinue(int scanType) {
            android.util.Log.v(TAG, "onScanContinue--scanType[" + scanType + "]");
        }

        /**
         * 扫描被取消时回调
         */
        @Override
        public void onScanCanceled(int scanType) {
            android.util.Log.v(TAG, "onScanCanceled--scanType[" + scanType + "]");
            mScanThread = null;
        }

        /**
         * 扫描结束
         *
         * @param scanType 扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
         *                 {@link QScanConstants#SCAN_SPECIALS}
         * @param results  扫描的所有结果
         */
        @Override
        public void onScanFinished(int scanType, List<QScanResultEntity> results) {
            android.util.Log.v(TAG, "onScanFinished--scanType[" + scanType + "]results.size()[" +
                    results.size() + "]");

            for (QScanResultEntity entity : results) {
                Log.v(TAG, "[onScanFinished]" +
                        "softName[" + entity.softName +
                        "]packageName[" + entity.packageName +
                        "]path[" + entity.path +
                        "]name[" + entity.name + "]");

                Log.v(TAG, "[onScanFinished]" +
                        "discription[" + entity.discription +
                        "]url[" + entity.url);
            }

            if(mHandler == null) {return;}

            if(mScanType == Utils.BUG_SCAN){
                mListData.get(mScanType).setParMulwareCount(0);
            }else{
                mListData.get(mScanType).setParMulwareCount(0);
                //mListData.get(mScanType).setParMulwareCount(results.size());
                if (results != null) {
                    updateMulwareTip(results);
                }
                //updateMulwareTipTest();
            }
            if(mScanType == Utils.SDCARD_SCAN) {
                mRadarProgress.setProgress(100);
                mHandler.sendEmptyMessageDelayed(Utils.SCAN_COMPLETE_ALL_MSG, 1000);
            }
        }
    }

    private void updateMulwareTip(List<QScanResultEntity> results) {
        ArrayList mulwareNameList = new ArrayList();
        ArrayList mulwareAdviceList = new ArrayList();
        ArrayList mulwarePackageNameList = new ArrayList();
        ArrayList mulwarePathNameList = new ArrayList();

        for (QScanResultEntity entity : results) {
            Log.v("wanghg", "on finish  " + entity.path + " type is " + entity.type);
            if(entity.type != QScanConstants.TYPE_RISK
                    || entity.type != QScanConstants.TYPE_VIRUS
                    || entity.type != QScanConstants.TYPE_SYSTEM_FLAW
                    || entity.type != QScanConstants.TYPE_TROJAN
                    || entity.type != QScanConstants.TYPE_NOT_OFFICIAL
                    || entity.type != QScanConstants.TYPE_RISK_PAY
                    || entity.type != QScanConstants.TYPE_RISK_STEALACCOUNT) {
                continue;
            }
            //Log.v("wanghg", "on finish  " + entity.path + " type is " + entity.type);
            //Log.v("wanghg", entity.softName + " discription is " + entity.discription);
            mulwareNameList.add(entity.softName);
            mulwarePackageNameList.add(entity.packageName);
            mulwarePathNameList.add(entity.path);
            //mulwareAdviceList.add(entity.discription);
            mListData.get(mScanType).setParMulwareCount(mListData.get(mScanType).getParMulwareCount() + 1);
            if (mScanType == Utils.SOFT_SCAN) {
                mulwareAdviceList.add(getString(R.string.advice_install_soft_delete));
            }else{
                mulwareAdviceList.add(getString(R.string.advice_sdcard_soft_delete));
            }
        }

        mListData.get(mScanType).setParResultList(mulwareNameList);
        mListData.get(mScanType).setParAdviceList(mulwareAdviceList);
        mListData.get(mScanType).setParPackageNameList(mulwarePackageNameList);
        mListData.get(mScanType).setParPathNameList(mulwarePathNameList);
    }

    private void updateMulwareTipTest() {
        ArrayList mulwareNameList = new ArrayList();
        ArrayList mulwareAdviceList = new ArrayList();
        ArrayList mulwarePackageNameList = new ArrayList();
        ArrayList mulwarePathNameList = new ArrayList();

        mulwareNameList.add("测试软件");
        mulwarePackageNameList.add("测试包");
        mulwarePathNameList.add("测试路径");
        mListData.get(mScanType).setParMulwareCount(mListData.get(mScanType).getParMulwareCount() + 1);
        if (mScanType == Utils.SOFT_SCAN) {
            mulwareAdviceList.add(getString(R.string.advice_install_soft_delete));
        }else{
            mulwareAdviceList.add(getString(R.string.advice_sdcard_soft_delete));
        }

        mListData.get(mScanType).setParResultList(mulwareNameList);
        mListData.get(mScanType).setParAdviceList(mulwareAdviceList);
        mListData.get(mScanType).setParPackageNameList(mulwarePackageNameList);
        mListData.get(mScanType).setParPathNameList(mulwarePathNameList);
    }

    private void updateProgressTip(QScanResultEntity result, int progress) {
        Log.i(TAG, "progressbar == " + progress);
        if(mHandler == null || mListData.size() == 0) {return;}

        String softName = result.softName;

        if (result.softName == null || result.softName.length() == 0) {
            softName = result.path;
        }

        getEntityDes(result);  //software count

        Message msg = mHandler.obtainMessage();
        msg.what = Utils.UPDATE_PROGRESS_VIEW_MSG;
        msg.obj = softName;//.toString();
        if (mScanType == Utils.BUG_SCAN) {
            msg.arg1 = progress * Utils.BUG_SCAN_MAX / 100;
        }else if(mScanType == Utils.SOFT_SCAN) {
            msg.arg1 = Utils.BUG_SCAN_MAX + progress * Utils.SOFT_SCAN_MAX / 100;
        }else if(mScanType == Utils.SDCARD_SCAN) {
            msg.arg1 = Utils.BUG_SCAN_MAX + Utils.SOFT_SCAN_MAX + progress * Utils.SDCARD_SCAN_MAX / 100;
        }
        Log.i(TAG, "progressbar == " + progress + " msg.arg1 == " + msg.arg1);
        Log.i(TAG, "result == " + result);
        msg.sendToTarget();
    }

    //software safe state and bug count
    private String getEntityDes(QScanResultEntity result) {
        StringBuilder content = new StringBuilder();
        String message = result.softName;
        if (message == null || message.length() == 0) {
            message = result.path;
        }
        message = message + "[" + result.discription + "]";

        mListData.get(mScanType).setParSoftWareCount(mListData.get(mScanType).getParSoftWareCount() + 1);
        Log.v("wanghg", result.softName + " type is " + result.type);
        Log.v("wanghg", result.path + " / " + result.softName + " discription is " + result.discription);

        switch (result.type) {
            case QScanConstants.TYPE_OK:
                content.append(getString(R.string.virus_type_normal));
                //mListData.get(mScanType).setParMulwareCount(mListData.get(mScanType).getParMulwareCount() + 1);
                break;

            case QScanConstants.TYPE_RISK:
                Log.v(TAG, result.softName + " is TYPE_RISK ");
                content.append(getString(R.string.virus_type_danger));
                //mListData.get(mScanType).setParMulwareCount(mListData.get(mScanType).getParMulwareCount() + 1);
                break;

            case QScanConstants.TYPE_VIRUS:
                android.util.Log.v(TAG, result.packageName + " is TYPE_VIRUS ");
                content.append(getString(R.string.virus_type_virus));
                //mListData.get(mScanType).setParMulwareCount(mListData.get(mScanType).getParMulwareCount() + 1);
                break;

            case QScanConstants.TYPE_SYSTEM_FLAW:
                android.util.Log.v(TAG, result.packageName + " is TYPE_SYSTEM_FLAW ");
                content.append(getString(R.string.virus_type_sysbug));
                //mListData.get(mScanType).setParMulwareCount(mListData.get(mScanType).getParMulwareCount() + 1);
                break;

            case QScanConstants.TYPE_TROJAN:
                android.util.Log.v(TAG, result.packageName + " is TYPE_TROJAN ");
                content.append(getString(R.string.virus_type_malware));
                //mListData.get(mScanType).setParMulwareCount(mListData.get(mScanType).getParMulwareCount() + 1);
                break;

            case QScanConstants.TYPE_UNKNOWN:
                content.append(getString(R.string.virus_type_unknown));
                break;

            default:
                android.util.Log.v(TAG, result.softName + " is others! ");
                content.append(getString(R.string.virus_type_unknown));
                break;
        }
        return content.toString();
    }
}
