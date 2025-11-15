scarf-java
================

A Java client for sending telemetry events to Scarf.

Installation
------------

This library targets Java 11+ and has no runtime dependencies.

- Maven (example, version subject to change):

  Add to your `pom.xml` once published:

  <dependency>
    <groupId>com.scarf</groupId>
    <artifactId>scarf-java</artifactId>
    <version>0.1.0</version>
  </dependency>

- Gradle (example): `implementation("com.scarf:scarf-java:0.1.0")`

Usage
-----

Import and initialize with the required endpoint URL:

```java
import com.scarf.ScarfEventLogger;
import java.util.*;

ScarfEventLogger logger = new ScarfEventLogger(
    "https://your-scarf-endpoint.com",
    5.0 // Optional: default timeout seconds (default: 3.0)
);

// Send an event with properties (String keys; values are stringified)
Map<String, Object> props = new LinkedHashMap<>();
props.put("event", "package_download");
props.put("package", "scarf");
props.put("version", "1.0.0");
boolean success = logger.logEvent(props);

// Send an event with a custom timeout
success = logger.logEvent(Map.of("event", "custom_event"), 1.0);

// Empty properties are allowed
success = logger.logEvent(Collections.emptyMap());
```

Configuration
-------------

The client can be configured through environment variables:

- `DO_NOT_TRACK=1`: Disable analytics
- `SCARF_NO_ANALYTICS=1`: Disable analytics (alternative)
- `SCARF_VERBOSE=1`: Enable verbose logging

Features
--------

- Simple API for sending telemetry events
- Environment variable configuration
- Configurable timeouts (default: 3 seconds)
- Respects user Do Not Track settings
- Verbose logging mode for debugging
- 100% dependency free at runtime

Notes
-----

- Properties must be a `Map<String, ?>`. Values are converted to strings via `toString()` (null becomes JSON `null`).

Development
-----------

Clone the repository and run tests with Maven:

```
mvn -q -DskipTests=false test
```

Using Nix (optional)
--------------------

This repo provides a Nix shell for a zero-setup environment. It prefers Temurin 17 when available in your `nixpkgs`, and falls back to OpenJDK 17.

```
# Drop into a shell with JDK + Maven
nix-shell

# Run tests
mvn -q test
```

Publishing
----------

To publish a new version (example outline):

1. Update version in `pom.xml`.
2. Create and push a new tag:

```
git tag v0.1.0
git push origin v0.1.0
```

CI can be configured to build and publish to Maven Central when a new version tag is pushed.

License
-------

Apache 2.0
