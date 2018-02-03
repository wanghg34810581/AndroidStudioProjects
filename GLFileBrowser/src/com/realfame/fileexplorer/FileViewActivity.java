package com.realfame.fileexplorer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import com.realfame.fileexplorer.FileExplorerTabActivity.IBackPressedListener;
import com.realfame.fileexplorer.FileViewInteractionHub.Mode;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.database.Cursor;
/*TYRD: weina 20150624 add begin*/
import android.os.Handler;
import android.os.Message;
/*TYRD: weina 20150624 add end*/
/* baitao 20160104 add begin*/
//import android.os.SystemProperties;
/* baitao 20160104 add end*/
import android.os.AsyncTask; //add by chen he 2016.02.22

public class FileViewActivity extends Fragment implements
        IFileInteractionListener, IBackPressedListener {

    public static final String EXT_FILTER_KEY = "ext_filter";

    private static final String LOG_TAG = "FileViewActivity";

    public static final String EXT_FILE_FIRST_KEY = "ext_file_first";

    public static final String ROOT_DIRECTORY = "root_directory";

    public static final String PICK_FOLDER = "pick_folder";

    private ListView mFileListView;

    // private TextView mCurrentPathTextView;
    private ArrayAdapter<FileInfo> mAdapter;

    private FileViewInteractionHub mFileViewInteractionHub;

    private FileCategoryHelper mFileCagetoryHelper;

    private FileIconHelper mFileIconHelper;

    private ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();

    private Activity mActivity;

    private View mRootView;
    private View popupMenuView;
    private static final String sdDir = Util.getSdDirectory();

	private static final int MAX_PUBLISH_COUNT = 50; //add by chen he 2016.02.22
    private String mLastPath = null; //add by chen he 2016.02.22
    // memorize the scroll positions of previous paths
    private ArrayList<PathScrollPositionItem> mScrollPositionList = new ArrayList<PathScrollPositionItem>();
    private String mPreviousPath;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.v(LOG_TAG, "received broadcast:" + intent.toString());
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)||action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {//TYRD: weina 20150818 add || action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)for PROD103983886
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        }
    };

    private boolean mBackspaceExit;
    /*TYRD: weina 20150624 add begin*/
	private final static int MSG_UPDATE_UI=1;
	private Handler handler = new Handler(){
		@Override
			public void handleMessage (Message msg){
               switch (msg.what){
                   case MSG_UPDATE_UI:{
				       updateUI();
					   break;
                   }
			   } 
		    }
	};
    /*TYRD: weina 20150624 add end*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        // getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);
        mRootView = inflater.inflate(R.layout.file_explorer_list, container, false);
        //ActivitiesManager.getInstance().registerActivity(ActivitiesManager.ACTIVITY_FILE_VIEW, mActivity);

        mFileCagetoryHelper = new FileCategoryHelper(mActivity);
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        Intent intent = mActivity.getIntent();
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)
                && (action.equals(Intent.ACTION_PICK) || action.equals(Intent.ACTION_GET_CONTENT)
                ||action.equals("com.android.fileexplorer.action.FILE_SINGLE_SEL"))) {//TYRD: weina 20150923 add ||action.equals("com.android.fileexplorer.action.FILE_SINGLE_SEL") forPROD104042909
            mFileViewInteractionHub.setMode(Mode.Pick);

            boolean pickFolder = intent.getBooleanExtra(PICK_FOLDER, false);
            if (!pickFolder) {
                String[] exts = intent.getStringArrayExtra(EXT_FILTER_KEY);
                if (exts != null) {
                    mFileCagetoryHelper.setCustomCategory(exts);
                }
            } else {
                mFileCagetoryHelper.setCustomCategory(new String[]{} /*folder only*/);
                mRootView.findViewById(R.id.pick_operation_bar).setVisibility(View.VISIBLE);

                mRootView.findViewById(R.id.button_pick_confirm).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        try {
                            Intent intent = Intent.parseUri(mFileViewInteractionHub.getCurrentPath(), 0);
                            mActivity.setResult(Activity.RESULT_OK, intent);
                            mActivity.finish();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                });

                mRootView.findViewById(R.id.button_pick_cancel).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        mActivity.finish();
                    }
                });
            }
        } else {
            mFileViewInteractionHub.setMode(Mode.View);
        }

        mFileListView = (ListView) mRootView.findViewById(R.id.file_path_list);
        mFileIconHelper = new FileIconHelper(mActivity);
        mAdapter = new FileListAdapter(mActivity, R.layout.file_browser_item, mFileNameList, mFileViewInteractionHub,
                mFileIconHelper);

        boolean baseSd = intent.getBooleanExtra(GlobalConsts.KEY_BASE_SD, !FileExplorerPreferenceActivity.isReadRoot(mActivity));
        Log.i(LOG_TAG, "baseSd = " + baseSd);

        String rootDir = intent.getStringExtra(ROOT_DIRECTORY);
        if (!TextUtils.isEmpty(rootDir)) {
            if (baseSd && this.sdDir.startsWith(rootDir)) {
                rootDir = this.sdDir;
            }
        } else {
            rootDir = baseSd ? this.sdDir : GlobalConsts.ROOT_PATH;
        }
		/*TYRD: weina 20150624 modity begin*/
	//baitao 20160104 mod
	mFileViewInteractionHub.setRootPath("/storage");
	//baitao 20160104 mod end
        /*TYRD: weina 20150624 modity end*/
        String currentDir = FileExplorerPreferenceActivity.getPrimaryFolder(mActivity);
        Uri uri = intent.getData();
        if (uri != null) {
            if (baseSd && this.sdDir.startsWith(uri.getPath())) {
                currentDir = this.sdDir;
            } else {
                currentDir = uri.getPath();
            }
        }
		/*TYRD: weina 20150624 modity begin*/
		mFileViewInteractionHub.setCurrentPath(currentDir);
	//baitao 20160104 mod end
		/*TYRD: weina 20150624 modity  end*/
        Log.i(LOG_TAG, "CurrentDir = " + currentDir);

        mBackspaceExit = (uri != null)
                && (TextUtils.isEmpty(action)
                || (!action.equals(Intent.ACTION_PICK) && !action.equals(Intent.ACTION_GET_CONTENT)));

        mFileListView.setAdapter(mAdapter);
       // mFileViewInteractionHub.refreshFileList();//TYRD: weina 20150624 deleted

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);//TYRD: weina 20150818 add for PROD103983886
        intentFilter.addDataScheme("file");
        mActivity.registerReceiver(mReceiver, intentFilter);

        
        setHasOptionsMenu(false);
        
        
        return mRootView;
    }
    

    @Override
    public void onResume(){
    	super.onResume();
    	/*hunan 2016-01-07 PROD104139696 optimize loading speed of the Fragment, delay the loading of data; the code originally added by weina 20150624*/
    	handler.sendEmptyMessageDelayed(MSG_UPDATE_UI, 100);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.unregisterReceiver(mReceiver);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
		mFileViewInteractionHub.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mFileViewInteractionHub.onCreateOptionsMenu(menu);
       
		
    }

	public void onMenuOpened(Menu menu){
        if("/storage".equals(mFileViewInteractionHub.getCurrentPath())){
			menu.setGroupEnabled(0,false);
		}else{
		   menu.setGroupEnabled(0,true);
		}
	}

    @Override
    public boolean onBack() {
        if (mBackspaceExit || !Util.isSDCardReady() || mFileViewInteractionHub == null) {
            return false;
        }
        return mFileViewInteractionHub.onBackPressed();
    }

    private class PathScrollPositionItem {
        String path;
        int pos;
        PathScrollPositionItem(String s, int p) {
            path = s;
            pos = p;
        }
    }

    // execute before change, return the memorized scroll position
    private int computeScrollPosition(String path) {
        int pos = 0;
        if(mPreviousPath!=null) {
            if (path.startsWith(mPreviousPath)) {
                int firstVisiblePosition = mFileListView.getFirstVisiblePosition();
                if (mScrollPositionList.size() != 0
                        && mPreviousPath.equals(mScrollPositionList.get(mScrollPositionList.size() - 1).path)) {
                    mScrollPositionList.get(mScrollPositionList.size() - 1).pos = firstVisiblePosition;
                    Log.i(LOG_TAG, "computeScrollPosition: update item: " + mPreviousPath + " " + firstVisiblePosition
                            + " stack count:" + mScrollPositionList.size());
                    pos = firstVisiblePosition;
                } else {
                    mScrollPositionList.add(new PathScrollPositionItem(mPreviousPath, firstVisiblePosition));
                    Log.i(LOG_TAG, "computeScrollPosition: add item: " + mPreviousPath + " " + firstVisiblePosition
                            + " stack count:" + mScrollPositionList.size());
                }
            } else {
                int i;
                boolean isLast = false;
                for (i = 0; i < mScrollPositionList.size(); i++) {
                    if (!path.startsWith(mScrollPositionList.get(i).path)) {
                        break;
                    }
                }
                // navigate to a totally new branch, not in current stack
                if (i > 0) {
                    pos = mScrollPositionList.get(i - 1).pos;
                }

                for (int j = mScrollPositionList.size() - 1; j >= i-1 && j>=0; j--) {
                    mScrollPositionList.remove(j);
                }
            }
        }

        Log.i(LOG_TAG, "computeScrollPosition: result pos: " + path + " " + pos + " stack count:" + mScrollPositionList.size());
        mPreviousPath = path;
        return pos;
    }
    public boolean onRefreshFileList(String path, FileSortHelper sort) {

		
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        //add by chen he 2016.02.22
        
        boolean refreshTheSamePath = false;
        if (!path.equals(mLastPath)) {
            mFileNameList.clear();
			onDataChanged();
            mLastPath = path;
            refreshTheSamePath = false;
			showEmptyView(false);
        } else {
            refreshTheSamePath = true;
        }
		if (mRefreshFileTask != null) {
			mRefreshFileTask.setCancelEnabled(true);
			mRefreshFileTask.cancel(true);
			mRefreshFileTask = null;
		}

		Object[] objects = new Object[3];
		objects[0] = path;
		objects[1] = sort;
        objects[2] = refreshTheSamePath;
		mRefreshFileTask = new MyRefreshFileListAsyncTask();
		mRefreshFileTask.execute(objects);
		return true;
		//add end
		/*
        final int pos = computeScrollPosition(path);
        ArrayList<FileInfo> fileList = mFileNameList;
        fileList.clear();

        File[] listFiles = file.listFiles(mFileCagetoryHelper.getFilter());
        if (listFiles == null)
            return true;
        for (File child : listFiles) {
            // do not show selected file if in move state
            if (mFileViewInteractionHub.isMoveState() && mFileViewInteractionHub.isFileSelected(child.getPath())){
                if (!mFileViewInteractionHub.isCopy()){ 
                    continue;
                }
            }
                

            String absolutePath = child.getAbsolutePath();
            if (Util.isNormalFile(absolutePath) && Util.shouldShowFile(absolutePath)) {
                FileInfo lFileInfo = Util.GetFileInfo(child,
                        mFileCagetoryHelper.getFilter(), Settings.instance().getShowDotAndHiddenFiles());
                if (lFileInfo != null) {
                    lFileInfo.dbId = getDbId(lFileInfo.filePath);
                    fileList.add(lFileInfo);
                }
            }
        }	
        sortCurrentList(sort);
        showEmptyView(fileList.size() == 0);
        mFileListView.post(new Runnable() {
            @Override
            public void run() {
                mFileListView.setSelection(pos);
            }
        });
        return true;*/
    }
	//add by chen he 2016.02.22
    public void cancelRefreshAsyncTask() {
        if (mRefreshFileTask != null) {
            mRefreshFileTask.setCancelEnabled(true);
            mRefreshFileTask.cancel(true);
            mRefreshFileTask = null;
        }
    }
	private MyRefreshFileListAsyncTask mRefreshFileTask = null;
	private class MyRefreshFileListAsyncTask extends AsyncTask<Object[], Object, Void> {
		private FileSortHelper mSort = null;
		private boolean mCancelEnable = false;
       private boolean mRefreshTheSamePath = false;
		private int mPos = 0;
		public void setCancelEnabled (boolean enable) {
			mCancelEnable = enable;
		}

		protected Void doInBackground (Object[]... arg0) {
			Object[] objects = (Object[])arg0[0];
			String path = (String)objects[0];
			mSort = (FileSortHelper)objects[1];
            //the same path, just refresh fews file, so update only once and update all
            mRefreshTheSamePath = (Boolean) objects[2];
			mPos = computeScrollPosition(path);
			File file = new File(path);
		    File[] listFiles = file.listFiles(mFileCagetoryHelper.getFilter());
		    if (listFiles == null) {
		        return null;
           }
			boolean showDotAndHidden = Settings.instance().getShowDotAndHiddenFiles();
			ArrayList<FileInfo> tempFileList = new ArrayList<FileInfo>();
		    for (File child : listFiles) {
				if (mCancelEnable) {
					break;
				}
		        if (mFileViewInteractionHub.isMoveState() && mFileViewInteractionHub.isFileSelected(child.getPath())){
		            if (!mFileViewInteractionHub.isCopy()){ 
		                continue;
		            }
		        }  
		        String absolutePath = child.getAbsolutePath();
		        if (Util.isNormalFile(absolutePath) && Util.shouldShowFile(absolutePath)) {
		            FileInfo lFileInfo = Util.GetFileInfo(child,
		                    mFileCagetoryHelper.getFilter(), showDotAndHidden);
		            if (lFileInfo != null) {
		                //lFileInfo.dbId = getDbId(lFileInfo.filePath);
		                if("uicc0".equals(lFileInfo.fileName)&& "/storage".equals(path))continue;
		                tempFileList.add(lFileInfo);
		            }
		        }
               if (!mRefreshTheSamePath) {
				if (tempFileList.size() >= MAX_PUBLISH_COUNT) {
					ArrayList<FileInfo> insertFileList = new ArrayList<FileInfo>();
					insertFileList.addAll(tempFileList);
					publishProgress(insertFileList);
					tempFileList.clear();
				} 
              }
		    }
			if (!mCancelEnable) {
				publishProgress(tempFileList);
			}
			for (int i = 0; i < mFileNameList.size(); i++) {
				if (mCancelEnable) {
					break;
				}
				try {
					FileInfo fileInfo = mFileNameList.get(i);
					if (fileInfo.dbId <= 0) {
						fileInfo.dbId = getDbId(fileInfo.filePath);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(LOG_TAG, "MyRefreshFileListAsyncTask---e = " + e.getMessage());
					break;
				}
			}
			return null;
		}
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
			ArrayList<FileInfo> fileInfos = (ArrayList<FileInfo>)values[0];
			if (mCancelEnable) {
				return;
			}
            if (mRefreshTheSamePath) {
               mFileNameList.clear();
            }
			mFileNameList.addAll(fileInfos);
			sortCurrentList(mSort);
		}
		protected void onPostExecute (Void result) {
			super.onPostExecute(result);
			if (mCancelEnable) {
				return;
			}
		    showEmptyView(mFileNameList.size() == 0);
		    mFileListView.post(new Runnable() {
		        @Override
		        public void run() {
		            mFileListView.setSelection(mPos);
		        }
		    });
			if(mFileViewInteractionHub!=null){
     			if(mAdapter.getCount()==0){
     				mFileViewInteractionHub.updateOperationMenuBar(false);
     			}else{
     			    mFileViewInteractionHub.updateOperationMenuBar(true);
     			}
		    }
		}
	}
	//add end
    private long getDbId(String path){
        String volumeName = "external";
        Uri uri = Files.getContentUri(volumeName);
        String selection = FileColumns.DATA + "=?";
        String[] selectionArgs = new String[] {
            path
        };

        String[] columns = new String[] {
                FileColumns._ID, FileColumns.DATA
        };

        Cursor c = mActivity.getContentResolver()
                .query(uri, columns, selection, selectionArgs, null);
        if (c == null) {
            return 0;
        }
        long id = 0;
        if (c.moveToNext()) {
            id = c.getLong(0);
        }
        c.close();
        return id;
        
    }

    private void updateUI() {
        boolean sdCardReady = Util.isSDCardReady();
        View noSdView = mRootView.findViewById(R.id.sd_not_available_page);
        noSdView.setVisibility(sdCardReady ? View.GONE : View.VISIBLE);

        //View navigationBar = mRootView.findViewById(R.id.navigation_bar);
        //navigationBar.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);
        mFileListView.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);

        if(sdCardReady) {
            mFileViewInteractionHub.refreshFileList();
        }
    }

    private void showEmptyView(boolean show) {
        View emptyView = mRootView.findViewById(R.id.empty_view);
        if (emptyView != null)
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

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
            }

        });
    }

	public boolean isConfirmButtonBarVisible(){
		if(mFileViewInteractionHub!=null){
			return mFileViewInteractionHub.isShowConfirmButtonBarVisible();
		}else{
		    return false;
		}
		
	}
    public void setConfirmButtonBarInvisible(){
		if(mFileViewInteractionHub!=null){
		   mFileViewInteractionHub.clearSelection();
		   mFileViewInteractionHub.showConfirmOperationBar(false);
		}
	}
    @Override
    public void onPick(FileInfo f) {
        try {
            Intent intent = Intent.parseUri(Uri.fromFile(new File(f.filePath)).toString(), 0);
            mActivity.setResult(Activity.RESULT_OK, intent);
            mActivity.finish();
            return;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldShowOperationPane() {
        return true;
    }

    @Override
    public boolean onOperation(int id) {
        return false;
    }

    //支持显示真实路径
    @Override
    public String getDisplayPath(String path) {
        if(path.equals("storage")){
            return getString(R.string.storage_display);
        }else if (path.equals("sdcard0")) {
            return getString(R.string.phone_storage);
        }else if (path.equals("sdcard1")) {
            return getString(R.string.external_sd_card);
        }//TYRD: weina 20150624 deleted end 
		/*else if (path.startsWith(this.sdDir) && !FileExplorerPreferenceActivity.showRealPath(mActivity)) {
            
            return getString(R.string.sd_folder) + path.substring(this.sdDir.length());
        } */
		//TYRD: weina 20150624 deleted end
		else {
            return path;
        }
    }

    @Override
    public String getRealPath(String displayPath) {
        final String perfixName = getString(R.string.sd_folder);
        if (displayPath.startsWith(perfixName)) {
            return this.sdDir + displayPath.substring(perfixName.length());
        } else {
            return displayPath;
        }
    }

    @Override
    public boolean onNavigation(String path) {
        return false;
    }

    @Override
    public boolean shouldHideMenu(int menu) {
        return false;
    }
    /*TYRD: weina 20150624 add begin*/
	@Override
	public boolean disenabledMenu(){
	 
        /*if(mAdapter.getCount()==0 &&(menu == GlobalConsts.MENU_MUTI_COPY || menu == GlobalConsts.MENU_MUTI_CUT
                || menu == GlobalConsts.MENU_MUTI_DEL || menu == GlobalConsts.MENU_SORT)) */
         if(mAdapter.getCount()==0) { 
             return true;
		}else{
            return false;
		}       
	}
    /*TYRD: weina 20150624 add end*/
    public void copyFile(ArrayList<FileInfo> files) {
        mFileViewInteractionHub.onOperationCopy(files);
    }

    public void refresh() {
        if (mFileViewInteractionHub != null) {
            mFileViewInteractionHub.refreshFileList();
        }
    }

    public void moveToFile(ArrayList<FileInfo> files) {
        mFileViewInteractionHub.moveFileFrom(files);
    }

    public interface SelectFilesCallback {
        // files equals null indicates canceled
        void selected(ArrayList<FileInfo> files);
    }

    public void startSelectFiles(SelectFilesCallback callback) {
        mFileViewInteractionHub.startSelectFiles(callback);
    }

    @Override
    public FileIconHelper getFileIconHelper() {
        return mFileIconHelper;
    }

    public boolean setPath(String location) {
        if (!location.startsWith(mFileViewInteractionHub.getRootPath())) {
            return false;
        }
        mFileViewInteractionHub.setCurrentPath(location);
        mFileViewInteractionHub.refreshFileList();
        return true;
    }

    @Override
    public FileInfo getItem(int pos) {
        if (pos < 0 || pos > mFileNameList.size() - 1)
            return null;

        return mFileNameList.get(pos);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void sortCurrentList(FileSortHelper sort) {
        Collections.sort(mFileNameList, sort.getComparator());
        onDataChanged();
    }

    @Override
    public ArrayList<FileInfo> getAllFiles() {
        return mFileNameList;
    }

    @Override
    public void addSingleFile(FileInfo file) {
        mFileNameList.add(file);
        onDataChanged();
    }

    @Override
    public int getItemCount() {
        return mFileNameList.size();
    }

    @Override
    public void runOnUiThread(Runnable r) {
        mActivity.runOnUiThread(r);
    }
}
