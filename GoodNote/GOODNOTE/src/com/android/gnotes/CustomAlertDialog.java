/*TY zhencc 20160902 add for PROD104179658*/
package com.android.gnotes;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class CustomAlertDialog implements View.OnClickListener {

	private Dialog dialog;
	private Context mContext;
	private TextView titleTv, messageTv, sure, cancel;
	private View.OnClickListener sureClicListener, cancelClickListener;

	public CustomAlertDialog(Context context) {
		this.mContext = context;
		init();
	}

	private void init() {
		dialog = new Dialog(mContext, R.style.alert_dialog);
		dialog.setContentView(R.layout.alert_dialog_layout);
		dialog.getWindow().setGravity(Gravity.CENTER);
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.8f);
		dialog.getWindow().setAttributes(params);

		titleTv = (TextView) dialog.findViewById(R.id.dialog_title);
		messageTv = (TextView) dialog.findViewById(R.id.message);
		sure = (TextView) dialog.findViewById(R.id.sure_btn);
		cancel = (TextView) dialog.findViewById(R.id.cancel_btn);

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
		sure.setOnClickListener(this);
		cancel.setOnClickListener(this);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (cancelClickListener != null) {
					cancelClickListener.onClick(cancel);
				}
			}
		});
	}

	public CustomAlertDialog hideTitleTv() {
		titleTv.setVisibility(View.GONE);
		return this;
	}

	public CustomAlertDialog setTitle(String title) {
		titleTv.setText(title);
		return this;
	}

	public CustomAlertDialog setTitle(int titleId) {
		titleTv.setText(titleId);
		return this;
	}

	public CustomAlertDialog setMessage(String message) {
		messageTv.setText(message);
		return this;
	}

	public CustomAlertDialog setMessage(int messageId) {
		messageTv.setText(messageId);
		return this;
	}

	public CustomAlertDialog setSureBtn(View.OnClickListener onClickListener) {
		return setSureBtn(R.string.sure, onClickListener);
	}

	public CustomAlertDialog setSureBtn(int strId, View.OnClickListener onClickListener) {
		return setSureBtn(mContext.getString(strId), onClickListener);
	}

	public CustomAlertDialog setSureBtn(String str, View.OnClickListener onClickListener) {
		sure.setVisibility(View.VISIBLE);
		sure.setText(str);
		sureClicListener = onClickListener;
		return this;
	}

	public CustomAlertDialog setCancelBtn(View.OnClickListener onClickListener) {
		return setCancelBtn(R.string.cancel, onClickListener);
	}

	public CustomAlertDialog setCancelBtn(int strId, View.OnClickListener onClickListener) {
		return setCancelBtn(mContext.getString(strId), onClickListener);
	}

	public CustomAlertDialog setCancelBtn(String str, View.OnClickListener onClickListener) {
		cancel.setVisibility(View.VISIBLE);
		cancel.setText(str);
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
