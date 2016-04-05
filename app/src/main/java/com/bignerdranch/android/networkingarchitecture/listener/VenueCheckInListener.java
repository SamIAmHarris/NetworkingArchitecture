package com.bignerdranch.android.networkingarchitecture.listener;

/**
 * Created by SamMyxer on 4/4/16.
 * Checking into a venue is an authenticated request
 * Any authenticated request can return the unauthorized exception so any listener
 * interfaces for authenticated requests should extend the AuthenticationListener
 */
public interface VenueCheckInListener extends AuthenticationListener{
    void onVenueCheckInFinished();
}
