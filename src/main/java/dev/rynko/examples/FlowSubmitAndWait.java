package dev.rynko.examples;

import dev.rynko.Rynko;
import dev.rynko.models.FlowGate;
import dev.rynko.models.FlowRun;
import dev.rynko.models.ListResponse;
import dev.rynko.models.SubmitRunRequest;

import java.util.List;

/**
 * Flow Submit and Wait Example
 *
 * <p>This example shows how to submit a run to a Flow gate and wait for
 * the validation result.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * export RYNKO_API_KEY=your_key
 * mvn exec:java -Dexec.mainClass="dev.rynko.examples.FlowSubmitAndWait"
 * </pre>
 */
public class FlowSubmitAndWait {

    public static void main(String[] args) {
        String apiKey = System.getenv("RYNKO_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("RYNKO_API_KEY environment variable is required");
            System.exit(1);
        }

        Rynko client = new Rynko(apiKey);

        try {
            // List gates and pick the first published one
            System.out.println("Listing Flow gates...");
            ListResponse<FlowGate> gatesResponse = client.flow().listGates();
            List<FlowGate> gates = gatesResponse.getData();

            if (gates.isEmpty()) {
                System.err.println("No gates found. Create a gate in the Rynko dashboard first.");
                System.exit(1);
            }

            FlowGate gate = null;
            for (FlowGate g : gates) {
                if ("published".equals(g.getStatus())) {
                    gate = g;
                    break;
                }
            }

            if (gate == null) {
                System.err.println("No published gates found. Publish a gate in the Rynko dashboard first.");
                System.exit(1);
            }

            System.out.println("Using gate: " + gate.getName() + " (" + gate.getId() + ")");

            // Submit a run with input fields
            System.out.println("\nSubmitting run for validation...");
            FlowRun run = client.flow().submitRun(gate.getId(),
                SubmitRunRequest.builder()
                    .inputField("name", "John Doe")
                    .inputField("email", "john@example.com")
                    .inputField("amount", 150.00)
                    .inputField("currency", "USD")
                    .build()
            );

            System.out.println("Run submitted: " + run.getId());
            System.out.println("Initial status: " + run.getStatus());

            // Wait for the run to reach a terminal state
            System.out.println("\nWaiting for validation result...");
            FlowRun completed = client.flow().waitForRun(
                run.getId(),
                1000,   // poll every 1 second
                60000   // timeout after 60 seconds
            );

            System.out.println("\nFinal status: " + completed.getStatus());

            // Handle different outcomes
            switch (completed.getStatus()) {
                case "approved":
                    System.out.println("Run approved! Output is valid.");
                    if (completed.getOutput() != null) {
                        System.out.println("Output: " + completed.getOutput());
                    }
                    break;

                case "rejected":
                    System.out.println("Run rejected. Check validation errors.");
                    if (completed.getErrors() != null) {
                        for (FlowRun.FlowValidationError error : completed.getErrors()) {
                            System.out.println("  - " + error.getField() + ": " + error.getMessage());
                        }
                    }
                    break;

                case "validation_failed":
                    System.out.println("Validation failed. Input did not pass schema or rules.");
                    if (completed.getErrors() != null) {
                        for (FlowRun.FlowValidationError error : completed.getErrors()) {
                            System.out.println("  - " + error.getField() + ": " + error.getMessage());
                        }
                    }
                    break;

                case "delivered":
                    System.out.println("Run delivered successfully via webhook.");
                    break;

                case "delivery_failed":
                    System.out.println("Delivery failed. The webhook endpoint may be unreachable.");
                    break;

                case "render_failed":
                    System.out.println("Render failed during document generation.");
                    break;

                case "completed":
                    System.out.println("Run completed.");
                    if (completed.getOutput() != null) {
                        System.out.println("Output: " + completed.getOutput());
                    }
                    break;

                default:
                    System.out.println("Unexpected status: " + completed.getStatus());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
