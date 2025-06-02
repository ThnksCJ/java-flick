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

/**
 * A {@link FeatureFlagProvider} implementation that wraps another provider
 * and adds caching functionality. Optionally supports periodic cache refresh and
 * propagation of change events to registered listeners.
 * <p>
 * This is useful when the underlying provider is slow or expensive to query (e.g. network-based).
 * </p>
 */
public class CachingFeatureFlagProvider implements ObservableFeatureFlagProvider {

    private final FeatureFlagProvider delegate;
    private final Cache<String, FlagValue> flagCache;
    private final List<FlagChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler;

    /**
     * Constructs a new caching feature flag provider.
     *
     * @param delegate         the underlying provider to fetch values from
     * @param cache            the cache strategy to use
     * @param refreshInterval  the interval for automatic cache refresh (set 0 to disable)
     * @param timeUnit         the time unit for the refresh interval
     */
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

    /**
     * Gets a flag value from the cache if present, or delegates to the underlying provider.
     *
     * @param key the flag key
     * @return the corresponding {@link FlagValue}
     */
    @Override
    public FlagValue getValue(String key) {
        return flagCache.get(key, delegate::getValue);
    }

    /**
     * Delegates directly to the underlying provider to fetch child flags.
     * Child values are not cached due to their dynamic nature.
     *
     * @param prefix the prefix to search for
     * @return a map of child flag values
     */
    @Override
    public Map<String, FlagValue> getChildren(String prefix) {
        return delegate.getChildren(prefix);
    }

    /**
     * Refreshes the entire cache and underlying provider.
     */
    @Override
    public void refresh() {
        refreshCache();
        delegate.refresh();
    }

    /**
     * Adds a change listener that is notified when flags are refreshed or changed by the source.
     *
     * @param listener the listener to add
     */
    @Override
    public void addChangeListener(FlagChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a previously registered change listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeChangeListener(FlagChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Shuts down the internal scheduler and the underlying delegate.
     */
    @Override
    public void shutdown() {
        scheduler.shutdownNow();
        delegate.shutdown();
    }

    /**
     * Clears and repopulates the cache from the underlying provider, and notifies listeners.
     */
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

    /**
     * Handles flag change events from the source provider and notifies listeners.
     * Note: changes from the delegate are not cached here to avoid stale data conflicts.
     *
     * @param key      the changed flag key
     * @param newValue the new flag value
     */
    private void handleSourceChange(String key, FlagValue newValue) {
        //flagCache.put(key, newValue); this line is commented out to avoid caching changes from the source
        notifyListeners(key, newValue);
    }

    /**
     * Notifies all registered listeners of a flag change.
     *
     * @param key   the key of the changed flag
     * @param value the new value
     */
    private void notifyListeners(String key, FlagValue value) {
        for (FlagChangeListener listener : listeners) {
            listener.onFlagChange(key, value);
        }
    }
}
