package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for creating a webhook subscription.
 */
public class CreateWebhookRequest {

    @JsonProperty("url")
    private String url;

    @JsonProperty("events")
    private List<String> events;

    @JsonProperty("description")
    private String description;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("maxRetries")
    private Integer maxRetries;

    @JsonProperty("timeoutMs")
    private Integer timeoutMs;

    @JsonProperty("workspaceId")
    private String workspaceId;

    private CreateWebhookRequest() {
    }

    public String getUrl() { return url; }
    public List<String> getEvents() { return events; }
    public String getDescription() { return description; }
    public Boolean getIsActive() { return isActive; }
    public Integer getMaxRetries() { return maxRetries; }
    public Integer getTimeoutMs() { return timeoutMs; }
    public String getWorkspaceId() { return workspaceId; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String url;
        private List<String> events = new ArrayList<>();
        private String description;
        private Boolean isActive;
        private Integer maxRetries;
        private Integer timeoutMs;
        private String workspaceId;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder events(List<String> events) {
            this.events = events;
            return this;
        }

        public Builder event(String event) {
            this.events.add(event);
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public CreateWebhookRequest build() {
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("url is required");
            }
            if (events == null || events.isEmpty()) {
                throw new IllegalArgumentException("At least one event is required");
            }

            CreateWebhookRequest request = new CreateWebhookRequest();
            request.url = this.url;
            request.events = this.events;
            request.description = this.description;
            request.isActive = this.isActive;
            request.maxRetries = this.maxRetries;
            request.timeoutMs = this.timeoutMs;
            request.workspaceId = this.workspaceId;
            return request;
        }
    }
}
