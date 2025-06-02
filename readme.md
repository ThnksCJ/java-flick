# Flick

A lightweight feature flag system designed for runtime flag evaluation and dynamic updates.

---

## Quick Start

### 1. Add Your Feature Provider

```java
import com.thnkscj.flick.providers.generic.GenericFeatureFlagProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class JsonFileProvider extends GenericFeatureFlagProvider {
    private final Path configPath;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonFileProvider(Path configPath) {
        this.configPath = configPath;
    }

    @Override
    protected void initialize() {
        loadFlags();
    }

    @Override
    protected void loadFlags() {
        try {
            Map<String, Object> config = mapper.readValue(
                configPath.toFile(),
                new TypeReference<Map<String, Object>>() {}
            );
            bulkUpdateFlags(config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load feature flags from " + configPath, e);
        }
    }
}
```

### 2. Configure and Use the Flags

```java
import com.thnkscj.flick.core.FeatureFlags;

public class Main {
    public static void main(String[] args) {
        JsonFileProvider provider = new JsonFileProvider(Path.of("config/flags.json"));
        provider.initialize(); // load flags from file
        FeatureFlags.setProvider(provider); // register provider globally

        boolean isFeatureEnabled = provider.get("newFeature").asBoolean();
        System.out.println("Is 'newFeature' enabled? " + isFeatureEnabled);
    }
}
```

---

## Example JSON Format

```json
{
  "newFeature": true,
  "rolloutPercentage": 25,
  "nested": {
    "subFeature": "beta"
  }
}
```

### Accessing Nested Flags

```java
Map<String, FlagValue> children = provider.getChildren("nested");
String sub = children.get("subFeature").asString(); // returns "beta"
```

---

## Refreshing Flags at Runtime

If your provider supports it:

```java
provider.refresh(); // Reloads flags via current provider
```

For `JsonFileProvider`, this will reload from the source file if `loadFlags()` is implemented accordingly.

---

## Listening to Flag Changes (Reactive)

If your provider is observable:

```java
provider.addGlobalChangeListener((key, value) -> {
    System.out.printf("Flag changed: %s -> %s%n", key, value);
});
```

---

## Shutdown and Cleanup

Gracefully release resources (e.g., threads):

```java
provider.shutdown();
```

---

## Pluggable Architecture

To integrate with another config system (e.g., Consul, etcd, DB):

1. Extend `FeatureFlagProvider` or `ObservableFeatureFlagProvider`
2. Implement `getValue`, `getChildren`, and optionally `refresh`, `shutdown`, etc.
3. Register with `FeatureFlags.setProvider(...)`

---

## Default Fallbacks

If a flag is missing, it returns a "null value" (safe defaults):

```java
boolean enabled = provider.get("unknown").asBoolean(); // false
String name = provider.get("missing").asString();       // ""
```
