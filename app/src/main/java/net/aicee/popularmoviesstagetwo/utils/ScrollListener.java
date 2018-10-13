package net.aicee.popularmoviesstagetwo.utils;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

abstract public class ScrollListener extends RecyclerView.OnScrollListener {

    private final GridLayoutManager mLayoutManager;
    private int previousItemCount = 10;
    private int page = 2;
    private boolean loading = true;

    protected ScrollListener(GridLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        int itemCount = mLayoutManager.getItemCount();
        int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();


        if (loading && (itemCount > previousItemCount)) {
            loading = false;
            previousItemCount = itemCount;
        }

        int threshold = 2;
        if (!loading && (lastVisibleItemPosition + threshold) > itemCount) {
            onLoadMore(++page);
            loading = true;
        }
    }

    public void resetState() {
        this.page = 1;
        this.previousItemCount = 0;
        this.loading = true;
    }

    public void setState(int page, int count) {
        this.page = page;
        this.previousItemCount = count;
        this.loading = false;
    }

    public int getCount() {
        return previousItemCount;
    }

    public int getPage() {
        return page;
    }

    public abstract void onLoadMore(int page);
}