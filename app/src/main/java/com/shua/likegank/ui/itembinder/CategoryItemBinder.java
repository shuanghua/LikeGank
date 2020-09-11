package com.shua.likegank.ui.itembinder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shua.likegank.data.uimodel.Category;
import com.shua.likegank.databinding.ItemCategoryBinding;

import me.drakeet.multitype.ItemViewBinder;

/**
 * CategoryItemViewProvider
 * Created by SHUA on 2017/5/2.
 */

public class CategoryItemBinder
        extends ItemViewBinder<Category, CategoryItemBinder.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder
            (@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        return new ViewHolder(ItemCategoryBinding.inflate(layoutInflater, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, @NonNull Category category) {
        viewHolder.category.setText(category.getText());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView category;

        ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.category = binding.androidCategory;
            category.getPaint().setFakeBoldText(true);
        }
    }
}
