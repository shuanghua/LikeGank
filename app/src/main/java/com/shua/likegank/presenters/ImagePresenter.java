package com.shua.likegank.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.LikeGankEntity;
import com.shua.likegank.data.entity.FuLi;
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
 * Created by moshu on 2017/5/13.
 */

public class ImagePresenter extends BasePresenter {

    public static final String KEY_IMAGE_PAGE = "image_page";

    public boolean isRefresh = true;
    public static int mPage = 1;

    private final Realm mRealm;
    private RefreshViewInterface<FuLi> mView;
    public List<FuLi> mMeiZis = new ArrayList<>();
    private Subscription mUnbscribeRealm;
    private Subscription mUnsubscribeRetrofit;

    public ImagePresenter(RefreshViewInterface<FuLi> viewInterface) {
        this.mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
    }

    private List<FuLi> conversionData(List<LikeGankEntity> list) {
        for (LikeGankEntity gankEntity : list)
            mMeiZis.add(new FuLi(gankEntity.get_id(), gankEntity.getUrl()));
        return mMeiZis;
    }

    /**
     * The call is automatically triggered when the local data is updated
     */
    @SuppressLint("WrongConstant")
    public void fromRealmLoad() {
        mUnbscribeRealm = mRealm.where(FuLi.class)
                .findAll()
                .asObservable()
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

    @SuppressLint("WrongConstant")
    public void fromNetWorkLoad() {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            mUnsubscribeRetrofit = ApiFactory.getGankApi()
                    .getFuLiData(mPage)
                    .filter(gankData -> !gankData.isError())
                    .map(GankData::getResults)
                    .single(likeGankEntities -> likeGankEntities.size() > 0)
                    .map(this::conversionData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(meiZis -> {
                        if (isRefresh) clearData();
                        saveData(meiZis);
                    }, throwable -> {
                        Toast.makeText((Context) mView, throwable.getMessage()
                                , Toast.LENGTH_SHORT).show();
                        mView.hideLoading();
                    });
        } else {
            Toast.makeText((Context) mView, R.string.error_net
                    , Toast.LENGTH_SHORT).show();
            mView.hideLoading();
        }
    }

    private void clearData() {
        mRealm.executeTransaction(realm -> realm.delete(FuLi.class));
    }

    private void saveData(List<FuLi> data) {
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
        editor.putInt(KEY_IMAGE_PAGE, mPage);
        editor.apply();
    }
}
