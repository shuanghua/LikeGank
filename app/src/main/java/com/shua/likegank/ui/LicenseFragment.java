package com.shua.likegank.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shua.likegank.data.Category;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.databinding.FragmentLincenseBinding;
import com.shua.likegank.interfaces.LicenseViewInterface;
import com.shua.likegank.presenters.LicensePresenter;
import com.shua.likegank.ui.base.BaseFragment;
import com.shua.likegank.ui.itembinder.CategoryItemBinder;
import com.shua.likegank.ui.itembinder.ContentItemBinder;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class LicenseFragment extends BaseFragment<FragmentLincenseBinding> implements LicenseViewInterface {

    private LicensePresenter mPresenter;
    private MultiTypeAdapter mAdapter;

    public static Intent newIntent(Context context) {
        return new Intent(context, LicenseFragment.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    protected void initData() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Category.class, new CategoryItemBinder());
        mAdapter.register(Content.class, new ContentItemBinder());
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(mAdapter);
        mPresenter.loadData();
    }

    @Override
    protected FragmentLincenseBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentLincenseBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new LicensePresenter(this);
    }

    @Override
    public void showData(Items result) {
        mAdapter.setItems(result);
    }
}
