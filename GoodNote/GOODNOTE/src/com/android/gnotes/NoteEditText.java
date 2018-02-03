package com.android.gnotes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;


public class NoteEditText extends EditText {
    private static final String TAG = "NoteEditText";
    private static final int DIVIDER_LINE_PADDING = 40;

    private Rect mRect;
    private Paint mPaint;

    public NoteEditText(Context context) {
        this(context, null);
    }

    public NoteEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub

        // Creates a Rect and a Paint object, and sets the style and color of the Paint object.
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(context.getResources().getColor(R.color.line));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();
                x += getScrollX();
                y += getScrollY();
                Layout layout = getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                Log.d(TAG, "line="+line+" off="+off);
                Selection.setSelection(getText(), off);
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float startX, y, stopX;
        Rect r = mRect;
        getDrawingRect(r);
        int lineheight = getLineHeight();
        int count =r.bottom/lineheight;
        Log.d(TAG, " lineH="+lineheight+" count:"+count);
        for (int i = 0; i < count; i++) {
            startX = r.left + DIVIDER_LINE_PADDING;
            y = (i+1)*lineheight + 5;
            stopX = r.right - DIVIDER_LINE_PADDING;
            Paint paint = mPaint;
            //FontMetrics pFontMetrics=paint.getFontMetrics();
            //Log.d(TAG, " l="+pFontMetrics.leading+" t="+pFontMetrics.top+"  a="+pFontMetrics.ascent+" d="+pFontMetrics.descent+" b="+pFontMetrics.bottom); 
            //Log.d(TAG, "line y="+y);
            canvas.drawLine(startX ,y, stopX, y, paint);
        }
        super.onDraw(canvas);
    }

}
