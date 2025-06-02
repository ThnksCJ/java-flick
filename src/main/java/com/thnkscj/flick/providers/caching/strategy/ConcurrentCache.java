package com.thnkscj.flick.providers.caching.strategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * A thread-safe cache implementation backed by {@link ConcurrentHashMap}.
 *
 * <p>This cache supports lazy loading of values using a {@link Function} loader,
 * and also allows external population and invalidation of cache entries.</p>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class ConcurrentCache<K, V> implements Cache<K, V> {

    private final ConcurrentMap<K, V> cache = new ConcurrentHashMap<>();
    private final Function<K, V> loader;

    /**
     * Constructs a new {@code ConcurrentCache} with a given default loader function.
     *
     * @param loader the default function used to compute a value for a key if not present
     */
    public ConcurrentCache(Function<K, V> loader) {
        this.loader = loader;
    }

    /**
     * Returns the value to which the specified key is mapped, computing and caching
     * it using the provided loader if necessary.
     *
     * @param key            the key whose associated value is to be returned
     * @param loaderOverride an optional override loader (if {@code null}, the default loader is used)
     * @return the current (existing or computed) value associated with the specified key
     */
    @Override
    public V get(K key, Function<? super K, ? extends V> loaderOverride) {
        return cache.computeIfAbsent(key, loaderOverride != null ? loaderOverride : loader);
    }

    /**
     * Associates the specified value with the specified key in the cache.
     * If the cache previously contained a mapping for the key, the old value is replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    /**
     * Removes the mapping for a key from this cache if it is present.
     *
     * @param key key whose mapping is to be removed
     */
    @Override
    public void invalidate(K key) {
        cache.remove(key);
    }

    /**
     * Clears all entries from the cache.
     */
    @Override
    public void invalidateAll() {
        cache.clear();
    }

    /**
     * Returns a string representation of the current cache contents.
     *
     * @return a string representation of the cache
     */
    @Override
    public String toString() {
        return "ConcurrentCache{" +
                "cache=" + cache +
                '}';
    }
}
