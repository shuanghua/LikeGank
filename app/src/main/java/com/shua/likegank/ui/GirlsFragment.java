package com.shua.likegank.ui;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.shua.likegank.data.entity.Content;
import com.shua.likegank.data.entity.Girl;
import com.shua.likegank.databinding.FragmentGirlsBinding;
import com.shua.likegank.interfaces.ImageViewInterface;
import com.shua.likegank.presenters.GirlsPresenter;
import com.shua.likegank.ui.base.RefreshFragment;
import com.shua.likegank.ui.itembinder.GirlsItemBinder;
import com.shua.likegank.utils.AppUtils;

import java.util.List;

import me.drakeet.multitype.MultiTypeAdapter;

/**
 * NetWork to Realm to View
 * Created by SHUA on 2017/3/27.
 */
public class GirlsFragment
        extends RefreshFragment<FragmentGirlsBinding>
        implements ImageViewInterface {

    private final static int SPAN_COUNT = 3;
    private MultiTypeAdapter mAdapter;
    private GirlsPresenter mPresenter;
    private RecyclerView.OnScrollListener bottomListener;

    private RecyclerView.OnScrollListener getOnBottomListener(GridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                bottomListener(layoutManager);
            }
        };
    }

    @Override
    protected void initPresenter() {
        mPresenter = new GirlsPresenter(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//父类已经实例化 presenter
        mPresenter.requestNetWorkData(GirlsPresenter.REQUEST_REFRESH);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        mPresenter.subscribeDBData();
    }

    @Override
    public void onDestroyView() {
        binding.refreshListLayout.
                recyclerView.removeOnScrollListener(bottomListener);
        bottomListener = null;
        super.onDestroyView();
    }

    /**
     * 必须清理 Presenter 和 RxJava 否则会造成内存泄露
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.unSubscribe();
        mPresenter = null;
        binding = null;
    }

    private void initRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), SPAN_COUNT);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Girl.class, new GirlsItemBinder());
        RecyclerView recyclerView = binding.refreshListLayout.recyclerView;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ImageSpaceItemDecoration(12, SPAN_COUNT, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        bottomListener = getOnBottomListener(layoutManager);
        recyclerView.addOnScrollListener(bottomListener);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mPresenter.requestNetWorkData(GirlsPresenter.REQUEST_REFRESH);
    }

    @Override
    public void showData(List<Girl> result) {
        mAdapter.setItems(result);
        mAdapter.notifyDataSetChanged();
        hideRefreshView();
        hideLoadingView();
    }

    @Override
    public void onError(String errorInfo) {
        hideRefreshView();
        hideLoadingView();
        AppUtils.toast(errorInfo);
    }

    private void bottomListener(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0) {
            showLoadingView();
            mPresenter.requestNetWorkData(GirlsPresenter.REQUEST_LOAD_MORE);
        }
    }

    @Override
    protected SwipeRefreshLayout swipeRefreshView() {
        return binding.refreshListLayout.refreshView;
    }

    @Override
    protected FragmentGirlsBinding viewBinding(LayoutInflater inflater
            , ViewGroup container) {
        return FragmentGirlsBinding.inflate(inflater, container, false);
    }

    static class ImageSpaceItemDecoration extends RecyclerView.ItemDecoration {
        int space;
        int count;
        boolean isEdge;

        ImageSpaceItemDecoration(int space, int count, boolean isEdge) {
            this.space = space;
            this.count = count;
            this.isEdge = isEdge;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view
                , RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % count; // item column

            if (isEdge) {
                outRect.left = space - column * space / count;
                outRect.right = (column + 1) * space / count;
                if (position < count) { // top edge
                    outRect.top = space;
                }
                outRect.bottom = space; // item bottom
            } else {
                outRect.left = column * space / count;
                outRect.right = space - (column + 1) * space / count;
                if (position >= count) {
                    outRect.top = space;
                }
            }
        }
    }
}
