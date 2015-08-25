package com.example.andrewpark.popularmovies.model;

import android.database.Cursor;

import com.example.andrewpark.popularmovies.MovieListActivityFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andrewpark on 8/15/15.
 */
public class Movie {

    public int id;
    public String poster_path;
    public String overview;
    public String original_title;
    public String release_date;
    public double vote_average;

    final String POSTER_PATH = "poster_path";
    final String MOVIE_ID = "id";
    final String MOVIE_OVERVIEW = "overview";
    final String MOVIE_ORIGINAL_TITLE = "original_title";
    final String MOVIE_VOTE_AVERAGE = "vote_average";
    final String MOVIE_RELEASE_DATE = "release_date";

    public Movie(JSONObject jsonObject) throws JSONException {
        if (jsonObject!=null) {
            id = jsonObject.getInt(MOVIE_ID);
            poster_path = jsonObject.getString(POSTER_PATH);
            overview = jsonObject.getString(MOVIE_OVERVIEW);
            original_title = jsonObject.getString(MOVIE_ORIGINAL_TITLE);
            release_date = jsonObject.getString(MOVIE_RELEASE_DATE);
            vote_average = jsonObject.getDouble(MOVIE_VOTE_AVERAGE);
        }
    }

    public Movie(Cursor cursor) {
        this.id = cursor.getInt(MovieListActivityFragment.COL_MOVIE_ID);
        this.original_title = cursor.getString(MovieListActivityFragment.COL_TITLE);
        this.poster_path = cursor.getString(MovieListActivityFragment.COL_IMAGE);
        this.overview = cursor.getString(MovieListActivityFragment.COL_OVERVIEW);
        this.vote_average = cursor.getInt(MovieListActivityFragment.COL_RATING);
        this.release_date = cursor.getString(MovieListActivityFragment.COL_DATE);
    }
}
