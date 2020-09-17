package com.shua.likegank.presenters;

import androidx.fragment.app.Fragment;

import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankBean;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.interfaces.HomeViewInterface;
import com.shua.likegank.utils.AppUtils;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import timber.log.Timber;

/**
 * HomePresenter
 * loadingView显示： 准备获取数据时显示，
 * loadingView隐藏： 显示数据后隐藏，请求过程出现错误时隐藏，窗口页面暂停时隐藏
 * 所以 loadingView 更适合在 Fragment / Activity 中使用，而不是在 Presenter 中使用
 * Created by shuanghua on 2017/5/13.
 */
public class HomePresenter extends NetWorkBasePresenter<HomeViewInterface> {

    /**
     * 后续优化： 把一些全局变量 和 部分方法抽取到父类一减少重复代码
     */

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;

    private int mPage = 1; //请求页
    private int mCurrentPage = 1;// 用于临时保存当前加载了多少页
    private int mPageCount = 0; // 服务器总页数

    private Realm mRealm;
    private CompositeDisposable mDisposable = new CompositeDisposable();

    public HomePresenter(HomeViewInterface viewInterface) {
        mFragment = viewInterface;
        mRealm = Realm.getDefaultInstance();
        int dbSize = mRealm.where(Home.class).findAll().size();
        if (dbSize > 0) mCurrentPage = (int) Math.ceil(dbSize / 50.0);// 获取当前数据库已经存了多少页
    }

    @Override
    public void requestNetWorkData(int requestType) {
        if (NetWorkUtils.hasNetwork(((Fragment) mFragment).requireContext())) {
            mFragment.onError("网络错误！");
            return;
        }
        if (requestType == REQUEST_REFRESH) {
            mPage = 1;
            fromNetWorkLoadV2();
        } else if (requestType == REQUEST_LOAD_MORE) {
            if (mCurrentPage == mPageCount) {// 1==4
                mFragment.onError("到底啦~");
            } else {
                mPage++;
                fromNetWorkLoadV2();
            }
        }
    }

    private void fromNetWorkLoadV2() {
        mDisposable.add(ApiFactory.getGankApi()
                .getHomeDataV2(mPage)
                .map(bean -> {
                    mPageCount = bean.getPage_count();
                    return bean;
                })
                .map(GankBean::getData)
                .flatMap(Flowable::fromIterable)
                .map(gankBean -> {
                    gankBean.setPublishedAt(AppUtils
                            .gankSubTimeString(gankBean.getPublishedAt()));
                    return gankBean;
                })
                .map(gankEntity -> new Home(gankEntity.get_id(),
                        gankEntity.getDesc(),
                        gankEntity.getPublishedAt(),
                        gankEntity.getType(),
                        gankEntity.getUrl(),
                        gankEntity.getAuthor()))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::saveDataToDB,
                        throwable -> mFragment.onError("服务器数据异常："
                                + throwable.getMessage())));
    }

    private void saveDataToDB(List<Home> data) {
        if (data.size() == 0) {
            return;
        }
        if (mPage == 1) {
            final Home home = mRealm
                    .where(Home.class)
                    .equalTo("_id", data.get(0)._id)
                    .findFirst();
            if (home == null) {
                mRealm.executeTransaction(realm -> {
                    realm.delete(Home.class);
                    realm.copyToRealmOrUpdate(data);
                    mCurrentPage = mPage;
                });
            } else {
                mPage = mCurrentPage;//用户先前在当前窗口可能已经加载了很多页数据，以让用户可以继续加载更多的操作
                mFragment.onError("已经是最新数据！");
            }
        } else {
            mCurrentPage = mPage;
            Timber.d("mCurrentPage:" + mCurrentPage + " pageCount:" + mPageCount + " mPage:" + mPage);
            mRealm.executeTransaction(realm -> realm.insertOrUpdate(data));
        }
    }

    public void subscribeDBData() {
        mDisposable.add(mRealm.where(Home.class).findAll()
                .asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homes -> mFragment.showData(homes)));
    }

    public void unSubscribe() {
        mDisposable.dispose();
        mRealm.close();
    }
}
