package com.android.gnotes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import java.lang.reflect.Field;

public class LineEditText extends EditText {
	private Rect mRect;
	private Paint mPaint;
    private static final String TAG = "LineEditText";
	private static int bottomY;
	private boolean  bLastNewCursor = false;
	private int  linewidth;	
	public LineEditText(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		super(context,attrs);
		mRect = new Rect();
		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(R.color.edit_text_line_color));
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		linewidth = wm.getDefaultDisplay().getWidth();		
	}
	
	public void SetLineColor(int color) {
		mPaint.setColor(color);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int lineCount = getLineCount();
		Rect r = mRect;
		Paint p = mPaint;
		int line_padding = getResources().getDimensionPixelSize(R.dimen.g_note_line_padding);
		int line_height = getLineHeight();
		int height =  getHeight();
		int count = height / line_height;
		//Log.i(TAG, "lineheight=" + line_height + " lineCount=" + lineCount + " count=" + count);
		
		int tempbaseline = 0;
		for(int i = 0; i < lineCount;i++){
			int baseline = getLineBounds(i, r);
			if (tempbaseline > 0 && (baseline - tempbaseline)<line_height) {
				baseline = tempbaseline + line_height;
            }
			tempbaseline = baseline;
			canvas.drawLine(0, baseline + line_padding, linewidth, baseline + line_padding, p);
			bottomY = baseline + line_padding;
			//Log.i(TAG, "onDraw baseline " + baseline + " bottomY=" + bottomY);
		}

		if (count > lineCount) {
			int baseline = bottomY;
			for(int i = lineCount; i < count; i++) {
				baseline = baseline + line_height;
				canvas.drawLine(0, baseline, linewidth, baseline, p);
			}
		}

    }

	public int getLineEditTextBottom() {
		//Log.i(TAG, "getLineEditTextBottom " + bottomY);	
		return bottomY;
	}	

	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		// TODO Auto-generated method stub
		super.onSelectionChanged(selStart, selEnd);
		//Log.i(TAG, "cursor onSelectionChanged start=" + selStart + " end=" + selEnd);

		setCursorDrawableColor();
		if (!isCursorVisible()) {
			setCursorVisible(true);
		}
			
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		//Log.i(TAG, "onLayout layout is " + getLayout());
		setCursorDrawableColor();
		if (!isCursorVisible()) {
			setCursorVisible(true);
		}		
	}
	
	protected void setCursorDrawableColor() {
		int off = getSelectionStart();
		int line_height = getLineHeight();
		int line = 0, top = 0, bottom = 0, act_height = 0, textHeight = 0;
		boolean bNewCursor = false;
		//Log.i(TAG, "cursor off=" + off + " layout is " + getLayout());
		TextPaint tPaint = getPaint();
		if (tPaint != null) {
			textHeight = tPaint.getFontMetricsInt(null);
			//Log.i(TAG, "cursor textHeight=" + textHeight + " size=" + tPaint.getTextSize() + " scaledszie=" + tPaint.getTextSize()/tPaint.density);

		}
			  
		if (getLayout() != null) {
			line = getLayout().getLineForOffset(off);
			top = getLayout().getLineTop(line);
			bottom = getLayout().getLineTop(line + 1);
			act_height = bottom -top;
		} else {
			return;
		}
		//Log.i(TAG, "cursor line=" + line + " line_height=" + NotesUtils.px2dip(getContext(), line_height) + " act_height=" + NotesUtils.px2dip(getContext(), act_height));

		if (act_height < line_height ||textHeight > getContext().getResources().getDimensionPixelSize(R.dimen.g_note_text_height)) {
			bNewCursor = true;
		} else {
			bNewCursor = false;
		}
		
		if (bNewCursor != bLastNewCursor) {
			try {
				Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
				fCursorDrawableRes.setAccessible(true);
				int mCursorDrawableRes = fCursorDrawableRes.getInt(this);
				Field fEditor = TextView.class.getDeclaredField("mEditor");
				fEditor.setAccessible(true);
				Object editor = fEditor.get(this);
				Class<?> clazz = editor.getClass();
				Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
				fCursorDrawable.setAccessible(true);
				Drawable[] drawables = new Drawable[2];
				drawables[0] = getContext().getResources().getDrawable(mCursorDrawableRes);
				drawables[1] = getContext().getResources().getDrawable(mCursorDrawableRes);
				if (bNewCursor) {
					//Log.i(TAG, "cursor set new cursor");
					drawables[0] = getContext().getResources().getDrawable(R.drawable.new_cursor);
					drawables[1] = getContext().getResources().getDrawable(R.drawable.new_cursor);
					fCursorDrawable.set(editor, drawables);
				} else {
					//Log.i(TAG, "cursor set short cursor");
					drawables[0] = getContext().getResources().getDrawable(R.drawable.short_cursor);
					drawables[1] = getContext().getResources().getDrawable(R.drawable.short_cursor);
					fCursorDrawable.set(editor, drawables);
				}
			} catch (Throwable ignored) {
				Log.i(TAG, "cursor setCursorDrawableColor catch " + ignored);
			}
		}
		bLastNewCursor = bNewCursor;
	}	
}