package com.example.andrewpark.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.andrewpark.popularmovies.data.MovieContract;
import com.example.andrewpark.popularmovies.model.Movie;
import com.example.andrewpark.popularmovies.model.Review;
import com.example.andrewpark.popularmovies.model.Trailer;
import com.squareup.picasso.Picasso;

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
public class MovieDetailActivityFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();
    private Picasso mPicasso;
    private Movie movie;
    private ArrayList<Trailer> trailerList = new ArrayList<Trailer>();
    private ArrayList<Review> reviewList = new ArrayList<Review>();

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.v(LOG_TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        //Create the id's that link to the detail fragment
        TextView movie_title = (TextView)rootView.findViewById(R.id.detail_movie_title);
        ImageView movie_icon = (ImageView)rootView.findViewById(R.id.detail_movie_icon);
        TextView release_date = (TextView)rootView.findViewById(R.id.detail_movie_release_date);
        TextView rating = (TextView)rootView.findViewById(R.id.detail_movie_rating);
        Button mark_favorite = (Button)rootView.findViewById(R.id.detail_movie_mark_favorite);
        TextView overview = (TextView)rootView.findViewById(R.id.detail_movie_overview);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            int movie_position = intent.getIntExtra(Intent.EXTRA_TEXT, 0);
            movie = MovieListActivityFragment.movies.get(movie_position);
        }

        movie_title.setText(movie.original_title);
        mPicasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185/" + movie.poster_path).into(movie_icon);

        String release_date_substring = movie.release_date.substring(0, 4);

        release_date.setText(release_date_substring);
        rating.setText("" + movie.vote_average + "/10");
        mark_favorite.setText("Mark As \n Favorite");
        overview.setText(movie.overview);

        mark_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertFavMovie(movie);
//                if (!MovieListActivityFragment.favorite_movies.contains(movie))
//                    MovieListActivityFragment.favorite_movies.add(movie);
            }
        });

        Log.v(LOG_TAG, "Trailer Array: " + trailerList.toString());
        return rootView;
    }

    public void insertFavMovie(Movie movie) {
        ContentValues cv = new ContentValues();

        Log.v(LOG_TAG,"movie: " + movie.toString());

        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID,movie.id);
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,movie.original_title);
        cv.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH,movie.poster_path);
        cv.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,movie.release_date);
        cv.put(MovieContract.MovieEntry.COLUMN_RATING,movie.vote_average);
        cv.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,movie.overview);

        getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, cv);
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchTrailerTask fetchTrailerTask = new FetchTrailerTask();
        fetchTrailerTask.execute();
    }

    public class FetchTrailerTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            final String BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String VIDEOS = "/videos";
            final String REVIEWS = "/reviews";

            HttpURLConnection trailerurlConnection = null;
            HttpURLConnection reviewurlConnection = null;
            BufferedReader bufferedReader = null;
            String trailerJSONstr = null;
            String reviewJSONstr = null;

            try {

                URL trailerURL = new URL(BASE_URL + movie.id + VIDEOS + "?api_key="+getResources().getString(R.string.api_key));
                URL reviewURL = new URL(BASE_URL + movie.id + REVIEWS + "?api_key="+getResources().getString(R.string.api_key));

                trailerurlConnection = (HttpURLConnection) trailerURL.openConnection();
                trailerurlConnection.setRequestMethod("GET");
                trailerurlConnection.connect();

                InputStream inputStream = trailerurlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                trailerJSONstr = buffer.toString();

                reviewurlConnection = (HttpURLConnection) reviewURL.openConnection();
                reviewurlConnection.setRequestMethod("GET");
                reviewurlConnection.connect();

                InputStream inputStream1 = reviewurlConnection.getInputStream();
                StringBuffer buffer1 = new StringBuffer();
                if (inputStream1 == null) {
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream1));

                while((line = bufferedReader.readLine())!= null) {
                    buffer1.append(line + "\n");
                }

                if (buffer1.length() == 0) {
                    return null;
                }
                reviewJSONstr = buffer1.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                getTrailerData(trailerJSONstr);
                getReviewData(reviewJSONstr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void getTrailerData(String trailerJSONStr) throws JSONException {

            JSONObject trailerJSON = new JSONObject(trailerJSONStr);
            JSONArray trailerArray = trailerJSON.getJSONArray("results");

            for (int i=0; i<trailerArray.length(); i++) {
                JSONObject jsonObject = trailerArray.getJSONObject(i);
                Trailer trailer = new Trailer(jsonObject);
                trailerList.add(trailer);
            }
        }

        public void getReviewData(String reviewJSONStr) throws JSONException {

            JSONObject reviewJSON = new JSONObject(reviewJSONStr);
            JSONArray reviewArray = reviewJSON.getJSONArray("results");

            for (int i=0; i<reviewArray.length(); i++) {
                JSONObject jsonObject = reviewArray.getJSONObject(i);
                Review review = new Review(jsonObject);
                reviewList.add(review);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateTrailers();
            updateReviews();
        }

        public void updateTrailers() {
            LinearLayout trailer_linearLayout = (LinearLayout)getActivity().findViewById(R.id.detail_linearLayout_trailers);
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            for (Trailer trailer : trailerList) {

                View view = layoutInflater.inflate(R.layout.list_item_trailer, trailer_linearLayout, false);
                String trailer_name = trailer.name;
                final String trailer_key = trailer.key;
                TextView trailer_textView = (TextView)view.findViewById(R.id.trailer_text);
                ImageButton trailer_img_btn = (ImageButton)view.findViewById(R.id.play_btn);
                trailer_textView.setText(trailer_name);
                trailer_linearLayout.addView(view);
                Log.v(LOG_TAG, "Trailer name: " + trailer_name);
                trailer_img_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailer_key)));
                    }
                });
            }
            TextView trailer_title = (TextView)getActivity().findViewById(R.id.trailer_title);
            if (trailerList == null) {
                trailer_title.setVisibility(View.INVISIBLE);
            } else {
                trailer_title.setVisibility(View.VISIBLE);
            }
        }

        public void updateReviews() {
            LinearLayout review_linearLayout = (LinearLayout)getActivity().findViewById(R.id.detail_linearLayout_reviews);
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            for (Review review : reviewList) {
                View view = layoutInflater.inflate(R.layout.list_item_review, review_linearLayout, false);
                String content = review.content;
                String author = review.author;
                TextView review_textView = (TextView) view.findViewById(R.id.review_text);
                review_textView.setText(content);
                TextView author_textView = (TextView) view.findViewById(R.id.author_textview);
                author_textView.setText("- " + author);
                review_linearLayout.addView(view);
            }
            TextView review_title = (TextView)getActivity().findViewById(R.id.review_title);
            if (reviewList.isEmpty()) {
                review_title.setVisibility(View.INVISIBLE);
            } else {
                review_title.setVisibility(View.VISIBLE);
            }
        }
    }
}
