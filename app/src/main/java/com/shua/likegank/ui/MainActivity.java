package com.shua.likegank.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.shua.likegank.R;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.interfaces.RefreshViewInterface;
import com.shua.likegank.presenters.HomePresenter;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.itembinder.HomeItemBinder;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;

import butterknife.BindView;
import me.drakeet.multitype.MultiTypeAdapter;

public class MainActivity extends RefreshActivity<RefreshViewInterface, HomePresenter>
        implements NavigationView.OnNavigationItemSelectedListener, RefreshViewInterface {

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    private MultiTypeAdapter mAdapter;
    private HomePresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavigationView();
    }

    @Override
    protected void initViews() {
        setTitle(R.string.bar_title_home);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Home.class, new HomeItemBinder());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecyclerView.setAdapter(mAdapter);
        showLoading();
        mPresenter.fromRealmLoad();
        refresh();
    }

    @Override
    public void showData(List data) {
        mPresenter.isRefresh = false;
        mAdapter.setItems(data);
        mAdapter.notifyDataSetChanged();
        hideLoading();
    }

    @Override
    protected void refresh() {

        if (NetWorkUtils.isNetworkConnected(this)) {
            mPresenter.isRefresh = true;
            HomePresenter.mPage = 1;
            mPresenter.fromNetWorkLoad();
        } else {
            showToast(getString(R.string.error_net));
            hideLoading();
            if (mPreferences != null) {
                int page = mPreferences.getInt(HomePresenter.KEY_HOME_PAGE, 1);
                if (page > HomePresenter.mPage) HomePresenter.mPage = page;
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
                HomePresenter.mPage++;
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
        outState.putInt(PAGE, HomePresenter.mPage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            HomePresenter.mPage = savedInstanceState.getInt(PAGE);
    }

    private void initNavigationView() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer, mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setItemIconTintList(null);
        mNavigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_gank_link:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(getString(R.string.gank_link));
                intent.setData(uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                break;
            case R.id.nav_android:
                startActivity(AndroidActivity.newIntent(this));
                break;
            case R.id.nav_ios:
                startActivity(IOSActivity.newIntent(this));
                break;
            case R.id.nav_surprise:
                Intent intent = new Intent(this, ImageActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_about:
                startActivity(AboutActivity.newIntent(this));
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected HomePresenter createPresenter() {
        mPresenter = new HomePresenter(this);
        return mPresenter;
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    protected int contentView() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean addBack() {
        return false;
    }

    RecyclerView.OnScrollListener getOnBottomListener(LinearLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                bottomListener(layoutManager);
            }
        };
    }
}
