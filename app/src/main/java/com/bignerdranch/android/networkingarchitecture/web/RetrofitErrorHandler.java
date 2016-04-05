package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.exception.UnauthorizedException;

import java.net.HttpURLConnection;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by SamMyxer on 4/4/16.
 * Class receives the RetroFitError object and allows you to return your own Throwable
 * object for the specific error
 *
 * Check if response status has the 401 code and return an UnauthorizedException in that case
 */
public class RetrofitErrorHandler implements ErrorHandler {

    @Override
    public Throwable handleError(RetrofitError cause) {
        Response response = cause.getResponse();
        if(response != null &&
                response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return new UnauthorizedException(cause);
        }
        return cause;
    }

}
