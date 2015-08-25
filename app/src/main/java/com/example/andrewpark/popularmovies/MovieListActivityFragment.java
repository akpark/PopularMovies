package com.example.andrewpark.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.andrewpark.popularmovies.adapters.MovieAdapter;
import com.example.andrewpark.popularmovies.data.MovieContract;
import com.example.andrewpark.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListActivityFragment extends Fragment {

    private static final String LOG_TAG = "LOG_TAG";

    public static ArrayList<Movie> movies = new ArrayList<Movie>();
    public static ArrayList<Movie> favorite_movies = new ArrayList<Movie>();
    private MovieAdapter mMovieAdapter;
    private MovieAdapter mFavMovieAdapter;
    private boolean fav_movie = false;

    //include on options menu


    private final String[] MOVIE_COLUMNS = {
//            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,

    };

//    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_IMAGE = 2;
    public static final int COL_DATE = 3;
    public static final int COL_RATING = 4;
    public static final int COL_OVERVIEW = 5;


    public MovieListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.v(LOG_TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        mMovieAdapter = new MovieAdapter(getActivity(), R.layout.grid_item_movie, R.id.gridview_movies, movies);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movies.get(position);
                int movie_position = movies.indexOf(movie);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class).putExtra(Intent.EXTRA_TEXT, movie_position);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieList();
        Log.v(LOG_TAG, "onStart");
    }

    public void updateMovieList() {
        FetchMovieList movieTask = new FetchMovieList();
        FetchFavMoviesTask fetchFavMoviesTask = new FetchFavMoviesTask(getActivity());

        String sort_by = Utility.getPreferredMovieSort(getActivity());
        if (sort_by.equals("favorites")) {
            fetchFavMoviesTask.execute();
        } else {
            movieTask.execute(sort_by);
        }
    }

    public class FetchMovieList extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMovieList.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            Log.v(LOG_TAG,"doInBackground");

            if (params.length == 0) {
                return null;
            }
            String sort_by_value = params[0];

            Log.v(LOG_TAG,"sort_by_value:" + sort_by_value);
            Log.v(LOG_TAG,"favorite_movies:" + favorite_movies.toString());

            if (sort_by_value.equals("favorites")) {
                movies.clear();
                for (Movie movie : favorite_movies) {
                    movies.add(movie);
                }
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;

            try {
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
                final String SORT_BY_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                //build uri
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, sort_by_value)
                        .appendQueryParameter(API_KEY_PARAM, getResources().getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                //send url request and connect
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //read input and input into movie Json String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        public void getMovieDataFromJson(String movieJSONStr) throws JSONException {

            if (movies != null) {
                movies.clear();
            }

            JSONObject movieJson = new JSONObject(movieJSONStr);
            JSONArray movieArray = movieJson.getJSONArray("results");

            for (int i=0; i<movieArray.length(); i++) {
                JSONObject jsonObject = movieArray.getJSONObject(i);
                Movie movie = new Movie(jsonObject);
                movies.add(movie);
            }

        }

        @Override
        protected void onPostExecute(ArrayList<Movie> newMovies) {
            mMovieAdapter.notifyDataSetChanged();
        }
    }


    public class FetchFavMoviesTask extends AsyncTask<Void,Void,Void> {

        private Context mContext;

        public FetchFavMoviesTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Cursor cursor = getActivity().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );

            Log.v(LOG_TAG,"cursor: " + cursor.toString());

            if (!movies.isEmpty()) {
                movies.clear();
            }

            if (cursor!=null && cursor.moveToFirst()) {
                //get cursor and create movie
                do {
                    Movie movie = new Movie(cursor);
                    movies.add(movie);
                } while (cursor.moveToNext());
                cursor.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mMovieAdapter.notifyDataSetChanged();
        }
    }

}
