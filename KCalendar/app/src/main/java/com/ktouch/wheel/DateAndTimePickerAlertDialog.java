package com.ktouch.wheel;


import com.ktouch.kcalendar.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class DateAndTimePickerAlertDialog {
	private Context context;
	private Dialog mDialog;
	private LinearLayout lLayout_bg;
	private TextView mDateField;
	private TextView mTimeField;

	private LinearLayout mDialogGroup;
	private Button mNegativeBtn;
	private Button mPositiveBtn;
	private Display display;

	public DateAndTimePickerAlertDialog(Context context) {
		this.context = context;
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
	}

	public DateAndTimePickerAlertDialog builder() {
		// 获取Dialog布局
		View view = LayoutInflater.from(context).inflate(R.layout.date_and_time_picker_alert_dialog, null);

		// 获取自定义Dialog布局中的控件
		lLayout_bg = (LinearLayout) view.findViewById(R.id.lLayout_bg);
		mDateField = (TextView) view.findViewById(R.id.date_title);
		mTimeField = (TextView) view.findViewById(R.id.time_title);

		mDialogGroup = (LinearLayout) view.findViewById(R.id.dialog_group);
		mNegativeBtn = (Button) view.findViewById(R.id.btn_neg);
		mPositiveBtn = (Button) view.findViewById(R.id.btn_pos);

		// 定义Dialog布局和参数
		mDialog = new Dialog(context, R.style.DateAndTimePickerDialogStyle);
		mDialog.setContentView(view);

		// 调整dialog背景大小
		lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (display.getWidth() * 0.95), LayoutParams.WRAP_CONTENT));

		return this;
	}

	public DateAndTimePickerAlertDialog setTitle(String title) {
		if ("".equals(title)) {
			mDateField.setText("标题");
		} else {
			mDateField.setText(title);
		}
		return this;
	}

	public TextView getDateField(){
		return mDateField;
	}

	public TextView getTimeField(){
		return mTimeField;
	}


	public DateAndTimePickerAlertDialog setView(View view) {
			mDialogGroup.addView(view, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		return this;
	}

	public DateAndTimePickerAlertDialog setCancelable(boolean cancel) {
		mDialog.setCancelable(cancel);
		return this;
	}

	public DateAndTimePickerAlertDialog setPositiveButton(String text, final OnClickListener listener) {
		if (text.isEmpty()) {
			mPositiveBtn.setText(context.getText(R.string.positive_button));
		} else {
			mPositiveBtn.setText(text);
		}
		mPositiveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(v);
				mDialog.dismiss();
			}
		});
		return this;
	}

	public DateAndTimePickerAlertDialog setNegativeButton(String text, final OnClickListener listener) {
		if (text.isEmpty()) {
			mNegativeBtn.setText(context.getText(R.string.negative_button));
		} else {
			mNegativeBtn.setText(text);
		}
		mNegativeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(v);
				mDialog.dismiss();
			}
		});
		return this;
	}

	public void show() {
		mDialog.show();
	}
}
