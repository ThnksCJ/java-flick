package com.thnkscj.flick.core;

import com.thnkscj.flick.core.exceptions.FlagTypeConversionException;

import java.util.Optional;

/**
 * Represents a value for a feature flag that can be converted to various types.
 * Encapsulates a raw value and provides methods to convert it to boolean, int, long,
 * double, String, or to retrieve it as a specific type.
 * <p>
 * Supports default values for conversions and throws {@link FlagTypeConversionException}
 * if a conversion cannot be performed.
 * </p>
 */
public final class FlagValue {
    private final Object rawValue;
    private static final FlagValue NULL_VALUE = new FlagValue(null);

    /**
     * Constructs a {@code FlagValue} wrapping the given raw value.
     *
     * @param value the raw value, can be null
     */
    public FlagValue(Object value) {
        this.rawValue = value;
    }

    /**
     * Creates a {@code FlagValue} wrapping the given value.
     * If the value is null, returns a singleton instance representing a null value.
     *
     * @param value the value to wrap, may be null
     * @return a {@code FlagValue} wrapping the given value or a null value instance
     */
    public static FlagValue of(Object value) {
        return value != null ? new FlagValue(value) : NULL_VALUE;
    }

    /**
     * Returns a singleton {@code FlagValue} instance representing a null value.
     *
     * @return a {@code FlagValue} representing null
     */
    public static FlagValue nullValue() {
        return NULL_VALUE;
    }

    /**
     * Converts the wrapped value to a boolean.
     * Returns false if the value is null or conversion is not possible.
     *
     * @return the boolean value or false if null
     * @throws FlagTypeConversionException if conversion is not possible
     */
    public boolean asBoolean() {
        return asBoolean(false);
    }

    /**
     * Converts the wrapped value to a boolean.
     * Returns the specified default value if the wrapped value is null.
     * Accepts Boolean, numeric (1=true, 0=false), and String ("true", "false", "1", "0").
     *
     * @param defaultValue the value to return if the wrapped value is null
     * @return the boolean value or defaultValue if null
     * @throws FlagTypeConversionException if conversion is not possible
     */
    public boolean asBoolean(boolean defaultValue) {
        if (rawValue == null) return defaultValue;
        if (rawValue instanceof Boolean) return (Boolean) rawValue;
        if (rawValue instanceof String) {
            String s = ((String) rawValue).trim().toLowerCase();
            if ("true".equals(s) || "1".equals(s)) return true;
            if ("false".equals(s) || "0".equals(s)) return false;
        }
        if (rawValue instanceof Number) {
            int num = ((Number) rawValue).intValue();
            if (num == 1) return true;
            if (num == 0) return false;
        }
        throw new FlagTypeConversionException("Cannot convert '" + rawValue + "' to boolean");
    }

    /**
     * Converts the wrapped value to a String.
     * Returns null if the wrapped value is null.
     *
     * @return the String representation or null if wrapped value is null
     */
    public String asString() {
        return asString(null);
    }

    /**
     * Converts the wrapped value to a String.
     * Returns the specified default value if the wrapped value is null.
     *
     * @param defaultValue the value to return if the wrapped value is null
     * @return the String representation or defaultValue if null
     */
    public String asString(String defaultValue) {
        return rawValue != null ? rawValue.toString() : defaultValue;
    }

    /**
     * Converts the wrapped value to an int.
     * Returns 0 if the wrapped value is null.
     *
     * @return the int value or 0 if null
     * @throws FlagTypeConversionException if conversion is not possible
     */
    public int asInt() {
        return asInt(0);
    }

    /**
     * Converts the wrapped value to an int.
     * Returns the specified default value if the wrapped value is null.
     * Accepts Number types or String parsable as int.
     *
     * @param defaultValue the value to return if the wrapped value is null
     * @return the int value or defaultValue if null
     * @throws FlagTypeConversionException if conversion is not possible
     */
    public int asInt(int defaultValue) {
        if (rawValue == null) return defaultValue;
        if (rawValue instanceof Number) return ((Number) rawValue).intValue();
        try {
            return Integer.parseInt(rawValue.toString());
        } catch (NumberFormatException e) {
            throw new FlagTypeConversionException("Cannot convert '" + rawValue + "' to int", e);
        }
    }

    /**
     * Converts the wrapped value to a long.
     * Returns 0L if the wrapped value is null.
     *
     * @return the long value or 0L if null
     * @throws FlagTypeConversionException if conversion is not possible
     */
    public long asLong() {
        return asLong(0L);
    }

    /**
     * Converts the wrapped value to a long.
     * Returns the specified default value if the wrapped value is null.
     * Accepts Number types or String parsable as long.
     *
     * @param defaultValue the value to return if the wrapped value is null
     * @return the long value or defaultValue if null
     * @throws FlagTypeConversionException if conversion is not possible
     */
    public long asLong(long defaultValue) {
        if (rawValue == null) return defaultValue;
        if (rawValue instanceof Number) return ((Number) rawValue).longValue();
        try {
            return Long.parseLong(rawValue.toString());
        } catch (NumberFormatException e) {
            throw new FlagTypeConversionException("Cannot convert '" + rawValue + "' to long", e);
        }
    }

    /**
     * Converts the wrapped value to a double.
     * Returns 0.0 if the wrapped value is null.
     *
     * @return the double value or 0.0 if null
     * @throws FlagTypeConversionException if conversion is not possible
     */
    public double asDouble() {
        return asDouble(0.0);
    }

    /**
     * Converts the wrapped value to a double.
     * Returns the specified default value if the wrapped value is null.
     * Accepts Number types or String parsable as double.
     *
     * @param defaultValue the value to return if the wrapped value is null
     * @return the double value or defaultValue if null
     * @throws FlagTypeConversionException if conversion is not possible
     */
    public double asDouble(double defaultValue) {
        if (rawValue == null) return defaultValue;
        if (rawValue instanceof Number) return ((Number) rawValue).doubleValue();
        try {
            return Double.parseDouble(rawValue.toString());
        } catch (NumberFormatException e) {
            throw new FlagTypeConversionException("Cannot convert '" + rawValue + "' to double", e);
        }
    }

    /**
     * Attempts to cast the wrapped value to the specified type.
     *
     * @param <T>  the target type
     * @param type the class object of the target type
     * @return an {@code Optional} containing the cast value if possible, otherwise empty
     */
    public <T> Optional<T> as(Class<T> type) {
        if (rawValue == null) return Optional.empty();
        if (type.isInstance(rawValue)) {
            return Optional.of(type.cast(rawValue));
        }
        return Optional.empty();
    }

    /**
     * Checks if the wrapped value is null.
     *
     * @return {@code true} if the wrapped value is null, {@code false} otherwise
     */
    public boolean isNull() {
        return rawValue == null;
    }

    /**
     * Checks if the wrapped value is present (not null).
     *
     * @return {@code true} if the wrapped value is not null, {@code false} otherwise
     */
    public boolean isPresent() {
        return rawValue != null;
    }

    /**
     * Returns a string representation of the {@code FlagValue}.
     */
    @Override
    public String toString() {
        return "FlagValue{" +
                "rawValue=" + rawValue +
                '}';
    }
}
