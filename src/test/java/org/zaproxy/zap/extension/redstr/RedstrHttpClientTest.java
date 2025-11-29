package org.zaproxy.zap.extension.redstr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RedstrHttpClient.
 */
class RedstrHttpClientTest {
    
    private RedstrHttpClient client;
    
    @BeforeEach
    void setUp() {
        client = new RedstrHttpClient();
    }
    
    @Test
    void testConstructorWithDefaults() {
        assertNotNull(client);
        assertEquals("http://localhost:8080", client.getApiUrl());
    }
    
    @Test
    void testConstructorWithCustomUrl() {
        RedstrHttpClient customClient = new RedstrHttpClient("http://example.com:9000", 3000);
        assertEquals("http://example.com:9000", customClient.getApiUrl());
    }
    
    @Test
    void testConstructorWithNullUrl() {
        RedstrHttpClient customClient = new RedstrHttpClient(null, 5000);
        assertEquals("http://localhost:8080", customClient.getApiUrl());
    }
    
    @Test
    void testSetApiUrl() {
        client.setApiUrl("http://newurl.com:8081");
        assertEquals("http://newurl.com:8081", client.getApiUrl());
    }
    
    @Test
    void testSetApiUrlToNull() {
        client.setApiUrl(null);
        assertEquals("http://localhost:8080", client.getApiUrl());
    }
    
    @Test
    void testTransformWithNullFunction() {
        assertThrows(IllegalArgumentException.class, () -> {
            client.transform(null, "input");
        });
    }
    
    @Test
    void testTransformWithEmptyFunction() {
        assertThrows(IllegalArgumentException.class, () -> {
            client.transform("", "input");
        });
    }
    
    @Test
    void testSetTimeout() {
        // Just verify it doesn't throw
        assertDoesNotThrow(() -> {
            client.setTimeout(10000);
        });
    }
    
    @Test
    void testGetApiUrl() {
        String url = client.getApiUrl();
        assertNotNull(url);
        assertFalse(url.isEmpty());
    }
}
