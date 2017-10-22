package com.shua.likegank.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.entity.IOS;
import com.shua.likegank.interfaces.RefreshViewInterface;
import com.shua.likegank.ui.base.BasePresenter;
import com.shua.likegank.utils.LikeGankUtils;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import me.drakeet.multitype.Items;

/**
 * ArticlePresenter
 * Created by moshu on 2017/5/13.
 */

public class IOSPresenter extends BasePresenter {

    public static int mPage = 1;
    public static final String KEY_IOS_PAGE = "IOS_PAGE";

    public boolean isRefresh = true;

    private RefreshViewInterface mView;
    private Realm mRealm;

    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;

    public IOSPresenter(RefreshViewInterface viewInterface) {
        this.mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
    }

    private void pareData(List<IOS> iosRealmResults) {
        Items items = new Items();
        String time1 = LikeGankUtils.timeString(iosRealmResults.get(0).time);
        String time2 = "";
        items.add(new Category(time1));
        for (int i = 0; i < iosRealmResults.size(); i++) {
            items.add(iosRealmResults.get(i));
            if (i < iosRealmResults.size() - 1)
                time2 = LikeGankUtils.timeString(iosRealmResults.get(i + 1).time);
            if (!time1.equals(time2)) {
                items.add(new Category(time2));
                time1 = time2;
            }
        }
        mView.showData(items);
    }

    public void fromRealmLoad() {
        mDisposable = mRealm.where(IOS.class)
                .findAll()
                .asFlowable()
                .filter(RealmResults::isLoaded)
                .subscribe(iosRealmResults -> {
                    if (iosRealmResults.size() > 0) pareData(iosRealmResults);
                }, throwable -> mView.hideLoading());
    }

    public void fromNetWorkLoad() {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            mNetWorkDisposable = ApiFactory.getGankApi()
                    .getiOSData(mPage)
                    .filter(gankData -> !gankData.isError())
                    .map(GankData::getResults)
                    .flatMap(Flowable::fromIterable)
                    .map(gankEntity -> new IOS(gankEntity.getPublishedAt(),
                            gankEntity.getDesc(),
                            gankEntity.getWho(),
                            gankEntity.get_id(),
                            gankEntity.getUrl()))
                    .buffer(30)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::addData);

        } else {
            Toast.makeText((Context) mView, R.string.error_net, Toast.LENGTH_SHORT).show();
            mView.hideLoading();
        }
    }

    private void deleteData() {
        mRealm.executeTransaction(realm -> realm.delete(IOS.class));
    }

    private void addData(List<IOS> data) {
        if (isRefresh) deleteData();
        mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
    }

    @Override
    protected void unSubscribe() {
        if (mDisposable != null) mDisposable.dispose();
        if (mNetWorkDisposable != null) mNetWorkDisposable.dispose();
    }

    @Override
    protected void savePage(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_IOS_PAGE, mPage);
        editor.apply();
    }
}
