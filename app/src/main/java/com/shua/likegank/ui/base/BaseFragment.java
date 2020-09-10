package com.shua.likegank.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

/**
 * BaseActivity
 * Created by SHUA on 2017/3/6.
 */

public abstract class BaseFragment<T extends ViewBinding> extends Fragment {

    protected T binding;

    abstract protected T viewBinding(LayoutInflater inflater, ViewGroup container);

    abstract protected void initPresenter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPresenter();
    }

    /**
     * 1: 获取 viewBinding
     * 2: 初始化 presenter
     * 3: 利用 viewBinding 获取布局中具体的 view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = viewBinding(inflater, container);
        }
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        // binding = null;
        super.onDestroy();
    }
}
