package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Specification for a single document in a batch request.
 *
 * <p>Each document spec contains the variables to use for that document,
 * along with optional filename and metadata for tracking.</p>
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * BatchDocumentSpec spec = BatchDocumentSpec.builder()
 *     .variable("invoiceNumber", "INV-001")
 *     .variable("customerName", "Acme Corp")
 *     .filename("invoice-acme.pdf")
 *     .metadata("orderId", "order_123")
 *     .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public class BatchDocumentSpec {

    @JsonProperty("variables")
    private Map<String, Object> variables;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    private BatchDocumentSpec() {
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getFilename() {
        return filename;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, Object> variables = new HashMap<>();
        private String filename;
        private Map<String, Object> metadata = new HashMap<>();

        /**
         * Sets all variables for this document.
         *
         * @param variables The variables map
         * @return This builder
         */
        public Builder variables(Map<String, Object> variables) {
            this.variables = variables != null ? variables : new HashMap<>();
            return this;
        }

        /**
         * Adds a single variable for this document.
         *
         * @param key   The variable name
         * @param value The variable value
         * @return This builder
         */
        public Builder variable(String key, Object value) {
            this.variables.put(key, value);
            return this;
        }

        /**
         * Sets the filename for this document.
         *
         * @param filename The filename (without extension)
         * @return This builder
         */
        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        /**
         * Sets all metadata for this document.
         *
         * <p>Metadata is returned in webhook payloads and job status responses,
         * useful for correlating generated documents with your system.</p>
         *
         * @param metadata The metadata map
         * @return This builder
         */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : new HashMap<>();
            return this;
        }

        /**
         * Adds a single metadata key-value pair for this document.
         *
         * @param key   The metadata key
         * @param value The metadata value
         * @return This builder
         */
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public BatchDocumentSpec build() {
            BatchDocumentSpec spec = new BatchDocumentSpec();
            spec.variables = this.variables;
            spec.filename = this.filename;
            spec.metadata = this.metadata.isEmpty() ? null : this.metadata;
            return spec;
        }
    }

    @Override
    public String toString() {
        return "BatchDocumentSpec{" +
                "variables=" + variables +
                ", filename='" + filename + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
