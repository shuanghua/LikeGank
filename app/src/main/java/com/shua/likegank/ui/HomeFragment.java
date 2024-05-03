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

import com.shua.likegank.data.entity.Home;
import com.shua.likegank.databinding.FragmentHomeBinding;
import com.shua.likegank.interfaces.HomeViewInterface;
import com.shua.likegank.presenters.HomePresenter;
import com.shua.likegank.ui.base.RefreshFragment;
import com.shua.likegank.ui.itembinder.HomeItemBinder;
import com.shua.likegank.utils.AppUtils;

import java.util.List;

import me.drakeet.multitype.MultiTypeAdapter;


public class HomeFragment extends
        RefreshFragment<FragmentHomeBinding> implements HomeViewInterface {

    private HomePresenter mPresenter; // 持有 Fragment,因此不能让其存活的时间比 Fragment 久
    private MultiTypeAdapter mAdapter;
    private RecyclerView.OnScrollListener bottomListener;

    @Override
    protected FragmentHomeBinding viewBinding(LayoutInflater inflater
            , ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.requestNetWorkData(HomePresenter.REQUEST_REFRESH);
    }

    @Override
    public void onViewCreated(@NonNull View view
            , @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        mPresenter.subscribeDBData();//单一数据源：数据库
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        binding.refreshListLayout.recyclerView
                .removeOnScrollListener(bottomListener);
        bottomListener = null;
        mAdapter = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.unSubscribe();
        mPresenter = null;
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(requireActivity());
        bottomListener = getOnBottomListener(layoutManager);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Home.class, new HomeItemBinder());

        RecyclerView recyclerView = binding.refreshListLayout.recyclerView;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(bottomListener);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mPresenter.requestNetWorkData(HomePresenter.REQUEST_REFRESH);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new HomePresenter(this);
    }

    @Override
    public void showData(List<Home> result) {
        if (result != null) {
            mAdapter.setItems(result);
            mAdapter.notifyDataSetChanged();
        }
        hideRefreshView();
        hideLoadingView();
    }

    @Override
    public void onError(String errorInfo) {
        AppUtils.toast(errorInfo);
        hideRefreshView();
        hideLoadingView();
    }

    @Override
    protected SwipeRefreshLayout swipeRefreshView() {
        return binding.refreshListLayout.refreshView;
    }

    private RecyclerView.OnScrollListener getOnBottomListener(
            LinearLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                int lastItemPosition, firstItemPosition, itemCount;
                itemCount = mAdapter.getItemCount();
                lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0) {
                    showLoadingView();
                    mPresenter.requestNetWorkData(HomePresenter.REQUEST_LOAD_MORE);//到底部时，加载下一页数据
                }
//                else if (firstItemPosition == 0) {
//                    isTransparent(true);
//                } else {
//                    isTransparent(false);
//                }
            }
        };
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_gank_link) {
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_VIEW);
//            Uri uri = Uri.parse(getString(R.string.gank_link));
//            intent.setData(uri);
//            if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}