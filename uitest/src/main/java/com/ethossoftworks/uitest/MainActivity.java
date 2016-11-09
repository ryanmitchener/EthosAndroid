package com.ethossoftworks.uitest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ethossoftworks.ethos.Dialogs.FingerprintAuthenticationDialog;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.bullet_list_button).setOnClickListener(new ButtonClick(BulletListActivity.class));
        findViewById(R.id.fingerprint_authentication_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (FingerprintAuthenticationDialog.isFingerprintAuthAvailable(MainActivity.this)) {
                    FingerprintAuthenticationDialog.build(MainActivity.this, "test", new FingerprintAuthenticationDialog.FingerprintAuthenticationDialogListener() {
                        @Override
                        public void onAuthenticated() {
                            Toast.makeText(MainActivity.this, "Authenticated!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        findViewById(R.id.test_activity_button).setOnClickListener(new ButtonClick(TestActivity.class));
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