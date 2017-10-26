package com.shua.likegank.ui.base;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * NetBaseInterface
 * Created by moshu on 2017/5/17.
 */

public abstract class BasePresenter<V> {

    private Reference<V> mViewRef;

    protected V getView() {
        return mViewRef.get();
    }

    private boolean isViewAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }

    void attachView(V view) {
        mViewRef = new WeakReference<>(view);
    }

    void detachView() {
        if (isViewAttached()) {
            mViewRef.clear();
            mViewRef = null;
        }
    }
}
