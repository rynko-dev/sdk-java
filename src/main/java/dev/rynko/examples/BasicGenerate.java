package dev.rynko.examples;

import dev.rynko.Rynko;
import dev.rynko.models.GenerateRequest;
import dev.rynko.models.GenerateResult;
import dev.rynko.models.ListResponse;
import dev.rynko.models.Template;
import dev.rynko.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic Document Generation Example
 *
 * <p>This example shows how to generate a PDF document and wait for completion.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * export RYNKO_API_KEY=your_key
 * mvn exec:java -Dexec.mainClass="dev.rynko.examples.BasicGenerate"
 * </pre>
 */
public class BasicGenerate {

    public static void main(String[] args) {
        String apiKey = System.getenv("RYNKO_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("RYNKO_API_KEY environment variable is required");
            System.exit(1);
        }

        Rynko client = new Rynko(apiKey);

        try {
            // Verify authentication
            User user = client.me();
            System.out.println("Authenticated as: " + user.getEmail());

            // Get first available template
            ListResponse<Template> templates = client.templates().list(1, 1);
            if (templates.getData().isEmpty()) {
                System.err.println("No templates found. Create a template first.");
                System.exit(1);
            }

            Template template = templates.getData().get(0);
            System.out.println("Using template: " + template.getName() + " (" + template.getId() + ")");

            // Prepare metadata for tracking (optional)
            // Metadata is returned in job status and webhook payloads
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("orderId", "ord_12345");
            metadata.put("customerId", "cust_67890");
            metadata.put("priority", 1);

            // Queue document generation with metadata
            GenerateResult job = client.documents().generate(
                GenerateRequest.builder()
                    .templateId(template.getId())
                    .format("pdf")
                    .variable("title", "Example Document")
                    .variable("date", "2025-01-30")
                    .metadata(metadata)  // Attach metadata for tracking
                    .build()
            );

            System.out.println("Job queued: " + job.getJobId());
            System.out.println("Status: " + job.getStatus());

            // Wait for completion
            System.out.println("Waiting for completion...");
            GenerateResult completed = client.documents().waitForCompletion(
                job.getJobId(),
                1000,  // poll every 1 second
                60000  // timeout after 60 seconds
            );

            if ("completed".equals(completed.getStatus())) {
                System.out.println("Document generated successfully!");
                System.out.println("Download URL: " + completed.getDownloadUrl());

                // Access metadata from the completed job
                Map<String, Object> returnedMetadata = completed.getMetadata();
                if (returnedMetadata != null) {
                    System.out.println("Metadata: " + returnedMetadata);
                    System.out.println("Order ID: " + returnedMetadata.get("orderId"));
                }
            } else {
                System.err.println("Generation failed: " + completed.getErrorMessage());
                System.err.println("Error code: " + completed.getErrorCode());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
