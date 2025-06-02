package com.thnkscj.flick.providers.caching.strategy;

import java.util.function.Function;

/**
 * A generic cache interface that supports retrieval, insertion, and invalidation
 * of cached values. This interface is designed to be implemented by various
 * caching strategies (e.g., in-memory, distributed, time-based).
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of values cached
 */
public interface Cache<K, V> {

    /**
     * Retrieves the value associated with the given key. If the value is not already cached,
     * the provided {@code loader} function is used to compute and store the value.
     *
     * @param key    the key whose associated value is to be returned
     * @param loader a function to compute the value if it's not cached; must not be {@code null}
     * @return the value associated with the specified key, possibly newly computed
     */
    V get(K key, Function<? super K, ? extends V> loader);

    /**
     * Associates the specified value with the specified key in the cache.
     * If the cache previously contained a mapping for the key, it is replaced.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to cache
     */
    void put(K key, V value);

    /**
     * Invalidates the cached value associated with the specified key, if present.
     *
     * @param key the key whose cached value is to be invalidated
     */
    void invalidate(K key);

    /**
     * Invalidates all cached entries, effectively clearing the cache.
     */
    void invalidateAll();
}
