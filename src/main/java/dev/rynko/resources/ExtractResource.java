package dev.rynko.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.rynko.exceptions.RynkoException;
import dev.rynko.models.CreateConfigRequest;
import dev.rynko.models.DiscoverRequest;
import dev.rynko.models.ExtractConfig;
import dev.rynko.models.ExtractJob;
import dev.rynko.models.ExtractJobRequest;
import dev.rynko.models.ExtractUsage;
import dev.rynko.models.FlowRun;
import dev.rynko.models.ListResponse;
import dev.rynko.models.PaginationMeta;
import dev.rynko.models.UpdateConfigRequest;
import dev.rynko.utils.HttpClient;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource for Rynko Extract operations.
 *
 * <p>Use this resource to extract structured data from documents using AI.
 * Supports creating extraction jobs, managing extraction configurations,
 * and integrating with Flow gates.</p>
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * // Create an extraction job
 * ExtractJob job = client.extract().createJob(
 *     ExtractJobRequest.builder()
 *         .file(new File("invoice.pdf"))
 *         .schema(schema)
 *         .build()
 * );
 *
 * System.out.println("Job ID: " + job.getId());
 * System.out.println("Status: " + job.getStatus());
 * }</pre>
 *
 * @since 1.4.0
 */
public class ExtractResource {

    private final HttpClient httpClient;

    public ExtractResource(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private String extractUrl(String path) {
        return httpClient.getBaseUrlWithoutVersion() + "/api/extract" + path;
    }

    private String flowUrl(String path) {
        return httpClient.getBaseUrlWithoutVersion() + "/api/flow" + path;
    }

    // ---- Jobs ----

    /**
     * Creates an extraction job.
     *
     * @param request The extraction job request with files and schema
     * @return The created extraction job
     * @throws RynkoException if the request fails
     */
    public ExtractJob createJob(ExtractJobRequest request) throws RynkoException {
        Map<String, String> formFields = new HashMap<>();
        if (request.getSchemaId() != null) {
            formFields.put("schemaId", request.getSchemaId());
        }
        if (request.getGateId() != null) {
            formFields.put("gateId", request.getGateId());
        }
        if (request.getInstructions() != null) {
            formFields.put("instructions", request.getInstructions());
        }

        if (request.getSchema() != null || request.getMetadata() != null) {
            Map<String, Object> jsonParts = new HashMap<>();
            if (request.getSchema() != null) {
                jsonParts.put("schema", request.getSchema());
            }
            if (request.getMetadata() != null) {
                jsonParts.put("metadata", request.getMetadata());
            }
            // Use the multipart with json approach for complex objects
            return httpClient.postMultipartWithJson(
                    extractUrl("/jobs"), request.getFiles(), jsonParts, "options", ExtractJob.class);
        }

        return httpClient.postMultipart(extractUrl("/jobs"), request.getFiles(), formFields, ExtractJob.class);
    }

    /**
     * Gets an extraction job by ID.
     *
     * @param jobId The job ID
     * @return The extraction job
     * @throws RynkoException if the request fails
     */
    public ExtractJob getJob(String jobId) throws RynkoException {
        return httpClient.getAbsolute(extractUrl("/jobs/" + jobId), ExtractJob.class);
    }

    /**
     * Lists extraction jobs.
     *
     * @return Paginated list of extraction jobs
     * @throws RynkoException if the request fails
     */
    public ListResponse<ExtractJob> listJobs() throws RynkoException {
        return listJobs(null, null, null);
    }

    /**
     * Lists extraction jobs with pagination and optional status filter.
     *
     * @param page   Page number (1-based)
     * @param limit  Number of items per page
     * @param status Filter by job status
     * @return Paginated list of extraction jobs
     * @throws RynkoException if the request fails
     */
    public ListResponse<ExtractJob> listJobs(Integer page, Integer limit, String status) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(effectiveLimit));
        params.put("page", String.valueOf(effectivePage));
        if (status != null) {
            params.put("status", status);
        }

        ExtractListResponse<ExtractJob> response = httpClient.getAbsolute(
                extractUrl("/jobs"), params,
                new TypeReference<ExtractListResponse<ExtractJob>>() {});

