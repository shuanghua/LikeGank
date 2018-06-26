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
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.shua.likegank.R;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.ui.PhotoViewActivity;

import me.drakeet.multitype.ItemViewBinder;


/**
 * ImageItemBinder
 * Created by SHUA on 2017/2/28.
 */

public class ImageItemBinder extends ItemViewBinder<Content, ImageItemBinder.MeiziHolder> {

    /**
     * It is recommended to request a compressed image
     */
    //private static final String URL_MEIZI_DIMENSION = "?imageView2/0/w/100";
    private Context mContext;
    private RequestOptions options;


    @NonNull
    @Override
    protected MeiziHolder onCreateViewHolder(@NonNull LayoutInflater inflater
            , @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.item_fuli, parent, false);
        MeiziHolder holder = new MeiziHolder(root);
        mContext = holder.mImageView.getContext();
        options = new RequestOptions()
                .placeholder(R.mipmap.ic_bg_fuli)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull MeiziHolder holder, @NonNull Content data) {
        Glide.with(mContext)
                .load(data.url)
                .apply(options)
                .into(holder.mImageView);
        holder.position = getPosition(holder);
        holder.url = data.url;
    }

    static class MeiziHolder extends RecyclerView.ViewHolder {
        int position;
        String url;
        ImageView mImageView;

        MeiziHolder(View itemView) {
            super(itemView);
            this.mImageView = itemView.findViewById(R.id.fuli_image);
            itemView.setOnClickListener((View v) ->
                    itemView.getContext().startActivity(
                            PhotoViewActivity.newIntent(itemView.getContext(), url)
                    )
            );

            DisplayMetrics dm = mImageView
                    .getContext()
                    .getResources()
                    .getDisplayMetrics();
            int mLikeImageW = dm.widthPixels / 2;
            int mLikeImageH = dm.heightPixels / 3;
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();
            params.width = mLikeImageW;
            params.height = mLikeImageH;
            mImageView.setLayoutParams(params);
        }
    }
}
