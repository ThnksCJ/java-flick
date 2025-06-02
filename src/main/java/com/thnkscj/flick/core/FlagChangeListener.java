package com.thnkscj.flick.core;

@FunctionalInterface
public interface FlagChangeListener {
    void onFlagChange(String key, FlagValue newValue);
}
