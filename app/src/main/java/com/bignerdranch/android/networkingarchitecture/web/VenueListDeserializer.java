package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by SamMyxer on 4/4/16.
 *
 * Cuts straight to the response object and skips the meta and notification json objects
 *
 * Could add model objects for the meta/notification but this avoids that unecessary code
 *
 * Also remember the VenueSearchResponse is actually a list of Venue objects since you are dealing with
 * an array of that data instead of just a simple object
 */
public class VenueListDeserializer implements JsonDeserializer<VenueSearchResponse> {

    @Override
    public VenueSearchResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonElement responseElement = json.getAsJsonObject().get("response");
        return new Gson().fromJson(responseElement, VenueSearchResponse.class);
    }

}
