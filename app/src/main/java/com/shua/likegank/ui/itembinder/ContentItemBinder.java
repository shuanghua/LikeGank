package com.shua.likegank.ui.itembinder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shua.likegank.R;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.ui.WebViewActivity;

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
        View view = layoutInflater.inflate
                (R.layout.item_content, viewGroup, false);
        return new ContentItemBinder.ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ContentItemBinder.ViewHolder viewHolder
            , @NonNull Content content) {
        viewHolder.content.setText(content.content);
        viewHolder.url = content.url;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView content;
        private String url;

        ViewHolder(View itemView) {
            super(itemView);
            this.content = (TextView) itemView.findViewById(R.id.item_content);
            itemView.setOnClickListener(view -> {
                if (!"".equals(url)) {
                    view.getContext().startActivity(WebViewActivity
                            .newIntent(view.getContext(), url, url));
                }
            });
        }
    }
}
