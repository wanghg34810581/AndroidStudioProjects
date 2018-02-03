/**
 * 
 */
package com.guli.secmanager.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.guli.secmanager.R;

/**
 * @author kince
 * @category View必须是正方形
 * 
 */
public class RadarView extends View {

	private Context mContext;

	private int mScreenWidth;
	private int mScreenHeight;

	private Paint mRingPaint;
	private Paint mCirclePaint;
	private Paint flowPaint;
    private Paint flowReversePaint;
	private Paint leftReversePaint;

	private int mRingSTROKEWidth = 15;
	private int mCircleSTROKEWidth = 1;
    private int mCircleMargin = 10;
	private int mProgressMargin = 6;

	private int mCircleColor = getResources().getColor(R.color.green);
	private int mRingColor = getResources().getColor(R.color.white);

	private Handler mHandler;
	private long c1 = 0L;
    private long c2 = 0L;
    private long Speed1 = 1L;
    private long Speed2 = 2L;
	private boolean mStarted = false;
	private final float f = 0.033F;
	private int mAlpha = 50;// 透明度
	private float mAmplitude = 10.0F; // 振幅
	private int mWaterLevel = 0;
	private Path mPath;

    private String unit_string = "%";
    private static final int DEFAULT_CIRCLE_COLOR = 0Xff6bc545; // 进度条默认颜色
    private static final int DEFAULT_CIRCLE_BACKGROUND_COLOR = 0Xffc0c0c0; // 进度条背景默认颜色
	private Bitmap bitmapRadar = null;
	private float degrees = 0f;

