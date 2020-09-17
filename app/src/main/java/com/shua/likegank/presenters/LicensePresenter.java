package com.shua.likegank.presenters;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.shua.likegank.R;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.data.uimodel.Category;
import com.shua.likegank.interfaces.LicenseViewInterface;

import me.drakeet.multitype.Items;

/**
 * AboutPresenter
 * Created by shuanghua on 2017/5/17.
 */

public class LicensePresenter {
    private final Context mContext;
    private final LicenseViewInterface mView;

    public LicensePresenter(LicenseViewInterface mView) {
        this.mView = mView;
        this.mContext = ((Fragment) mView).requireContext();
    }

    public void loadData() {
        Items items = new Items();
        //items.add(new Category("\n"));

        items.add(new Category("Realm-Java"));
        items.add(new Content(mContext.getResources().getString(R.string.license_apache_2_0)
                + "\n" + "This product is not being made available to any person located in Cuba, Iran, North Korea, Sudan, Syria or the Crimea region, or to any other person that is not eligible to receive the product under U.S. law.",
                "https://github.com/realm/realm-java"));

        items.add(new Category("Retrofit2"));
        items.add(new Content("Copyright 2013 Square, Inc.\n" + mContext.getResources().getString(R.string.license_apache_2_0),
                "https://github.com/square/retrofit"));

        items.add(new Category("Multitype"));
        items.add(new Content("Copyright 2017 drakeet.\n" + mContext.getResources().getString(R.string.license_apache_2_0),
                "https://github.com/drakeet/MultiType"));

        items.add(new Category("Leakcanary"));
        items.add(new Content("Copyright 2015 Square, Inc.\n" + mContext.getResources().getString(R.string.license_apache_2_0),
                "https://github.com/square/leakcanary"));

        items.add(new Category("Glide"));
        items.add(new Content("Copyright 2014 Google, Inc.\n" + "BSD, part MIT and Apache 2.0",
                "https://github.com/bumptech/glide"));

        items.add(new Category("Rxjava"));
        items.add(new Content("Copyright (c) 2016, RxJava Contributors.\n" + mContext.getResources().getString(R.string.license_apache_2_0),
                "https://github.com/ReactiveX/RxJava"));

        items.add(new Category("RxAndroid"));
        items.add(new Content("Copyright 2015 The RxAndroid authors.\n" + mContext.getResources().getString(R.string.license_apache_2_0),
                "https://github.com/ReactiveX/RxAndroid"));

        items.add(new Category("RxPermissions"));
        items.add(new Content("Copyright (C) 2015 Thomas Bruyelle.\n" + mContext.getResources().getString(R.string.license_apache_2_0),
                "https://github.com/tbruyelle/RxPermissions"));

        items.add(new Category("Photoview"));
        items.add(new Content("Copyright 2017 Chris Banes. \n" + mContext.getResources().getString(R.string.license_apache_2_0),
                "https://github.com/chrisbanes/PhotoView"));

        items.add(new Category("Timber"));
        items.add(new Content("Copyright 2013 Jake Wharton. \n" + mContext.getResources().getString(R.string.license_apache_2_0),
                "https://github.com/JakeWharton/timber"));

        mView.showData(items);
    }
}
