package com.shua.likegank.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;

import com.shua.likegank.R;

import butterknife.BindView;

/**
 * RefreshBaseActivity
 * Created by SHUA on 2017/3/16.
 */

public abstract class RefreshActivity extends ToolbarActivity {

    protected final static String PAGE = "PAGE";
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    protected abstract void refresh();

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initRefresh();
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorApp));
    }

    public void initRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(this::refresh);
        }
    }

    public void setRefreshStatus(boolean isRefresh) {
        if (mRefreshLayout == null) {
            return;
        }
        if (!isRefresh) {
            mRefreshLayout.postDelayed(() ->
                    mRefreshLayout.setRefreshing(false), 300);
        } else {
            mRefreshLayout.setRefreshing(true);
        }
    }
}
