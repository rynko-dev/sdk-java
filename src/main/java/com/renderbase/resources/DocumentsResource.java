package com.renderbase.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.renderbase.exceptions.RenderbaseException;
import com.renderbase.models.GenerateRequest;
import com.renderbase.models.GenerateResult;
import com.renderbase.models.ListResponse;
import com.renderbase.utils.HttpClient;

import java.util.HashMap;
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
 * // Queue document generation (async operation)
 * GenerateResult job = client.documents().generate(
 *     GenerateRequest.builder()
 *         .templateId("tmpl_invoice")
 *         .format("pdf")
 *         .variable("invoiceNumber", "INV-001")
 *         .variable("customerName", "Acme Corp")
 *         .build()
 * );
 *
 * System.out.println("Job ID: " + job.getJobId());
 * System.out.println("Status: " + job.getStatus());  // "queued"
 *
 * // Wait for completion to get download URL
 * GenerateResult completed = client.documents().waitForCompletion(job.getJobId());
 * System.out.println("Download URL: " + completed.getDownloadUrl());
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
     * @throws RenderbaseException if the request fails
     */
    public GenerateResult generate(GenerateRequest request) throws RenderbaseException {
        return httpClient.post("/documents/generate", request, GenerateResult.class);
    }

    /**
     * Gets a document generation job by ID.
     *
     * @param jobId The job ID
     * @return The generation result
     * @throws RenderbaseException if the request fails
     */
    public GenerateResult get(String jobId) throws RenderbaseException {
        return httpClient.get("/documents/jobs/" + jobId, GenerateResult.class);
    }

    /**
     * Lists document generation jobs.
     *
     * @return Paginated list of generation results
     * @throws RenderbaseException if the request fails
     */
    public ListResponse<GenerateResult> list() throws RenderbaseException {
        return list(null, null, null, null);
    }

    /**
     * Lists document generation jobs with pagination.
     *
     * @param page  Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated list of generation results
     * @throws RenderbaseException if the request fails
     */
    public ListResponse<GenerateResult> list(Integer page, Integer limit) throws RenderbaseException {
        return list(page, limit, null, null);
    }

    /**
     * Lists document generation jobs with pagination and filtering.
     *
     * @param page        Page number (1-based)
     * @param limit       Number of items per page
     * @param templateId  Filter by template ID
     * @param workspaceId Filter by workspace ID
     * @return Paginated list of generation results
     * @throws RenderbaseException if the request fails
     */
    public ListResponse<GenerateResult> list(Integer page, Integer limit, String templateId, String workspaceId) throws RenderbaseException {
        Map<String, String> params = new HashMap<>();
        if (page != null) {
            params.put("page", page.toString());
        }
        if (limit != null) {
            params.put("limit", limit.toString());
        }
        if (templateId != null) {
            params.put("templateId", templateId);
        }
        if (workspaceId != null) {
            params.put("workspaceId", workspaceId);
        }

        return httpClient.get("/documents/jobs", params, new TypeReference<ListResponse<GenerateResult>>() {});
    }

    /**
     * Deletes a generated document.
     *
     * @param jobId The job ID of the document to delete
     * @throws RenderbaseException if the request fails
     */
    public void delete(String jobId) throws RenderbaseException {
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
     * @throws RenderbaseException if the request fails or the job fails
     * @throws RuntimeException if the timeout is exceeded
     */
    public GenerateResult waitForCompletion(String jobId) throws RenderbaseException {
        return waitForCompletion(jobId, 1000, 30000);
    }

    /**
     * Waits for a document generation job to complete with custom polling settings.
     *
     * @param jobId The job ID to wait for
     * @param pollIntervalMs Time between polls in milliseconds (default: 1000)
     * @param timeoutMs Maximum wait time in milliseconds (default: 30000)
     * @return The completed generation result with download URL
     * @throws RenderbaseException if the request fails or the job fails
     * @throws RuntimeException if the timeout is exceeded
     */
    public GenerateResult waitForCompletion(String jobId, long pollIntervalMs, long timeoutMs) throws RenderbaseException {
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
     * @throws RenderbaseException if the download fails
     */
    public byte[] download(String downloadUrl) throws RenderbaseException {
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
            throw new RenderbaseException("Failed to download document", e);
        }
    }
}
