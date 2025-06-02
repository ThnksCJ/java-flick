package com.thnkscj.flick.core;

import static org.junit.jupiter.api.Assertions.*;

import com.thnkscj.flick.core.exceptions.FlagTypeConversionException;
import org.junit.jupiter.api.Test;

public class FlagValueTest {
    @Test
    void testBooleanConversion() {
        FlagValue trueVal = FlagValue.of(true);
        FlagValue falseVal = FlagValue.of(false);
        FlagValue stringTrue = FlagValue.of("true");
        FlagValue stringFalse = FlagValue.of("false");
        FlagValue numericOne = FlagValue.of(1);
        FlagValue numericZero = FlagValue.of(0);

        assertTrue(trueVal.asBoolean(false));
        assertFalse(falseVal.asBoolean(true));
        assertTrue(stringTrue.asBoolean(false));
        assertFalse(stringFalse.asBoolean(true));
        assertTrue(numericOne.asBoolean(false));
        assertFalse(numericZero.asBoolean(true));
    }

    @Test
    void testIntConversion() {
        FlagValue intVal = FlagValue.of(42);
        FlagValue stringVal = FlagValue.of("99");
        FlagValue doubleVal = FlagValue.of(3.14);

        assertEquals(42, intVal.asInt(0));
        assertEquals(99, stringVal.asInt(0));
        assertEquals(3, doubleVal.asInt(0));
    }

    @Test
    void testNullHandling() {
        FlagValue nullVal = FlagValue.nullValue();

        assertFalse(nullVal.asBoolean(false));
        assertEquals("default", nullVal.asString("default"));
        assertEquals(100, nullVal.asInt(100));
        assertTrue(nullVal.isNull());
    }

    @Test
    void testTypeMismatch() {
        FlagValue invalid = FlagValue.of("not a number");

        assertThrows(FlagTypeConversionException.class,
                () -> invalid.asInt(0));

        assertThrows(FlagTypeConversionException.class,
                () -> invalid.asBoolean(false));
    }
}
