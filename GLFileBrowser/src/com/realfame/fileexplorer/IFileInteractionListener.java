package com.realfame.fileexplorer;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.util.Collection;

public interface IFileInteractionListener {

    public View getViewById(int id);

    public Context getContext();

    public void startActivity(Intent intent);

    public void onDataChanged();

    public void onPick(FileInfo f);

    public boolean shouldShowOperationPane();

    /**
     * Handle operation listener.
     * @param id
     * @return true: indicate have operated it; false: otherwise.
     */
    public boolean onOperation(int id);

    public String getDisplayPath(String path);

    public String getRealPath(String displayPath);

    public void runOnUiThread(Runnable r);

    // return true indicates the navigation has been handled
    public boolean onNavigation(String path);

    public boolean shouldHideMenu(int menu);

	public boolean disenabledMenu();//TYRD: weina 20150624 add 

    public FileIconHelper getFileIconHelper();

    public FileInfo getItem(int pos);

    public void sortCurrentList(FileSortHelper sort);

    public Collection<FileInfo> getAllFiles();

    public void addSingleFile(FileInfo file);

    public boolean onRefreshFileList(String path, FileSortHelper sort);

    public void cancelRefreshAsyncTask(); //add by chen he 2016.02.22
    public int getItemCount();
}
