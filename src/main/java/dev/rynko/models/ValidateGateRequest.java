package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Request for validating data against a Flow gate (creates a run + validation_id).
 */
public class ValidateGateRequest {

    @JsonProperty("payload")
    private Map<String, Object> payload;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("webhookUrl")
    private String webhookUrl;

    private ValidateGateRequest() {
    }

    public Map<String, Object> getPayload() { return payload; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getWebhookUrl() { return webhookUrl; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, Object> payload = new HashMap<>();
        private Map<String, Object> metadata;
        private String webhookUrl;

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public Builder payloadField(String key, Object value) {
            this.payload.put(key, value);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        public ValidateGateRequest build() {
            if (payload == null || payload.isEmpty()) {
                throw new IllegalArgumentException("payload is required");
            }

            ValidateGateRequest request = new ValidateGateRequest();
            request.payload = this.payload;
            request.metadata = this.metadata;
            request.webhookUrl = this.webhookUrl;
            return request;
        }
    }
}
