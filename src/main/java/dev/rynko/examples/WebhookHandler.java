package dev.rynko.examples;

import dev.rynko.exceptions.WebhookSignatureException;
import dev.rynko.resources.WebhooksResource;

import java.util.Map;

/**
 * Webhook Handler Example
 *
 * <p>This example shows how to verify and handle Rynko webhook events.</p>
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
 *                 case "document.generated":
 *                     handleDocumentGenerated(event);
 *                     break;
 *                 case "document.failed":
 *                     handleDocumentFailed(event);
 *                     break;
 *                 case "document.downloaded":
 *                     handleDocumentDownloaded(event);
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
 *     private void handleDocumentGenerated(WebhooksResource.WebhookEvent event) {
 *         Map<String, Object> data = event.getData();
 *         String jobId = (String) data.get("jobId");
 *         String downloadUrl = (String) data.get("downloadUrl");
 *         System.out.println("Document ready: " + downloadUrl);
 *     }
 *
 *     private void handleDocumentFailed(WebhooksResource.WebhookEvent event) {
 *         Map<String, Object> data = event.getData();
 *         String jobId = (String) data.get("jobId");
 *         String error = (String) data.get("error");
 *         System.err.println("Document failed: " + error);
 *     }
 *
 *     private void handleDocumentDownloaded(WebhooksResource.WebhookEvent event) {
 *         Map<String, Object> data = event.getData();
 *         String jobId = (String) data.get("jobId");
 *         System.out.println("Document downloaded: " + jobId);
 *     }
 * }
 * }</pre>
 */
public class WebhookHandler {

    public static void main(String[] args) {
        System.out.println("This is a documentation example.");
        System.out.println("See the Javadoc above for a complete Spring Boot webhook handler.");
        System.out.println();
        System.out.println("For a runnable example, see:");
        System.out.println("https://github.com/rynko-dev/developer-resources");
    }
}
