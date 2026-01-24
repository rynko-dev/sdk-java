package com.renderbase.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.renderbase.RenderbaseConfig;
import com.renderbase.exceptions.RenderbaseException;
import com.renderbase.models.ApiError;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for making requests to the Renderbase API.
 */
public class HttpClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String USER_AGENT = "renderbase-java/1.0.0";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;

    public HttpClient(RenderbaseConfig config) {
        this.baseUrl = config.getBaseUrl();
        this.apiKey = config.getApiKey();

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
     * Makes a GET request.
     */
    public <T> T get(String path, Class<T> responseType) throws RenderbaseException {
        return get(path, null, responseType);
    }

    /**
     * Makes a GET request with query parameters.
     */
    public <T> T get(String path, Map<String, String> queryParams, Class<T> responseType) throws RenderbaseException {
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

        return execute(request, responseType);
    }

    /**
     * Makes a GET request with a TypeReference for generic types.
     */
    public <T> T get(String path, Map<String, String> queryParams, TypeReference<T> typeReference) throws RenderbaseException {
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

        return execute(request, typeReference);
    }

    /**
     * Makes a POST request.
     */
    public <T> T post(String path, Object body, Class<T> responseType) throws RenderbaseException {
        return post(path, body, responseType, null);
    }

    /**
     * Makes a POST request with a TypeReference.
     */
    public <T> T post(String path, Object body, TypeReference<T> typeReference) throws RenderbaseException {
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

            return execute(request, typeReference);
        } catch (IOException e) {
            throw new RenderbaseException("Failed to serialize request body", e);
        }
    }

    private <T> T post(String path, Object body, Class<T> responseType, Void unused) throws RenderbaseException {
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

            return execute(request, responseType);
        } catch (IOException e) {
            throw new RenderbaseException("Failed to serialize request body", e);
        }
    }

    /**
     * Makes a PUT request.
     */
    public <T> T put(String path, Object body, Class<T> responseType) throws RenderbaseException {
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

            return execute(request, responseType);
        } catch (IOException e) {
            throw new RenderbaseException("Failed to serialize request body", e);
        }
    }

    /**
     * Makes a PATCH request.
     */
    public <T> T patch(String path, Object body, Class<T> responseType) throws RenderbaseException {
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

            return execute(request, responseType);
        } catch (IOException e) {
            throw new RenderbaseException("Failed to serialize request body", e);
        }
    }

    /**
     * Makes a DELETE request.
     */
    public void delete(String path) throws RenderbaseException {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .delete()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", USER_AGENT)
                .build();

        executeVoid(request);
    }

    /**
     * Makes a GET request to an absolute URL (not relative to base URL).
     */
    public <T> T getAbsolute(String absoluteUrl, Class<T> responseType) throws RenderbaseException {
        Request request = new Request.Builder()
                .url(absoluteUrl)
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .build();

        return execute(request, responseType);
    }

    /**
     * Makes a GET request to an absolute URL with query parameters.
     */
    public <T> T getAbsolute(String absoluteUrl, Map<String, String> queryParams, TypeReference<T> typeReference) throws RenderbaseException {
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

        return execute(request, typeReference);
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

    private <T> T execute(Request request, Class<T> responseType) throws RenderbaseException {
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                handleError(response.code(), responseBody);
            }

            if (responseType == Void.class || responseBody.isEmpty()) {
                return null;
            }

            return objectMapper.readValue(responseBody, responseType);
        } catch (IOException e) {
            throw new RenderbaseException("Request failed", e);
        }
    }

    private <T> T execute(Request request, TypeReference<T> typeReference) throws RenderbaseException {
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                handleError(response.code(), responseBody);
            }

            if (responseBody.isEmpty()) {
                return null;
            }

            return objectMapper.readValue(responseBody, typeReference);
        } catch (IOException e) {
            throw new RenderbaseException("Request failed", e);
        }
    }

    private void executeVoid(Request request) throws RenderbaseException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                handleError(response.code(), responseBody);
            }
        } catch (IOException e) {
            throw new RenderbaseException("Request failed", e);
        }
    }

    private void handleError(int statusCode, String responseBody) throws RenderbaseException {
        try {
            ApiError error = objectMapper.readValue(responseBody, ApiError.class);
            throw new RenderbaseException(error.getMessage(), error.getCode(), statusCode);
        } catch (IOException e) {
            throw new RenderbaseException("HTTP " + statusCode + ": " + responseBody, null, statusCode);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
