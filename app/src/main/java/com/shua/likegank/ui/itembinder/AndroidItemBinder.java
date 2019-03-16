package com.shua.likegank.ui.itembinder;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

public class AndroidItemBinder extends ItemViewBinder<Android, AndroidItemBinder.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater,
                                            @NonNull ViewGroup viewGroup) {
        View view = layoutInflater.inflate(R.layout.item_content, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, @NonNull Android android) {

        SpannableString span = new SpannableString(new StringBuilder()
                .append(android.content)
                .append("(via-")
                .append(android.author)
                .append(")"));
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#9e9e9e"))
                , android.content.length()
                , span.length()
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        viewHolder.content.setText(span);
        viewHolder.url = android.url;
        viewHolder.title = android.content;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        String url, title;
        TextView content;

        ViewHolder(View itemView) {
            super(itemView);
            this.content = itemView.findViewById(R.id.item_content);
            itemView.setOnClickListener(v -> v.getContext()
                    .startActivity(WebViewActivity.newIntent(v.getContext(), url, title)));
        }
    }
}
