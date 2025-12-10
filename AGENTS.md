# Scarf Java – Agent Notes

This playbook gives future agents the quickest path to productive, low-risk work on the Java SDK.

## TL;DR
- Primary entry point is `src/main/java/sh/scarf/ScarfEventLogger.java`; its JSON helper lives in `JsonUtil.java`.
- Run the full unit suite with `mvn -q test`. Use the integration test class’ embedded HTTP server to exercise HTTP flows.
- Respect runtime toggles: analytics disablement via `DO_NOT_TRACK`/`SCARF_NO_ANALYTICS`, verbose diagnostics via `SCARF_VERBOSE`.
- Example usage is in `src/main/java/sh/scarf/examples/LiveExampleBasic.java`.
- Releases are tag-driven (`git tag vX.Y.Z && git push origin vX.Y.Z`); the GitHub workflow handles publishing.

## Project Layout
- `pom.xml`: Maven build config, Java 11+, JUnit 5, no runtime deps.
- `src/main/java/sh/scarf/ScarfEventLogger.java`: Public client API; performs payload serialization, timeout handling, user-agent construction, and HTTP POST.
- `src/main/java/sh/scarf/JsonUtil.java`: Minimal JSON encoder for primitives, maps, lists, arrays; keeps dependency footprint zero.
- `src/main/java/sh/scarf/examples/LiveExampleBasic.java`: Sends sample events against `https://scarf.gateway.scarf.sh/scarf-java`.
- `src/test/java/sh/scarf/*`: JUnit tests covering JSON encoding edge cases, environment switches, verbose logging, and a real HTTP round trip against an in-process `HttpServer`.
- `README.md`: Installation, usage, development, and publishing instructions. Mirror its tone when expanding docs.

## Development Workflow
- Use Maven for everything (`mvn -q test`, `mvn -q -DskipTests package`). The repo also ships a `nix-shell` for an on-demand Java 17 + Maven environment (`nix-shell --run "mvn -q test"`).
- Tests rely on standard JUnit assertions; no extra runner configuration required. The integration test spins up `com.sun.net.httpserver.HttpServer` automatically—no external services needed.
- Keep the library dependency-free. Prefer standard library features; justify any new dependency with maintainers before adding it.
- Stick to Java 11 language features unless asked otherwise.

## Runtime Behavior & Diagnostics
- Disable analytics by setting either `DO_NOT_TRACK=1` or `SCARF_NO_ANALYTICS=1`. `ScarfEventLogger` returns `false` immediately in these cases—tests assert this contract.
- Verbose mode (`SCARF_VERBOSE=1`) streams payloads, user-agent strings, and response summaries to `System.err`. Integration tests capture this output; regressions will surface quickly.
- User-Agent construction falls back to `dev` if the JAR manifest lacks `Implementation-Version`. When packaging, ensure the manifest is populated (handled automatically via Maven during release).

## Extending the SDK
- **Adding event helpers**: Build convenience wrappers around `logEvent` inside `ScarfEventLogger` or a new utility class. Maintain the class’ thread-safety (no shared mutable state beyond the constructor-populated fields).
- **Timeouts & retries**: Default timeout lives in the constructor; per-call overrides convert seconds → `Duration`. Any new retry/backoff logic should be opt-in to preserve the current lightweight behavior.
- **JSON enhancements**: Add serialization paths to `JsonUtil` if new data types are needed. Unit-test edge cases (nulls, escaping, NaN/Infinity) similarly to existing coverage.
- **Configuration**: If introducing new env toggles, update `README.md` and add focused tests that exercise the new switch.

## Testing Checklist
- Run `mvn -q test` locally. The suite is fast (<5s) and must remain so.
- For changes affecting HTTP behavior, extend `ScarfEventLoggerIntegrationTest` or add a sibling class; prefer deterministic embedded servers over external mocks.
- If new JSON cases are added, cover them in `JsonUtilTest`.
- Manual smoke: use `LiveExampleBasic` (`mvn -q -DskipTests compile && java -cp target/classes sh.scarf.examples.LiveExampleBasic`) against a non-production endpoint.

## Releasing & Versioning
- Release cadence is tag-based. Bump versions by creating a tag (`git tag v0.1.0`) and pushing it; GitHub Actions updates the manifest version, builds, signs, and ships to Maven Central.
- Update dependency snippets in `README.md` when releasing (Maven & Gradle examples).
- Ensure manifest metadata is correct before tagging (`mvn -q -DskipTests package` writes the version).

## Ready-to-Ship Checklist
- [ ] Code compiles (`mvn -q -DskipTests compile`).
- [ ] Tests pass (`mvn -q test`).
- [ ] Documentation updated (README and this file if guidance changes).
- [ ] No new dependencies without discussion.
- [ ] Verbose logging remains helpful and secure (no secrets emitted).

Treat this file as a living document—keep it aligned with reality after every meaningful change.
