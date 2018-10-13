package net.aicee.popularmoviesstagetwo;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import net.aicee.popularmoviesstagetwo.adapter.MovieAdapter;
import net.aicee.popularmoviesstagetwo.databinding.ActivityMainBinding;
import net.aicee.popularmoviesstagetwo.model.Movie;
import net.aicee.popularmoviesstagetwo.utils.GridDecoration;
import net.aicee.popularmoviesstagetwo.utils.ScrollListener;
import net.aicee.popularmoviesstagetwo.viewModel.MainViewModel;
import java.util.List;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding activityMainBinding;
    private MovieAdapter movieAdapter;
    public static final int FAVORITES = 2;
    public static final int NO_INTERNET = 1;
    private static final String BUNDLE_PAGE = "page";
    private static final String BUNDLE_COUNT = "count";
    private static final String BUNDLE_RECYCLER = "recycler";
    private static final String BUNDLE_PREF = "pref";
    private ScrollListener scrollListener;
    private Bundle mSavedInstanceState;
    private GridLayoutManager gridLayoutManager;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        activityMainBinding.setPresenter(this);
        gridLayoutManager = new GridLayoutManager(this, numberOfColumns());
        movieAdapter = new MovieAdapter(this);
        activityMainBinding.moviesList.setLayoutManager(gridLayoutManager);
        activityMainBinding.moviesList.addItemDecoration(new GridDecoration(this));
        activityMainBinding.moviesList.setAdapter(movieAdapter);
        activityMainBinding.swipeRefreshLayout.setEnabled(false);

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        showInternetStatus();

        scrollListener = new ScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                int sorting = Preferences.getSorting(MainActivity.this);
                mainViewModel.loadMovies(sorting, page);
            }
        };

        activityMainBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainViewModel.getStatus().setValue(0);
                int sorting = Preferences.getSorting(MainActivity.this);
                populateUI(sorting);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_PAGE, scrollListener.getPage());
        outState.putInt(BUNDLE_COUNT, scrollListener.getCount());
        outState.putInt(BUNDLE_PREF, Preferences.getSorting(this));
        outState.putParcelable(BUNDLE_RECYCLER, gridLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    protected void onResume() {
        super.onResume();
        movieAdapter.refreshFavorite();
    }

    private void populateUI(int selected) {
        mainViewModel.getPopularMovies().removeObservers(MainActivity.this);
        mainViewModel.getHighestMovies().removeObservers(MainActivity.this);
        mainViewModel.getFavoriteMoviesData().removeObservers(MainActivity.this);

        movieAdapter.clearMovieList();
        hideStatus();

        switch (selected) {
            case 0:
                mainViewModel.getPopularMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                movieAdapter.addMoviesList(movies);
                            }
                        });
                break;
            case 1:
                mainViewModel.getHighestMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                movieAdapter.addMoviesList(movies);
                            }
                        });
                break;
            default:
                activityMainBinding.swipeRefreshLayout.setEnabled(false);
                mainViewModel.getFavoriteMoviesData().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                if (movieAdapter.getItemCount() < movies.size()) {
                                    hideStatus();
                                    movieAdapter.addMoviesList(movies);
                                } else if (movies.size() == 0) {
                                    showNoFavoriteStatus();
                                }
                            }
                        });
        }

        if (mSavedInstanceState != null && selected == mSavedInstanceState.getInt(BUNDLE_PREF)) {
            if (selected == FAVORITES) {
                activityMainBinding.moviesList.clearOnScrollListeners();
            } else {
                scrollListener.setState(
                        mSavedInstanceState.getInt(BUNDLE_PAGE),
                        mSavedInstanceState.getInt(BUNDLE_COUNT));
                activityMainBinding.moviesList.addOnScrollListener(scrollListener);
            }
            gridLayoutManager
                    .onRestoreInstanceState(mSavedInstanceState.getParcelable(BUNDLE_RECYCLER));
        } else {
            if (selected == FAVORITES) {
                activityMainBinding.moviesList.clearOnScrollListeners();
            } else {
                scrollListener.resetState();
                activityMainBinding.moviesList.addOnScrollListener(scrollListener);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.sort_spinner);
        Spinner spinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_spinner_list, R.layout.sort_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(Preferences.getSorting(MainActivity.this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int selected, long l) {
                Preferences.setSorting(MainActivity.this, selected);
                populateUI(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return true;
    }

    private int numberOfColumns() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
            if (width > 1000) {
                return 2;
            } else {
                return 2;
            }
        } else {
            if (width > 1700) {
                return 5;
            } else if (width > 1200) {
                return 4;
            } else {
                return 3;
            }
        }
    }


    private void showInternetStatus() {
        mainViewModel.getStatus().observe(MainActivity.this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer status) {
                int sorting = Preferences.getSorting(MainActivity.this);
                if (sorting != FAVORITES) {
                    activityMainBinding.swipeRefreshLayout.setRefreshing(false);
                    if (status == NO_INTERNET) {
                        activityMainBinding.statusImage.setImageResource(R.drawable.ic_signal_wifi_off_white_24px);
                        activityMainBinding.statusImage.setVisibility(View.VISIBLE);
                        activityMainBinding.swipeRefreshLayout.setEnabled(true);
                    } else {
                        activityMainBinding.swipeRefreshLayout.setEnabled(false);
                        hideStatus();
                    }
                }
            }
        });

    }
    /**
     * shows no favorite text view
     */
    private void showNoFavoriteStatus() {
        activityMainBinding.statusImage.setImageResource(R.drawable.ic_star_border_white_24px);
        activityMainBinding.statusImage.setVisibility(View.VISIBLE);

    }

    /**
     * hides status text view
     */
    private void hideStatus() {
        activityMainBinding.statusImage.setVisibility(View.INVISIBLE);

    }

    public void notThisStar() {
        Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();

    }
}