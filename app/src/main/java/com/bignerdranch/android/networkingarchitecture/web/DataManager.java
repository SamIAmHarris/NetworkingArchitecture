package com.bignerdranch.android.networkingarchitecture.web;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.bignerdranch.android.networkingarchitecture.exception.UnauthorizedException;
import com.bignerdranch.android.networkingarchitecture.listener.VenueCheckInListener;
import com.bignerdranch.android.networkingarchitecture.listener.VenueSearchListener;
import com.bignerdranch.android.networkingarchitecture.model.TokenStore;
import com.bignerdranch.android.networkingarchitecture.model.Venue;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by SamMyxer on 4/4/16.
 *
 * Handle all of the web requests for your application
 * static get method and the constructor is private: only one instance gets created for the application
 *
 * Singleton Setup
 *
 * Configure the GsonBuilder to use the custom VenueListDeserializer
 *
 * Set LogLevel to FULL to show headers, body, and metadata
 *
 * Use RequestInterceptor to add default data to each call (like headers, authtoken, queryparams, etc)
 *
 * Have to keep track of the search listeners and notify listeners when it successfully gets the VenueSearchResult
 */
public class DataManager {

    private static final String TAG = "DataManager";
    private static final String FOURSQUARE_ENDPOINT
            = "https://api.foursquare.com/v2";
    private static final String OAUTH_ENDPOINT
            = "https://foursquare.com/oauth2/authenticate";
    public static final String OAUTH_REDIRECT_URI
            ="http://www.bignerdranch.com";
    private static final String CLIENT_ID
            ="V5ZRBVCRWDLOOIEQSUEGTED3DA55EWWI4EOUR4QRVMG0P2IR";
    private static final String CLIENT_SECRET
            ="FHRRDZRJDCD30LCYV1MD0M1N5VDNTEWZ32XAJ5GL23IKZ3FE";
    private static final String FOURSQUARE_VERSION ="20150406";
    private static final String FOURSQUARE_MODE = "foursquare";
    private static final String SWARM_MODE = "swarm";
    private static final String TEST_LAT_LNG = "33.759,-84.332";
    private static final String TEST_LOCATION = "Austin, Tx";

    private List<Venue> mVenueList;
    private List<VenueSearchListener> mSearchListenerList;
    private List<VenueCheckInListener> mCheckInListenerList;

    private static DataManager sDataManager;
    private Context mContext;
    private static TokenStore sTokenStore;
    private RestAdapter mBasicRestAdapter;
    private RestAdapter mAuthenticatedRestAdapter;

    public static DataManager get(Context context) {
        if(sDataManager == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(VenueSearchResponse.class,
                            new VenueListDeserializer())
                    .create();

            RestAdapter basicRestAdapter = new RestAdapter.Builder()
                    .setEndpoint(FOURSQUARE_ENDPOINT)
                    .setConverter(new GsonConverter(gson))
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setRequestInterceptor(sRequestInterceptor)
                    .build();

            RestAdapter authenticatedRestAdapter = new RestAdapter.Builder()
                    .setEndpoint(FOURSQUARE_ENDPOINT)
                    .setConverter(new GsonConverter(gson))
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setRequestInterceptor(sAuthenticatedRequestInterceptor)
                    .setErrorHandler(new RetrofitErrorHandler())
                    .build();

            sDataManager = new DataManager(context, basicRestAdapter,
                    authenticatedRestAdapter);
        }
        return sDataManager;
    }

    public static DataManager get(Context context, RestAdapter basicRestAdapter,
                                  RestAdapter authenticatedRestAdapter) {
        if(sDataManager == null) {
            sDataManager = new DataManager(context, basicRestAdapter,
                    authenticatedRestAdapter);
        }
        return sDataManager;
    }

    private DataManager(Context context, RestAdapter basicRestAdapter,
                        RestAdapter authenticatedRestAdapter) {
        mContext = context.getApplicationContext();
        sTokenStore = TokenStore.get(mContext);
        mBasicRestAdapter = basicRestAdapter;
        mAuthenticatedRestAdapter = authenticatedRestAdapter;
        mSearchListenerList = new ArrayList<>();
        mCheckInListenerList = new ArrayList<>();
    }

