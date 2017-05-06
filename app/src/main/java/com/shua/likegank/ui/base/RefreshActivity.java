package com.shua.likegank.ui.base;

import android.annotation.SuppressLint;
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

    protected abstract void topRefresh();

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initRefresh();
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorApp));
    }

    public void initRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            topRefresh();
                        }
                    });
        }
    }

    public void setRefreshStatus(boolean isRefresh) {
        if (mRefreshLayout == null) {
            return;
        }
        if (!isRefresh) {
            mRefreshLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRefreshLayout.setRefreshing(false);
                }
            }, 300);
        } else {
            mRefreshLayout.setRefreshing(true);
        }
    }
}
