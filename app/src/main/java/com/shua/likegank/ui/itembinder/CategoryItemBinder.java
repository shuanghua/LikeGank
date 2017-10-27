package com.shua.likegank.ui.itembinder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shua.likegank.R;
import com.shua.likegank.data.Category;

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
        View view = layoutInflater.inflate
                (R.layout.item_category, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, @NonNull Category category) {
        viewHolder.category.setText(category.text);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextPaint paint;
        TextView category;

        ViewHolder(View itemView) {
            super(itemView);
            this.category = itemView.findViewById(R.id.android_category);
            paint = category.getPaint();
            paint.setFakeBoldText(true);
        }
    }
}
