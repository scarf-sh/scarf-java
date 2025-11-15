package com.scarf;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ScarfEventLoggerTest {
    @Test
    void disabledByEnvironmentReturnsFalse() {
        Map<String, String> env = new HashMap<>();
        env.put("DO_NOT_TRACK", "1");
        ScarfEventLogger logger = new ScarfEventLogger("http://localhost:9", 0.1, env);
        boolean ok = logger.logEvent(Collections.<String, Object>emptyMap());
        assertFalse(ok, "Expected false when disabled by env");
    }
}
