package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Request for submitting a Flow run.
 */
public class SubmitRunRequest {

    @JsonProperty("input")
    private Map<String, Object> input;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("webhookUrl")
    private String webhookUrl;

    private SubmitRunRequest() {
    }

    public Map<String, Object> getInput() { return input; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getWebhookUrl() { return webhookUrl; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, Object> input = new HashMap<>();
        private Map<String, Object> metadata;
        private String webhookUrl;

        public Builder input(Map<String, Object> input) {
            this.input = input;
            return this;
        }

        public Builder inputField(String key, Object value) {
            this.input.put(key, value);
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

        public SubmitRunRequest build() {
            if (input == null || input.isEmpty()) {
                throw new IllegalArgumentException("input is required");
            }

            SubmitRunRequest request = new SubmitRunRequest();
            request.input = this.input;
            request.metadata = this.metadata;
            request.webhookUrl = this.webhookUrl;
            return request;
        }
    }
}
