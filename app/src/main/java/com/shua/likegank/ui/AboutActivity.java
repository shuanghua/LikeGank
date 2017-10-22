package com.shua.likegank.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.shua.likegank.R;
import com.shua.likegank.ui.base.BasePresenter;
import com.shua.likegank.ui.base.ToolbarActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class AboutActivity extends ToolbarActivity {
    @BindView(R.id.about_version)
    TextView mAboutVersion;

    private PackageInfo mInfo;

    private String getAppVersion() {
        PackageManager manager = this.getPackageManager();
        try {
            mInfo = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return mInfo.versionName;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @Override
    protected int contentView() {
        return R.layout.activity_about;
    }

    @Override
    protected void initViews() {
        setTitle(getResources().getString(R.string.about));
        mAboutVersion.setText("当前版本 V" + getAppVersion());
    }

    @Override
    protected boolean addBack() {
        return true;
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @OnClick({R.id.about_likegank, R.id.about_gank, R.id.about_meizi, R.id.about_open_license})
    public void onClick(View view) {
        Intent intent = new Intent();
        Uri uri;
        switch (view.getId()) {
            case R.id.about_likegank:
                intent.setAction(Intent.ACTION_VIEW);
                uri = Uri.parse(getString(R.string.likegank_github));
                intent.setData(uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.about_gank:
                intent.setAction(Intent.ACTION_VIEW);
                uri = Uri.parse(getString(R.string.gank_link));
                intent.setData(uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.about_meizi:
                intent.setAction(Intent.ACTION_VIEW);
                uri = Uri.parse(getString(R.string.meizi_github));
                intent.setData(uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.about_open_license:
                startActivity(LicenseActivity.newIntent(this));
                break;
        }
    }
}
