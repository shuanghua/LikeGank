package com.shua.likegank.ui.itembinder;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shua.likegank.R;
import com.shua.likegank.data.entity.IOS;
import com.shua.likegank.ui.WebViewActivity;

import me.drakeet.multitype.ItemViewBinder;

/**
 * IOSItemBinder
 * Created by SHUA on 2017/5/5.
 */

public class IOSItemBinder extends ItemViewBinder<IOS, IOSItemBinder.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater,
                                            @NonNull ViewGroup viewGroup) {
        View view = layoutInflater.inflate(R.layout.item_content, viewGroup, false);
        return new IOSItemBinder.ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull IOSItemBinder.ViewHolder viewHolder,
                                    @NonNull IOS ios) {
        SpannableString span = new SpannableString(new StringBuilder()
                .append(ios.content)
                .append("(via-")
                .append(ios.author)
                .append(")"));
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#9e9e9e"))
                , ios.content.length()
                , span.length()
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.content.setText(span);
        viewHolder.url = ios.url;
        viewHolder.title = ios.content;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        String url, title;

        ViewHolder(View itemView) {
            super(itemView);
            this.content = itemView.findViewById(R.id.item_content);
            itemView.setOnClickListener(v -> v.getContext().startActivity(WebViewActivity
                    .newIntent(v.getContext(), url, title)));
        }
    }
}
