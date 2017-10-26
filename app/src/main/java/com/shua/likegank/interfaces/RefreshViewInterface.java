package com.shua.likegank.interfaces;

public interface RefreshViewInterface<T> extends BaseViewInterface<T> {
    void showLoading();

    void hideLoading();

    //void showData(List data);
}
