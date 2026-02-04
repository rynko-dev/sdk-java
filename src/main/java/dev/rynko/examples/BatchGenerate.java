package dev.rynko.examples;

import dev.rynko.Rynko;
import dev.rynko.models.BatchDocumentSpec;
import dev.rynko.models.BatchStatusResult;
import dev.rynko.models.GenerateBatchRequest;
import dev.rynko.models.GenerateBatchResult;
import dev.rynko.models.ListResponse;
import dev.rynko.models.Template;

import java.util.Arrays;
import java.util.List;

/**
 * Batch Document Generation Example
 *
 * <p>This example shows how to generate multiple documents in a single batch
 * using the batch API. This is more efficient than making individual requests
 * when generating many documents from the same template.</p>
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

            // Build batch request
            GenerateBatchRequest.Builder requestBuilder = GenerateBatchRequest.builder()
                .templateId(template.getId())
                .format("pdf");

            // Add each invoice as a document in the batch
            for (InvoiceData invoice : invoices) {
                requestBuilder.addDocument(
                    BatchDocumentSpec.builder()
                        .variable("invoiceNumber", invoice.invoiceNumber)
                        .variable("customerName", invoice.customerName)
                        .variable("total", invoice.total)
                        .filename("invoice-" + invoice.invoiceNumber.toLowerCase())
                        .metadata("orderId", invoice.orderId)
                        .metadata("customerId", invoice.customerId)
                        .build()
                );
            }

            GenerateBatchRequest request = requestBuilder.build();

            // Submit the batch
            System.out.println("Submitting batch of " + invoices.size() + " documents...");
            GenerateBatchResult result = client.documents().generateBatch(request);

            System.out.println("Batch submitted!");
            System.out.println("  Batch ID: " + result.getBatchId());
            System.out.println("  Total jobs: " + result.getTotalJobs());
            System.out.println("  Status URL: " + result.getStatusUrl());

            // Optionally wait for completion (for demo purposes)
            System.out.println("\nWaiting for batch to complete...");
            BatchStatusResult status = client.documents().waitForBatchCompletion(result.getBatchId());

            System.out.println("\nBatch finished!");
            System.out.println("  Status: " + status.getStatus());
            System.out.println("  Completed: " + status.getCompletedJobs() + "/" + status.getTotalJobs());
            if (status.getFailedJobs() > 0) {
                System.out.println("  Failed: " + status.getFailedJobs());
            }

            // In production, you would typically:
            // 1. Submit the batch and return immediately
            // 2. Set up a webhook to receive notifications as each document completes
            // 3. Use the metadata (orderId, customerId) to correlate completed documents

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
