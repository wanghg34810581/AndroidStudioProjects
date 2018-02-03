package com.realfame.fileexplorer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.content.Context;
import android.provider.MediaStore.Files;
/*TYRD:weina 20150820 add for PROD103988457 begin*/
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ConditionVariable;
import com.realfame.fileexplorer.Util.SDCardInfo;
/*TYRD:weina 20150820 add for PROD103988457 end*/
import android.content.DialogInterface.OnDismissListener;//baitao 2016.01.28 add
import android.content.DialogInterface;//baitao 2016.01.28 add

public class FileOperationHelper {
    private static final String LOG_TAG = "FileOperation";

    private ArrayList<FileInfo> mCurFileNameList = new ArrayList<FileInfo>();
	/*TYRD:weina 20150820 add for PROD103988457 begin*/
	private ArrayList<FileInfo> mExistFileNameList = new ArrayList<FileInfo>();//
	private ArrayList<FileInfo> mReplaceFileNameList = new ArrayList<FileInfo>();
    AlertDialog alertDialog;
	ConditionVariable mConditionVariable = new ConditionVariable(false);
	HandlerThread mWorkThread = null;
	workHandler mHandler;
	Thread mProcessThread;
	/*TYRD:weina 20150820 add for PROD103988457 end*/
    private boolean mMoving;
    private boolean mCopying;
    private boolean mCanPaste = true;//baitao 2016.01.28 add
    private IOperationProgressListener mOperationListener;

    private FilenameFilter mFilter;

    private Context mContext;


    public interface IOperationProgressListener {
        void onFinish();

        void onFileChanged(String path);
    }

    public FileOperationHelper(Context context, IOperationProgressListener l) {
        mOperationListener = l;
        mContext = context;
		
    }

    public void setFilenameFilter(FilenameFilter f) {
        mFilter = f;
    }

    public boolean CreateFolder(String path, String name) {
        Log.v(LOG_TAG, "CreateFolder >>> " + path + "," + name);

        File f = new File(Util.makePath(path, name));
        if (f.exists())
            return false;

        return f.mkdir();
    }

    public void Copy(ArrayList<FileInfo> files) {
        mCopying = true;
        copyFileList(files);
    }

    public boolean Paste(String path) {
        if (mCurFileNameList.size() == 0)
            return false;
		/*TYRD: weina 20151021 add for PROD104074267 begin*/
        if(!isEnoughSpace(mCurFileNameList,mFilter)){
			mWorkThread = new HandlerThread("loop");
            mWorkThread.start();
            mHandler  = new workHandler(mWorkThread.getLooper());
            Message msg = mHandler.obtainMessage();
            msg.what = MSG_SHOW_NO_SPACE_DIALOG;
            mHandler.sendMessage(msg); 
			return false;
		}
		/*TYRD: weina 20151021 add for PROD104074267 END*/
        final String _path = path;
		/*TYRD:weina 20150820 add for PROD103988457 begin*/
        for(FileInfo f : mCurFileNameList){
            if( isFileExisted(f ,_path)){
                mExistFileNameList.add(f);
                mConditionVariable.close();
                mWorkThread = new HandlerThread("loop");
                mWorkThread.start();
                mHandler  = new workHandler(mWorkThread.getLooper());
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_SHOW_DIALOG;
                msg.obj= f;
                mHandler.sendMessage(msg);
                mConditionVariable.block();
            }
            if(mWorkThread!=null){
                mWorkThread =null;
            }
        }
	    //baitao 2016.01.28 add begin
        if (!mCanPaste) {
			mCanPaste = true;
			return false;
		}
		//baitao 2016.01.28 add end
		mCurFileNameList.removeAll(mExistFileNameList);
		for(FileInfo f: mReplaceFileNameList){
             mCurFileNameList.add(f);
		}
		/*TYRD:weina 20150820 add for PROD103988457 end*/
        asnycExecute(new Runnable() {
            @Override
                public void run() {
                    for (FileInfo f : mCurFileNameList) {
                            CopyFile(f, _path);
                    }
					Util.flag= false;//TYRD: weina 20150820 add 
                    mOperationListener.onFileChanged(Environment
                            .getExternalStorageDirectory()
                            .getAbsolutePath());
    
                    clear();
                        
                }
        });
		mCanPaste = true;//baitao add 2016.01.28
        return true;
    }

