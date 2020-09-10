package com.shua.likegank.ui.itembinder;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.shua.likegank.R;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.databinding.ItemFuliBinding;
import com.shua.likegank.ui.PhotoFragment;

import me.drakeet.multitype.ItemViewBinder;


/**
 * ImageItemBinder
 * Created by SHUA on 2017/2/28.
 */
public class ImageItemBinder extends ItemViewBinder<Content, ImageItemBinder.ViewHolder> {

    /**
     * It is recommended to request a compressed image
     */
    //private static final String URL_MEIZI_DIMENSION = "?imageView2/0/w/100";
    private Context mContext;
    private RequestOptions options;


    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater,
                                            @NonNull ViewGroup parent) {
        ViewHolder holder = new ViewHolder(ItemFuliBinding.inflate(inflater, parent, false));
        mContext = holder.mImageView.getContext();
        options = new RequestOptions()
                .placeholder(R.mipmap.ic_bg_fuli)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Content data) {
        Glide.with(mContext)
                .load(data.url)
                .apply(options)
                .into(holder.mImageView);
        holder.position = getPosition(holder);
        holder.url = data.url;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        int position;
        String url;
        ImageView mImageView;

        ViewHolder(ItemFuliBinding binding) {
            super(binding.getRoot());
            this.mImageView = binding.fuliImage;
            itemView.setOnClickListener((View v) ->
                    itemView.getContext().startActivity(
                            PhotoFragment.newIntent(itemView.getContext(), url)
                    )
            );

            DisplayMetrics dm = mImageView.getContext().getResources().getDisplayMetrics();
            int mLikeImageW = dm.widthPixels / 2;
            int mLikeImageH = dm.heightPixels / 3;
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();
            params.width = mLikeImageW;
            params.height = mLikeImageH;
            mImageView.setLayoutParams(params);
        }
    }
}
