package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * An extraction job result.
 */
public class ExtractJob {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("result")
    private Map<String, Object> result;

    @JsonProperty("schema")
    private Object schema;

    @JsonProperty("files")
    private List<Map<String, Object>> files;

    @JsonProperty("instructions")
    private String instructions;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("completedAt")
    private String completedAt;

    public ExtractJob() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> getResult() { return result; }
    public void setResult(Map<String, Object> result) { this.result = result; }

    public Object getSchema() { return schema; }
    public void setSchema(Object schema) { this.schema = schema; }

    public List<Map<String, Object>> getFiles() { return files; }
    public void setFiles(List<Map<String, Object>> files) { this.files = files; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    /**
     * Checks if the job is in a terminal state.
     */
    public boolean isTerminal() {
        return "completed".equals(status) || "failed".equals(status);
    }

    @Override
    public String toString() {
        return "ExtractJob{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
