//yuhf add for new screen delete dialog
package com.android.gnotes;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Button;
import android.widget.TextView;

public class CustomDeleteDialog implements View.OnClickListener {

	private Dialog dialog;
	private Context mContext;
	private TextView delInfo;
	private Button cacelBtn;
	private Button sureBtn;
	private View.OnClickListener sureClicListener, cancelClickListener;

	public CustomDeleteDialog(Context context) {
		this.mContext = context;
		init();
	}

	private void init() {
		dialog = new Dialog(mContext, R.style.alert_dialog);
		dialog.setContentView(R.layout.delete_dialog_layout);
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = mContext.getResources().getDisplayMetrics().widthPixels;
		dialog.getWindow().setAttributes(params);

		delInfo = (TextView) dialog.findViewById(R.id.delete_info);
		cacelBtn = (Button)dialog.findViewById(R.id.cancel_btn);
		sureBtn = (Button)dialog.findViewById(R.id.sure_btn);

		setListeners();
	}

	public void showDialog() {
		if (dialog != null && !dialog.isShowing())
			dialog.show();
	}

	public void hideDialog() {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}

	private void setListeners() {
		sureBtn.setOnClickListener(this);
		cacelBtn.setOnClickListener(this);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (cancelClickListener != null) {
					cancelClickListener.onClick(cacelBtn);
				}
			}
		});
	}

	public CustomDeleteDialog setMessage(String message) {
		delInfo.setText(message);
		return this;
	}

	public CustomDeleteDialog setMessage(int messageId) {
		delInfo.setText(messageId);
		return this;
	}

	public CustomDeleteDialog setSureBtn(View.OnClickListener onClickListener) {
		return setSureBtn(R.string.sure_del, onClickListener);
	}

	public CustomDeleteDialog setSureBtn(int strId, View.OnClickListener onClickListener) {
		return setSureBtn(mContext.getString(strId), onClickListener);
	}

	public CustomDeleteDialog setSureBtn(String str, View.OnClickListener onClickListener) {
		sureBtn.setVisibility(View.VISIBLE);
		sureBtn.setText(str);
		sureClicListener = onClickListener;
		return this;
	}

	public CustomDeleteDialog setCancelBtn(View.OnClickListener onClickListener) {
		return setCancelBtn(R.string.cancel, onClickListener);
	}

	public CustomDeleteDialog setCancelBtn(int strId, View.OnClickListener onClickListener) {
		return setCancelBtn(mContext.getString(strId), onClickListener);
	}

	public CustomDeleteDialog setCancelBtn(String str, View.OnClickListener onClickListener) {
		cacelBtn.setVisibility(View.VISIBLE);
		cacelBtn.setText(str);
		cancelClickListener = onClickListener;
		return this;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.sure_btn:
			dialog.dismiss();
			if (sureClicListener != null) {
				sureClicListener.onClick(v);
			}
			break;
		case R.id.cancel_btn:
			dialog.dismiss();
			if (cancelClickListener != null) {
				cancelClickListener.onClick(v);
			}
			break;
		}
	}
}
