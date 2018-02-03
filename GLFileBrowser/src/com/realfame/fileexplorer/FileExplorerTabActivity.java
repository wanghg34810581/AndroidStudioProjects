package com.realfame.fileexplorer;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;

import java.util.ArrayList;

/*TYRD: weina 20150624 add begin*/
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.util.TypedValue;
import android.content.Intent;

/*TYRD: weina 20150624 add emd*/
import com.umeng.analytics.MobclickAgent;//TYRD: weina 20160414 add for umeng andlytics
import java.util.ArrayList;
import android.view.Window;
public class FileExplorerTabActivity extends Activity {
    private static final String INSTANCESTATE_TAB = "tab";
    private static final int DEFAULT_OFFSCREEN_PAGES = 2;
    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pager);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGES);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        
        mTabsAdapter = new TabsAdapter(this, mViewPager);
		/*TYRD:weina 20150624 modify begin*/
        mTabsAdapter.addTab(setTabCustomView(R.string.tab_category,bar.newTab()),//bar.newTab().setText(R.string.tab_category),
                FileCategoryActivity.class, null);
        mTabsAdapter.addTab(setTabCustomView(R.string.tab_sd,bar.newTab()),//bar.newTab().setText(R.string.tab_sd),
                FileViewActivity.class, null);
        mTabsAdapter.addTab(setTabCustomView(R.string.tab_remote,bar.newTab()),//bar.newTab().setText(R.string.tab_remote),
                ServerControlActivity.class, null);
		/*TYRD: weina 20150624 modify end*/
		/*TYRD: weina 2015031 add for PROD104002390 BEGIN*/
		Intent intent = getIntent();
		int tabIndex =intent.getIntExtra(GlobalConsts.INTENT_EXTRA_TAB,0);
		if(tabIndex==2){
			bar.setSelectedNavigationItem(tabIndex);
		}/*TYRD: weina 20150923 add for PROD104042909 begin*/
		else if("com.android.fileexplorer.action.FILE_SINGLE_SEL".equals(intent.getAction())){
			bar.setSelectedNavigationItem(1);
		}
		/*TYRD: weina 20150923 add PROD104042909 end */
		else{
            bar.setSelectedNavigationItem(PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(INSTANCESTATE_TAB, Util.CATEGORY_TAB_INDEX));
		}
		
        //bar.setSelectedNavigationItem(PreferenceManager.getDefaultSharedPreferences(this)
        //        .getInt(INSTANCESTATE_TAB, Util.CATEGORY_TAB_INDEX));
        /*TYRD: weina 2015031 add for PROD104002390  END*/
		/*TYRD: weina 20150624 add begin*/
        for(int i=0; i<bar.getTabCount(); i++){
            TextView tv =(TextView)bar.getTabAt(i).getCustomView();
            
            if (bar.getSelectedNavigationIndex()== i) {
                tv.setTextColor(0XFFffffff);
            }else{
                tv.setTextColor(0XFF888d93);
            }
        }
    }
    /*TYRD: weina 20150624 add begin*/
    public ActionBar.Tab setTabCustomView(int strId, ActionBar.Tab tab ){
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT);
		lp.gravity = Gravity.CENTER;
		TextView tabView = new TextView(this);
		tabView.setText(strId);
		tabView.setLayoutParams(lp);
		tabView.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
		//tabView.setTextColor(this.getResources().getColor(R.color.ty_material_tab_text_material_dark));
		if(strId==R.string.tab_category){
			tabView.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
		}else if(strId==R.string.tab_sd){
		    tabView.setGravity(Gravity.CENTER);
		}else if(strId==R.string.tab_remote){
		    tabView.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
		}
		
		return tab.setCustomView(tabView);
    }
    /*TYRD: weina 20150624 add end*/
	/*TYRD:weina 20160414 add for umeng analytics begin*/
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
     /*TYRD:weina 20160414 add for umeng analytics end*/
    @Override
    public boolean onMenuOpened(int featureId,Menu menu){
    	super.onMenuOpened(featureId, menu);
    	if((mViewPager.getCurrentItem()==Util.SDCARD_TAB_INDEX) && (menu!=null))
		((FileViewActivity)mTabsAdapter.getItem(mViewPager.getCurrentItem())).onMenuOpened(menu);
    	return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
		/*TYRD: weina 20150624 deleted begin*/
        /*SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(INSTANCESTATE_TAB, getActionBar().getSelectedNavigationIndex());
        editor.commit();*/
		/*TYRD: weina 20150624 deleted end */
		MobclickAgent.onPause(this); //TYRD:weina 20160414 add for umeng analytics
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (getActionBar().getSelectedNavigationIndex() == Util.CATEGORY_TAB_INDEX) {
            FileCategoryActivity categoryFragement =(FileCategoryActivity) mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX);
            if (categoryFragement.isHomePage()) {
                reInstantiateCategoryTab();
            } else {
                categoryFragement.setConfigurationChanged(true);
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    public void reInstantiateCategoryTab() {
        mTabsAdapter.destroyItem(mViewPager, Util.CATEGORY_TAB_INDEX,
                mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX));
        mTabsAdapter.instantiateItem(mViewPager, Util.CATEGORY_TAB_INDEX);
    }

    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = (IBackPressedListener) mTabsAdapter
                .getItem(mViewPager.getCurrentItem());
        if (!backPressedListener.onBack()) {
            super.onBackPressed();
        }
    }

    public interface IBackPressedListener {
        /**
         * 处理back事件。
         * @return True: 表示已经处理; False: 没有处理，让基类处理。
         */
        boolean onBack();
    }

    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public Fragment getFragment(int tabIndex) {
        return mTabsAdapter.getItem(tabIndex);
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mActionBar = activity.getActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            if (info.fragment == null) {
                info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            }
            return info.fragment;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
            if (position == 0){
                FileCategoryActivity f = (FileCategoryActivity) getItem(0);
                if (f != null) {
                    //f.refreshFragment();// TYRD: weina 20150624 deleted
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
			/*TYRD: weina 20150624 add begin*/
            for(int i=0; i<mActionBar.getTabCount(); i++){
                TextView tv =(TextView)mActionBar.getTabAt(i).getCustomView();
                if (mActionBar.getTabAt(i).getTag()== tag) {
                    tv.setTextColor(0XFFffffff);
                }else{
                    tv.setTextColor(0XFF888d93);
                }
            }
			/*TYRD: weina 20150624 add end*/
            for (int i=0; i<mTabs.size(); i++) {
				
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
					Fragment f = getItem(i);
					/*TYRD: weina 20150820 deleted for PROD103990473 BEGIN*/
					/*if(f instanceof FileViewActivity){
						if(((FileViewActivity)f).isConfirmButtonBarVisible()){
							((FileViewActivity)f).setConfirmButtonBarInvisible();
						}
					}*/
					/*TYRD: weina 20150820 deleted for PROD103990473 END*/
					
                }
            }
           // if(!tab.getText().equals(mContext.getString(R.string.tab_sd))) { //TYRD: weina 20150624 deleted
                ActionMode actionMode = ((FileExplorerTabActivity) mContext).getActionMode();
                if (actionMode != null) {
                    actionMode.finish();
					
                }
            //}//TYRD: weina 20150624 deleted
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }
}
