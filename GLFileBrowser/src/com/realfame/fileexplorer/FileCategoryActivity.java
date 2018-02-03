
package com.realfame.fileexplorer;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.realfame.fileexplorer.FavoriteDatabaseHelper.FavoriteDatabaseListener;
import com.realfame.fileexplorer.FileCategoryHelper.CategoryInfo;
import com.realfame.fileexplorer.FileCategoryHelper.FileCategory;
import com.realfame.fileexplorer.FileExplorerTabActivity.IBackPressedListener;
import com.realfame.fileexplorer.FileViewInteractionHub.Mode;
import com.realfame.fileexplorer.Util.SDCardInfo;


/*TYRD: weina 20150624 add begin*/
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
/*TYRD: weina 20150624 add end*/
/*TYRD: weina 20150820 add for PROD103988369 BEGIN*/
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.view.ActionMode;
/*TYRD: weina 20150820 add for PROD103988369 END*/
public class FileCategoryActivity extends Fragment implements IFileInteractionListener,
        /*FavoriteDatabaseListener,*/ IBackPressedListener {

    public static final String EXT_FILETER_KEY = "ext_filter";

    private static final String LOG_TAG = "FileCategoryActivity";

    private static HashMap<Integer, FileCategory> button2Category = new HashMap<Integer, FileCategory>();

    private HashMap<FileCategory, Integer> categoryIndex = new HashMap<FileCategory, Integer>();

    private FileListCursorAdapter mAdapter;

    private FileViewInteractionHub mFileViewInteractionHub;

    private FileCategoryHelper mFileCagetoryHelper;

    private FileIconHelper mFileIconHelper;

    private CategoryBar mCategoryBar;

    private ScannerReceiver mScannerReceiver;
    private UpdateReceiver mReceiver;
    private FavoriteList mFavoriteList;

    private ViewPage curViewPage = ViewPage.Invalid;

    private ViewPage preViewPage = ViewPage.Invalid;

    private Activity mActivity;

    private View mRootView;

    private FileViewActivity mFileViewActivity;

    private boolean mConfigurationChanged = false;
    /*TYRD: weina 20150820 add for PROD103988369 BEGIN*/
    ContentObserver mContentObserver = new ContentObserver(new Handler()){
    	@Override
    	public void onChange(boolean selfChange){
    		updateUI();
    	}
    };
    /*TYRD: weina 20150820 add for PROD103988369 END*/

    public void setConfigurationChanged(boolean changed) {
        mConfigurationChanged = changed;
    }

    static {
        button2Category.put(R.id.category_music, FileCategory.Music);
        button2Category.put(R.id.category_video, FileCategory.Video);
        button2Category.put(R.id.category_picture, FileCategory.Picture);
        //button2Category.put(R.id.category_theme, FileCategory.Theme);
        button2Category.put(R.id.category_document, FileCategory.Doc);
        //button2Category.put(R.id.category_zip, FileCategory.Zip);
        button2Category.put(R.id.category_apk, FileCategory.Apk);
        button2Category.put(R.id.category_favorite, FileCategory.Favorite);
    }

    private FavoriteDatabaseListener listener = new FavoriteDatabaseListener(){
        @Override
        public void onFavoriteDatabaseChanged() {
            
            setCategoryCount(FileCategory.Favorite,mFavoriteList.getCount() );

            showEmptyView(mFavoriteList.getCount()==0);
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        mActivity = getActivity();
        mFileViewActivity = (FileViewActivity) ((FileExplorerTabActivity) mActivity)
                .getFragment(Util.SDCARD_TAB_INDEX);
        mRootView = inflater.inflate(R.layout.file_explorer_category, container, false);
        curViewPage = ViewPage.Invalid;
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        mFileViewInteractionHub.setMode(Mode.View);
        mFileViewInteractionHub.setRootPath("/");
        mFileIconHelper = new FileIconHelper(mActivity);
        mFavoriteList = new FavoriteList(mActivity, (ListView) mRootView.findViewById(R.id.favorite_list), listener, mFileIconHelper);
        mFavoriteList.initList();
        mAdapter = new FileListCursorAdapter(mActivity, null, mFileViewInteractionHub, mFileIconHelper);

        ListView fileListView = (ListView) mRootView.findViewById(R.id.file_path_list);
        fileListView.setAdapter(mAdapter);

        Button clearButton = (Button) mRootView.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(clearOnClickListener);

        setupClick();
        setupCategoryInfo();
        updateUI();
        registerScannerReceiver();
        /*TYRD: weina 20150820 add for PROD103988369 BEGIN*/
        mActivity.getContentResolver().registerContentObserver(Files.getContentUri("external"),true,mContentObserver);
        mActivity.getContentResolver().registerContentObserver(Audio.Media.getContentUri("external"),true,mContentObserver);
        mActivity.getContentResolver().registerContentObserver(Video.Media.getContentUri("external"),true,mContentObserver);
        mActivity.getContentResolver().registerContentObserver(Images.Media.getContentUri("external"),true,mContentObserver);
        /*TYRD: weina 20150820 add for PROD103988369 END*/

        
        return mRootView;
    }
    
    View.OnClickListener clearOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
              Intent intent = new Intent("com.market2345.clean.action.MAIN");
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              mActivity.startActivity(intent);
        }

    };

    private void registerScannerReceiver() {
        mScannerReceiver = new ScannerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        mActivity.registerReceiver(mScannerReceiver, intentFilter);
		mReceiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.action.UPDATE_FAVARITE_CATEGORY");
		mActivity.registerReceiver(mReceiver, filter);
    }

    private void setupCategoryInfo() {
        mFileCagetoryHelper = new FileCategoryHelper(mActivity);

        mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        int[] imgs = new int[] {
                R.drawable.category_bar_music, R.drawable.category_bar_video,
                R.drawable.category_bar_picture,R.drawable.category_bar_document,
                R.drawable.category_bar_apk,R.drawable.category_bar_other/* R.drawable.category_bar_theme,
                R.drawable.category_bar_zip,*/ //TYRD: weina 20150818 deleted  for PROD103986761
        };

        for (int i = 0; i < imgs.length; i++) {
            mCategoryBar.addCategory(imgs[i]);
        }

        for (int i = 0; i < FileCategoryHelper.sCategories.length; i++) {
            categoryIndex.put(FileCategoryHelper.sCategories[i], i);
        }
    }

    public void refreshCategoryInfo() {
        SDCardInfo sdCardInfo = Util.getSDCardInfo();
        if (sdCardInfo != null) {
            mCategoryBar.setFullValue(sdCardInfo.total);
			/*TYRD: weina 20150624 modify begin*/
            setTextView(R.id.sd_card_capacity, getActivity().getResources().getString(R.string.sd_card_size, Util.convertStorage(sdCardInfo.total)));
			
            //setTextView(R.id.sd_card_available, getActivity().getResources().getString(R.string.sd_card_available, Util.convertStorage(sdCardInfo.free)));
            //xueyingli add 20150317
            /*ProgressBar progressStroage = (ProgressBar) mRootView.findViewById(R.id.sd_card_progressbar);
            long gb = 1024*1024*1024;
            float freeStroage = sdCardInfo.free/gb;
            float totalStroage = sdCardInfo.total/gb;
            int pre = (int)(((totalStroage - freeStroage)/totalStroage)*100);
            progressStroage.setProgress(pre);
            progressStroage.setSecondaryProgress(pre);*/
            //xueyingli add end
            setTextView(R.id.free_space, getActivity().getResources().getString(R.string.sd_card_available, Util.convertStorage(sdCardInfo.free)));
			setTextView(R.id.total_space, getActivity().getResources().getString(R.string.total_size, Util.convertStorage(sdCardInfo.total)));
			/*TYRD: weina 20150624 modify  end*/
        }

        mFileCagetoryHelper.refreshCategoryInfo();

        // the other category size should include those files didn't get scanned.
        long size = 0;
        for (FileCategory fc : FileCategoryHelper.sCategories) {
            CategoryInfo categoryInfo = mFileCagetoryHelper.getCategoryInfos().get(fc);
            setCategoryCount(fc, categoryInfo.count);

            // other category size should be set separately with calibration
            if(fc == FileCategory.Other)
                continue;

            setCategorySize(fc, categoryInfo.size);
            setCategoryBarValue(fc, categoryInfo.size);
            size += categoryInfo.size;
        }

        if (sdCardInfo != null) {
            long otherSize = sdCardInfo.total - sdCardInfo.free - size;
            //baitao 2016.01.21 add begin
            if (otherSize < 0) {
                otherSize = Math.abs(otherSize);
            }
            //baitao 2016.01.21 add end
            setCategorySize(FileCategory.Other, otherSize);
            setCategoryBarValue(FileCategory.Other, otherSize);
        }

        setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());

        if (mCategoryBar.getVisibility() == View.VISIBLE) {
            mCategoryBar.startAnimation();
        }
    }

    public enum ViewPage {
        Home, Favorite, Category, NoSD, Invalid
    }

    private void showPage(ViewPage p) {
        if (curViewPage == p) return;

        curViewPage = p;

        showView(R.id.file_path_list, false);
        showView(R.id.navigation_bar, false);
        showView(R.id.category_page, false);
       // showView(R.id.operation_bar, false);
        showView(R.id.sd_not_available_page, false);
		showView(R.id.dropdown_navigation, false);//TYRD: weina 20150624 add 
        mFavoriteList.show(false);
        showEmptyView(false);

        switch (p) {
            case Home:
                showView(R.id.category_page, true);
                if (mConfigurationChanged) {
                    ((FileExplorerTabActivity) mActivity).reInstantiateCategoryTab();
                    mConfigurationChanged = false;
                }
                break;
            case Favorite:
			    /*TYRD: weina 20150624 modify begin*/
                //showView(R.id.navigation_bar, true); 
                showView(R.id.dropdown_navigation, true);
				/*TYRD: weina 20150624 modify end*/
                mFavoriteList.show(true);
				
                showEmptyView(mFavoriteList.getCount() == 0);
                break;
            case Category:
			    /*TYRD: weina 20150624 modify begin*/
                //showView(R.id.navigation_bar, true);weina test templemet
                showView(R.id.dropdown_navigation, true);
				/*TYRD: weina 20150624 modify end */
                showView(R.id.file_path_list,true);
                showEmptyView(mAdapter.getCount() == 0);
                break;
            case NoSD:
                showView(R.id.sd_not_available_page, true);
                break;
        }
    }

    private void showEmptyView(boolean show) {
	   
	   /*View emptyView = mActivity.findViewById(R.id.empty_view);
        if (emptyView != null)
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);*/
		/*TYRD: weina 20150624 modify begin*/
        View emptyView = getActivity().findViewById(R.id.empty_view);
        if (emptyView != null){
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
			ImageView icon = (ImageView)emptyView .findViewById(R.id.icon);
			icon.setImageResource(R.drawable.empty_icon);
			TextView text = (TextView)emptyView .findViewById(R.id.text);
			text.setText(R.string.no_file);
			emptyView.requestLayout();
    	}
		/*TYRD: weina 20150624 modify  end*/
		
    }

    private void showView(int id, boolean show) {
        View view = mRootView.findViewById(id);
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);			
        }
		
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileCategory f = button2Category.get(v.getId());
            if (f != null) {
                onCategorySelected(f);
                if (f != FileCategory.Favorite) {
                    setHasOptionsMenu(false);
                }
            }
        }

    };

    private void setCategoryCount(FileCategory fc, long count) {
        int id = getCategoryCountId(fc);
        if (id == 0)
            return;

        setTextView(id, count + getResources().getString(R.string.count_number));
    }

    private void setTextView(int id, String t) {
        TextView text = (TextView) mRootView.findViewById(id);
        text.setText(t);
    }

    private void onCategorySelected(FileCategory f) {
        if (mFileCagetoryHelper.getCurCategory() != f) {
            mFileCagetoryHelper.setCurCategory(f);
			/*TYRD: weina 20150624 modify begin*/
            mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub.getRootPath()+getString(R.string.tab_category) 
                    +"/"+ getString(mFileCagetoryHelper.getCurCategoryNameResId()));
			//mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub.getRootPath()
            //        + getString(mFileCagetoryHelper.getCurCategoryNameResId()));		
           // mFileViewInteractionHub.refreshFileList();
		   /*TYRD: weina 20150624 modify end*/
        }
        mFileViewInteractionHub.refreshFileList();//TYRD: weina 20150624 add 
        if (f == FileCategory.Favorite) {
            showPage(ViewPage.Favorite);
        } else {
            showPage(ViewPage.Category);
        }
		
    }

    private void setupClick(int id) {
        View button = mRootView.findViewById(id);
        button.setOnClickListener(onClickListener);
    }

    private void setupClick() {
        setupClick(R.id.category_music);
        setupClick(R.id.category_video);
        setupClick(R.id.category_picture);
        //setupClick(R.id.category_theme);
        setupClick(R.id.category_document);
        //setupClick(R.id.category_zip);
        setupClick(R.id.category_apk);
        setupClick(R.id.category_favorite);
    }

    @Override
    public boolean onBack() {
		if (isHomePage() || curViewPage == ViewPage.NoSD || mFileViewInteractionHub == null) {
			
            return false;
        }
        return mFileViewInteractionHub.onBackPressed();
    }

    public boolean isHomePage() {
        return curViewPage == ViewPage.Home;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (curViewPage != ViewPage.Category && curViewPage != ViewPage.Favorite) {
            return;
        }
        mFileViewInteractionHub.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
         if (!isHomePage() && mFileCagetoryHelper.getCurCategory() != FileCategory.Favorite) {
            mFileViewInteractionHub.onPrepareOptionsMenu(menu);
        }/*TYRD: weina 20150824 add for PROD103993635 BEGIN*/
		 else{
           menu.clear();
		}
		 /*TYRD: weina 20150824 add for PROD103993635 END*/
    }

    public boolean onRefreshFileList(String path, FileSortHelper sort) {
		FileCategory curCategory = mFileCagetoryHelper.getCurCategory();
		/*TYRD: weina 20150624 add begin*/
		if(mFileViewInteractionHub!=null){
			if(!mFileViewInteractionHub.isShowCheckBox()&&! mFileViewInteractionHub.isSelected()){
				mFileViewInteractionHub.showConfirmOperationBar(false);
			}else if(mFileViewInteractionHub.isShowCheckBox()){
				mFileViewInteractionHub.showConfirmOperationBar(true);
			}else{
			    mFileViewInteractionHub.showConfirmOperationBar(false);
			}
			
		}
		
		/*TYRD: weina 20150624 add end*/
        if (curCategory == FileCategory.Favorite || curCategory == FileCategory.All){
			if(mFileViewInteractionHub!=null){
				mFileViewInteractionHub.showOperationMenuBar(false);
			}
            return false;
        }
        Cursor c = mFileCagetoryHelper.query(curCategory, sort.getSortMethod());
		
        showEmptyView(c == null || c.getCount() == 0);
		
        mAdapter.changeCursor(c);
        if(mFileViewInteractionHub!=null){
			if(mAdapter.getCount()==0){
				mFileViewInteractionHub.updateOperationMenuBar(false);
			}else{
			    mFileViewInteractionHub.updateOperationMenuBar(true);
			}
		}
        return true;
    }

    //add by chen he 2016.02.22
    public void cancelRefreshAsyncTask() {

    }
    //add end
    
    @Override
    public View getViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                mFavoriteList.getArrayAdapter().notifyDataSetChanged();
                showEmptyView(mAdapter.getCount() == 0);
            }

        });
    }

    @Override
    public void onPick(FileInfo f) {
        // do nothing
    }

    @Override
    public boolean shouldShowOperationPane() {
        return true;
    }

    @Override
    public boolean onOperation(int id) {
        mFileViewInteractionHub.addContextMenuSelectedItem();
        switch (id) {
           // case R.id.button_operation_copy:
            case GlobalConsts.MENU_COPY:
                copyFileInFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
           // case R.id.button_operation_move:
            case GlobalConsts.MENU_MOVE:
                startMoveToFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case GlobalConsts.OPERATION_UP_LEVEL:
				setHasOptionsMenu(false);
                showPage(ViewPage.Home);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public String getDisplayPath(String path) {
	    /*TYRD: weina 20150624 mosify begin*/
        //return getString(R.string.tab_category) + path;
        return path;
        /*TYRD: weina 20150624 modify end*/
    }

    @Override
    public String getRealPath(String displayPath) {
	   /*TYRD: weina 20150624 modify begin*/
       // return "";
       return displayPath;
	   /*TYRD: weina 20150624 modify end*/
    }

    @Override
    public boolean onNavigation(String path) {
	    /*TYRD: weina 20150624 modify begin*/
		//showPage(ViewPage.Home);
		//	return true;
		if(("/"+getString(R.string.tab_category)).equals(path)){
			/*TYRD: weina 20150901 add begin*/
			if(mFileViewInteractionHub!=null){
    			mFileViewInteractionHub.clearSelection();
    			ActionMode actionMode = ((FileExplorerTabActivity)getActivity()).getActionMode();
                if (actionMode != null) {
                    actionMode.finish();
    				
                }	
    			mFileViewInteractionHub.showConfirmOperationBar(false);
			/*TYRD: weina 20150901 add end*/
			showPage(ViewPage.Home);
			mFileViewInteractionHub.showOperationMenuBar(false);
			}
			return true;
		}else{
            return false;
		}
        
        /*TYRD: weina 20150624 modify end*/
    }

    @Override
    public boolean shouldHideMenu(int menu) {
        return (menu == GlobalConsts.MENU_NEW_FOLDER || menu == GlobalConsts.MENU_FAVORITE
                || menu == GlobalConsts.MENU_PASTE || menu == GlobalConsts.MENU_SHOWHIDE);
    }
	/*TYRD: weina 20150624 add begin*/
    @Override
	public boolean disenabledMenu(){
        if(mAdapter.getCount()==0){
             return true;
		}else{
            return false;
		}       
	}
    /*TYRD: weina 20150624 add end*/
    @Override
    public void addSingleFile(FileInfo file) {
        refreshList();
    }

    @Override
    public Collection<FileInfo> getAllFiles() {
        return mAdapter.getAllFiles();
    }

    @Override
    public FileInfo getItem(int pos) {
        return mAdapter.getFileItem(pos);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getCount();
    }

    @Override
    public void sortCurrentList(FileSortHelper sort) {
        refreshList();
    }

    private void refreshList() {
        mFileViewInteractionHub.refreshFileList();
    }

    private void copyFileInFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
        mFileViewActivity.copyFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
    }

    private void startMoveToFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
        mFileViewActivity.moveToFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
    }

    @Override
    public FileIconHelper getFileIconHelper() {
        return mFileIconHelper;
    }

    private static int getCategoryCountId(FileCategory fc) {
        switch (fc) {
            case Music:
                return R.id.category_music_count;
            case Video:
                return R.id.category_video_count;
            case Picture:
                return R.id.category_picture_count;
            /*case Theme:
                return R.id.category_theme_count;*/
            case Doc:
                return R.id.category_document_count;
            /*case Zip:
                return R.id.category_zip_count;*/
            case Apk:
                return R.id.category_apk_count;
            case Favorite:
                return R.id.category_favorite_count;
        }

        return 0;
    }

    private void setCategorySize(FileCategory fc, long size) {
        int txtId = 0;
        int resId = 0;
        switch (fc) {
            case Music:
                txtId = R.id.category_legend_music;
                resId = R.string.category_music;
                break;
            case Video:
                txtId = R.id.category_legend_video;
                resId = R.string.category_video;
                break;
            case Picture:
                txtId = R.id.category_legend_picture;
                resId = R.string.category_picture;
                break;
            /*case Theme:
                txtId = R.id.category_legend_theme;
                resId = R.string.category_theme;
                break;*/
            case Doc:
                txtId = R.id.category_legend_document;
                resId = R.string.category_document;
                break;
            /*case Zip:
                txtId = R.id.category_legend_zip;
                resId = R.string.category_zip;
                break;*/
            case Apk:
                txtId = R.id.category_legend_apk;
                resId = R.string.category_apk;
                break;
            case Other:
                txtId = R.id.category_legend_other;
                resId = R.string.category_other;
                break;
        }

        if (txtId == 0 || resId == 0)
            return;

        setTextView(txtId, getString(resId) + ":" + Util.convertStorage(size));
    }

    private void setCategoryBarValue(FileCategory f, long size) {
        if (mCategoryBar == null) {
            mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        }
        mCategoryBar.setCategoryValue(categoryIndex.get(f), size);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mActivity != null) {
            mActivity.unregisterReceiver(mScannerReceiver);
			mActivity.unregisterReceiver(mReceiver);
            mActivity.getContentResolver().unregisterContentObserver(mContentObserver);//TYRD: weina 20150820 add for PROD103988369 
        }
    }

    private class ScannerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(LOG_TAG, "received broadcast: " + action.toString());
            // handle intents related to external storage
            if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED) || action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
					/*TYRD: weina 20150624 modify begin*/
                //notifyFileChanged();
                
                updateUI();
				/*TYRD: weina 20150624 modify end*/
            }
            
        }
    }
	public class UpdateReceiver extends BroadcastReceiver{
		@Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("com.action.UPDATE_FAVARITE_CATEGORY")){
				/* FavoriteDatabaseHelper  mFavoriteDatabase   = new FavoriteDatabaseHelper(mActivity);
     			int num = 0;
     			Cursor c = mFavoriteDatabase.query(); 
     			if(c != null){
     				num = c.getCount();
     				c.close();
     			}*/
                mFavoriteList.initList();
                 setCategoryCount(FileCategory.Favorite,mFavoriteList.getCount() );
		
		         showEmptyView(mFavoriteList.getCount()==0);
			}
		}
	}

    public void refreshFragment(){
        updateUI();
    }

    private void updateUI() {
        boolean sdCardReady = Util.isSDCardReady();
        if (sdCardReady) {
            if (preViewPage != ViewPage.Invalid) {
                showPage(preViewPage);
                preViewPage = ViewPage.Invalid;
            } else if (curViewPage == ViewPage.Invalid || curViewPage == ViewPage.NoSD) {
                showPage(ViewPage.Home);
            }
			/*TYRD: weina 20160411 modity for optimizing the showing-speeding begin */
			//refreshCategoryInfo();
			if(curViewPage ==ViewPage.Home){
				refreshCategoryInfo();
			}
            /*TYRD: weina 20160411 modity for optimizing the showing-speeding end*/
            // refresh file list
            mFileViewInteractionHub.refreshFileList();
            // refresh file list view in another tab
            //mFileViewActivity.refresh();//TYRD: weina 20150624 deleted
        } else {
            preViewPage = curViewPage;
            showPage(ViewPage.NoSD);
        }
        
    }

    // process file changed notification, using a timer to avoid frequent
    // refreshing due to batch changing on file system
    synchronized public void notifyFileChanged() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                timer = null;
                Message message = new Message();
                message.what = MSG_FILE_CHANGED_TIMER;
                handler.sendMessage(message);
            }

        }, 1000);
    }

    private static final int MSG_FILE_CHANGED_TIMER = 100;

    private Timer timer;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FILE_CHANGED_TIMER:
                    updateUI();
                    break;
            }
            super.handleMessage(msg);
        }

    };

    // update the count of favorite
   // @Override
   // public void onFavoriteDatabaseChanged() {
  //      setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());
		/*TYRD: weina 20150826 add begin*/
//		showEmptyView(mFavoriteList.getCount()==0);
		/*TYRD: weina 20150826 add end*/
  //  }

    @Override
    public void runOnUiThread(Runnable r) {
        mActivity.runOnUiThread(r);
    }
}
