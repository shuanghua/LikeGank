package com.shua.likegank.ui;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shua.likegank.R;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.entity.Android;
import com.shua.likegank.interfaces.AndroidViewInterface;
import com.shua.likegank.presenters.AndroidPresenter;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.itembinder.AndroidItemBinder;
import com.shua.likegank.ui.itembinder.CategoryItemBinder;

import butterknife.BindView;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class AndroidActivity extends RefreshActivity implements AndroidViewInterface {

    @BindView(R.id.list)
    RecyclerView mRecycler;

    private MultiTypeAdapter mAdapter;
    private AndroidPresenter mPresenter;

    public static Intent newIntent(Context context) {
        return new Intent(context, AndroidActivity.class);
    }

    protected void initViews() {
        //Log.d("AndroidActivity--->>", "" + mPresenter.mPage);
        setTitle("Android");
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Category.class, new CategoryItemBinder());
        mAdapter.register(Android.class, new AndroidItemBinder());
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.setHasFixedSize(true);
        mRecycler.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecycler.setAdapter(mAdapter);
        showLoading();
        mPresenter.fromRealmLoad();
        refresh();
    }

    @Override
    protected void initPresenter() {
        mPresenter = new AndroidPresenter(this);
    }

    @Override
    public void showData(Items result) {
        hideLoading();
        mAdapter.setItems(result);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void refresh() {
        mPresenter.requestData(AndroidPresenter.REQUEST_REFRESH);
    }

    private void bottomListener(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0 && lastItemPosition != 0) {
            mPresenter.requestData(AndroidPresenter.REQUEST_LOAD_MORE);
        }
    }

    RecyclerView.OnScrollListener getOnBottomListener(LinearLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                bottomListener(layoutManager);
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.unSubscribe();
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
}
