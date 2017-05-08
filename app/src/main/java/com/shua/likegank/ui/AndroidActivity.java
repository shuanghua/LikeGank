package com.shua.likegank.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.LikeGankEntity;
import com.shua.likegank.data.entity.Android;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.item_binder.CategoryItemBinder;
import com.shua.likegank.ui.item_binder.AndroidItemBinder;
import com.shua.likegank.utils.LikeGankUtils;
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

public class AndroidActivity extends RefreshActivity {

    private int mPage = 1;
    private MultiTypeAdapter mAdapter;

    @BindView(R.id.list)
    RecyclerView mRecycler;

    private Subscription subscribeNet;
    private Subscription subscribeRealm;
    private List<Android> androids;
    private Realm mRealm;

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

    @Override
    protected int contentView() {
        return R.layout.activity_android;
    }

    @Override
    protected boolean addBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Android");
        mRealm = Realm.getDefaultInstance();
        androids = new ArrayList<>();
        initRecyclerView();
        loadData();
    }

    private void initRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Android.class, new AndroidItemBinder());
        mAdapter.register(Category.class, new CategoryItemBinder());
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecycler.setAdapter(mAdapter);
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

    private void fromRealmLoad() {
        // When the local data is updated, the call is automatically triggered
        subscribeRealm = mRealm.where(Android.class)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .subscribe(androids -> {
                    if (androids.size() > 0) pareData(androids);
                }, throwable -> {
                    Logger.e(throwable.getMessage());
                    setRefreshStatus(false);
                });
    }

    private void fromNetWorkLoad() {
        subscribeNet = ApiFactory.getGankApi()
                .getAndroidData(mPage)
                .filter(gankData -> !gankData.isError())
                .map(GankData::getResults)
                .single(likeGankEntities -> likeGankEntities.size() > 0)
                .map(this::conversionData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(androidList -> mRealm.executeTransaction(realm
                        -> realm.copyToRealmOrUpdate(androidList)));
    }

    private void pareData(List<Android> androids) {
        Items items = new Items();
        String time1 = LikeGankUtils.timeString(androids.get(0).time);
        String time2 = "";
        items.add(new Category(time1));
        for (int i = 0; i < androids.size(); i++) {
            items.add(androids.get(i));
            if (i < androids.size() - 1)
                time2 = LikeGankUtils.timeString(androids.get(i + 1).time);
            if (!time1.equals(time2)) {
                items.add(new Category(time2));
                time1 = time2;
            }
        }
        setData(items);
    }

    private void setData(Items items) {
        if (items.size() > 0) {
            androids.clear();
            mAdapter.setItems(items);
            mAdapter.notifyDataSetChanged();
        }
        setRefreshStatus(false);
    }

    private void deleteData() {
        mRealm.executeTransaction(realm
                -> realm.where(Android.class)
                .findAll()
                .deleteAllFromRealm());
    }

    private List<Android> conversionData(List<LikeGankEntity> list) {
        for (LikeGankEntity gankEntity : list) {
            androids.add(new Android(
                    gankEntity.getPublishedAt(),
                    gankEntity.getDesc(),
                    gankEntity.getWho(),
                    gankEntity.getUrl()));
        }
        return androids;
    }

    @SuppressLint("WrongConstant")
    protected void bottomRefresh(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1) {
            mPage += 1;
            if (NetWorkUtils.isNetworkConnected(AndroidActivity.this)) {
                setRefreshStatus(true);
                fromNetWorkLoad();
            } else {
                Toast.makeText(AndroidActivity.this,
                        R.string.error_net, Toast.LENGTH_SHORT).show();
                setRefreshStatus(false);
            }
        }else if (firstItemPosition == 0) {
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

    public static Intent newIntent(Context context) {
        return new Intent(context, AndroidActivity.class);
    }

    RecyclerView.OnScrollListener getOnBottomListener(LinearLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                bottomRefresh(layoutManager);
            }
        };
    }
}
