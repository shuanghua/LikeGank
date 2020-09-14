package com.shua.likegank.presenters;

import com.shua.likegank.interfaces.BaseViewInterface;

abstract class NetWorkBasePresenter<T extends BaseViewInterface<?>> {

    T mFragment;

    public abstract void requestNetWorkData(int requestType);

    //can post network error
}
