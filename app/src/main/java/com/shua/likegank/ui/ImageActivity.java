package com.shua.likegank.ui;

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.shua.likegank.R;
import com.shua.likegank.data.entity.Content;
import com.shua.likegank.interfaces.ImageViewInterface;
import com.shua.likegank.presenters.ImagePresenter;
import com.shua.likegank.ui.base.RefreshActivity;
import com.shua.likegank.ui.itembinder.ImageItemBinder;

import java.util.List;

import butterknife.BindView;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * NetWork to Realm to View
 * Created by SHUA on 2017/3/27.
 */
public class ImageActivity extends RefreshActivity implements ImageViewInterface {

    private final static int SPAN_COUNT = 3;
    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    private MultiTypeAdapter mAdapter;
    private ImagePresenter mPresenter;

    @Override
    protected void initViews() {
        setTitle(R.string.bar_title_image);
        final GridLayoutManager layoutManager =
                new GridLayoutManager(this, SPAN_COUNT);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Content.class, new ImageItemBinder());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new ImageSpaceItemDecoration
                (12, SPAN_COUNT, true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        mRecyclerView.setAdapter(mAdapter);

        showLoading();
        mPresenter.fromRealmLoad();
        refresh();
    }

    @Override
    protected void initPresenter() {
        mPresenter = new ImagePresenter(this);
    }

    @Override
    protected void refresh() {
        mPresenter.requestData(ImagePresenter.REQUEST_REFRESH);
    }

    private void bottomListener(LinearLayoutManager layoutManager) {
        int lastItemPosition, firstItemPosition, itemCount;
        itemCount = mAdapter.getItemCount();
        lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (lastItemPosition == itemCount - 1 && lastItemPosition - firstItemPosition > 0) {
            mPresenter.requestData(ImagePresenter.REQUEST_LOAD_MORE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideLoading();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.unSubscribe();
    }

    @Override
    public void showLoading() {
        setRefreshStatus(true);
    }

    @Override
    public void hideLoading() {
        setRefreshStatus(false);
    }

    @Override
    protected boolean addBack() {
        return true;
    }

    @Override
    protected int contentView() {
        return R.layout.activity_image;
    }

    private RecyclerView.OnScrollListener getOnBottomListener(GridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                bottomListener(layoutManager);
            }
        };
    }

    @Override
    public void showData(List<Content> result) {
        mAdapter.setItems(result);
        mAdapter.notifyDataSetChanged();
        hideLoading();
    }

    class ImageSpaceItemDecoration extends RecyclerView.ItemDecoration {
        int space;
        int count;
        boolean isEdge;

        ImageSpaceItemDecoration(int space, int count, boolean isEdge) {
            this.space = space;
            this.count = count;
            this.isEdge = isEdge;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view
                , @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % count; // item column

            if (isEdge) {
                outRect.left = space - column * space / count;
                outRect.right = (column + 1) * space / count;
                if (position < count) { // top edge
                    outRect.top = space;
                }
                outRect.bottom = space; // item bottom
            } else {
                outRect.left = column * space / count;
                outRect.right = space - (column + 1) * space / count;
                if (position >= count) {
                    outRect.top = space;
                }
            }
        }
    }
}
