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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * 私密： getExternalFilesDir() 可把图片分享给别的应用，但相册识别不了这个目录里面的图片
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
                    final String imageName = title.replace("http://gank.io/images/", "");//去掉特殊字符，避免在某些系统上出错
                    System.out.println("imageName:" + imageName);
                    final Uri imageUri = saveImage(context, imageName, ".jpg", "LikeGank", "image/jpeg", bitmap);
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {// Android Q 之前需要手挡通知相册更新
                        notifyGallery(context, imageUri);
                    }
                    return Flowable.just(imageUri);
                }).subscribeOn(Schedulers.io());
    }

    /**
     * 保存到 Pictures 目录下 (已做 Android Q + 适配)
     *
     * @param context   activity
     * @param imageName 图片的名字
     * @param suffix    图片的格式：jpg 或者 png
     * @param directory 图片的目录（只需要层一目录，一般写自己的应用名字）
     * @param mimeType  传 image/jpeg 或 mime/png
     * @param bitmap    bitmap 资源
     * @return 保存成功后的图片路径 File
     */
    public static Uri saveImage(Activity context, String imageName,
                                String suffix, String directory,
                                String mimeType, Bitmap bitmap) throws IOException {

        final String imageDir = DIRECTORY_PICTURES + File.separator + directory;
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);

        //MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, imageDir);
        } else {
            File dirFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES), directory);
            if (!dirFile.exists()) dirFile.mkdirs();// 必须先创建第一层目录
            final File imageFile = File.createTempFile(imageName, suffix, dirFile);//然后创建全目录
            //File imageFile = new File(Environment.getExternalStoragePublicDirectory())
            values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
        }

        //这个Uri可直接用于分享(不用使用 fileProvider)
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        assert uri != null;
        OutputStream outputStream = resolver.openOutputStream(uri);//获取写出流
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);//写入
        assert outputStream != null;
        outputStream.flush();
        outputStream.close();
        return uri;
    }

    public void getImage(Activity activity, String imageName) throws IOException {
        Uri uri = null;
        String[] projection = {// 需要的信息，例如图片的所在数据库表对应的 ID，图片在数据库中的名字
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
        };
        String selection = MediaStore.Images.Media.DISPLAY_NAME + " == ?";
        String[] selectionArgs = new String[]{imageName};
        Context context = activity.getApplicationContext();
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);

        //resolver.loadThumbnail()

        try (Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            assert cursor != null;
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            }
        }
        if (uri != null) {
            InputStream inputStream = resolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            //return bitmap;
        }
    }

    /**
     * 如果是 Android Q 之前需要请求读取外部存储权限
     */
    public Bitmap readImage(Activity context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
        return pfd != null ? BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor()) : null;
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

    public static void notifyGallery(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(file.getAbsolutePath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static void notifyGallery(Context context, Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * 生成分享 Uri
     * 不要使用此 Uri 来通知相册
     */
    public static Uri createFileProviderUri(Activity context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0
            uri = FileProvider.getUriForFile(context, // 注意： 此 Uri 不能用于通知相册更新，仅用于分享
                    context.getApplicationContext().getPackageName() + ".fileprovider",
                    file); //.fileprovider 必须和清单文件保持一致
        } else {
            uri = Uri.fromFile(file);//此 Uri 可以用于通知相册更新
        }
        return uri;
    }
}
