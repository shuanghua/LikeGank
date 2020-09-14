package com.shua.likegank.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.shua.likegank.R;

/**
 * RefreshBaseActivity
 * Created by shuanghua on 2017/3/16.
 */
public abstract class RefreshFragment<T extends ViewBinding> extends BaseFragment<T> {

    protected abstract void refresh();

    protected abstract SwipeRefreshLayout swipeRefreshView();

    public SwipeRefreshLayout refreshView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.refreshView = swipeRefreshView();
        if (refreshView == null) {
            throw new IllegalStateException("No found SwipeRefreshView in Layout");
        }
        refreshView.setOnRefreshListener(this::refresh);
        refreshView.setColorSchemeColors(getResources().getColor(R.color.colorApp));
    }

    @Override
    public void onDestroyView() {
        refreshView.setOnRefreshListener(null);
        refreshView = null;
        super.onDestroyView();
    }

    public void setRefreshStatus(boolean isRefresh) {
        if (refreshView == null) {
            return;
        }
        if (!isRefresh) {
            refreshView.postDelayed(() -> {
                if (refreshView != null) {
                    refreshView.setRefreshing(false);
                }
            }, 400);
        } else {
            refreshView.setRefreshing(true);
        }
    }
}
