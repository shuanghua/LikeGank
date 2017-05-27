package com.shua.likegank.interfaces;

import java.util.List;

/**
 * RefreshInterface
 * Created by moshu on 2017/5/17.
 */

public interface RefreshViewInterface<T>  {
    void showLoading();

    void hideLoading();

    void showData(List<T> data);

}
