package com.shua.likegank.ui.itembinder;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.shua.likegank.R;
import com.shua.likegank.data.entity.Girl;
import com.shua.likegank.databinding.ItemFuliBinding;
import com.shua.likegank.ui.GirlsFragmentDirections;

import me.drakeet.multitype.ItemViewBinder;


/**
 * ImageItemBinder
 * Created by SHUA on 2017/2/28.
 */
public class GirlsItemBinder extends ItemViewBinder<Girl, GirlsItemBinder.ViewHolder> {

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
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Girl data) {
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
            itemView.setOnClickListener((View v) -> {
                GirlsFragmentDirections.ActionNavGirlsToNavPhoto action
                        = GirlsFragmentDirections.actionNavGirlsToNavPhoto(url);
                Navigation.findNavController(v).navigate(action);
            });

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
