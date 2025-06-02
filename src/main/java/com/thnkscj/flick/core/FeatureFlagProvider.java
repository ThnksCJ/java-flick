package com.thnkscj.flick.core;

import java.util.Map;

public interface FeatureFlagProvider {
    FlagValue getValue(String key);
    Map<String, FlagValue> getChildren(String prefix);
    default void refresh() {}
    default void shutdown() {}
}
