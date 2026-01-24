package com.renderbase;

import com.renderbase.models.GenerateRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Renderbase client.
 */
public class RenderbaseTest {

    @Test
    void testClientCreation() {
        Renderbase client = new Renderbase("test-api-key");
        assertNotNull(client);
        assertNotNull(client.documents());
        assertNotNull(client.templates());
        assertNotNull(client.webhooks());
    }

    @Test
    void testClientCreationWithConfig() {
        RenderbaseConfig config = RenderbaseConfig.builder()
                .apiKey("test-api-key")
                .baseUrl("https://custom.api.com/v1")
                .timeoutMs(60000)
                .build();

        Renderbase client = new Renderbase(config);
        assertNotNull(client);
    }

    @Test
    void testClientCreationWithoutApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Renderbase((String) null);
        });
    }

    @Test
    void testClientCreationWithEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Renderbase("");
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
    void testConfigBuilderDefaults() {
        RenderbaseConfig config = RenderbaseConfig.builder()
                .apiKey("test-key")
                .build();

        assertEquals("test-key", config.getApiKey());
        assertEquals("https://api.renderbase.dev/api/v1", config.getBaseUrl());
        assertEquals(30000, config.getTimeoutMs());
    }

    @Test
    void testConfigBuilderCustomValues() {
        RenderbaseConfig config = RenderbaseConfig.builder()
                .apiKey("test-key")
                .baseUrl("https://custom.com/v2")
                .timeoutMs(60000)
                .build();

        assertEquals("https://custom.com/v2", config.getBaseUrl());
        assertEquals(60000, config.getTimeoutMs());
    }
}
