package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request for creating an extraction job.
 */
public class ExtractJobRequest {

    private List<File> files;

    @JsonProperty("schema")
    private Object schema;

    @JsonProperty("schemaId")
    private String schemaId;

    @JsonProperty("gateId")
    private String gateId;

    @JsonProperty("instructions")
    private String instructions;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    private ExtractJobRequest() {
    }

    public List<File> getFiles() { return files; }
    public Object getSchema() { return schema; }
    public String getSchemaId() { return schemaId; }
    public String getGateId() { return gateId; }
    public String getInstructions() { return instructions; }
    public Map<String, Object> getMetadata() { return metadata; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<File> files = new ArrayList<>();
        private Object schema;
        private String schemaId;
        private String gateId;
        private String instructions;
        private Map<String, Object> metadata;

        public Builder file(File file) {
            this.files.add(file);
            return this;
        }

        public Builder files(List<File> files) {
            this.files = files;
            return this;
        }

        public Builder schema(Object schema) {
            this.schema = schema;
            return this;
        }

        public Builder schemaId(String schemaId) {
            this.schemaId = schemaId;
            return this;
        }

        public Builder gateId(String gateId) {
            this.gateId = gateId;
            return this;
        }

        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder metadataField(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }

        public ExtractJobRequest build() {
            if (files == null || files.isEmpty()) {
                throw new IllegalArgumentException("At least one file is required");
            }

            ExtractJobRequest request = new ExtractJobRequest();
            request.files = this.files;
            request.schema = this.schema;
            request.schemaId = this.schemaId;
            request.gateId = this.gateId;
            request.instructions = this.instructions;
            request.metadata = this.metadata;
            return request;
        }
    }
}
