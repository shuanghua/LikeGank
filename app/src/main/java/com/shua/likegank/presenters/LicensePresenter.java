package com.shua.likegank.presenters;

import android.content.Context;

import com.shua.likegank.R;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.interfaces.LicenseViewInterface;

import me.drakeet.multitype.Items;

/**
 * AboutPresenter
 * Created by moshu on 2017/5/17.
 */

public class LicensePresenter {
    private Context mContext;
    private LicenseViewInterface mView;

    public LicensePresenter(LicenseViewInterface mView) {
        this.mView = mView;
        this.mContext = (Context) mView;
    }

    public void loadData() {
        Items items = new Items();
        //items.add(new Category("\n"));
        final  String license_apache_2_0 = mContext.getResources().getString(R.string.apache_2_0);
        items.add(new Category("Realm-Java"));
        items.add(new Content(license_apache_2_0, "https://github.com/realm/realm-java"));

        items.add(new Category("Retrofit2"));
        items.add(new Content("Copyright 2013 Square, Inc.\n" + license_apache_2_0, "https://github.com/square/retrofit"));

        items.add(new Category("Multitype"));
        items.add(new Content("Copyright 2017 drakeet.\n" + license_apache_2_0,"https://github.com/drakeet/MultiType"));

        items.add(new Category("Leakcanary"));
        items.add(new Content("Copyright 2015 Square, Inc.\n" + license_apache_2_0, "https://github.com/square/leakcanary"));

        items.add(new Category("Butterknife"));
        items.add(new Content("Copyright 2013 Jake Wharton.\n" + license_apache_2_0, "https://github.com/JakeWharton/butterknife"));

        items.add(new Category("Glide"));
        items.add(new Content("BSD, part MIT and Apache 2.0.", "https://github.com/bumptech/glide"));

        items.add(new Category("Rxjava"));
        items.add(new Content("Copyright (c) 2016-present, RxJava Contributors.\n" + license_apache_2_0, "https://github.com/ReactiveX/RxJava"));

        items.add(new Category("RxAndroid"));
        items.add(new Content("Copyright 2015 The RxAndroid authors.\n" + license_apache_2_0, "https://github.com/ReactiveX/RxAndroid"));

        items.add(new Category("RxPermissions"));
        items.add(new Content(license_apache_2_0, "https://github.com/tbruyelle/RxPermissions"));

        items.add(new Category("Photoview"));
        items.add(new Content("Copyright 2017 Chris Banes \n" + license_apache_2_0, "https://github.com/chrisbanes/PhotoView"));

        items.add(new Category("Logger"));
        items.add(new Content("Copyright 2015 Orhan Obut \n" + license_apache_2_0, "https://github.com/orhanobut/logger"));

        items.add(new Category("Android-maven-gradle-plugin"));
        items.add(new Content(license_apache_2_0, "https://github.com/dcendents/android-maven-gradle-plugin"));

        mView.showData(items);
    }
}
