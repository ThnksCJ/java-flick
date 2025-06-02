package com.thnkscj.flick.providers.caching.strategy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ConcurrentCacheTest {

    @Test
    void testCacheOperations() {
        ConcurrentCache<String, String> cache = new ConcurrentCache<>(String::toUpperCase);

        // Test loader function
        assertEquals("TEST", cache.get("test", null));

        // Test manual put
        cache.put("key", "value");
        assertEquals("value", cache.get("key", null));

        // Test invalidation
        cache.invalidate("key");
        assertEquals("KEY", cache.get("key", null)); // Falls back to loader

        // Test bulk invalidation
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.invalidateAll();
        assertEquals("K1", cache.get("k1", null));
        assertEquals("K2", cache.get("k2", null));
    }
}
