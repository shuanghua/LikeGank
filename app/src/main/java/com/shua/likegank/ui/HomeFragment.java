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

import java.util.List;

import me.drakeet.multitype.MultiTypeAdapter;

public class HomeFragment extends
        RefreshFragment<FragmentHomeBinding> implements HomeViewInterface {

    private MultiTypeAdapter mAdapter;
    private HomePresenter mPresenter;

    @Override
    protected FragmentHomeBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        initRecyclerView();
        mPresenter.fromRealmLoad();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Home.class, new HomeItemBinder());
        RecyclerView recyclerView = binding.refreshListLayout.recyclerView;
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(getOnBottomListener(linearLayoutManager));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void refresh() {
        mPresenter.requestData(HomePresenter.REQUEST_REFRESH);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new HomePresenter(this);
    }

    @Override
    public void showData(List<Home> result) {
        mAdapter.setItems(result);
        mAdapter.notifyDataSetChanged();
        hideLoading();
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

    RecyclerView.OnScrollListener getOnBottomListener(LinearLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                int lastItemPosition, firstItemPosition, itemCount;
                itemCount = mAdapter.getItemCount();
                lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0) {
                    mPresenter.requestData(HomePresenter.REQUEST_LOAD_MORE);//到底部时，加载下一页数据
                } else {
                    //isTransparent(firstItemPosition == 0); // 到顶部
                }
            }
        };
    }
}