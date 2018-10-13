package net.aicee.popularmoviesstagetwo.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import net.aicee.popularmoviesstagetwo.R;
import net.aicee.popularmoviesstagetwo.data.AppDatabase;
import net.aicee.popularmoviesstagetwo.api.ApiResponse;
import net.aicee.popularmoviesstagetwo.model.Movie;
import net.aicee.popularmoviesstagetwo.api.MovieApiService;
import net.aicee.popularmoviesstagetwo.api.ServiceBuilder;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.aicee.popularmoviesstagetwo.MainActivity.NO_INTERNET;

public class MainViewModel extends AndroidViewModel {
    private MutableLiveData<List<Movie>> popularMovies;
    private MutableLiveData<List<Movie>> highestMovies;
    private LiveData<List<Movie>> favoriteMoviesData;
    private MutableLiveData<Integer> status;
    private AppDatabase database;
    private MovieApiService apiClient;

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(getApplication());
        apiClient = ServiceBuilder.createService(MovieApiService.class);
    }

    public LiveData<List<Movie>> getPopularMovies() {
        if (popularMovies == null) {
            popularMovies = new MutableLiveData<>();
            loadMovies(0, 1);
        }
        return popularMovies;
    }

    public LiveData<List<Movie>> getHighestMovies() {
        if (highestMovies == null) {
            highestMovies = new MutableLiveData<>();
            loadMovies(1, 1);
        }
        return highestMovies;
    }

    public LiveData<List<Movie>> getFavoriteMoviesData() {
        if (favoriteMoviesData == null) {
            favoriteMoviesData = new MutableLiveData<>();
            getFavoritesFromDatabase();
        }
        return favoriteMoviesData;
    }

    public MutableLiveData<Integer> getStatus() {
        if (status == null) {
            status = new MutableLiveData<>();
            status.setValue(0);
        }
        return status;
    }

    public void loadMovies(int sorting, int page) {

        if (sorting == 0) {
            Call<ApiResponse<Movie>> call = apiClient.getPopularMovies(
                    getApplication().getString(R.string.language),
                    String.valueOf(page));

            call.enqueue(new Callback<ApiResponse<Movie>>() {
                @Override
                public void onResponse(Call<ApiResponse<Movie>> call,
                                       Response<ApiResponse<Movie>> response) {
                    if (response.isSuccessful()) {
                        List<Movie> result = response.body().results;
                        List<Movie> value = popularMovies.getValue();
                        if (value == null || value.isEmpty()) {
                            popularMovies.setValue(result);
                        } else {
                            value.addAll(result);
                            popularMovies.setValue(value);
                        }
                        status.setValue(0);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                    popularMovies = null;
                    status.setValue(NO_INTERNET);
                }
            });
        } else if (sorting == 1) {
            Call<ApiResponse<Movie>> call = apiClient.getTopRatedMovies(
                    getApplication().getString(R.string.language),
                    String.valueOf(page));

            call.enqueue(new Callback<ApiResponse<Movie>>() {
                @Override
                public void onResponse(Call<ApiResponse<Movie>> call,
                                       Response<ApiResponse<Movie>> response) {
                    if (response.isSuccessful()) {
                        List<Movie> result = response.body().results;
                        List<Movie> value = highestMovies.getValue();
                        if (value == null || value.isEmpty()) {
                            highestMovies.setValue(result);
                        } else {
                            value.addAll(result);
                            highestMovies.setValue(value);
                        }
                        status.setValue(0);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                    highestMovies = null;
                    status.setValue(NO_INTERNET);
                }
            });
        }
    }

    private void getFavoritesFromDatabase() {
        favoriteMoviesData = database.movieDao().getAll();
    }
}