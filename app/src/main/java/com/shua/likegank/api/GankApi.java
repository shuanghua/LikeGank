package com.shua.likegank.api;

import com.shua.likegank.data.GankBean;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * GankApi
 */

public interface GankApi {

    //V1
//    @GET("data/福利/39/{page}")
//    Flowable<GankData> getGirlsData(@Path("page") int page);
//
//    @GET("data/all/60/{page}")
//    Flowable<GankData> getHomeData(@Path("page") int page);
//
//    @GET("data/Android/60/{page}")
//    Flowable<GankData> getAndroidData(@Path("page") int page);
//
//    @GET("data/iOS/60/{page}")
//    Flowable<GankData> getIOSData(@Path("page") int page);

    // V2
    @GET("data/category/All/type/All/page/{page}/count/50")
    Flowable<GankBean> getHomeDataV2(@Path("page") int page);

    @GET("data/category/GanHuo/type/Android/page/{page}/count/50")
    Flowable<GankBean> getAndroidDataV2(@Path("page") int page);

//    @GET("data/category/GanHuo/type/iOS/page/{page}/count/50")
//    Flowable<GankBean> getIOSDataV2(@Path("page") int page);

    @GET("data/category/Girl/type/Girl/page/{page}/count/30")
    Flowable<GankBean> getGirlsDataV2(@Path("page") int page);
}
