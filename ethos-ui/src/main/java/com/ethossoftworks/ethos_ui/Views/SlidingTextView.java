package com.ethossoftworks.ethos_ui.Views;


import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.ethossoftworks.ethos_ui.R;

// TODO: Implement save instance state
// TODO: Allow styleable gravity to work properly
// TODO: Allow styleable animation duration
public class SlidingTextView extends ViewGroup {
    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;

    private TextView mText1;
    private TextView mText2;
    private LayoutParams mLayoutParams;
    private boolean mIsAnimating = false;
    private QueueEntry mQueue;

    private int mGravity = Gravity.CENTER;
    private int mDirection = VERTICAL;
    private int mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics());
    private int mTextColor = Color.BLACK;


    public SlidingTextView(Context context) {
        super(context);
        init(null);
    }


    public SlidingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    public SlidingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SlidingTextView);
            float scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
            mTextColor = a.getColor(R.styleable.SlidingTextView_textColor, Color.BLACK);
            mTextSize = (int) (a.getDimensionPixelSize(R.styleable.SlidingTextView_textSize, mTextSize) / scaledDensity);
            mGravity = a.getInt(R.styleable.SlidingTextView_gravity, Gravity.CENTER);
            mDirection = a.getInt(R.styleable.SlidingTextView_direction, VERTICAL);
            a.recycle();
        }
        addViews();
    }


    private void addViews() {
        mLayoutParams = new LayoutParams(0, 0);
        mText1 = createTextView();
        mText2 = createTextView();
        addView(mText1);
        addView(mText2);
    }


    private TextView createTextView() {
        TextView view = new TextView(getContext());
        view.setGravity(mGravity);
        view.setTextSize(mTextSize);
        view.setTextColor(mTextColor);
        return view;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        mLayoutParams.width = MeasureSpec.getSize(widthMeasureSpec);
        mLayoutParams.height = MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            child.setLayoutParams(mLayoutParams);
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.layout(0, 0, getWidth(), getHeight());
            if (i == 1) {
                if (mDirection == HORIZONTAL) {
                    child.layout(getWidth(), 0, getWidth() * 2, getHeight());
                } else {
                    child.layout(0, getHeight(), getWidth(), getHeight() * 2);
                }
            }
        }
    }

    private void translateViews(boolean animate) {
        if (animate) {
            mIsAnimating = true;
            createAnim(mText1).start();
            createAnim(mText2)
                .setListener(new Animator.AnimatorListener() {
                    public void onAnimationStart(Animator animator) {}
                    public void onAnimationCancel(Animator animator) {}
                    public void onAnimationRepeat(Animator animator) {}

                    @Override
                    public void onAnimationEnd(Animator animator) { handleAnimationEnd();}
                })
                .start();
        } else if (mDirection == HORIZONTAL) {
            mText1.setTranslationX(-getWidth());
            mText2.setTranslationX(-getWidth());
        } else {
            mText1.setTranslationY(-getHeight());
            mText2.setTranslationY(-getHeight());
        }
    }


    private void handleAnimationEnd() {
        mIsAnimating = false;
        if (mQueue != null) {
            setText(mQueue.text, mQueue.animate);
            mQueue = null;
        }
    }


    private ViewPropertyAnimator createAnim(TextView view) {
        ViewPropertyAnimator anim = view.animate();
        if (mDirection == HORIZONTAL) {
            anim.translationX(-getWidth());
        } else {
            anim.translationY(-getHeight());
        }
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(500);
        return anim;
    }


    private void resetTranslation() {
        if (mDirection == HORIZONTAL) {
            mText1.setTranslationX(0);
            mText2.setTranslationX(0);
        } else {
            mText1.setTranslationY(0);
            mText2.setTranslationY(0);
        }
    }


    private class QueueEntry {
        private String text;
        private boolean animate;

        private QueueEntry(String text, boolean animate) {
            this.text = text;
            this.animate = animate;
        }
    }


    public void setText(int resId) {
        setText(getContext().getString(resId), true);
    }


    public void setText(String string) {
        setText(string, true);
    }


    public void setText(String string, boolean animate) {
        if (mIsAnimating) {
            mQueue = new QueueEntry(string, animate);
            return;
        }
        mText1.setText(mText2.getText());
        mText2.setText(string);
        resetTranslation();
        translateViews(animate);
    }


    public void setGravity(int gravity) {
        mGravity = gravity;
        mText1.setGravity(mGravity);
        mText2.setGravity(mGravity);
    }


    public void setDirection(int direction) {
        mDirection = direction;
        requestLayout();
    }
}