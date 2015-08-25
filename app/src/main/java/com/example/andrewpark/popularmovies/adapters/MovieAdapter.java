package com.example.andrewpark.popularmovies.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.andrewpark.popularmovies.R;
import com.example.andrewpark.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by andrewpark on 8/15/15.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    Picasso mPicasso;

    public MovieAdapter(Context context, int resource, int textViewResourceId, List<Movie> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String SIZE = getContext().getResources().getString(R.string.image_size_default) + "/";
        final String POSTER_PATH;

        View v = convertView;

        if (v==null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.grid_item_movie,null);
        }

        Movie movie = getItem(position);


        if (movie != null) {
            ImageView movie_image = (ImageView)v.findViewById(R.id.grid_movie_image);

            POSTER_PATH = movie.poster_path;
            String URL_PATH = BASE_URL + SIZE + POSTER_PATH;
            Picasso.with(getContext()).load(URL_PATH).into(movie_image);
        }

        return v;

    }


}
