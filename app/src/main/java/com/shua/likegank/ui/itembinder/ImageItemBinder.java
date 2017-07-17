package com.shua.likegank.ui.itembinder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shua.likegank.R;
import com.shua.likegank.data.entity.FuLi;
import com.shua.likegank.ui.PhotoViewActivity;

import me.drakeet.multitype.ItemViewBinder;


/**
 * FuLiViewProvider
 * Created by SHUA on 2017/2/28.
 */

public class ImageItemBinder extends ItemViewBinder<FuLi,
        ImageItemBinder.MeiziHolder> {

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
            , @NonNull FuLi data) {
        Context context = holder.mImageView.getContext();

        Glide.with(context)
                .load(data.url)
                .placeholder(R.mipmap.ic_bg_fuli)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.mImageView);

//        Glide.with(context)
//                .load(data.url + URL_MEIZI_DIMENSION)
//                .placeholder(R.mipmap.ic_bg_fuli)
//                .diskCacheStrategy(DiskCacheStrategy.RESULT)
//                .into(holder.mImageView);
        holder.position = getPosition(holder);
        holder.url = data.url;
    }

    static class MeiziHolder extends RecyclerView.ViewHolder {
        int position;
        String url;
        ImageView mImageView;

        MeiziHolder(View itemView) {
            super(itemView);
            this.mImageView = (ImageView) itemView.findViewById(R.id.fuli_image);
            itemView.setOnClickListener((View v) -> {
                itemView.getContext().startActivity(PhotoViewActivity
                        .newIntent(itemView.getContext(), url));
            });

            DisplayMetrics dm = mImageView.getContext().getResources().getDisplayMetrics();
            int mLikeImageW = dm.widthPixels / 2;
            int mLikeImageH = (int) (dm.heightPixels / 3);
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();
            params.width = mLikeImageW;
            params.height = mLikeImageH;
            mImageView.setLayoutParams(params);
        }
    }
}
