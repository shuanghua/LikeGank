package com.shua.likegank.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.interfaces.RefreshViewInterface;
import com.shua.likegank.ui.base.BasePresenter;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * ArticlePresenter
 * Created by moshu on 2017/5/13.
 */

public class HomePresenter extends BasePresenter {

    public static int mPage = 1;
    public static final String KEY_HOME_PAGE = "HOME_PAGE";

    public boolean isRefresh = true;

    private RefreshViewInterface mView;
    private Realm mRealm;
    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;

    public HomePresenter(RefreshViewInterface viewInterface) {
        this.mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
    }

    private void deleteData() {
        mRealm.executeTransaction(realm -> realm.delete(Home.class));
    }


    private void addData(List<Home> data) {
        if (isRefresh) deleteData();
        mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
    }

    /**
     * The call is automatically triggered when the local data is updated
     */

    public void fromRealmLoad() {
        mDisposable = mRealm.where(Home.class)
                .findAll()
                .asFlowable()
                .filter(RealmResults::isLoaded)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homes -> mView.showData(homes));
    }

    @SuppressLint("WrongConstant")
    public void fromNetWorkLoad() {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            mNetWorkDisposable = ApiFactory.getGankApi()
                    .getHomeData(mPage)
                    .filter(gankData -> !gankData.isError())
                    .map(GankData::getResults)
                    .flatMap(Flowable::fromIterable)
                    .map(gankEntity -> new Home(gankEntity.get_id(), gankEntity.getDesc(),
                            gankEntity.getPublishedAt(), gankEntity.getType(),
                            gankEntity.getUrl(), gankEntity.getWho()))
                    .buffer(30)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::addData);

        } else {
            Toast.makeText((Context) mView, R.string.error_net, Toast.LENGTH_SHORT).show();
            mView.hideLoading();
        }
    }

    @Override
    protected void unSubscribe() {
        if (mDisposable != null) mDisposable.dispose();
        if (mNetWorkDisposable != null) mNetWorkDisposable.dispose();
    }

    @Override
    protected void savePage(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_HOME_PAGE, mPage);
        editor.apply();
    }
}
