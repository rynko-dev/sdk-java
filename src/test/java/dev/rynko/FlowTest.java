package dev.rynko;

import dev.rynko.models.FlowApproval;
import dev.rynko.models.FlowDelivery;
import dev.rynko.models.FlowGate;
import dev.rynko.models.FlowRun;
import dev.rynko.models.SubmitRunRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Flow models.
 */
public class FlowTest {

    // ==========================================
    // FlowGate Tests
    // ==========================================

    @Test
    void testFlowGateConstructor() {
        FlowGate gate = new FlowGate();
        assertNotNull(gate);
    }

    @Test
    void testFlowGateGettersSetters() {
        FlowGate gate = new FlowGate();
        gate.setId("gate_abc123");
        gate.setName("Invoice Validator");
        gate.setSlug("invoice-validator");
        gate.setDescription("Validates invoice data");
        gate.setStatus("published");
        gate.setSchemaVersion(3);
        gate.setCreatedAt("2026-01-15T10:00:00Z");
        gate.setUpdatedAt("2026-02-20T14:30:00Z");

        assertEquals("gate_abc123", gate.getId());
        assertEquals("Invoice Validator", gate.getName());
        assertEquals("invoice-validator", gate.getSlug());
        assertEquals("Validates invoice data", gate.getDescription());
        assertEquals("published", gate.getStatus());
        assertEquals(3, gate.getSchemaVersion());
        assertEquals("2026-01-15T10:00:00Z", gate.getCreatedAt());
        assertEquals("2026-02-20T14:30:00Z", gate.getUpdatedAt());
    }

    @Test
    void testFlowGateToString() {
        FlowGate gate = new FlowGate();
        gate.setId("gate_abc123");
        gate.setName("Invoice Validator");
        gate.setStatus("published");

        String str = gate.toString();
        assertTrue(str.contains("gate_abc123"));
        assertTrue(str.contains("Invoice Validator"));
        assertTrue(str.contains("published"));
    }

    // ==========================================
    // FlowRun Tests
    // ==========================================

    @Test
    void testFlowRunConstructor() {
        FlowRun run = new FlowRun();
        assertNotNull(run);
    }

    @Test
    void testFlowRunGettersSetters() {
        FlowRun run = new FlowRun();
        run.setId("run_xyz789");
        run.setGateId("gate_abc123");
        run.setStatus("approved");
        run.setCreatedAt("2026-03-01T09:00:00Z");
        run.setUpdatedAt("2026-03-01T09:01:00Z");
        run.setCompletedAt("2026-03-01T09:01:00Z");

        Map<String, Object> input = new HashMap<>();
        input.put("name", "John Doe");
        input.put("amount", 150.00);
        run.setInput(input);

        Map<String, Object> output = new HashMap<>();
        output.put("validated", true);
        run.setOutput(output);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        run.setMetadata(metadata);

        assertEquals("run_xyz789", run.getId());
        assertEquals("gate_abc123", run.getGateId());
        assertEquals("approved", run.getStatus());
        assertEquals("John Doe", run.getInput().get("name"));
        assertEquals(true, run.getOutput().get("validated"));
        assertEquals("test", run.getMetadata().get("source"));
        assertEquals("2026-03-01T09:00:00Z", run.getCreatedAt());
        assertEquals("2026-03-01T09:01:00Z", run.getUpdatedAt());
        assertEquals("2026-03-01T09:01:00Z", run.getCompletedAt());
    }

    @Test
    void testFlowRunIsTerminalForTerminalStatuses() {
        String[] terminalStatuses = {
            "completed", "delivered", "approved", "rejected",
            "validation_failed", "render_failed", "delivery_failed"
        };

        for (String status : terminalStatuses) {
            FlowRun run = new FlowRun();
            run.setStatus(status);
            assertTrue(run.isTerminal(), "Expected '" + status + "' to be terminal");
        }
    }

    @Test
    void testFlowRunIsTerminalForNonTerminalStatuses() {
        String[] nonTerminalStatuses = {
            "pending", "validating", "review_required", "rendering", "delivering"
        };

        for (String status : nonTerminalStatuses) {
            FlowRun run = new FlowRun();
            run.setStatus(status);
            assertFalse(run.isTerminal(), "Expected '" + status + "' to NOT be terminal");
        }
    }

