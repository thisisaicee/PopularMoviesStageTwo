package net.aicee.popularmoviesstagetwo.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import net.aicee.popularmoviesstagetwo.model.MiniMovie;
import net.aicee.popularmoviesstagetwo.model.Movie;
import java.util.List;

@Dao
public interface MovieDAO {
    @Query("SELECT * FROM movie")
    LiveData<List<Movie>> getAll();

    @Query("SELECT movieId FROM movie WHERE movieId = :id")
    MiniMovie getMovieById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Movie movie);

    @Delete
    void delete(Movie movie);
}