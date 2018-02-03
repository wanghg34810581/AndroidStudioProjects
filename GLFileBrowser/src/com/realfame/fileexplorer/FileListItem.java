package com.realfame.fileexplorer;

import com.realfame.fileexplorer.FileViewInteractionHub.Mode;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
/*TYRD: weina 20150624 add begin*/
import android.widget.CheckBox;
import android.util.Log;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import java.io.File;
/*TYRD: weina 20150624 add end*/
public class FileListItem {
    public static void setupFileListItemInfo(Context context, View view,
            FileInfo fileInfo, FileIconHelper fileIcon,
            FileViewInteractionHub fileViewInteractionHub) {
        //add by chen he 2016.02.22
        if (fileInfo == null) {
            return;
        }
        //add end
        // if in moving mode, show selected file always
		/*TYRD: weina 20150624 modify begin*/
        /*if (fileViewInteractionHub.isMoveState()) {
			
            fileInfo.Selected = fileViewInteractionHub.isFileSelected(fileInfo.filePath);
        }*/
        //ImageView checkbox = (ImageView) view.findViewById(R.id.file_checkbox);
        CheckBox checkbox = (CheckBox) view.findViewById(R.id.file_checkbox);
		/*TYRD: weina 20150624 modify end*/
        if (fileViewInteractionHub.getMode() == Mode.Pick) {
            checkbox.setVisibility(View.GONE);
        } else {
            checkbox.setVisibility(fileViewInteractionHub.canShowCheckBox() ? View.VISIBLE : View.GONE);
			/*TYRD: weina 20150624 modify begin*/
           // checkbox.setImageResource(fileInfo.Selected ? R.drawable.btn_check_on_holo_light
           //         : R.drawable.btn_check_off_holo_light);
            checkbox.setTag(fileInfo);
           // view.setSelected(fileInfo.Selected);
			checkbox.setChecked(fileInfo.Selected);
			/*TYRD: weina 20150624 modify end*/
        }
        if (fileInfo.filePath.equals("/storage/sdcard0")){
            Util.setText(view, R.id.file_name, R.string.phone_storage);
        } else if (fileInfo.filePath.equals("/storage/sdcard1")){
            Util.setText(view, R.id.file_name, R.string.external_sd_card);
        } else {
            Util.setText(view, R.id.file_name, fileInfo.fileName);
        }
        String count_index = fileInfo.Count + context.getResources().getString(R.string.count_number);
		/*TYRD: weina 20150624 modify begin*/
		// Util.setText(view, R.id.file_count, fileInfo.IsDir ? count_index : "");
        Util.setText(view, R.id.file_count, fileInfo.IsDir ? count_index : Util.convertStorage(fileInfo.fileSize));
        Util.setText(view, R.id.modified_time, Util.formatDateString(context, fileInfo.ModifiedDate));
        //Util.setText(view, R.id.file_size, (fileInfo.IsDir ? "" : Util.convertStorage(fileInfo.fileSize)));
        /*TYRD: weina 20150624 modify end*/
        ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
        ImageView lFileImageFrame = (ImageView) view.findViewById(R.id.file_image_frame);
        /*TYRD: weina 20150824 add for PROD103997493 BEGIN*/
		File file = new File(fileInfo.filePath);
		if(file.getName().startsWith(".")){
			lFileImage.setColorFilter(Color.GRAY,PorterDuff.Mode.MULTIPLY);
		}else{
		    lFileImage.clearColorFilter();
		}
		/*TYRD: weina 20150824 add for PROD103997493 END*/
        if (fileInfo.IsDir) {
            lFileImageFrame.setVisibility(View.GONE);
			
			lFileImage.setImageResource(R.drawable.folder);
        } else {
		   /*TYRD: weina 20150624 modify begin*/  
		   //fileIcon.setIcon(fileInfo, lFileImage, lFileImageFrame);
            fileIcon.setIcon(fileInfo, lFileImage, lFileImageFrame,context);
			/*TYRD: weina 20150624 modify end*/
        }
		
    }


