package com.thnkscj.flick.core;

import static org.junit.jupiter.api.Assertions.*;

import com.thnkscj.flick.MockFeatureFlagProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

public class FeatureFlagsTest {
    private MockFeatureFlagProvider mockProvider;

    @BeforeEach
    void setup() {
        mockProvider = new MockFeatureFlagProvider();
        mockProvider.setFlag("feature.enabled", true);
        mockProvider.setFlag("service.timeout", 5000);
        FeatureFlags.setProvider(mockProvider);
    }

    @AfterEach
    void cleanup() {
        FeatureFlags.shutdown();
    }

    @Test
    void testBasicFlagAccess() {
        assertTrue(FeatureFlags.get("feature.enabled").asBoolean(false));
        assertEquals(5000, FeatureFlags.get("service.timeout").asInt(0));
    }

    @Test
    void testMissingFlag() {
        assertFalse(FeatureFlags.get("missing.feature").asBoolean(false));
        assertEquals("default", FeatureFlags.get("missing.string").asString("default"));
    }

    @Test
    void testProviderSwitch() {
        MockFeatureFlagProvider newProvider = new MockFeatureFlagProvider();
        newProvider.setFlag("new.feature", true);
        FeatureFlags.setProvider(newProvider, true);

        assertTrue(FeatureFlags.get("new.feature").asBoolean(false));
        assertFalse(FeatureFlags.get("feature.enabled").asBoolean(false));
    }

    @Test
    void testChildrenLookup() {
        mockProvider.setFlag("auth.login.enabled", true);
        mockProvider.setFlag("auth.registration.enabled", false);

        Map<String, FlagValue> authFlags = FeatureFlags.getChildren("auth");

        assertEquals(2, authFlags.size());
        assertTrue(authFlags.get("login.enabled").asBoolean(false));
        assertFalse(authFlags.get("registration.enabled").asBoolean(true));
    }

    @Test
    void testChangeListener() {
        AtomicBoolean eventReceived = new AtomicBoolean(false);

        FeatureFlags.addGlobalChangeListener((key, value) -> {
            if ("dynamic.flag".equals(key)) {
                eventReceived.set(true);
            }
        });

        mockProvider.setFlag("dynamic.flag", "new-value");

        await().atMost(2, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilTrue(eventReceived);
    }
}
