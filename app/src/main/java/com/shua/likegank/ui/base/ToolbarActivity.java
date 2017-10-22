package com.shua.likegank.ui.base;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.shua.likegank.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ToolbarActivity
 * Created by SHUA on 2017/3/6.
 */

public abstract class ToolbarActivity extends BaseActivity {

    @BindView(R.id.app_bar_layout)
    protected AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    abstract protected boolean addBack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        initToolbar(addBack());
    }

    private void initToolbar(boolean isAddBack) {
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        if (mToolbar == null || mAppBarLayout == null) {
            throw new IllegalStateException(
                    getString(R.string.error_toolbar));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility
                    (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setSupportActionBar(mToolbar);
        if (isAddBack)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    public void setToolbarElevation(float toolbarElevation) {
        if (Build.VERSION.SDK_INT >= 21) {
            mAppBarLayout.setElevation(toolbarElevation);
        }
    }
}
