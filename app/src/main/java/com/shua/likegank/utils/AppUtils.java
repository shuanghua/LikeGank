package com.shua.likegank.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

/**
 * LikeGankUtils
 * Created by SHUA on 2017/4/29.
 */

public class AppUtils {

    private static Context mAppContext;
    private static Toast toast = null;

    public static void setAppContext(Context context) {
        mAppContext = context;
    }

    public static String timeString(String string) {
        String[] strings = string.split("T");
        return strings[0];
    }

    @SuppressLint("WrongConstant")
    public static void copyToClipBoard(Context context, String text) {
        ClipData clipData = ClipData.newPlainText("LIKEGANK", text);
        ClipboardManager manager = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) manager.setPrimaryClip(clipData);
        Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("ShowToast")
    public static void toast(int msg) {
        if (toast == null) {
            toast = Toast.makeText(mAppContext, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    @SuppressLint("ShowToast")
    public static void toast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(mAppContext, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}
