package com.shua.likegank.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.shua.likegank.R;

import java.text.SimpleDateFormat;

/**
 * LikeGankUtils
 * Created by SHUA on 2017/4/29.
 */

public class LikeGankUtils {
    private static boolean mDebugLog = true;

    public static String timeString(String string) {
        String[] strings = string.split("T");
        return strings[0];
    }

    @SuppressLint("WrongConstant")
    public static void copyToClipBoard(Context context, String text, String success) {
        ClipData clipData = ClipData.newPlainText("LIKEGANK", text);
        @SuppressLint("WrongConstant") ClipboardManager manager
                = (ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(clipData);
        Toast.makeText(context, success, Toast.LENGTH_SHORT).show();
    }

    public static void debugLog(String log) {
        if (mDebugLog) {
            Logger.d(log);
        }
    }
}
