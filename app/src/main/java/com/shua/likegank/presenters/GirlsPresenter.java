package com.shua.likegank.presenters;

import androidx.collection.ArraySet;
import androidx.fragment.app.Fragment;

import com.shua.likegank.data.RealmRepository;
import com.shua.likegank.data.entity.Girl;
import com.shua.likegank.interfaces.ImageViewInterface;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.kotlin.Realm;
import io.realm.kotlin.RealmConfiguration;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import timber.log.Timber;

/**
 * ImageViewPresenter
 * Created by shuanghua on 2017/5/13.
 */
public class GirlsPresenter extends NetWorkBasePresenter<ImageViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;

    private Realm mRealm;

    private int mPage = 1; //请求页，每次下拉加载更多 mPage++
    private int mCurrentPage = 1;// 当前页，ui 列表当前显示的内容是第几页
    private int mMaxPageCount = 2; // 服务器总页数

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    List<Girl> fakeData1 = new ArrayList<>();
    List<Girl> fakeData2 = new ArrayList<>();


    /**
     * 首次打开默认从数据加载全部数据
     * 按每页 30 条进行计算数据库总页数
     *
     * @param viewInterface 实现了 ImageViewInterface 的 fragment
     */
    public GirlsPresenter(ImageViewInterface viewInterface) {
        girlsFakeData();
        mFragment = viewInterface;
        initRealm();
    }

    private void initRealm() {
        Timber.d("G------->>" + "initRealm");
        KClass<Girl> kClass = JvmClassMappingKt.getKotlinClass(Girl.class);// 反射获取java class 对应的 kotlin class
        Set<KClass<Girl>> arraySet = new ArraySet<>();// ArraySet 在 sdk 23+ 才提供
        arraySet.add(kClass);
        RealmConfiguration config = RealmConfiguration.Companion.create(arraySet);
        mRealm = Realm.Companion.open(config);
    }

    private void girlsFakeData() {
        for (int i = 0; i < 30; i++) {
            fakeData1.add(new Girl(
                    i + "",
                    "服务器已关闭,当前使用临时图片展示",
                    "https://img1.baidu.com/it/u=479423680,135458553&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500"));
        }

        for (int i = 30; i < 60; i++) {
            fakeData2.add(new Girl(
                    i + "",
                    "服务器已关闭,当前使用临时图片展示",
                    "https://img1.baidu.com/it/u=479423680,135458553&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500"));
        }
    }

    @Override
    public void requestNetWorkData(int requestType) {
        if (NetWorkUtils.hasNetwork(
                ((Fragment) mFragment).requireContext())) {
            mFragment.onError("网络错误！");
            return;
        }
        switch (requestType) {
            case REQUEST_REFRESH:
                mPage = 1;
                fromNetWorkLoadV2(fakeData1);
                break;
            case REQUEST_LOAD_MORE:
                if (mCurrentPage == mMaxPageCount) {// 1==4
                    mFragment.onError("到底啦~");
                    return;
                } else {
                    mPage++;
                    fromNetWorkLoadV2(fakeData2);
                }
                break;
            default:
                break;
        }
    }

    private void fromNetWorkLoadV2(List<Girl> fakeData) {
        mDisposable.add(Single.just(fakeData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::saveDataToDB,
                        throwable -> mFragment.onError("数据异常：")
                )
        );

    }

    private void saveDataToDB(List<Girl> girls) {
        if (girls.isEmpty()) {
            return;
        }
        if (mPage == 1) { // 下拉刷新
            saveRefreshData(girls);
        } else {// 上拉加载更多
            saveLoadMoreData(girls);
        }
    }

    private void saveRefreshData(List<Girl> girls) {
        mDisposable.add(Single // fromCallable()用于发射耗时任务结果
                .fromCallable(() -> RealmRepository.INSTANCE.hasExists(
                        mRealm,
                        girls.get(0).get_id(),
                        Girl.class
                ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hasSaved -> {
                            if (!hasSaved) {
                                RealmRepository.INSTANCE.saveGirls(mRealm, girls);
                                mCurrentPage = mPage;
                            } else {
                                mPage = mCurrentPage;
                                mFragment.onError("已经是最新数据！");
                            }
                        }
                ));
    }

    private void saveLoadMoreData(List<Girl> girls) {
        mCurrentPage = mPage;
        Timber.d("mCurrentPage:" + mCurrentPage + " mMaxPageCount:" + mMaxPageCount + " mPage:" + mPage);
        RealmRepository.INSTANCE.saveGirls(mRealm, girls);// Ream 插入数据，内部会切换线程，所以不需要考虑调用线程
    }

    public void subscribeDBData() {
        mDisposable.add(RealmRepository.INSTANCE.subscribeGirl(mRealm)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(girls -> {
                    mFragment.showData(girls);
                }));
    }

    public void destroy() {
        Timber.d("G------->>" + "destroy");
        mDisposable.dispose();
        mRealm.close();
    }
}
