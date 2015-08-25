package com.example.andrewpark.popularmovies.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andrewpark on 8/21/15.
 */
public class Review {

    public String id;
    public String author;
    public String content;
    public String url;

    final static String REVIEW_ID = "id";
    final static String REVIEW_AUTHOR = "author";
    final static String REVIEW_CONTENT = "content";
    final static String REVIEW_URL = "url";

    public Review(JSONObject jsonObject) throws JSONException {
        if (jsonObject!=null) {
            this.id = jsonObject.getString(REVIEW_ID);
            this.author = jsonObject.getString(REVIEW_AUTHOR);
            this.content = jsonObject.getString(REVIEW_CONTENT);
            this.url = jsonObject.getString(REVIEW_URL);
        }
    }

}
