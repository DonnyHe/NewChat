package com.lodestreams.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.lodestreams.chat.R;
import com.lodestreams.chat.util.DisplayUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by hjytl on 2016/8/1.
 */

public class Target extends ImageView {
    private boolean mIsScrolling;
    private Target mTarget;
    private Context mContext;
    private ObjectAnimator clockwiseAnimator = null;
    private ObjectAnimator translateToRightAnimation;
    private ObjectAnimator anticlockwiseAnimator;
    private ObjectAnimator translateToLeftAnimation;
    private int mWidth;
    public Target(Context context) {
        this(context, null);
    }

    public Target(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Target(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIsScrolling = false;
        mTarget = this;
        mContext = context;

        mTarget.setImageResource(R.drawable.shoot_target);
        mWidth = DisplayUtil.Dp2px(mContext,50);
        mTarget.post(new Runnable() {
            @Override
            public void run() {
                mTarget.setTranslationY(((ViewGroup)mTarget.getParent()).getHeight() - DisplayUtil.Dp2px(mContext,100));
            }
        });
    }

    public void startScroll(){

        try {
            final AnimatorSet mAnimatorToRight = new AnimatorSet();
            final AnimatorSet mAnimatorToLeft = new AnimatorSet();
            mTarget.setVisibility(View.VISIBLE);
            if (clockwiseAnimator == null) {
                clockwiseAnimator = ObjectAnimator.ofFloat(mTarget,"rotation",0,360);
                clockwiseAnimator.setRepeatCount(3);
                clockwiseAnimator.setInterpolator(new LinearInterpolator());
                clockwiseAnimator.setDuration(1000);

                translateToRightAnimation = ObjectAnimator.ofFloat(mTarget,"translationX",0, DisplayUtil.GetDisplayWidth(mContext) - mWidth);
                translateToRightAnimation.setDuration(4000);

                anticlockwiseAnimator = ObjectAnimator.ofFloat(mTarget,"rotation",0,-360);
                anticlockwiseAnimator.setRepeatCount(3);
                anticlockwiseAnimator.setInterpolator(new LinearInterpolator());
                anticlockwiseAnimator.setDuration(1000);

                translateToLeftAnimation = ObjectAnimator.ofFloat(mTarget,"translationX",DisplayUtil.GetDisplayWidth(mContext) - mWidth,0 );
                translateToLeftAnimation.setDuration(4000);
            }
            mAnimatorToRight.playTogether(clockwiseAnimator,translateToRightAnimation);
            mAnimatorToLeft.playTogether(translateToLeftAnimation,anticlockwiseAnimator);
            mAnimatorToRight.start();
            mAnimatorToRight.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimatorToLeft.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            mAnimatorToLeft.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimatorToRight.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void cancelScroll(){
        //mAnimatorToLeft.end();
        //mAnimatorToRight.end();
    }

}
