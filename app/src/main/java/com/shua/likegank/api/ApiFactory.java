package com.shua.likegank.api;

/**
 * 生产 Api 对象
 * Created by SHUA on 2017/3/6.
 */

public class ApiFactory {

    private static GankApi mGankApi = null;
    private static final Object monitor = new Object();

    public static GankApi getGankApi() {
        synchronized (monitor) {
            if (mGankApi == null) {
                mGankApi = new ServiceFcactory().getGankApiService();
            }
        }
        return mGankApi;
    }
}
