package net.aicee.popularmoviesstagetwo;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.aicee.popularmoviesstagetwo.adapter.ReviewAdapter;
import net.aicee.popularmoviesstagetwo.adapter.VideoAdapter;
import net.aicee.popularmoviesstagetwo.api.ApiResponse;
import net.aicee.popularmoviesstagetwo.data.AppDatabase;
import net.aicee.popularmoviesstagetwo.databinding.ActivityDetailBinding;
import net.aicee.popularmoviesstagetwo.model.MiniMovie;
import net.aicee.popularmoviesstagetwo.model.Movie;
import net.aicee.popularmoviesstagetwo.model.MovieDetail;
import net.aicee.popularmoviesstagetwo.model.Review;
import net.aicee.popularmoviesstagetwo.model.Video;
import net.aicee.popularmoviesstagetwo.api.MovieApiService;
import net.aicee.popularmoviesstagetwo.api.ServiceBuilder;
import net.aicee.popularmoviesstagetwo.utils.HorizontalDecoration;
import net.aicee.popularmoviesstagetwo.utils.AppExecutor;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {
    public static final String DETAIL_INTENT_KEY = "net.aicee.popularmoviesstagetwo.detail";
    public static final String MOVIE_NUMBER_KEY = "net.aicee.popularmoviesstagetwo.movie_number";

    private static final String BUNDLE_VIDEOS = "videos";
    private static final String BUNDLE_REVIEWS = "reviews";

    private ActivityDetailBinding activityDetailBinding;
    private AppDatabase appDatabase;
    private boolean isFavorite;
    private VideoAdapter videoAdapter;
    private ReviewAdapter reviewAdapter;
    private Target targetBackdrop;
    private Movie movie;
    private MovieApiService movieApiService;
    private Executor executor;
    private int movieNumber;
    private int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = displaymetrics.widthPixels;
            activityDetailBinding.collapsingToolbar.getLayoutParams().height = (int) Math.round(width / 1.5);
        }

        appDatabase = AppDatabase.getDatabase(this);
        movieApiService = ServiceBuilder.createService(MovieApiService.class);
        executor = new AppExecutor();
        Intent intent = getIntent();
        movieNumber = intent.getIntExtra(MOVIE_NUMBER_KEY, -1);
        movie = intent.getParcelableExtra(DETAIL_INTENT_KEY);

        activityDetailBinding.setMovie(movie);
        activityDetailBinding.setPresenter(this);

        setSupportActionBar(activityDetailBinding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        populateUI();
        populateVideos(savedInstanceState);
        populateReviews(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BUNDLE_VIDEOS, videoAdapter.getList());
        outState.putParcelableArrayList(BUNDLE_REVIEWS, reviewAdapter.getList());
    }

    private void populateUI() {
        targetBackdrop = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                activityDetailBinding.backdrop.setImageBitmap(bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@NonNull Palette palette) {
                        color = palette.getMutedColor(R.attr.colorPrimary) | 0xFF000000;
                        activityDetailBinding.collapsingToolbar.setContentScrimColor(color);
                        activityDetailBinding.collapsingToolbar.setStatusBarScrimColor(color);
                    }
                });
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        Picasso.get()
                .load("http://image.tmdb.org/t/p/w780" + movie.backdropPath)
                .into(targetBackdrop);

        Picasso.get()
                .load("http://image.tmdb.org/t/p/w342" + movie.posterPath)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(activityDetailBinding.movieDetails.poster);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                MiniMovie miniMovie = appDatabase.movieDao().getMovieById(movie.movieId);

                if (miniMovie != null) {
                    isFavorite = true;
                    activityDetailBinding.favoriteButton.setImageResource(R.drawable.ic_star_white_24px);
                } else {
                    isFavorite = false;
                    activityDetailBinding.favoriteButton.setImageResource(R.drawable.ic_star_border_white_24px);
                }
            }
        });
    }

    private void populateVideos(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        activityDetailBinding.movieVideos.videosList.setLayoutManager(layoutManager);
        activityDetailBinding.movieVideos.videosList.setHasFixedSize(true);
        activityDetailBinding.movieVideos.videosList.setNestedScrollingEnabled(false);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalDecoration(this);
        activityDetailBinding.movieVideos.videosList.addItemDecoration(itemDecoration);

        videoAdapter = new VideoAdapter(this);
        activityDetailBinding.movieVideos.videosList.setAdapter(videoAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_VIDEOS)) {
            videoAdapter.addVideosList(savedInstanceState.
                    <Video>getParcelableArrayList(BUNDLE_VIDEOS));
        } else {
            Call<ApiResponse<Video>> call = movieApiService.getVideos(movie.movieId);

            call.enqueue(new Callback<ApiResponse<Video>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Video>> call,
                                       @NonNull Response<ApiResponse<Video>> response) {
                    List<Video> result = response.body().results;
                    videoAdapter.addVideosList(result);
                    if (result.size() == 0) {
                        activityDetailBinding.movieVideos.videosLabel.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Video>> call, @NonNull Throwable t) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    private void populateReviews(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        activityDetailBinding.movieReviews.reviewsList.setLayoutManager(layoutManager);
        activityDetailBinding.movieReviews.reviewsList.setHasFixedSize(true);
        RecyclerView.ItemDecoration itemDecoration = new HorizontalDecoration(this);
        activityDetailBinding.movieReviews.reviewsList.addItemDecoration(itemDecoration);

        reviewAdapter = new ReviewAdapter(this);
        activityDetailBinding.movieReviews.reviewsList.setAdapter(reviewAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_REVIEWS)) {
            reviewAdapter.addReviewsList(savedInstanceState.<Review>getParcelableArrayList(BUNDLE_REVIEWS));
        } else {
            Call<ApiResponse<Review>> call = movieApiService.getReviews(movie.movieId);

            call.enqueue(new Callback<ApiResponse<Review>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Review>> call,
                                       @NonNull Response<ApiResponse<Review>> response) {
                    List<Review> result = response.body().results;
                    reviewAdapter.addReviewsList(result);
                    if (result.size() == 0) {
                        activityDetailBinding.movieReviews.reviewsLabel.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Review>> call, Throwable t) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    public void onClickFavoriteButton() {
        Preferences.setChangedMovie(this, movieNumber);
        if (isFavorite) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    appDatabase.movieDao().delete(movie);
                }
            });
            isFavorite = false;
            activityDetailBinding.favoriteButton.setImageResource(R.drawable.ic_star_border_white_24px);
            Toast.makeText(this, R.string.remove_from_favorites, Toast.LENGTH_SHORT).show();

        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    appDatabase.movieDao().insert(movie);
                }
            });
            isFavorite = true;
            activityDetailBinding.favoriteButton.setImageResource(R.drawable.ic_star_white_24px);
            Toast.makeText(this, R.string.add_to_favorites, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                String shareText = "https://www.themoviedb.org/movie/" + movie.movieId;
                ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                        .setText(shareText)
                        .setType("text/plain");
                try {
                    intentBuilder.startChooser();
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.no_suitable_app, Toast.LENGTH_LONG).show();
                }
                return true;
            case android.R.id.home:
                activityDetailBinding.favoriteButton.setVisibility(View.INVISIBLE);
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        activityDetailBinding.favoriteButton.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }

    public String formatReleaseDate(String releaseDate) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(releaseDate);
        } catch (ParseException e) {
            return releaseDate;
        }

        return DateFormat.getDateInstance(DateFormat.LONG).format(date);
    }

    public String getEnglishPlotSynopsis(String id) {
        Call<MovieDetail> call = movieApiService.getMovieById(id);

        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(@NonNull Call<MovieDetail> call, @NonNull Response<MovieDetail> response) {
                try {
                    String plotSynopsis = response.body().plotSynopsis;
                    activityDetailBinding.movieDetails.plotSynopsisTv.setText(plotSynopsis);
                } catch (NullPointerException e) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieDetail> call, @NonNull Throwable t) {
                Toast.makeText(DetailActivity.this,
                        getString(R.string.connection_error), Toast.LENGTH_LONG).show();
            }
        });

        return "";
    }

    public void onClickExpand(View view, Review review) {
        Intent intent = new Intent(this, ReviewActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this,
                        view,
                        ViewCompat.getTransitionName(view));
        intent.putExtra(ReviewActivity.REVIEW_INTENT_KEY, review);
        intent.putExtra(ReviewActivity.MOVIE_TITLE_KEY, movie.originalTitle);
        intent.putExtra(ReviewActivity.COLOR_ACTIONBAR_KEY, color);
        startActivity(intent, options.toBundle());
    }
}