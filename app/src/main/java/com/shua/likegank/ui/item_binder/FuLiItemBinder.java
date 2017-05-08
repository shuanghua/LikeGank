package com.shua.likegank.ui.item_binder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shua.likegank.R;
import com.shua.likegank.data.entity.MeiZi;
import com.shua.likegank.ui.PhotoViewActivity;

import me.drakeet.multitype.ItemViewBinder;

/**
 * FuLiViewProvider
 * Created by SHUA on 2017/2/28.
 */

public class FuLiItemBinder extends ItemViewBinder<MeiZi,
        FuLiItemBinder.MeiziHolder> {

    /**
     * It is recommended to request a compressed image
     */
    private static final String URL_MEIZI_DIMENSION = "?imageView2/0/w/100";

    @NonNull
    @Override
    protected MeiziHolder onCreateViewHolder(@NonNull LayoutInflater inflater
            , @NonNull ViewGroup parent) {
        View root = inflater
                .inflate(R.layout.item_fuli, parent, false);
        return new MeiziHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull MeiziHolder holder
            , @NonNull MeiZi data) {
        Glide.with(holder.mImageView.getContext())
                .load(data.url + URL_MEIZI_DIMENSION)
                .placeholder(R.mipmap.ic_bg_fuli)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.mImageView);
        holder.position = getPosition(holder);
        holder.url = data.url;
    }

    static class MeiziHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        int position;
        String url;

        MeiziHolder(View itemView) {
            super(itemView);
            this.mImageView = itemView.findViewById(R.id.fuli_image);
            itemView.setOnClickListener((View v) -> {
                itemView.getContext().startActivity(PhotoViewActivity
                        .newIntent(itemView.getContext(), url));
            });
        }
    }
}
