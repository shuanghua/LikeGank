package com.shua.likegank.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.shua.likegank.R;
import com.shua.likegank.databinding.FragmentAboutBinding;
import com.shua.likegank.ui.base.BaseFragment;

public class AboutFragment extends BaseFragment<FragmentAboutBinding> {

    private PackageInfo mInfo;
    private Uri uri;
    private Intent intent = new Intent();

    public static Intent newIntent(Context context) {
        return new Intent(context, AboutFragment.class);
    }

    @Override
    protected FragmentAboutBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAboutBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initPresenter() {
    }

    private String getAppVersion() {
        PackageManager manager = requireActivity().getPackageManager();
        try {
            mInfo = manager.getPackageInfo(requireActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return mInfo.versionName;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initData(FragmentAboutBinding.bind(view));
        initData();
    }

    @SuppressLint("SetTextI18n")
    protected void initData() {
        binding.aboutVersion.setText("当前版本 V" + getAppVersion());
        binding.aboutLikegank.setOnClickListener(v -> {
            intent.setAction(Intent.ACTION_VIEW);
            uri = Uri.parse(getString(R.string.likegank_github));
            intent.setData(uri);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        binding.aboutGank.setOnClickListener(v -> {
            intent.setAction(Intent.ACTION_VIEW);
            uri = Uri.parse(getString(R.string.gank_link));
            intent.setData(uri);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        binding.aboutGirls.setOnClickListener(v -> {
            intent.setAction(Intent.ACTION_VIEW);
            uri = Uri.parse(getString(R.string.meizi_github));
            intent.setData(uri);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        });


        //TODO("导航到 LicenseFragment")
        binding.aboutLicense.setOnClickListener(v -> {
            NavDirections action = AboutFragmentDirections.actionNavAboutToNavLicense();
            Navigation.findNavController(v).navigate(action);
        });
    }
}
