package com.realfame.fileexplorer;



import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;

import com.realfame.fileexplorer.FileCategoryHelper.FileCategory;
import com.realfame.fileexplorer.FileIconLoader.IconLoadFinishListener;
/*TYRD: weina 20150624 add begin*/
import android.graphics.drawable.Drawable;
import android.util.Log;
/*TYRD: weina 20150624 add end*/
public class FileIconHelper /*implements IconLoadFinishListener*/ {

    private static final String LOG_TAG = "FileIconHelper";

    private  HashMap<ImageView, ImageView> imageFrames = new HashMap<ImageView, ImageView>();

    private  HashMap<String, Integer> fileExtToIcons = new HashMap<String, Integer>();

	IconLoadFinishListener listerner  = new IconLoadFinishListener(){
		@Override
        public void onIconLoadFinished(ImageView view) {
            ImageView frame = imageFrames.get(view);
            if (frame != null) {
                frame.setVisibility(View.VISIBLE);
                imageFrames.remove(view);
    			view =null;
            }
    		
        }
	};

    private FileIconLoader mIconLoader;

     {
        addItem(new String[] {
            "mp3"
        }, R.drawable.ty_ic_file_music);
		//baitao 2016.02.26 add begin
		addItem(new String[] {
            "flac"
        }, R.drawable.ty_ic_file_music);
		//baitao 2016.02.26 add end
        addItem(new String[] {
            "wma"
        }, R.drawable.ty_ic_file_music);
        addItem(new String[] {
            "wav"
        }, R.drawable.ty_ic_file_music);
        addItem(new String[] {
            "mid"
        }, R.drawable.ty_ic_file_music);
        addItem(new String[] {
                "mp4", "wmv", "mpeg", "m4v", "3gp", "3gpp", "3g2", "3gpp2", "asf"
        }, R.drawable.ty_ic_file_video);
        addItem(new String[] {
                "jpg", "jpeg", "gif", "png", "bmp", "wbmp"
        }, R.drawable.ty_ic_file_image);
        addItem(new String[] {
                "txt", /*"log",*/ "xml", "ini", "lrc"
        }, R.drawable.ty_ic_file_txt);
        addItem(new String[] {
                "doc", "docx",
        }, R.drawable.ty_ic_file_word);
		addItem(new String[] {"ppt", "pptx",
        }, R.drawable.ty_ic_file_ppt);
		addItem(new String[] { "xsl", "xslx",
        }, R.drawable.ty_ic_file_excel);
        addItem(new String[] {
            "pdf"
        }, R.drawable.ty_ic_file_pdf);
        addItem(new String[] {
            "zip"
        }, R.drawable.file_icon_zip);
        addItem(new String[] {
            "mtz"
        }, R.drawable.file_icon_theme);
        addItem(new String[] {
            "rar"
        }, R.drawable.file_icon_rar);
    }

    public FileIconHelper(Context context) {
        mIconLoader = new FileIconLoader(context/*, this*/);
		mIconLoader.setOnIconLoadFinishListener(listerner);
    }

    private  void addItem(String[] exts, int resId) {
        if (exts != null) {
            for (String ext : exts) {
                fileExtToIcons.put(ext.toLowerCase(), resId);
            }
        }
    }

    public  int getFileIcon(String ext) {
        Integer i = fileExtToIcons.get(ext.toLowerCase());
        if (i != null) {
            return i.intValue();
        } else {
            return R.drawable.ty_ic_file_unknown;
        }

    }
	/*TYRD: weina 20150624 modify begin*/
    //public void setIcon(FileInfo fileInfo, ImageView fileImage, ImageView fileImageFrame) {
    public void setIcon(FileInfo fileInfo, ImageView fileImage, ImageView fileImageFrame,Context context) {
	/*TYRD: weina 20150624 modify end*/
        String filePath = fileInfo.filePath;
        long fileId = fileInfo.dbId;
        String extFromFilename = Util.getExtFromFilename(filePath);
        FileCategory fc = FileCategoryHelper.getCategoryFromPath(filePath);
        fileImageFrame.setVisibility(View.GONE);
        boolean set = false;
        int id = getFileIcon(extFromFilename);
		/*TYRD: weina 20150624 add begin*/
		Drawable dr = context.getApplicationContext().getResources().getDrawable(R.drawable.folder);
		fileImage.setAdjustViewBounds(true);
		fileImage.setMinimumHeight(dr.getIntrinsicHeight());
		fileImage.setMaxHeight(dr.getIntrinsicHeight());
		fileImage.setMinimumWidth(dr.getIntrinsicWidth());
		fileImage.setMaxWidth(dr.getIntrinsicWidth());
		/*TYRD: weina 20150624 add end*/
        fileImage.setImageResource(id);
        mIconLoader.cancelRequest(fileImage);
		mIconLoader.removeCachedIcon(filePath);//TYRD:weina 20150902 add for PROD104004711 
        switch (fc) {
            case Apk:
			    /*TYRD: weina 20150624 modify begin*/
                //set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
                fileImage.setImageResource(R.drawable.ty_ic_file_apk);
                imageFrames.put(fileImage, fileImageFrame);
				set = true;
				/*TYRD: weina 20150624 modify end*/
                break;
            case Picture:
            case Video:
				//add by chen he 2016.02.23
				if (fileId <= 0) {
					fileId = Util.getDbId(context.getApplicationContext(), fileInfo.filePath);
				}
				//add end
                set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
                if (set)
                    fileImageFrame.setVisibility(View.VISIBLE);
                else {
                    fileImage.setImageResource(fc == FileCategory.Picture ? R.drawable.ty_ic_file_image
                            : R.drawable.ty_ic_file_video);
                    imageFrames.put(fileImage, fileImageFrame);
					set = true;
                }
                break;
			/*TYRD: weina 20150624 add begin */
			case Music:
				fileImage.setImageResource(R.drawable.ty_ic_file_music);
                imageFrames.put(fileImage, fileImageFrame);
				set = true;
				break;
			/*TYRD: weina 20150624 modify end */
            default:
                set = true;
                break;
        }

        if (!set)
            fileImage.setImageResource(R.drawable.ty_ic_file_unknown);
    }

    

}
