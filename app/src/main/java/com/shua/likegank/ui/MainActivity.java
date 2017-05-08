package com.shua.likegank.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.LikeGankEntity;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.item_binder.HomeItemBinder;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.realm.Realm;
import io.realm.RealmResults;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends RefreshActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int mPage = 1;

    private Realm mRealm;
    private MultiTypeAdapter mAdapter;
    private Items items = new Items();
    private Subscription subscribeNet;
    private Subscription subscribeRealm;
    private List<Home> homeList;
    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    @Override
    protected int contentView() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean addBack() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        homeList = new ArrayList<>();
        setTitle(R.string.bar_title_home);
        initRecyclerView();
        initNavigationView();
        loadData();
    }

    private void initRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Home.class, new HomeItemBinder());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    RecyclerView.OnScrollListener getOnBottomListener(LinearLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {

                bottomRefresh(layoutManager);
            }
        };
    }

    @SuppressLint("WrongConstant")
    private void loadData() {
        setRefreshStatus(true);
        if (NetWorkUtils.isNetworkConnected(this) &&
                mRealm != null) {
            fromNetWorkLoad();
        } else {
            Toast.makeText(this,
                    R.string.error_net, Toast.LENGTH_SHORT).show();
            setRefreshStatus(false);
        }
    }

    /**
     * When the local data is updated,
     * the call is automatically triggered
     */
    private void fromRealmLoad() {
        subscribeRealm = mRealm.where(Home.class)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .subscribe(this::setData, throwable -> {
                    setRefreshStatus(false);
                    Logger.d(throwable.getMessage());
                });
    }

    private void fromNetWorkLoad() {
        subscribeNet = ApiFactory.getGankApi()
                .getHomeData(mPage)
                .filter(gankData -> !gankData.isError())
                .map(GankData::getResults)
                .single(likeGankEntities -> likeGankEntities.size() > 0)
                .map(this::conversionData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homeList -> mRealm.executeTransaction(realm
                        -> realm.copyToRealmOrUpdate(homeList)), throwable -> {
                    Logger.d(throwable);
                    setRefreshStatus(false);
                });
    }

    private List<Home> conversionData(List<LikeGankEntity> list) {
        for (LikeGankEntity gankEntity : list) {
            homeList.add(new Home(
                    gankEntity.get_id(),
                    gankEntity.getDesc(),
                    gankEntity.getCreatedAt(),
                    gankEntity.getType(),
                    gankEntity.getUrl(),
                    gankEntity.getWho()));
        }
        return homeList;
    }

    private void setData(List<Home> meiZis) {
        if (meiZis.size() > 0) {
            items.clear();
            homeList.clear();
            items.addAll(meiZis);
            mAdapter.setItems(items);
            mAdapter.notifyDataSetChanged();
        }
        setRefreshStatus(false);
    }

    private void deleteData() {
        mRealm.executeTransaction(realm
                -> realm.where(Home.class)
                .findAll()
                .deleteAllFromRealm());
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void topRefresh() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            deleteData();
            mPage = 1;
            fromNetWorkLoad();
        } else {
            Toast.makeText(this,
                    R.string.error_net, Toast.LENGTH_SHORT).show();
            setRefreshStatus(false);
        }
    }

    @SuppressLint("WrongConstant")
    protected void bottomRefresh(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1) {
            mPage += 1;
            if (NetWorkUtils.isNetworkConnected(MainActivity.this)) {
                setRefreshStatus(true);
                fromNetWorkLoad();
            } else {
                Toast.makeText(MainActivity.this,
                        R.string.error_net, Toast.LENGTH_SHORT).show();
                setRefreshStatus(false);
            }
        } else if (firstItemPosition == 0) {
            setToolbarElevation(0);
        } else {
            setToolbarElevation(8);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fromRealmLoad();
    }

    @Override
    protected void onPause() {
        setRefreshStatus(false);
        if (null != subscribeNet) subscribeNet.unsubscribe();
        if (null != subscribeRealm) subscribeRealm.unsubscribe();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (null != mRealm) mRealm.close();
        super.onDestroy();
    }

    private void initNavigationView() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer, mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setItemIconTintList(null);
        mNavigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.prompt);
            builder.setMessage(R.string.prompt_information);
            builder.setCancelable(true);
            builder.setPositiveButton("确定",
                    (dialog, which) -> finish());
            builder.setNegativeButton("取消",
                    (dialog, which) -> dialog.dismiss());
            builder.create().show();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
        } else if (id == R.id.nav_android) {
            startActivity(AndroidActivity.newIntent(this));
        } else if (id == R.id.nav_ios) {
            startActivity(IOSActivity.newIntent(this));
        } else if (id == R.id.nav_surprise) {
            Intent intent = new Intent(this, FuLiActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            startActivity(AboutActivity.newIntent(this));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
