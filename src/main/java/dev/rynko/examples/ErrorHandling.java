package dev.rynko.examples;

import dev.rynko.Rynko;
import dev.rynko.exceptions.RynkoException;
import dev.rynko.models.GenerateRequest;

/**
 * Error Handling Example
 *
 * <p>This example shows how to handle errors from the Rynko API.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * export RYNKO_API_KEY=your_key
 * mvn exec:java -Dexec.mainClass="dev.rynko.examples.ErrorHandling"
 * </pre>
 */
public class ErrorHandling {

    public static void main(String[] args) {
        String apiKey = System.getenv("RYNKO_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("RYNKO_API_KEY environment variable is required");
            System.exit(1);
        }

        Rynko client = new Rynko(apiKey);

        // Example 1: Template not found
        System.out.println("\n--- Example 1: Template not found ---");
        try {
            client.templates().get("non-existent-template-id");
        } catch (RynkoException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Code: " + e.getCode());
            System.out.println("Status: " + e.getStatusCode());
        }

        // Example 2: Invalid API key
        System.out.println("\n--- Example 2: Invalid API key ---");
        Rynko badClient = new Rynko("invalid-key");
        try {
            badClient.me();
        } catch (RynkoException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Code: " + e.getCode());
        }

        // Example 3: Handling specific error codes
        System.out.println("\n--- Example 3: Handling specific error codes ---");
        try {
            client.documents().generate(
                GenerateRequest.builder()
                    .templateId("invalid-id")
                    .format("pdf")
                    .build()
            );
        } catch (RynkoException e) {
            String code = e.getCode();
            if (code == null) code = "";

            switch (code) {
                case "ERR_TMPL_001":
                    System.out.println("Template not found - check the template ID");
                    break;
                case "ERR_TMPL_003":
                    System.out.println("Template validation failed - check your variables");
                    break;
                case "ERR_QUOTA_001":
                    System.out.println("Quota exceeded - upgrade your plan");
                    break;
                case "ERR_AUTH_001":
                case "ERR_AUTH_004":
                    System.out.println("Authentication failed - check your API key");
                    break;
                default:
                    System.out.println("Unexpected error: " + e.getMessage());
            }
        }
    }
}
