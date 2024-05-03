package com.shua.likegank.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.shua.likegank.R;
import com.shua.likegank.databinding.ActivityWebViewBinding;
import com.shua.likegank.utils.AppUtils;


public class WebActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "URL";
    private static final String EXTRA_TITLE = "TITLE";

    private ActivityWebViewBinding viewBinding;

    private String mUrl;

    private WebView mWebView;

    public static Intent newIntent(Context context, String url, String title) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_TITLE, title);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWebViewBinding viewBinding = contentView();
        setContentView(viewBinding.getRoot());
        Toolbar toolbar = viewBinding.toolbar;
        setSupportActionBar(toolbar);
        initViews(viewBinding);
    }

    protected ActivityWebViewBinding contentView() {
        return ActivityWebViewBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected void initViews(ActivityWebViewBinding viewBinding) {
        this.viewBinding = viewBinding;
        mWebView = viewBinding.webView;
        setTitle(getIntent().getStringExtra(EXTRA_TITLE));

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        mUrl = getIntent().getStringExtra(EXTRA_URL);
        mWebView.loadUrl(mUrl);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            mWebView.reload();
            return true;
        } else if (id == R.id.action_copy_url) {
            AppUtils.copyToClipBoard(this, mWebView.getUrl());
            return true;
        } else if (id == R.id.action_open_url) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(mUrl);
            intent.setData(uri);
            if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        mWebView = null;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !mWebView.canGoBack()) {
            mWebView.destroy();
            mWebView = null;
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {
        @SuppressLint("WrongConstant")
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                viewBinding.progressBar.setVisibility(GONE);
            } else {
                if (viewBinding.progressBar.getVisibility() == GONE) {
                    viewBinding.progressBar.setVisibility(VISIBLE);
                }
                viewBinding.progressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }
}
