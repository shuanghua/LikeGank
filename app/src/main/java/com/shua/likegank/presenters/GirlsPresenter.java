package com.shua.likegank.presenters;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.entity.Android;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.interfaces.ImageViewInterface;
import com.shua.likegank.utils.AppUtils;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * ImageViewPresenter
 * Created by moshu on 2017/5/13.
 */

public class GirlsPresenter extends NetWorkBasePresenter<ImageViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;
    private int mPage = 1;
    private int mPageIndex = 1;
    private final Realm mRealm;
    private final List<Content> mList;
    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;

    public GirlsPresenter(ImageViewInterface viewInterface) {
        mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
        mList = new ArrayList<>();
    }

    public void requestData(int requestType) {
        if (NetWorkUtils.isNetworkConnected(((Fragment) mView).requireContext())) {
            switch (requestType) {
                case REQUEST_REFRESH:
                    mPageIndex = mPage;
                    mPage = 1;
//                    fromNetWorkLoad();
                    fromNetWorkLoadV2();
                    break;
                case REQUEST_LOAD_MORE:
                    mView.showLoading();
                    mPage++;
//                    fromNetWorkLoad();
                    fromNetWorkLoadV2();
                    break;
                default:
                    break;
            }
        } else {
            AppUtils.toast(R.string.error_net);
            mView.hideLoading();
        }
    }

    private static final String ERROR_HOST = "http://7xi8d6.com1.z0.glb.clouddn.com";

    private void fromNetWorkLoad() {
        mNetWorkDisposable = ApiFactory.getGankApi()
                .getGirlsData(mPage)
                .filter(gankData -> !gankData.isError())
                .map(GankData::getResults)
                .flatMap(Flowable::fromIterable)
                .filter(gankEntity -> !gankEntity.getUrl().contains(ERROR_HOST))
                .map(gankEntity -> new Content(gankEntity.get_id(), gankEntity.getUrl()))
                .buffer(39)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (mPage == 1) {
                        saveData(result);
                    } else {
                        mList.addAll(result);
                        mView.showData(mList);
                    }
                }, throwable -> {
                    Log.e("ImagePresenter:", Objects.requireNonNull(throwable.getMessage()));
                    mView.hideLoading();
                    AppUtils.toast(R.string.error_net);
                });
    }

    private void fromNetWorkLoadV2() {
        mNetWorkDisposable = ApiFactory.getGankApi().getGirlsDataV2(1)
                .map(gankBean -> gankBean.getData())
                .concatMap(Flowable::fromIterable)
                .map(dataBean -> new Content(dataBean.get_id(), dataBean.getImages().get(0)))
                .buffer(39)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (mPage == 1) {
                        saveData(result);
                    } else {
                        mList.addAll(result);
                        mView.showData(mList);
                    }
                }, throwable -> {
                    Log.e("GirlsPresenter:", Objects.requireNonNull(throwable.getMessage()));
                    mView.hideLoading();
                    AppUtils.toast(R.string.error_net);
                });
    }

    private void saveData(List<Content> data) {
        RealmResults<Content> all = mRealm.where(Content.class).findAll();
        if (all.size() > 0) {
            Content content = all.get(0);
            if (content != null) {
                if (!(content.content).equals(data.get(0).content)) {//新数据和数据库数据不一样
                    mRealm.executeTransaction(realm -> {
                        realm.delete(Android.class);
                        mList.clear();
                        mList.addAll(data);
                        realm.copyToRealmOrUpdate(data);
                    });
                } else {
                    mPage = mPageIndex;
                    AppUtils.toast(R.string.tip_no_new_data);
                    mView.hideLoading();
                }
            }
        } else {
            mList.addAll(data);
            mRealm.executeTransaction(
                    realm -> realm.copyToRealmOrUpdate(data)
            );//第一次进入应用时
        }
    }

    public void fromRealmLoad() {
        mDisposable = mRealm.where(Content.class)
                .findAll()
                .asFlowable()
                .filter(results -> results.size() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        results -> mView.showData(results),
                        throwable -> Log.e("ImagePresenter:",
                                Objects.requireNonNull(throwable.getMessage()))
                );
    }

    public void unSubscribe() {
        if (mDisposable != null) mDisposable.dispose();
        if (mNetWorkDisposable != null) mNetWorkDisposable.dispose();
        if (mRealm != null) mRealm.close();
    }
}
