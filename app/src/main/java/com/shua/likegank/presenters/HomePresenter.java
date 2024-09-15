package com.shua.likegank.presenters;

import androidx.collection.ArraySet;
import androidx.fragment.app.Fragment;

import com.shua.likegank.data.GankBean;
import com.shua.likegank.data.RealmRepository;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.data.fake.FakeGankBeanKt;
import com.shua.likegank.interfaces.HomeViewInterface;
import com.shua.likegank.utils.AppUtils;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.kotlin.Realm;
import io.realm.kotlin.RealmConfiguration;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import timber.log.Timber;

/**
 * HomePresenter
 * loadingView显示： 准备获取数据时显示，
 * loadingView隐藏： 显示数据后隐藏，请求过程出现错误时隐藏，窗口页面暂停时隐藏
 * 所以 loadingView 更适合在 Fragment / Activity 中使用，而不是在 Presenter 中使用
 * Created by shuanghua on 2017/5/13.
 */
public class HomePresenter extends NetWorkBasePresenter<HomeViewInterface> {

    // 如果有重复代码建议抽取到父类，这里为了可读性，暂不抽取
    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;

    private Realm mRealm;

    private int mPage = 1; //请求页，每次下拉加载更多 mPage++
    private int mCurrentPage = 1;// 当前页，ui 列表当前显示的内容是第几页
    private int mMaxPageCount = 0; // 服务器总页数

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    public HomePresenter(HomeViewInterface viewInterface) {
        mFragment = viewInterface;
        initRealm();
        mDisposable.add(Single.fromCallable(() -> RealmRepository.INSTANCE.queryEntitySize(mRealm, Home.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(size -> {
                    if (size > 0) {
                        // 获取当前数据库已经存了多少页（ceil取整）, 50 是默认请求一页的数量});
                        mCurrentPage = (int) Math.ceil(size / 50.0);
                    }
                }));
    }

    //    java 创建 realm
    private void initRealm() {
        Timber.d("H------->>" + "initRealm");
        KClass<Home> kClass = JvmClassMappingKt.getKotlinClass(Home.class);// 反射获取java class 对应的 kotlin class
        Set<KClass<Home>> arraySet = new ArraySet<>();// ArraySet 在 sdk 23+ 才提供
        arraySet.add(kClass);
        RealmConfiguration config = RealmConfiguration.Companion.create(arraySet);
        mRealm = Realm.Companion.open(config);
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
            if (mCurrentPage == mMaxPageCount) {// 1==4
                mFragment.onError("到底啦~");
            } else {
                mPage++;
                fromNetWorkLoadV2();
            }
        }
    }


    private void fromNetWorkLoadV2() {
        // 由于服务器再可用，使用本地模拟数据
        mDisposable.add(Single.just(FakeGankBeanKt.getFakeGankBean())
                .map(bean -> {
                    mMaxPageCount = bean.getPage_count();
                    return bean;
                })
                .map(GankBean::getData)
                .flatMapObservable(Observable::fromIterable)
                // map 用于同步，flatMapSingle 用于异步，这里只是简单的修改值
                .map(dataBean -> { // 转成数据库模型
                    return new Home(dataBean.get_id(),
                            dataBean.getDesc(),
                            AppUtils.gankSubTimeString(dataBean.getPublishedAt()),
                            dataBean.getType(),
                            dataBean.getUrl(),
                            dataBean.getAuthor());
                })
                .toList()// 收集元素并转成 Single
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::saveDataToDB,
                        throwable -> mFragment.onError("数据异常："
                                + throwable.getMessage())
                ));
    }

    private void saveDataToDB(List<Home> homes) {
        if (homes.isEmpty()) return;
        if (mPage == 1) { // 下拉刷新
            saveRefreshData(homes);
        } else {// 上拉加载更多
            saveLoadMoreData(homes);
        }
    }

    private void saveRefreshData(List<Home> homes) {
        mDisposable.add(Single // fromCallable()用于发射耗时任务结果
                .fromCallable(() -> RealmRepository.INSTANCE.hasExists(
                        mRealm,
                        homes.get(0).get_id(),
                        Home.class
                ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hasSaved -> {
                            if (!hasSaved) {
                                RealmRepository.INSTANCE.saveHomes(mRealm, homes);
                                mCurrentPage = mPage;
                            } else {// 数据库中之前存过第一页的数据，此时就没必要再存，场景：用户一直下拉刷新，避免多次保存
                                mPage = mCurrentPage;//用户先前在当前窗口可能已经加载了很多页数据，以让用户可以继续加载更多的操作
                                mFragment.onError("已经是最新数据！");
                            }
                        }
                ));
    }

    private void saveLoadMoreData(List<Home> homes) {
        mCurrentPage = mPage;
        Timber.d("mCurrentPage:" + mCurrentPage + " pageCount:" + mMaxPageCount + " mPage:" + mPage);
        RealmRepository.INSTANCE.saveHomes(mRealm, homes);// Ream 插入数据，内部会切换线程，所以不需要考虑调用线程
    }

    public void subscribeDBData() {
        mDisposable.add(RealmRepository.INSTANCE.subscribeHome(mRealm)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homes -> {
                    mFragment.showData(homes);
                }));
    }

    public void destroy() {
        // 首页 Fragment 默认不会被销毁
        Timber.d("H------->>" + "destroy");
        mDisposable.dispose();
        mRealm.close();
    }
}
