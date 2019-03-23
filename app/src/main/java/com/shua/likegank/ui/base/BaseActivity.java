package com.shua.likegank.ui.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;

/**
 * BaseActivity
 * Created by SHUA on 2017/3/6.
 */

public abstract class BaseActivity extends AppCompatActivity {

    abstract protected int contentView();

    abstract protected void initViews();

    abstract protected void initPresenter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contentView());
        ButterKnife.bind(this);
        initPresenter();
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
