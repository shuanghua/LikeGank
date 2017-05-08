package com.shua.likegank.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shua.likegank.R;
import com.shua.likegank.data.Category;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.ui.base.ToolbarActivity;
import com.shua.likegank.ui.item_binder.CategoryItemBinder;
import com.shua.likegank.ui.item_binder.ContentItemBinder;

import butterknife.BindView;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class AboutActivity extends ToolbarActivity {

    private PackageInfo info;

    @Override
    protected int contentView() {
        return R.layout.activity_about;
    }

    @Override
    protected boolean addBack() {
        return true;
    }

    @BindView(R.id.list)
    RecyclerView mRecycler;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    private MultiTypeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshLayout.setEnabled(false);
        setTitle(getResources().getString(R.string.about));
        initRecyclerView();
        addData();
    }

    private void initRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Category.class, new CategoryItemBinder());
        mAdapter.register(Content.class, new ContentItemBinder());
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.setAdapter(mAdapter);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    public void addData() {
        Items items = new Items();
        items.add(new Category(getResources().getString(R.string.app_name)));
        items.add(new Content(getResources().getString(R.string.about_Introduction), ""));

        items.add(new Category("----------------------------------------------------------------------------------"));

        items.add(new Content(getResources().getString(R.string.like_gank_github),
                "https://github.com/Shuanghua/LikeGank"));
        items.add(new Content(getResources().getString(R.string.gank_api),
                "http://gank.io/api"));
        items.add(new Content(getResources().getString(R.string.meizi_github),
                "https://github.com/drakeet/Meizhi"));


        items.add(new Category("当前版本："));
        items.add(new Content("v" + getAppVersion(),
                ""));
        items.add(new Category(getResources().getString(R.string.open_license)));

        items.add(new Category("Realm-Java"));
        items.add(new Content(getResources().getString(R.string.license_realm_java),
                "https://github.com/realm/realm-java"));

        items.add(new Category("Retrofit2"));
        items.add(new Content("Copyright 2013 Square, Inc.\n" + getResources().getString(R.string.license_LICENSE_2_0),
                "https://github.com/square/retrofit"));

        items.add(new Category("Leakcanary"));
        items.add(new Content("Copyright 2015 Square, Inc.\n" + getResources().getString(R.string.license_LICENSE_2_0),
                "https://github.com/square/leakcanary"));

        items.add(new Category("Butterknife"));
        items.add(new Content("Copyright 2013 Jake Wharton.\n" + getResources().getString(R.string.license_LICENSE_2_0),
                "https://github.com/JakeWharton/butterknife"));

        items.add(new Category("Multitype"));
        items.add(new Content("Copyright 2017 drakeet.\n" + getResources().getString(R.string.license_LICENSE_2_0),
                "https://github.com/drakeet/MultiType"));

        items.add(new Category("Glide"));
        items.add(new Content("BSD, part MIT and Apache 2.0. See the https://github.com/bumptech/glide/blob/master/LICENSE ",
                "https://github.com/bumptech/glide"));

        items.add(new Category("Rxjava"));
        items.add(new Content("Copyright (c) 2016-present, RxJava Contributors.\n" + getResources().getString(R.string.license_LICENSE_2_0),
                "https://github.com/ReactiveX/RxJava"));

        items.add(new Category("RxAndroid"));
        items.add(new Content("Copyright 2015 The RxAndroid authors.\n" + getResources().getString(R.string.license_LICENSE_2_0),
                "https://github.com/ReactiveX/RxAndroid"));

        items.add(new Category("RxPermissions"));
        items.add(new Content(getResources().getString(R.string.license_RxPermissions),
                "https://github.com/tbruyelle/RxPermissions"));

        items.add(new Category("Photoview"));
        items.add(new Content("Copyright 2017 Chris Banes \n" + getResources().getString(R.string.license_LICENSE_2_0),
                "https://github.com/chrisbanes/PhotoView"));

        items.add(new Category("Logger"));
        items.add(new Content("Copyright 2015 Orhan Obut \n" + getResources().getString(R.string.license_LICENSE_2_0),
                " https://github.com/orhanobut/logger"));

        items.add(new Category("Android-maven-gradle-plugin"));
        items.add(new Content(getResources().getString(R.string.license_LICENSE_2_0),
                "https://github.com/dcendents/android-maven-gradle-plugin"));

        mAdapter.setItems(items);
    }

    public String getAppVersion() {
        PackageManager manager = this.getPackageManager();
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info.versionName;
    }
}
