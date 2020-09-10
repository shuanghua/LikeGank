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
import com.shua.likegank.databinding.FragmentGirlsBinding;
import com.shua.likegank.interfaces.ImageViewInterface;
import com.shua.likegank.presenters.ImagePresenter;
import com.shua.likegank.ui.base.RefreshFragment;
import com.shua.likegank.ui.itembinder.ImageItemBinder;

import java.util.List;

import me.drakeet.multitype.MultiTypeAdapter;

/**
 * NetWork to Realm to View
 * Created by SHUA on 2017/3/27.
 */
public class GirlsFragment extends RefreshFragment<FragmentGirlsBinding> implements ImageViewInterface {

    private final static int SPAN_COUNT = 3;
    private MultiTypeAdapter mAdapter;
    private ImagePresenter mPresenter;

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
        mPresenter = new ImagePresenter(this);
    }

    @Override
    protected void refresh() {
        mPresenter.requestData(ImagePresenter.REQUEST_REFRESH);
    }

    private void bottomListener(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0) {
            mPresenter.requestData(ImagePresenter.REQUEST_LOAD_MORE);
//        } else if (firstItemPosition == 0) {
//            isTransparent(true);
//        } else {
//            isTransparent(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideLoading();
    }

    @Override
    public void onStop() {
        super.onStop();
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
    protected FragmentGirlsBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentGirlsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    protected void initData() {
        initRecyclerView();
        showLoading();
        mPresenter.fromRealmLoad();
        refresh();
    }

    private void initRecyclerView() {
        final GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), SPAN_COUNT);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Content.class, new ImageItemBinder());
        RecyclerView recyclerView = binding.refreshListLayout.recyclerView;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ImageSpaceItemDecoration(12, SPAN_COUNT, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected SwipeRefreshLayout swipeRefreshView() {
        return binding.refreshListLayout.refreshView;
    }

    @Override
    public void showData(List<Content> result) {
        mAdapter.setItems(result);
        mAdapter.notifyDataSetChanged();
        hideLoading();
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
