package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Flow delivery representing a webhook delivery attempt.
 */
public class FlowDelivery {

    @JsonProperty("id")
    private String id;

    @JsonProperty("runId")
    private String runId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("url")
    private String url;

    @JsonProperty("httpStatus")
    private Integer httpStatus;

    @JsonProperty("attempts")
    private int attempts;

    @JsonProperty("lastAttemptAt")
    private String lastAttemptAt;

    @JsonProperty("error")
    private String error;

    @JsonProperty("createdAt")
    private String createdAt;

    public FlowDelivery() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public String getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(String lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "FlowDelivery{" +
                "id='" + id + '\'' +
                ", runId='" + runId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
