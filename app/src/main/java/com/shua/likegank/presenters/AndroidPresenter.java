package com.shua.likegank.presenters;

import androidx.fragment.app.Fragment;

import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankBean;
import com.shua.likegank.data.entity.Android;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.data.uimodel.Category;
import com.shua.likegank.interfaces.AndroidViewInterface;
import com.shua.likegank.utils.AppUtils;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.Sort;
import me.drakeet.multitype.Items;
import timber.log.Timber;

/**
 * AndroidPresenter
 * Created by moshu on 2017/5/13.
 */

public class AndroidPresenter extends NetWorkBasePresenter<AndroidViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;

    private int mPage = 1; //请求页
    private int mCurrentPage = 1;// 用于临时保存当前加载了多少页
    private int mPageCount = 0; // 服务器总页数

    private String time2 = "";
    private final Items items;
    private final Realm mRealm;
    private CompositeDisposable mDisposable = new CompositeDisposable();


    public AndroidPresenter(AndroidViewInterface viewInterface) {
        mFragment = viewInterface;
        mRealm = Realm.getDefaultInstance();
        items = new Items();

        int dbSize = mRealm.where(Android.class).findAll().size();
        if (dbSize > 0) mCurrentPage = (int) Math.ceil(dbSize / 50.0);// 获取当前数据库已经存了多少页
    }

    @Override
    public void requestNetWorkData(int requestType) {
        if (NetWorkUtils.hasNetwork(((Fragment) mFragment).requireContext())) {
            mFragment.onError("网络错误！");
            return;
        }
        switch (requestType) {
            case REQUEST_REFRESH:
                mPage = 1;
                fromNetWorkLoadV2();
                break;
            case REQUEST_LOAD_MORE:
                if (mCurrentPage == mPageCount) {// 1==4
                    mFragment.onError("到底啦~");
                    return;
                } else {
                    mPage++;
                    fromNetWorkLoadV2();
                }
                break;
            default:
                break;
        }
    }

    private void fromNetWorkLoadV2() {
        mDisposable.add(ApiFactory.getGankApi()
                .getAndroidDataV2(mPage)
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
                .map(gankBean -> new Android(
                        gankBean.get_id(),
                        gankBean.getPublishedAt(),
                        gankBean.getDesc(),
                        gankBean.getAuthor(),
                        gankBean.getUrl()))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::saveData,
                        throwable -> mFragment.onError("服务器数据异常："
                                + throwable.getMessage())));
    }

    public void subscribeDBData() {
        mDisposable.add(mRealm.where(Android.class)
                .findAll().sort("time", Sort.DESCENDING)//降序排序（最新时间->最旧时间）
                .asFlowable()
                .map(this::pareData)// 转换成按时间分组的模型
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> mFragment.showData(result))
        );
    }

    //        list.add(new Category(time1));
    //        list.add(androids.get(i));
    private Items pareData(List<Android> androids) {
        Items list = new Items();
        if (androids.size() == 0) return list;

        String time1 = AppUtils.gankSubTimeString(androids.get(0).time);
        list.add(new Category(time1));

        for (int i = 0; i < androids.size() - 1; i++) {
            Timber.d("     time=%s", androids.get(i).time);
            list.add(androids.get(i));
            time2 = AppUtils.gankSubTimeString(androids.get(i + 1).time);

            if (!time1.equals(time2)) {
                list.add(new Category(time2));
                time1 = time2;
            }
        }
        return list;
    }

    private void saveData(List<Android> data) {//刷新时才调用到
        if (data.size() == 0) {
            return;
        }
        if (mPage == 1) {// 下拉刷新
            Android android = mRealm
                    .where(Android.class)
                    .equalTo("_id", data.get(0)._id)
                    .findFirst();
            if (android == null) {// db 数据过期
                mRealm.executeTransaction(realm -> {
                    mCurrentPage = mPage;

                    items.clear();
                    time2 = "";

                    realm.delete(Android.class);
                    realm.copyToRealmOrUpdate(data);
                });
            } else {// db 数据没过期
                mPage = mCurrentPage;
                mFragment.onError("已经是最新数据！");
            }
        } else {// 上拉加载
            mCurrentPage = mPage;
            //Timber.d("mCurrentPage:" + mCurrentPage + " pageCount:" + pageCount + " mPage:" + mPage);
            mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(data));
        }
    }

    public void unSubscribe() {
        mDisposable.dispose();
        mRealm.close();
    }
}
