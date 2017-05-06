package com.shua.likegank.api;

import com.shua.likegank.data.GankData;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * GankApi
 */

public interface GankApi {

    @GET("data/福利/20/{page}")
    Observable<GankData> getFuLiData(@Path("page") int page);

    @GET("data/all/30/{page}")
    Observable<GankData> getHomeData(@Path("page") int page);

    @GET("data/Android/50/{page}")
    Observable<GankData> getAndroidData(@Path("page") int page);

    @GET("data/iOS/50/{page}")
    Observable<GankData> getiOSData(@Path("page") int page);
}
