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

import com.shua.likegank.data.entity.IOS;
import com.shua.likegank.data.uimodel.Category;
import com.shua.likegank.databinding.FragmentIosBinding;
import com.shua.likegank.interfaces.IOSViewInterface;
import com.shua.likegank.presenters.IOSPresenter;
import com.shua.likegank.ui.base.RefreshFragment;
import com.shua.likegank.ui.itembinder.CategoryItemBinder;
import com.shua.likegank.ui.itembinder.IOSItemBinder;
import com.shua.likegank.utils.AppUtils;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class IOSFragment extends RefreshFragment<FragmentIosBinding> implements IOSViewInterface {

    private MultiTypeAdapter mAdapter;
    private IOSPresenter mPresenter;
    private RecyclerView.OnScrollListener bottomListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.requestNetWorkData(IOSPresenter.REQUEST_REFRESH);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        setRefreshStatus(true);
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
        super.onDestroy();
        mPresenter.unSubscribe();
        mPresenter = null;
    }

    protected void initRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Category.class, new CategoryItemBinder());
        mAdapter.register(IOS.class, new IOSItemBinder());
        RecyclerView recyclerView = binding.refreshListLayout.recyclerView;
        recyclerView.setLayoutManager(layoutManager);
        bottomListener = getOnBottomListener(layoutManager);
        recyclerView.addOnScrollListener(bottomListener);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected FragmentIosBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentIosBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new IOSPresenter(this);
    }

    @Override
    public void showData(Items result) {
        mAdapter.setItems(result);
        mAdapter.notifyDataSetChanged();
        setRefreshStatus(false);
    }

    @Override
    public void onError(String errorInfo) {
        setRefreshStatus(false);
        AppUtils.toast(errorInfo);
    }

    @Override
    protected void refresh() {
        mPresenter.requestNetWorkData(IOSPresenter.REQUEST_REFRESH);
    }

    private void bottomListener(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0) {
            setRefreshStatus(true);
            mPresenter.requestNetWorkData(IOSPresenter.REQUEST_LOAD_MORE);
        }
    }

    @Override
    protected SwipeRefreshLayout swipeRefreshView() {
        return binding.refreshListLayout.refreshView;
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
