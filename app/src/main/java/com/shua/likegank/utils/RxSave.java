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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import static android.os.Environment.DIRECTORY_DCIM;

/**
 * 私密： getExternalFilesDir()
 * Created by drakeet on 8/17/15.
 * Modified by ShuangHua on 20/10/17.
 */
public class RxSave {
    public static Flowable<Uri> saveImageAndGetPathObservable(Activity context, String url, String title) {
        return Flowable.create((FlowableOnSubscribe<Bitmap>) subscribe -> {
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(context)
                        .asBitmap()
                        .load(url)
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (Exception e) {
                subscribe.onError(e);
            }
            if (bitmap == null) {
                subscribe.onError(new Exception("图片获取失败！"));
            }
            assert bitmap != null;
            subscribe.onNext(bitmap);
            subscribe.onComplete();
        }, BackpressureStrategy.BUFFER)
                .flatMap(bitmap -> {
                    final Uri uri; //此处的 Uri 仅用于把图片分享到其他应用
                    final String packageName = context.getApplicationContext().getPackageName();
                    final String imageName = title.replace('/', '-');
                    final OutputStream outputStream;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //这个会自动刷新相册，这种方式称为 MediaStore 方式
                        ContentResolver resolver = context.getContentResolver();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "LikeGank");
                        uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        assert uri != null;
                        outputStream = resolver.openOutputStream(uri);
                        try {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            assert outputStream != null;
                            outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //File imageDir = context.getExternalFilesDir(DIRECTORY_PICTURES);
                        File imageDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM) + File.separator + "LikeGank");
                        if (!imageDir.exists()) {
                            final boolean b = imageDir.mkdir();
                            System.out.println("RxSaveP---" + b + "-----:" + imageDir.getAbsolutePath());
                            if (!b) {
                                AppUtils.toast("文件夹创建失败~");
                            }
                        }

                        //生成路径
                        File imagePathFile = File.createTempFile(imageName, ".jpg", imageDir);

                        //写入
                        outputStream = new FileOutputStream(imagePathFile);
                        try {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // 通知相册
                        RxSave.notifyGallery(context, imagePathFile.getAbsolutePath());

                        //生成共享 Uri
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(context, packageName + ".fileprovider", imagePathFile);  //和清单文件保持一致
                        } else {
                            uri = Uri.fromFile(new File(imagePathFile.getAbsolutePath()));
                        }
                    }

                    return Flowable.just(uri);
                }).subscribeOn(Schedulers.io());
    }

    public static void notifyGallery(Context context, Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static void notifyGallery(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(file.getAbsolutePath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * @param path 完整路径（包含图片的格式）
     */
    public static void notifyGallery(Context context, String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
