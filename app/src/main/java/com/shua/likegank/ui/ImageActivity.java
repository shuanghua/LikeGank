package com.shua.likegank.ui;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.shua.likegank.R;
import com.shua.likegank.data.entity.FuLi;
import com.shua.likegank.interfaces.RefreshViewInterface;
import com.shua.likegank.presenters.ImagePresenter;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.itembinder.ImageItemBinder;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;

import butterknife.BindView;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * NetWork to Realm to View
 * Created by SHUA on 2017/3/27.
 */
public class ImageActivity extends RefreshActivity<RefreshViewInterface, ImagePresenter>
        implements RefreshViewInterface<FuLi> {

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    private final static int SPAN_COUNT = 3;

    private MultiTypeAdapter mAdapter;
    private ImagePresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.bar_title_fuli);
        initViews();
        showLoading();
        mPresenter.fromRealmLoad();
        topRefresh();
    }

    private void initViews() {
        final GridLayoutManager layoutManager =
                new GridLayoutManager(this, SPAN_COUNT);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(FuLi.class, new ImageItemBinder());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new ImageSpacItemDecoration
                (12, SPAN_COUNT, true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void showData(List data) {
        mPresenter.isRefresh = false;
        mPresenter.mMeiZis.clear();
        mAdapter.setItems(data);
        mAdapter.notifyDataSetChanged();
        hideLoading();
    }

    @Override
    protected ImagePresenter createPresenter() {
        mPresenter = new ImagePresenter(this);
        return mPresenter;
    }

    @Override
    protected void topRefresh() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            mPresenter.isRefresh = true;
            ImagePresenter.mPage = 1;
            mPresenter.fromNetWorkLoad();
        } else {
            showToast(getString(R.string.error_net));
            hideLoading();
            if (mPreferences != null) {
                int page = mPreferences.getInt(ImagePresenter.KEY_IMAGE_PAGE, 1);
                if (page > ImagePresenter.mPage) ImagePresenter.mPage = page;
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
                ImagePresenter.mPage++;
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
        outState.putInt(PAGE, ImagePresenter.mPage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            ImagePresenter.mPage = savedInstanceState.getInt(PAGE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
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
        return R.layout.activity_fuli;
    }

    private RecyclerView.OnScrollListener getOnBottomListener(GridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                bottomListener(layoutManager);
            }
        };
    }

    class ImageSpacItemDecoration extends RecyclerView.ItemDecoration {
        int spac;
        int count;
        boolean isEdge;

        ImageSpacItemDecoration(int spac, int count, boolean isEdge) {
            this.spac = spac;
            this.count = count;
            this.isEdge = isEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view
                , RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % count; // item column

            if (isEdge) {
                outRect.left = spac - column * spac / count;
                outRect.right = (column + 1) * spac / count;
                if (position < count) { // top edge
                    outRect.top = spac;
                }
                outRect.bottom = spac; // item bottom
            } else {
                outRect.left = column * spac / count;
                outRect.right = spac - (column + 1) * spac / count;
                if (position >= count) {
                    outRect.top = spac;
                }
            }
        }
    }
}
