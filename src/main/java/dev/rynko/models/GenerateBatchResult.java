package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of a batch document generation request.
 *
 * <p>When a batch is submitted, it is queued for processing and this result
 * is returned immediately. Use {@link #getBatchId()} to check status via
 * {@link dev.rynko.resources.DocumentsResource#getBatch(String)}.</p>
 *
 * @since 1.0.0
 */
public class GenerateBatchResult {

    @JsonProperty("batchId")
    private String batchId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("totalJobs")
    private int totalJobs;

    @JsonProperty("statusUrl")
    private String statusUrl;

    @JsonProperty("estimatedWaitSeconds")
    private Integer estimatedWaitSeconds;

    public GenerateBatchResult() {
    }

    /**
     * Gets the batch ID.
     *
     * <p>Use this ID to check batch status via
     * {@link dev.rynko.resources.DocumentsResource#getBatch(String)}.</p>
     *
     * @return The batch ID (format: batch_xxxxxxxx)
     */
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    /**
     * Gets the batch status.
     *
     * @return The status ("queued" when first created)
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the total number of jobs in the batch.
     *
     * @return The total job count
     */
    public int getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(int totalJobs) {
        this.totalJobs = totalJobs;
    }

    /**
     * Gets the URL to check batch status.
     *
     * @return The status URL path
     */
    public String getStatusUrl() {
        return statusUrl;
    }

    public void setStatusUrl(String statusUrl) {
        this.statusUrl = statusUrl;
    }

    /**
     * Gets the estimated wait time in seconds.
     *
     * @return The estimated wait time, or null if not available
     */
    public Integer getEstimatedWaitSeconds() {
        return estimatedWaitSeconds;
    }

    public void setEstimatedWaitSeconds(Integer estimatedWaitSeconds) {
        this.estimatedWaitSeconds = estimatedWaitSeconds;
    }

    @Override
    public String toString() {
        return "GenerateBatchResult{" +
                "batchId='" + batchId + '\'' +
                ", status='" + status + '\'' +
                ", totalJobs=" + totalJobs +
                ", statusUrl='" + statusUrl + '\'' +
                ", estimatedWaitSeconds=" + estimatedWaitSeconds +
                '}';
    }
}
