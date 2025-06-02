package com.thnkscj.flick.providers.composite;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Map;

import com.thnkscj.flick.core.FlagValue;
import com.thnkscj.flick.MockFeatureFlagProvider;
import org.junit.jupiter.api.Test;

public class CompositeFeatureFlagProviderTest {

    @Test
    void testShortCircuitResolution() {
        MockFeatureFlagProvider first = new MockFeatureFlagProvider();
        first.setFlag("common.flag", "first");

        MockFeatureFlagProvider second = new MockFeatureFlagProvider();
        second.setFlag("common.flag", "second");
        second.setFlag("unique.flag", true);

        CompositeFeatureFlagProvider composite = new CompositeFeatureFlagProvider(
                Arrays.asList(first, second), true
        );

        assertEquals("first", composite.getValue("common.flag").asString(null));
        assertTrue(composite.getValue("unique.flag").asBoolean(false));
    }

    @Test
    void testNonShortCircuitResolution() {
        MockFeatureFlagProvider first = new MockFeatureFlagProvider();
        MockFeatureFlagProvider second = new MockFeatureFlagProvider();
        second.setFlag("only.in.second", 42);

        CompositeFeatureFlagProvider composite = new CompositeFeatureFlagProvider(
                Arrays.asList(first, second), false
        );

        assertEquals(42, composite.getValue("only.in.second").asInt(0));
        assertTrue(composite.getValue("missing.flag").isNull());
    }

    @Test
    void testChildrenMerge() {
        MockFeatureFlagProvider first = new MockFeatureFlagProvider();
        first.setFlag("auth.login", true);

        MockFeatureFlagProvider second = new MockFeatureFlagProvider();
        second.setFlag("auth.register", false);
        second.setFlag("auth.login", false); // Should be ignored in merge

        CompositeFeatureFlagProvider composite = new CompositeFeatureFlagProvider(
                Arrays.asList(first, second), false
        );

        Map<String, FlagValue> children = composite.getChildren("auth");
        assertEquals(2, children.size());
        assertTrue(children.get("login").asBoolean(false)); // From first provider
        assertFalse(children.get("register").asBoolean(true)); // From second provider
    }
}
