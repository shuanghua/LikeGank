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
 * Created by SHUA on 2017/3/16.
 */

public abstract class RefreshFragment<T extends ViewBinding> extends BaseFragment<T> {

    protected abstract void refresh();
    protected abstract SwipeRefreshLayout swipeRefreshView();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (swipeRefreshView() != null) {
            swipeRefreshView().setOnRefreshListener(this::refresh);
            swipeRefreshView().setColorSchemeColors(getResources().getColor(R.color.colorApp));
        }
    }

    public void setRefreshStatus(boolean isRefresh) {
        if (swipeRefreshView() == null) {
            return;
        }
        if (!isRefresh) {
            swipeRefreshView().postDelayed(() ->
                    swipeRefreshView().setRefreshing(false), 300);
        } else {
            swipeRefreshView().setRefreshing(true);
        }
    }
}
