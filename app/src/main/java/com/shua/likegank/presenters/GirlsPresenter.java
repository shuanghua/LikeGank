package com.shua.likegank.presenters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.shua.likegank.api.ApiFactory;
import com.shua.likegank.data.GankBean;
import com.shua.likegank.data.entity.Girl;
import com.shua.likegank.interfaces.ImageViewInterface;
import com.shua.likegank.utils.NetWorkUtils;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

/**
 * ImageViewPresenter
 * Created by shuanghua on 2017/5/13.
 */
public class GirlsPresenter extends NetWorkBasePresenter<ImageViewInterface> {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_LOAD_MORE = 2;
    private int mPage = 1; //请求页
    private int mCurrentPage = 1;//用于临时保存当前下拉页
    private int pageCount = 0; //到底标记
    private final Realm mRealm;
    private Disposable mDisposable;
    private Disposable mNetWorkDisposable;

    public GirlsPresenter(ImageViewInterface viewInterface) {
        mFragment = viewInterface;
        mRealm = Realm.getDefaultInstance();
        int dbSize = mRealm.where(Girl.class).findAll().size();
        if (dbSize > 0) mCurrentPage = (int) Math.ceil(dbSize / 30.0);// 获取当前数据库已经存了多少页
    }

    @Override
    public void requestNetWorkData(int requestType) {
        if (!NetWorkUtils.isNetworkConnected(
                ((Fragment) mFragment).requireContext())) {
            mFragment.onError("网络错误！");
            return;
        }
        switch (requestType) {
            case REQUEST_REFRESH:
                mPage = 1;
                fromNetWorkLoadV2();//mpage = 1 cin = 0,
                break;
            case REQUEST_LOAD_MORE:
                if (mCurrentPage == pageCount) {
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
//        Flowable.create((FlowableOnSubscribe<List<GankBean>>) subscribe -> {
//
//        }, BackpressureStrategy.BUFFER)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread());

        mNetWorkDisposable = ApiFactory.getGankApi().getGirlsDataV2(mPage)
                .map(bean -> {
                    pageCount = bean.getPage_count();
                    return bean;
                })
                .map(GankBean::getData)
                .concatMap(Flowable::fromIterable)
                .map(gankBean -> new Girl(
                        gankBean.get_id(),
                        gankBean.getDesc(),
                        gankBean.getImages().get(0)))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::saveDataToDB,
                        throwable -> mFragment.onError("服务器数据异常："
                                + throwable.getMessage())
                );
    }

    private void saveDataToDB(List<Girl> girls) {
        if (girls.size() == 0) {
            return;
        }

        final Girl girl = mRealm
                .where(Girl.class)
                .equalTo("_id", girls.get(0)._id)
                .findFirst();

        if (mPage == 1) {//刷新时
            if (girl == null) {//数据库数据过时，意味用户需要删除数据库数据，如果没有过时，那么提醒用户，什么都不需要做
                mRealm.executeTransaction(realm -> {
                    realm.delete(Girl.class);
                    realm.copyToRealmOrUpdate(girls);
                    mCurrentPage = mPage;
                });
            } else {
                mPage = mCurrentPage;//用户先前在当前窗口可能已经加载了很多页数据，以让用户可以继续加载更多的操作
                mFragment.onError("已经是最新数据！");
            }
        } else {// 下拉加载更多
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    realm.insertOrUpdate(girls);
                }
            });
        }
    }

    public void subscribeDBData() {
        mDisposable = mRealm.where(Girl.class)
                .findAll()
                .asFlowable()
                .filter(results -> results.size() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        results -> mFragment.showData(results),
                        throwable -> mFragment.onError("数据库数据："
                                + throwable.getMessage())
                );
    }

    public void unSubscribe() {
        if (mDisposable != null) mDisposable.dispose();
        if (mNetWorkDisposable != null) mNetWorkDisposable.dispose();
        if (mRealm != null) mRealm.close();
    }
}
