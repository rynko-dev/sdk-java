package dev.rynko;

import dev.rynko.exceptions.RynkoException;
import dev.rynko.models.*;
import dev.rynko.resources.WebhooksResource.WebhookSubscription;

import java.util.*;

/**
 * Rynko Java SDK Integration Tests
 *
 * Run these tests against a live API to verify SDK functionality.
 *
 * Prerequisites:
 * 1. Set RYNKO_API_KEY environment variable
 * 2. Set RYNKO_API_URL environment variable (optional, defaults to https://api.rynko.dev)
 * 3. Have at least one template created in your workspace
 *
 * Usage:
 *   RYNKO_API_KEY=your_key mvn test -Dtest=IntegrationTest
 *
 * Or run directly:
 *   RYNKO_API_KEY=your_key java -cp target/test-classes:target/classes com.rynko.IntegrationTest
 */
public class IntegrationTest {

    private static final String API_KEY = System.getenv("RYNKO_API_KEY");
    private static final String API_URL = System.getenv().getOrDefault("RYNKO_API_URL", "https://api.rynko.dev");

    private static Rynko client;
    private static String templateId = null;
    private static Map<String, Object> templateVariables = new HashMap<>();
    private static String jobId = null;

    private static final List<TestResult> results = new ArrayList<>();

    /**
     * Build variables map from template variable definitions using default values
     */
    private static Map<String, Object> buildVariablesFromDefaults(List<TemplateVariable> variables) {
        Map<String, Object> result = new HashMap<>();

        if (variables == null) {
            return result;
        }

        for (TemplateVariable variable : variables) {
            if (variable.getName() != null && variable.getDefaultValue() != null) {
                result.put(variable.getName(), variable.getDefaultValue());
            }
        }

        return result;
    }

