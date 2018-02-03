package com.realfame.fileexplorer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
/*TYRD: weina 20150624 add begin*/
import android.widget.TextView;
import android.widget.Button;
import android.app.AlertDialog;
import android.view.LayoutInflater;
/*TYRD: weina 20150624 add end*/
/*TYRD: weina 20150624 modity begin*/
//public class TextInputDialog extends AlertDialog {
public class TextInputDialog  {
/*TYRD: weina 20150624 modity end*/
    private String mInputText;
    private String mTitle;
    private String mMsg;
    private OnFinishListener mListener;
    private Context mContext;
    private View mView;
    private EditText mFolderName;
	private AlertDialog alertDialog;//TYRD: weina 20150624 add

    public interface OnFinishListener {
        // return true to accept and dismiss, false reject
        boolean onFinish(String text);
    }

    public TextInputDialog(Context context, String title, String msg, String text, OnFinishListener listener) {
        //super(context);//TYRD: weina 20150624 deleted
        mTitle = title;
        mMsg = msg;
        mListener = listener;
        mInputText = text;
        mContext = context;
		showDialog();//TYRD: weina 20150624 add
    }

    public String getInputText() {
        return mInputText;
    }
	/*TYRD: weina 20150624 deleted begin*/
	/*    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.textinput_dialog, null);

        setTitle(mTitle);
        setMessage(mMsg);

        mFolderName = (EditText) mView.findViewById(R.id.text);
        mFolderName.setText(mInputText);

        setView(mView);
        setButton(BUTTON_POSITIVE, mContext.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE) {
                            mInputText = mFolderName.getText().toString();
                            if (mListener.onFinish(mInputText)) {
                                dismiss();
                            }
                        }
                    }
                });
        setButton(BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel),
                (DialogInterface.OnClickListener) null);

        super.onCreate(savedInstanceState);
    }*/
	/*TYRD: weina 20150624 deleted end*/
    /*TYRD: weina 20150624 add begin*/
    protected void showDialog() {
        mView = LayoutInflater.from(mContext).inflate(R.layout.textinput_dialog, null);

        TextView messageView = (TextView)mView.findViewById(R.id.message);
        messageView.setText(mMsg);
		TextView titleView =(TextView) mView.findViewById(R.id.alertTitle);
		titleView.setText(mTitle);
		
		
        mFolderName = (EditText) mView.findViewById(R.id.text);
        mFolderName.setText(mInputText);


		Button positiveButton = (Button)mView.findViewById(R.id.button1);
        Button cancleButton = (Button)mView.findViewById(R.id.button3);
        positiveButton.setText(R.string.confirm);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				mInputText = mFolderName.getText().toString();
                if (mListener.onFinish(mInputText)) {
                    if(alertDialog!=null){
						alertDialog.dismiss();
						alertDialog=null;
					}
                }
            }
		});
		cancleButton.setText(R.string.cancel);
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
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
	/*TYRD: weina 20150624 add end*/
}
