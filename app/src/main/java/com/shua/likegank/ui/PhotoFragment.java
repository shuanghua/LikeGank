package com.shua.likegank.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shua.likegank.R;
import com.shua.likegank.databinding.FragmentPhotoBinding;
import com.shua.likegank.ui.base.BaseFragment;
import com.shua.likegank.utils.RxSave;
import com.shua.likegank.utils.Shares;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static android.os.Environment.DIRECTORY_DCIM;


/**
 * Display image
 * Created by ShuangHua on 2017/4/23.
 */

public class PhotoFragment extends BaseFragment<FragmentPhotoBinding> {

    private PhotoView mPhotoView;
    private CompositeDisposable mDisposable;
    private PhotoViewAttacher mAttacher;
    private RxPermissions rxPermissions;
    private String imageUrl;

    @Override
    protected FragmentPhotoBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentPhotoBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            imageUrl = PhotoFragmentArgs.fromBundle(arguments).getUrlGirl();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDisposable = new CompositeDisposable();
        mPhotoView = binding.photoView;
        mAttacher = new PhotoViewAttacher(mPhotoView);
        mPhotoView.setImageResource(R.mipmap.likegank_launcher_round);
        rxPermissions = new RxPermissions(requireActivity());
        assert getArguments() != null;
        disPlayImage(imageUrl);
        setPhotoListener();
    }

    @Override
    public void onDestroyView() {
        mAttacher.setOnDoubleTapListener(null);
        mAttacher.setOnLongClickListener(null);
        mPhotoView = null;
        mAttacher = null;
        super.onDestroyView();
    }

    @Override
    protected void initPresenter() {
    }

    @SuppressLint("TimberExceptionLogging")
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
            Timber.e(vr.getMessage());
        }
    }

    private void setPhotoListener() {
        mAttacher.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                requireActivity().onBackPressed();
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
            new AlertDialog.Builder(requireActivity())
                    .setItems(R.array.photo_dialog_list, (dialog, position) -> {
                        if (position == 0) {
                            share();
                        } else if (position == 1) {
                            save();//检查权限，然后保存
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
                    Object model, Target<Drawable> target, boolean isFirstResource) {
                Toast.makeText(requireActivity(), "图片加载出错！", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onResourceReady(
                    Drawable resource,
                    Object model,
                    Target<Drawable> target,
                    DataSource dataSource,
                    boolean isFirstResource
            ) {
                return false;
            }
        }).into(mPhotoView);
    }

    private void saveImage() {
        mDisposable.add(RxSave.saveImageAndGetPathObservable(
                requireActivity(), imageUrl, imageUrl)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> {
                    File appDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);
                    String msg = String.format(getString(R.string.picture_has_save_to),
                            appDir.getAbsolutePath());
                    Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show();
                }));
    }

    private void save() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {//Android Q + 后不需要读写权限
            mDisposable.add(rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted) {
                            saveImage();
                        } else {
                            Toast.makeText(requireActivity()
                                    , "权限已被拒绝！", Toast.LENGTH_SHORT).show();
                        }
                    }));
        } else { //Android Q+
            saveImage();
        }
    }

    @SuppressLint("CheckResult")
    private void share() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mDisposable.add(rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted) {
                            shareImage();
                        } else {
                            Toast.makeText(requireActivity(),
                                    "权限已被拒绝！", Toast.LENGTH_SHORT).show();
                        }
                    })
            );
        } else {
            shareImage();
        }
    }

    @SuppressLint("TimberExceptionLogging")
    private void shareImage() {
        mDisposable.add(RxSave.saveImageAndGetPathObservable(requireActivity(), imageUrl, imageUrl)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> Shares.shareImage(
                        requireActivity(), uri, getString(R.string.share_to)),
                        throwable -> {
                            Timber.e(throwable.getMessage());
                            Toast.makeText(requireActivity(),
                                    throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) mDisposable.dispose();
    }
}
