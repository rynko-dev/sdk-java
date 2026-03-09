package dev.rynko.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.rynko.exceptions.RynkoException;
import dev.rynko.models.FlowApproval;
import dev.rynko.models.FlowDelivery;
import dev.rynko.models.FlowGate;
import dev.rynko.models.FlowRun;
import dev.rynko.models.ListResponse;
import dev.rynko.models.PaginationMeta;
import dev.rynko.models.SubmitRunRequest;
import dev.rynko.utils.HttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource for Rynko Flow operations.
 *
 * <p>Use this resource to submit runs for validation, manage approvals,
 * and monitor deliveries.</p>
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * // Submit a run for validation
 * FlowRun run = client.flow().submitRun("gate_abc123",
 *     SubmitRunRequest.builder()
 *         .inputField("name", "John Doe")
 *         .inputField("amount", 150.00)
 *         .build()
 * );
 *
 * // Wait for validation result
 * FlowRun result = client.flow().waitForRun(run.getId());
 * System.out.println("Status: " + result.getStatus());
 * }</pre>
 *
 * @since 1.3.0
 */
public class FlowResource {

    private final HttpClient httpClient;

    public FlowResource(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private String flowUrl(String path) {
        return httpClient.getBaseUrlWithoutVersion() + "/api/flow" + path;
    }

    // ---- Gates (read-only) ----

    /**
     * Lists all gates.
     *
     * @return Paginated list of gates
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowGate> listGates() throws RynkoException {
        return listGates(null, null, null);
    }

    /**
     * Lists gates with pagination and optional status filter.
     *
     * @param page   Page number (1-based)
     * @param limit  Number of items per page
     * @param status Filter by gate status
     * @return Paginated list of gates
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowGate> listGates(Integer page, Integer limit, String status) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(effectiveLimit));
        params.put("page", String.valueOf(effectivePage));
        if (status != null) {
            params.put("status", status);
        }

        FlowListResponse<FlowGate> response = httpClient.getAbsolute(
                flowUrl("/gates"), params,
                new TypeReference<FlowListResponse<FlowGate>>() {});

        return toListResponse(response, effectivePage, effectiveLimit);
    }

    /**
     * Gets a gate by ID.
     *
     * @param gateId The gate ID
     * @return The gate
     * @throws RynkoException if the request fails
     */
    public FlowGate getGate(String gateId) throws RynkoException {
        return httpClient.getAbsolute(flowUrl("/gates/" + gateId), FlowGate.class);
    }

    // ---- Runs ----

    /**
     * Submits a run to a gate for validation.
     *
     * @param gateId  The gate ID to submit to
     * @param request The run submission request
     * @return The created run
     * @throws RynkoException if the request fails
     */
    public FlowRun submitRun(String gateId, SubmitRunRequest request) throws RynkoException {
        return httpClient.postAbsolute(flowUrl("/gates/" + gateId + "/runs"), request, FlowRun.class);
    }

    /**
     * Gets a run by ID.
     *
     * @param runId The run ID
     * @return The run
     * @throws RynkoException if the request fails
     */
    public FlowRun getRun(String runId) throws RynkoException {
        return httpClient.getAbsolute(flowUrl("/runs/" + runId), FlowRun.class);
    }

    /**
     * Lists all runs.
     *
     * @return Paginated list of runs
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowRun> listRuns() throws RynkoException {
        return listRuns(null, null, null);
    }

    /**
     * Lists runs with pagination and optional status filter.
     *
     * @param page   Page number (1-based)
     * @param limit  Number of items per page
     * @param status Filter by run status
     * @return Paginated list of runs
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowRun> listRuns(Integer page, Integer limit, String status) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(effectiveLimit));
        params.put("page", String.valueOf(effectivePage));
        if (status != null) {
            params.put("status", status);
        }

        FlowListResponse<FlowRun> response = httpClient.getAbsolute(
                flowUrl("/runs"), params,
                new TypeReference<FlowListResponse<FlowRun>>() {});

        return toListResponse(response, effectivePage, effectiveLimit);
    }

    /**
     * Lists runs for a specific gate.
     *
     * @param gateId The gate ID
     * @return Paginated list of runs
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowRun> listRunsByGate(String gateId) throws RynkoException {
        return listRunsByGate(gateId, null, null, null);
    }

    /**
     * Lists runs for a specific gate with pagination.
     *
     * @param gateId The gate ID
     * @param page   Page number (1-based)
     * @param limit  Number of items per page
     * @param status Filter by run status
     * @return Paginated list of runs
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowRun> listRunsByGate(String gateId, Integer page, Integer limit, String status) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(effectiveLimit));
        params.put("page", String.valueOf(effectivePage));
        if (status != null) {
            params.put("status", status);
        }

        FlowListResponse<FlowRun> response = httpClient.getAbsolute(
                flowUrl("/gates/" + gateId + "/runs"), params,
                new TypeReference<FlowListResponse<FlowRun>>() {});

        return toListResponse(response, effectivePage, effectiveLimit);
    }

    /**
     * Lists active (non-terminal) runs.
     *
     * @return Paginated list of active runs
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowRun> listActiveRuns() throws RynkoException {
        return listActiveRuns(null, null);
    }

    /**
     * Lists active (non-terminal) runs with pagination.
     *
     * @param page  Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated list of active runs
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowRun> listActiveRuns(Integer page, Integer limit) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(effectiveLimit));
        params.put("page", String.valueOf(effectivePage));

        FlowListResponse<FlowRun> response = httpClient.getAbsolute(
                flowUrl("/runs/active"), params,
                new TypeReference<FlowListResponse<FlowRun>>() {});

        return toListResponse(response, effectivePage, effectiveLimit);
    }

    /**
     * Waits for a run to reach a terminal state.
     * Polls every 1 second with a 60 second timeout.
     *
     * @param runId The run ID to wait for
     * @return The completed run
     * @throws RynkoException if the request fails
     * @throws RuntimeException if the timeout is exceeded
     */
    public FlowRun waitForRun(String runId) throws RynkoException {
        return waitForRun(runId, 1000, 60000);
    }

    /**
     * Waits for a run to reach a terminal state with custom polling settings.
     *
     * @param runId          The run ID to wait for
     * @param pollIntervalMs Time between polls in milliseconds
     * @param timeoutMs      Maximum wait time in milliseconds
     * @return The completed run
     * @throws RynkoException if the request fails
     * @throws RuntimeException if the timeout is exceeded
     */
    public FlowRun waitForRun(String runId, long pollIntervalMs, long timeoutMs) throws RynkoException {
        long startTime = System.currentTimeMillis();

        while (true) {
            FlowRun run = getRun(runId);

            if (run.isTerminal()) {
                return run;
            }

            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new RuntimeException("Timeout waiting for run " + runId + " to complete");
            }

            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for run " + runId, e);
            }
        }
    }

    // ---- Approvals ----

    /**
     * Lists approvals.
     *
     * @return Paginated list of approvals
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowApproval> listApprovals() throws RynkoException {
        return listApprovals(null, null, null);
    }

    /**
     * Lists approvals with pagination and optional status filter.
     *
     * @param page   Page number (1-based)
     * @param limit  Number of items per page
     * @param status Filter by approval status
     * @return Paginated list of approvals
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowApproval> listApprovals(Integer page, Integer limit, String status) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(effectiveLimit));
        params.put("page", String.valueOf(effectivePage));
        if (status != null) {
            params.put("status", status);
        }

        FlowListResponse<FlowApproval> response = httpClient.getAbsolute(
                flowUrl("/approvals"), params,
                new TypeReference<FlowListResponse<FlowApproval>>() {});

        return toListResponse(response, effectivePage, effectiveLimit);
    }

    /**
     * Approves a pending approval.
     *
     * @param approvalId The approval ID
     * @return The updated approval
     * @throws RynkoException if the request fails
     */
    public FlowApproval approve(String approvalId) throws RynkoException {
        return approve(approvalId, null);
    }

    /**
     * Approves a pending approval with an optional note.
     *
     * @param approvalId The approval ID
     * @param note       Optional reviewer note
     * @return The updated approval
     * @throws RynkoException if the request fails
     */
    public FlowApproval approve(String approvalId, String note) throws RynkoException {
        Map<String, Object> body = new HashMap<>();
        if (note != null) {
            body.put("note", note);
        }
        return httpClient.postAbsolute(flowUrl("/approvals/" + approvalId + "/approve"), body, FlowApproval.class);
    }

    /**
     * Rejects a pending approval.
     *
     * @param approvalId The approval ID
     * @return The updated approval
     * @throws RynkoException if the request fails
     */
    public FlowApproval reject(String approvalId) throws RynkoException {
        return reject(approvalId, null);
    }

    /**
     * Rejects a pending approval with an optional reason.
     *
     * @param approvalId The approval ID
     * @param reason     Optional rejection reason
     * @return The updated approval
     * @throws RynkoException if the request fails
     */
    public FlowApproval reject(String approvalId, String reason) throws RynkoException {
        Map<String, Object> body = new HashMap<>();
        if (reason != null) {
            body.put("reason", reason);
        }
        return httpClient.postAbsolute(flowUrl("/approvals/" + approvalId + "/reject"), body, FlowApproval.class);
    }

    /**
     * Resends approval notification emails for a run.
     *
     * <p>Re-sends approval request emails to all pending approvers for a run
     * that is in {@code review_required} status.</p>
     *
     * @param runId The run ID
     * @return Map with success, sentCount, and totalApprovers
     * @throws RynkoException if the request fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> resendApprovalEmail(String runId) throws RynkoException {
        Map<String, Object> body = new HashMap<>();
        return httpClient.postAbsolute(flowUrl("/approvals/resend/" + runId), body, Map.class);
    }

    // ---- Deliveries ----

    /**
     * Lists deliveries for a run.
     *
     * @param runId The run ID
     * @return Paginated list of deliveries
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowDelivery> listDeliveries(String runId) throws RynkoException {
        return listDeliveries(runId, null, null);
    }

    /**
     * Lists deliveries for a run with pagination.
     *
     * @param runId The run ID
     * @param page  Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated list of deliveries
     * @throws RynkoException if the request fails
     */
    public ListResponse<FlowDelivery> listDeliveries(String runId, Integer page, Integer limit) throws RynkoException {
        int effectiveLimit = limit != null ? limit : 20;
        int effectivePage = page != null ? page : 1;

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(effectiveLimit));
        params.put("page", String.valueOf(effectivePage));

        FlowListResponse<FlowDelivery> response = httpClient.getAbsolute(
                flowUrl("/runs/" + runId + "/deliveries"), params,
                new TypeReference<FlowListResponse<FlowDelivery>>() {});

        return toListResponse(response, effectivePage, effectiveLimit);
    }

    /**
     * Retries a failed delivery.
     *
     * @param deliveryId The delivery ID
     * @return The updated delivery
     * @throws RynkoException if the request fails
     */
    public FlowDelivery retryDelivery(String deliveryId) throws RynkoException {
        Map<String, Object> body = new HashMap<>();
        return httpClient.postAbsolute(flowUrl("/deliveries/" + deliveryId + "/retry"), body, FlowDelivery.class);
    }

    // ---- Internal helpers ----

    private static <T> ListResponse<T> toListResponse(FlowListResponse<T> response, int page, int limit) {
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
     * Internal class to parse Flow list responses.
     */
    private static class FlowListResponse<T> {
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
