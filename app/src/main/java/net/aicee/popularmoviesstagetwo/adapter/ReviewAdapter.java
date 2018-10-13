package net.aicee.popularmoviesstagetwo.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import net.aicee.popularmoviesstagetwo.databinding.ItemReviewBinding;
import net.aicee.popularmoviesstagetwo.model.Review;
import net.aicee.popularmoviesstagetwo.DetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private final Activity movieActivity;
    private List<Review> movieList;

    public ReviewAdapter(Activity activity) {
        this.movieActivity = activity;
    }

    @NonNull
    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewBinding binding = ItemReviewBinding.inflate(LayoutInflater.from(movieActivity), parent, false);
        return new ReviewAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapterViewHolder holder, int position) {
        Review review = movieList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        while(movieList == null){
            return 0;
        }

        return movieList.size();
    }

    public void addReviewsList(List<Review> reviewsList) {
        movieList = reviewsList;
        notifyDataSetChanged();
    }

    public ArrayList<Review> getList() {
        return (ArrayList<Review>) movieList;
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemReviewBinding binding;

        ReviewAdapterViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Review review) {
            binding.setReview(review);
            binding.setPresenter((DetailActivity) movieActivity);
        }
    }
}