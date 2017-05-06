package com.shua.likegank.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import java.text.SimpleDateFormat;

/**
 * Created by SHUA on 2017/4/29.
 */

public class LikeGankUtils {
    public static String timeString(String string) {
        String[] strings = string.split("T");
        return strings[0];
    }

    public static void copyToClipBoard(Context context, String text, String success) {
        ClipData clipData = ClipData.newPlainText("LIKEGANK", text);
        ClipboardManager manager = (ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(clipData);
        Toast.makeText(context, success, Toast.LENGTH_SHORT).show();
    }
}
