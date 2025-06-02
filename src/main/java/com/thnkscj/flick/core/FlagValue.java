package com.thnkscj.flick.core;

import com.thnkscj.flick.core.exceptions.FlagTypeConversionException;

import java.util.Optional;

public final class FlagValue {
    private final Object rawValue;
    private static final FlagValue NULL_VALUE = new FlagValue(null);

    public FlagValue(Object value) {
        this.rawValue = value;
    }

    public static FlagValue of(Object value) {
        return value != null ? new FlagValue(value) : NULL_VALUE;
    }

    public static FlagValue nullValue() {
        return NULL_VALUE;
    }

    public boolean asBoolean() {
        return asBoolean(false);
    }

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

    public String asString() {
        return asString(null);
    }

    public String asString(String defaultValue) {
        return rawValue != null ? rawValue.toString() : defaultValue;
    }

    public int asInt() {
        return asInt(0);
    }

    public int asInt(int defaultValue) {
        if (rawValue == null) return defaultValue;
        if (rawValue instanceof Number) return ((Number) rawValue).intValue();
        try {
            return Integer.parseInt(rawValue.toString());
        } catch (NumberFormatException e) {
            throw new FlagTypeConversionException("Cannot convert '" + rawValue + "' to int", e);
        }
    }

    public long asLong() {
        return asLong(0L);
    }

    public long asLong(long defaultValue) {
        if (rawValue == null) return defaultValue;
        if (rawValue instanceof Number) return ((Number) rawValue).longValue();
        try {
            return Long.parseLong(rawValue.toString());
        } catch (NumberFormatException e) {
            throw new FlagTypeConversionException("Cannot convert '" + rawValue + "' to long", e);
        }
    }

    public double asDouble() {
        return asDouble(0.0);
    }

    public double asDouble(double defaultValue) {
        if (rawValue == null) return defaultValue;
        if (rawValue instanceof Number) return ((Number) rawValue).doubleValue();
        try {
            return Double.parseDouble(rawValue.toString());
        } catch (NumberFormatException e) {
            throw new FlagTypeConversionException("Cannot convert '" + rawValue + "' to double", e);
        }
    }

    public <T> Optional<T> as(Class<T> type) {
        if (rawValue == null) return Optional.empty();
        if (type.isInstance(rawValue)) {
            return Optional.of(type.cast(rawValue));
        }
        return Optional.empty();
    }

    public boolean isNull() {
        return rawValue == null;
    }

    public boolean isPresent() {
        return rawValue != null;
    }
}
