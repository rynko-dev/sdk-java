package dev.rynko;

import dev.rynko.exceptions.WebhookSignatureException;
import dev.rynko.resources.WebhooksResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for webhook signature verification.
 */
public class WebhookSignatureTest {

    private WebhooksResource webhooksResource;
    private static final String TEST_SECRET = "whsec_test_secret_key";

    @BeforeEach
    void setUp() {
        Rynko client = new Rynko("test-api-key");
        webhooksResource = client.webhooks();
    }

    @Test
    void testValidSignature() throws Exception {
        String payload = "{\"type\":\"document.generated\",\"data\":{\"id\":\"123\"}}";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = "v1=" + computeSignature(timestamp + "." + payload, TEST_SECRET);

        // Should not throw
        assertDoesNotThrow(() -> {
            webhooksResource.verifySignature(payload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testInvalidSignature() {
        String payload = "{\"type\":\"document.generated\"}";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = "v1=invalid_signature";

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(payload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testExpiredTimestamp() throws Exception {
        String payload = "{\"type\":\"document.generated\"}";
        // Timestamp from 10 minutes ago (beyond 5 minute tolerance)
        String timestamp = String.valueOf((System.currentTimeMillis() / 1000) - 600);
        String signature = "v1=" + computeSignature(timestamp + "." + payload, TEST_SECRET);

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(payload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testFutureTimestamp() throws Exception {
        String payload = "{\"type\":\"document.generated\"}";
        // Timestamp from 10 minutes in the future (beyond 5 minute tolerance)
        String timestamp = String.valueOf((System.currentTimeMillis() / 1000) + 600);
        String signature = "v1=" + computeSignature(timestamp + "." + payload, TEST_SECRET);

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(payload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testNullParameters() {
        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(null, "sig", "ts", TEST_SECRET);
        });

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature("payload", null, "ts", TEST_SECRET);
        });

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature("payload", "sig", null, TEST_SECRET);
        });

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature("payload", "sig", "ts", null);
        });
    }

    @Test
    void testInvalidTimestampFormat() {
        String payload = "{\"type\":\"document.generated\"}";
        String signature = "v1=somesig";

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(payload, signature, "not-a-number", TEST_SECRET);
        });
    }

    @Test
    void testSignatureWithoutPrefix() throws Exception {
        String payload = "{\"type\":\"document.generated\"}";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        // Signature without v1= prefix
        String signature = computeSignature(timestamp + "." + payload, TEST_SECRET);

        assertDoesNotThrow(() -> {
            webhooksResource.verifySignature(payload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testTamperedPayload() throws Exception {
        String originalPayload = "{\"type\":\"document.generated\",\"data\":{\"id\":\"123\"}}";
        String tamperedPayload = "{\"type\":\"document.generated\",\"data\":{\"id\":\"456\"}}";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = "v1=" + computeSignature(timestamp + "." + originalPayload, TEST_SECRET);

        // Verify with tampered payload should fail
        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(tamperedPayload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testConstructEventWithMetadata() throws Exception {
        // Test webhook event with metadata in the data payload
        String payload = "{\"id\":\"evt_123\",\"type\":\"document.generated\",\"timestamp\":\"2025-02-02T12:00:00Z\",\"data\":{\"jobId\":\"job_456\",\"status\":\"completed\",\"downloadUrl\":\"https://example.com/download\",\"metadata\":{\"orderId\":\"ord_789\",\"customerId\":\"cust_012\",\"priority\":1}}}";

        WebhooksResource.WebhookEvent event = webhooksResource.constructEvent(payload);

        assertEquals("evt_123", event.getId());
        assertEquals("document.generated", event.getType());
        assertTrue(event.isDocumentEvent());
        assertFalse(event.isBatchEvent());

        // Get typed document data
        WebhooksResource.DocumentWebhookData docData = event.getDocumentData();
        assertEquals("job_456", docData.getJobId());
        assertEquals("completed", docData.getStatus());
        assertEquals("https://example.com/download", docData.getDownloadUrl());

        // Verify metadata
        assertNotNull(docData.getMetadata());
        assertEquals("ord_789", docData.getMetadata().get("orderId"));
        assertEquals("cust_012", docData.getMetadata().get("customerId"));
    }

    @Test
    void testConstructBatchEventWithMetadata() throws Exception {
        // Test batch webhook event with metadata
        String payload = "{\"id\":\"evt_batch_123\",\"type\":\"batch.completed\",\"timestamp\":\"2025-02-02T12:00:00Z\",\"data\":{\"batchId\":\"batch_456\",\"status\":\"completed\",\"totalJobs\":10,\"completedJobs\":8,\"failedJobs\":2,\"metadata\":{\"batchRunId\":\"run_001\",\"triggeredBy\":\"scheduled\"}}}";

        WebhooksResource.WebhookEvent event = webhooksResource.constructEvent(payload);

        assertEquals("evt_batch_123", event.getId());
        assertEquals("batch.completed", event.getType());
        assertFalse(event.isDocumentEvent());
        assertTrue(event.isBatchEvent());

        // Get typed batch data
        WebhooksResource.BatchWebhookData batchData = event.getBatchData();
        assertEquals("batch_456", batchData.getBatchId());
        assertEquals("completed", batchData.getStatus());
        assertEquals(10, batchData.getTotalJobs());
        assertEquals(8, batchData.getCompletedJobs());
        assertEquals(2, batchData.getFailedJobs());

        // Verify batch metadata
        assertNotNull(batchData.getMetadata());
        assertEquals("run_001", batchData.getMetadata().get("batchRunId"));
        assertEquals("scheduled", batchData.getMetadata().get("triggeredBy"));
    }

    @Test
    void testConstructEventWithFailedDocument() throws Exception {
        // Test failed document event with error info and metadata
        String payload = "{\"id\":\"evt_fail\",\"type\":\"document.failed\",\"timestamp\":\"2025-02-02T12:00:00Z\",\"data\":{\"jobId\":\"job_fail\",\"status\":\"failed\",\"errorMessage\":\"Template not found\",\"errorCode\":\"ERR_TMPL_001\",\"metadata\":{\"orderId\":\"ord_failed\"}}}";

        WebhooksResource.WebhookEvent event = webhooksResource.constructEvent(payload);

        assertEquals("document.failed", event.getType());
        assertTrue(event.isDocumentEvent());

        WebhooksResource.DocumentWebhookData docData = event.getDocumentData();
        assertEquals("job_fail", docData.getJobId());
        assertEquals("failed", docData.getStatus());
        assertEquals("Template not found", docData.getErrorMessage());
        assertEquals("ERR_TMPL_001", docData.getErrorCode());
        assertNull(docData.getDownloadUrl());

        // Verify metadata is still present on failed events
        assertNotNull(docData.getMetadata());
        assertEquals("ord_failed", docData.getMetadata().get("orderId"));
    }

    @Test
    void testConstructEventWithoutMetadata() throws Exception {
        // Test event without metadata (should work without errors)
        String payload = "{\"id\":\"evt_no_meta\",\"type\":\"document.generated\",\"timestamp\":\"2025-02-02T12:00:00Z\",\"data\":{\"jobId\":\"job_789\",\"status\":\"completed\",\"downloadUrl\":\"https://example.com/dl\"}}";

        WebhooksResource.WebhookEvent event = webhooksResource.constructEvent(payload);

        WebhooksResource.DocumentWebhookData docData = event.getDocumentData();
        assertEquals("job_789", docData.getJobId());
        assertNull(docData.getMetadata());
    }

    private String computeSignature(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
        );
        mac.init(secretKey);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hmacBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
