package com.thnkscj.flick;

import com.thnkscj.flick.core.FlagChangeListener;
import com.thnkscj.flick.core.FlagValue;
import com.thnkscj.flick.core.ObservableFeatureFlagProvider;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockFeatureFlagProvider implements ObservableFeatureFlagProvider {
    private final Map<String, Object> flags = new ConcurrentHashMap<>();
    private final List<FlagChangeListener> listeners = new CopyOnWriteArrayList<>();

    public void setFlag(String key, Object value) {
        flags.put(key, value);
        notifyListeners(key, new FlagValue(value));
    }

    public void clearFlag(String key) {
        if (flags.containsKey(key)) {
            flags.remove(key);
            notifyListeners(key, null);
        }
    }

    public void clearAll() {
        for (String key : flags.keySet()) {
            notifyListeners(key, null);
        }
        flags.clear();
    }

    @Override
    public FlagValue getValue(String key) {
        return flags.containsKey(key) ? new FlagValue(flags.get(key)) : null;
    }

    @Override
    public Map<String, FlagValue> getChildren(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.unmodifiableMap(flags.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new FlagValue(e.getValue()))));
        }

        final String matchPrefix = prefix.endsWith(".") ? prefix : prefix + ".";
        Map<String, FlagValue> result = new ConcurrentHashMap<>();

        flags.forEach((key, value) -> {
            if (key.startsWith(matchPrefix)) {
                result.put(key.substring(matchPrefix.length()), new FlagValue(value));
            }
        });
        return Collections.unmodifiableMap(result);
    }

    @Override
    public void addChangeListener(FlagChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(FlagChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String key, FlagValue newValue) {
        for (FlagChangeListener listener : listeners) {
            listener.onFlagChange(key, newValue);
        }
    }
}
