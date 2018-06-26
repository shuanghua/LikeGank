package com.shua.likegank.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orhanobut.logger.Logger;
import com.shua.likegank.R;
import com.shua.likegank.utils.RxSave;
import com.shua.likegank.utils.Shares;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * Display image
 * Created by ShuangHua on 2017/4/23.
 */

public class PhotoViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL_MEIZI = "MEIZI_URL";

    @BindView(R.id.photo_view)
    PhotoView mPhotoView;
    private CompositeDisposable mDisposable;
    private String url;
    private RxPermissions rxPermissions;
    private PhotoViewAttacher mAttacher;

    public static Intent newIntent(Context context, String url) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(EXTRA_URL_MEIZI, url);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        mDisposable = new CompositeDisposable();

        mPhotoView = findViewById(R.id.photo_view);
        mAttacher = new PhotoViewAttacher(mPhotoView);
        mPhotoView.setImageResource(R.mipmap.ic_launcher);
        rxPermissions = new RxPermissions(this);
        url = getIntent().getStringExtra(EXTRA_URL_MEIZI);

        disPlayImage(url);
        setPhotoListener();
    }

    private void setPhotoViewScaleScheme(MotionEvent e) {
        try {
            float scale = mAttacher.getScale();
            float x = e.getX();
            float y = e.getY();

            if (scale < mAttacher.getMediumScale()) {
                mAttacher.setScale(
                        mAttacher.getMediumScale(), x, y, true);
            } else {
                mAttacher.setScale(1.0f, x, y, true);
            }
        } catch (ArrayIndexOutOfBoundsException vr) {
            Logger.e(vr.getMessage());
        }
    }

    private void setPhotoListener() {
        mAttacher.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                onBackPressed();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                setPhotoViewScaleScheme(e);
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }
        });

        mAttacher.setOnLongClickListener(v -> {
            new AlertDialog.Builder(PhotoViewActivity.this)
                    .setItems(R.array.photo_dialog_list, (dialog, position) -> {
                        if (position == 0) {
                            shareImage();
                        } else if (position == 1) {
                            saveImage();
                        }
                    }).create().show();
            return true;
        });
    }

    private void disPlayImage(String url) {
        Glide.with(this).load(url).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(
                    @Nullable GlideException e,
                    Object model,
                    Target<Drawable> target,
                    boolean isFirstResource) {
                Toast.makeText(PhotoViewActivity.this, "图片加载出错！",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onResourceReady(
                    Drawable resource,
                    Object model,
                    Target<Drawable> target,
                    DataSource dataSource,
                    boolean isFirstResource) {
                return false;
            }
        }).into(mPhotoView);
    }

    private void saveImage() {
        mDisposable.add(rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        mDisposable.add(RxSave.saveImageAndGetPathObservable(
                                PhotoViewActivity.this, url, url)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(uri -> {
                                    File appDir = new File(Environment.getExternalStorageDirectory(), "LikeGank");
                                    String msg = String.format(getString(
                                            R.string.picture_has_save_to), appDir.getAbsolutePath());
                                    Toast.makeText(PhotoViewActivity.this
                                            , msg, Toast.LENGTH_SHORT).show();
                                }));
                    } else {
                        Toast.makeText(PhotoViewActivity.this
                                , "权限已被拒绝！", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void shareImage() {
        mDisposable.add(rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        RxSave.saveImageAndGetPathObservable(this, url, url)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(uri -> Shares.shareImage(
                                        PhotoViewActivity.this,
                                        uri,
                                        getString(R.string.share_to)),
                                        throwable -> {
                                            Logger.e(throwable.getMessage());
                                            Toast.makeText(PhotoViewActivity.this,
                                                    throwable.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                    } else {
                        Toast.makeText(PhotoViewActivity.this,
                                "权限已被拒绝！", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) mDisposable.dispose();
    }
}
