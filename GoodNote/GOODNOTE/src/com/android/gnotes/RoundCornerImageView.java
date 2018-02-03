package com.android.gnotes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

public class RoundCornerImageView extends ImageView {

	private int mBorderRadius;

	public RoundCornerImageView(Context context) {
		this(context, null);
	}

	public RoundCornerImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundCornerImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.roundImageAttrs);
		mBorderRadius = a.getDimensionPixelSize(R.styleable.roundImageAttrs_borderRadius,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
		a.recycle();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Path clipPath = new Path();
		int w = this.getWidth();
		int h = this.getHeight();
		clipPath.addRoundRect(new RectF(0, 0, w, h), mBorderRadius, mBorderRadius, Path.Direction.CW);
		canvas.clipPath(clipPath);
		super.onDraw(canvas);

	}

}
