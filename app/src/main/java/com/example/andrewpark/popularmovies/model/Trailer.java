package com.example.andrewpark.popularmovies.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andrewpark on 8/20/15.
 */
public class Trailer {

    public String id;
    public String iso_639_1;
    public String key;
    public String name;
    public String site;
    public int size;
    public String type;

    final String TRAILER_ID = "id";
    final String TRAILER_ISO_639_1 = "iso_639_1";
    final String TRAILER_KEY = "key";
    final String TRAILER_NAME = "name";
    final String TRAILER_SITE = "site";
    final String TRAILER_SIZE = "size";
    final String TRAILER_TYPE= "type";

    public Trailer(JSONObject jsonObject) throws JSONException {
        if (jsonObject != null)
            id = jsonObject.getString(TRAILER_ID);
            iso_639_1 = jsonObject.getString(TRAILER_ISO_639_1);
            key = jsonObject.getString(TRAILER_KEY);
            name = jsonObject.getString(TRAILER_NAME);
            site = jsonObject.getString(TRAILER_SITE);
            size = jsonObject.getInt(TRAILER_SIZE);
            type = jsonObject.getString(TRAILER_TYPE);
    }

}
