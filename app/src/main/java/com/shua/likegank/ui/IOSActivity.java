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
import com.shua.likegank.data.entity.IOS;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.item_binder.CategoryItemBinder;
import com.shua.likegank.ui.item_binder.IOSItemBinder;
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

@SuppressLint("WrongConstant")
public class IOSActivity extends RefreshActivity {

    private int mPage = 1;
    private MultiTypeAdapter mAdapter;

    private Subscription subscribeNet;
    private Subscription subscribeRealm;
    private List<IOS> IOSs;
    private Realm mRealm;
    private boolean isRefresh;

    @BindView(R.id.list)
    RecyclerView mRecycler;

    @Override
    protected void topRefresh() {
        isRefresh = true;
        if (NetWorkUtils.isNetworkConnected(this)) {
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
        return R.layout.activity_ios;
    }

    @Override
    protected boolean addBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("IOS");
        mRealm = Realm.getDefaultInstance();
        IOSs = new ArrayList<>();
        initRecyclerView();
        loadData();
    }

    private void initRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(IOS.class, new IOSItemBinder());
        mAdapter.register(Category.class, new CategoryItemBinder());
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecycler.setAdapter(mAdapter);
    }

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
        subscribeRealm = mRealm.where(IOS.class)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .subscribe(IOSs -> {
                    if (IOSs.size() > 0) pareData(IOSs);
                }, throwable -> {
                    Logger.e(throwable.getMessage());
                    setRefreshStatus(false);
                });
    }

    private void fromNetWorkLoad() {
        subscribeNet = ApiFactory.getGankApi()
                .getiOSData(mPage)
                .filter(gankData -> !gankData.isError())
                .map(GankData::getResults)
                .single(likeGankEntities -> likeGankEntities.size() > 0)
                .map(this::conversionData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ios -> {
                    if (isRefresh) deleteData();
                    mRealm.executeTransaction(realm ->
                            realm.copyToRealmOrUpdate(ios));
                });
    }

    private void pareData(List<IOS> IOSs) {
        Items items = new Items();
        String time1 = LikeGankUtils.timeString(IOSs.get(0).time);
        String time2 = "";
        items.add(new Category(time1));
        for (int i = 0; i < IOSs.size(); i++) {
            items.add(IOSs.get(i));
            if (i < IOSs.size() - 1)
                time2 = LikeGankUtils.timeString(IOSs.get(i + 1).time);
            if (!time1.equals(time2)) {
                items.add(new Category(time2));
                time1 = time2;
            }
        }
        setData(items);
    }

    private void setData(Items items) {
        if (items.size() > 0) {
            IOSs.clear();
            mAdapter.setItems(items);
            mAdapter.notifyDataSetChanged();
        }
        setRefreshStatus(false);
        isRefresh = false;
    }

    private void deleteData() {
        mRealm.executeTransaction(realm
                -> realm.where(IOS.class)
                .findAll()
                .deleteAllFromRealm());
    }

    private List<IOS> conversionData(List<LikeGankEntity> list) {
        for (LikeGankEntity gankEntity : list) {
            IOSs.add(new IOS(
                    gankEntity.getPublishedAt(),
                    gankEntity.getDesc(),
                    gankEntity.getWho(),
                    gankEntity.getUrl()));
        }
        return IOSs;
    }

    protected void bottomRefresh(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1) {
            mPage += 1;
            if (NetWorkUtils.isNetworkConnected(IOSActivity.this)) {
                setRefreshStatus(true);
                fromNetWorkLoad();
            } else {
                Toast.makeText(IOSActivity.this,
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

    public static Intent newIntent(Context context) {
        return new Intent(context, IOSActivity.class);
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

