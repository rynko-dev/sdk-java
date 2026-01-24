package com.renderbase;

import com.renderbase.exceptions.WebhookSignatureException;
import com.renderbase.resources.WebhooksResource;
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
        Renderbase client = new Renderbase("test-api-key");
        webhooksResource = client.webhooks();
    }

    @Test
    void testValidSignature() throws Exception {
        String payload = "{\"type\":\"document.completed\",\"data\":{\"id\":\"123\"}}";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = "v1=" + computeSignature(timestamp + "." + payload, TEST_SECRET);

        // Should not throw
        assertDoesNotThrow(() -> {
            webhooksResource.verifySignature(payload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testInvalidSignature() {
        String payload = "{\"type\":\"document.completed\"}";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = "v1=invalid_signature";

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(payload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testExpiredTimestamp() throws Exception {
        String payload = "{\"type\":\"document.completed\"}";
        // Timestamp from 10 minutes ago (beyond 5 minute tolerance)
        String timestamp = String.valueOf((System.currentTimeMillis() / 1000) - 600);
        String signature = "v1=" + computeSignature(timestamp + "." + payload, TEST_SECRET);

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(payload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testFutureTimestamp() throws Exception {
        String payload = "{\"type\":\"document.completed\"}";
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
        String payload = "{\"type\":\"document.completed\"}";
        String signature = "v1=somesig";

        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(payload, signature, "not-a-number", TEST_SECRET);
        });
    }

    @Test
    void testSignatureWithoutPrefix() throws Exception {
        String payload = "{\"type\":\"document.completed\"}";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        // Signature without v1= prefix
        String signature = computeSignature(timestamp + "." + payload, TEST_SECRET);

        assertDoesNotThrow(() -> {
            webhooksResource.verifySignature(payload, signature, timestamp, TEST_SECRET);
        });
    }

    @Test
    void testTamperedPayload() throws Exception {
        String originalPayload = "{\"type\":\"document.completed\",\"data\":{\"id\":\"123\"}}";
        String tamperedPayload = "{\"type\":\"document.completed\",\"data\":{\"id\":\"456\"}}";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = "v1=" + computeSignature(timestamp + "." + originalPayload, TEST_SECRET);

        // Verify with tampered payload should fail
        assertThrows(WebhookSignatureException.class, () -> {
            webhooksResource.verifySignature(tamperedPayload, signature, timestamp, TEST_SECRET);
        });
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
