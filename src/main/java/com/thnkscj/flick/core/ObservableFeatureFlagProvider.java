package com.thnkscj.flick.core;

public interface ObservableFeatureFlagProvider extends FeatureFlagProvider {
    void addChangeListener(FlagChangeListener listener);
    void removeChangeListener(FlagChangeListener listener);
}
