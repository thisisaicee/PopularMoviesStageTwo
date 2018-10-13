package net.aicee.popularmoviesstagetwo.utils;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.aicee.popularmoviesstagetwo.R;

public class HorizontalDecoration extends RecyclerView.ItemDecoration {

    private final Context moviesContext;

    public HorizontalDecoration(Context context) {
        this.moviesContext = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            outRect.set(0, 0, 0, 0);
            return;
        }

        int itemDivider = moviesContext.getResources().getDimensionPixelSize(R.dimen.video_item_divider);
        int itemDividerSmall = moviesContext.getResources().getDimensionPixelSize(R.dimen.video_item_divider_small);

        outRect.top = itemDividerSmall;
        outRect.bottom = itemDividerSmall;

        if (position == 0) {
            outRect.left = itemDivider;
            outRect.right = itemDividerSmall;
        } else if (position == state.getItemCount() - 1) {
            outRect.left = itemDividerSmall;
            outRect.right = itemDivider;
        } else {
            outRect.left = itemDividerSmall;
            outRect.right = itemDividerSmall;
        }
    }
}