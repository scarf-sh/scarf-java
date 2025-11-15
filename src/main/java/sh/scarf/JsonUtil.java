package sh.scarf;

import java.util.*;

/**
 * Minimal JSON serializer to avoid external dependencies.
 */
final class JsonUtil {
    private JsonUtil() {}

    static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return quote((String) value);
        if (value instanceof Number) return numberToJson((Number) value);
        if (value instanceof Boolean) return ((Boolean) value).toString();
        if (value instanceof Map) return mapToJson((Map<?, ?>) value);
        if (value instanceof List) return listToJson((List<?>) value);
        if (value.getClass().isArray()) return arrayToJson(value);
        // Fallback: use toString as string value
        return quote(String.valueOf(value));
    }

    /**
     * Serialize a flat map of String keys and values that will be converted via toString().
     * Values are encoded as JSON strings; null values become JSON null.
     */
    static String toJsonProperties(Map<String, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, ?> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(quote(e.getKey()));
            sb.append(':');
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else {
                sb.append(quote(String.valueOf(v)));
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private static String numberToJson(Number n) {
        if (n instanceof Double) {
            double d = (Double) n;
            if (Double.isNaN(d) || Double.isInfinite(d)) return "null";
        }
        if (n instanceof Float) {
            float f = (Float) n;
            if (Float.isNaN(f) || Float.isInfinite(f)) return "null";
        }
        return n.toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!(e.getKey() instanceof String)) continue; // skip non-string keys
            if (!first) sb.append(',');
            first = false;
            sb.append(quote((String) e.getKey()));
            sb.append(':');
            sb.append(toJson(e.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }

    private static String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(toJson(list.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }

    private static String arrayToJson(Object array) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int len = java.lang.reflect.Array.getLength(array);
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(',');
            sb.append(toJson(java.lang.reflect.Array.get(array, i)));
        }
        sb.append(']');
        return sb.toString();
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
