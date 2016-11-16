package com.ethossoftworks.uitest;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.ethossoftworks.ethos.EthosActivity;
import com.ethossoftworks.ethos.EthosActivity.Layout;

import butterknife.BindView;

@Layout(R.layout.test_activity)
public class TestActivity extends EthosActivity {
    @BindView(R.id.text) TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView.setText("Hello");
    }
}