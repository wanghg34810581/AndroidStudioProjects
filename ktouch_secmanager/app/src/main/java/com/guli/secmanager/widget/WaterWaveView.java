/**
 * 
 */
package com.guli.secmanager.widget;

import android.content.Context;
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
public class WaterWaveView extends View {

	private Context mContext;

	private int mScreenWidth;
	private int mScreenHeight;

	private Paint mRingPaint;
	private Paint mCirclePaint;
	private Paint mWavePaint;
    private Paint mWaterPaint;
	private Paint flowPaint;
    private Paint flowReversePaint;
    private Paint leftPaint;
	private Paint leftReversePaint;

	private int mRingSTROKEWidth = 15;
	private int mCircleSTROKEWidth = 1;
    private int mCircleMargin = 10;
    private int mWaterRadius;

	private int mCircleColor = getResources().getColor(R.color.green);
	private int mRingColor = getResources().getColor(R.color.white);
	private int mWaveColor = getResources().getColor(R.color.green);

	private Handler mHandler;
	private long c1 = 0L;
    private long c2 = 0L;
    private long Speed1 = 1L;
    private long Speed2 = 2L;
	private boolean mStarted = false;
	private final float f = 0.033F;
	private int mAlpha = 50;// 透明度
	private float mAmplitude = 10.0F; // 振幅
	private float mWaterLevel = 0.5F;// 水高(0~1)
	private Path mPath;

	private String scan_ongoing_string = "0";
    private String unit_string = "MB";
	private String scan_state_string = "总共";
    private static final int DEFAULT_CIRCLE_COLOR = 0Xff6bc545; // 进度条默认颜色
    private static final int DEFAULT_CIRCLE_BACKGROUND_COLOR = 0Xffc0c0c0; // 进度条背景默认颜色

	/**
	 * @param context
	 */
	public WaterWaveView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		init(mContext);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public WaterWaveView(Context context, AttributeSet attrs) {
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
	public WaterWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		mContext = context;
		init(mContext);
	}

    public void setScanOngoing(String scan_ongoing) {
        scan_ongoing_string = scan_ongoing;
        //Log.i("wanghg", "mWaterLevel : " + scan_ongoing);
    }

    public void setUnit(String unit) {
        unit_string = unit;
        //Log.i("wanghg", "unit : " + unit);
    }

