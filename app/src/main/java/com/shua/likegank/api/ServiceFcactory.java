package com.shua.likegank.api;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by SHUA on 2017/3/6.
 */

public class ServiceFcactory {

    private final GankApi gankApiService;

    public ServiceFcactory(){
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl("http://gank.io/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        Retrofit gank = builder.build();
        gankApiService = gank.create(GankApi.class);
    }

    public GankApi getGankApiService(){
        return gankApiService;
    }
}
