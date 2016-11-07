package com.ethossoftworks.uitest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.bullet_list_button).setOnClickListener(new ButtonClick(BulletListActivity.class));
    }


    private class ButtonClick implements View.OnClickListener {
        private Class mActivity;

        public ButtonClick(Class activity) {
            mActivity = activity;
        }

        @Override
        public void onClick(View v) {
            startActivity(new Intent(getBaseContext(), mActivity));
        }
    }
}