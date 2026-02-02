package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Result of a document generation request.
 */
public class GenerateResult {

    @JsonProperty("jobId")
    private String jobId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("statusUrl")
    private String statusUrl;

    @JsonProperty("estimatedWaitSeconds")
    private Integer estimatedWaitSeconds;

    @JsonProperty("downloadUrl")
    private String downloadUrl;

    @JsonProperty("expiresAt")
    private Instant expiresAt;

    @JsonProperty("format")
    private String format;

    @JsonProperty("templateId")
    private String templateId;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("errorCode")
    private String errorCode;

    public GenerateResult() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusUrl() {
        return statusUrl;
    }

    public void setStatusUrl(String statusUrl) {
        this.statusUrl = statusUrl;
    }

    public Integer getEstimatedWaitSeconds() {
        return estimatedWaitSeconds;
    }

    public void setEstimatedWaitSeconds(Integer estimatedWaitSeconds) {
        this.estimatedWaitSeconds = estimatedWaitSeconds;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    /**
     * Gets the custom metadata attached to this document.
     *
     * <p>Metadata is a flat key-value object passed during document generation.
     * It is returned in job status responses and webhook payloads.</p>
     *
     * @return The metadata map, or null if no metadata was provided
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets the error message if the job failed.
     *
     * @return The error message, or null if the job did not fail
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the error code if the job failed.
     *
     * @return The error code, or null if the job did not fail
     */
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Checks if the job is in a terminal state (completed or failed).
     *
     * @return true if the job is completed or failed
     */
    public boolean isTerminal() {
        return "completed".equals(status) || "failed".equals(status);
    }

    /**
     * Checks if the job completed successfully.
     *
     * @return true if the job completed successfully
     */
    public boolean isCompleted() {
        return "completed".equals(status);
    }

    /**
     * Checks if the job failed.
     *
     * @return true if the job failed
     */
    public boolean isFailed() {
        return "failed".equals(status);
    }

    @Override
    public String toString() {
        return "GenerateResult{" +
                "jobId='" + jobId + '\'' +
                ", status='" + status + '\'' +
                ", statusUrl='" + statusUrl + '\'' +
                ", estimatedWaitSeconds=" + estimatedWaitSeconds +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", format='" + format + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
