package com.guli.secmanager.flowmonitor;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.CompoundButton;

import com.guli.secmanager.R;
/**
 *{@hide}
 */
public class TYAplaSwitch extends CompoundButton implements AnimatorUpdateListener ,AnimatorListener{
	private static final int TOUCH_MODE_IDLE = 0;
	private static final int TOUCH_MODE_DOWN = 1;
	private static final int TOUCH_MODE_DRAGGING = 2;

	private Drawable mThumbDrawable;
	private Drawable mThumbOnDrawable;
	private Drawable mThumbOffDrawable;
	private Drawable mTrackOnDrawable;
	private Drawable mTrackOffDrawable;

	private int mThumbWidth;
	private float mThumbPosition;

	private int mSwitchWidth;
	private int mSwitchHeight;
	private int mSwitchLeft;
	private int mSwitchTop;
	private int mSwitchBottom;
	private int mSwitchRight;

	private int mTouchMode;
	private int mTouchSlop;
	private float mTouchX;
	private float mTouchY;
	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	private int mMinFlingVelocity;
	private int mOnBackgroudAlpha = 0;
	private boolean type = false;
	private boolean isRunAnimate = false;
	private boolean isCancelAnimate = false;

	public int getOnBackgroudAlpha() {
		return mOnBackgroudAlpha;
	}

	public void setOnBackgroudAlpha(int onBackgroudAlpha) {
		mOnBackgroudAlpha = onBackgroudAlpha;
	}

	private ObjectAnimator mAnimatorOnToOff;
	private ObjectAnimator mAnimatorOffToOn;

	private final Rect mTempRect = new Rect();

	public TYAplaSwitch(Context context) {
		super(context);
		init(context);
	}

