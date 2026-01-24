package com.renderbase;

/**
 * Configuration for the Renderbase client.
 */
public class RenderbaseConfig {

    private static final String DEFAULT_BASE_URL = "https://api.renderbase.dev/api/v1";
    private static final int DEFAULT_TIMEOUT_MS = 30000;
    private static final int DEFAULT_MAX_RETRIES = 3;

    private final String apiKey;
    private final String baseUrl;
    private final int timeoutMs;
    private final int maxRetries;

    /**
     * Creates a configuration with the specified API key.
     *
     * @param apiKey Your Renderbase API key
     */
    public RenderbaseConfig(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    /**
     * Creates a configuration with the specified API key and base URL.
     *
     * @param apiKey  Your Renderbase API key
     * @param baseUrl Custom API base URL
     */
    public RenderbaseConfig(String apiKey, String baseUrl) {
        this(apiKey, baseUrl, DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES);
    }

    private RenderbaseConfig(String apiKey, String baseUrl, int timeoutMs, int maxRetries) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        this.timeoutMs = timeoutMs;
        this.maxRetries = maxRetries;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Creates a new configuration builder.
     *
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for RenderbaseConfig.
     */
    public static class Builder {
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private int timeoutMs = DEFAULT_TIMEOUT_MS;
        private int maxRetries = DEFAULT_MAX_RETRIES;

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public RenderbaseConfig build() {
            return new RenderbaseConfig(apiKey, baseUrl, timeoutMs, maxRetries);
        }
    }
}
