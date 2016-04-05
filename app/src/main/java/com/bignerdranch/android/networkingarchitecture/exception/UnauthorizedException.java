package com.bignerdranch.android.networkingarchitecture.exception;
/**
 * This is a custom exception class for handling expired tokens
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(RuntimeException cause) {
        super(cause);
    }
}
