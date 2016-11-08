package com.lodestreams.chat.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.lodestreams.chat.R;
import com.lodestreams.chat.util.DisplayUtil;
import com.lodestreams.chat.util.L;

/**
 * Created by hjytl on 2016/7/29.
 */

public class Arrow extends ImageView {
    private Target mTarget;
    private boolean mIsShooting;
    private Arrow mArrow;
    private int maxTime = 2000;
    private int mFlag ;//碰撞检测精细程度，越低越不精细
    public Arrow(Context context) {
        this(context, null);
    }

    public Arrow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Arrow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mArrow = this;
        mIsShooting = false;
        mFlag = DisplayUtil.Dp2px(context,5);
        mArrow.setImageResource(R.drawable.shoot_arrow);
    }

    public interface OnShotListener{
        void onShoot();
    }
    private OnShotListener mOnShotListener;
    public void setOnShotListener(OnShotListener onShotListener){
        mOnShotListener = onShotListener;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    public void setTarget(Target mTarget) {
        this.mTarget = mTarget;
    }

    /**
     * 主动发射
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param upX
     * @param upY
     * @param speed
     */
    public void shoot(float startX, float startY, float endX, float endY, float upX, float upY,float speed) {//float mStartX,float mStartY,float endX,float endY
        mArrow.setVisibility(View.VISIBLE);
        float degree = startX > endX ? -getRotateDegree(startX, startY, upX, upY) : getRotateDegree(startX, startY, upX, upY);
        mArrow.setRotation(degree);

        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, startX, Animation.ABSOLUTE, endX, Animation.ABSOLUTE, startY, Animation.ABSOLUTE, endY);
        translateAnimation.setDuration(maxTime / getShotStrength(speed));
        translateAnimation.setFillAfter(true);
        mArrow.startAnimation(translateAnimation);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsShooting = true;
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                L.e("end");
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsShooting = false;
                        mArrow.clearAnimation();
                        mArrow.invalidate();
                        mArrow.setVisibility(View.GONE);
                    }
                },1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void receiveShoot(float startX, float startY, float endX, float endY,int shootStength){
        mArrow.setTranslationX(0);
        mArrow.setVisibility(View.VISIBLE);
        float degree = startX > endX ? -getRotateDegree(startX, startY, endX, endY) : getRotateDegree(startX, startY, endX, endY);
        L.e("startX:"+startX);
        L.e("startY:"+startY);
        L.e("endX:"+endX);
        L.e("endY:"+endY);
        L.e("degree:"+degree);
        mArrow.setRotation(180-degree);
        mArrow.setTranslationX(startX);
        mArrow.setTranslationY(startY);

        final ObjectAnimator translationX = ObjectAnimator.ofFloat(mArrow,"translationX",endX);
        final ObjectAnimator translationY = ObjectAnimator.ofFloat(mArrow,"translationY",endY);

        translationX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(isShareRect(mArrow,mTarget) || isInRect(mArrow,mTarget)){
                    mOnShotListener.onShoot();
                    translationX.cancel();
                    translationY.cancel();
                    L.e("--------------------------------------------------------");
                }
                /*L.e("mArrow.getTranslationX:"+mArrow.getTranslationX());
                L.e("mArrow.getTranslationY:"+mArrow.getTranslationY());
                L.e("mTarget.getTranslationX:"+mTarget.getTranslationX());
                L.e("mTarget.getTranslationY:"+mTarget.getTranslationY());*/

            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translationX,translationY);
        animatorSet.setDuration(maxTime / shootStength);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                L.e("end");
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsShooting = false;
                        mArrow.clearAnimation();
                        mArrow.invalidate();
                        mArrow.setVisibility(View.GONE);
                    }
                },1000);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        /*TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, startX, Animation.ABSOLUTE, endX, Animation.ABSOLUTE, startY, Animation.ABSOLUTE, endY);
        translateAnimation.setDuration(maxTime / shootStength);
        translateAnimation.setFillAfter(true);
        mArrow.startAnimation(translateAnimation);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsShooting = true;
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                L.e("end");
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsShooting = false;
                        mArrow.clearAnimation();
                        mArrow.invalidate();
                        mArrow.setVisibility(View.GONE);
                    }
                },1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });*/
    }

    /**
     * 根据手滑速度区间返回力量等级
     * @param speed 实测范围为几十到几万
     * @return
     */
    public static int getShotStrength(float speed){
        int shootStrength;
        if (speed < 500){
            shootStrength = 1;
        }else if (speed < 2000){
            shootStrength = 2;
        }else if (speed < 3000){
            shootStrength = 3;
        }else if (speed < 4000){
            shootStrength = 4;
        }else {
            shootStrength = 5;
        }
        return shootStrength;
    }

    private float getRotateDegree(float startX, float startY, float upX, float upY) {

        float k = Math.abs((startY - upY) / (startX - upX));   //斜率
        double angle = Math.atan(k); //弧度
        L.e("startX:" + startX);
        L.e("startY:" + startY);
        L.e("upX:" + upX);
        L.e("upY:" + upY);
        L.e("mStartX > endX角度：" + (float) Math.toDegrees(angle));
        return 90 - (float) Math.toDegrees(angle);

    }

    public boolean isShooting() {
        return mIsShooting;
    }

    /**
     * 是否有交集
     * @param v1
     * @param v2
     * @return
     */
    public boolean isShareRect(View v1, View v2) {
        RectF rect1 = getTranslationRect(v1);
        RectF rect2 = getTranslationRect(v2);
        boolean isLeftIn = rect1.left + mFlag >= rect2.left && rect1.left + mFlag <= rect2.right;
        boolean isTopIn = rect1.top >= rect2.top && rect1.top <= rect2.bottom;
        boolean isRightIn = rect1.right - mFlag >= rect2.left && rect1.right - mFlag <= rect2.right;
        boolean isBottomIn = rect1.bottom >= rect2.top && rect1.bottom <= rect2.bottom;

        return (isLeftIn && isTopIn) || (isLeftIn && isBottomIn)
                || (isRightIn && isTopIn) || (isRightIn && isBottomIn)
                || (isTopIn && isLeftIn) || (isTopIn && isRightIn)
                || (isBottomIn && isLeftIn) || (isBottomIn && isRightIn);
    }

    public boolean isInRect(View v1, View v2) {
        RectF rect1 = getTranslationRect(v1);
        RectF rect2 = getTranslationRect(v2);
        return rect1.left >= rect2.left && rect1.top >= rect2.top
                && rect1.right <= rect2.right && rect1.bottom <= rect2.bottom;
    }

    /**
     * 获取相对于TranslationX，Y的矩形区域
     *
     * @param v
     * @return
     */
    public static RectF getTranslationRect(View v) {
        RectF rect = new RectF(v.getTranslationX(), v.getTranslationY(),
                v.getTranslationX() + v.getWidth(), v.getTranslationY()
                + v.getHeight());
        return rect;
    }
}
