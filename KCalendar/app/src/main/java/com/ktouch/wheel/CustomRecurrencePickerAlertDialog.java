package com.ktouch.wheel;


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

import com.ktouch.kcalendar.R;

public class CustomRecurrencePickerAlertDialog {
	private Context context;
	private Dialog mDialog;
	private LinearLayout mRecurrenceBackground;
	private TextView mNumField;
	private TextView mSuffixesField;

	private LinearLayout mDialogGroup;
	private Button mNegativeBtn;
	private Button mPositiveBtn;
	private Display display;

	public CustomRecurrencePickerAlertDialog(Context context) {
		this.context = context;
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
	}

	public CustomRecurrencePickerAlertDialog builder() {
		View view = LayoutInflater.from(context).inflate(R.layout.custom_recurrence_picker_alert_dialog, null);

		mRecurrenceBackground = (LinearLayout) view.findViewById(R.id.recurrence_bg);
		mNumField = (TextView) view.findViewById(R.id.num_title);
		mSuffixesField = (TextView) view.findViewById(R.id.suffixes_title);

		mDialogGroup = (LinearLayout) view.findViewById(R.id.recurrence_dialog_group);
		mNegativeBtn = (Button) view.findViewById(R.id.recurrence_neg);
		mPositiveBtn = (Button) view.findViewById(R.id.recurrence_pos);

		mDialog = new Dialog(context, R.style.DateAndTimePickerDialogStyle);
		mDialog.setContentView(view);

		mRecurrenceBackground.setLayoutParams(new FrameLayout.LayoutParams((int) (display.getWidth() * 0.85), LayoutParams.WRAP_CONTENT));
		return this;
	}

	public CustomRecurrencePickerAlertDialog setTitle(String title) {
		if ("".equals(title)) {
			mNumField.setText("标题");
		} else {
			mNumField.setText(title);
		}
		return this;
	}

	public TextView getNumField(){
		return mNumField;
	}

	public TextView getSuffixesField(){
		return mSuffixesField;
	}


	public CustomRecurrencePickerAlertDialog setView(View view) {
			mDialogGroup.addView(view, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		return this;
	}

	public CustomRecurrencePickerAlertDialog setCancelable(boolean cancel) {
		mDialog.setCancelable(cancel);
		return this;
	}

	public CustomRecurrencePickerAlertDialog setPositiveButton(String text, final OnClickListener listener) {
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

	public CustomRecurrencePickerAlertDialog setNegativeButton(String text, final OnClickListener listener) {
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
