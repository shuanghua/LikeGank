package com.shua.likegank.presenters;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.shua.likegank.R;
import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankBean;
import com.shua.likegank.data.entity.Android;
import com.shua.likegank.data.uimodel.Category;
import com.shua.likegank.interfaces.AndroidViewInterface;
import com.shua.likegank.utils.AppUtils;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import me.drakeet.multitype.Items;

/**
 * AndroidPresenter
 * Created by moshu on 2017/5/13.
 */

public class AndroidPresenter extends NetWorkBasePresenter<AndroidViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;
    private int mPage = 1;
    private int mPageIndex = 1;
    private final Items items;
    private final Realm mRealm;
    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;
    private String time2 = "";

    public AndroidPresenter(AndroidViewInterface viewInterface) {
        mFragment = viewInterface;
        mRealm = Realm.getDefaultInstance();
        items = new Items();
    }

    private Items pareData(List<Android> androids) {
        String time1 = AppUtils.timeString(androids.get(0).time);//存储第一条数据的时间
        if (!time1.equals(time2)) {//当第二页数据传过来时，第一页最后一个数据的时间和第二页第一条数据的时间不相等时
            items.add(new Category(time1));//add 时间，说明当前页的第一条数据的时间可以作为一个新的Header
        }
        for (int i = 0; i < androids.size(); i++) {
            items.add(androids.get(i));//add 内容
            if (i < androids.size() - 1)
                time2 = AppUtils.timeString(androids.get(i + 1).time);
            if (!time1.equals(time2)) {
                items.add(new Category(time2));
                time1 = time2;
            }
        }
        return items;
    }

    private void saveData(List<Android> data) {//刷新时才调用到
        RealmResults<Android> all = mRealm.where(Android.class).findAll();
        if (all.size() > 0) {
            Android android = all.get(0);
            if (android != null) {
                if (!(android.content).equals(data.get(0).content)) {
                    mRealm.executeTransaction(realm -> {
                        items.clear();
                        time2 = "";
                        realm.delete(Android.class);
                        realm.copyToRealmOrUpdate(data);
                    });
                } else {//新数据和本地数据一样,不保存，同时不做 Adapter 刷新
                    mPage = mPageIndex;
                    mFragment.onError("已经是最新数据了");
                }
            }
        } else {
            mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
        }
    }

    public void subscribeDBData() {
        mDisposable = mRealm.where(Android.class)
                .findAll()
                .asFlowable()
                .filter(androids -> androids.size() > 0)
                .map(this::pareData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> mFragment.showData(result));
    }

    private void fromNetWorkLoadV2() {
        mNetWorkDisposable = ApiFactory.getGankApi()
                .getAndroidDataV2(mPage)
                .map(GankBean::getData)
                .flatMap(Flowable::fromIterable)
                .map(gankBean -> new Android(gankBean.getPublishedAt(),
                        gankBean.getDesc(), gankBean.getAuthor(),
                        gankBean.get_id(), gankBean.getUrl()))
                .buffer(50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(androids -> {
                    if (mPage == 1) {
                        saveData(androids);
                    } else {
                        mFragment.showData(pareData(androids));
                    }
                }, throwable -> mFragment.onError("服务器数据异常："
                        + throwable.getMessage()));
    }

    @Override
    public void requestNetWorkData(int requestType) {
        if (!NetWorkUtils.isNetworkConnected(((Fragment) mFragment).requireContext())) {
            mFragment.onError("网络错误！");
            return;
        }
        switch (requestType) {
            case REQUEST_REFRESH:
                mPageIndex = mPage;
                mPage = 1;
                fromNetWorkLoadV2();
                break;
            case REQUEST_LOAD_MORE:
                mPage++;
                fromNetWorkLoadV2();
                break;
            default:
                break;
        }
    }

    public void unSubscribe() {
        if (mDisposable != null) mDisposable.dispose();
        if (mNetWorkDisposable != null) mNetWorkDisposable.dispose();
        if (mRealm != null) mRealm.close();
        mFragment = null;
    }
}
