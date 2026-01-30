package dev.rynko;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for the Rynko client.
 */
public class RynkoConfig {

    private static final String DEFAULT_BASE_URL = "https://api.rynko.dev/api/v1";
    private static final int DEFAULT_TIMEOUT_MS = 30000;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final int DEFAULT_INITIAL_DELAY_MS = 1000;
    private static final int DEFAULT_MAX_DELAY_MS = 30000;
    private static final int DEFAULT_MAX_JITTER_MS = 1000;
    private static final Set<Integer> DEFAULT_RETRYABLE_STATUSES =
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList(429, 503, 504)));

    private final String apiKey;
    private final String baseUrl;
    private final int timeoutMs;
    private final int maxRetries;
    private final int initialDelayMs;
    private final int maxDelayMs;
    private final int maxJitterMs;
    private final Set<Integer> retryableStatuses;
    private final boolean retryEnabled;

    /**
     * Creates a configuration with the specified API key.
     *
     * @param apiKey Your Rynko API key
     */
    public RynkoConfig(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    /**
     * Creates a configuration with the specified API key and base URL.
     *
     * @param apiKey  Your Rynko API key
     * @param baseUrl Custom API base URL
     */
    public RynkoConfig(String apiKey, String baseUrl) {
        this(apiKey, baseUrl, DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
             DEFAULT_INITIAL_DELAY_MS, DEFAULT_MAX_DELAY_MS, DEFAULT_MAX_JITTER_MS,
             DEFAULT_RETRYABLE_STATUSES, true);
    }

    private RynkoConfig(String apiKey, String baseUrl, int timeoutMs, int maxRetries,
                             int initialDelayMs, int maxDelayMs, int maxJitterMs,
                             Set<Integer> retryableStatuses, boolean retryEnabled) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        this.timeoutMs = timeoutMs;
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.maxJitterMs = maxJitterMs;
        this.retryableStatuses = retryableStatuses;
        this.retryEnabled = retryEnabled;
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

    public int getInitialDelayMs() {
        return initialDelayMs;
    }

    public int getMaxDelayMs() {
        return maxDelayMs;
    }

    public int getMaxJitterMs() {
        return maxJitterMs;
    }

    public Set<Integer> getRetryableStatuses() {
        return retryableStatuses;
    }

    public boolean isRetryEnabled() {
        return retryEnabled;
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
     * Builder for RynkoConfig.
     */
    public static class Builder {
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private int timeoutMs = DEFAULT_TIMEOUT_MS;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private int initialDelayMs = DEFAULT_INITIAL_DELAY_MS;
        private int maxDelayMs = DEFAULT_MAX_DELAY_MS;
        private int maxJitterMs = DEFAULT_MAX_JITTER_MS;
        private Set<Integer> retryableStatuses = DEFAULT_RETRYABLE_STATUSES;
        private boolean retryEnabled = true;

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

        /**
         * Sets the maximum number of retry attempts (default: 5).
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets the initial delay between retries in milliseconds (default: 1000).
         */
        public Builder initialDelayMs(int initialDelayMs) {
            this.initialDelayMs = initialDelayMs;
            return this;
        }

        /**
         * Sets the maximum delay between retries in milliseconds (default: 30000).
         */
        public Builder maxDelayMs(int maxDelayMs) {
            this.maxDelayMs = maxDelayMs;
            return this;
        }

        /**
         * Sets the maximum jitter to add to delay in milliseconds (default: 1000).
         */
        public Builder maxJitterMs(int maxJitterMs) {
            this.maxJitterMs = maxJitterMs;
            return this;
        }

        /**
         * Sets the HTTP status codes that should trigger a retry (default: 429, 503, 504).
         */
        public Builder retryableStatuses(Set<Integer> retryableStatuses) {
            this.retryableStatuses = Collections.unmodifiableSet(new HashSet<>(retryableStatuses));
            return this;
        }

        /**
         * Enables or disables automatic retry (default: true).
         */
        public Builder retryEnabled(boolean retryEnabled) {
            this.retryEnabled = retryEnabled;
            return this;
        }

        public RynkoConfig build() {
            return new RynkoConfig(apiKey, baseUrl, timeoutMs, maxRetries,
                                       initialDelayMs, maxDelayMs, maxJitterMs,
                                       retryableStatuses, retryEnabled);
        }
    }
}
