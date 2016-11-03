package com.ethossoftworks.ethos.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.ethossoftworks.ethos.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WheelPickerView extends View {
    public static final int STATE_DEFAULT = 0; // The user is not interacting with the wheel
    public static final int STATE_TOUCHING = 1; // The user is currently touching the wheel
    public static final int STATE_ANIMATING = 2; // The wheel is currently animating

    public static final int NOTIFY_SOURCE_INTERNAL = 0; // The value was changed inside the WheelPicker class
    public static final int NOTIFY_SOURCE_PROGRAMMATIC = 1; // The value was change outside of the WheelPicker class
    public static final int NOTIFY_SOURCE_USER = 2; // The value was changed by touching the wheel

    private final int MIN_FLING_VELOCITY = 500;
    private final int MAX_FLING_VELOCITY = 10000;
    private List<String> mList = new ArrayList<>();
    private int mNumDisplayItems = 9;
    private int mItemHeight = 0;
    private int mPaddingHeight = 0;
    private float mLastDownOrMoveEventY = 0;
    private float mMinScale = 0.25f;
    private float mMaxScale = 1.0f;
    private Paint mTextPaint;
    private int mTextColor = Color.BLACK;
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 21, getResources().getDisplayMetrics());
    private Rect mTextBounds = new Rect();
    private int mTopDividerY;
    private int mBottomDividerY;
    private Paint mDividerPaint;
    private int mDividerColor = Color.argb(50, 0, 0, 0);
    private int mDividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
    private int mCurrentIndex = 0;
    private Scroller mFlingScroller;
    private Scroller mAdjustScroller;
    private VelocityTracker mVelocityTracker;
    private OnValueChangeListener mValueChangeListener;
    private int mMaxHeight = -1;
    private int mCurrentInteractionState = 0;
    private String mExtraText = "";


    /**
     * Constructors and helpers
     * ------------------------------------------------------------------------
     */

    public WheelPickerView(Context context) {
        super(context);
        init(null);
    }

    public WheelPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WheelPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        // Create the wheel paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(mTextSize);
        paint.setColor(mTextColor);
        mTextPaint = paint;

        // Create the selection
        Paint dividerPaint = new Paint();
        dividerPaint.setColor(mDividerColor);
        mDividerPaint = dividerPaint;

        // Set up scrollers
        mFlingScroller = new Scroller(getContext(), null, true);
        mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));

        // Get attributes
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WheelPicker);
            mMaxHeight = a.getDimensionPixelSize(R.styleable.WheelPicker_maxHeight, -1); // Max height
            mExtraText = (a.hasValue(R.styleable.WheelPicker_extraText)) ? a.getString(R.styleable.WheelPicker_extraText) : "";
            int list = a.getResourceId(R.styleable.WheelPicker_list, -1);
            if (list != -1) {
                setList(Arrays.asList(getResources().getStringArray(list)));
            }
            mNumDisplayItems = a.getInt(R.styleable.WheelPicker_numDisplayItems, mNumDisplayItems);
            mTextSize = a.getDimensionPixelSize(R.styleable.WheelPicker_textSize, (int) mTextSize);
            mTextPaint.setColor(a.getColor(R.styleable.WheelPicker_textColor, mTextColor));
            int alignment = a.getInt(R.styleable.WheelPicker_textAlignment, 2);
            if (alignment == 1) {
                mTextPaint.setTextAlign(Paint.Align.LEFT);
            } else if (alignment == 3) {
                mTextPaint.setTextAlign(Paint.Align.RIGHT);
            } else {
                mTextPaint.setTextAlign(Paint.Align.CENTER);
            }
            mDividerColor = a.getColor(R.styleable.WheelPicker_dividerColor, mDividerColor);
            mDividerWidth = a.getDimensionPixelSize(R.styleable.WheelPicker_dividerWidth, mDividerWidth);
            a.recycle();
        }

        if (isInEditMode()) {
            setList(Arrays.asList("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"));
        }
    }



    /**
     * Overridden Methods
     * ------------------------------------------------------------------------
     */

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Update the padding and item height values
        mItemHeight = getHeight() / mNumDisplayItems;
        mPaddingHeight = (getHeight() / 2) - (mItemHeight / 2);

        // Update Font metrics
        updateFontMetrics();

        // Update the selection dividers
        updateDividerMetrics();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // Make sure that if the view has a value before it's laid out it is accurately represented
        // onLayout may be called when an adjacent view changes its bounds (i.e. TextView setting text)
        if (mCurrentInteractionState == STATE_DEFAULT) {
            ensureValueCentered(false);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float x = 0;
        float y = 0;
        float center = getScrollY() + (((getHeight() / 2) + (mItemHeight / 2) + mTextBounds.exactCenterY()));
        float centerOffset;
        float multiplier;
        int alpha;
        float scaleY;

        // Set X based on alignment
        if (mTextPaint.getTextAlign() == Paint.Align.CENTER) {
            x = getWidth() / 2;
        } else if (mTextPaint.getTextAlign() == Paint.Align.RIGHT) {
            x = getWidth();
        }

        // Skip drawing on header padding
        y += mPaddingHeight;

        // Draw the selector wheel
        y += ((mItemHeight / 2) - mTextBounds.exactCenterY());
        for (int i = 0, l = mList.size(); i < l; i++) {
            // Calculate and apply alpha
            centerOffset = Math.abs(y - center);
            multiplier = (1f - Math.min(1f, Math.max(0f, (centerOffset / (float) (getHeight() / 2))))); // 1.00 is centered, 0 is off screen
            alpha = (int) (255f * multiplier);
            mTextPaint.setAlpha(alpha);

            // Draw items only if the view is visible
            if (multiplier > 0) {
                // Calculate and apply skew
                canvas.save();
                scaleY = ((mMaxScale - mMinScale) * multiplier) + mMinScale;
                canvas.scale(1f, scaleY, 0, y);

                // Draw text
                canvas.drawText(mList.get(i).concat(mExtraText), x, y, mTextPaint);
                canvas.restore();
            }
            y += mItemHeight;
        }

        // Draw selection dividers
        canvas.drawRect(0, getScrollY() + mTopDividerY, getWidth(), getScrollY() + mTopDividerY + mDividerWidth, mDividerPaint);
        canvas.drawRect(0, getScrollY() + mBottomDividerY, getWidth(), getScrollY() + mBottomDividerY + mDividerWidth, mDividerPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        } else if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mCurrentInteractionState = STATE_TOUCHING;
                mLastDownOrMoveEventY = event.getY();
                if (!mFlingScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                }
                if (!mAdjustScroller.isFinished()) {
                    mAdjustScroller.forceFinished(true);
                }
            } break;
            case MotionEvent.ACTION_MOVE: {
                // Get Y delta
                float currentMoveY = event.getY();
                int deltaMoveY = (int) -((currentMoveY - mLastDownOrMoveEventY));
                int postScrollY = getScrollY() + deltaMoveY; // This will be the scroll y position after applying the delta
                mLastDownOrMoveEventY = currentMoveY;

                // Check bounds
                if (postScrollY < 0) {
                    scrollTo(0, 0);
                } else if (postScrollY > ((mList.size() - 1) * mItemHeight)) {
                    scrollTo(0, ((mList.size() - 1) * mItemHeight));
                } else {
                    scrollBy(0, deltaMoveY);
                }
            } break;
            case MotionEvent.ACTION_UP: {
                mCurrentInteractionState = STATE_ANIMATING;
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, MAX_FLING_VELOCITY);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > MIN_FLING_VELOCITY) {
                    fling(initialVelocity);
                } else {
                    setIndexInternal(findNearestValue(), NOTIFY_SOURCE_USER, true);
                    ensureValueCentered(true);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            } break;
        }

        // Call touch delegate
        super.onTouchEvent(event);
        return true;
    }


    @Override
    // Computes the scroll and animates fling and adjust scrollers
    public void computeScroll() {
        super.computeScroll();
        Scroller scroller = mFlingScroller;
        if (scroller.isFinished()) {
            scroller = mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        scrollBy(0, scroller.getCurrY() - getScrollY());
        if (scroller.isFinished()) {
            setIndexInternal(findNearestValue(), NOTIFY_SOURCE_USER, true);
            ensureValueCentered(true);
        } else {
            invalidate();
        }
    }


    @Override
    // Enable the use of MaxHeight
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mMaxHeight != -1 && parentHeight > mMaxHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha((enabled) ? 1f : 0.6f);
    }



    /**
     * Public Methods
     * ------------------------------------------------------------------------
     */

    // Set the list of items to display
    public void setList(List<String> items) {
        if (items.size() > 0) {
            mList = items;
            setIndexInternal(0, NOTIFY_SOURCE_INTERNAL, true);
        }
        updateFontMetrics();
        invalidate();
    }


    // Get the list of items in the wheel
    public List<String> getList() {
        return mList;
    }


    // Set text size
    public void setTextSize(float value) {
        mTextSize = value;
        mTextPaint.setTextSize(mTextSize);
        updateFontMetrics();
        invalidate();
    }


    public void setTextAlignment(Paint.Align alignment) {
        mTextPaint.setTextAlign(alignment);
        invalidate();
    }


    // Set text color
    public void setTextColor(int color) {
        mTextColor = color;
        mTextPaint.setColor(mTextColor);
        invalidate();
    }


    // Set display number
    public void setDisplayNumItems(int number) {
        mNumDisplayItems = number;
        invalidate();
    }


    // Set selection divider color
    public void setDividerColor(int color) {
        mDividerColor = color;
        mDividerPaint.setColor(mDividerColor);
        invalidate();
    }


    // Set divider width in pixels
    public void setDividerWidth(int pixels) {
        mDividerWidth = pixels;
        updateDividerMetrics();
        invalidate();
    }


    // Sets the value of the number wheel
    public void setValue(String value, boolean smoothScroll, boolean notifyListener) {
        if (!mList.contains(value)) {
            return;
        }
        setIndexInternal(mList.indexOf(value), NOTIFY_SOURCE_PROGRAMMATIC, notifyListener);
        ensureValueCentered(smoothScroll);
    }


    // Sets the value of the number wheel
    public void setValue(String value, boolean smoothScroll) {
        setValue(value, smoothScroll, true);
    }


    // Sets the value of the number wheel
    public void setValue(String value) {
        setValue(value, false);
    }


    public void setValueIndex(int index, boolean smoothScroll, boolean notifyListener) {
        setValue(mList.get(index), smoothScroll, notifyListener);
    }


    // Get value
    public String getValue() {
        return (mList.size() == 0) ? null : mList.get(mCurrentIndex);
    }


    public int getValueIndex() { return mCurrentIndex; }


    // Sets the OnChangeListener
    public void setOnValueChangeListener(OnValueChangeListener listener) {
        mValueChangeListener = listener;
    }


    // Returns the current user interaction state
    public int getInteractionState() {
        return mCurrentInteractionState;
    }


    // Set the extra text to be appended to the value in the view
    public void setExtraText(String text) {
        mExtraText = text;
    }



    /**
     * Private Methods
     * ------------------------------------------------------------------------
     */

    // Update Font metrics
    private void updateFontMetrics() {
        if (mList.size() == 0) {
            return;
        }
        String maxValue = mList.get(mList.size() - 1);
        mTextPaint.getTextBounds(maxValue, 0, maxValue.length(), mTextBounds);
    }


    // Update divider metrics
    private void updateDividerMetrics() {
        mTopDividerY = (getHeight() / 2) - (mItemHeight / 2) - (mDividerWidth / 2);
        mBottomDividerY = (getHeight() / 2) + (mItemHeight / 2) - (mDividerWidth / 2);
    }


    // Set the value internally
    // index: the index of the value to set
    // notifySource: Where this call is coming from, the user, the class, or outside this class
    // notifyListener: Whether or not to call the listener
    private void setIndexInternal(int index, int notifySource, boolean notifyListener) {
        if (mCurrentIndex == index) {
            return;
        } else if (mValueChangeListener != null && notifyListener) {
            String oldValue = mList.get(mCurrentIndex);
            mCurrentIndex = index;
            mValueChangeListener.onValueChanged(this, oldValue, mList.get(mCurrentIndex), notifySource);
        } else {
            mCurrentIndex = index;
        }
    }


    // Finds the nearest value index
    private int findNearestValue() {
        return Math.round((float) getScrollY() / mItemHeight);
    }


    // Scrolls to an index
    private void ensureValueCentered(boolean smoothScroll) {
        int deltaY = -(getScrollY() - (mItemHeight * mCurrentIndex));
        if (deltaY == 0) {
            mCurrentInteractionState = STATE_DEFAULT;
            return;
        } else if (smoothScroll) {
            mAdjustScroller.startScroll(0, getScrollY(), 0, deltaY, 500);
        } else {
            scrollBy(0, deltaY);
        }
        invalidate();
    }


    // Helper method for flinging
    private void fling(int velocityY) {
        mFlingScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, 0, ((mList.size() - 1) * mItemHeight));
        invalidate();
    }



    /**
     * Interfaces
     * ------------------------------------------------------------------------
     */

    // On value change listener
    public interface OnValueChangeListener {
        void onValueChanged(WheelPickerView view, String oldValue, String value, int notifySource);
    }
}