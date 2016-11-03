package com.ethossoftworks.ethos.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.ethossoftworks.ethos.R;


// Create a basic circle view
public class CircleView extends View {
    private Paint mBackgroundPaint;
    private Paint mStrokePaint;
    private RectF mBackgroundBounds;
    private RectF mStrokeBounds;
    private float mStartAngle;
    private float mSweepAngle;
    private int mBackgroundColor;
    private int mBackgroundColorPressed;



    public CircleView(Context context) {
        super(context);
        init(context, null);
    }


    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setClickable(true); // This allows the view to handle touch events

        // Get attributes
        TypedArray a = (attrs != null) ? context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleView, 0, 0) : null;
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mBackgroundColor = (a != null) ? a.getColor(R.styleable.CircleView_bgColor, Color.BLACK) : Color.BLACK;
        mBackgroundColorPressed = (a != null) ? a.getColor(R.styleable.CircleView_bgColorPressed, mBackgroundColor) : mBackgroundColor;
        mBackgroundPaint.setColor(mBackgroundColor);
        mStrokePaint.setColor((a != null) ? a.getColor(R.styleable.CircleView_outlineColor, Color.TRANSPARENT) : Color.TRANSPARENT);
        mStrokePaint.setStrokeWidth((a != null) ? a.getDimension(R.styleable.CircleView_outlineWidth, 0.00f) : 0.00f);
        mStartAngle = (a != null) ? a.getFloat(R.styleable.CircleView_startAngle, -90f) - 90f : -90f;
        mSweepAngle = (a != null) ? a.getFloat(R.styleable.CircleView_sweepAngle, 360f) : 360f;
        if (a != null) {
            a.recycle();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle background color change on press only if the view is enabled
        if (isEnabled()) {
            if (mBackgroundColor != mBackgroundColorPressed) {
                int id = event.getAction();
                if (id == MotionEvent.ACTION_DOWN) {
                    this.setTemporaryBackgroundColor(mBackgroundColorPressed);
                } else if (id == MotionEvent.ACTION_UP) {
                    this.setTemporaryBackgroundColor(mBackgroundColor);
                }
            }
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateBounds();
    }


    private void calculateBounds() {
        // Account for padding
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());
        float ww = (float) getWidth() - xpad;
        float hh = (float) getHeight() - ypad;
        float diameter = Math.min(ww, hh);
        float strokeWidth = mStrokePaint.getStrokeWidth();
        float strokeOffset = strokeWidth / 2;

        // Set background bounds
        float backgroundDiameter = diameter - (strokeWidth * 2);
        mBackgroundBounds = new RectF(0f, 0f, backgroundDiameter, backgroundDiameter);
        mBackgroundBounds.offsetTo(getPaddingLeft() + strokeWidth, getPaddingTop() + strokeWidth);

        // Set stroke bounds (stroke is centered, so we need to offset for it)
        float strokeDiameter = diameter - (strokeWidth / 2);
        mStrokeBounds = new RectF(strokeOffset, strokeOffset, strokeDiameter, strokeDiameter);
        mStrokeBounds.offset(getPaddingLeft(), getPaddingTop());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mBackgroundBounds, mStartAngle, mSweepAngle, true, mBackgroundPaint);
        canvas.drawArc(mStrokeBounds, mStartAngle, mSweepAngle, false, mStrokePaint);
    }



    /**
     * Getters and Setters
     * ------------------------------------------------------------------------
     */

    public void setOutlineWidth(float width) {
        mStrokePaint.setStrokeWidth(width);
        calculateBounds();
        invalidate();
    }


    public void setOutlineColor(int color) {
        mStrokePaint.setColor(color);
        invalidate();
    }


    public int getBackgroundColor() {
        // Return the paint color because it is theoretically possible for the current paint color
        // to not be the set background color due to a touch event
        return mBackgroundPaint.getColor();
    }


    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        mBackgroundPaint.setColor(color);
        invalidate();
    }


    private void setTemporaryBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
        invalidate();
    }


    public int getBackgroundColorPressed() {
        return mBackgroundColorPressed;
    }


    public void setBackgroundColorPressed(int color) {
        mBackgroundColorPressed = color;
        invalidate();
    }


    public float getStartAngle() {
        return mStartAngle + 90f;
    }


    public void setStartAngle(float angle) {
        mStartAngle = angle - 90f;
        invalidate();
    }


    public float getSweepAngle() {
        return mSweepAngle;
    }


    public void setSweepAngle(float angle) {
        mSweepAngle = angle;
        invalidate();
    }



    /**
     * Inner Classes
     * ------------------------------------------------------------------------
     */

    public static class CircleAngleAnimation extends Animation {
        private CircleView mCircle;
        private float mOldStart;
        private float mOldSweep;
        private float mNewStart;
        private float mNewSweep;

        public CircleAngleAnimation(CircleView circle, float startAngle, float sweepAngle) {
            mCircle = circle;
            mOldStart = circle.getStartAngle();
            mOldSweep = circle.getSweepAngle();
            mNewStart = startAngle;
            mNewSweep = sweepAngle;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (mOldStart != mNewStart) {
                float angle = mOldStart + ((mNewStart - mOldStart) * interpolatedTime);
                mCircle.setStartAngle(angle);
            }
            if (mOldSweep != mNewSweep) {
                float angle = mOldSweep + ((mNewSweep - mOldSweep) * interpolatedTime);
                mCircle.setSweepAngle(angle);
            }
        }
    }
}