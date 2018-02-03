package com.guli.secmanager.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

public class ListViewForFinish extends ListView {
    private OnItemTouchListener mOnItemTouchListener = null;

	public ListViewForFinish(Context context) {
		super(context);
	}

	public ListViewForFinish(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListViewForFinish(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
        //Log.i("wanghg", "onTouchEvent listview " + ev.getAction() + "  /  " + ev.getRawY());
		if(mOnItemTouchListener != null) {
            if(mOnItemTouchListener.onItemTouchEvent(ev) == true) {
                //Log.i("wanghg", "onTouchEvent return 111");
                return false;
            }
        }

        return super.onTouchEvent(ev);
	}

    public interface OnItemTouchListener {
        boolean onItemTouchEvent(MotionEvent ev);
    }

    public void setOnItemTouchListener(@Nullable OnItemTouchListener listener) {
        mOnItemTouchListener = listener;
    }
}

