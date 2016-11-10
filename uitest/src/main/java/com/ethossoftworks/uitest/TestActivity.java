package com.ethossoftworks.uitest;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.ethossoftworks.ethos.EthosActivity;
import com.ethossoftworks.ethos.EthosActivity.Layout;
import com.ethossoftworks.ethos.Util.StateSaver.State;

@Layout(R.layout.test_activity)
public class TestActivity extends EthosActivity {
    @State private int test = 0;
    TextView textView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        textView = (TextView) findViewById(R.id.text);
        textView.setText("Testing: " + test++);
    }
}