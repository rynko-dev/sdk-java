package dev.rynko.examples;

import dev.rynko.exceptions.WebhookSignatureException;
import dev.rynko.resources.WebhooksResource;
import dev.rynko.resources.WebhooksResource.DocumentWebhookData;
import dev.rynko.resources.WebhooksResource.BatchWebhookData;

import java.util.Map;

/**
 * Webhook Handler Example
 *
 * <p>This example shows how to verify and handle Rynko webhook events,
 * including accessing custom metadata attached to documents.</p>
 *
 * <p>For a complete Spring Boot implementation, see the developer-resources repository.</p>
 *
 * <h2>Spring Boot Controller Example:</h2>
 * <pre>{@code
 * @RestController
 * public class RynkoWebhookController {
 *
 *     @Value("${rynko.webhook-secret}")
 *     private String webhookSecret;
 *
 *     private final Rynko rynko;
 *
 *     public RynkoWebhookController(Rynko rynko) {
 *         this.rynko = rynko;
 *     }
 *
 *     @PostMapping("/webhooks/rynko")
 *     public ResponseEntity<Map<String, Boolean>> handleWebhook(
 *             @RequestBody String payload,
 *             @RequestHeader("X-Rynko-Signature") String signature,
 *             @RequestHeader("X-Rynko-Timestamp") String timestamp) {
 *
 *         try {
 *             WebhooksResource.WebhookEvent event = rynko.webhooks().constructEvent(
 *                 payload,
 *                 signature,
 *                 timestamp,
 *                 webhookSecret
 *             );
 *
 *             switch (event.getType()) {
 *                 case "document.completed":
 *                     handleDocumentCompleted(event);
 *                     break;
 *                 case "document.failed":
 *                     handleDocumentFailed(event);
 *                     break;
 *                 case "batch.completed":
 *                     handleBatchCompleted(event);
 *                     break;
 *             }
 *
 *             return ResponseEntity.ok(Map.of("received", true));
 *
 *         } catch (WebhookSignatureException e) {
 *             return ResponseEntity.status(401).body(Map.of("received", false));
 *         }
 *     }
 *
 *     private void handleDocumentCompleted(WebhooksResource.WebhookEvent event) {
 *         // Use typed accessor for document events
 *         DocumentWebhookData data = event.getDocumentData();
 *
 *         System.out.println("Document ready!");
 *         System.out.println("Job ID: " + data.getJobId());
 *         System.out.println("Download URL: " + data.getDownloadUrl());
 *
 *         // Access custom metadata passed in the generate request
 *         Map<String, Object> metadata = data.getMetadata();
 *         if (metadata != null) {
 *             String orderId = (String) metadata.get("orderId");
 *             String customerId = (String) metadata.get("customerId");
 *             System.out.println("Order: " + orderId + ", Customer: " + customerId);
 *
 *             // Use metadata to update your database, send notifications, etc.
 *         }
 *     }
 *
 *     private void handleDocumentFailed(WebhooksResource.WebhookEvent event) {
 *         DocumentWebhookData data = event.getDocumentData();
 *
 *         System.err.println("Document generation failed!");
 *         System.err.println("Job ID: " + data.getJobId());
 *         System.err.println("Error: " + data.getErrorMessage());
 *         System.err.println("Error Code: " + data.getErrorCode());
 *
 *         // Access metadata to identify which order/customer failed
 *         Map<String, Object> metadata = data.getMetadata();
 *         if (metadata != null) {
 *             System.err.println("Failed for order: " + metadata.get("orderId"));
 *         }
 *     }
 *
 *     private void handleBatchCompleted(WebhooksResource.WebhookEvent event) {
 *         // Use typed accessor for batch events
 *         BatchWebhookData data = event.getBatchData();
 *
 *         System.out.println("Batch completed!");
 *         System.out.println("Batch ID: " + data.getBatchId());
 *         System.out.println("Total: " + data.getTotalJobs());
 *         System.out.println("Completed: " + data.getCompletedJobs());
 *         System.out.println("Failed: " + data.getFailedJobs());
 *
 *         // Access batch-level metadata
 *         Map<String, Object> metadata = data.getMetadata();
 *         if (metadata != null) {
 *             System.out.println("Batch run ID: " + metadata.get("batchRunId"));
 *         }
 *     }
 * }
 * }</pre>
 */
public class WebhookHandler {

    public static void main(String[] args) {
        System.out.println("This is a documentation example.");
        System.out.println("See the Javadoc above for a complete Spring Boot webhook handler.");
        System.out.println();
        System.out.println("Webhook event types:");
        System.out.println("  - document.completed: Document was successfully generated");
        System.out.println("  - document.failed: Document generation failed");
        System.out.println("  - batch.completed: Batch of documents completed");
        System.out.println();
        System.out.println("For a runnable example, see:");
        System.out.println("https://github.com/rynko-dev/developer-resources");
    }
}