    private static RequestInterceptor sRequestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addQueryParam("client_id", CLIENT_ID);
            request.addQueryParam("client_secret", CLIENT_SECRET);
            request.addQueryParam("v", FOURSQUARE_VERSION);
            request.addQueryParam("m", FOURSQUARE_MODE);
        }
    };

    //Added in the auth token to this request
    private static RequestInterceptor sAuthenticatedRequestInterceptor =
            new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam("oauth_token", sTokenStore.getAccessToken());
                    request.addQueryParam("v", FOURSQUARE_VERSION);
                    request.addQueryParam("m", SWARM_MODE);
                }
            };

    public void fetchVenueSearch() {
        VenueInterface venueInterface =
                mBasicRestAdapter.create(VenueInterface.class);
        venueInterface.venueSearch(TEST_LAT_LNG, new Callback<VenueSearchResponse>() {
            @Override
            public void success(VenueSearchResponse venueSearchResponse, Response response) {
                mVenueList = venueSearchResponse.getVenueList();
                notifySearchListeners();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Failed to fetch venue search", error);
            }
        });
    }

    public void fetchNearVenueSearch() {
        VenueInterface venueInterface =
                mBasicRestAdapter.create(VenueInterface.class);
        venueInterface.venueLocationSearch(TEST_LOCATION, new Callback<VenueSearchResponse>() {
            @Override
            public void success(VenueSearchResponse venueSearchResponse, Response response) {
                mVenueList = venueSearchResponse.getVenueList();
                notifySearchListeners();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Failed to fetch venue search", error);
            }
        });
    }

    public void checkInToVenue(String venueId) {
        VenueInterface venueInterface =
                mAuthenticatedRestAdapter.create(VenueInterface.class);

        //1st version: Normal Callbacks
        //code that used Callback to handle threading
//        venueInterface.venueCheckIn(venueId, new Callback<Object>() {
//            @Override
//            public void success(Object o, Response response) {
//                notifyCheckInListeners();
//            }
//
//            //Check if access token is invalid with the UnauthorizedException
//            //Clear it out if it is invalid
//            @Override
//            public void failure(RetrofitError error) {
//                Log.e(TAG, "Failed to check in to venue", error);
//                if (error.getCause() instanceof UnauthorizedException) {
//                    sTokenStore.setAccessToken(null);
//                    notifyCheckInListenersTokenExpired();
//                }
//            }
//        });

        //2nd Version: RxJava with Callbacks
        //code that uses RxJava to handle threading
        //observeOn will dictate which thread the success/failure actions will be called on
        //First Action1 is success callback
        //Second Action1 is failure callback
//        venueInterface.venueCheckIn(venueId)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<Object>() {
//                    @Override
//                    public void call(Object o) {
//                        notifyCheckInListeners();
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        Log.d("venueCheckIn", "Have error: " + throwable);
//                        if(throwable instanceof UnauthorizedException) {
//                            sTokenStore.setAccessToken(null);
//                            notifyCheckInListenersTokenExpired();
//                        } else {
//                            Toast.makeText(mContext, "Oops something went wrong.\nTry to check in again",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });

        //3rd Version: RxJava with Lambdas
        //Code that uses lambdas to replace anon inner classes
        venueInterface.venueCheckIn(venueId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> notifyCheckInListeners(),
                        error -> handleCheckInException(error)
                );
    }

    private void handleCheckInException(Throwable error) {
        if(error instanceof UnauthorizedException) {
            sTokenStore.setAccessToken(null);
            notifyCheckInListenersTokenExpired();
        }
    }

    private void notifyCheckInListenersTokenExpired() {
        for(VenueCheckInListener listener: mCheckInListenerList) {
            listener.onTokenExpired();
        }
    }

    private void notifyCheckInListeners() {
        for(VenueCheckInListener listener: mCheckInListenerList) {
            listener.onVenueCheckInFinished();
        }
    }

    public List<Venue> getVenueList() {
        return mVenueList;
    }

    public Venue getVenue(String venueId) {
        for (Venue venue: mVenueList) {
            if(venue.getId().equals(venueId)) {
                return venue;
            }
        }
        return null;
    }

    //This tell FourSquare a few things
    //Client id which app our user is authorizing
    //response type tell them to send an OAuth token back
    //redirect URI tells them where to send the token
    //redirect uri must match the one you entered in your app settings or the
    //authentication will not work
    public String getAuthenticationUrl() {
        return Uri.parse(OAUTH_ENDPOINT).buildUpon()
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("response_type", "token")
                .appendQueryParameter("redirect_uri", OAUTH_REDIRECT_URI)
                .build()
                .toString();
    }

    public void addVenueSearchListener(VenueSearchListener listener) {
        mSearchListenerList.add(listener);
    }

    public void removeVenueSearchListener(VenueSearchListener listener) {
        mSearchListenerList.remove(listener);
    }

    public void addVenueCheckInListener(VenueCheckInListener listener) {
        mCheckInListenerList.add(listener);
    }

    public void removeVenueCheckInListener(VenueCheckInListener listener) {
        mCheckInListenerList.remove(listener);
    }

    private void notifySearchListeners() {
        for(VenueSearchListener listener: mSearchListenerList) {
            listener.onVenueSearchFinished();
        }
    }

    public void clear() {
        sDataManager = null;
    }
}
