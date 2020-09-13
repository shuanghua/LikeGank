package com.shua.likegank;

import android.app.Application;

import com.shua.likegank.utils.AppUtils;

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
        AppUtils.setAppContext(getApplicationContext());
        initRealm();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        //Realm.deleteRealm(config);
        Realm.setDefaultConfiguration(config);
    }
}
