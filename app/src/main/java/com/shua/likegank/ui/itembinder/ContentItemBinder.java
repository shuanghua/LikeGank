package com.shua.likegank.ui.itembinder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shua.likegank.data.entity.Content;
import com.shua.likegank.databinding.ItemContentBinding;
import com.shua.likegank.ui.WebActivity;

import me.drakeet.multitype.ItemViewBinder;

/**
 * ContentItemBinder for LicenseActivity
 * Created by moshu on 2017/5/7.
 */
public class ContentItemBinder
        extends ItemViewBinder<Content, ContentItemBinder.ViewHolder> {
    @NonNull
    @Override
    protected ContentItemBinder.ViewHolder onCreateViewHolder
            (@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        return new ViewHolder(ItemContentBinding.inflate(layoutInflater, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ContentItemBinder.ViewHolder viewHolder,
                                    @NonNull Content content) {
        viewHolder.content.setText(content.content);
        viewHolder.url = content.url;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView content;
        private String url;

        ViewHolder(ItemContentBinding binding) {
            super(binding.getRoot());
            this.content = binding.itemContent;
            itemView.setOnClickListener(view -> {
                if (!"".equals(url)) {
                    view.getContext().startActivity(
                            WebActivity.newIntent(view.getContext(), url, url)
                    );
                }
            });
        }
    }
}
