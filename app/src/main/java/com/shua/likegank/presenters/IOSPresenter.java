package com.shua.likegank.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.LikeGankEntity;
import com.shua.likegank.data.entity.IOS;
import com.shua.likegank.interfaces.RefreshViewInterface;
import com.shua.likegank.ui.base.BasePresenter;
import com.shua.likegank.utils.LikeGankUtils;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import me.drakeet.multitype.Items;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * ArticlePresenter
 * Created by moshu on 2017/5/13.
 */

public class IOSPresenter extends BasePresenter {

    public static final String KEY_IOS_PAGE = "iOS_page";

    public boolean isRefresh = true;
    public static int mPage = 1;

    private RefreshViewInterface mView;
    private Realm mRealm = Realm.getDefaultInstance();
    public List<IOS> mIOSs = new ArrayList<>();
    private Subscription mUnbscribeRealm;
    private Subscription mUnsubscribeRetrofit;

    public IOSPresenter(RefreshViewInterface viewInterface) {
        this.mView = viewInterface;
    }

    private List<IOS> conversionData(List<LikeGankEntity> list) {
        for (LikeGankEntity gankEntity : list) {
            mIOSs.add(new IOS(
                    gankEntity.getPublishedAt(),
                    gankEntity.getDesc(),
                    gankEntity.getWho(),
                    gankEntity.getUrl()));
        }
        return mIOSs;
    }

    private void pareData(List<IOS> IOSs) {
        Items items = new Items();
        String time1 = LikeGankUtils.timeString(IOSs.get(0).time);
        String time2 = "";
        items.add(new Category(time1));
        for (int i = 0; i < IOSs.size(); i++) {
            items.add(IOSs.get(i));
            if (i < IOSs.size() - 1) time2 = LikeGankUtils.timeString(IOSs.get(i + 1).time);
            if (!time1.equals(time2)) {
                items.add(new Category(time2));
                time1 = time2;
            }
        }
        mView.showData(items);
    }

    /**
     * The call is automatically triggered when the local data is updated
     */
    public void fromRealmLoad() {
        mUnbscribeRealm = mRealm.where(IOS.class)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .subscribe(IOSs -> {
                    if (IOSs.size() > 0) pareData(IOSs);
                }, throwable -> {
                    Logger.e(throwable.getMessage());
                    mView.hideLoading();
                });
    }

    @SuppressLint("WrongConstant")
    public void fromNetWorkLoad() {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            mUnsubscribeRetrofit = ApiFactory.getGankApi()
                    .getiOSData(mPage)
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

    private void clearData() {
        mRealm.executeTransaction(realm -> realm.delete(IOS.class));
    }

    private void saveData(List<IOS> data) {
        mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
    }

    @Override
    protected void unSubscribe() {
        if (mUnbscribeRealm != null) mUnbscribeRealm.unsubscribe();
        if (mUnsubscribeRetrofit != null) mUnsubscribeRetrofit.unsubscribe();
    }

    @Override
    protected void savePage(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_IOS_PAGE, mPage);
        editor.apply();
    }
}
