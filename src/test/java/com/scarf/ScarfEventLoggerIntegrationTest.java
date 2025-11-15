package com.scarf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ScarfEventLoggerIntegrationTest {
    private HttpServer server;
    private volatile String lastBody;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try (InputStream is = exchange.getRequestBody()) {
                    byte[] bytes = is.readAllBytes();
                    lastBody = new String(bytes, StandardCharsets.UTF_8);
                }
                byte[] resp = "ok".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, resp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp);
                }
            }
        });
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) server.stop(0);
        lastBody = null;
    }

    @Test
    void sendsEventSuccessfully() {
        String url = "http://localhost:" + server.getAddress().getPort() + "/";
        ScarfEventLogger logger = new ScarfEventLogger(url, 2.0, Collections.<String, String>emptyMap());

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("event", "package_download");
        props.put("package", "scarf");
        props.put("version", "1.0.0");

        boolean ok = logger.logEvent(props);
        assertTrue(ok, "Expected true for 2xx response");
        assertEquals(JsonUtil.toJsonProperties(props), lastBody);
    }
}
