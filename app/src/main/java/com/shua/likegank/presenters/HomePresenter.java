package com.shua.likegank.presenters;

import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankBean;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.interfaces.HomeViewInterface;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmAsyncTask;

/**
 * ArticlePresenter
 * Created by ShuangHua on 2017/5/13.
 */

public class HomePresenter extends NetWorkBasePresenter<HomeViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;
    private int mPage = 1;
    private Realm mRealm;
    private List<Home> mList;
    private RealmAsyncTask realmAsyncTask;
    private CompositeDisposable mDisposable = new CompositeDisposable();

    public HomePresenter(HomeViewInterface viewInterface) {
        mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
        mList = new ArrayList<>();
    }

    public void requestData(int requestType) {
        if (NetWorkUtils.isNetworkConnected(((Fragment) mView).requireContext())) {
            if (requestType == REQUEST_REFRESH) {
                mPage = 1;
                fromNetWorkLoadV2();
            } else if (requestType == REQUEST_LOAD_MORE) {
                mView.showLoading();
                mPage++;
                fromNetWorkLoadV2();
            }
        } else {
            Toast.makeText((Context) mView, R.string.error_net,
                    Toast.LENGTH_SHORT).show();
            mView.hideLoading();
        }
    }

    private void fromNetWorkLoadV2() {
        mDisposable.add(ApiFactory.getGankApi()
                .getHomeDataV2(mPage)
                .filter(gankBean -> gankBean.getTotal_counts() > 0)
                .map(GankBean::getData)
                .flatMap(Flowable::fromIterable)
                .map(gankEntity -> new Home(gankEntity.get_id(),
                        gankEntity.getDesc(),
                        gankEntity.getPublishedAt(),
                        gankEntity.getType(),
                        gankEntity.getUrl(),
                        gankEntity.getAuthor()))
                .buffer(50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homes -> {
                    if (mPage == 1) {
                        mList.clear();
                        mList.addAll(homes);
                        saveData(homes);
                    } else {
                        mList.addAll(homes);
                        saveData(mList);
                    }
                }, throwable -> {
                    mView.hideLoading();
                    Toast.makeText((Context) mView, "服务器数据获取出错",
                            Toast.LENGTH_SHORT).show();
                }));
    }

    private void saveData(List<Home> data) {
        realmAsyncTask = mRealm.executeTransactionAsync(realm -> {
            realm.delete(Home.class);
            realm.copyToRealmOrUpdate(data);
        });
    }

    public void subscribeDBData() {
        mDisposable.add(mRealm.where(Home.class).findAll()
                .asFlowable()
                .filter(homes -> homes.size() > 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homes -> {
                            mView.showData(homes);
                            mView.hideLoading();
                        }
                ));
    }

    public void unSubscribe() {
        mDisposable.dispose();
        if (!realmAsyncTask.isCancelled()) realmAsyncTask.cancel();
        if (mRealm != null) mRealm.close();
        mView = null;
    }
}
