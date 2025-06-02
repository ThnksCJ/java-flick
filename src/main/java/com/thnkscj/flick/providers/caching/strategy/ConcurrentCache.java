package com.thnkscj.flick.providers.caching.strategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ConcurrentCache<K, V> implements Cache<K, V> {
    private final ConcurrentMap<K, V> cache = new ConcurrentHashMap<>();
    private final Function<K, V> loader;

    public ConcurrentCache(Function<K, V> loader) {
        this.loader = loader;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loaderOverride) {
        return cache.computeIfAbsent(key, loaderOverride != null ? loaderOverride : loader);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void invalidate(K key) {
        cache.remove(key);
    }

    @Override
    public void invalidateAll() {
        cache.clear();
    }

    @Override
    public String toString() {
        return "ConcurrentCache{" +
                "cache=" + cache +
                '}';
    }
}
