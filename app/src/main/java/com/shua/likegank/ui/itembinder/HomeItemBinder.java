package com.shua.likegank.ui.itembinder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shua.likegank.R;
import com.shua.likegank.data.entity.Home;
import com.shua.likegank.ui.PhotoViewActivity;
import com.shua.likegank.ui.WebViewActivity;
import com.shua.likegank.utils.LikeGankUtils;

import me.drakeet.multitype.ItemViewBinder;

/**
 * FuLiViewProvider
 * Created by SHUA on 2017/4/17.
 */

public class HomeItemBinder extends ItemViewBinder<Home,
        HomeItemBinder.HomeHolder> {

    @NonNull
    @Override
    protected HomeHolder onCreateViewHolder(@NonNull LayoutInflater inflater
            , @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.item_home, parent, false);
        return new HomeHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull HomeHolder holder
            , @NonNull Home data) {
        switch (data.type) {
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
                holder.mImageView.setImageResource(R.mipmap.ic_launcher);
                break;
        }
        holder.mTextTime.setText(LikeGankUtils.timeString(data.createdAt));
        holder.mTextTitle.setText(Html.fromHtml(data.title
                + "<font color='#eeb211'>"
                + holder.mTextTime.getContext().getString(R.string.via) + data.who + ")"
                + "</font>"));

        holder.url = data.url;
        holder.title = data.title;
        holder.type = data.type;

        // set TextColor or use  SpannableStringBuilder.addend + StringStyles.format
//        SpannableStringBuilder builder = new SpannableStringBuilder(data.title)
//                .append(StringStyles.format(
//                        holder.gank.getContext(),
//                        " (by- " +
//                        data.who + ")",
//                        R.style.xxx));

    }

    static class HomeHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTextTitle;
        private TextView mTextTime;
        private String url;
        private String title;
        private String type;

        HomeHolder(View itemView) {
            super(itemView);
            this.mImageView = itemView.findViewById(R.id.home_image);
            this.mTextTitle = itemView.findViewById(R.id.home_title);
            this.mTextTime = itemView.findViewById(R.id.home_time);

            itemView.setOnClickListener(v -> {
                switch (type) {
                    case "福利":
                        itemView.getContext().startActivity(PhotoViewActivity
                                .newIntent(itemView.getContext(), url));
                        break;
                    default:
                        itemView.getContext().startActivity(WebViewActivity
                                .newIntent(itemView.getContext(), url, title));
                        break;
                }
            });
        }
    }
}
