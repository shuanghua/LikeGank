package com.shua.likegank.ui.base;

import android.content.SharedPreferences;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * NetBaseInterface
 * Created by moshu on 2017/5/17.
 */

public abstract class BasePresenter<T> {

    private Reference<T> mViewRef;


    void attachView(T view) {
        mViewRef = new WeakReference<T>(view);
    }

    protected T getView() {
        return mViewRef.get();
    }

    private boolean isViewAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }

    void detachView() {
        if (isViewAttached()) {
            mViewRef.clear();
            mViewRef = null;
        }
    }

    protected abstract void unSubscribe();
    protected abstract void savePage(SharedPreferences sp);
}
