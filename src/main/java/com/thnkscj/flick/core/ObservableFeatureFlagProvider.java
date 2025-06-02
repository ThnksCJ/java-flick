package com.thnkscj.flick.core;

/**
 * Extension of {@link FeatureFlagProvider} that supports registering
 * and removing listeners to be notified when feature flag values change.
 */
public interface ObservableFeatureFlagProvider extends FeatureFlagProvider {

    /**
     * Adds a listener that will be notified whenever a flag's value changes.
     *
     * @param listener the {@link FlagChangeListener} to register
     */
    void addChangeListener(FlagChangeListener listener);

    /**
     * Removes a previously registered flag change listener.
     *
     * @param listener the {@link FlagChangeListener} to remove
     */
    void removeChangeListener(FlagChangeListener listener);
}
