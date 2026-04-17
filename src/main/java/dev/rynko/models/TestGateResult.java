package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Result of a gate test (dry-run, no run created).
 */
public class TestGateResult {

    @JsonProperty("valid")
    private boolean valid;

    @JsonProperty("errors")
    private List<FlowRun.FlowValidationError> errors;

    @JsonProperty("output")
    private Map<String, Object> output;

    public TestGateResult() {
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public List<FlowRun.FlowValidationError> getErrors() { return errors; }
    public void setErrors(List<FlowRun.FlowValidationError> errors) { this.errors = errors; }

    public Map<String, Object> getOutput() { return output; }
    public void setOutput(Map<String, Object> output) { this.output = output; }

    @Override
    public String toString() {
        return "TestGateResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}
