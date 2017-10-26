package com.shua.likegank.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import butterknife.ButterKnife;

/**
 * BaseActivity
 * Created by SHUA on 2017/3/6.
 */

public abstract class BaseActivity extends AppCompatActivity {

//    public static final String SP_NAME_PAGE = "NAME_PAGE";
//    public static final String SATE_KEY = "LIST_STATE_KEY";
//    public static final String FIRST_START = "FIRST_START";

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

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
