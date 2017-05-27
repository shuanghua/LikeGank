package com.shua.likegank.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shua.likegank.R;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.entity.IOS;
import com.shua.likegank.interfaces.RefreshViewInterface;
import com.shua.likegank.presenters.IOSPresenter;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.itembinder.CategoryItemBinder;
import com.shua.likegank.ui.itembinder.IOSItemBinder;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;

import butterknife.BindView;
import me.drakeet.multitype.MultiTypeAdapter;

public class IOSActivity extends RefreshActivity<RefreshViewInterface, IOSPresenter> implements RefreshViewInterface<IOS> {

    @BindView(R.id.list)
    RecyclerView mRecycler;

    private MultiTypeAdapter mAdapter;
    private IOSPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("IOS");
        initViews();
        showLoading();
        mPresenter.fromRealmLoad();
        topRefresh();
    }

    private void initViews() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Category.class, new CategoryItemBinder());
        mAdapter.register(IOS.class, new IOSItemBinder());
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    protected IOSPresenter createPresenter() {
        mPresenter = new IOSPresenter(this);
        return mPresenter;
    }

    @Override
    public void showData(List<IOS> data) {
        mPresenter.isRefresh = false;
        mPresenter.mIOSs.clear();
        mAdapter.setItems(data);
        mAdapter.notifyDataSetChanged();
        hideLoading();
    }

    @Override
    protected void topRefresh() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            mPresenter.isRefresh = true;
            IOSPresenter.mPage = 1;
            mPresenter.fromNetWorkLoad();
        } else {
            showToast(getString(R.string.error_net));
            hideLoading();
            if (mPreferences != null) {
                int page = mPreferences.getInt(IOSPresenter.KEY_IOS_PAGE, 1);
                if (page > IOSPresenter.mPage) IOSPresenter.mPage = page;
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
                IOSPresenter.mPage++;
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
        outState.putInt(PAGE, IOSPresenter.mPage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            IOSPresenter.mPage = savedInstanceState.getInt(PAGE);
        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, IOSActivity.class);
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
        return R.layout.activity_ios;
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
