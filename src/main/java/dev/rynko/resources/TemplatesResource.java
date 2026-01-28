package dev.rynko.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.rynko.exceptions.RynkoException;
import dev.rynko.models.ListResponse;
import dev.rynko.models.PaginationMeta;
import dev.rynko.models.Template;
import dev.rynko.utils.HttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resource for template operations.
 *
 * <p>Use this resource to list and retrieve templates.</p>
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * // List all templates
 * ListResponse<Template> templates = client.templates().list();
 * for (Template template : templates.getData()) {
 *     System.out.println(template.getName() + ": " + template.getId());
 * }
 *
 * // List only PDF templates
 * ListResponse<Template> pdfTemplates = client.templates().listPdf();
 *
 * // Get a specific template
 * Template template = client.templates().get("tmpl_invoice");
 * System.out.println("Variables: " + template.getVariables().size());
 * }</pre>
 *
 * @since 1.0.0
 */
public class TemplatesResource {

    private final HttpClient httpClient;

    public TemplatesResource(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Lists all templates.
     *
     * @return Paginated list of templates
     * @throws RynkoException if the request fails
     */
    public ListResponse<Template> list() throws RynkoException {
        return list(null, null, null);
    }

    /**
     * Lists templates with pagination.
     *
     * @param page  Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated list of templates
     * @throws RynkoException if the request fails
     */
    public ListResponse<Template> list(Integer page, Integer limit) throws RynkoException {
        return list(page, limit, null);
    }

    /**
     * Lists templates with pagination and search.
     *
     * @param page   Page number (1-based)
     * @param limit  Number of items per page
     * @param search Search by template name
     * @return Paginated list of templates
     * @throws RynkoException if the request fails
     */
    public ListResponse<Template> list(Integer page, Integer limit, String search) throws RynkoException {
        Map<String, String> params = new HashMap<>();
        if (page != null) {
            params.put("page", page.toString());
        }
        if (limit != null) {
            params.put("limit", limit.toString());
        }
        if (search != null) {
            params.put("search", search);
        }

        // Templates use non-versioned API: /api/templates/attachment
        String url = httpClient.getBaseUrlWithoutVersion() + "/api/templates/attachment";
        return httpClient.getAbsolute(url, params, new TypeReference<ListResponse<Template>>() {});
    }

    /**
     * Lists PDF templates (client-side filter by outputFormats).
     *
     * @return Paginated list of PDF templates
     * @throws RynkoException if the request fails
     */
    public ListResponse<Template> listPdf() throws RynkoException {
        return listPdf(null, null);
    }

    /**
     * Lists PDF templates with pagination (client-side filter by outputFormats).
     *
     * @param page  Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated list of PDF templates
     * @throws RynkoException if the request fails
     */
    public ListResponse<Template> listPdf(Integer page, Integer limit) throws RynkoException {
        ListResponse<Template> result = list(page, limit);
        if (result.getData() != null) {
            List<Template> filtered = result.getData().stream()
                    .filter(t -> t.getOutputFormats() != null && t.getOutputFormats().contains("pdf"))
                    .collect(Collectors.toList());
            result.setData(filtered);
        }
        return result;
    }

    /**
     * Lists Excel templates (client-side filter by outputFormats).
     *
     * @return Paginated list of Excel templates
     * @throws RynkoException if the request fails
     */
    public ListResponse<Template> listExcel() throws RynkoException {
        return listExcel(null, null);
    }

    /**
     * Lists Excel templates with pagination (client-side filter by outputFormats).
     *
     * @param page  Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated list of Excel templates
     * @throws RynkoException if the request fails
     */
    public ListResponse<Template> listExcel(Integer page, Integer limit) throws RynkoException {
        ListResponse<Template> result = list(page, limit);
        if (result.getData() != null) {
            List<Template> filtered = result.getData().stream()
                    .filter(t -> t.getOutputFormats() != null &&
                            (t.getOutputFormats().contains("xlsx") || t.getOutputFormats().contains("excel")))
                    .collect(Collectors.toList());
            result.setData(filtered);
        }
        return result;
    }

    /**
     * Gets a template by ID.
     *
     * @param templateId The template ID (UUID, shortId, or slug)
     * @return The template
     * @throws RynkoException if the request fails
     */
    public Template get(String templateId) throws RynkoException {
        // Templates use non-versioned API: /api/templates/{id}
        String url = httpClient.getBaseUrlWithoutVersion() + "/api/templates/" + templateId;
        return httpClient.getAbsolute(url, Template.class);
    }

    /**
     * Gets a template by short ID.
     *
     * @param shortId The template short ID (e.g., "tmpl_abc123")
     * @return The template
     * @throws RynkoException if the request fails
     */
    public Template getByShortId(String shortId) throws RynkoException {
        return get(shortId);
    }

    /**
     * Gets a template by slug.
     *
     * @param slug The template slug (e.g., "invoice-template")
     * @return The template
     * @throws RynkoException if the request fails
     */
    public Template getBySlug(String slug) throws RynkoException {
        return get(slug);
    }
}
