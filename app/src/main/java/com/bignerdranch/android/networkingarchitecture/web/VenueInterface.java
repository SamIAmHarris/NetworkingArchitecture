package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by SamMyxer on 4/4/16.
 *
 * @Get is a retrofit annotation to make a get request
 *
 * @Query adds that parameter as a query param
 *
 * When have a callback object then the call will be async
 */
public interface VenueInterface {

    @GET("/venues/search")
    void venueSearch(@Query("ll") String latLngString,
                     Callback<VenueSearchResponse> callback);

    @GET("/venues/search")
    void venueLocationSearch(@Query("near") String nearString,
                     Callback<VenueSearchResponse> callback);

    //Using the post annotation to make the post request
    ////@Field parameters should be encoded and sent to the server as POST parameters
    @FormUrlEncoded
    @POST("/checkins/add")
    Observable<Object> venueCheckIn(@Field("venueId") String venueId);

}
