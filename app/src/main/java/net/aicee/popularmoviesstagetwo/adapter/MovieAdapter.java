package net.aicee.popularmoviesstagetwo.adapter;


import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.aicee.popularmoviesstagetwo.R;
import net.aicee.popularmoviesstagetwo.data.AppDatabase;
import net.aicee.popularmoviesstagetwo.Preferences;
import net.aicee.popularmoviesstagetwo.databinding.ItemMovieBinding;
import net.aicee.popularmoviesstagetwo.model.MiniMovie;
import net.aicee.popularmoviesstagetwo.model.Movie;
import net.aicee.popularmoviesstagetwo.DetailActivity;
import net.aicee.popularmoviesstagetwo.MainActivity;
import net.aicee.popularmoviesstagetwo.utils.AppExecutor;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {


    private AppDatabase appDatabase;
    private List<Movie> movieList;
    private Executor executor;
    private Activity movieActivity;

    public MovieAdapter(Activity activity) {
        this.movieActivity = activity;
        this.appDatabase = AppDatabase.getDatabase(activity);
        this.executor = new AppExecutor();
    }

    @Override
    @NonNull
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMovieBinding binding = ItemMovieBinding.inflate(LayoutInflater.from(movieActivity), parent, false);
        return new MovieAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {
        holder.bind(movieList.get(position));
    }

    @Override
    public int getItemCount() {
        while(movieList!=null){
            return movieList.size();

        }
       return 0;
    }

    public void clearMovieList() {

        while (movieList==null){
            movieList = new ArrayList<>();
        }
        int itemCount = movieList.size();
        movieList.clear();
        notifyItemRangeRemoved(0, itemCount);

    }

    public void addMoviesList(List<Movie> moviesList) {
        int positionStart = movieList.size();
        movieList.clear();

        movieList.addAll(moviesList);
        notifyItemRangeInserted(positionStart, moviesList.size() - positionStart);
    }

    public void refreshFavorite() {
        int movieNumber = Preferences.getChangedMovie(movieActivity);
        if (movieNumber != -1) {
            notifyItemChanged(movieNumber);
            Preferences.setChangedMovie(movieActivity, -1);
        }
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemMovieBinding binding;
        boolean isFavorite;

        MovieAdapterViewHolder(ItemMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Movie movie) {
            binding.setMovie(movie);
            binding.setPresenter(this);

            Picasso.get()
                    .load("http://image.tmdb.org/t/p/w342" + movie.posterPath)
                    .placeholder(R.drawable.scrim)
                    .error(R.drawable.error)
                    .into(binding.movieItemIv);

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final MiniMovie miniMovie = appDatabase.movieDao().getMovieById(movie.movieId);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (miniMovie != null) {
                                binding.favoriteIv.setImageResource(R.drawable.ic_star_white_24px);
                                isFavorite = true;
                            } else {
                                binding.favoriteIv.setImageResource(R.drawable.ic_star_border_white_24px);
                                isFavorite = false;
                            }
                        }
                    });
                }
            });
        }


        public void openMovieDetail(Movie movie) {
            int movieNumber = getAdapterPosition();

            Intent intent = new Intent(movieActivity, DetailActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(movieActivity,
                            binding.movieItemIv,
                            ViewCompat.getTransitionName(binding.movieItemIv));
            intent.putExtra(DetailActivity.DETAIL_INTENT_KEY, movie);
            intent.putExtra(DetailActivity.MOVIE_NUMBER_KEY, movieNumber);
            movieActivity.startActivity(intent, options.toBundle());
        }


        public void onClickFavorite(View view) {
            int position = getAdapterPosition();
            final Movie movie = movieList.get(position);

            if (isFavorite) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        appDatabase.movieDao().delete(movie);
                    }
                });
                isFavorite = false;
                binding.favoriteIv.setImageResource(R.drawable.ic_star_border_white_24px);
                Toast.makeText(movieActivity, R.string.remove_from_favorites, Toast.LENGTH_SHORT).show();


                if (Preferences.getSorting(movieActivity) == MainActivity.FAVORITES) {
                    movieList.remove(position);
                    notifyItemRemoved(position);
                }

            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        appDatabase.movieDao().insert(movie);
                    }
                });
                isFavorite = true;
                binding.favoriteIv.setImageResource(R.drawable.ic_star_white_24px);
                Toast.makeText(movieActivity, R.string.add_to_favorites, Toast.LENGTH_SHORT).show();
            }

        }
    }
}