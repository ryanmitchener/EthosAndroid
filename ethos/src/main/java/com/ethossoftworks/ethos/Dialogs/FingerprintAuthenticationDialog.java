package com.ethossoftworks.ethos.Dialogs;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ethossoftworks.ethos.R;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintAuthenticationDialog extends DialogFragment {
    public static final String TAG = "fingerprint_authentication_dialog";
    private static final long ERROR_DELAY_MILLIS = 1600;
    private static final long SUCCESS_DELAY_MILLIS = 1000;
    private static final int PERMISSIONS_REQUEST_USE_FINGERPRINT = 0xF3;

    private TextView mStatusText;
    private ImageView mIcon;

    private boolean mSelfCancelled = false;
    private CancellationSignal mCancellationSignal;
    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintAuthenticationDialogListener mListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);

        mStatusText = (TextView) v.findViewById(R.id.fingerprint_status);
        mIcon = (ImageView) v.findViewById(R.id.fingerprint_icon);

        v.findViewById(R.id.negative_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopListening();
                dismiss();
            }
        });
        return v;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        stopListening();
    }


    @Override
    public void onResume() {
        super.onResume();
        startListening();
    }


    @Override
    public void onPause() {
        super.onPause();
        stopListening();
    }


    private void showError(CharSequence error) {
        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mStatusText.setTextColor(ContextCompat.getColor(getActivity(), R.color.warning_color));
        mStatusText.setText(error);
        mStatusText.removeCallbacks(mResetStatusTextRunnable);
        mStatusText.postDelayed(mResetStatusTextRunnable, ERROR_DELAY_MILLIS);
    }


    private void showSuccess() {
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mStatusText.setTextColor(ContextCompat.getColor(getActivity(), R.color.success_color));
        mStatusText.setText(R.string.fingerprint_success);
        mStatusText.removeCallbacks(mResetStatusTextRunnable);
        mStatusText.postDelayed(mResetStatusTextRunnable, SUCCESS_DELAY_MILLIS);
    }


    private void startListening() {
        if (mCryptoObject == null) {
            throw new RuntimeException("You must call FingerprintAuthenticationDialog.initKeyStore() before show()");
        }

        mSelfCancelled = false;
        mCancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
            FingerprintManager fm = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            fm.authenticate(mCryptoObject, mCancellationSignal, 0, new FingerprintAuthenticationCallback(), null);
        } else {
            stopListening();
            requestPermissions(new String[]{Manifest.permission.USE_FINGERPRINT}, PERMISSIONS_REQUEST_USE_FINGERPRINT);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_USE_FINGERPRINT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                dismiss();
            }
        }
    }


    private void stopListening() {
        mSelfCancelled = true;
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }


    public void setFingerprintAuthenticationListener(FingerprintAuthenticationDialogListener listener) {
        mListener = listener;
    }


    public void initKeyStore(String keyStoreName) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(keyStoreName, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(keyStoreName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            mCryptoObject = new FingerprintManager.CryptoObject(cipher);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isFingerprintAuthAvailable(Context context) {
        FingerprintManager fm = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        return !(Build.VERSION.SDK_INT < 23 ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED ||
                !fm.isHardwareDetected());
    }


    public static void build(Activity activity, String keyStoreName, FingerprintAuthenticationDialogListener listener) {
        if (Build.VERSION.SDK_INT < 23) {
            Toast.makeText(activity, R.string.device_does_not_support_fingerprint, Toast.LENGTH_LONG).show();
            return;
        } else {
            FingerprintManager fm = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, R.string.enable_fingerprint_permission, Toast.LENGTH_LONG).show();
                return;
            } else if (!fm.isHardwareDetected()) {
                Toast.makeText(activity, R.string.device_does_not_support_fingerprint, Toast.LENGTH_LONG).show();
                return;
            } else if (!fm.hasEnrolledFingerprints()) {
                Toast.makeText(activity, R.string.register_fingerprint, Toast.LENGTH_LONG).show();
                return;
            }
        }

        FingerprintAuthenticationDialog fragment = new FingerprintAuthenticationDialog();
        fragment.initKeyStore(keyStoreName);
        fragment.setFingerprintAuthenticationListener(listener);
        fragment.show(activity.getFragmentManager(), FingerprintAuthenticationDialog.TAG);
    }



    /**
     * Error Runnable
     * ------------------------------------------------------------------------
     */

    private Runnable mResetStatusTextRunnable = new Runnable() {
        @Override
        public void run() {
            mStatusText.setTextColor(ContextCompat.getColor(getActivity(), R.color.hint_color));
            mStatusText.setText(getActivity().getResources().getString(R.string.fingerprint_hint));
            mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    };



    /**
     * Fingerprint Authentication Callback
     * ------------------------------------------------------------------------
     */

    private class FingerprintAuthenticationCallback extends FingerprintManager.AuthenticationCallback {
        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            showSuccess();
            mIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                    if (mListener != null) {
                        mListener.onAuthenticated();
                    }
                }
            }, SUCCESS_DELAY_MILLIS);
        }


        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            if (mSelfCancelled) {
                return;
            }

            showError(errString);
            mIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onError();
                    }
                }
            }, ERROR_DELAY_MILLIS);
        }


        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            showError(helpString);
        }


        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            showError(getActivity().getString(R.string.fingerprint_not_recognized));
        }
    }



    /**
     * Fingerprint Authentication Listener
     * ------------------------------------------------------------------------
     */
    public interface FingerprintAuthenticationDialogListener {
        public void onAuthenticated();

        public void onError();
    }
}