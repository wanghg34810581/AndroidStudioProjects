package com.guli.secmanager.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.guli.secmanager.Utils.UnitConverter;
import com.guli.secmanager.R;

public class RoundProgressBarWidthNumber extends
		HorizontalProgressBarWithNumber
{
	private static final int DEFAULT_OUTER_COLOR = 0Xffa1da88; // 外圈默认颜色
	private static final int DEFAULT_CIRCLE_COLOR = 0Xffd3eec8; // 进度条默认颜色
	private static final int DEFAULT_CIRCLE_BACKGROUND_COLOR = 0Xff6bc545; // 进度条背景默认颜色
	private static final int DEFAULT_SHADOW_COLOR = 0Xff389a0e; // 阴影内圈默认颜色
	/**
	 * mRadius of view
	 */
	private int mOuterCircleRadius = UnitConverter.dp2px(this, 114);
	private int mOuterCircleColor = DEFAULT_OUTER_COLOR;
	private int mOuterCirclePaintWidth = UnitConverter.dp2px(this, 1);
	private int mCircleProgressBarRadius = UnitConverter.dp2px(this, 101);
	private int mCircleProgressBarColor = DEFAULT_CIRCLE_COLOR;
	private int mCircleProgressBarBackgroundColor = DEFAULT_CIRCLE_BACKGROUND_COLOR;
	private int mCircleProgressBarPaintWidth = UnitConverter.dp2px(this, 9);
	private int mShadowRadius = UnitConverter.dp2px(this, 92);
	private int mShadowColor = DEFAULT_SHADOW_COLOR;
	private int mShadowCirclePatinWidth = UnitConverter.dp2px(this, 1);

	private int mMaxPaintWidth;

	public RoundProgressBarWidthNumber(Context context)
	{
		this(context, null);
	}

	public RoundProgressBarWidthNumber(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBarWidthNumber);
		mOuterCircleRadius = (int) ta.getDimension(R.styleable.RoundProgressBarWidthNumber_outer_circle_radius, mOuterCircleRadius);
		mOuterCircleColor = (int) ta.getColor(R.styleable.RoundProgressBarWidthNumber_outer_circle_radius_color, mOuterCircleColor);
		mOuterCirclePaintWidth = (int) ta.getDimension(R.styleable.RoundProgressBarWidthNumber_outer_circle_height, mOuterCirclePaintWidth);
		mCircleProgressBarRadius = (int) ta.getDimension(R.styleable.RoundProgressBarWidthNumber_radius, mCircleProgressBarRadius);
		mCircleProgressBarColor = (int) ta.getColor(R.styleable.RoundProgressBarWidthNumber_radius_color, mCircleProgressBarColor);
		mCircleProgressBarBackgroundColor = (int) ta.getColor(R.styleable.RoundProgressBarWidthNumber_radius_background_color, mCircleProgressBarBackgroundColor);
		mCircleProgressBarPaintWidth = (int) ta.getDimension(R.styleable.RoundProgressBarWidthNumber_radius_height, mCircleProgressBarPaintWidth);
		mShadowRadius = (int) ta.getDimension(R.styleable.RoundProgressBarWidthNumber_shadow_radius, mShadowRadius);
		mShadowColor = (int) ta.getColor(R.styleable.RoundProgressBarWidthNumber_shadow_color, mShadowColor);
		mShadowCirclePatinWidth = (int) (int) ta.getDimension(R.styleable.RoundProgressBarWidthNumber_shadow_height, mShadowCirclePatinWidth);
		ta.recycle();

		mPaint.setStyle(Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStrokeCap(Cap.SQUARE);
		/*
		mReachedProgressBarHeight = (int) (mUnReachedProgressBarHeight); // 相等，差别仅是颜色
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.RoundProgressBarWidthNumber);
		mRadius = (int) ta.getDimension(
				R.styleable.RoundProgressBarWidthNumber_radius, mRadius);
		mCircleRadius = (int) ta.getDimension(R.styleable.RoundProgressBarWidthNumber_circle_radius, mCircleRadius);
		// 内圈阴影半径
		mShadow_radius = (int) ta.getDimension(R.styleable.RoundProgressBarWidthNumber_shadow_radius, mShadow_radius);
		// 内圈阴影颜色

		ta.recycle();

		mPaint.setStyle(Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStrokeCap(Cap.SQUARE);
		*/

	}

	/**
	 * 这里默认在布局中padding值要么不设置，要么全部设置
	 */
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec)
	{
		int expect = mOuterCircleRadius * 2 + mOuterCirclePaintWidth + getPaddingLeft() + getPaddingRight();
		int width = resolveSize(expect, widthMeasureSpec);
		int height = resolveSize(expect, heightMeasureSpec);
		int realWidth = Math.min(width, height);

		mOuterCircleRadius = (realWidth - getPaddingLeft() - getPaddingRight() - mOuterCirclePaintWidth) / 2;

		setMeasuredDimension(realWidth, realWidth);
		/*
		mMaxPaintWidth = Math.max(mReachedProgressBarHeight,
				mUnReachedProgressBarHeight);
		int expect = mOuterCircleRadius * 2 + mMaxPaintWidth + getPaddingLeft()
				+ getPaddingRight();
		int width = resolveSize(expect, widthMeasureSpec);
		int height = resolveSize(expect, heightMeasureSpec);
		int realWidth = Math.min(width, height);

		mRadius = (realWidth - getPaddingLeft() - getPaddingRight() - mMaxPaintWidth) / 2;

		setMeasuredDimension(realWidth, realWidth);
		*/
	}

	@Override
	protected synchronized void onDraw(Canvas canvas)
	{

		String text = getProgress() + "%";
		float textWidth = mPaint.measureText(text);
		float textHeight = (mPaint.descent() + mPaint.ascent()) / 2;

		canvas.save();
		canvas.translate(getPaddingLeft() + mOuterCirclePaintWidth / 2, getPaddingTop()
				+ mOuterCirclePaintWidth / 2);
		mPaint.setStyle(Style.STROKE);
		// draw outer circle
		mPaint.setColor(mOuterCircleColor);
		mPaint.setStrokeWidth(mOuterCirclePaintWidth);
		canvas.drawArc(new RectF(0, 0, mOuterCircleRadius * 2, mOuterCircleRadius * 2), 133, 273.5f, false, mPaint);

		// draw Shadow circle

		mPaint.setColor(mShadowColor);
		mPaint.setStrokeWidth(mShadowCirclePatinWidth);
		canvas.drawArc(new RectF((mOuterCircleRadius - mShadowRadius) * 2, (mOuterCircleRadius - mShadowRadius) * 2, mShadowRadius * 2, mShadowRadius * 2), 132, 275.5f, false, mPaint);

		// draw background circle progress bar

		mPaint.setColor(mCircleProgressBarBackgroundColor);
		mPaint.setStrokeWidth(mCircleProgressBarPaintWidth);
		canvas.drawArc(new RectF((mOuterCircleRadius - mCircleProgressBarRadius) * 2, (mOuterCircleRadius - mCircleProgressBarRadius) * 2, mCircleProgressBarRadius * 2, mCircleProgressBarRadius * 2), 135, 270, false, mPaint);

		// draw circle progress bar

		mPaint.setColor(mCircleProgressBarColor);
		mPaint.setStrokeWidth(mCircleProgressBarPaintWidth);
		float sweepAngle = getProgress() * 1.0f / getMax() * 270;
		if (getProgress() > 0) {

			canvas.drawArc(new RectF((mOuterCircleRadius - mCircleProgressBarRadius) * 2, (mOuterCircleRadius - mCircleProgressBarRadius) * 2, mCircleProgressBarRadius * 2, mCircleProgressBarRadius * 2), 135, sweepAngle, false, mPaint);
		}

		// draw text
		/*
		mPaint.setColor(mTextColor);
		mPaint.setStyle(Style.FILL);
		canvas.drawText(text, mRadius - textWidth / 2, mRadius - textHeight,
				mPaint);
		*/
		canvas.restore();
	}

}
