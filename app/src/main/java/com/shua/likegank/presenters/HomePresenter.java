package com.shua.likegank.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.LikeGankEntity;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.interfaces.RefreshViewInterface;
import com.shua.likegank.ui.base.BasePresenter;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * ArticlePresenter
 * Created by moshu on 2017/5/13.
 */

public class HomePresenter extends BasePresenter {

    public static final String KEY_HOME_PAGE = "home_page";

    public boolean isRefresh = true;
    public static int mPage = 1;

    private RefreshViewInterface mView;
    private Realm mRealm = Realm.getDefaultInstance();
    public List<Home> mHomes = new ArrayList<>();
    private Subscription mUnbscribeRealm;
    private Subscription mUnsubscribeRetrofit;

    public HomePresenter(RefreshViewInterface viewInterface) {
        this.mView = viewInterface;
    }

    private List<Home> conversionData(List<LikeGankEntity> list) {
        for (LikeGankEntity gankEntity : list) {
            mHomes.add(new Home(gankEntity.get_id(), gankEntity.getDesc(),
                    gankEntity.getPublishedAt(), gankEntity.getType(),
                    gankEntity.getUrl(), gankEntity.getWho()));
        }
        return mHomes;
    }

    private void clearData() {
        mRealm.executeTransaction(realm -> realm.delete(Home.class));
    }

    private void saveData(List<Home> data) {
        mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
    }

    /**
     * The call is automatically triggered when the local data is updated
     */
    public void fromRealmLoad() {
        mUnbscribeRealm = mRealm.where(Home.class)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .subscribe(homes -> {
                    if (homes.size() > 0) mView.showData(homes);
                });
    }

    @SuppressLint("WrongConstant")
    public void fromNetWorkLoad() {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            mUnsubscribeRetrofit = ApiFactory.getGankApi()
                    .getHomeData(mPage)
                    .filter(gankData -> !gankData.isError())
                    .map(GankData::getResults)
                    .single(likeGankEntities -> likeGankEntities.size() > 0)
                    .map(this::conversionData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(data -> {
                        if (isRefresh) clearData();
                        saveData(data);
                    }, throwable -> {
                        Toast.makeText((Context) mView
                                , throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        mView.hideLoading();
                    });
        } else {
            Toast.makeText((Context) mView, R.string.error_net, Toast.LENGTH_SHORT).show();
            mView.hideLoading();
        }
    }

    @Override
    protected void unSubscribe() {
        if (mUnbscribeRealm != null) mUnbscribeRealm.unsubscribe();
        if (mUnsubscribeRetrofit != null) mUnsubscribeRetrofit.unsubscribe();
    }

    @Override
    protected void savePage(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_HOME_PAGE, mPage);
        editor.apply();
    }
}
