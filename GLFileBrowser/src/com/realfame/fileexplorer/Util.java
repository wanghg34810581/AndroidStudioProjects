package com.realfame.fileexplorer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
/*TYRD: weina 20150624 add begin*/
import java.util.List;
import android.provider.MediaStore;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentUris;
import android.util.Log;
import android.provider.MediaStore.Files;
/*TYRD: weina 20150624 add end*/

import android.provider.MediaStore.Files.FileColumns;
import android.database.Cursor;
public class Util {
    private static String ANDROID_SECURE = "/mnt/sdcard/.android_secure";

    private static final String LOG_TAG = "Util";
	public static boolean flag = false ;//TYRD:weina 20150824 add for PROD103989662

    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // if path1 contains path2
    public static boolean containsPath(String path1, String path2) {
        String path = path2;
        while (path != null) {
            if (path.equalsIgnoreCase(path1))
                return true;

            if (path.equals(GlobalConsts.ROOT_PATH))
                break;
            path = new File(path).getParent();
        }

        return false;
    }

    public static String makePath(String path1, String path2) {
        if (path1.endsWith(File.separator))
            return path1 + path2;

        return path1 + File.separator + path2;
    }

    public static String getSdDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static boolean isNormalFile(String fullName) {
        return !fullName.equals(ANDROID_SECURE);
    }

    public static FileInfo GetFileInfo(String filePath) {
        File lFile = new File(filePath);
        if (!lFile.exists())
            return null;

        FileInfo lFileInfo = new FileInfo();
        lFileInfo.canRead = lFile.canRead();
        lFileInfo.canWrite = lFile.canWrite();
        lFileInfo.isHidden = lFile.isHidden();
        lFileInfo.fileName = Util.getNameFromFilepath(filePath);
        lFileInfo.ModifiedDate = lFile.lastModified();
        lFileInfo.IsDir = lFile.isDirectory();
        lFileInfo.filePath = filePath;
        lFileInfo.fileSize = lFile.length();
        return lFileInfo;
    }

    public static FileInfo GetFileInfo(File f, FilenameFilter filter, boolean showHidden) {
        FileInfo lFileInfo = new FileInfo();
        String filePath = f.getPath();
        File lFile = new File(filePath);
        lFileInfo.canRead = lFile.canRead();
        lFileInfo.canWrite = lFile.canWrite();
        lFileInfo.isHidden = lFile.isHidden();
        lFileInfo.fileName = f.getName();
        lFileInfo.ModifiedDate = lFile.lastModified();
        lFileInfo.IsDir = lFile.isDirectory();
        lFileInfo.filePath = filePath;
        if (lFileInfo.fileName.equals("emulated")){
            return null;
        }
        if (lFileInfo.IsDir) {
            int lCount = 0;
            File[] files = lFile.listFiles(filter);

            // null means we cannot access this dir
            if (files == null) {
                return null;
            }

            for (File child : files) {
                if ((!child.isHidden() || showHidden)
                        && Util.isNormalFile(child.getAbsolutePath())) {
                    lCount++;
                }
            }
            lFileInfo.Count = lCount;

        } else {

            lFileInfo.fileSize = lFile.length();

        }
        return lFileInfo;
    }
	/*TYRD: weina 20151021 add for PROD104074267 BEGIN*/
	public static long getFileSize(FileInfo fileInfo,FilenameFilter filter){
	    long filesize =0;
        if(fileInfo.IsDir){
			File lFile = new File(fileInfo.filePath);
            File[] files = lFile.listFiles(filter);
            if (files == null) {
                return filesize;
            }

            for (File child : files) {
               filesize += getFileSize(GetFileInfo(child,filter,Settings.instance().getShowDotAndHiddenFiles()),filter);
            }
		}else{
           filesize=fileInfo.fileSize;
		}
		return filesize;
	}
	/*TYRD: weina 20151021 add for PROD104074267 END*/

