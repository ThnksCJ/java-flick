package com.thnkscj.flick.core;

import java.util.Map;

/**
 * Interface defining a provider of feature flags.
 * Implementations supply flag values, support hierarchical querying,
 * and optionally support refresh and shutdown operations.
 */
public interface FeatureFlagProvider {

    /**
     * Retrieves the value of a feature flag by its key.
     *
     * @param key the key/name of the flag to retrieve
     * @return the {@link FlagValue} associated with the key, or null if not found
     */
    FlagValue getValue(String key);

    /**
     * Retrieves all child flags with keys that start with the specified prefix.
     *
     * @param prefix the prefix to filter keys by
     * @return a map of flag keys to their corresponding {@link FlagValue}s,
     *         or an empty map if none are found
     */
    Map<String, FlagValue> getChildren(String prefix);

    /**
     * Refreshes the flags from the underlying source.
     * Default implementation is a no-op.
     * Implementations may override to reload or update flag data.
     */
    default void refresh() {}

    /**
     * Shuts down any resources held by the provider.
     * Default implementation is a no-op.
     * Implementations may override to release resources when no longer needed.
     */
    default void shutdown() {}
}
