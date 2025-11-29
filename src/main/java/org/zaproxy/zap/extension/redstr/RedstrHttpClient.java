package org.zaproxy.zap.extension.redstr;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HTTP client for communicating with the redstr-server API.
 * Handles transformation requests and response parsing.
 */
public class RedstrHttpClient {
    
    private static final Logger LOGGER = LogManager.getLogger(RedstrHttpClient.class);
    private static final String DEFAULT_API_URL = "http://localhost:8080";
    private static final int DEFAULT_TIMEOUT_MS = 5000;
    
    private final HttpClient httpClient;
    private String apiUrl;
    private Duration timeout;
    
    /**
     * Creates a new RedstrHttpClient with default settings.
     */
    public RedstrHttpClient() {
        this(DEFAULT_API_URL, DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * Creates a new RedstrHttpClient with custom settings.
     * 
     * @param apiUrl The base URL of the redstr-server API
     * @param timeoutMs Request timeout in milliseconds
     */
    public RedstrHttpClient(String apiUrl, int timeoutMs) {
        this.apiUrl = apiUrl != null ? apiUrl : DEFAULT_API_URL;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        
        LOGGER.debug("RedstrHttpClient initialized with URL: {} and timeout: {}ms", 
                this.apiUrl, timeoutMs);
    }
    
    /**
     * Transforms input using the specified redstr function.
     * 
     * @param function The redstr function name (e.g., "sql_comment_injection")
     * @param input The input string to transform
     * @return The transformed string
     * @throws IOException If the request fails or times out
     */
    public String transform(String function, String input) throws IOException {
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        if (input == null) {
            input = "";
        }
        
        String jsonPayload = buildJsonPayload(function, input);
        
        LOGGER.debug("Sending transformation request - Function: {}, Input length: {}", 
                function, input.length());
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/transform"))
                    .header("Content-Type", "application/json")
                    .timeout(timeout)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                String errorMsg = String.format("Request failed with status %d: %s", 
                        response.statusCode(), response.body());
                LOGGER.error(errorMsg);
                throw new IOException(errorMsg);
            }
            
            String result = parseResponse(response.body());
            LOGGER.debug("Transformation successful - Output length: {}", result.length());
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request was interrupted", e);
        }
    }
    
    /**
     * Builds a JSON payload for the transformation request.
     */
    private String buildJsonPayload(String function, String input) {
        // Escape JSON special characters
        String escapedInput = escapeJson(input);
        String escapedFunction = escapeJson(function);
        
        return String.format("{\"function\":\"%s\",\"input\":\"%s\"}", 
                escapedFunction, escapedInput);
    }
    
    /**
     * Escapes JSON special characters in a string.
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
    
    /**
     * Parses the JSON response and extracts the output field.
     */
    private String parseResponse(String jsonResponse) throws IOException {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            throw new IOException("Empty response from server");
        }
        
        // Simple JSON parsing for the "output" field
        // Format: {"output":"transformed_string"}
        int outputStart = jsonResponse.indexOf("\"output\"");
        if (outputStart == -1) {
            throw new IOException("Invalid response format: missing 'output' field");
        }
        
        int valueStart = jsonResponse.indexOf("\"", outputStart + 8);
        if (valueStart == -1) {
            throw new IOException("Invalid response format: malformed output value");
        }
        
        valueStart++; // Move past the opening quote
        int valueEnd = findClosingQuote(jsonResponse, valueStart);
        if (valueEnd == -1) {
            throw new IOException("Invalid response format: unterminated output string");
        }
        
        String output = jsonResponse.substring(valueStart, valueEnd);
        return unescapeJson(output);
    }
    
    /**
     * Finds the closing quote, accounting for escaped quotes.
     */
    private int findClosingQuote(String str, int start) {
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == '"') {
                // Check if it's escaped
                int backslashCount = 0;
                for (int j = i - 1; j >= start && str.charAt(j) == '\\'; j--) {
                    backslashCount++;
                }
                // If even number of backslashes (including 0), the quote is not escaped
                if (backslashCount % 2 == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Unescapes JSON special characters.
     */
    private String unescapeJson(String str) {
        if (str == null) {
            return "";
        }
        
        // Process backslash last to avoid incorrectly unescaping already-processed sequences
        return str.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\\\", "\\");
    }
    
    /**
     * Tests the connection to the redstr server.
     * Note: This attempts a basic transformation. If the server doesn't support
     * the test function, it may return false even when the server is running.
     * 
     * @return true if the server is reachable and responds successfully, false otherwise
     */
    public boolean testConnection() {
        try {
            // Try a simple transformation to test connectivity
            // Using a basic function that should be supported by most configurations
            transform("base64_encode", "test");
            return true;
        } catch (Exception e) {
            LOGGER.debug("Connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Sets the API URL.
     * 
     * @param apiUrl The new API URL
     */
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl != null ? apiUrl : DEFAULT_API_URL;
        LOGGER.info("API URL updated to: {}", this.apiUrl);
    }
    
    /**
     * Gets the current API URL.
     * 
     * @return The API URL
     */
    public String getApiUrl() {
        return apiUrl;
    }
    
    /**
     * Sets the request timeout.
     * 
     * @param timeoutMs Timeout in milliseconds
     */
    public void setTimeout(int timeoutMs) {
        this.timeout = Duration.ofMillis(timeoutMs);
        LOGGER.info("Timeout updated to: {}ms", timeoutMs);
    }
}
