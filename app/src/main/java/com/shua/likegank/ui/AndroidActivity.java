package com.shua.likegank.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shua.likegank.R;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.entity.Android;
import com.shua.likegank.interfaces.RefreshViewInterface;
import com.shua.likegank.presenters.AndroidPresenter;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.itembinder.AndroidItemBinder;
import com.shua.likegank.ui.itembinder.CategoryItemBinder;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;

import butterknife.BindView;
import me.drakeet.multitype.MultiTypeAdapter;

public class AndroidActivity extends RefreshActivity<RefreshViewInterface, AndroidPresenter>
        implements RefreshViewInterface {

    @BindView(R.id.list)
    RecyclerView mRecycler;

    private MultiTypeAdapter mAdapter;
    private AndroidPresenter mPresenter;

    protected void initViews() {
        setTitle("Android");
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Category.class, new CategoryItemBinder());
        mAdapter.register(Android.class, new AndroidItemBinder());
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecycler.setAdapter(mAdapter);
        showLoading();
        mPresenter.fromRealmLoad();
        refresh();
    }

    @Override
    protected AndroidPresenter createPresenter() {
        mPresenter = new AndroidPresenter(this);
        mPresenter.fromNetWorkLoad();
        return mPresenter;
    }

    @Override
    public void showData(List data) {
        mPresenter.isRefresh = false;
        mAdapter.setItems(data);
        mAdapter.notifyDataSetChanged();
        hideLoading();
    }

    @Override
    protected void refresh() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            mPresenter.isRefresh = true;
            AndroidPresenter.mPage = 1;
            mPresenter.fromNetWorkLoad();
        } else {
            showToast(getString(R.string.error_net));
            hideLoading();
            if (mPreferences != null) {
                int page = mPreferences.getInt(AndroidPresenter.KEY_ANDROID_PAGE, 1);
                if (page > AndroidPresenter.mPage) AndroidPresenter.mPage = page;
            }
        }
    }

    private void bottomListener(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0) {
            if (NetWorkUtils.isNetworkConnected(this)) {
                showLoading();
                AndroidPresenter.mPage++;
                mPresenter.fromNetWorkLoad();
            } else {
                showToast(getString(R.string.error_net));
                hideLoading();
            }
        } else if (firstItemPosition == 0) {
            setToolbarElevation(0);
        } else {
            setToolbarElevation(6);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE, AndroidPresenter.mPage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            AndroidPresenter.mPage = savedInstanceState.getInt(PAGE);
        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AndroidActivity.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showLoading() {
        setRefreshStatus(true);
    }

    @Override
    public void hideLoading() {
        setRefreshStatus(false);
    }

    @Override
    protected boolean addBack() {
        return true;
    }

    @Override
    protected int contentView() {
        return R.layout.activity_android;
    }

    RecyclerView.OnScrollListener getOnBottomListener(LinearLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                bottomListener(layoutManager);
            }
        };
    }
}
