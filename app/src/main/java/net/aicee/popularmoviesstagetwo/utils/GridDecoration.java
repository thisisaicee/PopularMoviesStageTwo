package net.aicee.popularmoviesstagetwo.utils;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.aicee.popularmoviesstagetwo.R;

public class GridDecoration extends RecyclerView.ItemDecoration {

    private final Context context;

    public GridDecoration(Context context) {
        this.context = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager.LayoutParams layoutParams
                = (GridLayoutManager.LayoutParams) view.getLayoutParams();

        int position = layoutParams.getViewLayoutPosition();
        if (position == RecyclerView.NO_POSITION) {
            outRect.set(0, 0, 0, 0);
            return;
        }

        int itemDivider = context.getResources().getDimensionPixelSize(R.dimen.grid_item_divider);
        outRect.top = itemDivider;
        outRect.bottom = itemDivider;
        outRect.left = itemDivider;
        outRect.right = itemDivider;
    }
}