        return toListResponse(response, effectivePage, effectiveLimit);
    }

    /**
     * Cancels an extraction job.
     *
     * @param jobId The job ID to cancel
     * @throws RynkoException if the request fails
     */
    public void cancelJob(String jobId) throws RynkoException {
        httpClient.deleteAbsolute(extractUrl("/jobs/" + jobId));
    }

    /**
     * Gets extraction usage statistics.
     *
     * @return The usage statistics
     * @throws RynkoException if the request fails
     */
    public ExtractUsage getUsage() throws RynkoException {
        return httpClient.getAbsolute(extractUrl("/usage"), ExtractUsage.class);
    }

    /**
     * Discovers extraction schema from sample files.
     *
     * @param request The discover request with files
     * @return The discovered extraction job with inferred schema
     * @throws RynkoException if the request fails
     */
    public ExtractJob discover(DiscoverRequest request) throws RynkoException {
        Map<String, String> formFields = new HashMap<>();
        if (request.getInstructions() != null) {
            formFields.put("instructions", request.getInstructions());
        }

        return httpClient.postMultipart(extractUrl("/discover"), request.getFiles(), formFields, ExtractJob.class);
    }

    // ---- Configs ----

    /**
     * Creates an extraction configuration.
     *
     * @param request The create config request
     * @return The created configuration
     * @throws RynkoException if the request fails
     */
    public ExtractConfig createConfig(CreateConfigRequest request) throws RynkoException {
        return httpClient.postAbsolute(extractUrl("/configs"), request, ExtractConfig.class);
    }

    /**
     * Gets an extraction configuration by ID.
     *
     * @param configId The config ID
     * @return The configuration
     * @throws RynkoException if the request fails
     */
    public ExtractConfig getConfig(String configId) throws RynkoException {
        return httpClient.getAbsolute(extractUrl("/configs/" + configId), ExtractConfig.class);
    }

    /**
     * Lists extraction configurations.
     *
     * @return Paginated list of configurations
     * @throws RynkoException if the request fails
     */
    public ListResponse<ExtractConfig> listConfigs() throws RynkoException {
        return listConfigs(null, null, null);
    }

    /**
     * Lists extraction configurations with pagination and optional status filter.
     *
     * @param page   Page number (1-based)
     * @param limit  Number of items per page
     * @param status Filter by config status
     * @return Paginated list of configurations
     * @throws RynkoException if the request fails
     */
    public ListResponse<ExtractConfig> listConfigs(Integer page, Integer limit, String status) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(effectiveLimit));
        params.put("page", String.valueOf(effectivePage));
        if (status != null) {
            params.put("status", status);
        }

        ExtractListResponse<ExtractConfig> response = httpClient.getAbsolute(
                extractUrl("/configs"), params,
                new TypeReference<ExtractListResponse<ExtractConfig>>() {});

        return toListResponse(response, effectivePage, effectiveLimit);
    }

    /**
     * Updates an extraction configuration.
     *
     * @param configId The config ID
     * @param request  The update request
     * @return The updated configuration
     * @throws RynkoException if the request fails
     */
    public ExtractConfig updateConfig(String configId, UpdateConfigRequest request) throws RynkoException {
        return httpClient.patchAbsolute(extractUrl("/configs/" + configId), request, ExtractConfig.class);
    }

    /**
     * Deletes an extraction configuration.
     *
     * @param configId The config ID
     * @throws RynkoException if the request fails
     */
    public void deleteConfig(String configId) throws RynkoException {
        httpClient.deleteAbsolute(extractUrl("/configs/" + configId));
    }

    /**
     * Publishes an extraction configuration.
     *
     * @param configId The config ID
     * @return The published configuration
     * @throws RynkoException if the request fails
     */
    public ExtractConfig publishConfig(String configId) throws RynkoException {
        Map<String, Object> body = new HashMap<>();
        return httpClient.postAbsolute(extractUrl("/configs/" + configId + "/publish"), body, ExtractConfig.class);
    }

    /**
     * Gets version history for an extraction configuration.
     *
     * @param configId The config ID
     * @return Paginated list of config versions
     * @throws RynkoException if the request fails
     */
    public ListResponse<ExtractConfig> getConfigVersions(String configId) throws RynkoException {
        ExtractListResponse<ExtractConfig> response = httpClient.getAbsolute(
                extractUrl("/configs/" + configId + "/versions"), null,
                new TypeReference<ExtractListResponse<ExtractConfig>>() {});

        return toListResponse(response, 1, 100);
    }

    /**
     * Restores a specific version of an extraction configuration.
     *
     * @param configId  The config ID
     * @param versionId The version ID to restore
     * @return The restored configuration
     * @throws RynkoException if the request fails
     */
    public ExtractConfig restoreConfigVersion(String configId, String versionId) throws RynkoException {
        Map<String, Object> body = new HashMap<>();
        return httpClient.postAbsolute(
                extractUrl("/configs/" + configId + "/versions/" + versionId + "/restore"),
                body, ExtractConfig.class);
    }

    /**
     * Runs an extraction configuration against files.
     *
     * @param configId The config ID
     * @param files    Files to extract from
     * @return The extraction job
     * @throws RynkoException if the request fails
     */
    public ExtractJob runConfig(String configId, List<File> files) throws RynkoException {
        return httpClient.postMultipart(
                extractUrl("/configs/" + configId + "/run"), files, null, ExtractJob.class);
    }

    /**
     * Extracts data using a Flow gate.
     *
     * @param gateId The gate ID
     * @param files  Files to extract from
     * @return The extraction job
     * @throws RynkoException if the request fails
     */
    public ExtractJob extractWithGate(String gateId, List<File> files) throws RynkoException {
        return httpClient.postMultipart(
                flowUrl("/gates/" + gateId + "/extract"), files, null, ExtractJob.class);
    }

    /**
     * Submits files to a Flow gate for processing (Stage 0 file extraction).
     *
     * @param gateId The gate ID
     * @param files  Files to submit
     * @return The created Flow run
     * @throws RynkoException if the request fails
     */
    public FlowRun submitFileRun(String gateId, List<File> files) throws RynkoException {
        return httpClient.postMultipart(
                flowUrl("/gates/" + gateId + "/runs/file"), files, null, FlowRun.class);
    }

    // ---- Internal helpers ----

    private static <T> ListResponse<T> toListResponse(ExtractListResponse<T> response, int page, int limit) {
        ListResponse<T> result = new ListResponse<>();
        result.setData(response.getData());

        PaginationMeta meta = new PaginationMeta();
        meta.setTotal(response.getTotal());
        meta.setPage(page);
        meta.setLimit(limit);
        meta.setTotalPages(limit > 0 ? (response.getTotal() + limit - 1) / limit : 1);
        result.setMeta(meta);

        return result;
    }

    /**
     * Internal class to parse Extract list responses.
     */
    private static class ExtractListResponse<T> {
        @JsonProperty("data")
        private List<T> data;

        @JsonProperty("total")
        private int total;

        public List<T> getData() { return data; }
        public void setData(List<T> data) { this.data = data; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }
}
