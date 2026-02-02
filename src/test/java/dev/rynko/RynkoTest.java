package dev.rynko;

import dev.rynko.models.GenerateRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Rynko client.
 */
public class RynkoTest {

    @Test
    void testClientCreation() {
        Rynko client = new Rynko("test-api-key");
        assertNotNull(client);
        assertNotNull(client.documents());
        assertNotNull(client.templates());
        assertNotNull(client.webhooks());
    }

    @Test
    void testClientCreationWithConfig() {
        RynkoConfig config = RynkoConfig.builder()
                .apiKey("test-api-key")
                .baseUrl("https://custom.api.com/v1")
                .timeoutMs(60000)
                .build();

        Rynko client = new Rynko(config);
        assertNotNull(client);
    }

    @Test
    void testClientCreationWithoutApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Rynko((String) null);
        });
    }

    @Test
    void testClientCreationWithEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Rynko("");
        });
    }

    @Test
    void testGenerateRequestBuilder() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Test");

        GenerateRequest request = GenerateRequest.builder()
                .templateId("tmpl_test")
                .format("pdf")
                .variables(variables)
                .filename("test.pdf")
                .build();

        assertEquals("tmpl_test", request.getTemplateId());
        assertEquals("pdf", request.getFormat());
        assertEquals("Test", request.getVariables().get("name"));
        assertEquals("test.pdf", request.getFilename());
    }

    @Test
    void testGenerateRequestBuilderWithVariableMethod() {
        GenerateRequest request = GenerateRequest.builder()
                .templateId("tmpl_test")
                .variable("key1", "value1")
                .variable("key2", 123)
                .build();

        assertEquals("value1", request.getVariables().get("key1"));
        assertEquals(123, request.getVariables().get("key2"));
    }

    @Test
    void testGenerateRequestBuilderWithWorkspaceId() {
        GenerateRequest request = GenerateRequest.builder()
                .templateId("tmpl_test")
                .workspaceId("ws_abc123")
                .variable("name", "Test")
                .build();

        assertEquals("tmpl_test", request.getTemplateId());
        assertEquals("ws_abc123", request.getWorkspaceId());
    }

    @Test
    void testGenerateRequestRequiresTemplateId() {
        assertThrows(IllegalArgumentException.class, () -> {
            GenerateRequest.builder()
                    .format("pdf")
                    .build();
        });
    }

    @Test
    void testGenerateRequestDefaultFormat() {
        GenerateRequest request = GenerateRequest.builder()
                .templateId("tmpl_test")
                .build();

        assertEquals("pdf", request.getFormat());
    }

    @Test
    void testGenerateRequestWithMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orderId", "ord_12345");
        metadata.put("customerId", "cust_67890");
        metadata.put("priority", 1);
        metadata.put("isUrgent", true);
        metadata.put("discount", null);

        GenerateRequest request = GenerateRequest.builder()
                .templateId("tmpl_test")
                .format("pdf")
                .metadata(metadata)
                .build();

        assertNotNull(request.getMetadata());
        assertEquals("ord_12345", request.getMetadata().get("orderId"));
        assertEquals("cust_67890", request.getMetadata().get("customerId"));
        assertEquals(1, request.getMetadata().get("priority"));
        assertEquals(true, request.getMetadata().get("isUrgent"));
        assertNull(request.getMetadata().get("discount"));
    }

    @Test
    void testGenerateRequestMetadataTypes() {
        // Test that metadata supports all valid types: string, number, boolean, null
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("stringValue", "hello");
        metadata.put("intValue", 42);
        metadata.put("floatValue", 3.14);
        metadata.put("boolTrue", true);
        metadata.put("boolFalse", false);
        metadata.put("nullValue", null);

        GenerateRequest request = GenerateRequest.builder()
                .templateId("tmpl_test")
                .metadata(metadata)
                .build();

        Map<String, Object> result = request.getMetadata();
        assertEquals("hello", result.get("stringValue"));
        assertEquals(42, result.get("intValue"));
        assertEquals(3.14, result.get("floatValue"));
        assertEquals(true, result.get("boolTrue"));
        assertEquals(false, result.get("boolFalse"));
        assertNull(result.get("nullValue"));
    }

    @Test
    void testGenerateRequestWithVariablesAndMetadata() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("invoiceNumber", "INV-001");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orderId", "ord_123");

        GenerateRequest request = GenerateRequest.builder()
                .templateId("tmpl_test")
                .variables(variables)
                .metadata(metadata)
                .build();

        assertEquals("INV-001", request.getVariables().get("invoiceNumber"));
        assertEquals("ord_123", request.getMetadata().get("orderId"));
    }

    @Test
    void testConfigBuilderDefaults() {
        RynkoConfig config = RynkoConfig.builder()
                .apiKey("test-key")
                .build();

        assertEquals("test-key", config.getApiKey());
        assertEquals("https://api.rynko.dev/api/v1", config.getBaseUrl());
        assertEquals(30000, config.getTimeoutMs());
    }

    @Test
    void testConfigBuilderCustomValues() {
        RynkoConfig config = RynkoConfig.builder()
                .apiKey("test-key")
                .baseUrl("https://custom.com/v2")
                .timeoutMs(60000)
                .build();

        assertEquals("https://custom.com/v2", config.getBaseUrl());
        assertEquals(60000, config.getTimeoutMs());
    }
}
