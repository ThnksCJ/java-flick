package com.thnkscj.flick.core.exceptions;

/**
 * Runtime exception thrown when a feature flag value cannot be converted
 * to the requested type.
 */
public class FlagTypeConversionException extends RuntimeException {

    public FlagTypeConversionException(String message) {
        super(message);
    }

    public FlagTypeConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
