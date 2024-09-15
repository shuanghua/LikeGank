package com.shua.likegank.api;

import com.shua.likegank.data.GankBean;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * GankApi
 */

public interface GankApi {
    // V2
    @GET("data/category/All/type/All/page/{page}/count/50")
    Single<GankBean> getHomeDataV2(@Path("page") int page);

    @GET("data/category/GanHuo/type/Android/page/{page}/count/50")
    Single<GankBean> getAndroidDataV2(@Path("page") int page);

//    @GET("data/category/GanHuo/type/iOS/page/{page}/count/50")
//    Flowable<GankBean> getIOSDataV2(@Path("page") int page);

    @GET("data/category/Girl/type/Girl/page/{page}/count/30")
    Single<GankBean> getGirlsDataV2(@Path("page") int page);
}
