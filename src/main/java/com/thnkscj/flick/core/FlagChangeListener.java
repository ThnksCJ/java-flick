package com.thnkscj.flick.core;

/**
 * Functional interface to listen for changes to feature flags.
 * Implementations of this interface will be notified when a flag's value changes.
 */
@FunctionalInterface
public interface FlagChangeListener {

    /**
     * Called when the value of a flag changes.
     *
     * @param key      the key/name of the flag that changed
     * @param newValue the new {@link FlagValue} of the flag
     */
    void onFlagChange(String key, FlagValue newValue);
}
