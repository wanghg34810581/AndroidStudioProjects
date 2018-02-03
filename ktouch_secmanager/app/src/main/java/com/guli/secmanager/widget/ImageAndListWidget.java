/**
 * TIANYURD: yuanht add for tycontact
 */
package com.guli.secmanager.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.util.Log;
import android.os.Handler;

public class ImageAndListWidget extends RelativeLayout implements ListViewForFinish.OnItemTouchListener {
    private Context mContext;

    interface WidgetStatus {
        public static final int STATUS_SLIDING = 0;
        public static final int STATUS_OPENED = 1;
        public static final int STATUS_CLOSED = 2;
    }

    interface SlidingDirection {
        public static final int SLIDING_NONE = 0;
        public static final int SLIDING_DOWN = 1;
        public static final int SLIDING_UP = 2;
        public static final int SLIDING_RESTORE_DOWN = 3;
        public static final int SLIDING_RESTORE_UP = 4;
    }

    private int mCurrentStatus = WidgetStatus.STATUS_OPENED;
    private int mSlidingDirection = SlidingDirection.SLIDING_NONE;

    private RelativeLayout mRoundView;
    private RelativeLayout mTextView;
    private RelativeLayout mArrowView;

    private AbsListView.LayoutParams mThisViewLayoutParams;
    private RelativeLayout.LayoutParams mRoundViewLayoutParams;
    private RelativeLayout.LayoutParams mTextViewLayoutParams;
    private RelativeLayout.LayoutParams mArrowViewLayoutParams;

    private int mThisViewMinHeight = 240;
    private int mThisViewMaxHeight;
    private int mRoundViewMinHeight = 0;
    private int mRoundViewMaxHeight;
    private int mTextViewMinMargin;
    private int mTextViewMaxMargin;
    private int mArrowViewTopMargin;

    private int mYDown;
    private int mTouchSlop;
    private boolean mLoadOnce = false;
    private boolean mMeasureLoadOnce = false;

    private Handler mHandler = new Handler();
    private FlashingSliding mFlashingSliding = new FlashingSliding();
    private RestoreSliding mRestoreSliding = new RestoreSliding();
    private ArrowSliding mArrowSliding = new ArrowSliding();