	public TYAplaSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Log.i("yujsh log", "TYAplaSwitch");
		// TypedArray ta = context.obtainStyledAttributes(attrs,
		// R.styleable.ty_view);
		// type = ta.getBoolean(R.attr.type, false);
		// ta.recycle();
		init(context);
	}

	public TYAplaSwitch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TypedArray ta = context.obtainStyledAttributes(attrs,
		// R.styleable.ty_view);
		// type = ta.getBoolean(R.attr.type, false);
		// ta.recycle();
		init(context);
	}

	private void init(Context context) {
		ViewConfiguration config = ViewConfiguration.get(context);
		mThumbOnDrawable = context.getResources().getDrawable(
				R.drawable.toggle_btn_thumb_on_selector);
		mThumbOffDrawable = context.getResources().getDrawable(
				R.drawable.toggle_btn_thumb_off_selector);
		mThumbDrawable = mThumbOffDrawable;
		mTrackOnDrawable = context.getResources().getDrawable(R.drawable.toggle_btn_bg_on_selector);
		mTrackOffDrawable = context.getResources().getDrawable(R.drawable.toggle_btn_bg_off_selector);
		mTouchSlop = config.getScaledTouchSlop();
		mMinFlingVelocity = config.getScaledMinimumFlingVelocity();

		// Refresh display with current params
		refreshDrawableState();
		setChecked(isChecked());
		setClickable(true);
		initAnimator();

		if(isChecked()){
			mOnBackgroudAlpha = 255;
		}else{
			mOnBackgroudAlpha = 0;
		}
//		setChecked(isChecked());
		drawableStateChanged();
	}

	private void initAnimator() {
		mAnimatorOnToOff = ObjectAnimator.ofInt(this, "onBackgroudAlpha", 255, 0);
		mAnimatorOnToOff.setDuration(300);
		// mAnimatorOnToOff.setInterpolator(new LinearInterpolator());
		mAnimatorOffToOn = ObjectAnimator.ofInt(this, "onBackgroudAlpha", 0, 255);
		mAnimatorOffToOn.setDuration(300);
		// mAnimatorOffToOn.setInterpolator(new LinearInterpolator());
		mAnimatorOnToOff.addUpdateListener(this);
		mAnimatorOnToOff.addListener(this);
		mAnimatorOffToOn.addUpdateListener(this);
		mAnimatorOffToOn.addListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		mThumbPosition = isChecked() ? getThumbScrollRange() : 0;

		int switchRight = getWidth() - getPaddingRight();
		int switchLeft = switchRight - mSwitchWidth;
		int switchTop = 0;
		int switchBottom = 0;
		switch (getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
		default:
		case Gravity.TOP:
			switchTop = getPaddingTop();
			switchBottom = switchTop + mSwitchHeight;
			break;

		case Gravity.CENTER_VERTICAL:
			switchTop = (getPaddingTop() + getHeight() - getPaddingBottom()) / 2 - mSwitchHeight
					/ 2;
			switchBottom = switchTop + mSwitchHeight;
			break;

		case Gravity.BOTTOM:
			switchBottom = getHeight() - getPaddingBottom();
			switchTop = switchBottom - mSwitchHeight;
			break;
		}

		mSwitchLeft = switchLeft;
		mSwitchTop = switchTop;
		mSwitchBottom = switchBottom;
		mSwitchRight = switchRight;
		mSwitchHeight = switchBottom - switchTop;

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);

		int switchLeft = mSwitchLeft;
		int switchTop = mSwitchTop;
		int switchRight = mSwitchRight;
		int switchBottom = mSwitchBottom;
		final int thumbPos = (int) (mThumbPosition + 0.5f);

		mTrackOffDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
		mTrackOffDrawable.draw(canvas);

		mTrackOnDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
		mTrackOnDrawable.setAlpha(mOnBackgroudAlpha);
		mTrackOnDrawable.draw(canvas);

		// draw backgroud
		// Bitmap tempBitmap = Bitmap.createBitmap(mSwitchWidth, mSwitchHeight,
		// Bitmap.Config.ARGB_8888);
		// Canvas tempCanvas = new Canvas(tempBitmap);
		//
		// Paint paint = new Paint();
		// paint.setColor(Color.WHITE);
		// paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		// mTrackOffDrawable.getPadding(mTempRect);
		int switchInnerLeft = switchLeft + mTempRect.left;
		//
		// tempCanvas.drawBitmap(mSwitchBtm,
		// -mSwitchBtm.getWidth() / 2 + thumbPos +
		// mThumbDrawable.getIntrinsicWidth() / 2, 0,
		// null);
		// tempCanvas.drawBitmap(mSwitchMaskBtm, null, new RectF(switchLeft,
		// switchTop, switchRight,
		// switchBottom), paint);
		//
		// canvas.drawBitmap(tempBitmap, 0, 0, null);
		//
		// tempBitmap.recycle();
		// tempBitmap = null;

		// draw toggle button
		mThumbDrawable.getPadding(mTempRect);
		int thumbLeft = switchInnerLeft - mTempRect.left + thumbPos;
		int thumbRight = switchInnerLeft + thumbPos + mThumbWidth + mTempRect.right;
		mThumbDrawable.setBounds(thumbLeft, switchTop, thumbRight, switchBottom);
		mThumbDrawable.draw(canvas);

	}

	private int getThumbScrollRange() {
		if (mTrackOffDrawable == null) {
			return 0;
		}
		mTrackOffDrawable.getPadding(mTempRect);
		return mSwitchWidth - mThumbWidth - mTempRect.left - mTempRect.right;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		// if (mOnLayout == null) {
		// mOnLayout = makeLayout(mTextOn);
		// }
		// if (mOffLayout == null) {
		// mOffLayout = makeLayout(mTextOff);
		// }

		mTrackOffDrawable.getPadding(mTempRect);
		// final int maxTextWidth = Math.max(mOnLayout.getWidth(),
		// mOffLayout.getWidth());
		final int switchWidth = mTrackOffDrawable.getIntrinsicWidth();
		final int switchHeight = mTrackOffDrawable.getIntrinsicHeight();

		mThumbWidth = mThumbDrawable.getIntrinsicHeight();// maxTextWidth +
															// mThumbTextPadding
															// * 2;

		switch (widthMode) {
		case MeasureSpec.AT_MOST:
			widthSize = Math.min(widthSize, switchWidth);

			break;

		case MeasureSpec.UNSPECIFIED:
			widthSize = switchWidth;
			break;

		case MeasureSpec.EXACTLY:
			// Just use what we were given
			break;
		}

		switch (heightMode) {
		case MeasureSpec.AT_MOST:
			heightSize = Math.min(heightSize, switchHeight);
			break;

		case MeasureSpec.UNSPECIFIED:
			heightSize = switchHeight;
			break;

		case MeasureSpec.EXACTLY:
			// Just use what we were given
			break;
		}

		mSwitchWidth = switchWidth;
		mSwitchHeight = switchHeight;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int measuredHeight = getMeasuredHeight();
		if (measuredHeight < switchHeight) {
			setMeasuredDimension(switchWidth, switchHeight);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		mVelocityTracker.addMovement(ev);
		final int action = ev.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			isRunAnimate = true;

			final float x = ev.getX();
			final float y = ev.getY();
			if (isEnabled() && hitThumb(x, y)) {
				mTouchMode = TOUCH_MODE_DOWN;
				mTouchX = x;
				mTouchY = y;
			}
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			switch (mTouchMode) {
			case TOUCH_MODE_IDLE:
				// Didn't target the thumb, treat normally.
				break;

			case TOUCH_MODE_DOWN: {
				final float x = ev.getX();
				final float y = ev.getY();
				if (Math.abs(x - mTouchX) > mTouchSlop || Math.abs(y - mTouchY) > mTouchSlop) {
					mTouchMode = TOUCH_MODE_DRAGGING;
					getParent().requestDisallowInterceptTouchEvent(true);
					mTouchX = x;
					mTouchY = y;
					return true;
				}
				break;
			}

			case TOUCH_MODE_DRAGGING: {
				final float x = ev.getX();
				final float dx = x - mTouchX;
				float newPos = Math.max(0, Math.min(mThumbPosition + dx, getThumbScrollRange()));
				if (newPos != mThumbPosition) {
					mThumbPosition = newPos;
					mTouchX = x;
					invalidate();
				}
				return true;
			}
			}
			break;
		}

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: {
			if (mTouchMode == TOUCH_MODE_DRAGGING) {
				stopDrag(ev);
				return true;
			}
			mTouchMode = TOUCH_MODE_IDLE;
			mVelocityTracker.clear();
			break;
		}
		}
		boolean result = super.onTouchEvent(ev);
		return result;
	}

	private void cancelSuperTouch(MotionEvent ev) {
		MotionEvent cancel = MotionEvent.obtain(ev);
		cancel.setAction(MotionEvent.ACTION_CANCEL);
		super.onTouchEvent(cancel);
		cancel.recycle();
	}

	/**
	 * @return true if (x, y) is within the target area of the switch thumb
	 */
	private boolean hitThumb(float x, float y) {
		mThumbDrawable.getPadding(mTempRect);
		final int thumbTop = mSwitchTop - mTouchSlop;
		final int thumbLeft = mSwitchLeft + (int) (mThumbPosition + 0.5f) - mTouchSlop;
		final int thumbRight = thumbLeft + mThumbWidth + mTempRect.left + mTempRect.right
				+ mTouchSlop;
		final int thumbBottom = mSwitchBottom + mTouchSlop;
		return x > thumbLeft && x < thumbRight && y > thumbTop && y < thumbBottom;
	}

	/**
	 * Called from onTouchEvent to end a drag operation.
	 *
	 * @param ev
	 *            Event that triggered the end of drag mode - ACTION_UP or
	 *            ACTION_CANCEL
	 */
	private void stopDrag(MotionEvent ev) {
		mTouchMode = TOUCH_MODE_IDLE;
		// Up and not canceled, also checks the switch has not been disabled
		// during the drag
		boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP && isEnabled();

		cancelSuperTouch(ev);

		if (commitChange) {
			boolean newState;
			mVelocityTracker.computeCurrentVelocity(1000);
			float xvel = mVelocityTracker.getXVelocity();
			if (Math.abs(xvel) > mMinFlingVelocity) {
				newState = xvel > 0;
			} else {
				newState = getTargetCheckedState();
			}
			animateThumbToCheckedState(newState);
		} else {
			animateThumbToCheckedState(isChecked());
		}
	}

	private void animateThumbToCheckedState(boolean newCheckedState) {
		float targetPos = newCheckedState ? 0 : getThumbScrollRange();
		mThumbPosition = targetPos;
		setChecked(newCheckedState);
	}

	private boolean getTargetCheckedState() {
		return mThumbPosition >= getThumbScrollRange() / 2;
	}

	@Override
	public void setChecked(boolean checked) {

		mThumbPosition = checked ? getThumbScrollRange() : 0;

		if (checked) {
			mThumbDrawable = mThumbOnDrawable;
		} else {
			mThumbDrawable = mThumbOffDrawable;
		}

		if (isRunAnimate) {
			if (mAnimatorOffToOn != null && mAnimatorOnToOff != null && isChecked() != checked) {
				mAnimatorOffToOn.cancel();
				mAnimatorOnToOff.cancel();
				if (checked) {
					mThumbDrawable = mThumbOnDrawable;
					mAnimatorOffToOn.start();
				} else {
					mThumbDrawable = mThumbOffDrawable;
					mAnimatorOnToOff.start();
				}
			}
		}else{
			mOnBackgroudAlpha = checked ? 255 : 0;
		}

		super.setChecked(checked);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		invalidate();
	}

	protected void drawableStateChanged() {
		super.drawableStateChanged();
		int[] mDrawableState = getDrawableState();
		if (mThumbDrawable != null && mTrackOnDrawable != null && mTrackOnDrawable != null) {
			mThumbDrawable.setState(mDrawableState);
			mTrackOnDrawable.setState(mDrawableState);
			mTrackOffDrawable.setState(mDrawableState);
			invalidate();
		}
	}

	@Override
	public void onAnimationStart(Animator animation) {
		isCancelAnimate = false;
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		if(!isCancelAnimate){
			isRunAnimate = false;
		}
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		isCancelAnimate = true;

	}

	@Override
	public void onAnimationRepeat(Animator animation) {

	}

}
