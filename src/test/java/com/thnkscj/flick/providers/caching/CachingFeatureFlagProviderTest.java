package com.thnkscj.flick.providers.caching;

import com.thnkscj.flick.providers.caching.strategy.ConcurrentCache;
import com.thnkscj.flick.MockFeatureFlagProvider;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CachingFeatureFlagProviderTest {
    @Test
    void testCachingBehavior() {
        MockFeatureFlagProvider mockProvider = new MockFeatureFlagProvider();
        mockProvider.setFlag("cached.flag", "initial");

        // Create cache with manual refresh
        CachingFeatureFlagProvider cachingProvider = new CachingFeatureFlagProvider(
                mockProvider,
                new ConcurrentCache<>(null),
                0, // No auto-refresh
                TimeUnit.SECONDS
        );

        // Initial value
        assertEquals("initial", cachingProvider.getValue("cached.flag").asString(null));

        // Update source
        mockProvider.setFlag("cached.flag", "updated");

        // Should still return cached value
        assertEquals("initial", cachingProvider.getValue("cached.flag").asString(null));

        // Refresh cache
        cachingProvider.refresh();

        // Should now return updated value
        assertEquals("updated", cachingProvider.getValue("cached.flag").asString(null));
    }

    @Test
    void testChangePropagation() {
        MockFeatureFlagProvider mockProvider = new MockFeatureFlagProvider();
        mockProvider.setFlag("observable.flag", 1);

        CachingFeatureFlagProvider cachingProvider = new CachingFeatureFlagProvider(
                mockProvider,
                new ConcurrentCache<>(null),
                1,
                TimeUnit.SECONDS
        );

        CountDownLatch latch = new CountDownLatch(1);
        cachingProvider.addChangeListener((key, value) -> {
            if ("observable.flag".equals(key) && value.asInt(0) == 2) {
                latch.countDown();
            }
        });

        // Update source
        mockProvider.setFlag("observable.flag", 2);

        await().atMost(2, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> cachingProvider.getValue("observable.flag").asInt(0) == 2);
    }
}
