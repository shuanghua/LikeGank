package com.shua.likegank.presenters;

import com.shua.likegank.interfaces.RefreshViewInterface;

abstract class NetWorkBasePresenter<T extends RefreshViewInterface<?>> {

    T mView;

    //can post network error
}