    public boolean canPaste() {
        return mCurFileNameList.size() != 0;
    }

    public void StartMove(ArrayList<FileInfo> files) {
        if (mMoving)
            return;

        mMoving = true;
        copyFileList(files);
    }

    public boolean isMoveState() {
        return mMoving;
    }

    public boolean isCopy(){
        return mCopying;
    }

    public boolean canMove(String path) {
		if("/storage".equals(path)) return false;
        for (FileInfo f : mCurFileNameList) {
            if (!f.IsDir)
                continue;

            if (Util.containsPath(f.filePath, path))
                return false;
        }

        return true;
    }

    public void clear() {
        synchronized(mCurFileNameList) {
            mCurFileNameList.clear();
			/*TYRD: weina 20150824 add for PROD103988457 begin*/
			mExistFileNameList.clear();
			mReplaceFileNameList.clear();
			/*TYRD: weina 20150824 add for PROD103988457 end*/
        }
    }

    public boolean EndMove(String path) {
        if (!mMoving)
            return false;
        mMoving = false;
        mCopying = false;

        if (TextUtils.isEmpty(path))
            return false;

        final String _path = path;
		/*TYRD:weina 20151021 add for PROD104074294 BEGIN */
		for(FileInfo f : mCurFileNameList){
            if( isFileExisted(f ,_path)){
                mExistFileNameList.add(f);
                mConditionVariable.close();
                mWorkThread = new HandlerThread("loop");
                mWorkThread.start();
                mHandler  = new workHandler(mWorkThread.getLooper());
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_SHOW_DIALOG;
                msg.obj= f;
                mHandler.sendMessage(msg);
                mConditionVariable.block();
            }
            if(mWorkThread!=null){
                mWorkThread =null;
            }
        }
	    
	    //baitao 2016.01.28 add begin
        if (!mCanPaste) {
			mCanPaste = true;
			return false;
		}
		//baitao 2016.01.28 add end

		mCurFileNameList.removeAll(mExistFileNameList);
		for(FileInfo f: mReplaceFileNameList){
             mCurFileNameList.add(f);
		}
		/*TYRD:weina 20151021 add for PROD104074294 END*/
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                    
                        for (FileInfo f : mCurFileNameList) {
                            MoveFile(f, _path);
                        }
                        mOperationListener.onFileChanged(Environment
                                .getExternalStorageDirectory()
                                .getAbsolutePath());
    
                        clear();
                    }
        });
		mCanPaste = true;//baitao add 2016.01.28
        return true;
    }

    public ArrayList<FileInfo> getFileList() {
        return mCurFileNameList;
    }

    private void asnycExecute(Runnable r) {
        final Runnable _r = r;
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                synchronized(mCurFileNameList) {
                    _r.run();
                }
                if (mOperationListener != null) {
                    mOperationListener.onFinish();
                }

                return null;
            }
        }.execute();
    }

    public boolean isFileSelected(String path) {
        synchronized(mCurFileNameList) {
            for (FileInfo f : mCurFileNameList) {
                if (f.filePath.equalsIgnoreCase(path))
                    return true;
            }
        }
        return false;
    }

    public boolean Rename(FileInfo f, String newName) {
        if (f == null || newName == null) {
            Log.e(LOG_TAG, "Rename: null parameter");
            return false;
        }

        File file = new File(f.filePath);
        String newPath = Util.makePath(Util.getPathFromFilepath(f.filePath), newName);
        final boolean needScan = file.isFile();
        try {
            boolean ret = file.renameTo(new File(newPath));
			//baitao 2016.01.16 add begin
            if (!ret){
                CopyFile(f, newPath);
                DeleteFile(f);
                ret = true;
            }
			//baitao 2016.01.16 add end
            if (ret) {
                if (needScan) {
                    mOperationListener.onFileChanged(f.filePath);
                }
                mOperationListener.onFileChanged(newPath);
            }
            return ret;
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Fail to rename file," + e.toString());
        }
        return false;
    }

    public boolean Delete(ArrayList<FileInfo> files) {
        copyFileList(files);
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                for (FileInfo f : mCurFileNameList) {
                    DeleteFile(f);
                }

                mOperationListener.onFileChanged(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath());

                clear();
            }
        });
        return true;
    }

    protected void DeleteFile(FileInfo f) {
        if (f == null) {
            Log.e(LOG_TAG, "DeleteFile: null parameter");
            return;
        }

        File file = new File(f.filePath);
        boolean directory = file.isDirectory();
        if (directory) {
            for (File child : file.listFiles(mFilter)) {
                if (Util.isNormalFile(child.getAbsolutePath())) {
                    DeleteFile(Util.GetFileInfo(child, mFilter, true));
                }
            }
        }

        file.delete();
		//add by chen he 2016.02.23
		if (f.dbId <= 0) {
			f.dbId = Util.getDbId(mContext, f.filePath);
		}
		//add end
        mContext.getContentResolver().delete(
            Files.getContentUri("external"), "_id=" + f.dbId, null); //xueyingli add 20150603

        Log.v(LOG_TAG, "DeleteFile >>> " + f.filePath + ",id =" + f.dbId);
    }

    private void CopyFile(FileInfo f, String dest) {
        if (f == null || dest == null) {
            Log.e(LOG_TAG, "CopyFile: null parameter");
            return;
        }

        File file = new File(f.filePath);
        if (file.isDirectory()) {

            // directory exists in destination, rename it
            String destPath = Util.makePath(dest, f.fileName);
            File destFile = new File(destPath);
            int i = 1;
			/*TYRD: weina 20150825 add for PROD begin*/
			if(destPath.equals(f.filePath)){
				return ;
			}
			/*TYRD: weina 20150825 add for PROD  end*/
            if (destFile.exists()) {
				/*TYRD: weina 20150824 modity for PROD103988457 begin*/
               /* destPath = Util.makePath(dest, f.fileName + "(" + i++  +")");
                destFile = new File(destPath);*/
                DeleteFile(Util.GetFileInfo(destFile, mFilter, true));
				/*TYRD: weina 20150824 modity for PROD103988457 end*/
            }
            destFile.mkdirs();
			

            for (File child : file.listFiles(mFilter)) {
			    /*TYRD:weina 20150820 add for PROD103988457 begin*/
				if(Util.flag){
					DeleteFile(Util.GetFileInfo(destFile, mFilter, true));
					break;
				}
				/*TYRD:weina 20150820 add for PROD103988457 begin*/
                if (!child.isHidden() && Util.isNormalFile(child.getAbsolutePath())) {//weina add && !Util.flag
                    CopyFile(Util.GetFileInfo(child, mFilter, Settings.instance().getShowDotAndHiddenFiles()), destPath);
                }
            }
        } else {
            /*TYRD: weina 20150824 modity for PROD103988457 end*/
            //String destFile = Util.copyFile(f.filePath, dest);
            String destFile = Util.copyFile(f.filePath, dest,mContext);
		    /*TYRD: weina 20150824 modity for PROD103988457 end*/
        }
        Log.v(LOG_TAG, "CopyFile >>> " + f.filePath + "," + dest);
    }

    private boolean MoveFile(FileInfo f, String dest) {
        Log.v(LOG_TAG, "MoveFile >>> " + f.filePath + "," + dest);

        if (f == null || dest == null) {
            Log.e(LOG_TAG, "CopyFile: null parameter");
            return false;
        }

        File file = new File(f.filePath);
		String newPath = Util.makePath(dest, f.fileName);
		
        try {
			//baitao 2016.01.16 mod begin
            //return file.renameTo(new File(newPath));
			boolean result = file.renameTo(new File(newPath));
            if (!result){
                CopyFile(f, dest);
                DeleteFile(f);
            }
			//baitao 2016.01.16 mod end
            return result;
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Fail to move file," + e.toString());
        }
        return false;
    }

    private void copyFileList(ArrayList<FileInfo> files) {
        synchronized(mCurFileNameList) {
            mCurFileNameList.clear();
            for (FileInfo f : files) {
                mCurFileNameList.add(f);
            }
        }
    }
    /*TYRD:weina 20150820 add for PROD103988457 begin*/
	public void showDialog(FileInfo fileinfo){
		final FileInfo f = fileinfo;
        AlertDialog.Builder dialog= new AlertDialog.Builder(mContext);
        View alertDialogLayout = LayoutInflater.from(mContext).inflate(R.layout.ty_no_title_alertdialog,null);
        dialog.setView(alertDialogLayout);
        TextView messageView = (TextView)alertDialogLayout.findViewById(R.id.message);
        messageView.setText(fileinfo.fileName + mContext.getString(R.string.replace_exist_file));
        Button positiveButton = (Button)alertDialogLayout.findViewById(R.id.button1);
        Button cancleButton = (Button)alertDialogLayout.findViewById(R.id.button3);
        positiveButton.setText(R.string.confirm);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
				dismissDialog();
				mReplaceFileNameList.add(f);
				mConditionVariable.open();
				mCanPaste = true;//baitao 2016.01.28 add
            }
		});
        
        cancleButton.setText(R.string.cancel);
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				dismissDialog();
				mConditionVariable.open();
				mCanPaste = false;//baitao 2016.01.28 add
            }
        });
		//baitao 2016.01.28 add begin
		dialog.setOnDismissListener(new OnDismissListener() {	
			@Override
			public void onDismiss(DialogInterface dialog) {
				dismissDialog();
				mConditionVariable.open();
				mCanPaste = false;
			}
		});
		//baitao 2016.01.28 add end
        dialog.setView(alertDialogLayout);
		alertDialog = dialog.create();
		alertDialog.show();   
	}

	private  void dismissDialog(){
        if(alertDialog!=null){
			alertDialog.dismiss();
			alertDialog=null;
		}
	}
	public boolean isFileExisted(FileInfo f ,String dest){
		if (f == null || dest == null) {
            Log.e(LOG_TAG, "isFileExisted: null parameter");
            return false;
        }
        File file = new File(f.filePath);
        if (file.isDirectory()) {

            // directory exists in destination, rename it
            String destPath = Util.makePath(dest, f.fileName);
            File destFile = new File(destPath);
			return destFile.exists();
        }else{
            File destPlace = new File(dest);
            if (!destPlace.exists()) {
                if (!destPlace.mkdirs())
                    return false;
            }

            String destPath = Util.makePath(dest, f.fileName);
            File destFile = new File(destPath);
			return destFile.exists();
		}
	}

	private final static int MSG_SHOW_DIALOG=1;
	private final static int MSG_SHOW_NO_SPACE_DIALOG=2; //add for PROD104074267
	public class workHandler extends Handler{
		public workHandler(Looper looper){
			super(looper);
		}

		@Override
		public void handleMessage (Message msg){
           switch (msg.what){
               case MSG_SHOW_DIALOG:{
			       showDialog((FileInfo)msg.obj);
				   break;
               }
			   /*add for PROD104074267*/
			   case MSG_SHOW_NO_SPACE_DIALOG:{
			   	   showNoSpaceDialog();
				   break;
			   }
			   /*add for PROD104074267*/
		   } 
	    }

   }
   /*TYRD:weina 20150820 add for PROD103988457 end*/
   /*TYRD: weina 20151021 add for PROD104074267 BEGIN*/
   private boolean isEnoughSpace(ArrayList<FileInfo> fileNameList,FilenameFilter filter){
       SDCardInfo sdCardInfo = Util.getSDCardInfo();
	   if(sdCardInfo==null) return false;
	   long fileSize =0;
	   for(int i=0;i<fileNameList.size();i++){
	   	  fileSize += Util.getFileSize(fileNameList.get(i),filter);
	   }
	   Log.d("weina","isEnoughSpace: fileSize="+fileSize+",sdCardInfo="+sdCardInfo.free);
	   return sdCardInfo.free > fileSize ? true : false;
   }
   private void showNoSpaceDialog(){
        AlertDialog.Builder dialog= new AlertDialog.Builder(mContext);
        View alertDialogLayout = LayoutInflater.from(mContext).inflate(R.layout.ty_no_title_alertdialog,null);
        dialog.setView(alertDialogLayout);
        TextView messageView = (TextView)alertDialogLayout.findViewById(R.id.message);
        messageView.setText(mContext.getString(R.string.no_space));
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
   }
   /*TYRD: weina 20151021 add for PROD104074267 END*/
    
}
