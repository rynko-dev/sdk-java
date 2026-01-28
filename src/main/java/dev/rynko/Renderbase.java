package dev.rynko;

import dev.rynko.exceptions.RynkoException;
import dev.rynko.models.User;
import dev.rynko.resources.DocumentsResource;
import dev.rynko.resources.TemplatesResource;
import dev.rynko.resources.WebhooksResource;
import dev.rynko.utils.HttpClient;

/**
 * Rynko Java SDK client.
 *
 * <p>The main entry point for interacting with the Rynko API.
 * Use this client to generate PDF and Excel documents from templates.</p>
 *
 * <h2>Example usage:</h2>
 * <pre>{@code
 * Rynko client = new Rynko("your-api-key");
 *
 * // Generate a PDF document
 * Map<String, Object> variables = new HashMap<>();
 * variables.put("invoiceNumber", "INV-001");
 * variables.put("customerName", "Acme Corp");
 *
 * GenerateResult result = client.documents().generate(
 *     GenerateRequest.builder()
 *         .templateId("tmpl_invoice")
 *         .format("pdf")
 *         .variables(variables)
 *         .build()
 * );
 *
 * System.out.println("Download URL: " + result.getDownloadUrl());
 * }</pre>
 *
 * @since 1.0.0
 */
public class Rynko {

    private static final String DEFAULT_BASE_URL = "https://api.rynko.dev/api/v1";

    private final HttpClient httpClient;
    private final DocumentsResource documents;
    private final TemplatesResource templates;
    private final WebhooksResource webhooks;

    /**
     * Creates a new Rynko client with the specified API key.
     *
     * @param apiKey Your Rynko API key
     */
    public Rynko(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new Rynko client with a custom base URL.
     *
     * @param apiKey  Your Rynko API key
     * @param baseUrl Custom API base URL
     */
    public Rynko(String apiKey, String baseUrl) {
        this(new RynkoConfig(apiKey, baseUrl));
    }

    /**
     * Creates a new Rynko client with the specified configuration.
     *
     * @param config Client configuration
     */
    public Rynko(RynkoConfig config) {
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }

        this.httpClient = new HttpClient(config);
        this.documents = new DocumentsResource(httpClient);
        this.templates = new TemplatesResource(httpClient);
        this.webhooks = new WebhooksResource(httpClient);
    }

    /**
     * Returns the Documents resource for document generation operations.
     *
     * @return Documents resource
     */
    public DocumentsResource documents() {
        return documents;
    }

    /**
     * Returns the Templates resource for template operations.
     *
     * @return Templates resource
     */
    public TemplatesResource templates() {
        return templates;
    }

    /**
     * Returns the Webhooks resource for webhook operations.
     *
     * @return Webhooks resource
     */
    public WebhooksResource webhooks() {
        return webhooks;
    }

    /**
     * Gets the current authenticated user.
     *
     * @return The authenticated user
     * @throws RynkoException if the request fails
     */
    public User me() throws RynkoException {
        String authUrl = httpClient.getBaseUrlWithoutVersion() + "/api/auth/verify";
        return httpClient.getAbsolute(authUrl, User.class);
    }

    /**
     * Verifies if the API key is valid.
     *
     * @return true if the API key is valid, false otherwise
     */
    public boolean verifyApiKey() {
        try {
            me();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a new builder for Rynko configuration.
     *
     * @return Configuration builder
     */
    public static RynkoConfig.Builder builder() {
        return new RynkoConfig.Builder();
    }
}
