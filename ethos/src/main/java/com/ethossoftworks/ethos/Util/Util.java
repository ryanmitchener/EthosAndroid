package com.ethossoftworks.ethos.Util;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;

public class Util {
    public static boolean checkNetworkConnection(Context context) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().smallestScreenWidthDp >= 600);
    }


    public static float dpToPx(int dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }


    public static void setRotationPortraitIfPhone(Activity activity) {
        if (!isTablet(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}