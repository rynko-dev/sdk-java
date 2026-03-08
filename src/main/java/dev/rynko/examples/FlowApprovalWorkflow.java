package dev.rynko.examples;

import dev.rynko.Rynko;
import dev.rynko.models.FlowApproval;
import dev.rynko.models.FlowRun;
import dev.rynko.models.ListResponse;

import java.util.List;

/**
 * Flow Approval Workflow Example
 *
 * <p>This example shows how to list pending approvals, review run details,
 * and approve or reject them programmatically.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * export RYNKO_API_KEY=your_key
 * mvn exec:java -Dexec.mainClass="dev.rynko.examples.FlowApprovalWorkflow"
 * </pre>
 */
public class FlowApprovalWorkflow {

    public static void main(String[] args) {
        String apiKey = System.getenv("RYNKO_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("RYNKO_API_KEY environment variable is required");
            System.exit(1);
        }

        Rynko client = new Rynko(apiKey);

        try {
            // List pending approvals
            System.out.println("Listing pending approvals...\n");
            ListResponse<FlowApproval> response = client.flow().listApprovals(null, null, "pending");
            List<FlowApproval> approvals = response.getData();

            if (approvals.isEmpty()) {
                System.out.println("No pending approvals found.");
                System.out.println("Submit a run to a gate with human review enabled to see approvals.");
                System.exit(0);
            }

            System.out.println("Found " + approvals.size() + " pending approval(s):\n");

            for (FlowApproval approval : approvals) {
                System.out.println("--- Approval: " + approval.getId() + " ---");
                System.out.println("  Run ID: " + approval.getRunId());
                System.out.println("  Gate ID: " + approval.getGateId());
                System.out.println("  Status: " + approval.getStatus());
                System.out.println("  Created: " + approval.getCreatedAt());

                // Get the run details for context
                FlowRun run = client.flow().getRun(approval.getRunId());
                System.out.println("  Run Status: " + run.getStatus());
                if (run.getInput() != null) {
                    System.out.println("  Run Input: " + run.getInput());
                }
                if (run.getErrors() != null && !run.getErrors().isEmpty()) {
                    System.out.println("  Validation Errors:");
                    for (FlowRun.FlowValidationError error : run.getErrors()) {
                        System.out.println("    - " + error.getField() + ": " + error.getMessage());
                    }
                }
                System.out.println();
            }

            // Process the first pending approval as a demo
            FlowApproval firstApproval = approvals.get(0);
            FlowRun run = client.flow().getRun(firstApproval.getRunId());

            // Decision logic: approve if no validation errors, reject otherwise
            boolean hasErrors = run.getErrors() != null && !run.getErrors().isEmpty();

            if (hasErrors) {
                // Reject with a reason
                System.out.println("Rejecting approval " + firstApproval.getId() + " (has validation errors)...");
                FlowApproval rejected = client.flow().reject(
                    firstApproval.getId(),
                    "Rejected: validation errors found in run input"
                );
                System.out.println("Result: " + rejected.getStatus());
            } else {
                // Approve with a note
                System.out.println("Approving approval " + firstApproval.getId() + " (no validation errors)...");
                FlowApproval approved = client.flow().approve(
                    firstApproval.getId(),
                    "Approved via automated review - no validation errors"
                );
                System.out.println("Result: " + approved.getStatus());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