    public static void main(String[] args) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("❌ RYNKO_API_KEY environment variable is required");
            System.exit(1);
        }

        client = new Rynko(API_KEY, API_URL + "/api/v1");

        System.out.println("\n🧪 Rynko Java SDK Integration Tests\n");
        System.out.println("API URL: " + API_URL);
        System.out.println("API Key: " + API_KEY.substring(0, Math.min(10, API_KEY.length())) + "...");
        System.out.println("\n---\n");

        runTests();
        printSummary();
    }

    private static void runTests() {
        // ==========================================
        // Client Tests
        // ==========================================

        test("client.me() - Get authenticated user", () -> {
            com.rynko.models.User user = client.me();
            if (user.getId() == null || user.getEmail() == null) {
                throw new Exception("Invalid user response");
            }
            System.out.println("  User: " + user.getEmail());
        });

        test("client.verifyApiKey() - Verify API key is valid", () -> {
            boolean result = client.verifyApiKey();
            if (!result) {
                throw new Exception("API key verification failed");
            }
        });

        // ==========================================
        // Templates Tests
        // ==========================================

        test("templates.list() - List all templates", () -> {
            ListResponse<Template> response = client.templates().list();
            List<Template> templates = response.getData();
            if (templates == null) {
                throw new Exception("Expected array of templates");
            }
            System.out.println("  Found " + templates.size() + " templates");

            // Save first template ID for later tests
            if (!templates.isEmpty()) {
                templateId = templates.get(0).getId();
                System.out.println("  Using template: " + templates.get(0).getName() + " (" + templateId + ")");
            }
        });

        test("templates.list(type='pdf') - Filter by type", () -> {
            ListResponse<Template> response = client.templates().list(1, 10, "pdf");
            List<Template> templates = response.getData();
            if (templates == null) {
                throw new Exception("Expected array of templates");
            }
            System.out.println("  Found " + templates.size() + " PDF templates");
        });

        if (templateId != null) {
            test("templates.get() - Get template by ID", () -> {
                Template template = client.templates().get(templateId);
                if (template.getId() == null || template.getName() == null) {
                    throw new Exception("Invalid template response");
                }
                System.out.println("  Template: " + template.getName());
                System.out.println("  Variables: " + (template.getVariables() != null ? template.getVariables().size() : 0));

                // Extract default values from template variables
                templateVariables = buildVariablesFromDefaults(template.getVariables());
                System.out.println("  Using " + templateVariables.size() + " variables with defaults");
            });
        }

        // ==========================================
        // Documents Tests
        // ==========================================

        if (templateId != null) {
            // --- PDF Generation ---
            test("documents.generate(pdf) - Generate PDF document", () -> {
                GenerateResult job = client.documents().generate(
                    GenerateRequest.builder()
                        .templateId(templateId)
                        .format("pdf")
                        .variables(templateVariables)
                        .build()
                );

                if (job.getJobId() == null || !"queued".equals(job.getStatus())) {
                    throw new Exception("Invalid job response");
                }

                jobId = job.getJobId();
                System.out.println("  Job ID: " + jobId);
                System.out.println("  Status: " + job.getStatus());
            });

            if (jobId != null) {
                test("documents.get() - Get PDF job status", () -> {
                    GenerateResult job = client.documents().get(jobId);
                    if (job.getJobId() == null) {
                        throw new Exception("Invalid job response");
                    }
                    System.out.println("  Status: " + job.getStatus());
                });

                test("documents.waitForCompletion() - Wait for PDF completion", () -> {
                    GenerateResult completed = client.documents().waitForCompletion(jobId, 1000, 60000);

                    if (!"completed".equals(completed.getStatus()) && !"failed".equals(completed.getStatus())) {
                        throw new Exception("Job not finished: " + completed.getStatus());
                    }

                    System.out.println("  Final status: " + completed.getStatus());
                    if (completed.getDownloadUrl() != null) {
                        String url = completed.getDownloadUrl();
                        System.out.println("  Download URL: " + url.substring(0, Math.min(50, url.length())) + "...");
                    }
                });
            }

            // --- Excel Generation ---
            final String[] excelJobId = {null};

            test("documents.generate(xlsx) - Generate Excel document", () -> {
                GenerateResult job = client.documents().generate(
                    GenerateRequest.builder()
                        .templateId(templateId)
                        .format("xlsx")
                        .variables(templateVariables)
                        .build()
                );

                if (job.getJobId() == null || !"queued".equals(job.getStatus())) {
                    throw new Exception("Invalid job response");
                }

                excelJobId[0] = job.getJobId();
                System.out.println("  Job ID: " + excelJobId[0]);
                System.out.println("  Status: " + job.getStatus());
            });

            if (excelJobId[0] != null) {
                test("documents.get() - Get Excel job status", () -> {
                    GenerateResult job = client.documents().get(excelJobId[0]);
                    if (job.getJobId() == null) {
                        throw new Exception("Invalid job response");
                    }
                    System.out.println("  Status: " + job.getStatus());
                });

                test("documents.waitForCompletion() - Wait for Excel completion", () -> {
                    GenerateResult completed = client.documents().waitForCompletion(excelJobId[0], 1000, 60000);

                    if (!"completed".equals(completed.getStatus()) && !"failed".equals(completed.getStatus())) {
                        throw new Exception("Job not finished: " + completed.getStatus());
                    }

                    System.out.println("  Final status: " + completed.getStatus());
                    if (completed.getDownloadUrl() != null) {
                        String url = completed.getDownloadUrl();
                        System.out.println("  Download URL: " + url.substring(0, Math.min(50, url.length())) + "...");
                    }
                });
            }

            // --- List Jobs ---
            test("documents.list() - List document jobs", () -> {
                ListResponse<GenerateResult> response = client.documents().list(1, 10);
                List<GenerateResult> jobs = response.getData();
                if (jobs == null) {
                    throw new Exception("Expected array of jobs");
                }
                System.out.println("  Found " + jobs.size() + " jobs");
            });
        }

        // ==========================================
        // Webhooks Tests
        // ==========================================

        test("webhooks.list() - List webhooks", () -> {
            ListResponse<WebhookSubscription> response = client.webhooks().list();
            List<WebhookSubscription> webhooks = response.getData();
            if (webhooks == null) {
                throw new Exception("Expected array of webhooks");
            }
            System.out.println("  Found " + webhooks.size() + " webhooks");

            // If webhooks exist, test get by ID
            if (!webhooks.isEmpty()) {
                String webhookId = webhooks.get(0).getId();
                WebhookSubscription webhook = client.webhooks().get(webhookId);
                if (webhook.getId() == null || webhook.getUrl() == null) {
                    throw new Exception("Invalid webhook response");
                }
                System.out.println("  Retrieved webhook: " + webhookId);
            }
        });

        // ==========================================
        // Error Handling Tests
        // ==========================================

        test("Error handling - Invalid template ID", () -> {
            try {
                client.templates().get("invalid-template-id-12345");
                throw new Exception("Expected error for invalid template");
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
