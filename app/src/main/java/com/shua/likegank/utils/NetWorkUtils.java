package com.shua.likegank.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetWorkUtils {
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            @SuppressLint("WrongConstant") ConnectivityManager manager
                    = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null) return networkInfo.isAvailable();
        }
        return false;
    }
}