    public ImageAndListWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mContext = context;
    }

    public void setContentView(View roundView,
                               View textView, View arrowView) {
        mRoundView = (RelativeLayout) roundView;
        mTextView = (RelativeLayout) textView;
        mArrowView = (RelativeLayout) arrowView;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //Log.i("wanghg", "onLayout");
        super.onLayout(changed, l, t, r, b);
        if (changed && !mLoadOnce) {
            //Log.i("wanghg", "onLayout load once");
            mRoundViewLayoutParams = (RelativeLayout.LayoutParams) mRoundView.getLayoutParams();

            mTextViewLayoutParams = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
            mTextViewMaxMargin = mTextViewLayoutParams.topMargin;
            //Log.i("wanghg", "up mTextViewLayoutParams.topMargin : " + mTextViewLayoutParams.topMargin);

            mArrowViewLayoutParams = (RelativeLayout.LayoutParams) mArrowView.getChildAt(0).getLayoutParams();
            mArrowViewTopMargin = mArrowViewLayoutParams.topMargin;
            //Log.i("wanghg", "up mArrowViewLayoutParams.topMargin : " + mArrowViewLayoutParams.topMargin);

            mThisViewLayoutParams = (AbsListView.LayoutParams) this.getLayoutParams();
            //Log.i("wanghg", "up mThisViewLayoutParams.height : " + mThisViewLayoutParams);

            mLoadOnce = true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)  {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //Log.i("wanghg", "this.width : " + this.getWidth());
        //Log.i("wanghg", "this.height : " + this.getHeight());
        //Log.i("wanghg", "mRoundView.width : " + mRoundView.getWidth());
        //Log.i("wanghg", "mRoundView.height : " + mRoundView.getHeight());

        if(mLoadOnce && !mMeasureLoadOnce) {
            mRoundViewLayoutParams.width = mRoundView.getWidth();
            mRoundViewMaxHeight = mRoundViewLayoutParams.height = mRoundView.getHeight();
            //Log.i("wanghg", "mRoundViewLayoutParams.height : " + mRoundViewLayoutParams.height);

            mThisViewLayoutParams.width = this.getWidth();
            mThisViewMaxHeight = mThisViewLayoutParams.height = this.getHeight();
            //Log.i("wanghg", "mThisViewLayoutParams.height : " + mThisViewLayoutParams.height);

            mTextViewMinMargin = (mThisViewMinHeight - mTextView.getHeight()) / 2 - mRoundViewLayoutParams.topMargin;
            //Log.i("wanghg", "mTextView.getHeight() : " + mTextView.getHeight());
            //Log.i("wanghg", "mRoundViewLayoutParams.topMargin : " + mRoundViewLayoutParams.topMargin);
            //Log.i("wanghg", "mTextViewMinMargin : " + mTextViewMinMargin);

            mMeasureLoadOnce = true;

            mArrowSliding.start(false, true);
            mSlidingDirection = SlidingDirection.SLIDING_UP;
            mFlashingSliding.start(1500);
        }
    }

    @Override
    public boolean onItemTouchEvent(MotionEvent event) {
        if (mCurrentStatus == WidgetStatus.STATUS_SLIDING) {
            return false;
        }

        if (this.getTop() < 0) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                mHandler.removeCallbacksAndMessages(null);
            }
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mYDown = (int) event.getRawY();
                mRestoreSliding.start(mYDown);

                if(mCurrentStatus == WidgetStatus.STATUS_OPENED) {
                    mArrowSliding.start(false, false);
                }
               // Log.i("wanghg", "onTouch down Y = " + mYDown);
                return false;
            case MotionEvent.ACTION_MOVE:
                int yMove = (int) event.getRawY();
                int distance = (int) (yMove - mYDown);
                mRestoreSliding.setMoveY(yMove);

                //Log.i("wanghg", "onTouch move Y = " + yMove + "  mCurrentStatus = " + mCurrentStatus + "  distance : " + distance);

                if (mCurrentStatus != WidgetStatus.STATUS_SLIDING) {
                    if (distance > 0  && mCurrentStatus == WidgetStatus.STATUS_CLOSED) {
                        mSlidingDirection = SlidingDirection.SLIDING_DOWN;
                    }
                    else if (distance < 0 && mCurrentStatus == WidgetStatus.STATUS_OPENED) {
                        mSlidingDirection = SlidingDirection.SLIDING_UP;
                    }
                    else {
                        //Log.i("wanghg", "return 222 ");
                        return false;
                    }

                    mFlashingSliding.move(Math.abs(distance));
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                //Log.i("wanghg", "onTouch cancel");
            case MotionEvent.ACTION_UP:
                //Log.i("wanghg", "onTouch up");
                int yUp = (int) event.getRawY();
                mRestoreSliding.setUpY(yUp);
                mFlashingSliding.start(0);
                break;
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mYDown = (int) event.getRawY();
        }
        return true;
    }

    final private class FlashingSliding implements Runnable {
        int moveDistance;
        private static final int SCROLL_SPEED = 40;
        private static final int SCROLL_INTERVAL = 20;

        public void start(int time) {
            if (mSlidingDirection == SlidingDirection.SLIDING_DOWN
                    || mSlidingDirection == SlidingDirection.SLIDING_RESTORE_UP) {
                moveDistance = mRoundViewLayoutParams.width - mRoundViewMinHeight;
            } else if (mSlidingDirection == SlidingDirection.SLIDING_RESTORE_DOWN
                    || mSlidingDirection == SlidingDirection.SLIDING_UP) {
                moveDistance = mRoundViewMaxHeight - mRoundViewLayoutParams.width;
            }
            else {
                //Log.i("wanghg", "run error : mSlidingDirection = " + mSlidingDirection);
                return;
            }

            //Log.i("wanghg", "mSlidingDirection : " + mSlidingDirection);
            //Log.i("wanghg", "mCurrentStatus : " + mCurrentStatus);
            mCurrentStatus = WidgetStatus.STATUS_SLIDING;
            //mHandler.removeCallbacks(mRestoreSliding);
            mHandler.postDelayed(this, time);
        }

        private void move(int moveDistance) {
            if (mSlidingDirection == SlidingDirection.SLIDING_DOWN
                    || mSlidingDirection == SlidingDirection.SLIDING_RESTORE_UP) {
                mRoundViewLayoutParams.width = mRoundViewMinHeight + moveDistance;
                if (mRoundViewLayoutParams.width > mRoundViewMaxHeight){
                    mRoundViewLayoutParams.width = mRoundViewMaxHeight;
                }
                mRoundViewLayoutParams.height = mRoundViewLayoutParams.width;
                //Log.i("wanghg", "down mRoundViewLayoutParams.width : " + mRoundViewLayoutParams.width);
                mRoundView.setLayoutParams(mRoundViewLayoutParams);
                float scale = (float) mRoundViewLayoutParams.width / (float) mRoundViewMaxHeight;
                //mRoundView.setScaleX(scale);
                //mRoundView.setScaleY(scale);
                mRoundView.setAlpha(scale);

                mThisViewLayoutParams.height = mThisViewMinHeight + (int) (moveDistance * ((float) (mThisViewMaxHeight - mThisViewMinHeight) / (float) mRoundViewMaxHeight));
                if(mRoundViewLayoutParams.width == mRoundViewMaxHeight) {//mean finished
                    mThisViewLayoutParams.height = mThisViewMaxHeight;
                }
                ImageAndListWidget.this.setLayoutParams(mThisViewLayoutParams);

                mTextViewLayoutParams.topMargin = mTextViewMinMargin + (int) (moveDistance * ((float) (mTextViewMaxMargin - mTextViewMinMargin) / (float) mRoundViewMaxHeight));
                if(mRoundViewLayoutParams.width == mRoundViewMaxHeight) {//mean finished
                    mTextViewLayoutParams.topMargin = mTextViewMaxMargin;
                }
                mTextView.setLayoutParams(mTextViewLayoutParams);
            } else if (mSlidingDirection == SlidingDirection.SLIDING_RESTORE_DOWN
                    || mSlidingDirection == SlidingDirection.SLIDING_UP) {
                mRoundViewLayoutParams.width = mRoundViewMaxHeight - moveDistance;
                if (mRoundViewLayoutParams.width < mRoundViewMinHeight){
                    mRoundViewLayoutParams.width = mRoundViewMinHeight;
                }
                mRoundViewLayoutParams.height = mRoundViewLayoutParams.width;
                //Log.i("wanghg", "up mRoundViewLayoutParams.width : " + mRoundViewLayoutParams.width);
                mRoundView.setLayoutParams(mRoundViewLayoutParams);
                float scale = (float) mRoundViewLayoutParams.width / (float) mRoundViewMaxHeight;
                //mRoundView.setScaleX(scale);
                //mRoundView.setScaleY(scale);
                mRoundView.setAlpha(scale);

                mThisViewLayoutParams.height = mThisViewMaxHeight - (int) (moveDistance * ((float) (mThisViewMaxHeight - mThisViewMinHeight) / (float) mRoundViewMaxHeight));
                if( mRoundViewLayoutParams.width == mRoundViewMinHeight) {//mean finished
                    mThisViewLayoutParams.height = mThisViewMinHeight;
                }
                ImageAndListWidget.this.setLayoutParams(mThisViewLayoutParams);

                mTextViewLayoutParams.topMargin = mTextViewMaxMargin - (int) (moveDistance * ((float) (mTextViewMaxMargin - mTextViewMinMargin) / (float) mRoundViewMaxHeight));
                if( mRoundViewLayoutParams.width == mRoundViewMinHeight) {//mean finished
                    mTextViewLayoutParams.topMargin = mTextViewMinMargin;
                }
                //Log.i("wanghg", "up mTextViewLayoutParams.topMargin : " + mTextViewLayoutParams.topMargin);
                mTextView.setLayoutParams(mTextViewLayoutParams);
            }
        }

        public void run() {
            boolean isFinish = false;

            moveDistance += SCROLL_SPEED;
            if (moveDistance >= mRoundViewMaxHeight) {
                isFinish = true;
            }

            move(moveDistance);

            if(isFinish) {
                if (mSlidingDirection == SlidingDirection.SLIDING_DOWN
                        || mSlidingDirection == SlidingDirection.SLIDING_RESTORE_UP) {
                    mCurrentStatus = WidgetStatus.STATUS_OPENED;
                    mArrowSliding.start(true, false);
                } else if (mSlidingDirection == SlidingDirection.SLIDING_RESTORE_DOWN
                        || mSlidingDirection == SlidingDirection.SLIDING_UP) {
                    mCurrentStatus = WidgetStatus.STATUS_CLOSED;
                }
                mSlidingDirection = SlidingDirection.SLIDING_NONE;
            }
            else {
                mHandler.postDelayed(this, SCROLL_INTERVAL);
            }
        }
    }

    final private class RestoreSliding implements Runnable {
        private int mMoveY;
        private int mSaveY;
        private int mUpY;
        private boolean isFinish = false;
        private static final int SAVE_INTERVAL = 200;

        public void start(int y) {
            mSaveY = mMoveY = y;
            isFinish = false;
            mHandler.post(this);
        }

        public void setMoveY(int y) {
            mMoveY = y;
        }

        public void setUpY(int y) {
            mUpY = y;

            //Log.i("wanghg", "mSaveY = " + mSaveY + "  mUpY = " + mUpY + "  mSlidingDirection : " + mSlidingDirection);
            if (mUpY < mSaveY && mSlidingDirection == SlidingDirection.SLIDING_DOWN) {
                mSlidingDirection = SlidingDirection.SLIDING_RESTORE_DOWN;
            } else if (mUpY > mSaveY && mSlidingDirection == SlidingDirection.SLIDING_UP) {
                mSlidingDirection = SlidingDirection.SLIDING_RESTORE_UP;
            }

            isFinish = true;
        }

        public void run() {
            if(!isFinish) {
                mSaveY = mMoveY;
                //Log.i("wanghg", "mSaveY = " + mSaveY);
                mHandler.postDelayed(this, SAVE_INTERVAL);
            }
        }
    }

    final private class ArrowSliding implements Runnable {
        private float mFrom;
        private float mAlpha;
        private boolean mIsDisplay;
        private boolean mIsMove;
        private boolean isFinish = false;
        private static final float ALPHA_SPEED = 0.05f;
        private static final int ALPHA_INTERVAL = 50;
        private static final int MOVE_DISTANCE = 20;

        public void start(boolean isDisplay, boolean isMove) {
            mIsDisplay = isDisplay;
            mIsMove = isMove;
            if(mIsDisplay) {
                mAlpha = 0.0f;
            }
            else {
                mAlpha = 1.0f;
            }
            isFinish = false;
            mHandler.post(this);
        }

        public void run() {
            if(!isFinish) {
                if(mIsDisplay) {
                    mAlpha += ALPHA_SPEED;
                    if(mAlpha >= 1.0f) {
                        mAlpha = 1.0f;
                        mArrowViewLayoutParams.topMargin = mArrowViewTopMargin;
                        mArrowView.getChildAt(0).setLayoutParams(mArrowViewLayoutParams);
                        isFinish = true;
                    }
                }
                else {
                    if(mIsMove) {
                        mArrowViewLayoutParams.topMargin -= 2;
                    }
                    mAlpha -= ALPHA_SPEED;
                    if(mAlpha <= 0.0f) {
                        mAlpha = 0.0f;
                        mArrowViewLayoutParams.topMargin = mArrowViewTopMargin;
                        mArrowView.getChildAt(0).setLayoutParams(mArrowViewLayoutParams);
                        isFinish = true;
                    }
                }
                //Log.i("wanghg", "mAlpha = " + mAlpha);

                if(mIsMove) {
                    mArrowView.getChildAt(0).setLayoutParams(mArrowViewLayoutParams);
                }
                mArrowView.setAlpha(mAlpha);

                mHandler.postDelayed(this, ALPHA_INTERVAL);
            }
        }
    }
}
