package com.thnkscj.flick.providers.composite;

import com.thnkscj.flick.core.FeatureFlagProvider;
import com.thnkscj.flick.core.FlagValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompositeFeatureFlagProvider implements FeatureFlagProvider {
    private final List<FeatureFlagProvider> providers;
    private final boolean shortCircuit;

    public CompositeFeatureFlagProvider(List<FeatureFlagProvider> providers, boolean shortCircuit) {
        this.providers = Collections.unmodifiableList(new ArrayList<>(providers));
        this.shortCircuit = shortCircuit;
    }

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

    @Override
    public void refresh() {
        providers.forEach(FeatureFlagProvider::refresh);
    }

    @Override
    public void shutdown() {
        providers.forEach(FeatureFlagProvider::shutdown);
    }
}