    @Test
    void testFlowRunIsTerminalWithNullStatus() {
        FlowRun run = new FlowRun();
        assertFalse(run.isTerminal());
    }

    @Test
    void testFlowRunToString() {
        FlowRun run = new FlowRun();
        run.setId("run_xyz789");
        run.setGateId("gate_abc123");
        run.setStatus("approved");

        String str = run.toString();
        assertTrue(str.contains("run_xyz789"));
        assertTrue(str.contains("gate_abc123"));
        assertTrue(str.contains("approved"));
    }

    // ==========================================
    // FlowRun.FlowValidationError Tests
    // ==========================================

    @Test
    void testFlowValidationErrorConstructor() {
        FlowRun.FlowValidationError error = new FlowRun.FlowValidationError();
        assertNotNull(error);
    }

    @Test
    void testFlowValidationErrorGettersSetters() {
        FlowRun.FlowValidationError error = new FlowRun.FlowValidationError();
        error.setField("amount");
        error.setRule("min_value");
        error.setMessage("Amount must be greater than 0");

        assertEquals("amount", error.getField());
        assertEquals("min_value", error.getRule());
        assertEquals("Amount must be greater than 0", error.getMessage());
    }

    @Test
    void testFlowValidationErrorToString() {
        FlowRun.FlowValidationError error = new FlowRun.FlowValidationError();
        error.setField("email");
        error.setMessage("Invalid email format");

        String str = error.toString();
        assertTrue(str.contains("email"));
        assertTrue(str.contains("Invalid email format"));
    }

    // ==========================================
    // FlowApproval Tests
    // ==========================================

    @Test
    void testFlowApprovalConstructor() {
        FlowApproval approval = new FlowApproval();
        assertNotNull(approval);
    }

    @Test
    void testFlowApprovalGettersSetters() {
        FlowApproval approval = new FlowApproval();
        approval.setId("apr_001");
        approval.setRunId("run_xyz789");
        approval.setGateId("gate_abc123");
        approval.setStatus("pending");
        approval.setReviewerEmail("reviewer@example.com");
        approval.setReviewerNote("Looks good");
        approval.setCreatedAt("2026-03-01T09:00:00Z");
        approval.setUpdatedAt("2026-03-01T09:05:00Z");
        approval.setResolvedAt("2026-03-01T09:05:00Z");

        assertEquals("apr_001", approval.getId());
        assertEquals("run_xyz789", approval.getRunId());
        assertEquals("gate_abc123", approval.getGateId());
        assertEquals("pending", approval.getStatus());
        assertEquals("reviewer@example.com", approval.getReviewerEmail());
        assertEquals("Looks good", approval.getReviewerNote());
        assertEquals("2026-03-01T09:00:00Z", approval.getCreatedAt());
        assertEquals("2026-03-01T09:05:00Z", approval.getUpdatedAt());
        assertEquals("2026-03-01T09:05:00Z", approval.getResolvedAt());
    }

    @Test
    void testFlowApprovalToString() {
        FlowApproval approval = new FlowApproval();
        approval.setId("apr_001");
        approval.setRunId("run_xyz789");
        approval.setStatus("pending");

        String str = approval.toString();
        assertTrue(str.contains("apr_001"));
        assertTrue(str.contains("run_xyz789"));
        assertTrue(str.contains("pending"));
    }

    // ==========================================
    // FlowDelivery Tests
    // ==========================================

    @Test
    void testFlowDeliveryConstructor() {
        FlowDelivery delivery = new FlowDelivery();
        assertNotNull(delivery);
    }

