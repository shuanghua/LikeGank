package com.shua.likegank.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.shua.likegank.data.entity.Android;
import com.shua.likegank.data.uimodel.Category;
import com.shua.likegank.databinding.FragmentAndroidBinding;
import com.shua.likegank.interfaces.AndroidViewInterface;
import com.shua.likegank.presenters.AndroidPresenter;
import com.shua.likegank.ui.base.RefreshFragment;
import com.shua.likegank.ui.itembinder.AndroidItemBinder;
import com.shua.likegank.ui.itembinder.CategoryItemBinder;
import com.shua.likegank.utils.AppUtils;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class AndroidFragment
        extends RefreshFragment<FragmentAndroidBinding>
        implements AndroidViewInterface {

    private MultiTypeAdapter mAdapter;
    private AndroidPresenter mPresenter;
    private RecyclerView.OnScrollListener bottomListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.requestNetWorkData(AndroidPresenter.REQUEST_REFRESH);
    }

    @Override
    public void onViewCreated(@NonNull View view
            , @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        mPresenter.subscribeDBData();
    }

    @Override
    public void onDestroyView() {
        binding.refreshListLayout.recyclerView.removeOnScrollListener(bottomListener);
        bottomListener = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.unSubscribe();
        mPresenter = null;
        super.onDestroy();
    }

    private void initRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Category.class, new CategoryItemBinder());
        mAdapter.register(Android.class, new AndroidItemBinder());
        RecyclerView recyclerView = binding.refreshListLayout.recyclerView;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        bottomListener = getOnBottomListener(layoutManager);
        recyclerView.addOnScrollListener(bottomListener);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new AndroidPresenter(this);
    }

    @Override
    public void showData(Items result) {
        if (result != null) {
            mAdapter.setItems(result);
            mAdapter.notifyDataSetChanged();
        }
        hideRefreshView();
        hideLoadingView();
    }


    @Override
    public void onError(String errorInfo) {
        hideRefreshView();
        hideLoadingView();
        AppUtils.toast(errorInfo);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mPresenter.requestNetWorkData(AndroidPresenter.REQUEST_REFRESH);
    }

    private void getBottomListener(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0
                && lastItemPosition != 0) {
            showLoadingView();
            mPresenter.requestNetWorkData(AndroidPresenter.REQUEST_LOAD_MORE);
        }
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
                getBottomListener(layoutManager);
            }
        };
    }
}
