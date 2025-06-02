package com.thnkscj.flick.providers.generic;

import static org.junit.jupiter.api.Assertions.*;

import com.thnkscj.flick.core.FlagValue;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class GenericFeatureFlagProviderTest {

    static class TestProvider extends GenericFeatureFlagProvider {
        @Override
        protected void initialize() {
            updateFlag("test.flag", true);
            updateFlag("nested.feature.enabled", false);
        }

        @Override
        protected void loadFlags() {
            // Simulate refresh
            updateFlag("dynamic.flag", System.currentTimeMillis());
        }
    }

    @Test
    void testBasicFunctionality() {
        TestProvider provider = new TestProvider();

        assertTrue(provider.getValue("test.flag").asBoolean(false));
        assertFalse(provider.getValue("nested.feature.enabled").asBoolean(true));
        assertTrue(provider.getValue("missing.flag").isNull());
    }

    @Test
    void testChildrenLookup() {
        TestProvider provider = new TestProvider();
        provider.updateFlag("parent.child1", "value1");
        provider.updateFlag("parent.child2", "value2");
        provider.updateFlag("other.flag", "x");

        Map<String, FlagValue> children = provider.getChildren("parent");

        assertEquals(2, children.size());
        assertEquals("value1", children.get("child1").asString(null));
        assertEquals("value2", children.get("child2").asString(null));
    }

    @Test
    void testRefresh() {
        TestProvider provider = new TestProvider();
        long firstValue = provider.getValue("dynamic.flag").asLong(0);

        provider.refresh();
        long secondValue = provider.getValue("dynamic.flag").asLong(0);

        assertTrue(secondValue > firstValue);
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        TestProvider provider = new TestProvider();
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    provider.getValue("test.flag");
                    provider.getChildren("nested");
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // No assertions needed - just verifying no exceptions occur
        assertTrue(provider.getValue("test.flag").asBoolean(false));
    }
}
