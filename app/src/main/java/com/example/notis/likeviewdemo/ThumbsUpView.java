package com.example.notis.likeviewdemo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

public class ThumbsUpView extends View implements Like, ValueAnimator.AnimatorUpdateListener {

    private Drawable selectedDrawable;
    private Drawable shiningDrawable;
    private Drawable unselectedDrawable;

    private Paint mPaint;

    private ObjectAnimator outAnimator;
    private ObjectAnimator likeInAnimator;
    private ObjectAnimator unlikeInAnimator;
    private ObjectAnimator flashAnimator;
    private AnimatorSet likeAnimatorSet;
    private AnimatorSet unlikeAnimatorSet;

    private float outProgress = 1;
    private float likeInProgress;
    private float unlikeInProgress = 1;
    private float flashProgress;

    private int mWidth;
    private int mHeight;
    private int mDefauleWidth = dp2px(32);
    private int mDefauleHeight = dp2px(32);

    private Path clipFlashPath = new Path();

    private boolean liked;

    public ThumbsUpView(Context context) {
        this(context, null);
    }

    public ThumbsUpView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbsUpView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        selectedDrawable = getResources().getDrawable(R.drawable.ic_messages_like_selected);
        shiningDrawable = getResources().getDrawable(R.drawable.ic_messages_like_selected_shining);
        unselectedDrawable = getResources().getDrawable(R.drawable.ic_messages_like_unselected);

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dp2px(2));
        mPaint.setShadowLayer(dp2px(1), dp2px(1), dp2px(1), Color.RED);
        setLayerType(LAYER_TYPE_HARDWARE, mPaint);

        outAnimator = ObjectAnimator.ofFloat(this, "outProgress", 0, 1);
        likeInAnimator = ObjectAnimator.ofFloat(this, "likeInProgress", 0, 1);
        unlikeInAnimator = ObjectAnimator.ofFloat(this, "unlikeInProgress", 0, 1);
        flashAnimator = ObjectAnimator.ofFloat(this, "flashProgress", 0, 1);

        outAnimator.setDuration(100);
        likeInAnimator.setDuration(400);

        likeInAnimator.setInterpolator(new BounceInterpolator());

        outAnimator.addUpdateListener(this);
        likeInAnimator.addUpdateListener(this);
        unlikeInAnimator.addUpdateListener(this);

        likeAnimatorSet = new AnimatorSet();
        unlikeAnimatorSet = new AnimatorSet();
        likeAnimatorSet.play(outAnimator).before(likeInAnimator).before(flashAnimator);
        unlikeAnimatorSet.play(outAnimator).before(unlikeInAnimator);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST)
            setMeasuredDimension(mDefauleWidth, mDefauleHeight);
        else if (widthMode == MeasureSpec.AT_MOST)
            setMeasuredDimension(mDefauleWidth, heightSize);
        else if (heightMode == MeasureSpec.AT_MOST)
            setMeasuredDimension(widthSize, mDefauleHeight);
    }

    // 画布用于主要内容的绘制比例，剩余的0.05用于点赞时一个红圈扩散效果。
    private float mainDrawScale = 0.95f;
    // 拇指大小占绘制区域的比例
    private float thumbsScale = 0.8f;
    // 拇指图位置偏离绘制中心的距离
    private int thumbsOffsetY;
    // 发光图占绘制区域的比例
    private float shiningScale = 0.625f;
    // 发光图位置偏离绘制中心的距离
    private int shiningOffsetY;
    // 缩小动画执行时，图片缩小部分的比例
    private float minifyScale = 0.3f;
    // 发光图的缩小比例
    private float shiningMinifyScale = 0.15f;
    // 画拇指的区域
    private Rect thumbsRect = new Rect();
    // 画发光的区域
    private Rect shiningRect = new Rect();
    // 画布绘制的中心点
    private Point centerPoint = new Point();
    // 正方形绘制区域的边长
    private int squareSideLen = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制区域的高和宽
        mWidth = (int) (mainDrawScale * (getWidth() - getPaddingLeft() - getPaddingRight()));
        mHeight = (int) (mainDrawScale * (getHeight() - getPaddingTop() - getPaddingBottom()));

        // 绘制中心
        centerPoint.x = getWidth() / 2;
        centerPoint.y = getHeight() / 2;

        // 使用正方形的画布来进行绘制，选出宽高较小的作为边长
        if (Math.max(mWidth, mHeight) == mHeight)
            squareSideLen = mWidth;
        else
            squareSideLen = mHeight;

        // 拇指的绘制位置在y轴上的偏移量
        thumbsOffsetY = (int) (0.15 * squareSideLen);
        // 发光的绘制位置在y轴上的偏移量
        shiningOffsetY = (int) (-0.28 * squareSideLen);
        // 拇指缩小时，跟随拇指往下移动，保持两张图片的一致性
        int animateY = (int) (thumbsScale * minifyScale * outProgress * squareSideLen / 2);

        /*** 退出动画 ***/
        // 缩小动画期间，拇指drawable的绘制区域（包括灰色和红色）
        if (outProgress < 1) {
            thumbsRect.left = (int) (centerPoint.x - thumbsScale * (1 - minifyScale * outProgress) * squareSideLen / 2);
            thumbsRect.top = (int) (centerPoint.y - thumbsScale * (1 - minifyScale * outProgress) * squareSideLen / 2) + thumbsOffsetY;
            thumbsRect.right = (int) (centerPoint.x + thumbsScale * (1 - minifyScale * outProgress) * squareSideLen / 2);
            thumbsRect.bottom = (int) (centerPoint.y + thumbsScale * (1 - minifyScale * outProgress) * squareSideLen / 2) + thumbsOffsetY;

            if (liked) {
                unselectedDrawable.draw(canvas);
                unselectedDrawable.setBounds(thumbsRect);
            } else {
                selectedDrawable.draw(canvas);
                selectedDrawable.setBounds(thumbsRect);
            }
        }

        // 发光的缩小动画期间，发光drawable的绘制区域
        if (outProgress < 1 && !liked) {
            shiningRect.left = (int) (centerPoint.x - shiningScale * (1 - shiningMinifyScale * outProgress) * squareSideLen / 2);
            shiningRect.top = (int) (centerPoint.y - shiningScale * (1 - shiningMinifyScale * outProgress) * squareSideLen / 2) + shiningOffsetY + animateY;
            shiningRect.right = (int) (centerPoint.x + shiningScale * (1 - shiningMinifyScale * outProgress) * squareSideLen / 2);
            shiningRect.bottom = (int) (centerPoint.y + shiningScale * (1 - shiningMinifyScale * outProgress) * squareSideLen / 2) + shiningOffsetY + animateY;
            shiningDrawable.setBounds(shiningRect);
            shiningDrawable.draw(canvas);
        }

        if (outProgress < 1)
            // 避免if嵌套太多，这里作一个return
            return;

        /*** 进入动画 ***/
        if (liked) {
            // 画红色扩散波纹
            if (flashProgress < 1) {
                mPaint.setAlpha((int) (30 * (1 - flashProgress)));
                canvas.drawCircle(centerPoint.x, centerPoint.y + squareSideLen / 6,
                        (float) (squareSideLen * 25 / 48 * (0.5 + 0.5 * flashProgress)), mPaint);
            }

            // 画拇指
            thumbsRect.left = (int) (centerPoint.x - thumbsScale * (1 - minifyScale * (1 - likeInProgress)) * squareSideLen / 2);
            thumbsRect.top = (int) (centerPoint.y - thumbsScale * (1 - minifyScale * (1 - likeInProgress)) * squareSideLen / 2) + thumbsOffsetY;
            thumbsRect.right = (int) (centerPoint.x + thumbsScale * (1 - minifyScale * (1 - likeInProgress)) * squareSideLen / 2);
            thumbsRect.bottom = (int) (centerPoint.y + thumbsScale * (1 - minifyScale * (1 - likeInProgress)) * squareSideLen / 2) + thumbsOffsetY;
            selectedDrawable.setBounds(thumbsRect);
            selectedDrawable.draw(canvas);

            // 画发光
            shiningRect.left = (int) (centerPoint.x - shiningScale * squareSideLen / 2);
            shiningRect.top = (int) (centerPoint.y - shiningScale * squareSideLen / 2) + shiningOffsetY;
            shiningRect.right = (int) (centerPoint.x + shiningScale * squareSideLen / 2);
            shiningRect.bottom = (int) (centerPoint.y + shiningScale * squareSideLen / 2) + shiningOffsetY;
            canvas.save();
            clipFlashPath.addCircle(mWidth / 2 + mHeight * 2 / 24, mHeight * 5 / 8, mHeight * 14 / 24 * flashProgress, Path.Direction.CCW);
            canvas.clipPath(clipFlashPath);
            shiningDrawable.setBounds(shiningRect);
            shiningDrawable.draw(canvas);
            clipFlashPath.reset();
            canvas.restore();
        } else {
            // 画拇指
            thumbsRect.left = (int) (centerPoint.x - thumbsScale * (1 - minifyScale * (1 - unlikeInProgress)) * squareSideLen / 2);
            thumbsRect.top = (int) (centerPoint.y - thumbsScale * (1 - minifyScale * (1 - unlikeInProgress)) * squareSideLen / 2) + thumbsOffsetY;
            thumbsRect.right = (int) (centerPoint.x + thumbsScale * (1 - minifyScale * (1 - unlikeInProgress)) * squareSideLen / 2);
            thumbsRect.bottom = (int) (centerPoint.y + thumbsScale * (1 - minifyScale * (1 - unlikeInProgress)) * squareSideLen / 2) + thumbsOffsetY;
            unselectedDrawable.setBounds(thumbsRect);
            unselectedDrawable.draw(canvas);

            // 画发光(看原效果，发光消失前会有一个顿一下的效果，猜测是这里还需要画一点，加上效果接近)
            shiningDrawable.setAlpha((int) (120 * (1 - unlikeInProgress * 2)));
            animateY = (int) (thumbsScale * minifyScale * (1 - unlikeInProgress) * squareSideLen / 2);
            if (unlikeInProgress < 0.1) {
                shiningRect.left = (int) (centerPoint.x - shiningScale * (1 - shiningMinifyScale * (1 - unlikeInProgress)) * squareSideLen / 2);
                shiningRect.top = (int) (centerPoint.y - shiningScale * (1 - shiningMinifyScale * (1 - unlikeInProgress)) * squareSideLen / 2) + shiningOffsetY + animateY;
                shiningRect.right = (int) (centerPoint.x + shiningScale * (1 - shiningMinifyScale * (1 - unlikeInProgress)) * squareSideLen / 2);
                shiningRect.bottom = (int) (centerPoint.y + shiningScale * (1 - shiningMinifyScale * (1 - unlikeInProgress)) * squareSideLen / 2) + shiningOffsetY + animateY;
                shiningDrawable.setBounds(shiningRect);
                shiningDrawable.draw(canvas);
            }
            shiningDrawable.setAlpha(255);
        }
    }

    public float getOutProgress() {
        return outProgress;
    }

    public void setOutProgress(float outProgress) {
        this.outProgress = outProgress;
    }

    public float getLikeInProgress() {
        return likeInProgress;
    }

    public void setLikeInProgress(float likeInProgress) {
        this.likeInProgress = likeInProgress;
    }

    public float getUnlikeInProgress() {
        return unlikeInProgress;
    }

    public void setUnlikeInProgress(float unlikeInProgress) {
        this.unlikeInProgress = unlikeInProgress;
    }

    public float getFlashProgress() {
        return flashProgress;
    }

    public void setFlashProgress(float flashProgress) {
        this.flashProgress = flashProgress;
    }

    private int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + 0.5);
    }

    public void like() {
        liked = true;
        if (likeAnimatorSet.isRunning())
            return;
        if (unlikeAnimatorSet.isRunning())
            unlikeAnimatorSet.cancel();
        likeAnimatorSet.start();
    }

    public void unlike() {
        liked = false;
        if (unlikeAnimatorSet.isRunning())
            return;
        if (likeAnimatorSet.isRunning())
            unlikeAnimatorSet.cancel();
        unlikeAnimatorSet.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

    @Override
    public void changeLike() {
        if (liked)
            unlike();
        else
            like();
    }
}
