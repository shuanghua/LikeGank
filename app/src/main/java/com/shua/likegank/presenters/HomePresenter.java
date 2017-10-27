package com.shua.likegank.presenters;

import android.content.Context;
import android.widget.Toast;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.interfaces.HomeViewInterface;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

/**
 * ArticlePresenter
 * Created by moshu on 2017/5/13.
 */

public class HomePresenter extends NetWorkBasePresenter<HomeViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;
    private static int mPage = 1;
    private Realm mRealm;
    private List<Home> mList;
    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;

    public HomePresenter(HomeViewInterface viewInterface) {
        mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
        mList = new ArrayList<>();
    }

    public void requestData(int requestType) {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            switch (requestType) {
                case REQUEST_REFRESH:
                    deleteData();
                    mList.clear();
                    mPage = 1;
                    fromNetWorkLoad();
                    break;
                case REQUEST_LOAD_MORE:
                    mView.showLoading();
                    mPage++;
                    fromNetWorkLoad();
                    break;
                default:
                    break;
            }
        } else {
            Toast.makeText((Context) mView, R.string.error_net, Toast.LENGTH_SHORT).show();
            mView.hideLoading();
        }
    }

    private void fromNetWorkLoad() {
        mNetWorkDisposable = ApiFactory.getGankApi()
                .getHomeData(mPage)
                .filter(gankData -> !gankData.isError())
                .map(GankData::getResults)
                .flatMap(Flowable::fromIterable)
                .map(gankEntity -> new Home(gankEntity.get_id(), gankEntity.getDesc(),
                        gankEntity.getPublishedAt(), gankEntity.getType(),
                        gankEntity.getUrl(), gankEntity.getWho()))
                .buffer(60)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homes -> {
                    if (mPage == 1) {
                        mList.addAll(homes);
                        saveData(homes);
                    } else {
                        mList.addAll(homes);
                        mView.showData(mList);
                    }
                });
    }

    private void saveData(List<Home> data) {
        mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
    }

    private void deleteData() {
        mRealm.executeTransaction(realm -> realm.delete(Home.class));
    }

    public void fromRealmLoad() {
        mDisposable = mRealm.where(Home.class)
                .findAll()
                .asFlowable()
                .filter(homes -> homes.size() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homes -> {
                    System.out.println(homes.size());
                    mView.showData(homes);
                });
    }

    public void unSubscribe() {
        if (mDisposable != null) mDisposable.dispose();
        if (mNetWorkDisposable != null) mNetWorkDisposable.dispose();
        if (mRealm != null) mRealm.close();
    }
}
