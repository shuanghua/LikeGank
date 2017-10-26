package com.shua.likegank.api;

import com.shua.likegank.data.GankData;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * GankApi
 */

public interface GankApi {

    @GET("data/福利/40/{page}")
    Flowable<GankData> getFuLiData(@Path("page") int page);

    @GET("data/all/60/{page}")
    Flowable<GankData> getHomeData(@Path("page") int page);

    @GET("data/Android/60/{page}")
    Flowable<GankData> getAndroidData(@Path("page") int page);

    @GET("data/iOS/60/{page}")
    Flowable<GankData> getiOSData(@Path("page") int page);
}
