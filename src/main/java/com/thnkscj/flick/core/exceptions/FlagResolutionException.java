package com.thnkscj.flick.core.exceptions;

/**
 * Runtime exception thrown when a feature flag cannot be resolved,
 * for example, due to missing configuration or lookup failures.
 */
public class FlagResolutionException extends RuntimeException {
    public FlagResolutionException(String message) {
        super(message);
    }

    public FlagResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
