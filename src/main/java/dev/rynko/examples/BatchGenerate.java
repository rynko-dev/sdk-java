package dev.rynko.examples;

import dev.rynko.Rynko;
import dev.rynko.models.GenerateRequest;
import dev.rynko.models.GenerateResult;
import dev.rynko.models.ListResponse;
import dev.rynko.models.Template;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Batch Document Generation Example
 *
 * <p>This example shows how to generate multiple documents in a single request.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * export RYNKO_API_KEY=your_key
 * mvn exec:java -Dexec.mainClass="dev.rynko.examples.BatchGenerate"
 * </pre>
 */
public class BatchGenerate {

    public static void main(String[] args) {
        String apiKey = System.getenv("RYNKO_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("RYNKO_API_KEY environment variable is required");
            System.exit(1);
        }

        Rynko client = new Rynko(apiKey);

        try {
            // Get first available template
            ListResponse<Template> templates = client.templates().list(1, 1);
            if (templates.getData().isEmpty()) {
                System.err.println("No templates found. Create a template first.");
                System.exit(1);
            }

            Template template = templates.getData().get(0);
            System.out.println("Using template: " + template.getName());

            // Prepare batch documents
            List<Map<String, Object>> documents = Arrays.asList(
                createInvoice("INV-001", "Alice", 150.00),
                createInvoice("INV-002", "Bob", 275.50),
                createInvoice("INV-003", "Charlie", 89.99)
            );

            // Generate multiple documents
            // Note: The SDK may need a generateBatch method - this is a conceptual example
            System.out.println("Generating " + documents.size() + " documents...");

            for (Map<String, Object> docVars : documents) {
                GenerateResult job = client.documents().generate(
                    GenerateRequest.builder()
                        .templateId(template.getId())
                        .format("pdf")
                        .variables(docVars)
                        .build()
                );
                System.out.println("Queued job: " + job.getJobId() + " for " + docVars.get("customerName"));
            }

            System.out.println("All documents queued!");
            System.out.println("Use webhooks to get notified when each completes.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Map<String, Object> createInvoice(String invoiceNumber, String customerName, double total) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("invoiceNumber", invoiceNumber);
        vars.put("customerName", customerName);
        vars.put("total", total);
        return vars;
    }
}
