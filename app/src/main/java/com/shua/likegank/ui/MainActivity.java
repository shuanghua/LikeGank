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
import com.shua.likegank.interfaces.HomeViewInterface;
import com.shua.likegank.presenters.HomePresenter;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.itembinder.HomeItemBinder;

import java.util.List;

import butterknife.BindView;
import me.drakeet.multitype.MultiTypeAdapter;

public class MainActivity extends RefreshActivity implements
        NavigationView.OnNavigationItemSelectedListener, HomeViewInterface {

    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayoutManager;
    private MultiTypeAdapter mAdapter;
    private HomePresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavigationView();
        mPresenter.fromRealmLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    protected void initViews() {
        setTitle(R.string.bar_title_home);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Home.class, new HomeItemBinder());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(getOnBottomListener(mLinearLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
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

    private void initNavigationView() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
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
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
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
                if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
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
                int lastItemPosition, firstItemPosition, itemCount;
                itemCount = mAdapter.getItemCount();
                lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0) {
                    mPresenter.requestData(HomePresenter.REQUEST_LOAD_MORE);
                } else if (firstItemPosition == 0) {
                    isTransparent(true);
                } else {
                    isTransparent(false);
                }
            }
        };
    }
}
