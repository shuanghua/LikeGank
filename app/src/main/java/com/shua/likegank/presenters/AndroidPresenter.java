package com.shua.likegank.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.entity.Android;
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

public class AndroidPresenter extends BasePresenter {

    public static int mPage = 1;
    public static final String KEY_ANDROID_PAGE = "ANDROID_PAGE";

    public boolean isRefresh = true;

    private RefreshViewInterface mView;
    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;
    private Realm mRealm;

    public AndroidPresenter(RefreshViewInterface viewInterface) {
        this.mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
    }

    private Items pareData(List<Android> androids) {
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
        return items;
    }

    private void deleteData() {
        mRealm.executeTransaction(realm -> realm.delete(Android.class));
    }

    private void addData(List<Android> data) {
        if (isRefresh) deleteData();
        mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
    }

    public void fromRealmLoad() {
        mDisposable = mRealm.where(Android.class)
                .findAll()
                .asFlowable()
                .filter(androids -> androids.size() > 0)
                .map(this::pareData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> mView.showData(items), throwable -> {
                    Logger.e(throwable.getMessage());
                    mView.hideLoading();
                });
    }

    public void fromNetWorkLoad() {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            mNetWorkDisposable = ApiFactory.getGankApi()
                    .getAndroidData(mPage)
                    .filter(gankData -> !gankData.isError())
                    .map(GankData::getResults)
                    .flatMap(Flowable::fromIterable)
                    .map(gankEntity -> new Android(gankEntity.getPublishedAt(),
                            gankEntity.getDesc(), gankEntity.getWho(),
                            gankEntity.get_id(), gankEntity.getUrl()))
                    .buffer(60)
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
        editor.putInt(KEY_ANDROID_PAGE, mPage);
        editor.apply();
    }
}
