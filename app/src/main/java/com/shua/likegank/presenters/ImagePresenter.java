package com.shua.likegank.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.entity.Content;
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

public class ImagePresenter extends BasePresenter {

    public static int mPage = 1;
    public static final String KEY_IMAGE_PAGE = "IMAGE_PAGE";

    public boolean isRefresh = true;

    private Realm mRealm;
    private RefreshViewInterface mView;
    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;

    public ImagePresenter(RefreshViewInterface viewInterface) {
        this.mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
    }

    public void fromRealmLoad() {
        mDisposable = mRealm.where(Content.class)
                .findAll()
                .asFlowable()
                .filter(RealmResults::isLoaded)
                .subscribe(meiZis -> {
                    if (meiZis.size() > 0) {
                        mView.showData(meiZis);
                    } else {
                        mView.hideLoading();
                    }
                }, throwable -> {
                    Toast.makeText((Context) mView, throwable.getMessage()
                            , Toast.LENGTH_SHORT).show();
                    mView.hideLoading();
                });
    }

    public void fromNetWorkLoad() {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            mNetWorkDisposable = ApiFactory.getGankApi()
                    .getFuLiData(mPage)
                    .filter(gankData -> !gankData.isError())
                    .map(GankData::getResults)
                    .flatMap(Flowable::fromIterable)
                    .map(gankEntity -> new Content(gankEntity.get_id(), gankEntity.getUrl()))
                    .buffer(50)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::addData);

        } else {
            Toast.makeText((Context) mView, R.string.error_net, Toast.LENGTH_SHORT).show();
            mView.hideLoading();
        }
    }

    private void deleteData() {
        mRealm.executeTransaction(realm -> realm.delete(Content.class));
    }

    private void addData(List<Content> data) {
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
        editor.putInt(KEY_IMAGE_PAGE, mPage);
        editor.apply();
    }
}
