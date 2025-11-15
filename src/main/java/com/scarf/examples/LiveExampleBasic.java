package com.scarf.examples;

import com.scarf.ScarfEventLogger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class LiveExampleBasic {
    private static final String ENDPOINT = "https://scarf.gateway.scarf.sh/scarf-java";

    public static void main(String[] args) {
        // Optional: set SCARF_VERBOSE=1 to see debug logs
        ScarfEventLogger logger = new ScarfEventLogger(ENDPOINT, 5.0);

        Map<String, Object> event1 = new LinkedHashMap<>();
        event1.put("event", "example_event_a");
        event1.put("package", "scarf-java");
        event1.put("version", "0.1.0-SNAPSHOT");
        event1.put("run_id", UUID.randomUUID());

        boolean ok1 = logger.logEvent(event1);
        System.out.println("Event 1 sent: " + ok1);

        Map<String, Object> event2 = new LinkedHashMap<>();
        event2.put("event", "example_event_b");
        event2.put("package", "scarf-java");
        event2.put("value", "B");
        event2.put("count", 2);

        boolean ok2 = logger.logEvent(event2, 2.0);
        System.out.println("Event 2 sent: " + ok2);
    }
}
