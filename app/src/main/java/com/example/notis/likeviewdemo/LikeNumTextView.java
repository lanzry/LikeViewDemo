package com.example.notis.likeviewdemo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

public class LikeNumTextView extends View implements Like, ValueAnimator.AnimatorUpdateListener {

    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private int mADigitWidth;

    private float mDefaultTextSize = sp2px(12);

    private float mTextSize;
    private float mRealTextSize;
    private int mTextColor;


    private ObjectAnimator animator;
    private float animProgress = 1;

    private boolean liked;
    private int sumLike;
    private int[] oldSumLikeArray = new int[11];
    private int[] nowSumLikeArray = new int[11];
    private int oldSumLikeArrayLength;
    private int nowSumLikeArrayLength;

    public LikeNumTextView(Context context) {
        this(context, null);
    }

    public LikeNumTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LikeNumTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LikeNumTextView);
        mTextSize = typedArray.getFloat(R.styleable.LikeNumTextView_textSize, -1);
        mTextColor = typedArray.getColor(R.styleable.LikeNumTextView_textColor, Color.BLACK);

        init();
    }

    private void init() {
        mPaint = new Paint();
        mTextSize = mTextSize == -1 ? mDefaultTextSize : mTextSize;
        mRealTextSize = sp2px(mTextSize);
        mPaint.setTextSize(mRealTextSize);
        mPaint.setColor(mTextColor);

        animator = ObjectAnimator.ofFloat(this, "animProgress", 0, 1);
        animator.addUpdateListener(this);

        setSumLike(1099);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) mPaint.measureText("0") * 6,
                    (int) (mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top));
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) mPaint.measureText("0") * 6, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize,
                    (int) (mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        mHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        mADigitWidth = (int) mPaint.measureText("0");
        if (nowSumLikeArrayLength != oldSumLikeArray.length) {
            if (animProgress < 1) {
                mPaint.setAlpha((int) (255 - 255 * animProgress));
                for (int i = oldSumLikeArrayLength - 1; i >= 0; i--) {
                    canvas.drawText("" + oldSumLikeArray[i]
                            , (i != oldSumLikeArrayLength - 1 ? mADigitWidth : 0) * (oldSumLikeArrayLength - 1 - i)
                            , mHeight / 2 - mPaint.descent() / 2 - mPaint.ascent() / 2 - (liked ? 1 : -1) * mRealTextSize * animProgress, mPaint);
                }
            }

            mPaint.setAlpha((int) (255 * animProgress));
            for (int i = nowSumLikeArrayLength - 1; i >= 0; i--) {
                canvas.drawText("" + nowSumLikeArray[i]
                        , (i != nowSumLikeArrayLength - 1 ? mADigitWidth : 0) * (nowSumLikeArrayLength - 1 - i)
                        , mHeight / 2 - mPaint.descent() / 2 - mPaint.ascent() / 2 + (liked ? 1 : -1) * mRealTextSize * (1 - animProgress), mPaint);
            }
        } else {

            for (int i = oldSumLikeArrayLength - 1; i >= 0; i--) {
                if (oldSumLikeArray[i] == nowSumLikeArray[i]) {
                    mPaint.setAlpha(255);
                    canvas.drawText("" + nowSumLikeArray[i]
                            , (i != nowSumLikeArrayLength - 1 ? mADigitWidth : 0) * (nowSumLikeArrayLength - 1 - i)
                            , mHeight / 2 - mPaint.descent() / 2 - mPaint.ascent() / 2, mPaint);
                    continue;
                }
                if (animProgress < 1) {
                    mPaint.setAlpha((int) (255 - 255 * animProgress));
                    canvas.drawText("" + oldSumLikeArray[i]
                            , (i != oldSumLikeArrayLength - 1 ? mADigitWidth : 0) * (oldSumLikeArrayLength - 1 - i)
                            , mHeight / 2 - mPaint.descent() / 2 - mPaint.ascent() / 2 - (liked ? 1 : -1) * mRealTextSize * animProgress, mPaint);
                }

                mPaint.setAlpha((int) (255 * animProgress));
                canvas.drawText("" + nowSumLikeArray[i]
                        , (i != nowSumLikeArrayLength - 1 ? mADigitWidth : 0) * (nowSumLikeArrayLength - 1 - i)
                        , mHeight / 2 - mPaint.descent() / 2 - mPaint.ascent() / 2 + (liked ? 1 : -1) * mRealTextSize * (1 - animProgress), mPaint);
            }
        }
    }

    public void like() {
        liked = true;
        sumLike++;
    }

    public void unlike() {
        liked = false;
        sumLike--;
    }

    @Override
    public void changeLike() {
        if (liked)
            unlike();
        else
            like();

        anylasisLikes();
        requestLayout();
        animator.start();
    }

    private void anylasisLikes() {
        oldSumLikeArrayLength = nowSumLikeArrayLength;
        oldSumLikeArray = Arrays.copyOf(nowSumLikeArray, oldSumLikeArrayLength);
        nowSumLikeArrayLength = String.valueOf(sumLike).length();
        for (int i = 0; i < nowSumLikeArrayLength; i++) {
            nowSumLikeArray[i] = (int) (sumLike / Math.pow(10, i) % 10);
        }
    }

    public float getAnimProgress() {
        return animProgress;
    }

    public void setAnimProgress(float animProgress) {
        this.animProgress = animProgress;
    }

    public int getSumLike() {
        return sumLike;
    }

    public void setSumLike(int sumLike) {
        this.sumLike = sumLike;
        anylasisLikes();
        requestLayout();
        invalidate();
    }

    private int dp2px(int dp) {
        return dp * getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }
}
