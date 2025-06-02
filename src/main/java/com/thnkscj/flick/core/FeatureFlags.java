package com.thnkscj.flick.core;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides a centralized interface to access and manage feature flags.
 * <p>
 * This class maintains a singleton {@link FeatureFlagProvider} instance
 * that supplies flag values and supports listening for changes.
 * It also manages an internal executor for dispatching flag change notifications.
 * </p>
 * <p>
 * By default, it uses a {@link NullProvider} which returns no flags.
 * The provider can be set or replaced dynamically.
 * </p>
 */
public final class FeatureFlags {

    private static final AtomicReference<FeatureFlagProvider> providerRef =
            new AtomicReference<>(new NullProvider());

    private static volatile ExecutorService listenerExecutor = createExecutor();

    private FeatureFlags() {}

    /**
     * Sets the active feature flag provider.
     * If the given provider is null, a default {@link NullProvider} will be used.
     * Does not shut down the previous provider.
     *
     * @param provider the new provider to use, or null to reset to a null provider
     */
    public static void setProvider(FeatureFlagProvider provider) {
        setProvider(provider, false);
    }

    /**
     * Sets the active feature flag provider.
     * Optionally shuts down the previous provider.
     * If the given provider is null, a default {@link NullProvider} will be used.
     *
     * @param provider         the new provider to use, or null to reset to a null provider
     * @param shutdownPrevious if true, shuts down the previous provider
     */
    public static void setProvider(FeatureFlagProvider provider, boolean shutdownPrevious) {
        FeatureFlagProvider newProvider = Optional.ofNullable(provider).orElseGet(NullProvider::new);
        FeatureFlagProvider previousProvider = providerRef.getAndSet(newProvider);

        if (shutdownPrevious && previousProvider != null) {
            previousProvider.shutdown();
        }

        ensureExecutor();
    }

    /**
     * Creates a single-threaded executor for executing flag change listeners.
     * The thread is a daemon thread named "flick-listener".
     *
     * @return the created {@link ExecutorService}
     */
    private static ExecutorService createExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "flick-listener");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Ensures the listener executor is active.
     * If the executor is null or shut down, recreates it in a thread-safe manner.
     */
    private static void ensureExecutor() {
        if (listenerExecutor == null || listenerExecutor.isShutdown()) {
            synchronized (FeatureFlags.class) {
                if (listenerExecutor == null || listenerExecutor.isShutdown()) {
                    listenerExecutor = createExecutor();
                }
            }
        }
    }

    /**
     * Retrieves the value of the flag with the given key from the current provider.
     * If no value is found, returns {@link FlagValue#nullValue()}.
     *
     * @param key the key/name of the flag to retrieve
     * @return the {@link FlagValue} for the given key, never null
     */
    public static FlagValue get(String key) {
        return Optional.ofNullable(providerRef.get().getValue(key)).orElse(FlagValue.nullValue());
    }

    /**
     * Retrieves all child flags that have keys starting with the given prefix.
     *
     * @param prefix the prefix to filter keys by
     * @return a map of flag keys to {@link FlagValue}s matching the prefix, never null
     */
    public static Map<String, FlagValue> getChildren(String prefix) {
        return providerRef.get().getChildren(prefix);
    }

    /**
     * Refreshes the flags from the underlying provider.
     * This may trigger updates or reloads depending on the provider implementation.
     */
    public static void refresh() {
        providerRef.get().refresh();
    }

    /**
     * Adds a global listener that will be notified asynchronously whenever
     * any flag changes value.
     * <p>
     * Only effective if the current provider implements {@link ObservableFeatureFlagProvider}.
     * </p>
     *
     * @param listener the {@link FlagChangeListener} to register
     */
    public static void addGlobalChangeListener(FlagChangeListener listener) {
        FeatureFlagProvider provider = providerRef.get();
        if (provider instanceof ObservableFeatureFlagProvider) {
            ((ObservableFeatureFlagProvider) provider).addChangeListener((key, value) ->
                    getExecutor().execute(() -> listener.onFlagChange(key, value)));
        }
    }

    /**
     * Shuts down the current provider and the listener executor service.
     * After shutdown, the feature flag system may no longer function until reinitialized.
     */
    public static void shutdown() {
        providerRef.get().shutdown();
        if (listenerExecutor != null) {
            listenerExecutor.shutdown();
        }
    }

    /**
     * Returns the internal executor service for listener notifications,
     * ensuring it is active.
     *
     * @return the executor service used for dispatching flag change events
     */
    private static ExecutorService getExecutor() {
        ensureExecutor();
        return listenerExecutor;
    }

    /**
     * A no-op provider implementation that returns no flags.
     */
    private static class NullProvider implements FeatureFlagProvider {
        @Override
        public FlagValue getValue(String key) {
            return FlagValue.nullValue();
        }

        @Override
        public Map<String, FlagValue> getChildren(String prefix) {
            return Collections.emptyMap();
        }
    }
}