    public static class FileItemOnClickListener implements OnClickListener {
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        public FileItemOnClickListener(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public void onClick(View v) {
			/*TYRD: weina 20150624 modify begin*/
            //ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
           CheckBox img = (CheckBox) v;
		   	/*TYRD: weina 20150624 modify end*/
            assert (img != null && img.getTag() != null);

            FileInfo tag = (FileInfo) img.getTag();
            tag.Selected = !tag.Selected;
			/*TYRD: weina 20150624 changed begin*/
			if (mFileViewInteractionHub.onCheckItem(tag, v)) {
                //img.setImageResource(tag.Selected ? R.drawable.btn_check_on_holo_light
              //          : R.drawable.btn_check_off_holo_light);
            } else {
                tag.Selected = !tag.Selected;
            }
			img.setChecked(tag.Selected);
            ActionMode actionMode = ((FileExplorerTabActivity) mContext).getActionMode();
            if (actionMode == null) {
                actionMode = ((FileExplorerTabActivity) mContext)
                        .startActionMode(new ModeCallback(mContext,
                                mFileViewInteractionHub));
                ((FileExplorerTabActivity) mContext).setActionMode(actionMode);
            } else {
                actionMode.invalidate();
            }
            /*TYRD: weina 20150624 changed end*/
            Util.updateActionModeTitle(actionMode, mContext,
                    mFileViewInteractionHub.getSelectedFileList().size());
        }
    }

    public static class ModeCallback implements ActionMode.Callback {
        private Menu mMenu;
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        private void initMenuItemSelectAllOrCancel() {
            boolean isSelectedAll = mFileViewInteractionHub.isSelectedAll();
            mMenu.findItem(R.id.action_cancel).setVisible(isSelectedAll);
            mMenu.findItem(R.id.action_select_all).setVisible(!isSelectedAll);
        }

        private void scrollToSDcardTab() {
            ActionBar bar = ((FileExplorerTabActivity) mContext).getActionBar();
            if (bar.getSelectedNavigationIndex() != Util.SDCARD_TAB_INDEX) {
                bar.setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
            }
        }

        public ModeCallback(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
            mMenu = menu;
            inflater.inflate(R.menu.operation_menu, mMenu);
           initMenuItemSelectAllOrCancel();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
           /*TYRD: weina 20150624 deleted begin*/
           /* mMenu.findItem(R.id.action_copy_path).setVisible(
                    mFileViewInteractionHub.getSelectedFileList().size() == 1);*/
           /*TYRD: weina 20150624 deleted  end*/
            mMenu.findItem(R.id.action_cancel).setVisible(
            		mFileViewInteractionHub.isSelected());
            mMenu.findItem(R.id.action_select_all).setVisible(
            		!mFileViewInteractionHub.isSelectedAll());
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
				/*TYRD: weina 20150624 deleted  begin*/
               /* case R.id.action_delete:
                    mFileViewInteractionHub.onOperationDelete();
                    mode.finish();
                    break;
                case R.id.action_copy:
                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .copyFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;
                case R.id.action_move:
                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .moveToFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;
                case R.id.action_send:
                    mFileViewInteractionHub.onOperationSend();
                    mode.finish();
                    break;
                case R.id.action_copy_path:
                    mFileViewInteractionHub.onOperationCopyPath();
                    mode.finish();
                    break;*/
                    /*TYRD: weina 20150624 deleted end*/
                case R.id.action_cancel:
                    mFileViewInteractionHub.clearSelection();
                    initMenuItemSelectAllOrCancel();
                   // mode.finish();//TYRD: weina 20150624 deleted 
                   mFileViewInteractionHub.updateConfirmButtons();//TYRD: weina 20150624 add
                    break;
                case R.id.action_select_all:
                    mFileViewInteractionHub.onOperationSelectAll();
                    initMenuItemSelectAllOrCancel();
					mFileViewInteractionHub.showConfirmOperationBar(true);//TYRD: weina 20150624 add
					mFileViewInteractionHub.updateConfirmButtons();//TYRD: weina 20150624 add
                    break;
            }
            Util.updateActionModeTitle(mode, mContext, mFileViewInteractionHub
                    .getSelectedFileList().size());
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mFileViewInteractionHub.onKeyBack();//TYRD: weina 20150624 add
            mFileViewInteractionHub.clearSelection();
			mFileViewInteractionHub.showConfirmOperationBar(false);//TYRD: weina 20150624 add
            ((FileExplorerTabActivity) mContext).setActionMode(null);
			mFileViewInteractionHub.showOperationMenuBar(true);
			mFileViewInteractionHub.updateOperationMenuBar(true);
        }
    }
}
