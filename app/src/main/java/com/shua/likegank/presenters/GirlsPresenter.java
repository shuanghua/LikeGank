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
import io.reactivex.disposables.CompositeDisposable;
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
    private int mCurrentPage = 1;// 用于临时保存当前加载了多少页
    private int mPageCount = 0; // 服务器总页数

    private final Realm mRealm;
    private CompositeDisposable mDisposable = new CompositeDisposable();


    /**
     * 首次打开默认从数据加载全部数据
     * 按每页 30 条进行计算数据库总页数
     *
     * @param viewInterface 实现了 ImageViewInterface 的 fragment
     */
    public GirlsPresenter(ImageViewInterface viewInterface) {
        mFragment = viewInterface;
        mRealm = Realm.getDefaultInstance();
        int dbSize = mRealm.where(Girl.class).findAll().size();
        if (dbSize > 0) mCurrentPage = (int) Math.ceil(dbSize / 30.0);// 获取当前数据库已经存了多少页
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
//        Flowable.create((FlowableOnSubscribe<List<GankBean>>) subscribe -> {
//
//        }, BackpressureStrategy.BUFFER)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread());

        mDisposable.add(ApiFactory.getGankApi().getGirlsDataV2(mPage)
                .map(bean -> {
                    mPageCount = bean.getPage_count();
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
                ));
    }

    private void saveDataToDB(List<Girl> girls) {
        if (girls.size() == 0) {
            return;
        }
        if (mPage == 1) {//刷新时
            final Girl girl = mRealm
                    .where(Girl.class)
                    .equalTo("_id", girls.get(0)._id)
                    .findFirst();
            if (girl == null) {//数据库数据过期
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
            mCurrentPage = mPage;
            //Timber.d("mCurrentPage:" + mCurrentPage + " pageCount:" + pageCount + " mPage:" + mPage);
            mRealm.executeTransaction(realm -> realm.insertOrUpdate(girls));
        }
    }

    public void subscribeDBData() {
        mDisposable.add(mRealm.where(Girl.class)
                .findAll()
                .asFlowable()
                .filter(results -> results.size() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        results -> mFragment.showData(results),
                        throwable -> mFragment.onError("数据库数据："
                                + throwable.getMessage())
                ));
    }

    public void unSubscribe() {
        mDisposable.dispose();
        mRealm.close();
    }
}