    public void setProgress(float progress) {
        progress += 0.01F;
        if(progress == 0F) {
            progress = 0.01F;
        }
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
        flowReversePaint.setTextSize(sp2px(context, 40));

		leftPaint = new Paint();
		leftPaint.setColor(getResources().getColor(R.color.green));
		leftPaint.setStyle(Paint.Style.FILL);
		leftPaint.setAntiAlias(true);
		leftPaint.setTextSize(sp2px(context, 12));

        leftReversePaint = new Paint();
        leftReversePaint.setColor(getResources().getColor(R.color.white));
        leftReversePaint.setStyle(Paint.Style.FILL);
        leftReversePaint.setAntiAlias(true);
        leftReversePaint.setTextSize(sp2px(context, 12));

        mWaterPaint = new Paint();
        mWaterPaint.setStyle(Paint.Style.FILL);
        mWaterPaint.setColor(mWaveColor);
        mWaterPaint.setAlpha(mAlpha);
        mWaterPaint.setAntiAlias(true);

		mWavePaint = new Paint();
		mWavePaint.setStrokeWidth(1.0F);
		mWavePaint.setColor(mWaveColor);
		mWavePaint.setAlpha(mAlpha);
        mWavePaint.setAntiAlias(true);
		mPath = new Path();

		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				if (msg.what == 0) {
					invalidate();
					if (mStarted) {
						// 不断发消息给自己，使自己不断被重绘
						mHandler.sendEmptyMessageDelayed(0, 10L);
					}
				}
			}
		};
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
        mWaterRadius = mScreenWidth / 2 - mRingSTROKEWidth - mCircleSTROKEWidth - mCircleMargin;
		//Log.i("wanghg", "mScreenWidth : " + mScreenWidth + "   mScreenHeight : " + mScreenHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		// 得到控件的宽高
		//int width = getWidth();
		//int height = getHeight();
		//Log.i("wanghg", "onDraw  mScreenWidth : " + width + "   mScreenHeight : " + height);
		//setBackgroundColor(mContext.getResources().getColor(R.color.green));

        //计算当前油量线和水平中线的距离
        float centerOffset = Math.abs(mWaterRadius * 2 * mWaterLevel - mWaterRadius);
        //Log.i("wanghg", "centerOffset : " + centerOffset);
        //计算油量线和与水平中线的角度
        float horiAngle = (float)(Math.asin(centerOffset / mWaterRadius) * 180 / Math.PI);
        //Log.i("wanghg", "horiAngle : " + horiAngle);
        //扇形的起始角度和扫过角度
        float startAngle, sweepAngle;
        if (mWaterLevel > 0.5F) {
            startAngle = 360F - horiAngle;
            sweepAngle = 180F + 2 * horiAngle;
        } else {
            startAngle = horiAngle;
            sweepAngle = 180F - 2 * horiAngle;
        }
        //Log.i("wanghg", "startAngle : " + startAngle);
        //Log.i("wanghg", "sweepAngle : " + sweepAngle);

		if (this.c1 >= 8388607L) {
			this.c1 = 0L;
		}
        if (this.c2 >= 8388607L) {
            this.c2 = 0L;
        }
        // 每次onDraw时c都会自增
        c1 = (Speed1 + c1);
        c2 = (Speed2 + c2);

        //Log.i("wanghg", "c1 : " + c1);
        float f1 = mWaterRadius * 2 * (1.0F - mWaterLevel);
        //Log.i("wanghg", "f1 : " + f1);
        //当前油量线的长度
        float waveWidth = (float)Math.sqrt(mWaterRadius * mWaterRadius - centerOffset * centerOffset);
        //Log.i("wanghg", "waveWidth : " + waveWidth);
        //与圆半径的偏移量
        float offsetWidth = mWaterRadius - waveWidth;
        //Log.i("wanghg", "offsetWidth : " + offsetWidth);

        int top = (int) (f1 + mAmplitude);
        //Log.i("wanghg", "top : " + top);
        mPath.reset();
        //起始振动X坐标，结束振动X坐标
        int startX, endX;
        if (mWaterLevel > 0.50F) {
            startX = (int) 0;//(offsetWidth + mScreenHeight / 2 - mWaterRadius);
            endX = (int) mScreenHeight;//(mScreenHeight / 2 + mWaterRadius - offsetWidth);
        } else {
            startX = (int) 0;//(offsetWidth - mAmplitude + mScreenHeight / 2 - mWaterRadius);
            endX = (int) mScreenHeight;//(mScreenHeight / 2 + mWaterRadius - offsetWidth + mAmplitude);
        }
        //Log.i("wanghg", "startX : " + startX);
        //Log.i("wanghg", "endX : " + endX);

        canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(0, 0, mScreenWidth, top);
        float scan_ongoing = flowPaint.measureText(scan_ongoing_string);
        //Log.i("wanghg", "scan_ongoing : " + scan_ongoing);
        canvas.drawText(scan_ongoing_string, mScreenWidth / 2 - scan_ongoing / 2,
                mScreenHeight / 2 + scan_ongoing / (2 *scan_ongoing_string.length()), flowPaint);
        float unit = leftPaint.measureText(unit_string);
        //Log.i("wanghg", "unit : " + unit);
        canvas.drawText(unit_string, mScreenWidth / 2 + scan_ongoing / 2 + 5,
                mScreenHeight / 2 + scan_ongoing / (2 *scan_ongoing_string.length()), leftPaint);
        float scan_state = leftPaint.measureText(scan_state_string);
        //Log.i("wanghg", "scan_state : " + scan_state);
        canvas.drawText(scan_state_string, mScreenWidth / 2 - scan_state / 2,
                mScreenHeight * 3 / 4, leftPaint);
        canvas.restore();

        canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(0, top, mScreenWidth, mScreenHeight);
        canvas.drawCircle(mScreenWidth / 2, mScreenHeight / 2, mWaterRadius, mWaterPaint);
        canvas.drawCircle(mScreenWidth / 2, mScreenHeight / 2, mWaterRadius, mWaterPaint);
        canvas.drawText(scan_ongoing_string, mScreenWidth / 2 - scan_ongoing / 2,
                mScreenHeight / 2 + scan_ongoing / (2 *scan_ongoing_string.length()), flowReversePaint);
        canvas.drawText(unit_string, mScreenWidth / 2 + scan_ongoing / 2 + 5,
                mScreenHeight / 2 + scan_ongoing / (2 *scan_ongoing_string.length()), leftReversePaint);
        canvas.drawText(scan_state_string, mScreenWidth / 2 - scan_state / 2,
                mScreenHeight * 3 / 4, leftReversePaint);
        canvas.restore();

        canvas.drawCircle(mScreenWidth / 2, mScreenHeight / 2,
                mScreenWidth / 2 - mCircleMargin - mRingSTROKEWidth / 2, mRingPaint);

        //进度条
        //canvas.drawCircle(mScreenWidth / 2, mScreenHeight / 2, mScreenWidth / 2, mCirclePaint);
        float sweepAngle2 = mWaterLevel / 1.0f * 360f;
        int margin = 6;
        mCirclePaint.setColor(DEFAULT_CIRCLE_BACKGROUND_COLOR);
        canvas.drawArc(new RectF(margin, margin, mScreenWidth - margin, mScreenHeight - margin), 270, 360, false, mCirclePaint);
        mCirclePaint.setColor(DEFAULT_CIRCLE_COLOR);
        canvas.drawArc(new RectF(margin, margin, mScreenWidth - margin, mScreenHeight - margin), 270, sweepAngle2, false, mCirclePaint);

        float radius = (mScreenWidth - 2 * margin) / 2;
        float angle = (270 + sweepAngle2) > 360 ? (270 + sweepAngle2 - 360) : (270 + sweepAngle2);
        canvas.drawCircle((float)(radius + radius * Math.cos(angle * Math.PI / 180) + margin),   //起始点角度在圆上对应的横坐标
                (float)(radius + radius * Math.sin(angle * Math.PI / 180) + margin),
                6, flowPaint);

        // 波浪效果
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        mPath.addCircle(mScreenWidth / 2, mScreenHeight / 2, mWaterRadius, Path.Direction.CW);
        canvas.clipPath(mPath);
        while (startX < endX) {
            int startY = (int)
                    (f1 - mAmplitude * Math.sin(Math.PI * (2.0F * (startX + this.c1 * mWaterRadius * 2 * this.f)) / (mWaterRadius * 2)));
            //Log.i("wanghg", "startX : " + startX);
            //Log.i("wanghg", "startY : " + startY);
            canvas.drawLine(startX, startY, startX, top, mWavePaint);

            int startY2 = (int)
                    (f1 - mAmplitude * Math.sin(Math.PI * (2.0F * (startX + this.c2 * mWaterRadius * 2 * this.f)) / (mWaterRadius * 2)));
            //Log.i("wanghg", "startY2 : " + startY2);
            canvas.drawLine(startX, startY2, startX, top, mWavePaint);

            startX++;
        }
        canvas.restore();
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
		}
	}
}
