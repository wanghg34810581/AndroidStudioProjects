package com.realfame.fileexplorer;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
/*TYRD: weina 20150624 add begin*/
import android.widget.Button;
import android.view.LayoutInflater;
/*TYRD: weina 20150624 add end*/
/*TYRD: weina 20150624 modity begin*/
//public class InformationDialog extends AlertDialog {
public class InformationDialog  {
/*TYRD: weina 20150624 modity  end*/
    protected static final int ID_USER = 100;
    private FileInfo mFileInfo;
    private FileIconHelper mFileIconHelper;
    private Context mContext;
    private View mView;
	private AlertDialog alertDialog;//TYRD: weina 20150624 add 

    public InformationDialog(Context context, FileInfo f, FileIconHelper iconHelper) {
        //super(context);//TYRD: weina 20150624 deleted 
        mFileInfo = f;
        mFileIconHelper = iconHelper;
        mContext = context;
		showDialog();//TYRD: weina 20150624 add
    }
/*TYRD: weina 20150624 modity begin*/
    //protected void onCreate(Bundle savedInstanceState) {
    public void showDialog(){
        //mView = getLayoutInflater().inflate(R.layout.information_dialog, null);
        mView = LayoutInflater.from(mContext).inflate(R.layout.ty_material_alert, null);
       /* if (mFileInfo.IsDir) {
            setIcon(R.drawable.folder);
            asyncGetSize();
        } else {
            setIcon(R.drawable.file_icon_default);
        }*/
        //setTitle(mFileInfo.fileName);
         View view = mView.findViewById(R.id.message);
        ((TextView) view.findViewById(R.id.information_size))
                .setText(formatFileSizeString(mFileInfo.fileSize));
        ((TextView) view.findViewById(R.id.information_location))
                .setText(mFileInfo.filePath);
        ((TextView) view.findViewById(R.id.information_modified)).setText(Util
                .formatDateString(mContext, mFileInfo.ModifiedDate));
        ((TextView) view.findViewById(R.id.information_canread))
                .setText(mFileInfo.canRead ? R.string.yes : R.string.no);
        ((TextView) view.findViewById(R.id.information_canwrite))
                .setText(mFileInfo.canWrite ? R.string.yes : R.string.no);
        ((TextView) view.findViewById(R.id.information_ishidden))
                .setText(mFileInfo.isHidden ? R.string.yes : R.string.no);

       // setView(mView);
       // setButton(BUTTON_NEGATIVE, mContext.getString(R.string.confirm_know), (DialogInterface.OnClickListener) null);

       // super.onCreate(savedInstanceState);
		TextView titleView =(TextView) mView.findViewById(R.id.alertTitle);
		titleView.setText(mFileInfo.fileName);
    	Button positiveButton = (Button)mView.findViewById(R.id.button3);
        positiveButton.setText(R.string.confirm_know);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(alertDialog!=null){
					alertDialog.dismiss();
					alertDialog=null;
				}
            }
		});
		
        AlertDialog.Builder builder =  new AlertDialog.Builder(mContext);
	    alertDialog=builder.setView(mView)
			.create();
		alertDialog.show();   
    }
    /*TYRD: weina 20150624 modity end */
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ID_USER:
                    Bundle data = msg.getData();
                    long size = data.getLong("SIZE");
                    ((TextView) mView.findViewById(R.id.information_size)).setText(formatFileSizeString(size));
            }
        };
    };

    private AsyncTask task;

    @SuppressWarnings("unchecked")
    private void asyncGetSize() {
        task = new AsyncTask() {
            private long size;

            @Override
            protected Object doInBackground(Object... params) {
                String path = (String) params[0];
                size = 0;
                getSize(path);
                task = null;
                return null;
            }

            private void getSize(String path) {
                if (isCancelled())
                    return;
                File file = new File(path);
                if (file.isDirectory()) {
                    File[] listFiles = file.listFiles();
                    if (listFiles == null)
                        return;

                    for (File f : listFiles) {
                        if (isCancelled())
                            return;

                        getSize(f.getPath());
                    }
                } else {
                    size += file.length();
                    onSize(size);
                }
            }

        }.execute(mFileInfo.filePath);
    }

    private void onSize(final long size) {
        Message msg = new Message();
        msg.what = ID_USER;
        Bundle bd = new Bundle();
        bd.putLong("SIZE", size);
        msg.setData(bd);
        mHandler.sendMessage(msg); // 向Handler发送消息,更新UI
    }

    private String formatFileSizeString(long size) {
        String ret = "";
        if (size >= 1024) {
            ret = Util.convertStorage(size);
            ret += (" (" + mContext.getResources().getString(R.string.file_size, size) + ")");
        } else {
            ret = mContext.getResources().getString(R.string.file_size, size);
        }

        return ret;
    }
}
