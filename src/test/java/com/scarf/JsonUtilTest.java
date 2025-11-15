package com.scarf;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilTest {
    @Test
    void emptyMapProducesEmptyObject() {
        String json = JsonUtil.toJsonProperties(Collections.emptyMap());
        assertEquals("{}", json);
    }

    @Test
    void simpleMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("event", "download");
        m.put("version", "1.0.0");
        String json = JsonUtil.toJsonProperties(m);
        assertEquals("{\"event\":\"download\",\"version\":\"1.0.0\"}", json);
    }

    @Test
    void toStringConversionForValues() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a", 1);
        m.put("b", true);
        m.put("c", 12.5);
        String json = JsonUtil.toJsonProperties(m);
        assertEquals("{\"a\":\"1\",\"b\":\"true\",\"c\":\"12.5\"}", json);
    }

    @Test
    void stringEscaping() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("s", "a\"b\\c\n");
        String json = JsonUtil.toJsonProperties(m);
        assertEquals("{\"s\":\"a\\\"b\\\\c\\n\"}", json);
    }
}
