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
 * <p>This example shows how to generate multiple documents with metadata
 * for tracking purposes. Metadata is returned in webhook payloads.</p>
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

            // Prepare batch documents with metadata for tracking
            List<InvoiceData> invoices = Arrays.asList(
                new InvoiceData("INV-001", "Alice", 150.00, "ord_001", "cust_alice"),
                new InvoiceData("INV-002", "Bob", 275.50, "ord_002", "cust_bob"),
                new InvoiceData("INV-003", "Charlie", 89.99, "ord_003", "cust_charlie")
            );

            // Generate multiple documents with metadata
            System.out.println("Generating " + invoices.size() + " documents with metadata...");

            for (InvoiceData invoice : invoices) {
                // Create metadata to track this document
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("orderId", invoice.orderId);
                metadata.put("customerId", invoice.customerId);
                metadata.put("invoiceNumber", invoice.invoiceNumber);

                GenerateResult job = client.documents().generate(
                    GenerateRequest.builder()
                        .templateId(template.getId())
                        .format("pdf")
                        .variable("invoiceNumber", invoice.invoiceNumber)
                        .variable("customerName", invoice.customerName)
                        .variable("total", invoice.total)
                        .metadata(metadata)  // Attach metadata for tracking
                        .build()
                );
                System.out.println("Queued job: " + job.getJobId() + " for " + invoice.customerName);
                System.out.println("  Metadata: orderId=" + invoice.orderId);
            }

            System.out.println();
            System.out.println("All documents queued!");
            System.out.println("Metadata will be returned in webhook payloads when each document completes.");
            System.out.println("Use metadata to correlate completed documents with your orders.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper class to hold invoice data with tracking IDs.
     */
    private static class InvoiceData {
        final String invoiceNumber;
        final String customerName;
        final double total;
        final String orderId;
        final String customerId;

        InvoiceData(String invoiceNumber, String customerName, double total, String orderId, String customerId) {
            this.invoiceNumber = invoiceNumber;
            this.customerName = customerName;
            this.total = total;
            this.orderId = orderId;
            this.customerId = customerId;
        }
    }
}
