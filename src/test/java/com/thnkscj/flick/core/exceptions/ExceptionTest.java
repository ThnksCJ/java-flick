package com.thnkscj.flick.core.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ExceptionTest {
    @Test
    void testFlagTypeConversionException() {
        FlagTypeConversionException ex = new FlagTypeConversionException("Test message");
        assertEquals("Test message", ex.getMessage());

        Throwable cause = new RuntimeException();
        FlagTypeConversionException exWithCause = new FlagTypeConversionException("Message", cause);
        assertEquals(cause, exWithCause.getCause());
    }

    @Test
    void testFlagResolutionException() {
        FlagResolutionException ex = new FlagResolutionException("Resolution failed");
        assertEquals("Resolution failed", ex.getMessage());
    }
}
