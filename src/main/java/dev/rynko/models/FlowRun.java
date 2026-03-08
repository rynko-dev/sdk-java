package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Flow run representing a validation submission.
 */
public class FlowRun {

    private static final Set<String> TERMINAL_STATUSES = new HashSet<>(Arrays.asList(
            "completed", "delivered", "approved", "rejected",
            "validation_failed", "render_failed", "delivery_failed"
    ));

    @JsonProperty("id")
    private String id;

    @JsonProperty("gateId")
    private String gateId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("input")
    private Map<String, Object> input;

    @JsonProperty("output")
    private Map<String, Object> output;

    @JsonProperty("errors")
    private List<FlowValidationError> errors;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("completedAt")
    private String completedAt;

    public FlowRun() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGateId() { return gateId; }
    public void setGateId(String gateId) { this.gateId = gateId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> getInput() { return input; }
    public void setInput(Map<String, Object> input) { this.input = input; }

    public Map<String, Object> getOutput() { return output; }
    public void setOutput(Map<String, Object> output) { this.output = output; }

    public List<FlowValidationError> getErrors() { return errors; }
    public void setErrors(List<FlowValidationError> errors) { this.errors = errors; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    /**
     * Checks if the run is in a terminal state.
     */
    public boolean isTerminal() {
        return TERMINAL_STATUSES.contains(status);
    }

    @Override
    public String toString() {
        return "FlowRun{" +
                "id='" + id + '\'' +
                ", gateId='" + gateId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    /**
     * A validation error from a Flow run.
     */
    public static class FlowValidationError {
        @JsonProperty("field")
        private String field;

        @JsonProperty("rule")
        private String rule;

        @JsonProperty("message")
        private String message;

        public FlowValidationError() {
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getRule() { return rule; }
        public void setRule(String rule) { this.rule = rule; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        @Override
        public String toString() {
            return "FlowValidationError{field='" + field + "', message='" + message + "'}";
        }
    }
}
