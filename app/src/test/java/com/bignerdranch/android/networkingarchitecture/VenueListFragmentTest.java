package com.bignerdranch.android.networkingarchitecture;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.android.networkingarchitecture.controller.VenueListActivity;
import com.bignerdranch.android.networkingarchitecture.controller.VenueListFragment;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;
import com.bignerdranch.android.networkingarchitecture.web.DataManager;
import com.bignerdranch.android.networkingarchitecture.web.VenueListDeserializer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by SamMyxer on 4/4/16.
 *
 * When you create a WireMockRule, it will handle setting up WireMock for each test case
 * It creates a local server using the integer parameter as the port number
 *
 * Set up the RestAdapter to point to fake endpoint that matched wire mock rule
 *
 * SynchronousExecutor just makes sure we run everything on main thread
 * first parameter is for web requests, and second parameter is for callbacks
 *
 * stubFor invokes WireMock to fake a response for a particular URL
 * configure the response in the willReturn method
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP, constants = BuildConfig.class)
public class VenueListFragmentTest {

    @Rule
    public WireMockRule mWireMockRule = new WireMockRule(1111);
    private String mEndpoint = "http://localhost:1111";
    private DataManager mDataManager;
    private VenueListActivity mVenueListActivity;
    private VenueListFragment mVenueListFragment;

    @Before
    public void setUp() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(VenueSearchResponse.class,
                        new VenueListDeserializer())
                .create();
        SynchronousExecutor executor = new SynchronousExecutor();
        RestAdapter basicRestAdapter = new RestAdapter.Builder()
                .setEndpoint(mEndpoint)
                .setConverter(new GsonConverter(gson))
                .setExecutors(executor, executor)
                .build();
        mDataManager = DataManager.get(
                RuntimeEnvironment.application, basicRestAdapter, null);

        stubFor(get(urlMatching("/venues/search.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("search.json")));

        //Start fragment with an activity
        mVenueListActivity = Robolectric.buildActivity(VenueListActivity.class)
                .create().start().resume().get();
        mVenueListFragment = (VenueListFragment) mVenueListActivity
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);

        //Start Fragment with Robolectric support library
//        mVenueListFragment = new VenueListFragment();
//        SupportFragmentTestUtil.startFragment(mVenueListFragment);
    }

    @Test
    public void activityListsVenuesReturnedFromSearch() {
        assertThat(mVenueListFragment, is(notNullValue()));
        RecyclerView venueRecyclerView = (RecyclerView) mVenueListFragment.getView()
                .findViewById(R.id.venueListRecyclerView);
        assertThat(venueRecyclerView, is(notNullValue()));
        assertThat(venueRecyclerView.getAdapter().getItemCount(), is(2));

        venueRecyclerView.measure(0, 0);
        venueRecyclerView.layout(0, 0, 100, 100);

        String bnrTitle = "BNR Intergalactic Headquarters";
        String rndTitle = "Ration and Dram";

        View firstVenueView = venueRecyclerView.getChildAt(0);
        TextView venueTitleTextView = (TextView) firstVenueView
                .findViewById(R.id.view_venue_list_VenueTitleTextView);
        assertThat(venueTitleTextView.getText(), is(equalTo(bnrTitle)));
        //Different way to check assertion, depends on preference
        //assertEquals(venueTitleTextView.getText(), bnrTitle);

        View secondVenueView = venueRecyclerView.getChildAt(1);
        TextView venueTitleTextView2 = (TextView) secondVenueView
                .findViewById(R.id.view_venue_list_VenueTitleTextView);
        assertThat(venueTitleTextView2.getText(), is(equalTo(rndTitle)));
        //Different way to check assertion, depends on preference
        //assertEquals(venueTitleTextView2.getText(), rndTitle);

    }

}
