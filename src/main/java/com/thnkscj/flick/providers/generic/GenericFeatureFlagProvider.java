package com.thnkscj.flick.providers.generic;

import com.thnkscj.flick.core.FeatureFlagProvider;
import com.thnkscj.flick.core.FlagValue;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base provider implementation without caching that can be extended
 * for various configuration sources (files, databases, HTTP, etc.)
 */
public abstract class GenericFeatureFlagProvider implements FeatureFlagProvider {
    protected final Map<String, FlagValue> flags = new ConcurrentHashMap<>();
    protected volatile boolean initialized = false;

    /**
     * Initialize the provider and load initial flag values
     */
    protected abstract void initialize();

    /**
     * Concrete implementations should implement this to refresh flag values
     */
    protected abstract void loadFlags();

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

    @Override
    public void refresh() {
        loadFlags();
    }

    @Override
    public void shutdown() {
        flags.clear();
    }

    protected void updateFlag(String key, Object value) {
        if (value == null) {
            flags.remove(key);
        } else {
            flags.put(key, FlagValue.of(value));
        }
    }

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
