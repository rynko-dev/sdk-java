package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Status result for a document batch.
 *
 * <p>Contains the current state of a batch including progress information
 * and metadata.</p>
 *
 * @since 1.0.0
 */
public class BatchStatusResult {

    @JsonProperty("batchId")
    private String batchId;

    @JsonProperty("templateId")
    private String templateId;

    @JsonProperty("templateName")
    private String templateName;

    @JsonProperty("templateShortId")
    private String templateShortId;

    @JsonProperty("format")
    private String format;

    @JsonProperty("status")
    private String status;

    @JsonProperty("totalJobs")
    private int totalJobs;

    @JsonProperty("completedJobs")
    private int completedJobs;

    @JsonProperty("failedJobs")
    private int failedJobs;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("processingAt")
    private Instant processingAt;

    @JsonProperty("completedAt")
    private Instant completedAt;

    public BatchStatusResult() {
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateShortId() {
        return templateShortId;
    }

    public void setTemplateShortId(String templateShortId) {
        this.templateShortId = templateShortId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(int totalJobs) {
        this.totalJobs = totalJobs;
    }

    public int getCompletedJobs() {
        return completedJobs;
    }

    public void setCompletedJobs(int completedJobs) {
        this.completedJobs = completedJobs;
    }

    public int getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(int failedJobs) {
        this.failedJobs = failedJobs;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getProcessingAt() {
        return processingAt;
    }

    public void setProcessingAt(Instant processingAt) {
        this.processingAt = processingAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Checks if the batch is in a terminal state (completed, partial, or failed).
     *
     * @return true if the batch is finished
     */
    public boolean isTerminal() {
        return "completed".equals(status) || "partial".equals(status) || "failed".equals(status);
    }

    /**
     * Checks if the batch completed successfully (all jobs succeeded).
     *
     * @return true if status is "completed"
     */
    public boolean isCompleted() {
        return "completed".equals(status);
    }

    /**
     * Checks if the batch partially completed (some jobs failed).
     *
     * @return true if status is "partial"
     */
    public boolean isPartial() {
        return "partial".equals(status);
    }

    /**
     * Checks if the batch failed (all jobs failed).
     *
     * @return true if status is "failed"
     */
    public boolean isFailed() {
        return "failed".equals(status);
    }

    /**
     * Gets the progress percentage (0-100).
     *
     * @return The progress percentage
     */
    public int getProgressPercent() {
        if (totalJobs == 0) return 0;
        return Math.round((float) (completedJobs + failedJobs) / totalJobs * 100);
    }

    @Override
    public String toString() {
        return "BatchStatusResult{" +
                "batchId='" + batchId + '\'' +
                ", templateId='" + templateId + '\'' +
                ", format='" + format + '\'' +
                ", status='" + status + '\'' +
                ", progress=" + completedJobs + "/" + totalJobs +
                (failedJobs > 0 ? " (" + failedJobs + " failed)" : "") +
                '}';
    }
}
