package com.guli.secmanager;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Build;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.pm.PackageManager;

import com.guli.secmanager.Checkup.V2GarbageCleanCheckupFragment;
import com.guli.secmanager.Checkup.V2VirusScanCheckupFragment;
import com.guli.secmanager.Checkup.V2FlowMonitorCheckupFragment;
import com.guli.secmanager.UpdateCenter.UpdateService;
import com.guli.secmanager.Utils.ShareUtil;
import com.guli.secmanager.flowmonitor.FlowAutoCorrectService;
import com.guli.secmanager.flowmonitor.FlowMonitorService;
import com.guli.secmanager.flowmonitor.FlowSettingActivity;
import com.guli.secmanager.widget.viewpagerindicator.TabPageIndicator;
import java.util.ArrayList;
import java.util.List;

public class V2MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";

    private RelativeLayout mActionBar;
    private ImageButton mActionbarSettings;
    private TabPageIndicator mIndicator;
    private ViewPager mViewPager;
    private List<Fragment> mFragmentList;

    private long mExitTime;

    private static final int REQUEST_CODE = 0; // 请求码
    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private PermissionsChecker mPermissionsChecker; // 权限检测器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            //        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            //        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.green));
            //window.setNavigationBarColor(getResources().getColor(R.color.green));
        }
        setContentView(R.layout.v2_activity_fragment);

        mActionBar = (RelativeLayout) findViewById(R.id.layout_actionbar);
        mActionbarSettings = (ImageButton) findViewById(R.id.actionbar_settings);
        mActionbarSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(V2MainActivity.this, FlowSettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
            }
        });
        mIndicator = (TabPageIndicator)findViewById(R.id.indicator);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mFragmentList = new ArrayList<Fragment>();
        mFragmentList.add(new V2GarbageCleanCheckupFragment());
        mFragmentList.add(new V2VirusScanCheckupFragment());
        mFragmentList.add(new V2FlowMonitorCheckupFragment());

        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mFragmentList.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public CharSequence getPageTitle(final int position) {
                switch (position) {
                    case 0:
                        return getResources().getString(R.string.tab_garbage_clean);
                    case 1:
                        return getResources().getString(R.string.tab_virus_scan);
                    case 2:
                        return getResources().getString(R.string.tab_flow_monitor);
                    default:
                        return null;
                }
            }
        };

        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                switch (arg0) {
                    case 0:
                        setBackgroundColor(V2GarbageCleanCheckupFragment.mStatusColor, arg0);
                        mActionbarSettings.setVisibility(View.GONE);
                        break;
                    case 1:
                        setBackgroundColor(V2VirusScanCheckupFragment.mStatusColor, arg0);
                        mActionbarSettings.setVisibility(View.GONE);
                        break;
                    case 2:
                        setBackgroundColor(V2FlowMonitorCheckupFragment.mStatusColor, arg0);
                        mActionbarSettings.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mIndicator.setViewPager(mViewPager);

        SharedPreferences sPreferences = getSharedPreferences(ShareUtil.DATABASE_NAME, Context.MODE_PRIVATE);
        if(sPreferences.getBoolean(ShareUtil.FIRST_OPEN, true)) {
            Intent intent = new Intent(this, UpdateService.class);
            startService(intent);
        }

        // start updateService
        if (!UpdateService.isServiceAlarmOn(this)) {
            Log.i(TAG, "--->>> Start UpdateService Timer!");
            UpdateService.setServiceAlarm(this, true);
        }

        if(!FlowAutoCorrectService.isServiceAlarmOn(this)){
            FlowAutoCorrectService.setAlarm(this, true);
        }

        startService(new Intent(this, FlowMonitorService.class));

        mPermissionsChecker = new PermissionsChecker(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        }
    }

    public void setBackgroundColor(int color, int item) {
        if(mViewPager.getCurrentItem() != item) {
            Log.i("wanghg", "item : " + mViewPager.getCurrentItem() + "   ,   " + item);
            return;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(color));
        }

        if(mActionBar != null) {
            mActionBar.setBackgroundDrawable(getResources().getDrawable(color));
        }

        mIndicator.setBackgroundColor(getResources().getColor(color));
    }

    private boolean popAllBackStackItem() {
        int stackSize = getFragmentManager().getBackStackEntryCount();
        if (stackSize == 0) {
            return false;
        }
        for (int i = 0; i < stackSize; i++) {
            getFragmentManager().popBackStack();
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown Enter... ");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (popAllBackStackItem()) {
                return true;
            }
            Log.i(TAG, "--->>> mExitTime = " + mExitTime);
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Log.i(TAG, "--->>> mExitTime = " + mExitTime);
                Toast.makeText(this, getString(R.string.exit_prompt), Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
                /*FragmentManager fm = getFragmentManager();
                Fragment fragment = fm.findFragmentByTag(CheckupFragment.TAG);

                if (fragment != null) {
                    CheckupFragment cf = (CheckupFragment) fragment;
                    cf.stopScanTask();
                }*/
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
