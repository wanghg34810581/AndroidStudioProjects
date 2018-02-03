package com.realfame.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.realfame.fileexplorer.FileListItem.ModeCallback;
import com.realfame.fileexplorer.FileOperationHelper.IOperationProgressListener;
import com.realfame.fileexplorer.FileSortHelper.SortMethod;
import com.realfame.fileexplorer.FileViewActivity.SelectFilesCallback;
import com.realfame.fileexplorer.TextInputDialog.OnFinishListener;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
/*TYRD: weina 20150623 add begin*/
import android.app.ActionBar;
import android.net.Uri;
import android.app.Fragment;
import android.widget.CheckBox;
import android.media.AudioManager;
import android.view.MenuInflater;
import android.widget.HorizontalScrollView;
import android.content.DialogInterface;
/*TYRD: weina 20150623 add end*/
//import android.os.SystemProperties;//baitao 2016.01.19 add

public class FileViewInteractionHub implements IOperationProgressListener {
    private static final String LOG_TAG = "FileViewInteractionHub";

    private IFileInteractionListener mFileViewListener;

    private ArrayList<FileInfo> mCheckedFileNameList = new ArrayList<FileInfo>();

    private FileOperationHelper mFileOperationHelper;

    private FileSortHelper mFileSortHelper;

    private View mConfirmOperationBar;
	
	private View mOperationMenuBar;
    private ImageView copyMenuView;
	private ImageView moveMenuView;
	private ImageView deleteMenuView;
	private ImageView moreMenuView;
	
    private ProgressDialog progressDialog;

    private View mNavigationBar;

    private TextView mNavigationBarText;
    //private TextView mCurrentPathBarText;

    private View mDropdownNavigation;


    private ImageView mNavigationBarUpDownArrow;

    private Context mContext;

    public enum Mode {
        View, Pick
    };
	/*TYRD:weina 20150624 add begin*/
	private HorizontalScrollView mScrollView;
    AlertDialog alertDialog;
	private boolean canShowCheckBox = false;
    private boolean isCopyOperation = false;
	private boolean isCutOperation = false;
	private boolean isDelOperation = false;
	private static final String  ACTION_MEDIA_SCANNER_SCAN_ALL ="com.android.fileexplorer.action.MEDIA_SCANNER_SCAN_ALL";
	/*TYRD:weina 20150624 add end*/
    public FileViewInteractionHub(IFileInteractionListener fileViewListener) {
        assert (fileViewListener != null);
        mFileViewListener = fileViewListener;
        setup();
        mContext = mFileViewListener.getContext();
        mFileOperationHelper = new FileOperationHelper(mContext, this);
        mFileSortHelper = new FileSortHelper();
    }

