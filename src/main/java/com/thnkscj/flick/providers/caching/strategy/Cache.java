package com.thnkscj.flick.providers.caching.strategy;

import java.util.function.Function;

public interface Cache<K, V> {
    V get(K key, Function<? super K, ? extends V> loader);
    void put(K key, V value);
    void invalidate(K key);
    void invalidateAll();
}
