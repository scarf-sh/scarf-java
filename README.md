scarf-java
================

[![CI](https://github.com/scarf-sh/scarf-java/actions/workflows/ci.yml/badge.svg)](https://github.com/scarf-sh/scarf-java/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/sh.scarf/scarf-sdk.svg)](https://central.sonatype.com/artifact/sh.scarf/scarf-sdk)

A Java client for sending telemetry events to Scarf.

Installation
------------

This library targets Java 11+ and has no runtime dependencies.

- Maven (example, version subject to change). Add to your `pom.xml`, making sure to use the latest version:

```xml
<dependency>
  <groupId>sh.scarf</groupId>
  <artifactId>scarf-sdk</artifactId>
  <version>0.1.4</version>
</dependency>
```

- Gradle (example):

```groovy
implementation "sh.scarf:scarf-sdk:0.1.4"
```

Usage
-----

Import and initialize with the required endpoint URL:

```java
import sh.scarf.ScarfEventLogger;
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
- User-Agent version:
  - When running from classes (`java -cp target/classes ...`) the version shows as `dev`.
  - When packaged as a JAR, the version is taken from the JAR manifest (`Implementation-Version` = `${project.version}`), e.g. `0.1.0` or `0.1.0-SNAPSHOT`.

Development
-----------

Clone the repository and run tests with Maven:

```bash
mvn -q -DskipTests=false test
```

Using Nix (optional)
--------------------

This repo provides a Nix shell for a zero-setup environment. It prefers Temurin 17 when available in your `nixpkgs`, and falls back to OpenJDK 17.

```bash
# Drop into a shell with JDK + Maven
nix-shell

# Run tests
mvn -q test
```

Runnable example
----------------

Sends two events to the public endpoint `https://scarf.gateway.scarf.sh/scarf-java`.

```bash
# Ensure analytics are not disabled
unset DO_NOT_TRACK SCARF_NO_ANALYTICS

# Option A: compile then run classes
mvn -q -DskipTests package
java -cp target/classes sh.scarf.examples.LiveExampleBasic

# Option B: run directly from classes without packaging
mvn -q -DskipTests compile
java -cp target/classes sh.scarf.examples.LiveExampleBasic

# With Nix
nix-shell --run "mvn -q -DskipTests compile && java -cp target/classes sh.scarf.examples.LiveExampleBasic"
```

Publishing
----------

Publishing uses Git tags and GitHub Actions to deploy to Maven Central via Sonatype Central (modern flow).

Setup (one-time)
- Create an account on Sonatype Central and ensure ownership of the `sh.scarf` groupId.
- Create a Publishing Token in Sonatype Central (username is typically `token`, password is the token value).
- Generate a GPG key (publishing requires signed artifacts).
- Add the following GitHub repository secrets:
  - `CENTRAL_USERNAME`: Your Central publishing username (often `token`)
  - `CENTRAL_PASSWORD`: Your Central publishing token value
  - `GPG_PRIVATE_KEY`: Your private key, either ASCII‑armored (begins with `-----BEGIN PGP PRIVATE KEY BLOCK-----`) or the same content base64‑encoded
  - `GPG_PASSPHRASE`: Passphrase for the key

Release
- Create and push a version tag. The workflow derives the version from the tag (strip the leading `v`).

```bash
git tag v0.1.0
git push origin v0.1.0
```

The `Release` workflow will:
- Set the Maven project version to `0.1.0`
- Build, sign, and publish to Maven Central via Sonatype Central tokens

Troubleshooting
- If you see `base64: invalid input` or `gpg: no valid OpenPGP data found`, paste the ASCII‑armored private key directly into the `GPG_PRIVATE_KEY` secret (not base64). The workflow auto‑detects and imports either format.
- If Central reports invalid signature or cannot find your public key by fingerprint:
  - Upload your public key to a supported keyserver, e.g. `keys.openpgp.org`, and verify the email identity used for the key (you will receive a verification email).
  - Alternatively, add the same public key in the Sonatype Central portal under your account’s PGP keys.
  - Make sure the key fingerprint in CI matches the one you uploaded (the workflow prints it during release).


License
-------

Apache 2.0
