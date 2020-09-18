package com.shua.likegank;

import android.app.Application;

import com.shua.likegank.utils.AppUtils;

import io.realm.Realm;
import io.realm.RealmConfiguration;
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

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        initRealm();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        //Realm.deleteRealm(config);
        Realm.setDefaultConfiguration(config);
    }

//    private class LikeGankMigration implements RealmMigration{
//
//        @Override
//        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
//            RealmSchema schema = realm.getSchema();
//        }
//    }
}
