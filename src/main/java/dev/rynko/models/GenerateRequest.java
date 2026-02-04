package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Request for generating a document.
 */
public class GenerateRequest {

    @JsonProperty("templateId")
    private String templateId;

    @JsonProperty("format")
    private String format;

    @JsonProperty("variables")
    private Map<String, Object> variables;

    @JsonProperty("workspaceId")
    private String workspaceId;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("source")
    private String source;

    private GenerateRequest() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getFormat() {
        return format;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getFilename() {
        return filename;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getSource() {
        return source;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String templateId;
        private String format = "pdf";
        private Map<String, Object> variables = new HashMap<>();
        private String workspaceId;
        private String filename;
        private Map<String, Object> metadata;

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public Builder variable(String key, Object value) {
            this.variables.put(key, value);
            return this;
        }

        /**
         * Sets the workspace ID to generate the document in.
         * If not provided, defaults to the user's current workspace.
         *
         * @param workspaceId The workspace ID
         * @return This builder
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public GenerateRequest build() {
            if (templateId == null || templateId.isEmpty()) {
                throw new IllegalArgumentException("templateId is required");
            }

            GenerateRequest request = new GenerateRequest();
            request.templateId = this.templateId;
            request.format = this.format;
            request.variables = this.variables;
            request.workspaceId = this.workspaceId;
            request.filename = this.filename;
            request.metadata = this.metadata;
            // Automatically set source to identify SDK usage
            request.source = "sdk_java";
            return request;
        }
    }
}
