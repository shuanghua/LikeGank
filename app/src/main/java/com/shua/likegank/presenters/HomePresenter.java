package com.shua.likegank.presenters;

import androidx.fragment.app.Fragment;

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

/**
 * HomePresenter
 * loadingView显示： 准备获取数据时显示，
 * loadingView隐藏： 显示数据后隐藏，请求过程出现错误时隐藏，窗口页面暂停时隐藏
 * 所以 loadingView 更适合在 Fragment / Activity 中使用，而不是在 Presenter 中使用
 * Created by shuanghua on 2017/5/13.
 */
public class HomePresenter extends NetWorkBasePresenter<HomeViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;
    private int mPage = 1;
    private Realm mRealm;
    private List<Home> mList;
    private CompositeDisposable mDisposable = new CompositeDisposable();

    public HomePresenter(HomeViewInterface viewInterface) {
        mFragment = viewInterface;
        mRealm = Realm.getDefaultInstance();
        mList = new ArrayList<>();
    }

    @Override
    public void requestNetWorkData(int requestType) {
        if (!NetWorkUtils.isNetworkConnected(((Fragment) mFragment).requireContext())) {
            mFragment.onError("网络错误！");
            return;
        }
        if (requestType == REQUEST_REFRESH) {
            mPage = 1;
            fromNetWorkLoadV2();
        } else if (requestType == REQUEST_LOAD_MORE) {
            mPage++;
            fromNetWorkLoadV2();
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
                }, throwable -> mFragment.onError("服务器数据异常："
                        + throwable.getMessage())));
    }

    private void saveData(List<Home> data) {
        mRealm.executeTransaction(realm -> {
            realm.delete(Home.class);
            realm.copyToRealmOrUpdate(data);
        });
    }

    public void subscribeDBData() {
        mDisposable.add(mRealm.where(Home.class).findAll()
                .asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homes -> mFragment.showData(homes)));
    }

    public void unSubscribe() {
        mDisposable.dispose();
        if (mRealm != null) mRealm.close();
        mFragment = null;
    }
}
