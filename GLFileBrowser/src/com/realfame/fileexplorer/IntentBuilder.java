package com.realfame.fileexplorer;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
/*TYRD: weina 20150624 add begin*/
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.util.Log;
/*TYRD: weina 20150624 add end*/
public class IntentBuilder {
    /*TYRD: weina 20150624 add begin*/
    private static String path ="";
	private  static Context mContext ;
	private static AlertDialog alertDialog;
	/*TYRD: weina 20150624 add end*/
    public static void viewFile(final Context context, final String filePath) {
        String type = getMimeType(filePath);
		/*TYRD: weina 20150624 add begin*/
        path =  filePath;
		mContext = context;
		/*TYRD: weina 20150624 add end*/
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            /* 设置intent的file与MimeType */
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
            //baitao 2016.01.30 add begin
            if (type.equals("application/x-flac")) {
              try {
				intent.setClassName("com.android.music", "com.android.music.AudioPreview");
              } catch (Exception ex) {
				Log.d("baitao", "Failed to set class name: " + ex);
              }
            }
            //baitao 2016.01.30 add end
            context.startActivity(intent);
        } else {
            // unknown MimeType
			/*TYRD: weina 20150624 modity begin*/
            View mView = LayoutInflater.from(context).inflate(R.layout.ty_material_list_alert, null);
			View view = mView.findViewById(R.id.message);
			View mText = view.findViewById(R.id.text_id);
			View mAudio= view.findViewById(R.id.audio_id);
			View mVideo =view.findViewById(R.id.video_id);
			View mImage =view.findViewById(R.id.image_id);
			TextView titleView =(TextView) mView.findViewById(R.id.alertTitle);
		    titleView.setText(R.string.dialog_select_type);
			mText.setOnClickListener(clickListener);
			mAudio.setOnClickListener(clickListener);
			mVideo.setOnClickListener(clickListener);
			mImage.setOnClickListener(clickListener);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
			alertDialog = dialogBuilder.setView(mView)
					.create();
        	alertDialog.show();
            //dialogBuilder.setTitle(R.string.dialog_select_type);

            /*CharSequence[] menuItemArray = new CharSequence[] {
                    context.getString(R.string.dialog_type_text),
                    context.getString(R.string.dialog_type_audio),
                    context.getString(R.string.dialog_type_video),
                    context.getString(R.string.dialog_type_image) };
            dialogBuilder.setItems(menuItemArray,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {*/
                          //  String selectType = "*/*";
                           /* switch (which) {
                            case 0:
                                selectType = "text/plain";
                                break;
                            case 1:
                                selectType = "audio/*";
                                break;
                            case 2:
                                selectType = "video/*";
                                break;
                            case 3:
                                selectType = "image/*";
                                break;
                            }
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(filePath)), selectType);
                            context.startActivity(intent);
                        }
                    });
            dialogBuilder.show();*/
        }
    }

    private static OnClickListener clickListener = new OnClickListener() {
    
        @Override
        public void onClick(View v) {
            String selectType = "*/*";
            switch (v.getId()){

				case R.id.text_id:
                    selectType = "text/plain";
                    break;
                case R.id.audio_id:
                    selectType = "audio/*";
                    break;
                case R.id.video_id:
                    selectType = "video/*";
                    break;
                case R.id.image_id:
                    selectType = "image/*";
                    break;
				
			}

			Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(path)), selectType);
            mContext.startActivity(intent);
			if(alertDialog!=null){
    			alertDialog.dismiss();
    			alertDialog=null;
    		}
			mContext = null;
        
        }
    };
    /*TYRD: weina 20150624 modity end*/
    public static Intent buildSendFile(ArrayList<FileInfo> files) {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        String mimeType = "*/*";
        for (FileInfo file : files) {
            if (file.IsDir)
                continue;

            File fileIn = new File(file.filePath);
            mimeType = getMimeType(file.fileName);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }

        if (uris.size() == 0)
            return null;

        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE
                : android.content.Intent.ACTION_SEND);

        if (multiple) {
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
			//baitao 2016.01.14 add begin
			if (mimeType.equals("*/*")) {
				mimeType = "application/zip";
			}
			//baitao 2016.01.14 add end
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }

        return intent;
    }

    private static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1)
            return "*/*";

        String ext = filePath.substring(dotPosition + 1, filePath.length()).toLowerCase();
        String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
        if (ext.equals("mtz")) {
            mimeType = "application/miui-mtz";
        }

        return mimeType != null ? mimeType : "*/*";
    }
}
