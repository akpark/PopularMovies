package com.example.andrewpark.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by andrewpark on 8/17/15.
 */
public class Utility {

    public static String getPreferredMovieSort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),context.getString(R.string.popularity_value));
    }

}
