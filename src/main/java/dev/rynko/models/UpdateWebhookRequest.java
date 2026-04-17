package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request for updating a webhook subscription.
 */
public class UpdateWebhookRequest {

    @JsonProperty("description")
    private String description;

    @JsonProperty("events")
    private List<String> events;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("maxRetries")
    private Integer maxRetries;

    @JsonProperty("timeoutMs")
    private Integer timeoutMs;

    private UpdateWebhookRequest() {
    }

    public String getDescription() { return description; }
    public List<String> getEvents() { return events; }
    public Boolean getIsActive() { return isActive; }
    public Integer getMaxRetries() { return maxRetries; }
    public Integer getTimeoutMs() { return timeoutMs; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String description;
        private List<String> events;
        private Boolean isActive;
        private Integer maxRetries;
        private Integer timeoutMs;

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder events(List<String> events) {
            this.events = events;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder timeoutMs(Integer timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public UpdateWebhookRequest build() {
            UpdateWebhookRequest request = new UpdateWebhookRequest();
            request.description = this.description;
            request.events = this.events;
            request.isActive = this.isActive;
            request.maxRetries = this.maxRetries;
            request.timeoutMs = this.timeoutMs;
            return request;
        }
    }
}
