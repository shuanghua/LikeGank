package com.shua.likegank.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.shua.likegank.R;

public class NetWorkUtils {
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            @SuppressLint("WrongConstant") ConnectivityManager manager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert manager != null;
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null) return networkInfo.isAvailable();
        }
        return false;
    }
}
