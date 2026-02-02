package dev.rynko.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.rynko.exceptions.RynkoException;
import dev.rynko.models.GenerateRequest;
import dev.rynko.models.GenerateResult;
import dev.rynko.models.ListResponse;
import dev.rynko.models.PaginationMeta;
import dev.rynko.utils.HttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource for document generation operations.
 *
 * <p>Use this resource to generate PDF and Excel documents from templates.
 * Document generation is asynchronous - jobs are queued and processed in the background.
 * Use {@link #waitForCompletion(String)} to poll until the job completes.</p>
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * // Queue document generation with metadata for tracking
 * Map<String, Object> metadata = new HashMap<>();
 * metadata.put("orderId", "ord_12345");
 * metadata.put("customerId", "cust_67890");
 *
 * GenerateResult job = client.documents().generate(
 *     GenerateRequest.builder()
 *         .templateId("tmpl_invoice")
 *         .format("pdf")
 *         .variable("invoiceNumber", "INV-001")
 *         .variable("customerName", "Acme Corp")
 *         .metadata(metadata)  // Attach metadata for tracking
 *         .build()
 * );
 *
 * System.out.println("Job ID: " + job.getJobId());
 * System.out.println("Status: " + job.getStatus());  // "queued"
 *
 * // Wait for completion to get download URL
 * GenerateResult completed = client.documents().waitForCompletion(job.getJobId());
 * System.out.println("Download URL: " + completed.getDownloadUrl());
 *
 * // Metadata is returned in the completed job
 * System.out.println("Metadata: " + completed.getMetadata());
 * // Output: {orderId=ord_12345, customerId=cust_67890}
 * }</pre>
 *
 * @since 1.0.0
 */
public class DocumentsResource {

    private final HttpClient httpClient;

    public DocumentsResource(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Generates a document from a template.
     *
     * @param request The generation request containing template ID, format, variables, and optional workspaceId
     * @return The generation result with download URL
     * @throws RynkoException if the request fails
     */
    public GenerateResult generate(GenerateRequest request) throws RynkoException {
        return httpClient.post("/documents/generate", request, GenerateResult.class);
    }

    /**
     * Gets a document generation job by ID.
     *
     * @param jobId The job ID
     * @return The generation result
     * @throws RynkoException if the request fails
     */
    public GenerateResult get(String jobId) throws RynkoException {
        return httpClient.get("/documents/jobs/" + jobId, GenerateResult.class);
    }

    /**
     * Lists document generation jobs.
     *
     * @return Paginated list of generation results
     * @throws RynkoException if the request fails
     */
    public ListResponse<GenerateResult> list() throws RynkoException {
        return list(null, null, null, null, null);
    }

    /**
     * Lists document generation jobs with pagination.
     *
     * @param page  Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated list of generation results
     * @throws RynkoException if the request fails
     */
    public ListResponse<GenerateResult> list(Integer page, Integer limit) throws RynkoException {
        return list(page, limit, null, null, null);
    }

    /**
     * Lists document generation jobs with pagination and filtering.
     *
     * @param page        Page number (1-based)
     * @param limit       Number of items per page
     * @param templateId  Filter by template ID
     * @param workspaceId Filter by workspace ID
     * @return Paginated list of generation results
     * @throws RynkoException if the request fails
     */
    public ListResponse<GenerateResult> list(Integer page, Integer limit, String templateId, String workspaceId) throws RynkoException {
        return list(page, limit, templateId, workspaceId, null);
    }

    /**
     * Lists document generation jobs with pagination and filtering.
     *
     * @param page        Page number (1-based)
     * @param limit       Number of items per page
     * @param templateId  Filter by template ID
     * @param workspaceId Filter by workspace ID
     * @param status      Filter by status (queued, processing, completed, failed)
     * @return Paginated list of generation results
     * @throws RynkoException if the request fails
     */
    public ListResponse<GenerateResult> list(Integer page, Integer limit, String templateId, String workspaceId, String status) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;
        int offset = (effectivePage - 1) * effectiveLimit;

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(effectiveLimit));
        params.put("offset", String.valueOf(offset));
        if (templateId != null) {
            params.put("templateId", templateId);
        }
        if (workspaceId != null) {
            params.put("workspaceId", workspaceId);
        }
        if (status != null) {
            params.put("status", status);
        }

        // Backend returns { jobs: [], total: number }
        JobsListResponse response = httpClient.get("/documents/jobs", params, new TypeReference<JobsListResponse>() {});

        // Convert to ListResponse format
        ListResponse<GenerateResult> result = new ListResponse<>();
        result.setData(response.getJobs());

        PaginationMeta meta = new PaginationMeta();
        meta.setTotal(response.getTotal());
        meta.setPage(effectivePage);
        meta.setLimit(effectiveLimit);
        meta.setTotalPages(effectiveLimit > 0 ? (response.getTotal() + effectiveLimit - 1) / effectiveLimit : 1);
        result.setMeta(meta);

        return result;
    }

    /**
     * Internal class to parse backend response format.
     */
    private static class JobsListResponse {
        @JsonProperty("jobs")
        private List<GenerateResult> jobs;

        @JsonProperty("total")
        private int total;

        public List<GenerateResult> getJobs() { return jobs; }
        public void setJobs(List<GenerateResult> jobs) { this.jobs = jobs; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }

    /**
     * Deletes a generated document.
     *
     * @param jobId The job ID of the document to delete
     * @throws RynkoException if the request fails
     */
    public void delete(String jobId) throws RynkoException {
        httpClient.delete("/documents/jobs/" + jobId);
    }

    /**
     * Waits for a document generation job to complete.
     *
     * <p>Polls the job status at regular intervals until the job completes or fails,
     * or until the timeout is exceeded.</p>
     *
     * @param jobId The job ID to wait for
     * @return The completed generation result with download URL
     * @throws RynkoException if the request fails or the job fails
     * @throws RuntimeException if the timeout is exceeded
     */
    public GenerateResult waitForCompletion(String jobId) throws RynkoException {
        return waitForCompletion(jobId, 1000, 30000);
    }

    /**
     * Waits for a document generation job to complete with custom polling settings.
     *
     * @param jobId The job ID to wait for
     * @param pollIntervalMs Time between polls in milliseconds (default: 1000)
     * @param timeoutMs Maximum wait time in milliseconds (default: 30000)
     * @return The completed generation result with download URL
     * @throws RynkoException if the request fails or the job fails
     * @throws RuntimeException if the timeout is exceeded
     */
    public GenerateResult waitForCompletion(String jobId, long pollIntervalMs, long timeoutMs) throws RynkoException {
        long startTime = System.currentTimeMillis();

        while (true) {
            GenerateResult job = get(jobId);

            if (job.isTerminal()) {
                return job;
            }

            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new RuntimeException("Timeout waiting for job " + jobId + " to complete");
            }

            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for job " + jobId, e);
            }
        }
    }

    /**
     * Downloads a generated document as bytes.
     *
     * @param downloadUrl The download URL from the generation result
     * @return The document bytes
     * @throws RynkoException if the download fails
     */
    public byte[] download(String downloadUrl) throws RynkoException {
        try {
            java.net.URL url = new java.net.URL(downloadUrl);
            java.io.InputStream inputStream = url.openStream();
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            return outputStream.toByteArray();
        } catch (java.io.IOException e) {
            throw new RynkoException("Failed to download document", e);
        }
    }
}
