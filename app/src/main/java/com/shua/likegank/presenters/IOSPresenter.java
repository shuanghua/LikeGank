package com.shua.likegank.presenters;

import android.content.Context;
import android.widget.Toast;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.entity.IOS;
import com.shua.likegank.interfaces.IOSViewInterface;
import com.shua.likegank.utils.AppUtils;
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
 * IOSPresenter
 * Created by moshu on 2017/5/13.
 */

public class IOSPresenter extends NetWorkBasePresenter<IOSViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;
    private int mPage = 1;
    private int mPageIndex = 1;
    private Items items;
    private Realm mRealm;
    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;
    private String time2 = "";

    public IOSPresenter(IOSViewInterface viewInterface) {
        mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
        items = new Items();
    }

    /**
     * Group the data by time
     */
    private Items pareData(List<IOS> ioss) {
        String time1 = AppUtils.timeString(ioss.get(0).time);//存储第一条数据的时间
        if (!time1.equals(time2))//当第二页数据传过来时，第一页最后一个数据的时间和第二页第一条数据的时间不相等时
            items.add(new Category(time1));//add 时间，说明当前页的第一条数据的时间可以作为一个新的Header
        for (int i = 0; i < ioss.size(); i++) {
            items.add(ioss.get(i));//add 内容
            if (i < ioss.size() - 1)
                time2 = AppUtils.timeString(ioss.get(i + 1).time);
            if (!time1.equals(time2)) {
                items.add(new Category(time2));
                time1 = time2;
            }
        }
        return items;
    }

    private void saveData(List<IOS> data) {//刷新时才调用到
        RealmResults<IOS> all = mRealm.where(IOS.class).findAll();
        if (all.size() > 0) {
            IOS ios = all.get(0);
            if (ios != null) {
                if (!(ios.content).equals(data.get(0).content)) {
                    mRealm.executeTransaction(realm -> {
                        items.clear();
                        time2 = "";
                        realm.delete(IOS.class);
                        realm.copyToRealmOrUpdate(data);
                    });
                } else {//数据一样不保存，同时不做 Adapter 刷新
                    mPage = mPageIndex;
                    AppUtils.toast(R.string.tip_no_new_data);
                    mView.hideLoading();
                }
            }
        } else {
            mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
        }
    }

    public void fromRealmLoad() {
        mDisposable = mRealm.where(IOS.class)
                .findAll()
                .asFlowable()
                .filter(ios -> ios.size() > 0)
                .map(this::pareData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> mView.showData(result));
    }

    private void fromNetWorkLoad() {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            mNetWorkDisposable = ApiFactory.getGankApi()
                    .getiOSData(mPage)
                    .map(GankData::getResults)
                    .flatMap(Flowable::fromIterable)
                    .map(gankEntity -> new IOS(gankEntity.getPublishedAt(),
                            gankEntity.getDesc(), gankEntity.getWho(),
                            gankEntity.get_id(), gankEntity.getUrl()))
                    .buffer(60)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ioss -> {
                        if (mPage == 1) {
                            saveData(ioss);
                        } else {
                            mView.showData(pareData(ioss));
                        }
                    }, throwable -> {
                        mView.hideLoading();
                    });
        } else {
            AppUtils.toast(R.string.error_net);
            mView.hideLoading();
        }
    }

    public void requestData(int requestType) {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            switch (requestType) {
                case REQUEST_REFRESH:
                    mPageIndex = mPage;
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

    public void unSubscribe() {
        if (mDisposable != null) mDisposable.dispose();
        if (mNetWorkDisposable != null) mNetWorkDisposable.dispose();
        if (mRealm != null) mRealm.close();
        mView = null;
    }
}
