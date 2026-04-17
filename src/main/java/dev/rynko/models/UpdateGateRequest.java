package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Request for updating a Flow gate.
 */
public class UpdateGateRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("schema")
    private Object schema;

    @JsonProperty("rules")
    private List<Map<String, Object>> rules;

    private UpdateGateRequest() {
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Object getSchema() { return schema; }
    public List<Map<String, Object>> getRules() { return rules; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
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

        public Builder schema(Object schema) {
            this.schema = schema;
            return this;
        }

        public Builder rules(List<Map<String, Object>> rules) {
            this.rules = rules;
            return this;
        }

        public UpdateGateRequest build() {
            UpdateGateRequest request = new UpdateGateRequest();
            request.name = this.name;
            request.description = this.description;
            request.schema = this.schema;
            request.rules = this.rules;
            return request;
        }
    }
}
