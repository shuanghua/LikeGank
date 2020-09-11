package com.shua.likegank.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.shua.likegank.data.uimodel.Category;
import com.shua.likegank.data.entity.Android;
import com.shua.likegank.databinding.FragmentAndroidBinding;
import com.shua.likegank.interfaces.AndroidViewInterface;
import com.shua.likegank.presenters.AndroidPresenter;
import com.shua.likegank.ui.base.RefreshFragment;
import com.shua.likegank.ui.itembinder.AndroidItemBinder;
import com.shua.likegank.ui.itembinder.CategoryItemBinder;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class AndroidFragment extends RefreshFragment<FragmentAndroidBinding> implements AndroidViewInterface {

    private MultiTypeAdapter mAdapter;
    private AndroidPresenter mPresenter;

    public static Intent newIntent(Context context) {
        return new Intent(context, AndroidFragment.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
    }

    private void initRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Category.class, new CategoryItemBinder());
        mAdapter.register(Android.class, new AndroidItemBinder());
        RecyclerView recyclerView = binding.refreshListLayout.recyclerView;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        recyclerView.setAdapter(mAdapter);
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
        } else {
            //isTransparent(firstItemPosition == 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideLoading();
    }

    @Override
    public void onDestroy() {
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
    protected SwipeRefreshLayout swipeRefreshView() {

        return binding.refreshListLayout.refreshView;
    }

    @Override
    protected FragmentAndroidBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAndroidBinding.inflate(inflater, container, false);
    }

    RecyclerView.OnScrollListener getOnBottomListener(LinearLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                bottomListener(layoutManager);
            }
        };
    }
}
