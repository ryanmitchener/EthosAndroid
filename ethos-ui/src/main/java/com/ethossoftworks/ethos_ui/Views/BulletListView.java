package com.ethossoftworks.ethos_ui.Views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ethossoftworks.ethos.Util;
import com.ethossoftworks.ethos_ui.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ethossoftworks.ethos_ui.R.id.circle;

// TODO: Add setSelectable for child clicking
// TODO: Add ordered and unordered list options
public class BulletListView extends ListView {
    private LayoutInflater mInflater;
    private float mTextSize;
    private float mBulletSize;
    private int mBulletIndent;
    private int mTextPadding;
    private int mTextColor;
    private int mLineSpacingExtra;


    public BulletListView(Context context) {
        super(context);
        init(null);
    }


    public BulletListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    public BulletListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        mInflater = LayoutInflater.from(getContext());
        setDivider(null);
        setInternalAdapter();
        populateAttributes(attrs);
        setSelector(android.R.color.transparent);

        if (isInEditMode()) {
            setList(Arrays.asList("One", "-Two", "--Three", "-Four"));
        }
    }


    private void populateAttributes(AttributeSet attrs) {
        mTextColor = Color.BLACK;
        mTextSize = Util.spToPx(16, getContext());
        mLineSpacingExtra = 0;
        mBulletSize = Util.dpToPx(9, getContext());
        mBulletIndent = (int) mBulletSize * 2;
        mTextPadding = (int) mBulletSize;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.BulletListView, 0, 0);
            mTextColor = a.getColor(R.styleable.BulletListView_textColor, mTextColor);
            mTextSize = a.getDimensionPixelSize(R.styleable.BulletListView_textSize, (int) mTextSize);
            mTextPadding = a.getDimensionPixelSize(R.styleable.BulletListView_textPadding, mTextPadding);
            mLineSpacingExtra = a.getDimensionPixelSize(R.styleable.BulletListView_lineSpacingExtra, mLineSpacingExtra);
            mBulletSize = a.getDimensionPixelSize(R.styleable.BulletListView_bulletSize, (int) mBulletSize);
            mBulletIndent = a.getDimensionPixelSize(R.styleable.BulletListView_bulletIndent, mBulletIndent);
            if (a.hasValue(R.styleable.BulletListView_list)) {
                setList(Arrays.asList(a.getResources().getStringArray(a.getResourceId(R.styleable.BulletListView_list, 0))));
            }
            a.recycle();
        }
    }


    private void setInternalAdapter() {
        setAdapter(new BulletListAdapter());
    }


    public void setList(List<String> data) {
        ((BulletListAdapter) getAdapter()).setData(data);
    }


    public void setBulletSize(int pixels) {
        mBulletSize = pixels;
        ((BulletListAdapter) getAdapter()).notifyDataSetChanged();
    }


    public float getBulletSize() {
        return mBulletSize;
    }


    public void setTextPadding(int pixels) {
        mTextPadding = pixels;
    }


    public int getTextPadding() {
        return mTextPadding;
    }


    public void setLineSpacingExtra(int pixels) {
        mLineSpacingExtra = pixels;
        ((BulletListAdapter) getAdapter()).notifyDataSetChanged();
    }


    public int getLineSpacingExtra() {
        return mLineSpacingExtra;
    }


    public void setTextSize(int pixels) {
        mTextSize = pixels;
        ((BulletListAdapter) getAdapter()).notifyDataSetChanged();
    }


    public float getTextSize() {
        return mTextSize;
    }


    public void setTextColor(int color) {
        mTextColor = color;
        ((BulletListAdapter) getAdapter()).notifyDataSetChanged();
    }


    public int getTextColor() {
        return mTextColor;
    }


    public void setBulletIndent(int pixels) {
        mBulletIndent = pixels;
        ((BulletListAdapter) getAdapter()).notifyDataSetChanged();
    }


    public int getBulletIndent() {
        return mBulletIndent;
    }


    public static int getBulletDepth(String item) {
        int depth = 0;
        while (item.charAt(depth) == '-') {
            depth++;
        }
        return depth;
    }



    /**
     * Adapter
     * ------------------------------------------------------------------------
     */
    public class BulletListAdapter extends BaseAdapter {
        private List<String> mData = new ArrayList<>();
        private Rect mBounds = new Rect();

        private class ViewHolder {
            private View cont;
            private CircleView circle;
            private TextView text;
        }


        public void setData(List<String> data) {
            mData = data;
            notifyDataSetChanged();
        }


        @Override
        public String getItem(int position) {
            return mData.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public int getCount() {
            return mData.size();
        }


        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder vh = new ViewHolder();
            if (view == null) {
                view = mInflater.inflate(R.layout.bullet_list_view, parent, false);
                vh.cont = view;
                vh.circle = (CircleView) view.findViewById(circle);
                vh.text = (TextView) view.findViewById(R.id.text);
                view.setTag(vh);
            } else {
                vh = (ViewHolder) view.getTag();
            }

            setViewAttributes(vh, position);
            measureViews(vh);
            setBulletTranslationY(vh);
            return view;
        }


        private void setViewAttributes(ViewHolder vh, int position) {
            int depth = BulletListView.getBulletDepth(getItem(position));
            vh.cont.setPadding(mBulletIndent * depth, mLineSpacingExtra / 2, 0, mLineSpacingExtra / 2);
            vh.text.setText(getItem(position).substring(depth));
            vh.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            vh.text.setLineSpacing(mLineSpacingExtra, 1.0f);
            vh.text.setTextColor(mTextColor);
            vh.text.setPadding(mTextPadding, 0, 0, 0);
            vh.circle.setLayoutParams(new LinearLayout.LayoutParams((int) mBulletSize, (int) mBulletSize));
            vh.circle.setBackgroundColor(mTextColor);
        }


        private void measureViews(ViewHolder vh) {
            vh.text.measure(0, MeasureSpec.UNSPECIFIED);
            vh.circle.measure(MeasureSpec.makeMeasureSpec((int) mBulletSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) mBulletSize, MeasureSpec.EXACTLY));
        }


        private void setBulletTranslationY(ViewHolder vh) {
            vh.text.getLineBounds(0, mBounds);
            float y = (mBounds.height() / 2) - (vh.circle.getMeasuredHeight() / 2);
            vh.circle.setTranslationY(y);
        }
    }
}