    /*
     * 閲囩敤浜嗘柊鐨勫姙娉曡幏鍙朅PK鍥炬爣锛屼箣鍓嶇殑澶辫触鏄洜涓篴ndroid涓瓨鍦ㄧ殑涓�釜BUG,閫氳繃
     * appInfo.publicSourceDir = apkPath;鏉ヤ慨姝ｈ繖涓棶棰橈紝璇︽儏鍙傝:
     * http://code.google.com/p/android/issues/detail?id=9151
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
        return null;
    }

    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }

    public static String getPathFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(0, pos);
        }
        return "";
    }

    public static String getNameFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(pos + 1);
        }
        return "";
    }

    // return new file path if successful, or return null
    /*TYRD: weina 20150824 add begin*/
    //public static String copyFile(String src, String dest) {
    public static String copyFile(String src, String dest,Context context) {
    /*TYRD: weina 20150824 add end*/
        File file = new File(src);
        if (!file.exists() || file.isDirectory()) {
            Log.v(LOG_TAG, "copyFile: file not exist or is directory, " + src);
            return null;
        }
        FileInputStream fi = null;
        FileOutputStream fo = null;
        try {
            fi = new FileInputStream(file);
            File destPlace = new File(dest);
            if (!destPlace.exists()) {
                if (!destPlace.mkdirs())
                    return null;
            }

            String destPath = Util.makePath(dest, file.getName());
            File destFile = new File(destPath);
            int i = 1;
			/*TYRD: weina 20150825 add for PRO begin*/
			if(destPath.equals(src)){
				return null;
			}
			/*TYRD: weina 20150825 modity for begin */
            while (destFile.exists()) {
				/*TYRD: weina 20150824 add*/
                /*String destName = Util.getNameFromFilename(file.getName()) + "(" + i++ + ")."
                        + Util.getExtFromFilename(file.getName());
                destPath = Util.makePath(dest, destName);
                destFile = new File(destPath);*/
                FileInfo fileinfo = GetFileInfo(src);
				fileinfo.dbId = getDbId(context, fileinfo.filePath);//add by chen he 2016.02.23
                destFile.delete();
				context.getContentResolver().delete(
                     Files.getContentUri("external"), "_id=" + fileinfo.dbId, null);
				/*TYRD: weina 20150824 modity end*/
            }

            if (!destFile.createNewFile())
                return null;
            fo = new FileOutputStream(destFile);
            int count = 102400;
            byte[] buffer = new byte[count];
            int read = 0;
            while ((read = fi.read(buffer, 0, count)) != -1) {
			    /*TYRD: weina 20150824 add begin*/
				if(flag){ 
					destFile.delete();
					break;
				}
				/*TYRD: weina 20150824 add  end*/
                fo.write(buffer, 0, read);
            }

            // TODO: set access privilege

            return destPath;
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "copyFile: file not found, " + src);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "copyFile: " + e.toString());
        } finally {
            try {
                if (fi != null)
                    fi.close();
                if (fo != null)
                    fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
	//add by chen he 2016.02.23
	public static long getDbId(Context context, String path){
        String volumeName = "external";
        Uri uri = Files.getContentUri(volumeName);
        String selection = FileColumns.DATA + "=?";
        String[] selectionArgs = new String[] {
            path
        };

        String[] columns = new String[] {
                FileColumns._ID, FileColumns.DATA
        };

        Cursor c = context.getContentResolver()
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
	//add end
    // does not include sd card folder
    private static String[] SysFileDirs = new String[] {
        "miren_browser/imagecaches"
    };

    public static boolean shouldShowFile(String path) {
        return shouldShowFile(new File(path));
    }

    public static boolean shouldShowFile(File file) {
        boolean show = Settings.instance().getShowDotAndHiddenFiles();
        if (show)
            return true;

        if (file.isHidden())
            return false;

        if (file.getName().startsWith("."))
            return false;

        String sdFolder = getSdDirectory();
        for (String s : SysFileDirs) {
            if (file.getPath().startsWith(makePath(sdFolder, s)))
                return false;
        }

        return true;
    }

    public static ArrayList<FavoriteItem> getDefaultFavorites(Context context) {
        ArrayList<FavoriteItem> list = new ArrayList<FavoriteItem>();
        list.add(new FavoriteItem(context.getString(R.string.favorite_photo), makePath(getSdDirectory(), "DCIM/Camera")));
        list.add(new FavoriteItem(context.getString(R.string.favorite_sdcard), getSdDirectory()));
        //list.add(new FavoriteItem(context.getString(R.string.favorite_root), getSdDirectory()));
        list.add(new FavoriteItem(context.getString(R.string.favorite_screen_cap), makePath(getSdDirectory(), "MIUI/screen_cap")));
        list.add(new FavoriteItem(context.getString(R.string.favorite_ringtone), makePath(getSdDirectory(), "MIUI/ringtone")));
        return list;
    }

    public static boolean setText(View view, int id, String text) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView == null)
            return false;

        textView.setText(text);
        return true;
    }

    public static boolean setText(View view, int id, int text) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView == null)
            return false;

        textView.setText(text);
        return true;
    }

    // comma separated number
    public static String convertNumber(long number) {
        return String.format("%,d", number);
    }

    // storage, G M K B
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    public static class SDCardInfo {
        public long total;

        public long free;
    }

    public static SDCardInfo getSDCardInfo() {
        String sDcString = android.os.Environment.getExternalStorageState();

        if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
            File pathFile = android.os.Environment.getExternalStorageDirectory();

            try {
                android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());

                // 鑾峰彇SDCard涓夿LOCK鎬绘暟
                long nTotalBlocks = statfs.getBlockCount();

                // 鑾峰彇SDCard涓婃瘡涓猙lock鐨凷IZE
                long nBlocSize = statfs.getBlockSize();

                // 鑾峰彇鍙緵绋嬪簭浣跨敤鐨凚lock鐨勬暟閲�     
                long nAvailaBlock = statfs.getAvailableBlocks();

                // 鑾峰彇鍓╀笅鐨勬墍鏈塀lock鐨勬暟閲�鍖呮嫭棰勭暀鐨勪竴鑸▼搴忔棤娉曚娇鐢ㄧ殑鍧�
                long nFreeBlock = statfs.getFreeBlocks();

                SDCardInfo info = new SDCardInfo();
                // 璁＄畻SDCard 鎬诲閲忓ぇ灏廙B
                info.total = nTotalBlocks * nBlocSize;

                // 璁＄畻 SDCard 鍓╀綑澶у皬MB
                info.free = nAvailaBlock * nBlocSize;

                return info;
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }

        return null;
    }

    public static void showNotification(Context context, Intent intent, String title, String body, int drawableId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(drawableId, body, System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        if (intent == null) {
            // FIXEME: category tab is disabled
            intent = new Intent(context, FileViewActivity.class);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        notification.setLatestEventInfo(context, title, body, contentIntent);
        manager.notify(drawableId, notification);
    }

    public static String formatDateString(Context context, long time) {
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat
                .getTimeFormat(context);
        Date date = new Date(time);
        if (time == 0){
            return "2015/1/1 " + timeFormat.format(date);
        }else {
            return dateFormat.format(date) + " " + timeFormat.format(date);
        }
    }

    public static void updateActionModeTitle(ActionMode mode, Context context, int selectedNum) {
        if (mode != null) {
            mode.setTitle(context.getString(R.string.multi_select_title,selectedNum));
			/*TYRD: weina 20150624 delete begin*/
           /* if(selectedNum == 0){
                mode.finish();
            }*/
            /*TYRD: weina 20150624 delete end*/
        }
    }

    public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
        {
            add("text/plain");
            add("text/plain");
            add("application/pdf");
            add("application/msword");
            add("application/vnd.ms-excel");
            add("application/vnd.ms-excel");
        }
    };

    public static String sZipFileMimeType = "application/zip";

    public static int CATEGORY_TAB_INDEX = 0;
    public static int SDCARD_TAB_INDEX = 1;
    /*TYRD: weina 20150624 add begin*/
    public static List<File> getFile(String filePath ,boolean isViewableHiddenFolder,boolean isContainFile){
    	try{
    		List <File> items = new ArrayList<File>();
    		File f = new File(filePath);
    		File[] files = f.listFiles();
    		if(null!=files){
    			for(File file : files){
    				if(file.isHidden()&&!isViewableHiddenFolder){
    					continue;
    				}
    				items.add(file);
    			}
    		}
    		return items;
    	}catch(Exception e){
    		return null;
    	}
    }

	public static Uri getMediaFileUtiForContent(File f, Context context){
       /* if(!isMediaFile(f)){
			return null ;
		}*/

		Uri[] mediaUris = new Uri[]{ MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			MediaStore.Video.Media.EXTERNAL_CONTENT_URI};
		ContentResolver cr =context.getContentResolver();
		Cursor c = null;
		try{
			for(Uri uri : mediaUris){
				c= cr.query(uri,new String[]{MediaStore.MediaColumns._ID,MediaStore.MediaColumns.DATA},
					MediaStore.MediaColumns.DATA+"=? COLLATE NOCASE",
					new String[]{f.getAbsolutePath()},null);
				if(c==null){
					continue;
				}
				int size = c.getCount();
				if (size<=0){
					c.close();
					c=null;
					continue;
				}
				c.moveToFirst();
				int id = c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID));
				Uri mUri = ContentUris.withAppendedId(uri,id);
				return mUri;
			}
		}finally{
		    if(c!=null){
				c.close();
				c=null;
			}
		}
		return null;
	}
	/*TYRD: weina 20150624 add end*/
}