	/**
	 * @param context
	 */
	public RadarView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		init(mContext);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public RadarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		init(mContext);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 */
	public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		mContext = context;
		init(mContext);
	}

    public void setProgress(int progress) {
        mWaterLevel = progress;
        //Log.i("wanghg", "mWaterLevel : " + mWaterLevel);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private void init(Context context) {
		mRingPaint = new Paint();
		mRingPaint.setColor(mRingColor);
		mRingPaint.setAlpha(mAlpha);
		mRingPaint.setStyle(Paint.Style.STROKE);
		mRingPaint.setAntiAlias(true);
		mRingPaint.setStrokeWidth(mRingSTROKEWidth);

		mCirclePaint = new Paint();
		mCirclePaint.setColor(mCircleColor);
		mCirclePaint.setStyle(Paint.Style.STROKE);
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setStrokeWidth(mCircleSTROKEWidth);

		flowPaint = new Paint();
		flowPaint.setColor(getResources().getColor(R.color.green));
		flowPaint.setStyle(Paint.Style.FILL);
		flowPaint.setAntiAlias(true);
		flowPaint.setTextSize(sp2px(context, 40));

        flowReversePaint = new Paint();
        flowReversePaint.setColor(getResources().getColor(R.color.white));
        flowReversePaint.setStyle(Paint.Style.FILL);
        flowReversePaint.setAntiAlias(true);
        flowReversePaint.setTextSize(sp2px(context, 50));

        leftReversePaint = new Paint();
        leftReversePaint.setColor(getResources().getColor(R.color.white));
        leftReversePaint.setStyle(Paint.Style.FILL);
        leftReversePaint.setAntiAlias(true);
        leftReversePaint.setTextSize(sp2px(context, 12));

		mPath = new Path();

		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				if (msg.what == 0) {
					if (mStarted) {
						// 不断发消息给自己，使自己不断被重绘
						invalidate();
						mHandler.sendEmptyMessageDelayed(0, 5L);
					}
				}
			}
		};

		try {
		   // 实例化Bitmap
			bitmapRadar = BitmapFactory.decodeResource(getResources(),
					R.drawable.radar);
		} catch (OutOfMemoryError e) {
			Log.e("wanghg", "RadarView OutOfMemoryError!");
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("wanghg", "widthMeasureSpec : " + widthMeasureSpec + "   heightMeasureSpec : " + heightMeasureSpec);
		int width = measure(widthMeasureSpec, true);
		int height = measure(heightMeasureSpec, true);
        //Log.i("wanghg", "width : " + width + "   height : " + height);
		if (width < height) {
			setMeasuredDimension(width, width);
		} else {
			setMeasuredDimension(height, height);
		}

	}

	/**
	 * @category 测量
	 * @param measureSpec
	 * @param isWidth
	 * @return
	 */
	private int measure(int measureSpec, boolean isWidth) {
		int result;
		int mode = MeasureSpec.getMode(measureSpec);
		int size = MeasureSpec.getSize(measureSpec);
		int padding = isWidth ? getPaddingLeft() + getPaddingRight()
				: getPaddingTop() + getPaddingBottom();
		if (mode == MeasureSpec.EXACTLY) {
			result = size;
		} else {
			result = isWidth ? getSuggestedMinimumWidth()
					: getSuggestedMinimumHeight();
			result += padding;
			if (mode == MeasureSpec.AT_MOST) {
				if (isWidth) {
					result = Math.max(result, size);
				} else {
					result = Math.min(result, size);
				}
			}
		}
		return result;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		mScreenWidth = w;
		mScreenHeight = h;
        //mWaterRadius = mScreenWidth / 2 - mRingSTROKEWidth - mCircleSTROKEWidth - mCircleMargin;
		//Log.i("wanghg", "mScreenWidth : " + mScreenWidth + "   mScreenHeight : " + mScreenHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		//canvas.drawCircle(mScreenWidth / 2, mScreenHeight / 2,
		//		mScreenWidth / 2 - mCircleMargin, mRingPaint);

		if(bitmapRadar != null) {
			canvas.save();
			degrees = (degrees += 2) >= 360 ? 0 : degrees;
			canvas.rotate(degrees, mScreenWidth / 2, mScreenHeight / 2);

			canvas.drawBitmap(bitmapRadar, (mScreenWidth - bitmapRadar.getWidth()) / 2, (mScreenHeight - bitmapRadar.getHeight()) / 2, null);
			canvas.restore();
		}

		String sProgress = mWaterLevel + "";
        float scan_ongoing = flowReversePaint.measureText(sProgress);
        //Log.i("wanghg", "scan_ongoing : " + scan_ongoing);
        canvas.drawText(sProgress, mScreenWidth / 2 - scan_ongoing / 2,
                mScreenHeight / 2 + scan_ongoing / (2 *sProgress.length()), flowReversePaint);
        float unit = leftReversePaint.measureText(unit_string);
        //Log.i("wanghg", "unit : " + unit);
        canvas.drawText(unit_string, mScreenWidth / 2 + scan_ongoing / 2,
                mScreenHeight / 2 - scan_ongoing / (2 *sProgress.length()), leftReversePaint);

        //进度条
        float sweepAngle2 = (float) mWaterLevel / 100f * 360f;
		mCirclePaint.setColor(DEFAULT_CIRCLE_BACKGROUND_COLOR);
		canvas.drawArc(new RectF(mProgressMargin, mProgressMargin, mScreenWidth - mProgressMargin, mScreenHeight - mProgressMargin), 270, 360, false, mCirclePaint);
		mCirclePaint.setColor(DEFAULT_CIRCLE_COLOR);
		canvas.drawArc(new RectF(mProgressMargin, mProgressMargin, mScreenWidth - mProgressMargin, mScreenHeight - mProgressMargin), 270, sweepAngle2, false, mCirclePaint);

        float radius = (mScreenWidth - 2 * mProgressMargin) / 2;
        float angle = (270 + sweepAngle2) > 360 ? (270 + sweepAngle2 - 360) : (270 + sweepAngle2);
        canvas.drawCircle((float)(radius + radius * Math.cos(angle * Math.PI / 180) + mProgressMargin),   //起始点角度在圆上对应的横坐标
                (float)(radius + radius * Math.sin(angle * Math.PI / 180) + mProgressMargin),
                6, flowPaint);

        canvas.restore();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		// Force our ancestor class to save its state
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.progress = (int) c1;
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		c1 = ss.progress;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		// 关闭硬件加速，防止异常unsupported operation exception
		this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	/**
	 * @category 开始波动
	 */
	public void startWave() {
		if (!mStarted) {
			this.c1 = 0L;
			mStarted = true;
			this.mHandler.sendEmptyMessage(0);
		}
	}

	/**
	 * @category 停止波动
	 */
	public void stopWave() {
		if (mStarted) {
			this.c1 = 0L;
			mStarted = false;
			this.mHandler.removeMessages(0);

			if(bitmapRadar != null && !bitmapRadar.isRecycled()){
				// 回收并且置为null
				bitmapRadar.recycle();
				bitmapRadar = null;
			}
		}
	}

	/**
	 * @category 保存状态
	 */
	static class SavedState extends BaseSavedState {
		int progress;

		/**
		 * Constructor called from {@link ProgressBar#onSaveInstanceState()}
		 */
		SavedState(Parcelable superState) {
			super(superState);
		}

		/**
		 * Constructor called from {@link #CREATOR}
		 */
		private SavedState(Parcel in) {
			super(in);
			progress = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(progress);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}
