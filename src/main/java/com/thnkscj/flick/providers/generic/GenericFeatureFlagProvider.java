package com.thnkscj.flick.providers.generic;

import com.thnkscj.flick.core.FeatureFlagProvider;
import com.thnkscj.flick.core.FlagValue;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base implementation of {@link FeatureFlagProvider} designed to be extended
 * for various backing sources such as files, databases, or remote services.
 * <p>
 * This class handles lazy initialization, thread-safe flag storage,
 * and provides helper methods for updating flag values.
 * </p>
 */
public abstract class GenericFeatureFlagProvider implements FeatureFlagProvider {

    /**
     * Internal thread-safe storage of flag key-value pairs.
     */
    protected final Map<String, FlagValue> flags = new ConcurrentHashMap<>();

    /**
     * Indicates whether the provider has been initialized.
     */
    protected volatile boolean initialized = false;

    /**
     * Initializes the provider by performing one-time setup and loading initial flags.
     * <p>
     * This method is called lazily on the first call to {@link #getValue(String)}.
     * Subclasses must implement this method to perform custom initialization logic.
     * </p>
     */
    protected abstract void initialize();

    /**
     * Refreshes the current set of flags by reloading them from the underlying source.
     * Subclasses must implement this method to provide the actual loading logic.
     */
    protected abstract void loadFlags();

    /**
     * Retrieves the value of a feature flag by key.
     * <p>
     * Lazily initializes the provider on first use.
     * </p>
     *
     * @param key the key of the flag
     * @return the corresponding {@link FlagValue}, or {@link FlagValue#nullValue()} if not found
     */
    @Override
    public FlagValue getValue(String key) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    initialize();
                    initialized = true;
                }
            }
        }
        return flags.getOrDefault(key, FlagValue.nullValue());
    }

    /**
     * Returns a map of all flags whose keys start with the given prefix.
     * The returned keys are trimmed to remove the prefix.
     *
     * @param prefix the prefix to match
     * @return an unmodifiable map of child keys to values
     */
    @Override
    public Map<String, FlagValue> getChildren(String prefix) {
        String normalizedPrefix = prefix.endsWith(".") ? prefix : prefix + ".";
        Map<String, FlagValue> result = new ConcurrentHashMap<>();

        flags.forEach((key, value) -> {
            if (key.startsWith(normalizedPrefix)) {
                String childKey = key.substring(normalizedPrefix.length());
                result.put(childKey, value);
            }
        });

        return Collections.unmodifiableMap(result);
    }

    /**
     * Reloads all flags by invoking {@link #loadFlags()}.
     */
    @Override
    public void refresh() {
        loadFlags();
    }

    /**
     * Clears all loaded flags and releases internal state.
     */
    @Override
    public void shutdown() {
        flags.clear();
    }

    /**
     * Updates or removes a single flag value in the internal map.
     *
     * @param key   the flag key
     * @param value the new value (or {@code null} to remove the flag)
     */
    protected void updateFlag(String key, Object value) {
        if (value == null) {
            flags.remove(key);
        } else {
            flags.put(key, FlagValue.of(value));
        }
    }

    /**
     * Updates multiple flags at once.
     * Entries with {@code null} values will be removed.
     *
     * @param newFlags a map of keys to new flag values
     */
    protected void bulkUpdateFlags(Map<String, Object> newFlags) {
        newFlags.forEach((key, value) -> {
            if (value == null) {
                flags.remove(key);
            } else {
                flags.put(key, FlagValue.of(value));
            }
        });
    }
}
