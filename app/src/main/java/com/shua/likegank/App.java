package com.shua.likegank;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.shua.likegank.utils.AppUtils;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * LikeGank application
 * Created by SHUA on 2017/3/31.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        AppUtils.setAppContext(getApplicationContext());

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)           // Enables Crashlytics debugger
                .build();
        Fabric.with(fabric);
        initRealm();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        //Realm.deleteRealm(config);
        Realm.setDefaultConfiguration(config);
    }
}
