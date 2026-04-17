package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A webhook delivery attempt.
 */
public class WebhookDelivery {

    @JsonProperty("id")
    private String id;

    @JsonProperty("webhookId")
    private String webhookId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("httpStatus")
    private Integer httpStatus;

    @JsonProperty("attempts")
    private int attempts;

    @JsonProperty("error")
    private String error;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    public WebhookDelivery() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWebhookId() { return webhookId; }
    public void setWebhookId(String webhookId) { this.webhookId = webhookId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "WebhookDelivery{" +
                "id='" + id + '\'' +
                ", webhookId='" + webhookId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
