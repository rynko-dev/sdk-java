package dev.rynko.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.rynko.exceptions.RynkoException;
import dev.rynko.exceptions.WebhookSignatureException;
import dev.rynko.models.ListResponse;
import dev.rynko.models.PaginationMeta;
import dev.rynko.utils.HttpClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource for webhook operations and signature verification.
 *
 * <p>Use this resource to list webhook subscriptions and verify incoming webhook signatures.
 * Webhook subscriptions are managed through the Rynko dashboard.</p>
 *
 * <h2>Signature Verification Example:</h2>
 * <pre>{@code
 * String payload = request.getBody();
 * String signature = request.getHeader("X-Rynko-Signature");
 * String timestamp = request.getHeader("X-Rynko-Timestamp");
 *
 * try {
 *     client.webhooks().verifySignature(payload, signature, timestamp, webhookSecret);
 *     // Signature is valid, process the webhook
 * } catch (WebhookSignatureException e) {
 *     // Invalid signature
 *     response.setStatus(401);
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class WebhooksResource {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300; // 5 minutes

    private final HttpClient httpClient;

    public WebhooksResource(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Lists webhook subscriptions.
     *
     * @return Paginated list of webhook subscriptions
     * @throws RynkoException if the request fails
     */
    public ListResponse<WebhookSubscription> list() throws RynkoException {
        return list(null, null);
    }

    /**
     * Lists webhook subscriptions with pagination.
     *
     * @param page  Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated list of webhook subscriptions
     * @throws RynkoException if the request fails
     */
    public ListResponse<WebhookSubscription> list(Integer page, Integer limit) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(effectivePage));
        params.put("limit", String.valueOf(effectiveLimit));

        // Backend returns { data: [], total: number }
        WebhooksListResponse response = httpClient.get("/webhook-subscriptions", params, new TypeReference<WebhooksListResponse>() {});

        // Convert to ListResponse format
        ListResponse<WebhookSubscription> result = new ListResponse<>();
        result.setData(response.getData());

        PaginationMeta meta = new PaginationMeta();
        meta.setTotal(response.getTotal());
        meta.setPage(effectivePage);
        meta.setLimit(effectiveLimit);
        meta.setTotalPages(effectiveLimit > 0 ? (response.getTotal() + effectiveLimit - 1) / effectiveLimit : 1);
        result.setMeta(meta);

        return result;
    }

    /**
     * Internal class to parse backend response format.
     */
    private static class WebhooksListResponse {
        @JsonProperty("data")
        private List<WebhookSubscription> data;

        @JsonProperty("total")
        private int total;

        public List<WebhookSubscription> getData() { return data; }
        public void setData(List<WebhookSubscription> data) { this.data = data; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }

    /**
     * Gets a webhook subscription by ID.
     *
     * @param webhookId The webhook subscription ID
     * @return The webhook subscription
     * @throws RynkoException if the request fails
     */
    public WebhookSubscription get(String webhookId) throws RynkoException {
        return httpClient.get("/webhook-subscriptions/" + webhookId, WebhookSubscription.class);
    }

    /**
     * Verifies a webhook signature.
     *
     * <p>This method validates that a webhook payload was sent by Rynko
     * and has not been tampered with.</p>
     *
     * @param payload   The raw request body
     * @param signature The X-Rynko-Signature header value
     * @param timestamp The X-Rynko-Timestamp header value
     * @param secret    Your webhook signing secret
     * @throws WebhookSignatureException if the signature is invalid or expired
     */
    public void verifySignature(String payload, String signature, String timestamp, String secret)
            throws WebhookSignatureException {
        if (payload == null || signature == null || timestamp == null || secret == null) {
            throw new WebhookSignatureException("Missing required parameters for signature verification");
        }

        // Verify timestamp is within tolerance
        long timestampSeconds;
        try {
            timestampSeconds = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new WebhookSignatureException("Invalid timestamp format");
        }

        long currentSeconds = System.currentTimeMillis() / 1000;
        if (Math.abs(currentSeconds - timestampSeconds) > TIMESTAMP_TOLERANCE_SECONDS) {
            throw new WebhookSignatureException("Timestamp is outside the tolerance window");
        }

        // Compute expected signature
        String signedPayload = timestamp + "." + payload;
        String expectedSignature = computeHmacSha256(signedPayload, secret);

        // Parse signature header (format: "v1=signature")
        String actualSignature = signature;
        if (signature.startsWith("v1=")) {
            actualSignature = signature.substring(3);
        }

        // Constant-time comparison to prevent timing attacks
        if (!constantTimeEquals(expectedSignature, actualSignature)) {
            throw new WebhookSignatureException("Signature verification failed");
        }
    }

    /**
     * Constructs a webhook event from the payload.
     *
     * <p>Use this after verifying the signature to parse the webhook event.</p>
     *
     * @param payload The raw request body
     * @return The parsed webhook event
     * @throws RynkoException if parsing fails
     */
    public WebhookEvent constructEvent(String payload) throws RynkoException {
        try {
            return httpClient.getObjectMapper().readValue(payload, WebhookEvent.class);
        } catch (Exception e) {
            throw new RynkoException("Failed to parse webhook event", e);
        }
    }

    /**
     * Verifies signature and constructs event in one call.
     *
     * @param payload   The raw request body
     * @param signature The X-Rynko-Signature header value
     * @param timestamp The X-Rynko-Timestamp header value
     * @param secret    Your webhook signing secret
     * @return The parsed webhook event
     * @throws WebhookSignatureException if the signature is invalid
     * @throws RynkoException       if parsing fails
     */
    public WebhookEvent constructEvent(String payload, String signature, String timestamp, String secret)
            throws WebhookSignatureException, RynkoException {
        verifySignature(payload, signature, timestamp, secret);
        return constructEvent(payload);
    }

    private String computeHmacSha256(String data, String key) throws WebhookSignatureException {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new WebhookSignatureException("Failed to compute HMAC", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        if (aBytes.length != bBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }

    // Inner classes for webhook-related models

    /**
     * Webhook subscription.
     */
    public static class WebhookSubscription {
        private String id;
        private String url;
        private String[] events;
        private String description;
        private boolean isActive;
        private String secret;
        private String createdAt;
        private String updatedAt;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String[] getEvents() { return events; }
        public void setEvents(String[] events) { this.events = events; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean isActive) { this.isActive = isActive; }
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * Webhook event payload.
     */
    public static class WebhookEvent {
        private String id;
        private String type;
        private String createdAt;
        private Object data;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }

        /**
         * Gets the event data cast to a specific type.
         *
         * @param <T>  The expected type
         * @param type The class to cast to
         * @return The data cast to the specified type
         */
        @SuppressWarnings("unchecked")
        public <T> T getDataAs(Class<T> type) {
            return (T) data;
        }
    }
}
