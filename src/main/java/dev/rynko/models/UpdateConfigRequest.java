package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for updating an extraction configuration.
 */
public class UpdateConfigRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("schema")
    private Object schema;

    @JsonProperty("instructions")
    private String instructions;

    private UpdateConfigRequest() {
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Object getSchema() { return schema; }
    public String getInstructions() { return instructions; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private Object schema;
        private String instructions;

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

        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public UpdateConfigRequest build() {
            UpdateConfigRequest request = new UpdateConfigRequest();
            request.name = this.name;
            request.description = this.description;
            request.schema = this.schema;
            request.instructions = this.instructions;
            return request;
        }
    }
}
