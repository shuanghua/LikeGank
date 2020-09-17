package com.shua.likegank.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.shua.likegank.R;
import com.shua.likegank.databinding.ActivityMainBinding;
import com.shua.likegank.ui.base.OnLoadingVisibilityListener;

public class MainActivity extends AppCompatActivity implements OnLoadingVisibilityListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding mViewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());
        Toolbar toolbar = mViewBinding.appBarMain.toolbar;
        setSupportActionBar(toolbar);

        DrawerLayout drawer = mViewBinding.drawerLayout;
        NavigationView navigationView = mViewBinding.navView;
        navigationView.setItemIconTintList(null);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_android, R.id.nav_girls,
                R.id.nav_about, R.id.nav_photo, R.id.nav_license)
                .setOpenableLayout(drawer)
                .build();

//        NavController navController = Navigation.findNavController(this, R.id.fragment_container);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mViewBinding.appBarMain.loadingView.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment_container);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void showLoadingView(boolean isLoading) {
        ProgressBar loadingView = mViewBinding.appBarMain.loadingView;
        if (!isLoading) {
            loadingView.postDelayed(() ->
                    loadingView.setVisibility(View.GONE), 400);
        } else {
            loadingView.setVisibility(View.VISIBLE);
        }
    }
}