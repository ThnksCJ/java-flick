package com.thnkscj.flick.core.exceptions;

public class FlagResolutionException extends RuntimeException {
    public FlagResolutionException(String message) {
        super(message);
    }

    public FlagResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
