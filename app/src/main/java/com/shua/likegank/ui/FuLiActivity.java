package com.shua.likegank.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.LikeGankEntity;
import com.shua.likegank.data.entity.MeiZi;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.item_binder.FuLiItemBinder;
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

/**
 * NetWork to Realm to View
 * Created by SHUA on 2017/3/27.
 */
@SuppressLint("WrongConstant")
public class FuLiActivity extends RefreshActivity {

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    private int mPage = 1;
    private final static int SPAN_COUNT = 2;

    private Realm mRealm;
    private MultiTypeAdapter mAdapter;
    private Subscription subscribeNet;
    private Subscription subscribeRealm;

    private Items items = new Items();
    private List<MeiZi> meiZiList = new ArrayList<>();

    @Override
    protected int contentView() {
        return R.layout.activity_fuli;
    }

    @Override
    protected boolean addBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        setTitle(R.string.bar_title_fuli);
        initRecyclerView();
        loadData();
    }

    private void initRecyclerView() {
        final GridLayoutManager layoutManager = new GridLayoutManager(this, SPAN_COUNT);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(MeiZi.class, new FuLiItemBinder());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    private RecyclerView.OnScrollListener getOnBottomListener(GridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
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

    private void fromRealmLoad() {
        subscribeRealm = mRealm.where(MeiZi.class)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .subscribe(this::setDataToAdapter, (Throwable throwable) -> {
                    setRefreshStatus(false);
                    Logger.d(throwable.getMessage());
                });
    }

    private void fromNetWorkLoad() {
        subscribeNet = ApiFactory.getGankApi()
                .getFuLiData(mPage)
                .filter(gankData -> !gankData.isError())
                .map(GankData::getResults)
                .single(likeGankEntities -> likeGankEntities.size() > 0)
                .map(this::conversionData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(meiZis -> mRealm.executeTransaction(realm
                        -> realm.copyToRealmOrUpdate(meiZis)), throwable -> {
                    Logger.d(throwable);
                    setRefreshStatus(false);
                });
    }

    private List<MeiZi> conversionData(List<LikeGankEntity> list) {
        for (LikeGankEntity gankEntity : list) {
            meiZiList.add(new MeiZi(gankEntity.getUrl()));
        }
        return meiZiList;
    }

    private void setDataToAdapter(List<MeiZi> meiZis) {
        if (meiZis.size() > 0) {
            items.clear();
            meiZiList.clear();
            items.addAll(meiZis);
            mAdapter.setItems(items);
            mAdapter.notifyDataSetChanged();
        }
        setRefreshStatus(false);
    }

    private void deleteData() {
        mRealm.executeTransaction(realm
                -> realm.where(MeiZi.class)
                .findAll()
                .deleteAllFromRealm());
    }

    @Override
    public void topRefresh() {
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

    protected void bottomRefresh(GridLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1) {
            mPage += 1;
            if (NetWorkUtils.isNetworkConnected(FuLiActivity.this)) {
                setRefreshStatus(true);
                fromNetWorkLoad();
            } else {
                Toast.makeText(FuLiActivity.this,
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
        if (subscribeRealm != null) subscribeRealm.unsubscribe();
        if (subscribeNet != null) subscribeNet.unsubscribe();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRealm != null) mRealm.close();
    }
}
