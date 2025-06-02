package com.thnkscj.flick.core.exceptions;

public class FlagTypeConversionException extends RuntimeException {
    public FlagTypeConversionException(String message) {
        super(message);
    }

    public FlagTypeConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
