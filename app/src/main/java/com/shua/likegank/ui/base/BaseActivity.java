package com.shua.likegank.ui.base;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import butterknife.ButterKnife;

/**
 * BaseActivity
 * Created by SHUA on 2017/3/6.
 */

public abstract class BaseActivity<V, T extends BasePresenter<V>> extends AppCompatActivity {

    public static final String SP_NAME_PAGE = "NAME_PAGE";

    protected T mBasePresenter;
    protected SharedPreferences mPreferences;

    protected abstract T createPresenter();

    abstract protected int contentView();

    abstract protected void initViews();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contentView());
        ButterKnife.bind(this);
        mBasePresenter = createPresenter();
        initViews();
        mPreferences = getSharedPreferences(SP_NAME_PAGE, 0);
        if (mBasePresenter != null)
            mBasePresenter.attachView((V) this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBasePresenter != null)
            mBasePresenter.savePage(mPreferences);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBasePresenter != null) {
            mBasePresenter.unSubscribe();
            mBasePresenter.detachView();
        }
    }

    @SuppressLint("WrongConstant")
    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