    @Test
    void testFlowDeliveryGettersSetters() {
        FlowDelivery delivery = new FlowDelivery();
        delivery.setId("del_001");
        delivery.setRunId("run_xyz789");
        delivery.setStatus("delivered");
        delivery.setUrl("https://webhook.example.com/callback");
        delivery.setHttpStatus(200);
        delivery.setAttempts(1);
        delivery.setLastAttemptAt("2026-03-01T09:02:00Z");
        delivery.setError(null);
        delivery.setCreatedAt("2026-03-01T09:01:00Z");

        assertEquals("del_001", delivery.getId());
        assertEquals("run_xyz789", delivery.getRunId());
        assertEquals("delivered", delivery.getStatus());
        assertEquals("https://webhook.example.com/callback", delivery.getUrl());
        assertEquals(200, delivery.getHttpStatus());
        assertEquals(1, delivery.getAttempts());
        assertEquals("2026-03-01T09:02:00Z", delivery.getLastAttemptAt());
        assertNull(delivery.getError());
        assertEquals("2026-03-01T09:01:00Z", delivery.getCreatedAt());
    }

    @Test
    void testFlowDeliveryToString() {
        FlowDelivery delivery = new FlowDelivery();
        delivery.setId("del_001");
        delivery.setRunId("run_xyz789");
        delivery.setStatus("delivered");

        String str = delivery.toString();
        assertTrue(str.contains("del_001"));
        assertTrue(str.contains("run_xyz789"));
        assertTrue(str.contains("delivered"));
    }

    // ==========================================
    // SubmitRunRequest Tests
    // ==========================================

    @Test
    void testSubmitRunRequestBuilder() {
        SubmitRunRequest request = SubmitRunRequest.builder()
                .inputField("name", "John Doe")
                .inputField("amount", 150.00)
                .build();

        assertNotNull(request);
        assertEquals("John Doe", request.getInput().get("name"));
        assertEquals(150.00, request.getInput().get("amount"));
    }

    @Test
    void testSubmitRunRequestInputFieldMethod() {
        SubmitRunRequest request = SubmitRunRequest.builder()
                .inputField("field1", "value1")
                .inputField("field2", 42)
                .inputField("field3", true)
                .build();

        assertEquals("value1", request.getInput().get("field1"));
        assertEquals(42, request.getInput().get("field2"));
        assertEquals(true, request.getInput().get("field3"));
    }

    @Test
    void testSubmitRunRequestInputRequired() {
        assertThrows(IllegalArgumentException.class, () -> {
            SubmitRunRequest.builder().build();
        });
    }

    @Test
    void testSubmitRunRequestWithMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test-suite");
        metadata.put("priority", 1);

        SubmitRunRequest request = SubmitRunRequest.builder()
                .inputField("name", "Test")
                .metadata(metadata)
                .build();

        assertNotNull(request.getMetadata());
        assertEquals("test-suite", request.getMetadata().get("source"));
        assertEquals(1, request.getMetadata().get("priority"));
    }

    @Test
    void testSubmitRunRequestWithWebhookUrl() {
        SubmitRunRequest request = SubmitRunRequest.builder()
                .inputField("name", "Test")
                .webhookUrl("https://webhook.example.com/callback")
                .build();

        assertEquals("https://webhook.example.com/callback", request.getWebhookUrl());
    }

    @Test
    void testSubmitRunRequestWithInputMap() {
        Map<String, Object> input = new HashMap<>();
        input.put("email", "test@example.com");
        input.put("amount", 99.99);

        SubmitRunRequest request = SubmitRunRequest.builder()
                .input(input)
                .build();

        assertEquals("test@example.com", request.getInput().get("email"));
        assertEquals(99.99, request.getInput().get("amount"));
    }

    @Test
    void testSubmitRunRequestMetadataIsNullByDefault() {
        SubmitRunRequest request = SubmitRunRequest.builder()
                .inputField("name", "Test")
                .build();

        assertNull(request.getMetadata());
    }

    @Test
    void testSubmitRunRequestWebhookUrlIsNullByDefault() {
        SubmitRunRequest request = SubmitRunRequest.builder()
                .inputField("name", "Test")
                .build();

        assertNull(request.getWebhookUrl());
    }

    // ==========================================
    // FlowResource Accessibility Test
    // ==========================================

    @Test
    void testFlowResourceAccessible() {
        Rynko client = new Rynko("test-key");
        assertNotNull(client.flow());
    }
}
