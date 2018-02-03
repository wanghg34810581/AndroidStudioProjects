package com.guli.secmanager.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

/**
 * Created by wangdsh on 16-7-14.
 */
public class AnimUtil {
    public static AlphaAnimation AlphaAnim(AlphaAnimation animation){
        animation = new AlphaAnimation(1, 0);
        animation.setDuration(800);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        return animation;
    }

    public static ScaleAnimation littleToGreatAnim(ScaleAnimation animation){
        animation = new ScaleAnimation(0f,1.0f,0f,1.0f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        return animation;
    }
    public static ScaleAnimation littleToGreatAnim2(ScaleAnimation animation){
        animation = new ScaleAnimation(0f,1.0f,1.0f,1.0f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        return animation;
    }

    public static ScaleAnimation greatToLittleAnim(ScaleAnimation animation){
        animation = new ScaleAnimation(1f,0f,1f,0f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        return animation;
    }

    public static AnimatorSet setBackgroundAnim(ImageView image,float trans_x,float trans_y,int execute_time,int delay){
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(image, "translationX", 0f, trans_x);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(image, "translationY", 0f, trans_y);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(image, "ScaleX", 1f,2f,1f);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(image, "ScaleY", 1f,2f,1f);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(image, "alpha", 0f,1f,0.2f);
        anim1.setRepeatCount(execute_time);
        anim2.setRepeatCount(execute_time);
        anim3.setRepeatCount(execute_time);
        anim4.setRepeatCount(execute_time);
        anim5.setRepeatCount(execute_time);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anim1).with(anim2).with(anim3).with(anim4).with(anim5);
        animatorSet.setDuration(execute_time);
        animatorSet.setStartDelay(delay);
        animatorSet.start();

        return animatorSet;
    }
}
