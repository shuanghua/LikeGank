package com.shua.likegank.ui.itembinder;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.shua.likegank.R;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.databinding.ItemHomeBinding;
import com.shua.likegank.ui.HomeFragmentDirections;
import com.shua.likegank.ui.WebActivity;
import com.shua.likegank.utils.AppUtils;

import me.drakeet.multitype.ItemViewBinder;

/**
 * FuLiViewProvider
 * Created by SHUA on 2017/4/17.
 */
public class HomeItemBinder extends ItemViewBinder<Home, HomeItemBinder.HomeHolder> {

    @NonNull
    @Override
    protected HomeHolder onCreateViewHolder(@NonNull LayoutInflater inflater
            , @NonNull ViewGroup viewGroup) {
        return new HomeHolder(ItemHomeBinding.inflate(inflater, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull HomeHolder holder, @NonNull Home home) {
        switch (home.type) {
            case "Android":
                holder.mImageView.setImageResource(R.mipmap.ic_menu_android);
                break;
            case "iOS":
                holder.mImageView.setImageResource(R.mipmap.ic_menu_ios);
                break;
            case "瞎推荐":
                holder.mImageView.setImageResource(R.mipmap.ic_recommend);
                break;
            case "福利":
                holder.mImageView.setImageResource(R.mipmap.ic_menu_fuli);
                break;
            case "拓展资源":
                holder.mImageView.setImageResource(R.mipmap.ic_web);
                break;
            case "休息视频":
                holder.mImageView.setImageResource(R.mipmap.ic_video);
                break;
            default:
                holder.mImageView.setImageResource(R.mipmap.likegank_launcher_round);
                break;
        }
        holder.mTextTime.setText(AppUtils.gankSubTimeString(home.createdAt));

        SpannableString span = new SpannableString(new StringBuilder()
                .append(home.title)
                .append("(via-")
                .append(home.who)
                .append(")"));
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#eeb211"))
                , home.title.length()
                , span.length()
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.mTextTitle.setText(span);
        holder.url = home.url;
        holder.title = home.title;
        holder.type = home.type;


        // set TextColor or use  SpannableStringBuilder.addend + StringStyles.format
//        SpannableStringBuilder builder = new SpannableStringBuilder(data.title)
//                .append(StringStyles.format(
//                        holder.gank.getContext(),
//                        " (by- " +
//                        data.who + ")",
//                        R.style.xxx));

    }

    static class HomeHolder extends RecyclerView.ViewHolder {
        private final ImageView mImageView;
        private final TextView mTextTitle;
        private final TextView mTextTime;
        private String url;
        private String title;
        private String type;

        HomeHolder(ItemHomeBinding binding) {
            super(binding.getRoot());
            this.mImageView = binding.homeImage;
            this.mTextTitle = binding.homeTitle;
            this.mTextTime = binding.homeTime;

            itemView.setOnClickListener(v -> {
                if ("Girl".equals(type)) {
                        HomeFragmentDirections.ActionNavHomeToNavPhoto action
                                = HomeFragmentDirections.actionNavHomeToNavPhoto(url);
                        Navigation.findNavController(v).navigate(action);
                } else {
                    itemView.getContext().startActivity(WebActivity
                            .newIntent(itemView.getContext(), url, title));
                }
            });
        }
    }
}