	private void showProgress(String msg) {
        progressDialog = new ProgressDialog(mContext);
        // dialog.setIcon(R.drawable.icon);
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    /*TYRD: weina 20150824 add for PROD103988457 BEGIN*/
    private void showCopyProgress(String msg) {
        progressDialog = new ProgressDialog(mContext);
        // dialog.setIcon(R.drawable.icon);
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
		
		progressDialog.setButton(mContext.getResources().getString(R.string.cancel),new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				Util.flag = true;
			}
		});
        progressDialog.show();
    }
    /*TYRD: weina 20150824 add for PROD103988457 END*/
    public void sortCurrentList() {
        mFileViewListener.sortCurrentList(mFileSortHelper);
    }

    public boolean canShowCheckBox() {
		/*TYRD: weina 20150624 modity begin*/
        //return mConfirmOperationBar.getVisibility() != View.VISIBLE;
        return canShowCheckBox && (mConfirmOperationBar.getVisibility() == View.VISIBLE);
		/*TYRD: weina 20150624 modity end*/
    }
	/*TYRD: weina 20150624 modity begin*/
	public boolean isShowCheckBox() {

        return canShowCheckBox ;
		
    }

	public boolean isShowConfirmButtonBarVisible() {

        return mConfirmOperationBar.getVisibility() == View.VISIBLE; 
		
    }
	/*TYRD: weina 20150624 modity end*/	
    /*TYRD: weina 20150624 modity begin*/
    //private void showConfirmOperationBar(boolean show) {
    public void showConfirmOperationBar(boolean show) {
    /*TYRD: weina 20150624 modity end*/
        mConfirmOperationBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

	

    public void addContextMenuSelectedItem() {
        if (mCheckedFileNameList.size() == 0) {
            int pos = mListViewContextMenuSelectedItem;
            if (pos != -1) {
                FileInfo fileInfo = mFileViewListener.getItem(pos);
                if (fileInfo != null) {
                    mCheckedFileNameList.add(fileInfo);
                }
            }
        }
    }

    public ArrayList<FileInfo> getSelectedFileList() {
        return mCheckedFileNameList;
    }

    public boolean canPaste() {
        return mFileOperationHelper.canPaste();
    }

    // operation finish notification
    @Override
    public void onFinish() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        mFileViewListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showConfirmOperationBar(false);
                clearSelection();
                refreshFileList();
            }
        });
    }

    public FileInfo getItem(int pos) {
        return mFileViewListener.getItem(pos);
    }

    public boolean isInSelection() {
        return mCheckedFileNameList.size() > 0;
    }

    public boolean isMoveState() {
        return mFileOperationHelper.isMoveState() || mFileOperationHelper.canPaste();
    }

    public boolean isCopy(){
        return mFileOperationHelper.isCopy();
    }

    private void setup() {
        setupNaivgationBar();
        setupFileListView();
        setupOperationPane();
		setupOperationMenu();
    }

    private void setupNaivgationBar() {
        mNavigationBar = mFileViewListener.getViewById(R.id.navigation_bar);
        mNavigationBarText = (TextView) mFileViewListener.getViewById(R.id.current_path_view);
        mNavigationBarUpDownArrow = (ImageView) mFileViewListener.getViewById(R.id.path_pane_arrow);
        View clickable = mFileViewListener.getViewById(R.id.current_path_pane);
        clickable.setOnClickListener(buttonClick);
        //mCurrentPathBarText = (TextView) mFileViewListener.getViewById(R.id.current_path_drawable);

        mDropdownNavigation = mFileViewListener.getViewById(R.id.dropdown_navigation);

        setupClick(mNavigationBar, R.id.path_pane_up_level);
    }

    // buttons
    private void setupOperationPane() {
        mConfirmOperationBar = mFileViewListener.getViewById(R.id.moving_operation_bar);
        setupClick(mConfirmOperationBar, R.id.button_moving_confirm);
        setupClick(mConfirmOperationBar, R.id.button_moving_cancel);
    }

	private void setupOperationMenu(){
		mOperationMenuBar = mFileViewListener.getViewById(R.id.operatrion_menu_bar);
		setupClick(mOperationMenuBar, R.id.operatrion_menu_copy);
		setupClick(mOperationMenuBar, R.id.operatrion_menu_move);
		setupClick(mOperationMenuBar, R.id.operatrion_menu_delete);
        setupClick(mOperationMenuBar, R.id.operatrion_menu_more);
	}

	public void showOperationMenuBar(boolean show) {
        mOperationMenuBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
	
    public void showPopUpMenu( Context ctx,View view){
    	
        PopupMenu popupMenu = new PopupMenu(ctx, view);
        onCreateOptionsMenu(popupMenu.getMenu());
        onPrepareOptionsMenu(popupMenu.getMenu());
        popupMenu.show();
       
    }

	public void updateOperationMenuBar(boolean enabled){
        if (mOperationMenuBar.getVisibility() == View.GONE)
            return;
        copyMenuView= (ImageView) mOperationMenuBar.findViewById(R.id.operatrion_menu_copy);
        moveMenuView= (ImageView) mOperationMenuBar.findViewById(R.id.operatrion_menu_move);
        deleteMenuView= (ImageView) mOperationMenuBar.findViewById(R.id.operatrion_menu_delete);
		moreMenuView = (ImageView) mOperationMenuBar.findViewById(R.id.operatrion_menu_more);
		copyMenuView.setEnabled(enabled);
        moveMenuView.setEnabled(enabled);
        deleteMenuView.setEnabled(enabled);
		moreMenuView.setEnabled(enabled);
	}

    private void setupClick(View v, int id) {
        View button = (v != null ? v.findViewById(id) : mFileViewListener.getViewById(id));
        if (button != null)
            button.setOnClickListener(buttonClick);
    }

    private View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
               /* case R.id.button_operation_copy:
                    onOperationCopy();
                    break;
                case R.id.button_operation_move:
                    onOperationMove();
                    break;
                case R.id.button_operation_send:
                    onOperationSend();
                    break;
                case R.id.button_operation_delete:
                    onOperationDelete();
                    break;
                case R.id.button_operation_cancel:
                    onOperationSelectAllOrCancel();
                    break;*/
               /**/
			    case R.id.operatrion_menu_copy:
                    canShowCheckBox= true;
					isCopyOperation = true;
					isDelOperation=isCutOperation= false;//TYRD: weina 20150820 add for PROD103990596
					
					getActionMode();
					showConfirmOperationBar(true);
					refreshFileList();
                    break;
                case R.id.operatrion_menu_move:
                    canShowCheckBox= true;
					isCutOperation = true;
					isDelOperation=isCopyOperation= false;//TYRD: weina 20150820 add for PROD103990596
					getActionMode();
					showConfirmOperationBar(true);
					refreshFileList();
                    break;
                case R.id.operatrion_menu_delete:
                    canShowCheckBox= true;
					isDelOperation = true;
					isCopyOperation=isCutOperation= false;//TYRD: weina 20150820 add for PROD103990596
					getActionMode();
					showConfirmOperationBar(true);
					refreshFileList();
                    break;
				case R.id.operatrion_menu_more:
                    showPopUpMenu(mContext, v);
                    break;	
			   /**/
                case R.id.current_path_pane:
                    onNavigationBarClick();
                    break;
                case R.id.button_moving_confirm:
					/*TYRD: weina 20150624 modity  begin*/
                    //onOperationButtonConfirm();
                    if(isCopyOperation){
						canShowCheckBox= false;
						isCopyOperation  =false;
						((FileViewActivity) ((FileExplorerTabActivity) mContext).getFragment(Util.SDCARD_TAB_INDEX))
							.copyFile(getSelectedFileList());
						if(((FileExplorerTabActivity)mContext).getActionMode()!=null){ 
							((FileExplorerTabActivity)mContext).getActionMode().finish();
						}
						ActionBar bar =((FileExplorerTabActivity)mContext).getActionBar();
						if(bar.getSelectedNavigationIndex()!=Util.SDCARD_TAB_INDEX){
                            bar.setSelectedNavigationItem (Util.SDCARD_TAB_INDEX);
						}else{
						    mCurrentPath = "/storage";
						} 
						
						showConfirmOperationBar(true);
						refreshFileList();
						/*Button confirmButton = (Button)mConfirmOperationBar.findViewById(R.id.button_moving_confirm);
						int text = R.string.confirm;
						confirmButton.setEnabled(true);
						confirmButton.setText(text);*/
						
					}else if(isCutOperation){
					    canShowCheckBox= false;
						isCutOperation  =false;
						
						((FileViewActivity)((FileExplorerTabActivity) mContext)
							.getFragment(Util.SDCARD_TAB_INDEX))
							.moveToFile(getSelectedFileList());
						ActionBar bar =((FileExplorerTabActivity)mContext).getActionBar();
						if(bar.getSelectedNavigationIndex()!=Util.SDCARD_TAB_INDEX){
                            bar.setSelectedNavigationItem (Util.SDCARD_TAB_INDEX);
						}else{
						    mCurrentPath = "/storage";
						}
						
						if(((FileExplorerTabActivity)mContext).getActionMode()!=null){
							((FileExplorerTabActivity)mContext).getActionMode().finish();
						}
						
						showConfirmOperationBar(true);
						refreshFileList();
						/*Button confirmButton = (Button)mConfirmOperationBar.findViewById(R.id.button_moving_confirm);
						int text = R.string.confirm;
						confirmButton.setEnabled(true);
						confirmButton.setText(text);*/
						

					}else if(isDelOperation){
					    canShowCheckBox= false;
						isDelOperation  =false;
						onOperationDelete();
						if(((FileExplorerTabActivity)mContext).getActionMode()!=null){
							((FileExplorerTabActivity)mContext).getActionMode().finish();
						}
					}else{
                         onOperationButtonConfirm();
					}
                    /*TYRD: weina 20150624 modity  end*/
                    break;
                case R.id.button_moving_cancel:
					 /*TYRD: weina 20150624 modity  begin*/
					 if(isCopyOperation||isCutOperation||isDelOperation){
					 	isCopyOperation=isCutOperation=isDelOperation;
					 }
					 canShowCheckBox= false;
					 
					 
                    onOperationButtonCancel();
					if(((FileExplorerTabActivity)mContext).getActionMode()!=null){
					 	
					 	((FileExplorerTabActivity)mContext).getActionMode().finish();
						
						
					 }
					 
					 /*TYRD: weina 20150624 modity  end*/
                    break;
                case R.id.path_pane_up_level:
                    onOperationUpLevel();
                    ActionMode mode = ((FileExplorerTabActivity) mContext).getActionMode();
                    if (mode != null) {
                        mode.finish();
                    }
                    break;
            }
        }

    };

    private void onOperationReferesh() {
        refreshFileList();
    }

    private void onOperationFavorite() {
        String path = mCurrentPath;

        if (mListViewContextMenuSelectedItem != -1) {
            path = mFileViewListener.getItem(mListViewContextMenuSelectedItem).filePath;
        }

        onOperationFavorite(path);
    }

    private void onOperationSetting() {
        Intent intent = new Intent(mContext, FileExplorerPreferenceActivity.class);
        if (intent != null) {
            try {
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(LOG_TAG, "fail to start setting: " + e.toString());
            }
        }
    }

    private void onOperationFavorite(String path) {
        FavoriteDatabaseHelper databaseHelper = new FavoriteDatabaseHelper(mContext);//FavoriteDatabaseHelper.getInstance();
        databaseHelper.setOnFavoriteDatabaseListener(new FavoriteDatabaseHelper.FavoriteDatabaseListener() {
            @Override
            public void onFavoriteDatabaseChanged() {
                Intent intent = new Intent("com.action.UPDATE_FAVARITE_CATEGORY");
				mContext.sendBroadcast(intent);
            }
        });
        if (databaseHelper != null) {
            int stringId = 0;
            if (databaseHelper.isFavorite(path)) {
                databaseHelper.delete(path);
                stringId = R.string.removed_favorite;
            } else {
			   /*TYRD: weina 20150624 modity begin*/
                if(path.equals("/storage/emulated/0")){
					databaseHelper.insert(mContext.getResources().getString(R.string.sd_folder), path);
				}else{
                    databaseHelper.insert(Util.getNameFromFilepath(path), path);
				}
				/*TYRD: weina 20150624 modity end*/
                stringId = R.string.added_favorite;
            }

            Toast.makeText(mContext, stringId, Toast.LENGTH_SHORT).show();
        }
        refreshFileList();
    }

    private void onOperationShowSysFiles() {
        Settings.instance().setShowDotAndHiddenFiles(!Settings.instance().getShowDotAndHiddenFiles());
       
		refreshFileList();
    }

    public void onOperationSelectAllOrCancel() {
        if (!isSelectedAll()) {
            onOperationSelectAll();
        } else {
            clearSelection();
        }
    }

    public void onOperationSelectAll() {
        mCheckedFileNameList.clear();
        for (FileInfo f : mFileViewListener.getAllFiles()) {
            f.Selected = true;
            mCheckedFileNameList.add(f);
        }
        FileExplorerTabActivity fileExplorerTabActivity = (FileExplorerTabActivity) mContext;
        ActionMode mode = fileExplorerTabActivity.getActionMode();
        if (mode == null) {
            mode = fileExplorerTabActivity.startActionMode(new ModeCallback(mContext, this));
            fileExplorerTabActivity.setActionMode(mode);
            Util.updateActionModeTitle(mode, mContext, getSelectedFileList().size());
        }
		
        mFileViewListener.onDataChanged();
    }

    private OnClickListener navigationClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            String path = (String) v.getTag();
            //baitao 2016.02.15 mod begin
            //String platform = SystemProperties.get("ro.seccap.platform");
           // if (!TextUtils.isEmpty(platform) && platform.contains("QRD")) {
           if(true){
                path = path.replace(mContext.getResources().getString(R.string.storage_display),"storage");//TYRD: weina 20150624 add 
                assert (path != null);
                //showDropdownNavigation(false);
                /*TYRD: weina 20150901 add begin*/
                if(("/"+mContext.getResources().getString(R.string.tab_category)).equals(path)){
                    canShowCheckBox=false;
                    }
                    /*TYRD: weina 20150901  add end*/
                    if (mFileViewListener.onNavigation(path))
                        return;

                    if(path.isEmpty()){
                        mCurrentPath = mRoot;
                    }
                    /*TYRD: weina 20150624 add begin*/
                    else if("/storage/emulated".equals(path)){
                        return;
                    }
                    /*TYRD: weina 20150624 add  end*/
                    else{
                        mCurrentPath = path;
                    }
            } else {
                assert (path != null);
                //showDropdownNavigation(false);
                if (mFileViewListener.onNavigation(path))
                    return;

                if(path.isEmpty()){
                    mCurrentPath = mRoot;
                } else{
                    mCurrentPath = path;
                }
            }
            //baitao 2016.02.16 mod end
            refreshFileList();
        }

    };

    protected void onNavigationBarClick() {
        /*if (mDropdownNavigation.getVisibility() == View.VISIBLE) {
            showDropdownNavigation(false);
        } else {*/
			//storge_path
			//scroll_id
			/*TYRD: weina 20150624 modity begin*/
			TextView mTextView = (TextView)mDropdownNavigation.findViewById(R.id.storge_path);
	        mScrollView = (HorizontalScrollView)mDropdownNavigation.findViewById(R.id.scroll_id);
            LinearLayout list = (LinearLayout) mScrollView.findViewById(R.id.dropdown_navigation_list);
            list.removeAllViews();
            int pos = 1;
			String displayPath = mFileViewListener.getDisplayPath(mCurrentPath)+"/";
            boolean root = true;
            int left = 0;
			if(mCurrentPath.equals("/")){
				displayPath = "/"+mContext.getResources().getString(R.string.tab_category)+"/";
			}
			//if(mCurrentPath.startsWith(Util.getSdDirectory())){
			if(mCurrentPath.startsWith("/storage")){
				//displayPath = displayPath.replace(Util.getSdDirectory(),"/"+mContext.getResources().getString(R.string.storage_display));
				//displayPath = displayPath.replace(Util.getSdDirectory(),"");
				displayPath =displayPath.replace("storage",mContext.getResources().getString(R.string.storage_display));
				mTextView.setText(mContext.getResources().getString(R.string.storage_display));
				mTextView.setTag(mFileViewListener.getRealPath("/"+mContext.getResources().getString(R.string.storage_display)));
				mTextView.setOnClickListener(navigationClick);
			}
			/*TYRD: weina 20150624 modity end*/
			while (pos != -1 && !displayPath.equals("/")) {//如果当前位置在根文件夹则不显示导航条
                int end = displayPath.indexOf("/", pos);
                if (end == -1)
                    break;

                View listItem = LayoutInflater.from(mContext).inflate(R.layout.dropdown_item,
                        null);

                /*View listContent = listItem.findViewById(R.id.list_item);
                listContent.setPadding(left, 0, 0, 0);
                left += 20;
                ImageView img = (ImageView) listItem.findViewById(R.id.item_icon);

                img.setImageResource(root ? R.drawable.dropdown_icon_root : R.drawable.dropdown_icon_folder);
                root = false;*/

                TextView text = (TextView) listItem.findViewById(R.id.path_name);
                String substring = displayPath.substring(pos, end);
                //if(substring.isEmpty())substring = "/";
                String str = mFileViewListener.getDisplayPath(substring);
				
                text.setText(str);
				

                listItem.setOnClickListener(navigationClick);
                listItem.setTag(mFileViewListener.getRealPath(displayPath.substring(0, end)));
				
                pos = end + 1;
				/*TYRD: weina 20150624 modity begin*/
				if(! mContext.getResources().getString(R.string.storage_display).equals(substring)){
					list.addView(listItem);
				}
                /*TYRD: weina 20150624 modity end*/
            }
			/*TYRD: weina 20150624 modity begin*/
			mDropdownNavigation.post(new Runnable() {   
                public void run() {  
                    mScrollView.scrollTo(1000, 0);  
                }   
            }); 
			/*TYRD: weina 20150624 modity end*/	
            //if (list.getChildCount() > 0)
            //    showDropdownNavigation(true);

        //}
    }

    public boolean onOperationUpLevel() {
        //showDropdownNavigation(false);

        if (mFileViewListener.onOperation(GlobalConsts.OPERATION_UP_LEVEL)) {
			showOperationMenuBar(false);
            return true;
        }
        if (!mRoot.equals(mCurrentPath)) {
            mCurrentPath = new File(mCurrentPath).getParent();
            refreshFileList();
            return true;
        }

        return false;
    }

    public void onOperationCreateFolder() {
        TextInputDialog dialog = new TextInputDialog(mContext, mContext.getString(
                R.string.operation_create_folder), mContext.getString(R.string.operation_create_folder_message),
                mContext.getString(R.string.new_folder_name), new OnFinishListener() {
                    @Override
                    public boolean onFinish(String text) {
                        return doCreateFolder(text);
                    }
                });

       // dialog.show();/*TYRD: weina 20150624 deleted */
    }

    private boolean doCreateFolder(String text) {
        if (TextUtils.isEmpty(text))
            return false;

        if (mFileOperationHelper.CreateFolder(mCurrentPath, text)) {
            mFileViewListener.addSingleFile(Util.GetFileInfo(Util.makePath(mCurrentPath, text)));
            mFileListView.setSelection(mFileListView.getCount() - 1);
			refreshFileList();//TYRD: weina 20150624 ADD
        } else {
            /*TYRD: weina 20150624 add begin*/
            //new AlertDialog.Builder(mContext).setMessage(mContext.getString(R.string.fail_to_create_folder))
            //        .setPositiveButton(R.string.confirm, null).create().show();

			final AlertDialog.Builder dialog= new AlertDialog.Builder(mContext);
            View alertDialogLayout = LayoutInflater.from(mContext).inflate(R.layout.ty_no_title_alertdialog,null);
            dialog.setView(alertDialogLayout);
            TextView messageView = (TextView)alertDialogLayout.findViewById(R.id.message);
            messageView.setText(mContext.getString(R.string.fail_to_create_folder));
            Button positiveButton = (Button)alertDialogLayout.findViewById(R.id.button1);
            Button cancleButton = (Button)alertDialogLayout.findViewById(R.id.button3);
            cancleButton.setVisibility(View.GONE);
            positiveButton.setText(R.string.confirm);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
    				dismissDialog();
                }
    		});
            dialog.setView(alertDialogLayout);
    		alertDialog = dialog.create();
    		alertDialog.show();
            /*TYRD: weina 20150624 add end*/
            return false;
        }

        return true;
    }

    public void onOperationSearch() {

    }

    public void onSortChanged(SortMethod s) {
        if (mFileSortHelper.getSortMethod() != s) {
            mFileSortHelper.setSortMethog(s);
            sortCurrentList();
        }
    }

    public void onOperationCopy() {
        onOperationCopy(getSelectedFileList());
    }

    public void onOperationCopy(ArrayList<FileInfo> files) {
        mFileOperationHelper.Copy(files);
        clearSelection();
        showConfirmOperationBar(true);
        View confirmButton = mConfirmOperationBar.findViewById(R.id.button_moving_confirm);
        confirmButton.setEnabled(false);
        // refresh to hide selected files
        refreshFileList();
    }

    public void onOperationCopyPath() {
        if (getSelectedFileList().size() == 1) {
            copy(getSelectedFileList().get(0).filePath);
        }
        clearSelection();
    }

    private void copy(CharSequence text) {
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(
                Context.CLIPBOARD_SERVICE);
        cm.setText(text);
    }

    private void onOperationPaste() {
        mFileViewListener.cancelRefreshAsyncTask();//add by chen he 2016.02.22
        if (mFileOperationHelper.Paste(mCurrentPath)) {
			/*TYRD: weina 20150824 add for PROD103988457 BEGIN*/
			//showProgress(mContext.getString(R.string.operation_pasting));
            showCopyProgress(mContext.getString(R.string.operation_pasting));
			/*TYRD: weina 20150824 add for PROD103988457 END*/
        }/*TYRD: weina 20151021 add for PROD104074267 begin*/
		else{
			mFileOperationHelper.clear();//baitao 2016.01.28 ad
            showConfirmOperationBar(false);
            clearSelection();
            refreshFileList();
		}
		/*TYRD: weina 20151021 add for PROD104074267 END*/
    }

    public void onOperationMove() {
        mFileOperationHelper.StartMove(getSelectedFileList());
        clearSelection();
        showConfirmOperationBar(true);
        View confirmButton = mConfirmOperationBar.findViewById(R.id.button_moving_confirm);
        confirmButton.setEnabled(false);
        // refresh to hide selected files
        refreshFileList();
    }

    public void refreshFileList() {
        clearSelection();
        updateNavigationPane();
         
        // onRefreshFileList returns true indicates list has changed
 		if(isShowConfirmButtonBarVisible()||"/storage".equals(mCurrentPath)||"/".equals(mCurrentPath)){
			showOperationMenuBar(false);
		}else{
            showOperationMenuBar(true);
		}
		//updateOperationMenuBar();
		mFileViewListener.onRefreshFileList(mCurrentPath, mFileSortHelper);
        // update move operation button state
        updateConfirmButtons();
        onNavigationBarClick();
		
    }
   /*TYRD: weina 20150624 modity begin*/
   // private void updateConfirmButtons() {
   public void updateConfirmButtons() {
   /*TYRD: weina 20150624 modity end*/
        if (mConfirmOperationBar.getVisibility() == View.GONE)
            return;

        Button confirmButton = (Button) mConfirmOperationBar.findViewById(R.id.button_moving_confirm);
        int text = R.string.operation_paste;
		/*TYRD:weina 20150624 add begin*/
		if(isCopyOperation){
			text = R.string.operation_copy;
		}else if(isCutOperation){
		    text = R.string.operation_move;
		}else if(isDelOperation){
		    text = R.string.operation_delete;
		}
		/*TYRD:weina 20150624 add end*/
        if (isSelectingFiles()) {
            confirmButton.setEnabled(mCheckedFileNameList.size() != 0);
            text = R.string.operation_send;
        } else if (isMoveState()) {
            confirmButton.setEnabled(mFileOperationHelper.canMove(mCurrentPath));
        }
		/*TYRD: weina 20150624 add begin*/
		else {
			if(mCheckedFileNameList.size()!=0){
				if("/storage".equals(mCurrentPath)){
					confirmButton.setEnabled(false);
				}else{
				    confirmButton.setEnabled(true);
				}
			}else{
			    confirmButton.setEnabled(false);
			}
		}
		/*TYRD: weina 20150624 add  end*/

        confirmButton.setText(text);
    }

    private void updateNavigationPane() {
        /*View upLevel = mFileViewListener.getViewById(R.id.path_pane_up_level);
        upLevel.setVisibility(mRoot.equals(mCurrentPath) ? View.INVISIBLE : View.VISIBLE);

        View arrow = mFileViewListener.getViewById(R.id.path_pane_arrow);
        arrow.setVisibility(mRoot.equals(mCurrentPath) ? View.GONE : View.VISIBLE);*/
        mNavigationBarText.setText(mFileViewListener.getDisplayPath(mCurrentPath));
    }

    public void onOperationSend() {
        ArrayList<FileInfo> selectedFileList = getSelectedFileList();
        for (FileInfo f : selectedFileList) {
            if (f.IsDir) {
				/*TYRD: weina 20150624 add begin*/
                //AlertDialog dialog = new AlertDialog.Builder(mContext).setMessage(
                //        R.string.error_info_cant_send_folder).setPositiveButton(R.string.confirm, null).create();
                //dialog.show();
                final AlertDialog.Builder dialog= new AlertDialog.Builder(mContext);
                View alertDialogLayout = LayoutInflater.from(mContext).inflate(R.layout.ty_no_title_alertdialog,null);
                dialog.setView(alertDialogLayout);
                TextView messageView = (TextView)alertDialogLayout.findViewById(R.id.message);
                messageView.setText(mContext.getString(R.string.error_info_cant_send_folder));
                Button positiveButton = (Button)alertDialogLayout.findViewById(R.id.button1);
                Button cancleButton = (Button)alertDialogLayout.findViewById(R.id.button3);
                cancleButton.setVisibility(View.GONE);
                positiveButton.setText(R.string.confirm);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
        				dismissDialog();
                    }
        		});
                dialog.setView(alertDialogLayout);
        		alertDialog = dialog.create();
        		alertDialog.show();
                /*TYRD: weina 20150624 add end*/
			
                refreshFileList();
                return;
            }
        }

        Intent intent = IntentBuilder.buildSendFile(selectedFileList);
        if (intent != null) {
            try {
                mFileViewListener.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(LOG_TAG, "fail to view file: " + e.toString());
            }
        }
        clearSelection();
    }

    public void onOperationRename() {
        int pos = mListViewContextMenuSelectedItem;
        if (pos == -1)
            return;

        if (getSelectedFileList().size() == 0)
            return;

        final FileInfo f = getSelectedFileList().get(0);
        clearSelection();

        TextInputDialog dialog = new TextInputDialog(mContext, mContext.getString(R.string.operation_rename),
                mContext.getString(R.string.operation_rename_message), f.fileName, new OnFinishListener() {
                    @Override
                    public boolean onFinish(String text) {
                        return doRename(f, text);
                    }

                });

       // dialog.show();//TYRD: weina 20150624 deleted
    }

    private boolean doRename(final FileInfo f, String text) {
        if (TextUtils.isEmpty(text))
            return false;

        if (mFileOperationHelper.Rename(f, text)) {
            f.fileName = text;
            mFileViewListener.onDataChanged();
        } else {
            /*TYRD: weina 20150624 add begin*/
            //new AlertDialog.Builder(mContext).setMessage(mContext.getString(R.string.fail_to_rename))
            //        .setPositiveButton(R.string.confirm, null).create().show();
            final AlertDialog.Builder dialog= new AlertDialog.Builder(mContext);
            View alertDialogLayout = LayoutInflater.from(mContext).inflate(R.layout.ty_no_title_alertdialog,null);
            dialog.setView(alertDialogLayout);
            TextView messageView = (TextView)alertDialogLayout.findViewById(R.id.message);
            messageView.setText(mContext.getString(R.string.fail_to_rename));
            Button positiveButton = (Button)alertDialogLayout.findViewById(R.id.button1);
            Button cancleButton = (Button)alertDialogLayout.findViewById(R.id.button3);
            cancleButton.setVisibility(View.GONE);
            positiveButton.setText(R.string.confirm);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
    				dismissDialog();
                }
    		});
            dialog.setView(alertDialogLayout);
    		alertDialog = dialog.create();
    		alertDialog.show();
			/*TYRD: weina 20150624 add end*/
            return false;
        }

        return true;
    }

    private void notifyFileSystemChanged(String path) {
        if (path == null)
            return;
        final File f = new File(path);
        final Intent intent;
        /*if (f.isDirectory()) {
            intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
            intent.setClassName("com.android.providers.media", "com.android.providers.media.MediaScannerReceiver");
            intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
            Log.v(LOG_TAG, "directory changed, send broadcast:" + intent.toString());
        } else {*/
			/*TYRD: weina 20150624 modity begin*/
            //intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
           // intent.setData(Uri.fromFile(new File(path)));
           intent = new Intent(ACTION_MEDIA_SCANNER_SCAN_ALL,Uri.fromFile(new File(path)));
           /*TYRD: weina 20150624 modity end*/
            Log.v(LOG_TAG, "file changed, send broadcast:" + intent.toString());
        //}
        mContext.sendBroadcast(intent);
    }

    public void onOperationDelete() {
        doOperationDelete(getSelectedFileList());
    }

    public void onOperationDelete(int position) {
        FileInfo file = mFileViewListener.getItem(position);
        if (file == null)
            return;

        ArrayList<FileInfo> selectedFileList = new ArrayList<FileInfo>();
        selectedFileList.add(file);
        doOperationDelete(selectedFileList);
    }

    private void doOperationDelete(final ArrayList<FileInfo> selectedFileList) {
        final ArrayList<FileInfo> selectedFiles = new ArrayList<FileInfo>(selectedFileList);
		/*TYRD: weina 20150624 modity begin*/
       // Dialog dialog 
        AlertDialog.Builder dialog= new AlertDialog.Builder(mContext);
        View alertDialogLayout = LayoutInflater.from(mContext).inflate(R.layout.ty_no_title_alertdialog,null);
        dialog.setView(alertDialogLayout);
        TextView messageView = (TextView)alertDialogLayout.findViewById(R.id.message);
        messageView.setText(mContext.getString(R.string.operation_delete_confirm_message));
        Button positiveButton = (Button)alertDialogLayout.findViewById(R.id.button1);
        Button cancleButton = (Button)alertDialogLayout.findViewById(R.id.button3);
        positiveButton.setText(R.string.confirm);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				mFileViewListener.cancelRefreshAsyncTask();//add by chen he 2016.02.22
                if (mFileOperationHelper.Delete(selectedFiles)) {
                    showProgress(mContext.getString(R.string.operation_deleting));
                }
                clearSelection();
				dismissDialog();
            }
		});
        
        cancleButton.setText(R.string.cancel);
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelection();
				dismissDialog();
            }
        });
        dialog//.setMessage(mContext.getString(R.string.operation_delete_confirm_message))
              .setView(alertDialogLayout);
		alertDialog = dialog.create();
             /* .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (mFileOperationHelper.Delete(selectedFiles)) {
                            showProgress(mContext.getString(R.string.operation_deleting));
                        }
                        clearSelection();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearSelection();
                    }
                }).create();*/
		alertDialog.show();
		/*TYRD: weina 20150624 modity end*/
    }

    public void onOperationInfo() {
        if (getSelectedFileList().size() == 0)
            return;

        FileInfo file = getSelectedFileList().get(0);
        if (file == null)
            return;

        InformationDialog dialog = new InformationDialog(mContext, file, mFileViewListener
                .getFileIconHelper());
        //dialog.show();
        clearSelection();
    }

    public void onOperationButtonConfirm() {
        if (isSelectingFiles()) {
            mSelectFilesCallback.selected(mCheckedFileNameList);
            mSelectFilesCallback = null;
            clearSelection();
        } else if (mFileOperationHelper.isMoveState()) {
            if (mFileOperationHelper.EndMove(mCurrentPath)) {
                showProgress(mContext.getString(R.string.operation_moving));
            } else {
				mFileOperationHelper.clear();
            	showConfirmOperationBar(false);
            	clearSelection();
            	refreshFileList();
			}
        } else {
            onOperationPaste();
        }
    }

    public void onOperationButtonCancel() {
        mFileOperationHelper.clear();
        showConfirmOperationBar(false);
        if (isSelectingFiles()) {
            mSelectFilesCallback.selected(null);
            mSelectFilesCallback = null;
            clearSelection();
        } else if (mFileOperationHelper.isMoveState()) {
            // refresh to show previously selected hidden files
            mFileOperationHelper.EndMove(null);
            refreshFileList();
        } else {
            refreshFileList();
        }
    }

    // context menu
	/*TYRD: weina 20150624 modity begin*/
	// private OnCreateContextMenuListener mListViewContextMenuListener = new OnCreateContextMenuListener() {
    public OnCreateContextMenuListener mListViewContextMenuListener = new OnCreateContextMenuListener() {
	/*TYRD: weina 20150624 modity end*/
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (isInSelection() || isMoveState()||"/storage".equals(mCurrentPath))
                return;
            
            //showDropdownNavigation(false);
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

            FavoriteDatabaseHelper databaseHelper = new FavoriteDatabaseHelper(mContext);//FavoriteDatabaseHelper.getInstance();
            FileInfo file = mFileViewListener.getItem(info.position);
            if (databaseHelper != null && file != null) {
                int stringId = databaseHelper.isFavorite(file.filePath) ? R.string.operation_unfavorite
                        : R.string.operation_favorite;
                addMenuItem(menu, GlobalConsts.MENU_FAVORITE, 0, stringId);
                databaseHelper =null;
            }
            addMenuItem(menu, GlobalConsts.MENU_COPY, 0, R.string.operation_copy);
            addMenuItem(menu, GlobalConsts.MENU_COPY_PATH, 0, R.string.operation_copy_path);
            // addMenuItem(menu, GlobalConsts.MENU_PASTE, 0,
            // R.string.operation_paste);
            addMenuItem(menu, GlobalConsts.MENU_MOVE, 0, R.string.operation_move);
            addMenuItem(menu, MENU_SEND, 0, R.string.operation_send);
            addMenuItem(menu, MENU_RENAME, 0, R.string.operation_rename);
            addMenuItem(menu, MENU_DELETE, 0, R.string.operation_delete);
            addMenuItem(menu, MENU_INFO, 0, R.string.operation_info);
			/*TYRD: weina 20150624 add begin*/
			/*if(file!=null &&!file.IsDir &&(MediaFile.getFileType(file.filePath)!=null)&& MediaFile.isAudioFileType(MediaFile.getFileType(file.filePath).fileType)){
			    addMenuItem(menu, MENU_SET_RING_TONE, 0, R.string.set_ring_tone);
			}*/
			/*TYRD: weina 20150624 add end*/
            if (!canPaste()) {
                MenuItem menuItem = menu.findItem(GlobalConsts.MENU_PASTE);
                if (menuItem != null)
                    menuItem.setEnabled(false);
            }
        }
    };

    // File List view setup
    private ListView mFileListView;

    private int mListViewContextMenuSelectedItem;

    private void setupFileListView() {
        mFileListView = (ListView) mFileViewListener.getViewById(R.id.file_path_list);
        mFileListView.setLongClickable(true);
        mFileListView.setOnCreateContextMenuListener(mListViewContextMenuListener);
        mFileListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(parent, view, position, id);
            }
        });
    }

    // menu
    private static final int MENU_SEARCH = 1;

    // private static final int MENU_NEW_FOLDER = 2;
    //private static final int MENU_SORT = 3; //TYRD: weina 20150624 deleted

    private static final int MENU_SEND = 7;

    private static final int MENU_RENAME = 8;

    private static final int MENU_DELETE = 9;

    private static final int MENU_INFO = 10;

    private static final int MENU_SORT_NAME = 11;

    private static final int MENU_SORT_SIZE = 12;

    private static final int MENU_SORT_DATE = 13;

    private static final int MENU_SORT_TYPE = 14;

    private static final int MENU_REFRESH = 15;

    private static final int MENU_SELECTALL = 16;

    private static final int MENU_SETTING = 17;

    private static final int MENU_EXIT = 18;
	/*TYRD: weina 20150624 add begin*/
	private static final int MENU_SET_RING_TONE = 22;
	/*TYRD: weina 20150624 add end*/

    private OnMenuItemClickListener menuItemClick = new OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            mListViewContextMenuSelectedItem = info != null ? info.position : -1;
            
            int itemId = item.getItemId();
			if (mFileViewListener.onOperation(itemId)) {
                return true;
            }

            addContextMenuSelectedItem();

            switch (itemId) {
                case MENU_SEARCH:
                    onOperationSearch();
                    break;
                case GlobalConsts.MENU_NEW_FOLDER:
                    onOperationCreateFolder();
                    break;
                case MENU_REFRESH:
                    onOperationReferesh();
                    break;
				/*TYRD:weina 20150624 deleted begin*/
                /*case MENU_SELECTALL:
                    onOperationSelectAllOrCancel();
                    break;*/
                /*TYRD:weina 20150624 deleted end*/
                case GlobalConsts.MENU_SHOWHIDE:
                    onOperationShowSysFiles();
                    break;
                case GlobalConsts.MENU_FAVORITE:
                    onOperationFavorite();
                    break;
                case MENU_SETTING:
                    onOperationSetting();
                    break;
				/*TYRD:weina 20150624 deleted begin*/
                /*case MENU_EXIT:
                    ((FileExplorerTabActivity) mContext).finish();
                    break;*/
                /*TYRD:weina 20150624 deleted end*/
                // sort
                case MENU_SORT_NAME:
                    item.setChecked(true);
                    onSortChanged(SortMethod.name);
                    break;
                case MENU_SORT_SIZE:
                    item.setChecked(true);
                    onSortChanged(SortMethod.size);
                    break;
                case MENU_SORT_DATE:
                    item.setChecked(true);
                    onSortChanged(SortMethod.date);
                    break;
                case MENU_SORT_TYPE:
                    item.setChecked(true);
                    onSortChanged(SortMethod.type);
                    break;

                case GlobalConsts.MENU_COPY:
                    onOperationCopy();
                    break;
                case GlobalConsts.MENU_COPY_PATH:
                    onOperationCopyPath();
                    break;
                case GlobalConsts.MENU_PASTE:
                    onOperationPaste();
                    break;
                case GlobalConsts.MENU_MOVE:
                    onOperationMove();
                    break;
                case MENU_SEND:
                    onOperationSend();
                    break;
                case MENU_RENAME:
                    onOperationRename();
                    break;
                case MENU_DELETE:
                    onOperationDelete();
                    break;
                case MENU_INFO:
                    onOperationInfo();
                    break;
				/*TYRD: weina 20150624 add begin*/
                case GlobalConsts.MENU_MUTI_COPY:
                    canShowCheckBox= true;
					isCopyOperation = true;
					isDelOperation=isCutOperation= false;//TYRD: weina 20150820 add for PROD103990596
					((FileExplorerTabActivity) mContext).getWindow();
					getActionMode();
					showConfirmOperationBar(true);
					refreshFileList();
                    break;
                case GlobalConsts.MENU_MUTI_CUT:
                    canShowCheckBox= true;
					isCutOperation = true;
					isDelOperation=isCopyOperation= false;//TYRD: weina 20150820 add for PROD103990596
					getActionMode();
					showConfirmOperationBar(true);
					refreshFileList();
                    break;
                case GlobalConsts.MENU_MUTI_DEL:
                    canShowCheckBox= true;
					isDelOperation = true;
					isCopyOperation=isCutOperation= false;//TYRD: weina 20150820 add for PROD103990596
					getActionMode();
					showConfirmOperationBar(true);
					refreshFileList();
                    break;	
				case MENU_SET_RING_TONE:
					AudioManager aduioManager=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
					if(aduioManager.getRingerMode()==AudioManager.RINGER_MODE_SILENT){
						Toast.makeText(mContext, R.string.ringtone_invalid_in_silent, Toast.LENGTH_SHORT).show();
					}else{
					   if(mListViewContextMenuSelectedItem !=-1){
					       FileInfo fileInfo = mFileViewListener.getItem(mListViewContextMenuSelectedItem);
						   if(fileInfo!=null){
						   	   new SetBellDialog(mContext,fileInfo.filePath).showDialog();
						   }
					   }
						
					}
					refreshFileList();
					break;
				/*TYRD: weina 20150624 add end*/
                default:
                    return false;
            }

            mListViewContextMenuSelectedItem = -1;
            return true;
        }

    };

    private com.realfame.fileexplorer.FileViewInteractionHub.Mode mCurrentMode;

    private String mCurrentPath;

    private String mRoot;

    private SelectFilesCallback mSelectFilesCallback;

    public boolean onCreateOptionsMenu(Menu menu) {
        clearSelection();
        //showDropdownNavigation(false);

        // menu.add(0, MENU_SEARCH, 0,
        // R.string.menu_item_search).setOnMenuItemClickListener(
        // menuItemClick);
        /*TYRD: weina 20150624 delete begin*/
		/*
        addMenuItem(menu, MENU_SELECTALL, 0, R.string.operation_selectall,
                R.drawable.ic_menu_select_all);*/
        /*TYRD: weina 20150624 delete end*/  
        SubMenu sortMenu = menu.addSubMenu(0, GlobalConsts.MENU_SORT, 1, R.string.menu_item_sort).setIcon(
                R.drawable.ic_menu_sort);
        addMenuItem(sortMenu, MENU_SORT_NAME, 0, R.string.menu_item_sort_name);
        addMenuItem(sortMenu, MENU_SORT_SIZE, 1, R.string.menu_item_sort_size);
        addMenuItem(sortMenu, MENU_SORT_DATE, 2, R.string.menu_item_sort_date);
        addMenuItem(sortMenu, MENU_SORT_TYPE, 3, R.string.menu_item_sort_type);
        sortMenu.setGroupCheckable(0, true, true);
        sortMenu.getItem(0).setChecked(true);

        // addMenuItem(menu, GlobalConsts.MENU_PASTE, 2,
        // R.string.operation_paste);
        addMenuItem(menu, GlobalConsts.MENU_NEW_FOLDER, 3, R.string.operation_create_folder,
                R.drawable.ic_menu_new_folder);
        addMenuItem(menu, GlobalConsts.MENU_FAVORITE, 4, R.string.operation_favorite,
                R.drawable.ic_menu_delete_favorite);
        addMenuItem(menu, GlobalConsts.MENU_SHOWHIDE, 5, R.string.operation_show_sys,
                R.drawable.ic_menu_show_sys);
        addMenuItem(menu, MENU_REFRESH, 6, R.string.operation_refresh,
                R.drawable.ic_menu_refresh);
		/*TYRD: weina 20150624 modify begin*/		
        //addMenuItem(menu, MENU_SETTING, 7, R.string.menu_setting, drawable.ic_menu_preferences);
        //addMenuItem(menu, MENU_EXIT, 8, R.string.menu_exit, drawable.ic_menu_close_clear_cancel);// /*TYRD: weina 20150624 delete */
        //addMenuItem(menu,GlobalConsts.MENU_MUTI_COPY, 9, R.string.operation_copy,R.drawable.operation_button_copy);
       // addMenuItem(menu, GlobalConsts.MENU_MUTI_CUT, 10, R.string.operation_move,R.drawable.operation_button_move);
      //  addMenuItem(menu, GlobalConsts.MENU_MUTI_DEL, 11, R.string.operation_delete, R.drawable.operation_button_delete);
		/*TYRD: weina 20150624 modify end*/
        return true;
    }

    private void addMenuItem(Menu menu, int itemId, int order, int string) {
        addMenuItem(menu, itemId, order, string, -1);
    }

    private void addMenuItem(Menu menu, int itemId, int order, int string, int iconRes) {
        if (!mFileViewListener.shouldHideMenu(itemId)) {
            MenuItem item = menu.add(0, itemId, order, string).setOnMenuItemClickListener(menuItemClick);
			
            if (iconRes > 0) {
                item.setIcon(iconRes);
            }
			//item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			//item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
		
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenuItems(menu);
        return true;
    }

    private void updateMenuItems(Menu menu) {
		/*TYRD: weina 20150624 delete begin*/
       /* menu.findItem(MENU_SELECTALL).setTitle(
                isSelectedAll() ? R.string.operation_cancel_selectall : R.string.operation_selectall);
        menu.findItem(MENU_SELECTALL).setEnabled(mCurrentMode != Mode.Pick);*/
        /*TYRD: weina 20150624 delete  end*/
        MenuItem menuItem = menu.findItem(GlobalConsts.MENU_SHOWHIDE);
        if (menuItem != null) {
            menuItem.setTitle(Settings.instance().getShowDotAndHiddenFiles() ? R.string.operation_hide_sys
                    : R.string.operation_show_sys);
        }

        FavoriteDatabaseHelper databaseHelper =new FavoriteDatabaseHelper(mContext);//FavoriteDatabaseHelper.getInstance();

        if (databaseHelper != null) {
            MenuItem item = menu.findItem(GlobalConsts.MENU_FAVORITE);
            if (item != null) {
                item.setTitle(databaseHelper.isFavorite(mCurrentPath) ? R.string.operation_unfavorite
                        : R.string.operation_favorite);
            }
        }
		/*TYRD: weina 20150624 modify begin*/
	//baitao 201601104 mod begin
	//try {
		//MenuItem item = menu.findItem(GlobalConsts.MENU_MUTI_COPY);
		/*if (item != null) {item.setEnabled(!mFileViewListener.disenabledMenu(GlobalConsts.MENU_MUTI_COPY));}
		item = menu.findItem(GlobalConsts.MENU_MUTI_CUT);
		if (item != null) {item.setEnabled(!mFileViewListener.disenabledMenu(GlobalConsts.MENU_MUTI_CUT));}
		item = menu.findItem(GlobalConsts.MENU_MUTI_DEL);
		if (item != null) {item.setEnabled(!mFileViewListener.disenabledMenu(GlobalConsts.MENU_MUTI_DEL));}
		*/
		//item = menu.findItem(GlobalConsts.MENU_SORT);
		//if (item != null) {item.setEnabled(!mFileViewListener.disenabledMenu(GlobalConsts.MENU_SORT));}
		/*
		menu.findItem(GlobalConsts.MENU_MUTI_COPY).setEnabled(!mFileViewListener.disenabledMenu(GlobalConsts.MENU_MUTI_COPY));
		menu.findItem(GlobalConsts.MENU_MUTI_CUT).setEnabled(!mFileViewListener.disenabledMenu(GlobalConsts.MENU_MUTI_CUT));
		menu.findItem(GlobalConsts.MENU_MUTI_DEL).setEnabled(!mFileViewListener.disenabledMenu(GlobalConsts.MENU_MUTI_DEL));
		menu.findItem(GlobalConsts.MENU_SORT).setEnabled(!mFileViewListener.disenabledMenu(GlobalConsts.MENU_SORT));*/

	/*} catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, "fail to view file: " + e.toString());
        }*/
	//baitao mod end
        /*TYRD: weina 20150624 modify end*/
    }

    public boolean isFileSelected(String filePath) {
        return mFileOperationHelper.isFileSelected(filePath);
    }

    public void setMode(Mode m) {
        mCurrentMode = m;
    }

    public Mode getMode() {
        return mCurrentMode;
    }

    public void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo lFileInfo = mFileViewListener.getItem(position);
        //showDropdownNavigation(false);

        if (lFileInfo == null) {
            Log.e(LOG_TAG, "file does not exist on position:" + position);
            return;
        }
        /*TYRD: weina 20150624 modity begin*/
        //if (isInSelection()) {
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.file_checkbox);
        if(checkBox.getVisibility() == View.VISIBLE){
			/*TYRD: weina 20150624 modity end*/
            boolean selected = lFileInfo.Selected;
            ActionMode actionMode = ((FileExplorerTabActivity) mContext).getActionMode();
            if (selected) {
                mCheckedFileNameList.remove(lFileInfo);
                //checkBox.setImageResource(R.drawable.btn_check_off_holo_light);//TYRD: weina 20150624 deleted
                
            } else {
                mCheckedFileNameList.add(lFileInfo);
                //checkBox.setImageResource(R.drawable.btn_check_on_holo_light); //TYRD: weina 20150624 deleted
            }
				
            if (actionMode != null) {
				/*TYRD: weina 20150624 delete  begin*/
                /*if (mCheckedFileNameList.size() == 0) actionMode.finish();
                else */
                /*TYRD: weina 20150624 delete  end*/
                actionMode.invalidate();
            }
            lFileInfo.Selected = !selected;
			
			/*TYRD: weina 20150624 add begin*/
			checkBox.setChecked(lFileInfo.Selected);
			if(mCheckedFileNameList.size()!=0 && mConfirmOperationBar.getVisibility()!= View.VISIBLE){
				showConfirmOperationBar(true);
			}
			updateConfirmButtons();
			/*TYRD: weina 20150624 add  end*/
            Util.updateActionModeTitle(actionMode, mContext, mCheckedFileNameList.size());
            return;
        }
        if (!lFileInfo.IsDir) {
            if (mCurrentMode == Mode.Pick) {
                mFileViewListener.onPick(lFileInfo);
            } else {
			    /*TYRD: weina 20150624 modify begin*/
                if(lFileInfo!=null &&(MediaFile.getFileType(lFileInfo.filePath)!=null)&& MediaFile.isImageFileType(MediaFile.getFileType(lFileInfo.filePath).fileType)){
					File f= new File(lFileInfo.filePath);
					//baitao 2016.01.19 mod begin
					//Uri uri = Util.getMediaFileUtiForContent(f,mContext);
					//String platform = SystemProperties.get("ro.seccap.platform");
					Uri uri = null;
					//if (!TextUtils.isEmpty(platform) && platform.contains("QRD")) {
					if(true){
						uri = Util.getMediaFileUtiForContent(f,mContext);
					} else {
						uri = Uri.fromFile(f);
					}
					//baitao 2016.01.19 mod end
					Intent intent = new Intent();
					intent.setAction("com.android.camera.action.REVIEW");
					intent.setDataAndType(uri,MediaFile.getFileType(lFileInfo.filePath).mimeType);
					mContext.startActivity(intent);
				}else{
                    viewFile(lFileInfo);
                }
				/*TYRD: weina 20150624 modify end*/
            }
            return;
        }
        
        mCurrentPath = getAbsoluteName(mCurrentPath, lFileInfo.fileName); 
        ActionMode actionMode = ((FileExplorerTabActivity) mContext).getActionMode();
        if (actionMode != null) {
            actionMode.finish();
        }
		/*TYRD: weina 20150624 add begin*/
		refreshFileList();
		/*if(mFileOperationHelper.getFileList().size()!=0){
			showConfirmOperationBar(true);
			Button confirmButton = (Button)mConfirmOperationBar.findViewById(R.id.button_moving_confirm);
			int text = R.string.confirm;
			confirmButton.setEnabled(true);
			confirmButton.setText(text);
		}else{
            showConfirmOperationBar(false);
		}*/
        /*TYRD: weina 20150624 add end*/
    }

    public void setRootPath(String path) {
        mRoot = path;
        mCurrentPath = path;
    }

    public String getRootPath() {
        return mRoot;
    }

    public String getCurrentPath() {
        return mCurrentPath;
    }

    public void setCurrentPath(String path) {
        mCurrentPath = path;
    }

    private String getAbsoluteName(String path, String name) {
        return path.equals(GlobalConsts.ROOT_PATH) ? path + name : path + File.separator + name;
    }

    // check or uncheck
    public boolean onCheckItem(FileInfo f, View v) {
        if (isMoveState())
            return false;

        if(isSelectingFiles() && f.IsDir)
            return false;

        if (f.Selected) {
            mCheckedFileNameList.add(f);
        } else {
            mCheckedFileNameList.remove(f);
        }
        /*TYRD: weina 20150624 add begin*/
        if(mCheckedFileNameList.size()!=0 && mConfirmOperationBar.getVisibility()!= View.VISIBLE){
            showConfirmOperationBar(true);
        }
        updateConfirmButtons();
        /*TYRD: weina 20150624 add  end*/
        return true;
    }

    private boolean isSelectingFiles() {
        return mSelectFilesCallback != null;
    }

    public boolean isSelectedAll() {
        return mFileViewListener.getItemCount() != 0 && mCheckedFileNameList.size() == mFileViewListener.getItemCount();
    }
    
    public boolean isSelected() {
        return mCheckedFileNameList.size() != 0;
    }

    public void clearSelection() {
        if (mCheckedFileNameList.size() > 0) {
            for (FileInfo f : mCheckedFileNameList) {
                if (f == null) {
                    continue;
                }
                f.Selected = false;
            }
            mCheckedFileNameList.clear();
            mFileViewListener.onDataChanged();
        }
    }

    private void viewFile(FileInfo lFileInfo) {
        try {
            IntentBuilder.viewFile(mContext, lFileInfo.filePath);
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, "fail to view file: " + e.toString());
        }
    }

    public boolean onBackPressed() {
        /*if (mDropdownNavigation.getVisibility() == View.VISIBLE) {
            mDropdownNavigation.setVisibility(View.GONE);
        } else*/ 
        if (isInSelection()) {
            clearSelection();
			showConfirmOperationBar(true);
			updateOperationMenuBar(true);
        } else if (!onOperationUpLevel()) {
            return false;
        }
        return true;
    }

    public void copyFile(ArrayList<FileInfo> files) {
        mFileOperationHelper.Copy(files);
    }

    public void moveFileFrom(ArrayList<FileInfo> files) {
        mFileOperationHelper.StartMove(files);
        showConfirmOperationBar(true);
        updateConfirmButtons();
        // refresh to hide selected files
        refreshFileList();
    }

    /*private void showDropdownNavigation(boolean show) {
        mDropdownNavigation.setVisibility(show ? View.VISIBLE : View.GONE);
        mNavigationBarUpDownArrow
                .setImageResource(mDropdownNavigation.getVisibility() == View.VISIBLE ? R.drawable.arrow_up
                        : R.drawable.arrow_down);
    }*/

    @Override
    public void onFileChanged(String path) {
        notifyFileSystemChanged(path);
    }

    public void startSelectFiles(SelectFilesCallback callback) {
        mSelectFilesCallback = callback;
        showConfirmOperationBar(true);
        updateConfirmButtons();
    }
	/*TYRD: weina 20150624 add  begin*/
	public ActionMode getActionMode(){
        FileExplorerTabActivity fileExplorerTabActivity = (FileExplorerTabActivity)mContext ;
		ActionMode mode = fileExplorerTabActivity.getActionMode();
		if(mode==null){
			mode = fileExplorerTabActivity.getWindow().getDecorView().startActionMode(new ModeCallback(mContext,
                    this));
			fileExplorerTabActivity.setActionMode(mode);
			Util.updateActionModeTitle(mode, mContext, mCheckedFileNameList.size());
		}
		return mode;
	}

	public void onKeyBack(){
		if(canShowCheckBox && mConfirmOperationBar.getVisibility()!= View.VISIBLE){
			if(isCopyOperation||isCutOperation||isDelOperation){
				isCopyOperation=isCutOperation=isDelOperation= false;
			}
			canShowCheckBox= false;
			onOperationButtonCancel();
		}
	}
	private  void dismissDialog(){
        if(alertDialog!=null){
			alertDialog.dismiss();
			alertDialog=null;
		}
	}
	/*TYRD: weina 20150624 add  end*/
}
