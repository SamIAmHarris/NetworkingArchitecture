package com.bignerdranch.android.networkingarchitecture.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by SamMyxer on 4/4/16.
 *
 * Holds list of venues from the server
 */
public class VenueSearchResponse {
    @SerializedName("venues") List<Venue> mVenueList;

    public List<Venue> getVenueList() {
        return mVenueList;
    }
}
