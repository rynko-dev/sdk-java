package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Request for creating a Flow gate.
 */
public class CreateGateRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("workspaceId")
    private String workspaceId;

    @JsonProperty("schema")
    private Object schema;

    @JsonProperty("rules")
    private List<Map<String, Object>> rules;

    private CreateGateRequest() {
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getWorkspaceId() { return workspaceId; }
    public Object getSchema() { return schema; }
    public List<Map<String, Object>> getRules() { return rules; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private String workspaceId;
        private Object schema;
        private List<Map<String, Object>> rules;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public Builder schema(Object schema) {
            this.schema = schema;
            return this;
        }

        public Builder rules(List<Map<String, Object>> rules) {
            this.rules = rules;
            return this;
        }

        public CreateGateRequest build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name is required");
            }

            CreateGateRequest request = new CreateGateRequest();
            request.name = this.name;
            request.description = this.description;
            request.workspaceId = this.workspaceId;
            request.schema = this.schema;
            request.rules = this.rules;
            return request;
        }
    }
}
