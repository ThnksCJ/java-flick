package com.thnkscj.flick.providers.composite;

import com.thnkscj.flick.core.FeatureFlagProvider;
import com.thnkscj.flick.core.FlagValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link FeatureFlagProvider} implementation that aggregates multiple providers
 * into a single composite source. This allows flags to be resolved from multiple
 * underlying sources in a prioritized order.
 * <p>
 * Optionally supports "short-circuit" resolution, which stops at the first
 * provider that returns a non-null, present value.
 * </p>
 */
public class CompositeFeatureFlagProvider implements FeatureFlagProvider {

    private final List<FeatureFlagProvider> providers;
    private final boolean shortCircuit;

    /**
     * Constructs a composite provider with the given list of delegate providers.
     *
     * @param providers     the list of underlying providers to delegate to, in order of priority
     * @param shortCircuit  if {@code true}, resolution stops at the first present flag value
     */
    public CompositeFeatureFlagProvider(List<FeatureFlagProvider> providers, boolean shortCircuit) {
        this.providers = Collections.unmodifiableList(new ArrayList<>(providers));
        this.shortCircuit = shortCircuit;
    }

    /**
     * Resolves a flag value from the first provider that has a present value.
     * If {@code shortCircuit} is enabled, resolution stops immediately when a value is found.
     *
     * @param key the flag key to resolve
     * @return the resolved {@link FlagValue}, or {@link FlagValue#nullValue()} if not found
     */
    @Override
    public FlagValue getValue(String key) {
        for (FeatureFlagProvider provider : providers) {
            FlagValue value = provider.getValue(key);

            if (value == null || value.isNull()) {
                continue;
            }

            if (value.isPresent() && shortCircuit) {
                return value;
            } else if (value.isPresent()) {
                return value;
            }
        }
        return FlagValue.nullValue();
    }

    /**
     * Merges child flags with the specified prefix from all underlying providers.
     * If {@code shortCircuit} is enabled, the first non-empty result is returned immediately.
     * Later providers cannot override earlier ones.
     *
     * @param prefix the prefix to filter child flags
     * @return an unmodifiable map of child flag keys and values
     */
    @Override
    public Map<String, FlagValue> getChildren(String prefix) {
        Map<String, FlagValue> result = new ConcurrentHashMap<>();
        String normalizedPrefix = prefix.endsWith(".") ? prefix : prefix + ".";

        for (FeatureFlagProvider provider : providers) {
            Map<String, FlagValue> children = provider.getChildren(prefix);
            if (shortCircuit && !children.isEmpty()) {
                return children;
            }
            children.forEach(result::putIfAbsent);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Refreshes all underlying providers.
     */
    @Override
    public void refresh() {
        providers.forEach(FeatureFlagProvider::refresh);
    }

    /**
     * Shuts down all underlying providers.
     */
    @Override
    public void shutdown() {
        providers.forEach(FeatureFlagProvider::shutdown);
    }
}
