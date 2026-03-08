package dev.rynko;

import dev.rynko.exceptions.RynkoException;
import dev.rynko.models.*;

import java.util.*;

/**
 * Rynko Java SDK Flow Integration Tests
 *
 * Run these tests against a live API to verify Flow SDK functionality.
 *
 * Prerequisites:
 * 1. Set RYNKO_API_KEY environment variable
 * 2. Set RYNKO_API_URL environment variable (optional, defaults to https://api.rynko.dev)
 * 3. Have at least one published Flow gate in your environment
 *
 * Usage:
 *   RYNKO_API_KEY=your_key mvn test -Dtest=FlowIntegrationTest
 *
 * Or run directly:
 *   RYNKO_API_KEY=your_key java -cp target/test-classes:target/classes dev.rynko.FlowIntegrationTest
 */
public class FlowIntegrationTest {

    private static final String API_KEY = System.getenv("RYNKO_API_KEY");
    private static final String API_URL = System.getenv().getOrDefault("RYNKO_API_URL", "https://api.rynko.dev");

    private static Rynko client;
    private static String gateId = null;
    private static String runId = null;
    private static String approvalId = null;
    private static String deliveryId = null;

    private static final List<TestResult> results = new ArrayList<>();

    public static void main(String[] args) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("❌ RYNKO_API_KEY environment variable is required");
            System.exit(1);
        }

        client = new Rynko(API_KEY, API_URL + "/api/v1");

        System.out.println("\n🧪 Rynko Java SDK Flow Integration Tests\n");
        System.out.println("API URL: " + API_URL);
        System.out.println("API Key: " + API_KEY.substring(0, Math.min(10, API_KEY.length())) + "...");
        System.out.println("\n---\n");

        runTests();
        printSummary();
    }

    private static void runTests() {
        // ==========================================
        // Gates Tests
        // ==========================================

        test("flow().listGates() - List all gates", () -> {
            ListResponse<FlowGate> response = client.flow().listGates();
            List<FlowGate> gates = response.getData();
            if (gates == null) {
                throw new Exception("Expected array of gates");
            }
            System.out.println("  Found " + gates.size() + " gates");

            // Save first published gate ID for later tests
            for (FlowGate gate : gates) {
                if ("published".equals(gate.getStatus())) {
                    gateId = gate.getId();
                    System.out.println("  Using gate: " + gate.getName() + " (" + gateId + ")");
                    break;
                }
            }

            if (gateId == null && !gates.isEmpty()) {
                gateId = gates.get(0).getId();
                System.out.println("  Using gate (non-published): " + gates.get(0).getName() + " (" + gateId + ")");
            }
        });

        if (gateId != null) {
            test("flow().getGate(gateId) - Get gate by ID", () -> {
                FlowGate gate = client.flow().getGate(gateId);
                if (gate.getId() == null || gate.getName() == null) {
                    throw new Exception("Invalid gate response");
                }
                System.out.println("  Gate: " + gate.getName());
                System.out.println("  Status: " + gate.getStatus());
                System.out.println("  Schema Version: " + gate.getSchemaVersion());
            });
        }

        // ==========================================
        // Runs Tests
        // ==========================================

        if (gateId != null) {
            test("flow().submitRun(gateId, request) - Submit a run", () -> {
                FlowRun run = client.flow().submitRun(gateId,
                    SubmitRunRequest.builder()
                        .inputField("name", "Integration Test")
                        .inputField("amount", 99.99)
                        .inputField("email", "test@example.com")
                        .metadata(Collections.singletonMap("source", "java-sdk-integration-test"))
                        .build()
                );

                if (run.getId() == null) {
                    throw new Exception("Invalid run response - missing ID");
                }

                runId = run.getId();
                System.out.println("  Run ID: " + runId);
                System.out.println("  Status: " + run.getStatus());
            });
        }

        if (runId != null) {
            test("flow().getRun(runId) - Get run by ID", () -> {
                FlowRun run = client.flow().getRun(runId);
                if (run.getId() == null || run.getGateId() == null) {
                    throw new Exception("Invalid run response");
                }
                System.out.println("  Run: " + run.getId());
                System.out.println("  Status: " + run.getStatus());
                System.out.println("  Gate ID: " + run.getGateId());
            });
        }

        test("flow().listRuns() - List all runs", () -> {
            ListResponse<FlowRun> response = client.flow().listRuns();
            List<FlowRun> runs = response.getData();
            if (runs == null) {
                throw new Exception("Expected array of runs");
            }
            System.out.println("  Found " + runs.size() + " runs");
            System.out.println("  Total: " + response.getMeta().getTotal());
        });

        if (gateId != null) {
            test("flow().listRunsByGate(gateId) - List runs by gate", () -> {
                ListResponse<FlowRun> response = client.flow().listRunsByGate(gateId);
                List<FlowRun> runs = response.getData();
                if (runs == null) {
                    throw new Exception("Expected array of runs");
                }
                System.out.println("  Found " + runs.size() + " runs for gate " + gateId);
            });
        }

        test("flow().listActiveRuns() - List active runs", () -> {
            ListResponse<FlowRun> response = client.flow().listActiveRuns();
            List<FlowRun> runs = response.getData();
            if (runs == null) {
                throw new Exception("Expected array of runs");
            }
            System.out.println("  Found " + runs.size() + " active runs");
        });

        if (runId != null) {
            test("flow().waitForRun(runId) - Wait for run completion", () -> {
                FlowRun completed = client.flow().waitForRun(runId, 1000, 60000);

                if (!completed.isTerminal()) {
                    throw new Exception("Run not in terminal state: " + completed.getStatus());
                }

                System.out.println("  Final status: " + completed.getStatus());
                if (completed.getErrors() != null && !completed.getErrors().isEmpty()) {
                    System.out.println("  Errors: " + completed.getErrors().size());
                    for (FlowRun.FlowValidationError error : completed.getErrors()) {
                        System.out.println("    - " + error.getField() + ": " + error.getMessage());
                    }
                }
            });
        }

        // ==========================================
        // Approvals Tests
        // ==========================================

        test("flow().listApprovals() - List approvals", () -> {
            ListResponse<FlowApproval> response = client.flow().listApprovals();
            List<FlowApproval> approvals = response.getData();
            if (approvals == null) {
                throw new Exception("Expected array of approvals");
            }
            System.out.println("  Found " + approvals.size() + " approvals");
            System.out.println("  Total: " + response.getMeta().getTotal());

            // Find a pending approval for subsequent tests
            for (FlowApproval approval : approvals) {
                if ("pending".equals(approval.getStatus())) {
                    approvalId = approval.getId();
                    System.out.println("  Found pending approval: " + approvalId);
                    break;
                }
            }
        });

        test("flow().approve(approvalId) - Approve a pending approval", () -> {
            if (approvalId == null) {
                System.out.println("  Skipped - no pending approvals found");
                return;
            }

            FlowApproval approval = client.flow().approve(approvalId, "Approved via Java SDK integration test");
            if (approval.getId() == null) {
                throw new Exception("Invalid approval response");
            }
            System.out.println("  Approved: " + approval.getId());
            System.out.println("  Status: " + approval.getStatus());

            // Reset approvalId since it's been used
            approvalId = null;
        });

        test("flow().reject(approvalId) - Reject a pending approval", () -> {
            // Try to find another pending approval
            ListResponse<FlowApproval> response = client.flow().listApprovals(null, null, "pending");
            String pendingId = null;
            if (response.getData() != null && !response.getData().isEmpty()) {
                pendingId = response.getData().get(0).getId();
            }

            if (pendingId == null) {
                System.out.println("  Skipped - no pending approvals found");
                return;
            }

            FlowApproval approval = client.flow().reject(pendingId, "Rejected via Java SDK integration test");
            if (approval.getId() == null) {
                throw new Exception("Invalid approval response");
            }
            System.out.println("  Rejected: " + approval.getId());
            System.out.println("  Status: " + approval.getStatus());
        });

        // ==========================================
        // Deliveries Tests
        // ==========================================

        if (runId != null) {
            test("flow().listDeliveries(runId) - List deliveries for a run", () -> {
                ListResponse<FlowDelivery> response = client.flow().listDeliveries(runId);
                List<FlowDelivery> deliveries = response.getData();
                if (deliveries == null) {
                    throw new Exception("Expected array of deliveries");
                }
                System.out.println("  Found " + deliveries.size() + " deliveries for run " + runId);
                System.out.println("  Total: " + response.getMeta().getTotal());

                // Find a failed delivery for retry test
                for (FlowDelivery delivery : deliveries) {
                    if ("failed".equals(delivery.getStatus())) {
                        deliveryId = delivery.getId();
                        System.out.println("  Found failed delivery: " + deliveryId);
                        break;
                    }
                }
            });
        }

        test("flow().retryDelivery(deliveryId) - Retry a failed delivery", () -> {
            if (deliveryId == null) {
                System.out.println("  Skipped - no failed deliveries found");
                return;
            }

            FlowDelivery delivery = client.flow().retryDelivery(deliveryId);
            if (delivery.getId() == null) {
                throw new Exception("Invalid delivery response");
            }
            System.out.println("  Retried: " + delivery.getId());
            System.out.println("  Status: " + delivery.getStatus());
        });

        // ==========================================
        // Error Handling Tests
        // ==========================================

        test("Error handling - Invalid run ID", () -> {
            try {
                client.flow().getRun("invalid-run-id-12345");
                throw new Exception("Expected error for invalid run ID");
            } catch (RynkoException e) {
                System.out.println("  Error code: " + e.getCode());
                System.out.println("  Status: " + e.getStatusCode());
                // Test passed
            }
        });
    }

    private static void test(String name, TestAction action) {
        try {
            action.run();
            results.add(new TestResult(name, true, null));
            System.out.println("✅ " + name);
        } catch (Exception e) {
            results.add(new TestResult(name, false, e.getMessage()));
            System.out.println("❌ " + name + ": " + e.getMessage());
        }
    }

    private static void printSummary() {
        System.out.println("\n---\n");
        System.out.println("📊 Test Results Summary\n");

        long passed = results.stream().filter(r -> r.passed).count();
        long failed = results.stream().filter(r -> !r.passed).count();

        System.out.println("Total: " + results.size());
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0) {
            System.out.println("\n❌ Failed Tests:");
            results.stream()
                .filter(r -> !r.passed)
                .forEach(r -> System.out.println("  - " + r.name + ": " + r.error));
            System.exit(1);
        } else {
            System.out.println("\n✅ All tests passed!");
        }
    }

    @FunctionalInterface
    interface TestAction {
        void run() throws Exception;
    }

    static class TestResult {
        final String name;
        final boolean passed;
        final String error;

        TestResult(String name, boolean passed, String error) {
            this.name = name;
            this.passed = passed;
            this.error = error;
        }
    }
}
