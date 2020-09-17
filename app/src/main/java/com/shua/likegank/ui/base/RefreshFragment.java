package com.shua.likegank.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.shua.likegank.R;
import com.shua.likegank.ui.MainActivity;

import timber.log.Timber;

/**
 * RefreshBaseActivity
 * Created by shuanghua on 2017/3/16.
 */
public abstract class RefreshFragment<T extends ViewBinding>
        extends BaseFragment<T>
        implements SwipeRefreshLayout.OnRefreshListener {

    private boolean isRefreshShowing;
    private OnLoadingVisibilityListener loadingListener;

    public SwipeRefreshLayout refreshView;

    protected abstract SwipeRefreshLayout swipeRefreshView();

    @Override
    public void onRefresh() {
        isRefreshShowing = true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (loadingListener == null) {
            loadingListener = ((MainActivity) requireActivity());
            loadingListener.showLoadingView(true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view
            , @Nullable Bundle savedInstanceState) {
        this.refreshView = swipeRefreshView();
        if (refreshView == null) {
            throw new IllegalStateException("No found SwipeRefreshView in Layout");
        }
        refreshView.setOnRefreshListener(this);
        refreshView.setColorSchemeColors(
                getResources().getColor(R.color.colorApp));
    }

    @Override
    public void onDestroyView() {
        refreshView.setOnRefreshListener(null);
        refreshView = null;
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideRefreshView();
        hideRefreshView();
    }

    public void showLoadingView() {
        if (loadingListener != null) {
            loadingListener.showLoadingView(true);
        }
    }

    public void hideLoadingView() {
        if (loadingListener != null) {
            loadingListener.showLoadingView(false);
        }
    }

    public void hideRefreshView() {
        if (refreshView == null || !isRefreshShowing) {
            return;
        }
        refreshView.postDelayed(() -> {
            if (refreshView != null) {
                isRefreshShowing = false;
                refreshView.setRefreshing(false);
            }
        }, 400);
    }

    public void showRefreshView() {
        isRefreshShowing = true;
        refreshView.setRefreshing(true);
    }
}
