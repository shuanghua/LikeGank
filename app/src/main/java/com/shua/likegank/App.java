package com.shua.likegank;

import static timber.log.Timber.DebugTree;

import android.app.Application;

import com.shua.likegank.utils.AppUtils;

import timber.log.Timber;

/**
 * LikeGank application
 * Created by SHUA on 2017/3/31.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.setAppContext(getApplicationContext());
        Timber.plant(new DebugTree());
    }
}
