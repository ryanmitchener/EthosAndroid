package com.ethossoftworks.ethos.Views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ethossoftworks.ethos.R;

public class BulletListView extends ListView {
    private LayoutInflater mInflater;

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
        populateAttributes(attrs);
        setInternalAdapter();
    }


    private void populateAttributes(AttributeSet attrs) {

    }


    private void setInternalAdapter() {
        setAdapter(new BulletListAdapter());
    }



    public class BulletListAdapter extends BaseAdapter {
        private class ViewHolder {
            private CircleView circle;
            private TextView text;
        }


        @Override
        public Object getItem(int position) {
            return null;
        }


        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public int getCount() {
            return 0;
        }


        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder vh = new ViewHolder();
            if (view == null) {
                view = mInflater.inflate(R.layout.bullet_list_view, parent, false);
                vh.circle = (CircleView) view.findViewById(R.id.circle);
                vh.text = (TextView) view.findViewById(R.id.text);
            }
            return view;
        }
    }
}
