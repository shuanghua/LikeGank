package com.shua.likegank.ui.itembinder;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shua.likegank.data.entity.IOS;
import com.shua.likegank.databinding.ItemContentBinding;
import com.shua.likegank.ui.WebActivity;

import me.drakeet.multitype.ItemViewBinder;

/**
 * IOSItemBinder
 * Created by SHUA on 2017/5/5.
 */
public class IOSItemBinder extends ItemViewBinder<IOS, IOSItemBinder.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater,
                                            @NonNull ViewGroup viewGroup) {
        return new ViewHolder(ItemContentBinding.inflate(inflater, viewGroup, false));
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

        ViewHolder(ItemContentBinding binding) {
            super(binding.getRoot());
            this.content = binding.itemContent;
            itemView.setOnClickListener(v -> v.getContext().startActivity(WebActivity
                    .newIntent(v.getContext(), url, title)));
        }
    }
}
