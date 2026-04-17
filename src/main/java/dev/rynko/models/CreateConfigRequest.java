package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for creating an extraction configuration.
 */
public class CreateConfigRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("schema")
    private Object schema;

    @JsonProperty("description")
    private String description;

    @JsonProperty("instructions")
    private String instructions;

    private CreateConfigRequest() {
    }

    public String getName() { return name; }
    public Object getSchema() { return schema; }
    public String getDescription() { return description; }
    public String getInstructions() { return instructions; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Object schema;
        private String description;
        private String instructions;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder schema(Object schema) {
            this.schema = schema;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public CreateConfigRequest build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name is required");
            }

            CreateConfigRequest request = new CreateConfigRequest();
            request.name = this.name;
            request.schema = this.schema;
            request.description = this.description;
            request.instructions = this.instructions;
            return request;
        }
    }
}
