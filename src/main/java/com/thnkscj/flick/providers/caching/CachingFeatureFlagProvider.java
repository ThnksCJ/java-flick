package com.thnkscj.flick.providers.caching;

import com.thnkscj.flick.core.FeatureFlagProvider;
import com.thnkscj.flick.core.FlagChangeListener;
import com.thnkscj.flick.core.FlagValue;
import com.thnkscj.flick.core.ObservableFeatureFlagProvider;
import com.thnkscj.flick.providers.caching.strategy.Cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CachingFeatureFlagProvider implements ObservableFeatureFlagProvider {
    private final FeatureFlagProvider delegate;
    private final Cache<String, FlagValue> flagCache;
    private final List<FlagChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler;

    public CachingFeatureFlagProvider(FeatureFlagProvider delegate,
                                      Cache<String, FlagValue> cache,
                                      long refreshInterval,
                                      TimeUnit timeUnit) {
        this.delegate = delegate;
        this.flagCache = cache;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "flick-cache-refresh");
            t.setDaemon(true);
            return t;
        });

        if (refreshInterval > 0) {
            scheduler.scheduleAtFixedRate(this::refreshCache,
                    refreshInterval, refreshInterval, timeUnit);
        }

        if (delegate instanceof ObservableFeatureFlagProvider) {
            ((ObservableFeatureFlagProvider) delegate).addChangeListener(this::handleSourceChange);
        }

        refresh();
    }

    @Override
    public FlagValue getValue(String key) {
        return flagCache.get(key, delegate::getValue);
    }

    @Override
    public Map<String, FlagValue> getChildren(String prefix) {
        // Do not cache children as they might change frequently
        return delegate.getChildren(prefix);
    }

    @Override
    public void refresh() {
        refreshCache();
        delegate.refresh();
    }

    @Override
    public void addChangeListener(FlagChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(FlagChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void shutdown() {
        scheduler.shutdownNow();
        delegate.shutdown();
    }

    private void refreshCache() {
        flagCache.invalidateAll();

        Map<String, FlagValue> allFlags = delegate.getChildren("");
        for (Map.Entry<String, FlagValue> entry : allFlags.entrySet()) {
            String key = entry.getKey();
            FlagValue value = entry.getValue();

            flagCache.put(key, value);
            notifyListeners(key, value);
        }
    }

    private void handleSourceChange(String key, FlagValue newValue) {
        //flagCache.put(key, newValue); this line is commented out to avoid caching changes from the source
        notifyListeners(key, newValue);
    }

    private void notifyListeners(String key, FlagValue value) {
        for (FlagChangeListener listener : listeners) {
            listener.onFlagChange(key, value);
        }
    }
}
