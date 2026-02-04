package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request for generating multiple documents in a single batch.
 *
 * <p>Use batch generation when you need to create multiple documents from the same
 * template with different variable sets. This is more efficient than making multiple
 * individual requests.</p>
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * GenerateBatchRequest request = GenerateBatchRequest.builder()
 *     .templateId("tmpl_invoice")
 *     .format("pdf")
 *     .addDocument(BatchDocumentSpec.builder()
 *         .variable("invoiceNumber", "INV-001")
 *         .variable("customerName", "Acme Corp")
 *         .filename("invoice-acme")
 *         .build())
 *     .addDocument(BatchDocumentSpec.builder()
 *         .variable("invoiceNumber", "INV-002")
 *         .variable("customerName", "Globex Inc")
 *         .filename("invoice-globex")
 *         .build())
 *     .webhookUrl("https://example.com/webhook")
 *     .build();
 *
 * GenerateBatchResult result = client.documents().generateBatch(request);
 * System.out.println("Batch ID: " + result.getBatchId());
 * System.out.println("Total jobs: " + result.getTotalJobs());
 * }</pre>
 *
 * @since 1.0.0
 */
public class GenerateBatchRequest {

    @JsonProperty("templateId")
    private String templateId;

    @JsonProperty("format")
    private String format;

    @JsonProperty("documents")
    private List<BatchDocumentSpec> documents;

    @JsonProperty("webhookUrl")
    private String webhookUrl;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("useDraft")
    private Boolean useDraft;

    @JsonProperty("useCredit")
    private Boolean useCredit;

    @JsonProperty("source")
    private String source;

    private GenerateBatchRequest() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getFormat() {
        return format;
    }

    public List<BatchDocumentSpec> getDocuments() {
        return documents;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Boolean getUseDraft() {
        return useDraft;
    }

    public Boolean getUseCredit() {
        return useCredit;
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
        private List<BatchDocumentSpec> documents = new ArrayList<>();
        private String webhookUrl;
        private Map<String, Object> metadata;
        private Boolean useDraft;
        private Boolean useCredit;

        /**
         * Sets the template ID for the batch.
         *
         * @param templateId The template ID (UUID, shortId, or slug)
         * @return This builder
         */
        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        /**
         * Sets the output format for all documents in the batch.
         *
         * @param format The format ("pdf" or "excel")
         * @return This builder
         */
        public Builder format(String format) {
            this.format = format;
            return this;
        }

        /**
         * Adds a document specification to the batch.
         *
         * @param document The document specification
         * @return This builder
         */
        public Builder addDocument(BatchDocumentSpec document) {
            this.documents.add(document);
            return this;
        }

        /**
         * Sets all documents for the batch.
         *
         * @param documents The list of document specifications
         * @return This builder
         */
        public Builder documents(List<BatchDocumentSpec> documents) {
            this.documents = documents != null ? documents : new ArrayList<>();
            return this;
        }

        /**
         * Sets the webhook URL to receive completion notifications.
         *
         * <p>The webhook will be called when each document completes,
         * with the document's metadata included in the payload.</p>
         *
         * @param webhookUrl The webhook URL (must be HTTPS)
         * @return This builder
         */
        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        /**
         * Sets batch-level metadata.
         *
         * <p>This metadata is attached to the batch as a whole.
         * For per-document metadata, use {@link BatchDocumentSpec#metadata}.</p>
         *
         * @param metadata The metadata map
         * @return This builder
         */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Whether to use the draft version of the template.
         *
         * @param useDraft True to use draft version, false for published version
         * @return This builder
         */
        public Builder useDraft(boolean useDraft) {
            this.useDraft = useDraft;
            return this;
        }

        /**
         * Whether to use premium credits for generation.
         *
         * <p>When true, documents are generated without watermarks using
         * purchased document credits.</p>
         *
         * @param useCredit True to use premium credits
         * @return This builder
         */
        public Builder useCredit(boolean useCredit) {
            this.useCredit = useCredit;
            return this;
        }

        public GenerateBatchRequest build() {
            if (templateId == null || templateId.isEmpty()) {
                throw new IllegalArgumentException("templateId is required");
            }
            if (documents.isEmpty()) {
                throw new IllegalArgumentException("At least one document is required");
            }

            GenerateBatchRequest request = new GenerateBatchRequest();
            request.templateId = this.templateId;
            request.format = this.format;
            request.documents = this.documents;
            request.webhookUrl = this.webhookUrl;
            request.metadata = this.metadata;
            request.useDraft = this.useDraft;
            request.useCredit = this.useCredit;
            // Automatically set source to identify SDK usage
            request.source = "sdk_java";
            return request;
        }
    }

    @Override
    public String toString() {
        return "GenerateBatchRequest{" +
                "templateId='" + templateId + '\'' +
                ", format='" + format + '\'' +
                ", documents=" + documents.size() + " items" +
                ", webhookUrl='" + webhookUrl + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
