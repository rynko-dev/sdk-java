package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Flow approval for human review.
 */
public class FlowApproval {

    @JsonProperty("id")
    private String id;

    @JsonProperty("runId")
    private String runId;

    @JsonProperty("gateId")
    private String gateId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("reviewerEmail")
    private String reviewerEmail;

    @JsonProperty("reviewerNote")
    private String reviewerNote;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("resolvedAt")
    private String resolvedAt;

    public FlowApproval() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public String getGateId() { return gateId; }
    public void setGateId(String gateId) { this.gateId = gateId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewerEmail() { return reviewerEmail; }
    public void setReviewerEmail(String reviewerEmail) { this.reviewerEmail = reviewerEmail; }

    public String getReviewerNote() { return reviewerNote; }
    public void setReviewerNote(String reviewerNote) { this.reviewerNote = reviewerNote; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(String resolvedAt) { this.resolvedAt = resolvedAt; }

    @Override
    public String toString() {
        return "FlowApproval{" +
                "id='" + id + '\'' +
                ", runId='" + runId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
