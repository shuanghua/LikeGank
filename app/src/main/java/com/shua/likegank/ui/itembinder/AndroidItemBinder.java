package com.shua.likegank.ui.itembinder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shua.likegank.R;
import com.shua.likegank.data.entity.Android;
import com.shua.likegank.ui.WebViewActivity;

import me.drakeet.multitype.ItemViewBinder;

/**
 * ContentViewProvider
 * Created by SHUA on 2017/5/2.
 */

public class AndroidItemBinder
        extends ItemViewBinder<Android, AndroidItemBinder.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater,
                                            @NonNull ViewGroup viewGroup) {
        View view = layoutInflater.inflate(R.layout.item_content,
                viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, @NonNull Android android) {
        viewHolder.setData(android);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView content;
        String url;
        String title;

        ViewHolder(View itemView) {
            super(itemView);
            this.content = (TextView) itemView.findViewById(R.id.item_content);
            itemView.setOnClickListener(v -> v.getContext().startActivity(WebViewActivity
                    .newIntent(v.getContext(), url, title)));
        }

        public void setData(Android data) {
            content.setText(Html.fromHtml(data.content
                    + "<font color='#9e9e9e'>"
                    + content.getContext().getString(R.string.via) + data.author + ")"
                    + "</font>"));
            url = data.url;
            title = data.content;
        }
    }
}
