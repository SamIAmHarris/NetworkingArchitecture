package com.bignerdranch.android.networkingarchitecture;

import android.os.Build;

import com.bignerdranch.android.networkingarchitecture.exception.UnauthorizedException;
import com.bignerdranch.android.networkingarchitecture.listener.VenueCheckInListener;
import com.bignerdranch.android.networkingarchitecture.listener.VenueSearchListener;
import com.bignerdranch.android.networkingarchitecture.model.TokenStore;
import com.bignerdranch.android.networkingarchitecture.model.Venue;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;
import com.bignerdranch.android.networkingarchitecture.web.DataManager;
import com.bignerdranch.android.networkingarchitecture.web.VenueInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import rx.Observable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by SamMyxer on 4/4/16.
 *
 * Mockito is a popular test double library for Java
 *
 * ArgumentCaptor is a test double that captures arguments passed
 * into the stub and allows you to call methods on those arguments
 *
 * Before/After get run between every test. Not just at the start/end of testing the class
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP, constants = BuildConfig.class)
public class DataManagerTest {

    @Captor
    private ArgumentCaptor<Callback<VenueSearchResponse>> mSearchCaptor;
    private DataManager mDataManager;
    private static RestAdapter mBasicRestAdapter = mock(RestAdapter.class);
    private static RestAdapter mAuthenticatedRestAdapter = mock(RestAdapter.class);
    private static VenueInterface mVenueInterface = mock(VenueInterface.class);
    private static VenueSearchListener mVenueSearchListener
            = mock(VenueSearchListener.class);
    private static VenueCheckInListener mVenueCheckInListener
            = mock(VenueCheckInListener.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mDataManager = DataManager.get(RuntimeEnvironment.application,
                mBasicRestAdapter, mAuthenticatedRestAdapter);

        when(mBasicRestAdapter.create(VenueInterface.class))
                .thenReturn(mVenueInterface);
        when(mAuthenticatedRestAdapter.create(VenueInterface.class))
                .thenReturn(mVenueInterface);

        mDataManager.addVenueSearchListener(mVenueSearchListener);
        mDataManager.addVenueCheckInListener(mVenueCheckInListener);
    }

    @After
    public void tearDown() {
        //Clear DataManager state in between tests
        reset(mBasicRestAdapter, mAuthenticatedRestAdapter, mVenueInterface,
                mVenueSearchListener, mVenueCheckInListener);
        mDataManager.removeVenueSearchListener(mVenueSearchListener);
        mDataManager.removeVenueCheckInListener(mVenueCheckInListener);
        mDataManager.clear();
    }

    @Test
    public void searchListenerTriggeredOnSuccessfulSearch() {
        mDataManager.fetchVenueSearch();

        verify(mVenueInterface).venueSearch(anyString(), mSearchCaptor.capture());

        VenueSearchResponse response = mock(VenueSearchResponse.class);
        mSearchCaptor.getValue().success(response, null);

        verify(mVenueSearchListener).onVenueSearchFinished();
    }

    @Test
    public void venueSearchListSavedOnSuccessfulSearch() {
        mDataManager.fetchVenueSearch();

        verify(mVenueInterface).venueSearch(anyString(), mSearchCaptor.capture());

        String firstVenueName = "Cool first name";
        Venue firstVenue = mock(Venue.class);
        when(firstVenue.getName()).thenReturn(firstVenueName);

        String secondVenueName = "awesome second venue";
        Venue secondVenue = mock(Venue.class);
        when(secondVenue.getName()).thenReturn(secondVenueName);

        List<Venue> venueList = new ArrayList<>();
        venueList.add(firstVenue);
        venueList.add(secondVenue);

        VenueSearchResponse response = mock(VenueSearchResponse.class);
        when(response.getVenueList()).thenReturn(venueList);

        mSearchCaptor.getValue().success(response, null);

        List<Venue> dataManagerVenueList = mDataManager.getVenueList();
        assertThat(dataManagerVenueList, is(equalTo(venueList)));
    }

    @Test
    public void checkInListenerTriggeredOnSuccessfulCheckIn() {
        Observable<Object> successObservable = Observable.just(new Object());
        when(mVenueInterface.venueCheckIn(anyString())).thenReturn(successObservable);

        String fakeVenueId = "fakeVenueId";
        mDataManager.checkInToVenue(fakeVenueId);

        verify(mVenueCheckInListener).onVenueCheckInFinished();
    }

    @Test
    public void checkInListenerNotifiesTokenExpiredOnUnauthorizedException() {
        Observable<Object> unauthorizedObservable =
                Observable.error(new UnauthorizedException(null));
        when(mVenueInterface.venueCheckIn(anyString()))
                .thenReturn(unauthorizedObservable);

        String fakeVenueId = "fakeVenueId";
        mDataManager.checkInToVenue(fakeVenueId);

        verify(mVenueCheckInListener).onTokenExpired();
    }

    @Test
    public void checkInListenerDoesNotNotifyTokenExpiredOnPlainException() {
        Observable<Object> runtimeObservable =
                Observable.error(new RuntimeException());
        when(mVenueInterface.venueCheckIn(anyString()))
                .thenReturn(runtimeObservable);

        String fakeVenueId = "fakeVenueId";
        mDataManager.checkInToVenue(fakeVenueId);

        verify(mVenueCheckInListener, never()).onTokenExpired();
    }

    @Test
    public void tokenClearedFromTokenStoreOnUnauthorizedException() {
        String testToken = "oooohyeeeaah";
        TokenStore tokenStore = TokenStore.get(RuntimeEnvironment.application);
        tokenStore.setAccessToken(testToken);
        assertThat(tokenStore.getAccessToken(), is(equalTo(testToken)));

        Observable<Object> unauthorizedObservable =
                Observable.error(new UnauthorizedException(null));
        when(mVenueInterface.venueCheckIn(anyString()))
                .thenReturn(unauthorizedObservable);

        String fakeVenueId = "fakeVenueId";
        mDataManager.checkInToVenue(fakeVenueId);

        assertThat(tokenStore.getAccessToken(), is(equalTo(null)));
    }
}
