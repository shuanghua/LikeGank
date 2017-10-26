package com.shua.likegank.presenters;

import android.content.Context;
import android.widget.Toast;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.GankData;
import com.shua.likegank.data.entity.IOS;
import com.shua.likegank.interfaces.IOSViewInterface;
import com.shua.likegank.utils.LikeGankUtils;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import me.drakeet.multitype.Items;

/**
 * IOSPresenter
 * Created by moshu on 2017/5/13.
 */

public class IOSPresenter extends NetWorkBasePresenter<IOSViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;
    private static int mPage = 1;
    private Realm mRealm;
    private List<IOS> mList;
    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;

    public IOSPresenter(IOSViewInterface viewInterface) {
        mView = viewInterface;
        mRealm = Realm.getDefaultInstance();
        mList = new ArrayList<>();
    }

    private Items pareData(List<IOS> iosList) {
        Items items = new Items();
        String time1 = LikeGankUtils.timeString(iosList.get(0).time);//拿到第一条数据的时间
        String time2 = "";
        items.add(new Category(time1));//先添加第一条数据的时间
        for (int i = 0; i < iosList.size(); i++) {
            items.add(iosList.get(i));
            if (i < iosList.size() - 1)
                time2 = LikeGankUtils.timeString(iosList.get(i + 1).time);//拿到第二条数据的时间
            if (!time1.equals(time2)) {
                items.add(new Category(time2));
                time1 = time2;
            }
        }
        return items;
    }

    private void deleteData() {
        mRealm.executeTransaction(realm -> realm.delete(IOS.class));
    }

    private void saveData(List<IOS> data) {
        mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
    }

    public void fromRealmLoad() {
        mDisposable = mRealm.where(IOS.class)
                .findAll()
                .asFlowable()
                .filter(androids -> androids.size() > 0)
                .map(this::pareData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> mView.showData(result));
    }

    private void fromNetWorkLoad() {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            mNetWorkDisposable = ApiFactory.getGankApi()
                    .getiOSData(mPage)
                    .filter(gankData -> !gankData.isError())
                    .map(GankData::getResults)
                    .flatMap(Flowable::fromIterable)
                    .map(gankEntity -> new IOS(gankEntity.getPublishedAt(),
                            gankEntity.getDesc(), gankEntity.getWho(),
                            gankEntity.get_id(), gankEntity.getUrl()))
                    .buffer(60)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(iosList -> {
                        if (mPage == 1) {
                            mList.addAll(iosList);
                            saveData(mList);
                        } else {
                            mList.addAll(iosList);
                            mView.showData(pareData(mList));
                        }
                    });
        } else {
            Toast.makeText((Context) mView, R.string.error_net, Toast.LENGTH_SHORT).show();
            mView.hideLoading();
        }
    }

    public void requestData(int requestType) {
        if (NetWorkUtils.isNetworkConnected((Context) mView)) {
            switch (requestType) {
                case REQUEST_REFRESH:
                    deleteData();
                    mList.clear();
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
    }
}
