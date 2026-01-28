package dev.rynko.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.rynko.RynkoConfig;
import dev.rynko.exceptions.RynkoException;
import dev.rynko.models.ApiError;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for making requests to the Rynko API with automatic retry
 * and exponential backoff.
 */
public class HttpClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String USER_AGENT = "rynko-java/1.0.0";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final RynkoConfig config;
    private final Random random;

    public HttpClient(RynkoConfig config) {
        this.baseUrl = config.getBaseUrl();
        this.apiKey = config.getApiKey();
        this.config = config;
        this.random = new Random();

        this.client = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Calculate delay for exponential backoff with jitter.
     */
    private long calculateDelay(int attempt, Long retryAfterMs) {
        // If server specified Retry-After, respect it (with jitter)
        if (retryAfterMs != null) {
            long jitter = (long) (random.nextDouble() * config.getMaxJitterMs());
            return Math.min(retryAfterMs + jitter, config.getMaxDelayMs());
        }

        // Exponential backoff: initialDelay * 2^attempt
        long exponentialDelay = config.getInitialDelayMs() * (long) Math.pow(2, attempt);

        // Add random jitter to prevent thundering herd
        long jitter = (long) (random.nextDouble() * config.getMaxJitterMs());

        // Cap at maxDelay
        return Math.min(exponentialDelay + jitter, config.getMaxDelayMs());
    }

    /**
     * Parse Retry-After header value to milliseconds.
     */
    private Long parseRetryAfter(String retryAfter) {
        if (retryAfter == null || retryAfter.isEmpty()) {
            return null;
        }

        // Try to parse as integer (seconds)
        try {
            return Long.parseLong(retryAfter) * 1000;
        } catch (NumberFormatException ignored) {
        }

        // HTTP-date parsing is complex and rarely used, so we skip it
        return null;
    }

    /**
     * Check if the status code should trigger a retry.
     */
    private boolean shouldRetry(int statusCode) {
        if (!config.isRetryEnabled()) {
            return false;
        }
        return config.getRetryableStatuses().contains(statusCode);
    }

    /**
     * Makes a GET request.
     */
    public <T> T get(String path, Class<T> responseType) throws RynkoException {
        return get(path, null, responseType);
    }

    /**
     * Makes a GET request with query parameters.
     */
    public <T> T get(String path, Map<String, String> queryParams, Class<T> responseType) throws RynkoException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (entry.getValue() != null) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .build();

        return executeWithRetry(request, responseType);
    }

    /**
     * Makes a GET request with a TypeReference for generic types.
     */
    public <T> T get(String path, Map<String, String> queryParams, TypeReference<T> typeReference) throws RynkoException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (entry.getValue() != null) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .build();

        return executeWithRetry(request, typeReference);
    }

    /**
     * Makes a POST request.
     */
    public <T> T post(String path, Object body, Class<T> responseType) throws RynkoException {
        return post(path, body, responseType, null);
    }

    /**
     * Makes a POST request with a TypeReference.
     */
    public <T> T post(String path, Object body, TypeReference<T> typeReference) throws RynkoException {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            RequestBody requestBody = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url(baseUrl + path)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            return executeWithRetry(request, typeReference);
        } catch (IOException e) {
            throw new RynkoException("Failed to serialize request body", e);
        }
    }

    private <T> T post(String path, Object body, Class<T> responseType, Void unused) throws RynkoException {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            RequestBody requestBody = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url(baseUrl + path)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            return executeWithRetry(request, responseType);
        } catch (IOException e) {
            throw new RynkoException("Failed to serialize request body", e);
        }
    }

    /**
     * Makes a PUT request.
     */
    public <T> T put(String path, Object body, Class<T> responseType) throws RynkoException {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            RequestBody requestBody = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url(baseUrl + path)
                    .put(requestBody)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            return executeWithRetry(request, responseType);
        } catch (IOException e) {
            throw new RynkoException("Failed to serialize request body", e);
        }
    }

    /**
     * Makes a PATCH request.
     */
    public <T> T patch(String path, Object body, Class<T> responseType) throws RynkoException {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            RequestBody requestBody = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url(baseUrl + path)
                    .patch(requestBody)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            return executeWithRetry(request, responseType);
        } catch (IOException e) {
            throw new RynkoException("Failed to serialize request body", e);
        }
    }

    /**
     * Makes a DELETE request.
     */
    public void delete(String path) throws RynkoException {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .delete()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", USER_AGENT)
                .build();

        executeVoidWithRetry(request);
    }

    /**
     * Makes a GET request to an absolute URL (not relative to base URL).
     */
    public <T> T getAbsolute(String absoluteUrl, Class<T> responseType) throws RynkoException {
        Request request = new Request.Builder()
                .url(absoluteUrl)
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .build();

        return executeWithRetry(request, responseType);
    }

    /**
     * Makes a GET request to an absolute URL with query parameters.
     */
    public <T> T getAbsolute(String absoluteUrl, Map<String, String> queryParams, TypeReference<T> typeReference) throws RynkoException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(absoluteUrl).newBuilder();
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (entry.getValue() != null) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .build();

        return executeWithRetry(request, typeReference);
    }

    /**
     * Gets the base URL without the /api/v1 suffix (for auth endpoints).
     */
    public String getBaseUrlWithoutVersion() {
        if (baseUrl.endsWith("/api/v1")) {
            return baseUrl.substring(0, baseUrl.length() - 7);
        }
        return baseUrl;
    }

    private <T> T executeWithRetry(Request request, Class<T> responseType) throws RynkoException {
        int maxAttempts = config.isRetryEnabled() ? config.getMaxRetries() : 1;
        RynkoException lastError = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    if (shouldRetry(response.code())) {
                        Long retryAfterMs = parseRetryAfter(response.header("Retry-After"));
                        long delay = calculateDelay(attempt, retryAfterMs);

                        // Store the error in case this is the last attempt
                        lastError = createExceptionFromResponse(response.code(), responseBody);

                        // If we have more attempts, wait and retry
                        if (attempt < maxAttempts - 1) {
                            Thread.sleep(delay);
                            continue;
                        }
                    }
                    handleError(response.code(), responseBody);
                }

                if (responseType == Void.class || responseBody.isEmpty()) {
                    return null;
                }

                return objectMapper.readValue(responseBody, responseType);
            } catch (IOException e) {
                throw new RynkoException("Request failed", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RynkoException("Request interrupted during retry", e);
            }
        }

        // If we've exhausted all retries, throw the last error
        if (lastError != null) {
            throw lastError;
        }

        throw new RynkoException("Request failed after retries", null, 0);
    }

    private <T> T executeWithRetry(Request request, TypeReference<T> typeReference) throws RynkoException {
        int maxAttempts = config.isRetryEnabled() ? config.getMaxRetries() : 1;
        RynkoException lastError = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    if (shouldRetry(response.code())) {
                        Long retryAfterMs = parseRetryAfter(response.header("Retry-After"));
                        long delay = calculateDelay(attempt, retryAfterMs);

                        // Store the error in case this is the last attempt
                        lastError = createExceptionFromResponse(response.code(), responseBody);

                        // If we have more attempts, wait and retry
                        if (attempt < maxAttempts - 1) {
                            Thread.sleep(delay);
                            continue;
                        }
                    }
                    handleError(response.code(), responseBody);
                }

                if (responseBody.isEmpty()) {
                    return null;
                }

                return objectMapper.readValue(responseBody, typeReference);
            } catch (IOException e) {
                throw new RynkoException("Request failed", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RynkoException("Request interrupted during retry", e);
            }
        }

        // If we've exhausted all retries, throw the last error
        if (lastError != null) {
            throw lastError;
        }

        throw new RynkoException("Request failed after retries", null, 0);
    }

    private void executeVoidWithRetry(Request request) throws RynkoException {
        int maxAttempts = config.isRetryEnabled() ? config.getMaxRetries() : 1;
        RynkoException lastError = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    if (shouldRetry(response.code())) {
                        Long retryAfterMs = parseRetryAfter(response.header("Retry-After"));
                        long delay = calculateDelay(attempt, retryAfterMs);

                        // Store the error in case this is the last attempt
                        lastError = createExceptionFromResponse(response.code(), responseBody);

                        // If we have more attempts, wait and retry
                        if (attempt < maxAttempts - 1) {
                            Thread.sleep(delay);
                            continue;
                        }
                    }
                    handleError(response.code(), responseBody);
                }
                return;
            } catch (IOException e) {
                throw new RynkoException("Request failed", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RynkoException("Request interrupted during retry", e);
            }
        }

        // If we've exhausted all retries, throw the last error
        if (lastError != null) {
            throw lastError;
        }

        throw new RynkoException("Request failed after retries", null, 0);
    }

    private RynkoException createExceptionFromResponse(int statusCode, String responseBody) {
        try {
            ApiError error = objectMapper.readValue(responseBody, ApiError.class);
            return new RynkoException(error.getMessage(), error.getCode(), statusCode);
        } catch (IOException e) {
            return new RynkoException("HTTP " + statusCode + ": " + responseBody, null, statusCode);
        }
    }

    private void handleError(int statusCode, String responseBody) throws RynkoException {
        try {
            ApiError error = objectMapper.readValue(responseBody, ApiError.class);
            throw new RynkoException(error.getMessage(), error.getCode(), statusCode);
        } catch (IOException e) {
            throw new RynkoException("HTTP " + statusCode + ": " + responseBody, null, statusCode);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
