package com.ethossoftworks.uitest;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.ethossoftworks.ethos.EthosActivity;
import com.ethossoftworks.ethos.EthosActivity.Layout;
import com.ethossoftworks.annotations.SaveState;

import butterknife.BindView;

@Layout(R.layout.test_activity)
public class TestActivity extends EthosActivity {
    @BindView(R.id.text) TextView textView;
    @SaveState private int testVar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView.setText("Hello 2");

        if (savedInstanceState == null) {
            testVar = 1;
        }
    }
}