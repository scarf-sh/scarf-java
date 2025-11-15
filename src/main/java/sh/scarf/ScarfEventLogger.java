package sh.scarf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A minimal, dependency-free Java client for sending telemetry events to Scarf.
 */
public class ScarfEventLogger {
    private final String endpointUrl;
    private final Duration defaultTimeout;
    private final Map<String, String> env;
    private final boolean verbose;
    private final String userAgent;

    /**
     * Create a logger with default timeout of 3.0 seconds.
     */
    public ScarfEventLogger(String endpointUrl) {
        this(endpointUrl, 3.0, System.getenv());
    }

    /**
     * Create a logger with a custom default timeout in seconds.
     */
    public ScarfEventLogger(String endpointUrl, double defaultTimeoutSeconds) {
        this(endpointUrl, defaultTimeoutSeconds, System.getenv());
    }

    /**
     * Internal constructor with injectable environment (for testing).
     */
    public ScarfEventLogger(String endpointUrl, double defaultTimeoutSeconds, Map<String, String> environment) {
        Objects.requireNonNull(endpointUrl, "endpointUrl");
        if (endpointUrl.isBlank()) throw new IllegalArgumentException("endpointUrl must not be blank");
        this.endpointUrl = endpointUrl;
        this.defaultTimeout = Duration.ofMillis(Math.max(0, (long) (defaultTimeoutSeconds * 1000)));
        this.env = environment == null ? Collections.emptyMap() : new HashMap<>(environment);
        this.verbose = isTruthy(env.get("SCARF_VERBOSE"));
        this.userAgent = buildUserAgent();
    }

    /**
     * Send an event with the default timeout.
     * Returns true if the event was sent and acknowledged by a 2xx HTTP code.
     */
    public boolean logEvent(Map<String, ?> properties) {
        return logEvent(properties, this.defaultTimeout);
    }

    /**
     * Send an event with a per-call timeout in seconds.
     */
    public boolean logEvent(Map<String, ?> properties, double timeoutSeconds) {
        Duration timeout = Duration.ofMillis(Math.max(0, (long) (timeoutSeconds * 1000)));
        return logEvent(properties, timeout);
    }

    private boolean logEvent(Map<String, ?> properties, Duration timeout) {
        if (isDisabled()) {
            if (verbose) System.err.println("Scarf analytics disabled via environment variable.");
            return false;
        }

        Map<String, ?> props = (properties == null) ? Collections.emptyMap() : properties;
        String body = JsonUtil.toJsonProperties(props);
        if (verbose) {
            System.err.println("Scarf payload: " + body);
            System.err.println("Scarf user-agent: " + userAgent);
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("User-Agent", userAgent)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            boolean ok = code >= 200 && code < 300;
            if (verbose) {
                String respBody = response.body();
                System.err.println("Scarf response status=" + code + ", body=" + (respBody == null ? "" : respBody));
            }
            return ok;
        } catch (IOException | InterruptedException e) {
            if (verbose) {
                System.err.println("Scarf request failed: " + e.getMessage());
            }
            // Restore interrupt flag if interrupted
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return false;
        } catch (RuntimeException e) {
            if (verbose) {
                System.err.println("Scarf request error: " + e.getMessage());
            }
            return false;
        }
    }

    private boolean isDisabled() {
        return isTruthy(env.get("DO_NOT_TRACK")) || isTruthy(env.get("SCARF_NO_ANALYTICS"));
    }

    private static boolean isTruthy(String v) {
        if (v == null) return false;
        String s = v.trim().toLowerCase(Locale.ROOT);
        return s.equals("1") || s.equals("true") || s.equals("yes") || s.equals("on");
    }

    private static String buildUserAgent() {
        String baseVersion = "dev";
        try {
            Package p = ScarfEventLogger.class.getPackage();
            if (p != null && p.getImplementationVersion() != null) {
                baseVersion = p.getImplementationVersion();
            }
        } catch (Throwable ignored) {
        }

        String extra = "";
        try {
            String osName = System.getProperty("os.name", "unknown");
            String platformName;
            String lower = osName.toLowerCase(Locale.ROOT);
            if (lower.contains("mac")) {
                platformName = "macOS";
            } else if (lower.contains("linux")) {
                platformName = "linux";
            } else if (lower.contains("windows")) {
                platformName = "windows";
            } else {
                platformName = lower.isBlank() ? "unknown" : lower;
            }

            String arch = System.getProperty("os.arch", "unknown");
            if (arch == null || arch.isBlank()) arch = "unknown";

            String jver = System.getProperty("java.version", "unknown");
            if (jver == null || jver.isBlank()) jver = "unknown";

            extra = " (platform=" + platformName + "; arch=" + arch + ", java=" + jver + ")";
        } catch (Throwable ignored) {
            // fall back to base UA
        }

        return "scarf-java/" + baseVersion + extra;
    }
}
