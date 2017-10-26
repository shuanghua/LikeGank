package com.shua.likegank.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shua.likegank.R;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.interfaces.LicenseViewInterface;
import com.shua.likegank.presenters.LicensePresenter;
import com.shua.likegank.ui.base.ToolbarActivity;
import com.shua.likegank.ui.itembinder.CategoryItemBinder;
import com.shua.likegank.ui.itembinder.ContentItemBinder;

import butterknife.BindView;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class LicenseActivity extends ToolbarActivity implements LicenseViewInterface {
    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    private LicensePresenter mPresenter;
    private MultiTypeAdapter mAdapter;

    public static Intent newIntent(Context context) {
        return new Intent(context, LicenseActivity.class);
    }

    @Override
    protected int contentView() {
        return R.layout.activity_lincense;
    }

    @Override
    protected boolean addBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.open_license));
        initViews();
        mPresenter.loadData();
    }

    protected void initViews() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRefreshLayout.setEnabled(false);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Category.class, new CategoryItemBinder());
        mAdapter.register(Content.class, new ContentItemBinder());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new LicensePresenter(this);
    }

    @Override
    public void showData(Items result) {
        mAdapter.setItems(result);
    }
}
