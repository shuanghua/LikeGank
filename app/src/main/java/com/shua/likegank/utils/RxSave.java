/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 * Copyright (C) 2017 ShuangHua <moshuanghua17@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shua.likegank.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by drakeet on 8/17/15.
 * Modified by ShuangHua on 20/10/17.
 */
public class RxSave {
    public static Flowable<Uri> saveImageAndGetPathObservable(Context context, String url, String title) {
        return Flowable.create((FlowableOnSubscribe<Bitmap>) subscribe -> {
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(context)
                        .asBitmap()
                        .load(url)
                        .submit(Target.SIZE_ORIGINAL,
                                Target.SIZE_ORIGINAL)
                        .get();
            } catch (Exception e) {
                subscribe.onError(e);
            }
            if (bitmap == null)
                subscribe.onError(new Exception("图片获取失败！"));
            subscribe.onNext(bitmap);
            subscribe.onComplete();
        }, BackpressureStrategy.BUFFER)
                .flatMap(bitmap -> {
                    File appDir = new File(Environment.getExternalStorageDirectory(), "LikeGank");
                    if (!appDir.exists()) appDir.mkdir();
                    String fileName = title.replace('/', '-') + ".jpg";
                    File file = new File(appDir, fileName);
                    try {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        assert bitmap != null;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(context,
                                context.getApplicationContext().getPackageName() + ".provider", file);
                    } else {
                        uri = Uri.fromFile(file);
                    }
                    // 通知图库更新
                    Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                    context.sendBroadcast(scannerIntent);
                    return Flowable.just(uri);
                }).subscribeOn(Schedulers.io());
    }
}
