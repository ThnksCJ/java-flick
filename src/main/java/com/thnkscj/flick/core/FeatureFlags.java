package com.thnkscj.flick.core;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public final class FeatureFlags {
    private static final AtomicReference<FeatureFlagProvider> providerRef =
            new AtomicReference<>(new NullProvider());

    private static volatile ExecutorService listenerExecutor = createExecutor();

    private FeatureFlags() {}

    public static void setProvider(FeatureFlagProvider provider) {
        setProvider(provider, false);
    }

    public static void setProvider(FeatureFlagProvider provider, boolean shutdownPrevious) {
        FeatureFlagProvider newProvider = Optional.ofNullable(provider).orElseGet(NullProvider::new);
        FeatureFlagProvider previousProvider = providerRef.getAndSet(newProvider);

        if (shutdownPrevious && previousProvider != null) {
            previousProvider.shutdown();
        }

        ensureExecutor();
    }

    private static ExecutorService createExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "flick-listener");
            t.setDaemon(true);
            return t;
        });
    }

    private static void ensureExecutor() {
        if (listenerExecutor == null || listenerExecutor.isShutdown()) {
            synchronized (FeatureFlags.class) {
                if (listenerExecutor == null || listenerExecutor.isShutdown()) {
                    listenerExecutor = createExecutor();
                }
            }
        }
    }

    public static FlagValue get(String key) {
        return Optional.ofNullable(providerRef.get().getValue(key)).orElse(FlagValue.nullValue());
    }

    public static Map<String, FlagValue> getChildren(String prefix) {
        return providerRef.get().getChildren(prefix);
    }

    public static void refresh() {
        providerRef.get().refresh();
    }

    public static void addGlobalChangeListener(FlagChangeListener listener) {
        FeatureFlagProvider provider = providerRef.get();
        if (provider instanceof ObservableFeatureFlagProvider) {
            ((ObservableFeatureFlagProvider) provider).addChangeListener((key, value) ->
                    getExecutor().execute(() -> listener.onFlagChange(key, value)));
        }
    }

    public static void shutdown() {
        providerRef.get().shutdown();
        if (listenerExecutor != null) {
            listenerExecutor.shutdown();
        }
    }

    private static ExecutorService getExecutor() {
        ensureExecutor();
        return listenerExecutor;
    }

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
