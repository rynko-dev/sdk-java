# Rynko Java SDK

Official Java SDK for [Rynko](https://rynko.dev) - the document generation and AI output validation platform with unified template design for PDF and Excel documents.

[![Maven Central](https://img.shields.io/maven-central/v/dev.rynko/sdk.svg)](https://search.maven.org/artifact/dev.rynko/sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Features](#features)
- [Authentication](#authentication)
- [Document Generation](#document-generation)
  - [Generate PDF](#generate-pdf)
  - [Generate Excel](#generate-excel)
  - [Using Builder Pattern](#using-builder-pattern)
  - [Wait for Completion](#wait-for-completion)
  - [Download Document](#download-document)
- [Document Jobs](#document-jobs)
  - [Get Job Status](#get-job-status)
  - [List Jobs](#list-jobs)
  - [Retry and Cancel Jobs](#retry-and-cancel-jobs)
- [Templates](#templates)
  - [List Templates](#list-templates)
  - [Get Template Details](#get-template-details)
- [Webhooks](#webhooks)
  - [List Webhooks](#list-webhooks)
  - [Webhook CRUD](#webhook-crud)
  - [Webhook Deliveries](#webhook-deliveries)
  - [Verify Webhook Signatures](#verify-webhook-signatures)
- [Rynko Extract](#rynko-extract)
  - [Create Extraction Job](#create-extraction-job)
  - [Manage Configs](#manage-configs)
  - [Flow Integration](#flow-integration)
- [Rynko Flow](#rynko-flow)
  - [Submit and Wait for Run](#submit-and-wait-for-run)
  - [List Gates](#list-gates)
  - [Gate Management](#gate-management)
  - [Test and Validate Gates](#test-and-validate-gates)
  - [Run Payload and Chains](#run-payload-and-chains)
  - [Manage Approvals](#manage-approvals)
  - [Monitor Deliveries](#monitor-deliveries)
- [Configuration](#configuration)
- [Error Handling](#error-handling)
- [Thread Safety](#thread-safety)
- [Spring Boot Integration](#spring-boot-integration)
- [API Reference](#api-reference)
- [Support](#support)

## Requirements

- Java 8 or higher
- Maven or Gradle

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.rynko</groupId>
    <artifactId>sdk</artifactId>
    <version>1.4.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation 'dev.rynko:sdk:1.4.0'
```

### Gradle (Kotlin DSL)

```kotlin
implementation("dev.rynko:sdk:1.4.0")
```

## Quick Start

```java
import dev.rynko.Rynko;
import dev.rynko.models.GenerateRequest;
import dev.rynko.models.GenerateResult;

public class Example {
    public static void main(String[] args) {
        // Initialize the client
        Rynko client = new Rynko(System.getenv("RYNKO_API_KEY"));

        // Queue document generation (async operation)
        GenerateResult job = client.documents().generate(
            GenerateRequest.builder()
                .templateId("tmpl_invoice")
                .format("pdf")
                .variable("invoiceNumber", "INV-2026-001")
                .variable("customerName", "Acme Corporation")
                .variable("amount", 1250.00)
                .build()
        );

        System.out.println("Job ID: " + job.getJobId());
        System.out.println("Status: " + job.getStatus());  // "queued"

        // Wait for completion to get download URL
        GenerateResult completed = client.documents().waitForCompletion(job.getJobId());

        if (completed.isCompleted()) {
            System.out.println("Download URL: " + completed.getDownloadUrl());
        } else if (completed.isFailed()) {
            System.err.println("Generation failed: " + completed.getErrorMessage());
        }
    }
}
```

## Features

- **Java 8+ compatible** - Works with modern and legacy Java versions
- **Builder pattern** - Fluent API for constructing requests
- **Thread-safe** - Client can be shared across threads
- **PDF generation** - Generate PDF documents from templates
- **Excel generation** - Generate Excel spreadsheets from templates
- **Batch generation** - Generate multiple documents in a single request
- **Environment support** - Generate documents in specific environments
- **Webhook verification** - Secure HMAC signature verification for incoming webhooks
- **Polling utility** - Built-in `waitForCompletion()` method with configurable timeout
- **Rynko Extract** - AI-powered data extraction from documents
- **Rynko Flow** - Submit runs for validation, manage gates, approvals, and deliveries
- **Webhook CRUD** - Full webhook subscription management with delivery tracking

## Authentication

### Get an API Key

1. Log in to your [Rynko Dashboard](https://app.rynko.dev)
2. Navigate to **Settings** → **API Keys**
3. Click **Create API Key**
4. Copy the key and store it securely (it won't be shown again)

### Initialize the Client

```java
import dev.rynko.Rynko;
import dev.rynko.models.User;

// Using environment variable (recommended)
Rynko client = new Rynko(System.getenv("RYNKO_API_KEY"));

// Verify authentication
User user = client.me();
System.out.println("Authenticated as: " + user.getEmail());
System.out.println("Name: " + user.getName());
```

### Verify API Key

```java
// Check if API key is valid
boolean isValid = client.verifyApiKey();
System.out.println("API Key valid: " + isValid);
```

## Document Generation

Document generation in Rynko is **asynchronous**. When you call `generate()`, the job is queued for processing and you receive a job ID immediately. Use `waitForCompletion()` to poll until the document is ready.

### Generate PDF

```java
import dev.rynko.models.GenerateRequest;
import dev.rynko.models.GenerateResult;
import java.util.Arrays;
import java.util.Map;

// Queue PDF generation
GenerateResult job = client.documents().generate(
    GenerateRequest.builder()
        .templateId("tmpl_invoice")
        .format("pdf")
        .variable("invoiceNumber", "INV-001")
        .variable("customerName", "John Doe")
        .variable("customerEmail", "john@example.com")
        .variable("items", Arrays.asList(
            Map.of("description", "Product A", "quantity", 2, "price", 50.00),
            Map.of("description", "Product B", "quantity", 1, "price", 50.00)
        ))
        .variable("subtotal", 150.00)
        .variable("tax", 15.00)
        .variable("total", 165.00)
        .build()
);

System.out.println("Job queued: " + job.getJobId());
System.out.println("Status: " + job.getStatus());  // "queued"

// Wait for completion
GenerateResult completed = client.documents().waitForCompletion(job.getJobId());
System.out.println("Download URL: " + completed.getDownloadUrl());
```

### Generate Excel

```java
GenerateResult job = client.documents().generate(
    GenerateRequest.builder()
        .templateId("tmpl_sales_report")
        .format("excel")
        .variable("reportTitle", "Q1 2026 Sales Report")
        .variable("reportDate", "2026-03-31")
        .variable("salesData", Arrays.asList(
            Map.of("region", "North", "q1", 125000),
            Map.of("region", "South", "q1", 98000),
            Map.of("region", "East", "q1", 145000),
            Map.of("region", "West", "q1", 112000)
        ))
        .variable("totalSales", 480000)
        .build()
);

GenerateResult completed = client.documents().waitForCompletion(job.getJobId());
System.out.println("Excel file ready: " + completed.getDownloadUrl());
```

### Using Builder Pattern

The `GenerateRequest.Builder` provides a fluent API for constructing requests:

```java
GenerateRequest request = GenerateRequest.builder()
    // Required
    .templateId("tmpl_contract")
    .format("pdf")  // "pdf", "excel", or "csv"

    // Add variables one at a time
    .variable("contractNumber", "CTR-2026-001")
    .variable("clientName", "Acme Corporation")
    .variable("startDate", "2026-02-01")
    .variable("endDate", "2027-01-31")

    // Or add all variables at once
    .variables(Map.of(
        "terms", "Standard Terms",
        "value", 50000.00
    ))

    // Optional settings
    .filename("contract-acme-2026")      // Custom filename (without extension)
    .workspaceId("ws_abc123")            // Generate in specific environment
    .metadata(Map.of(                     // Custom metadata (passed to webhook)
        "orderId", "ORD-12345",
        "userId", "user_abc"
    ))
    .build();

GenerateResult job = client.documents().generate(request);
```

### Wait for Completion

The `waitForCompletion()` method polls the job status until it completes or fails:

```java
// Default settings (1 second interval, 30 second timeout)
GenerateResult completed = client.documents().waitForCompletion(job.getJobId());

// Custom polling settings
GenerateResult completed = client.documents().waitForCompletion(
    job.getJobId(),
    2000,   // pollIntervalMs - Check every 2 seconds
    60000   // timeoutMs - Wait up to 60 seconds
);

// Check result
if (completed.isCompleted()) {
    System.out.println("Download URL: " + completed.getDownloadUrl());
    System.out.println("Expires at: " + completed.getExpiresAt());
} else if (completed.isFailed()) {
    System.err.println("Generation failed: " + completed.getErrorMessage());
    System.err.println("Error code: " + completed.getErrorCode());
}
```

### Download Document

```java
// After waiting for completion, download the document
byte[] documentBytes = client.documents().download(completed.getDownloadUrl());

// Save to file
try (FileOutputStream fos = new FileOutputStream("document.pdf")) {
    fos.write(documentBytes);
    System.out.println("Document saved to document.pdf");
}

// Or process the bytes directly
// e.g., upload to S3, attach to email, etc.
```

## Document Jobs

### Get Job Status

```java
import dev.rynko.models.GenerateResult;

GenerateResult job = client.documents().get("job_abc123");

System.out.println("Status: " + job.getStatus());
// Possible values: "queued", "processing", "completed", "failed"

System.out.println("Template ID: " + job.getTemplateId());
System.out.println("Format: " + job.getFormat());

if (job.isCompleted()) {
    System.out.println("Download URL: " + job.getDownloadUrl());
    System.out.println("URL expires: " + job.getExpiresAt());
}

if (job.isFailed()) {
    System.out.println("Error: " + job.getErrorMessage());
    System.out.println("Error code: " + job.getErrorCode());
}
```

### List Jobs

```java
import dev.rynko.models.ListResponse;
import dev.rynko.models.GenerateResult;

// List recent jobs with pagination
ListResponse<GenerateResult> result = client.documents().list(1, 20);

System.out.println("Total jobs: " + result.getMeta().getTotal());
System.out.println("Total pages: " + result.getMeta().getTotalPages());

for (GenerateResult job : result.getData()) {
    System.out.println(job.getJobId() + ": " + job.getStatus() + " - " + job.getTemplateId());
}

// Check for more pages
if (result.hasMore()) {
    int nextPage = result.getMeta().getPage() + 1;
    ListResponse<GenerateResult> nextResult = client.documents().list(nextPage, 20);
}

// Filter by template
ListResponse<GenerateResult> invoiceJobs = client.documents().list(1, 20, "tmpl_invoice", null);

// Filter by environment
ListResponse<GenerateResult> workspaceJobs = client.documents().list(1, 20, null, "ws_abc123");

// Filter by status
ListResponse<GenerateResult> completedJobs = client.documents().list(1, 20, null, null, "completed");

// Combine filters
ListResponse<GenerateResult> filteredJobs = client.documents().list(
    1,              // page
    50,             // limit
    "tmpl_invoice", // templateId
    "ws_abc123",    // workspaceId
    "completed"     // status
);
```

### Retry and Cancel Jobs

```java
// Retry a failed document generation job
GenerateResult retried = client.documents().retry("job_abc123");
System.out.println("Retried job status: " + retried.getStatus());

// Cancel a queued or processing job
client.documents().cancel("job_abc123");
```

## Templates

### List Templates

```java
import dev.rynko.models.ListResponse;
import dev.rynko.models.Template;

// List all templates
ListResponse<Template> result = client.templates().list();

System.out.println("Total templates: " + result.getMeta().getTotal());

for (Template template : result.getData()) {
    System.out.println(template.getId() + ": " + template.getName() + " (" + template.getType() + ")");
}

// Paginated list
ListResponse<Template> page2 = client.templates().list(2, 10);

// Search by name
ListResponse<Template> invoiceTemplates = client.templates().list(1, 20, "invoice");

// Filter by type using convenience methods
ListResponse<Template> pdfOnly = client.templates().listPdf();
ListResponse<Template> excelOnly = client.templates().listExcel();
```

### Get Template Details

```java
import dev.rynko.models.Template;
import dev.rynko.models.TemplateVariable;

// Get template by ID (supports UUID, shortId, or slug)
Template template = client.templates().get("tmpl_invoice");

System.out.println("Template: " + template.getName());
System.out.println("Type: " + template.getType());  // "pdf" or "excel"
System.out.println("Description: " + template.getDescription());
System.out.println("Created: " + template.getCreatedAt());
System.out.println("Updated: " + template.getUpdatedAt());

// View template variables
if (template.getVariables() != null) {
    System.out.println("\nVariables:");
    for (TemplateVariable variable : template.getVariables()) {
        System.out.println("  " + variable.getName() + " (" + variable.getType() + ")");
        System.out.println("    Required: " + variable.isRequired());
        if (variable.getDefaultValue() != null) {
            System.out.println("    Default: " + variable.getDefaultValue());
        }
    }
}
```

## Webhooks

The SDK provides full webhook management including creating, updating, and deleting subscriptions, plus signature verification for incoming webhooks.

### List Webhooks

```java
import dev.rynko.models.ListResponse;
import dev.rynko.resources.WebhooksResource.WebhookSubscription;

ListResponse<WebhookSubscription> result = client.webhooks().list();

for (WebhookSubscription webhook : result.getData()) {
    System.out.println(webhook.getId() + ": " + webhook.getUrl());
    System.out.println("  Events: " + String.join(", ", webhook.getEvents()));
    System.out.println("  Active: " + webhook.isActive());
    System.out.println("  Created: " + webhook.getCreatedAt());
}
```

### Get Webhook Details

```java
WebhookSubscription webhook = client.webhooks().get("wh_abc123");

System.out.println("URL: " + webhook.getUrl());
System.out.println("Events: " + Arrays.toString(webhook.getEvents()));
System.out.println("Active: " + webhook.isActive());
System.out.println("Description: " + webhook.getDescription());
```

### Webhook CRUD

```java
import dev.rynko.models.CreateWebhookRequest;
import dev.rynko.models.UpdateWebhookRequest;

// Create a webhook subscription
WebhookSubscription webhook = client.webhooks().create(
    CreateWebhookRequest.builder()
        .url("https://your-app.com/webhooks/rynko")
        .event("document.generated")
        .event("document.failed")
        .event("batch.completed")
        .description("Production webhook")
        .workspaceId("ws_abc123")  // Optional
        .build()
);
System.out.println("Webhook ID: " + webhook.getId());
System.out.println("Secret: " + webhook.getSecret());

// Update a webhook
WebhookSubscription updated = client.webhooks().update(webhook.getId(),
    UpdateWebhookRequest.builder()
        .description("Updated production webhook")
        .isActive(true)
        .build()
);

// Rotate the signing secret
WebhookSubscription rotated = client.webhooks().rotateSecret(webhook.getId());
System.out.println("New secret: " + rotated.getSecret());

// Send a test event
client.webhooks().test(webhook.getId());

// Delete a webhook
client.webhooks().delete(webhook.getId());
```

### Webhook Deliveries

```java
import dev.rynko.models.WebhookDelivery;

// List deliveries for a webhook
ListResponse<WebhookDelivery> deliveries = client.webhooks().listDeliveries("wh_abc123");

for (WebhookDelivery delivery : deliveries.getData()) {
    System.out.println(delivery.getId() + ": " + delivery.getStatus()
        + " (HTTP " + delivery.getHttpStatus() + ")");
}

// List with pagination
ListResponse<WebhookDelivery> page = client.webhooks().listDeliveries("wh_abc123", 10, 0);

// Retry a failed delivery
WebhookDelivery retried = client.webhooks().retryDelivery("wh_abc123", "del_xyz789");
System.out.println("Retry status: " + retried.getStatus());
```

### Verify Webhook Signatures

When receiving webhooks, always verify the signature to ensure the request came from Rynko:

```java
import dev.rynko.resources.WebhooksResource.WebhookEvent;
import dev.rynko.exceptions.WebhookSignatureException;

// In your webhook endpoint handler (e.g., Spring Controller, Servlet)
public void handleWebhook(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

    // Read the raw request body
    String payload = request.getReader().lines().collect(Collectors.joining());
    String signature = request.getHeader("X-Rynko-Signature");
    String timestamp = request.getHeader("X-Rynko-Timestamp");
    String webhookSecret = System.getenv("WEBHOOK_SECRET");

    try {
        // Verify signature and construct event
        WebhookEvent event = client.webhooks().constructEvent(
            payload, signature, timestamp, webhookSecret
        );

        // Process the verified event
        System.out.println("Event type: " + event.getType());
        System.out.println("Event ID: " + event.getId());

        switch (event.getType()) {
            case "document.generated":
                handleDocumentCompleted(event);
                break;
            case "document.failed":
                handleDocumentFailed(event);
                break;
            case "batch.completed":
                handleBatchCompleted(event);
                break;
            case "document.downloaded":
                handleDocumentDownloaded(event);
                break;
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }

        response.setStatus(200);
        response.getWriter().write("OK");

    } catch (WebhookSignatureException e) {
        // Invalid signature - reject the webhook
        System.err.println("Invalid webhook signature: " + e.getMessage());
        response.setStatus(401);
        response.getWriter().write("Invalid signature");
    }
}

private void handleDocumentCompleted(WebhookEvent event) {
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) event.getData();

    String jobId = (String) data.get("jobId");
    String downloadUrl = (String) data.get("downloadUrl");
    String templateId = (String) data.get("templateId");
    @SuppressWarnings("unchecked")
    Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");

    System.out.println("Document " + jobId + " ready: " + downloadUrl);
    // Access metadata you passed during generation
    if (metadata != null) {
        System.out.println("Order ID: " + metadata.get("orderId"));
    }
    // Download or process the document
}

private void handleDocumentFailed(WebhookEvent event) {
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) event.getData();

    String jobId = (String) data.get("jobId");
    String errorMessage = (String) data.get("errorMessage");
    String errorCode = (String) data.get("errorCode");
    @SuppressWarnings("unchecked")
    Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");

    System.err.println("Document " + jobId + " failed: " + errorMessage);
    // Access metadata for correlation
    if (metadata != null) {
        System.out.println("Failed order: " + metadata.get("orderId"));
    }
    // Handle failure (retry, notify user, etc.)
}

private void handleBatchCompleted(WebhookEvent event) {
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) event.getData();

    String batchId = (String) data.get("batchId");
    int totalJobs = ((Number) data.get("totalJobs")).intValue();
    int completedJobs = ((Number) data.get("completedJobs")).intValue();
    int failedJobs = ((Number) data.get("failedJobs")).intValue();
    System.out.println("Batch " + batchId + " done: " + completedJobs + "/" + totalJobs
            + " succeeded, " + failedJobs + " failed");
}
```

#### Spring Boot Webhook Controller

```java
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import dev.rynko.Rynko;
import dev.rynko.resources.WebhooksResource.WebhookEvent;
import dev.rynko.exceptions.WebhookSignatureException;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final Rynko rynko;
    private final String webhookSecret;

    public WebhookController(Rynko rynko,
                            @Value("${rynko.webhook-secret}") String webhookSecret) {
        this.rynko = rynko;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/rynko")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Rynko-Signature") String signature,
            @RequestHeader("X-Rynko-Timestamp") String timestamp) {

        try {
            WebhookEvent event = rynko.webhooks().constructEvent(
                payload, signature, timestamp, webhookSecret
            );

            switch (event.getType()) {
                case "document.generated":
                    // Handle document completion
                    break;
                case "document.failed":
                    // Handle document failure
                    break;
                case "batch.completed":
                    // Handle batch completion
                    break;
            }

            return ResponseEntity.ok("OK");

        } catch (WebhookSignatureException e) {
            return ResponseEntity.status(401).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal error");
        }
    }
}
```

#### Webhook Event Types

| Event | Description | Payload |
|-------|-------------|---------|
| `document.generated` | Document successfully generated | `jobId`, `templateId`, `format`, `downloadUrl`, `fileSize`, `metadata` |
| `document.failed` | Document generation failed | `jobId`, `templateId`, `errorMessage`, `errorCode`, `metadata` |
| `document.downloaded` | Document was downloaded | `jobId`, `downloadedAt` |
| `batch.completed` | Batch generation finished | `batchId`, `templateId`, `format`, `totalJobs`, `completedJobs`, `failedJobs`, `metadata` |
| `flow.run.completed` | Flow run completed successfully | `runId`, `gateId`, `status`, `output` |
| `flow.run.approved` | Flow run approved by reviewer | `runId`, `gateId`, `status`, `approvalId` |
| `flow.run.rejected` | Flow run rejected by reviewer | `runId`, `gateId`, `status`, `reason` |
| `flow.run.review_required` | Flow run requires human review | `runId`, `gateId`, `status` |
| `flow.delivery.failed` | Flow delivery failed | `deliveryId`, `runId`, `error`, `attempts` |

#### Webhook Headers

Rynko sends these headers with each webhook request:

| Header | Description |
|--------|-------------|
| `X-Rynko-Signature` | HMAC-SHA256 signature (format: `t=<timestamp>,v1=<hex>`) |
| `X-Rynko-Timestamp` | Unix timestamp when the webhook was sent |
| `X-Rynko-Event-Id` | Unique event identifier |
| `X-Rynko-Event-Type` | Event type (e.g., `document.generated`) |

#### Manual Signature Verification

For advanced use cases, you can verify signatures manually:

```java
// Verify signature without constructing event
try {
    client.webhooks().verifySignature(payload, signature, timestamp, webhookSecret);
    // Signature is valid
} catch (WebhookSignatureException e) {
    // Invalid signature
}

// Then parse the event separately
WebhookEvent event = client.webhooks().constructEvent(payload);
```

## Rynko Extract

[Rynko Extract](https://rynko.dev/extract) provides AI-powered data extraction from documents.

### Create Extraction Job

```java
import dev.rynko.models.ExtractJobRequest;
import dev.rynko.models.ExtractJob;

import java.io.File;
import java.util.Map;

// Extract data from a document with a schema
ExtractJob job = client.extract().createJob(
    ExtractJobRequest.builder()
        .file(new File("invoice.pdf"))
        .schema(Map.of(
            "invoiceNumber", Map.of("type", "string"),
            "total", Map.of("type", "number"),
            "lineItems", Map.of("type", "array")
        ))
        .instructions("Extract invoice details")
        .build()
);

System.out.println("Job ID: " + job.getId());
System.out.println("Status: " + job.getStatus());

// Get job result
ExtractJob result = client.extract().getJob(job.getId());
if (result.isTerminal()) {
    System.out.println("Result: " + result.getResult());
}

// List extraction jobs
ListResponse<ExtractJob> jobs = client.extract().listJobs(1, 20, "completed");

// Cancel a job
client.extract().cancelJob("job_abc123");

// Get usage statistics
ExtractUsage usage = client.extract().getUsage();
System.out.println("Used: " + usage.getUsed() + "/" + usage.getLimit());
```

### Discover Schema

```java
import dev.rynko.models.DiscoverRequest;

// Discover schema from sample files
ExtractJob discovered = client.extract().discover(
    DiscoverRequest.builder()
        .file(new File("sample-invoice.pdf"))
        .instructions("Identify all fields in this invoice")
        .build()
);

System.out.println("Discovered schema: " + discovered.getSchema());
```

### Manage Configs

```java
import dev.rynko.models.CreateConfigRequest;
import dev.rynko.models.UpdateConfigRequest;
import dev.rynko.models.ExtractConfig;

// Create a config
ExtractConfig config = client.extract().createConfig(
    CreateConfigRequest.builder()
        .name("Invoice Extractor")
        .description("Extracts data from invoices")
        .schema(schema)
        .instructions("Extract all invoice fields")
        .build()
);

// Update a config
ExtractConfig updated = client.extract().updateConfig(config.getId(),
    UpdateConfigRequest.builder()
        .description("Updated description")
        .build()
);

// Publish a config
client.extract().publishConfig(config.getId());

// List configs
ListResponse<ExtractConfig> configs = client.extract().listConfigs();

// Get config versions
ListResponse<ExtractConfig> versions = client.extract().getConfigVersions(config.getId());

// Restore a version
client.extract().restoreConfigVersion(config.getId(), "version_abc123");

// Run a config against files
ExtractJob result = client.extract().runConfig(config.getId(),
    Arrays.asList(new File("invoice1.pdf"), new File("invoice2.pdf")));

// Delete a config
client.extract().deleteConfig(config.getId());
```

### Flow Integration

```java
// Extract data using a Flow gate
ExtractJob extracted = client.extract().extractWithGate("gate_abc123",
    Arrays.asList(new File("document.pdf")));

// Submit files to a Flow gate pipeline
FlowRun run = client.extract().submitFileRun("gate_abc123",
    Arrays.asList(new File("document.pdf")));
System.out.println("Run ID: " + run.getId());
```

## Rynko Flow

[Rynko Flow](https://rynko.dev/flow) is an AI output validation gateway. Define gates with schemas and business rules, submit data for validation, handle human-in-the-loop approvals, and track webhook deliveries.

### Submit and Wait for Run

```java
import dev.rynko.models.FlowRun;
import dev.rynko.models.SubmitRunRequest;

// Submit data to a gate for validation
FlowRun run = client.flow().submitRun("gate_abc123",
    SubmitRunRequest.builder()
        .inputField("customerName", "John Doe")
        .inputField("email", "john@example.com")
        .inputField("amount", 150.00)
        .metadata("source", "checkout")
        .webhookUrl("https://your-app.com/webhooks/flow")
        .build()
);

System.out.println("Run ID: " + run.getId());
System.out.println("Status: " + run.getStatus());    // "validated" or "validation_failed"
System.out.println("Success: " + run.getSuccess());  // true or false

// Check immediate validation result
if (!run.getSuccess()) {
    System.out.println("Validation failed!");
}

// For gates with rendering/approval steps, wait for terminal state
FlowRun result = client.flow().waitForRun(run.getId(), 2000, 120000);
// pollIntervalMs: 2000 (default: 1000)
// timeoutMs: 120000 (default: 60000)

if ("validated".equals(result.getStatus()) || "approved".equals(result.getStatus())) {
    System.out.println("Validation passed!");
} else if ("validation_failed".equals(result.getStatus())) {
    System.out.println("Validation failed: " + result.getErrors());
} else if ("rejected".equals(result.getStatus())) {
    System.out.println("Rejected by reviewer: " + result.getErrors());
}
```

### List Gates

```java
import dev.rynko.models.FlowGate;
import dev.rynko.models.ListResponse;

// List all gates
ListResponse<FlowGate> gates = client.flow().listGates();

for (FlowGate gate : gates.getData()) {
    System.out.println(gate.getId() + ": " + gate.getName() + " (" + gate.getStatus() + ")");
}

// Get a specific gate
FlowGate gate = client.flow().getGate("gate_abc123");
System.out.println("Gate: " + gate.getName());
```

### Gate Management

```java
import dev.rynko.models.CreateGateRequest;
import dev.rynko.models.UpdateGateRequest;

// Create a gate
FlowGate gate = client.flow().createGate(
    CreateGateRequest.builder()
        .name("Order Validator")
        .description("Validates order data")
        .workspaceId("ws_abc123")
        .schema(orderSchema)
        .rules(validationRules)
        .build()
);

// Update a gate
FlowGate updated = client.flow().updateGate(gate.getId(),
    UpdateGateRequest.builder()
        .description("Updated order validator")
        .build()
);

// Update gate schema
client.flow().updateGateSchema(gate.getId(), newSchema);

// Publish a gate
client.flow().publishGate(gate.getId());

// Rollback a gate
client.flow().rollbackGate(gate.getId());
client.flow().rollbackGate(gate.getId(), "version_abc123");  // To specific version

// Export and import gates
Map<String, Object> exported = client.flow().exportGate(gate.getId());
FlowGate imported = client.flow().importGate(exported);

// Delete a gate
client.flow().deleteGate(gate.getId());
```

### Test and Validate Gates

```java
import dev.rynko.models.TestGateResult;
import dev.rynko.models.ValidateGateRequest;

// Test a gate (dry-run, no run created)
TestGateResult testResult = client.flow().testGate("gate_abc123",
    Map.of("name", "John Doe", "amount", 150.00));

System.out.println("Valid: " + testResult.isValid());
if (!testResult.isValid()) {
    System.out.println("Errors: " + testResult.getErrors());
}

// Validate a gate (creates a run + validation_id)
FlowRun validated = client.flow().validateGate("gate_abc123",
    ValidateGateRequest.builder()
        .payloadField("name", "John Doe")
        .payloadField("amount", 150.00)
        .webhookUrl("https://your-app.com/webhooks/flow")
        .build()
);

// Verify a validation
Map<String, Object> verification = client.flow().verifyValidation(
    "validation_abc123", Map.of("confirmed", true));
```

### Run Payload and Chains

```java
// Get the full payload for a run
Map<String, Object> payload = client.flow().getRunPayload("run_abc123");

// Get a specific field from the payload
Map<String, Object> field = client.flow().getRunPayload("run_abc123", "customerName");

// Get all runs in a correlation chain
ListResponse<FlowRun> chain = client.flow().getRunChain("correlation_abc123");
for (FlowRun r : chain.getData()) {
    System.out.println(r.getId() + ": " + r.getStatus());
}

// Get a transaction
Map<String, Object> transaction = client.flow().getTransaction("txn_abc123");
```

### List and Filter Runs

```java
import dev.rynko.models.FlowRun;
import dev.rynko.models.ListResponse;

// List all runs
ListResponse<FlowRun> runs = client.flow().listRuns();

// Filter by status
ListResponse<FlowRun> approved = client.flow().listRuns(1, 20, "approved");

// List runs for a specific gate
ListResponse<FlowRun> gateRuns = client.flow().listRunsByGate("gate_abc123");

// List active (in-progress) runs
ListResponse<FlowRun> active = client.flow().listActiveRuns();
System.out.println(active.getData().size() + " runs in progress");

// Get a specific run
FlowRun run = client.flow().getRun("run_abc123");
System.out.println("Status: " + run.getStatus());
```

### Manage Approvals

When a gate has approval rules, runs may enter a `pending_approval` state:

```java
import dev.rynko.models.FlowApproval;
import dev.rynko.models.ListResponse;

// List pending approvals
ListResponse<FlowApproval> approvals = client.flow().listApprovals(1, 20, "pending");

for (FlowApproval approval : approvals.getData()) {
    System.out.println("Approval " + approval.getId() + " for run " + approval.getRunId());

    // Approve with a note
    client.flow().approve(approval.getId(), "Looks good, approved.");

    // Or reject with a reason
    // client.flow().reject(approval.getId(), "Amount exceeds limit.");
}
```

### Monitor Deliveries

Track webhook deliveries for completed runs:

```java
import dev.rynko.models.FlowDelivery;
import dev.rynko.models.ListResponse;

// List deliveries for a run
ListResponse<FlowDelivery> deliveries = client.flow().listDeliveries("run_abc123");

for (FlowDelivery delivery : deliveries.getData()) {
    System.out.println(delivery.getId() + ": " + delivery.getStatus() + " → " + delivery.getUrl());
}

// Retry a failed delivery
FlowDelivery retried = client.flow().retryDelivery("delivery_abc123");
System.out.println("Retry status: " + retried.getStatus());
```

## Configuration

### Basic Configuration

```java
// Simple initialization with API key
Rynko client = new Rynko(System.getenv("RYNKO_API_KEY"));

// With custom base URL
Rynko client = new Rynko(
    System.getenv("RYNKO_API_KEY"),
    "https://api.rynko.dev/api"
);
```

### Advanced Configuration

```java
import dev.rynko.RynkoConfig;

RynkoConfig config = RynkoConfig.builder()
    .apiKey(System.getenv("RYNKO_API_KEY"))
    .baseUrl("https://api.rynko.dev/api")
    .timeoutMs(60000)              // 60 seconds (default: 30000)
    .maxRetries(3)                 // Max retry attempts (default: 5)
    .initialDelayMs(500)           // Initial retry delay (default: 1000)
    .maxDelayMs(10000)             // Max retry delay (default: 30000)
    .maxJitterMs(500)              // Max jitter (default: 1000)
    .build();

Rynko client = new Rynko(config);

// Disable retry entirely
RynkoConfig noRetryConfig = RynkoConfig.builder()
    .apiKey(System.getenv("RYNKO_API_KEY"))
    .retryEnabled(false)
    .build();
```

## Error Handling

```java
import dev.rynko.exceptions.RynkoException;

try {
    GenerateResult result = client.documents().generate(request);
} catch (RynkoException e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("Error Code: " + e.getCode());
    System.err.println("Status Code: " + e.getStatusCode());

    // Handle specific error codes
    switch (e.getCode()) {
        case "ERR_TMPL_001":
            System.err.println("Template not found");
            break;
        case "ERR_TMPL_003":
            System.err.println("Template validation failed");
            break;
        case "ERR_QUOTA_001":
            System.err.println("Document quota exceeded - upgrade your plan");
            break;
        case "ERR_AUTH_001":
            System.err.println("Invalid API key");
            break;
        case "ERR_AUTH_004":
            System.err.println("API key expired or revoked");
            break;
        default:
            System.err.println("Unknown error");
    }
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| `ERR_AUTH_001` | Invalid credentials / API key |
| `ERR_AUTH_004` | Token expired or revoked |
| `ERR_TMPL_001` | Template not found |
| `ERR_TMPL_003` | Template validation failed |
| `ERR_DOC_001` | Document job not found |
| `ERR_DOC_004` | Document generation failed |
| `ERR_QUOTA_001` | Document quota exceeded |
| `ERR_QUOTA_002` | Rate limit exceeded |

## Thread Safety

The `Rynko` client is **thread-safe** and can be shared across multiple threads. We recommend creating a single instance and reusing it throughout your application.

```java
// Application-wide singleton pattern
public class RynkoClient {
    private static final Rynko INSTANCE;

    static {
        INSTANCE = new Rynko(System.getenv("RYNKO_API_KEY"));
    }

    public static Rynko getInstance() {
        return INSTANCE;
    }

    private RynkoClient() {} // Prevent instantiation
}

// Usage from any thread
GenerateResult job = RynkoClient.getInstance().documents().generate(request);
```

## Spring Boot Integration

### Configuration Class

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import dev.rynko.Rynko;
import dev.rynko.RynkoConfig;

@Configuration
public class RynkoConfiguration {

    @Value("${rynko.api-key}")
    private String apiKey;

    @Value("${rynko.base-url:https://api.rynko.dev/api}")
    private String baseUrl;

    @Value("${rynko.timeout-ms:30000}")
    private int timeoutMs;

    @Bean
    public Rynko rynko() {
        RynkoConfig config = RynkoConfig.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .timeoutMs(timeoutMs)
            .build();
        return new Rynko(config);
    }
}
```

### Application Properties

```properties
# application.properties
rynko.api-key=${RYNKO_API_KEY}
rynko.base-url=https://api.rynko.dev/api
rynko.timeout-ms=30000
rynko.webhook-secret=${WEBHOOK_SECRET}
```

### Service Example

```java
import org.springframework.stereotype.Service;
import dev.rynko.Rynko;
import dev.rynko.models.GenerateRequest;
import dev.rynko.models.GenerateResult;

@Service
public class DocumentService {

    private final Rynko rynko;

    public DocumentService(Rynko rynko) {
        this.rynko = rynko;
    }

    public String generateInvoice(Invoice invoice) {
        // Queue document generation
        GenerateResult job = rynko.documents().generate(
            GenerateRequest.builder()
                .templateId("tmpl_invoice")
                .format("pdf")
                .variable("invoiceNumber", invoice.getNumber())
                .variable("customerName", invoice.getCustomer().getName())
                .variable("customerEmail", invoice.getCustomer().getEmail())
                .variable("lineItems", invoice.getLineItems())
                .variable("subtotal", invoice.getSubtotal())
                .variable("tax", invoice.getTax())
                .variable("total", invoice.getTotal())
                .build()
        );

        // Wait for completion
        GenerateResult completed = rynko.documents().waitForCompletion(job.getJobId());

        if (completed.isFailed()) {
            throw new RuntimeException("Document generation failed: " + completed.getErrorMessage());
        }

        return completed.getDownloadUrl();
    }

    public byte[] downloadDocument(String downloadUrl) {
        return rynko.documents().download(downloadUrl);
    }
}
```

### Controller Example

```java
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final DocumentService documentService;

    public InvoiceController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/{id}/generate-pdf")
    public ResponseEntity<Map<String, String>> generatePdf(@PathVariable Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));

        String downloadUrl = documentService.generateInvoice(invoice);

        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
    }
}
```

## API Reference

### Rynko

| Method | Returns | Description |
|--------|---------|-------------|
| `me()` | `User` | Get current authenticated user |
| `verifyApiKey()` | `boolean` | Verify API key is valid |
| `documents()` | `DocumentsResource` | Access document generation operations |
| `extract()` | `ExtractResource` | Access data extraction operations |
| `templates()` | `TemplatesResource` | Access template operations |
| `webhooks()` | `WebhooksResource` | Access webhook operations |
| `flow()` | `FlowResource` | Access Flow operations |

### DocumentsResource

| Method | Returns | Description |
|--------|---------|-------------|
| `generate(request)` | `GenerateResult` | Queue document generation |
| `get(jobId)` | `GenerateResult` | Get document job by ID |
| `list()` | `ListResponse<GenerateResult>` | List document jobs |
| `list(page, limit)` | `ListResponse<GenerateResult>` | List with pagination |
| `list(page, limit, templateId, workspaceId)` | `ListResponse<GenerateResult>` | List with filters |
| `list(page, limit, templateId, workspaceId, status)` | `ListResponse<GenerateResult>` | List with all filters |
| `waitForCompletion(jobId)` | `GenerateResult` | Poll until job completes (default timeout) |
| `waitForCompletion(jobId, pollIntervalMs, timeoutMs)` | `GenerateResult` | Poll with custom settings |
| `retry(jobId)` | `GenerateResult` | Retry a failed job |
| `cancel(jobId)` | `void` | Cancel a queued/processing job |
| `delete(jobId)` | `void` | Delete a generated document |
| `download(url)` | `byte[]` | Download document as bytes |

### TemplatesResource

| Method | Returns | Description |
|--------|---------|-------------|
| `list()` | `ListResponse<Template>` | List all templates |
| `list(page, limit)` | `ListResponse<Template>` | List with pagination |
| `list(page, limit, search)` | `ListResponse<Template>` | List with search filter |
| `listPdf()` | `ListResponse<Template>` | List PDF templates only |
| `listPdf(page, limit)` | `ListResponse<Template>` | List PDF templates with pagination |
| `listExcel()` | `ListResponse<Template>` | List Excel templates only |
| `listExcel(page, limit)` | `ListResponse<Template>` | List Excel templates with pagination |
| `get(templateId)` | `Template` | Get template by ID (UUID, shortId, or slug) |

### ExtractResource

| Method | Returns | Description |
|--------|---------|-------------|
| `createJob(request)` | `ExtractJob` | Create an extraction job (multipart) |
| `getJob(jobId)` | `ExtractJob` | Get extraction job by ID |
| `listJobs()` | `ListResponse<ExtractJob>` | List extraction jobs |
| `listJobs(page, limit, status)` | `ListResponse<ExtractJob>` | List with filters |
| `cancelJob(jobId)` | `void` | Cancel an extraction job |
| `getUsage()` | `ExtractUsage` | Get extraction usage stats |
| `discover(request)` | `ExtractJob` | Discover schema from sample files |
| `createConfig(request)` | `ExtractConfig` | Create extraction config |
| `getConfig(configId)` | `ExtractConfig` | Get config by ID |
| `listConfigs()` | `ListResponse<ExtractConfig>` | List extraction configs |
| `listConfigs(page, limit, status)` | `ListResponse<ExtractConfig>` | List with filters |
| `updateConfig(configId, request)` | `ExtractConfig` | Update a config |
| `deleteConfig(configId)` | `void` | Delete a config |
| `publishConfig(configId)` | `ExtractConfig` | Publish a config |
| `getConfigVersions(configId)` | `ListResponse<ExtractConfig>` | Get config version history |
| `restoreConfigVersion(configId, versionId)` | `ExtractConfig` | Restore a config version |
| `runConfig(configId, files)` | `ExtractJob` | Run config against files |
| `extractWithGate(gateId, files)` | `ExtractJob` | Extract using a Flow gate |
| `submitFileRun(gateId, files)` | `FlowRun` | Submit files to a Flow gate pipeline |

### WebhooksResource

| Method | Returns | Description |
|--------|---------|-------------|
| `list()` | `ListResponse<WebhookSubscription>` | List webhook subscriptions |
| `list(page, limit)` | `ListResponse<WebhookSubscription>` | List with pagination |
| `get(webhookId)` | `WebhookSubscription` | Get webhook by ID |
| `create(request)` | `WebhookSubscription` | Create a webhook subscription |
| `update(webhookId, request)` | `WebhookSubscription` | Update a webhook subscription |
| `delete(webhookId)` | `void` | Delete a webhook subscription |
| `rotateSecret(webhookId)` | `WebhookSubscription` | Rotate signing secret |
| `test(webhookId)` | `void` | Send a test event |
| `listDeliveries(webhookId)` | `ListResponse<WebhookDelivery>` | List webhook deliveries |
| `listDeliveries(webhookId, limit, offset)` | `ListResponse<WebhookDelivery>` | List with pagination |
| `retryDelivery(webhookId, deliveryId)` | `WebhookDelivery` | Retry a failed delivery |
| `verifySignature(payload, signature, timestamp, secret)` | `void` | Verify webhook signature (throws on failure) |
| `constructEvent(payload)` | `WebhookEvent` | Parse webhook event from payload |
| `constructEvent(payload, signature, timestamp, secret)` | `WebhookEvent` | Verify and parse webhook event |

### FlowResource

| Method | Returns | Description |
|--------|---------|-------------|
| `listGates()` | `ListResponse<FlowGate>` | List all gates |
| `listGates(page, limit, status)` | `ListResponse<FlowGate>` | List gates with filters |
| `getGate(gateId)` | `FlowGate` | Get gate by ID |
| `createGate(request)` | `FlowGate` | Create a new gate |
| `updateGate(gateId, request)` | `FlowGate` | Update a gate |
| `deleteGate(gateId)` | `void` | Delete a gate |
| `updateGateSchema(gateId, schema)` | `FlowGate` | Update gate schema |
| `publishGate(gateId)` | `FlowGate` | Publish a gate |
| `rollbackGate(gateId)` | `FlowGate` | Rollback to previous version |
| `rollbackGate(gateId, versionId)` | `FlowGate` | Rollback to specific version |
| `exportGate(gateId)` | `Map<String, Object>` | Export gate configuration |
| `importGate(data)` | `FlowGate` | Import gate configuration |
| `testGate(gateId, payload)` | `TestGateResult` | Dry-run test (no run created) |
| `validateGate(gateId, request)` | `FlowRun` | Validate and create run |
| `verifyValidation(validationId, payload)` | `Map<String, Object>` | Verify a validation |
| `submitRun(gateId, request)` | `FlowRun` | Submit a run for validation |
| `getRun(runId)` | `FlowRun` | Get run by ID |
| `listRuns()` | `ListResponse<FlowRun>` | List all runs |
| `listRuns(page, limit, status)` | `ListResponse<FlowRun>` | List runs with filters |
| `listRunsByGate(gateId)` | `ListResponse<FlowRun>` | List runs for a gate |
| `listRunsByGate(gateId, page, limit, status)` | `ListResponse<FlowRun>` | List runs for a gate with filters |
| `listActiveRuns()` | `ListResponse<FlowRun>` | List active runs |
| `listActiveRuns(page, limit)` | `ListResponse<FlowRun>` | List active runs with pagination |
| `waitForRun(runId)` | `FlowRun` | Poll until run reaches terminal state |
| `waitForRun(runId, pollIntervalMs, timeoutMs)` | `FlowRun` | Poll with custom settings |
| `listApprovals()` | `ListResponse<FlowApproval>` | List approvals |
| `listApprovals(page, limit, status)` | `ListResponse<FlowApproval>` | List approvals with filters |
| `approve(approvalId)` | `FlowApproval` | Approve a pending approval |
| `approve(approvalId, note)` | `FlowApproval` | Approve with a note |
| `reject(approvalId)` | `FlowApproval` | Reject a pending approval |
| `reject(approvalId, reason)` | `FlowApproval` | Reject with a reason |
| `listDeliveries(runId)` | `ListResponse<FlowDelivery>` | List deliveries for a run |
| `listDeliveries(runId, page, limit)` | `ListResponse<FlowDelivery>` | List deliveries with pagination |
| `resendApprovalEmail(runId)` | `Map<String, Object>` | Resend approval emails |
| `retryDelivery(deliveryId)` | `FlowDelivery` | Retry a failed delivery |
| `getRunPayload(runId)` | `Map<String, Object>` | Get run payload |
| `getRunPayload(runId, field)` | `Map<String, Object>` | Get specific field from payload |
| `getRunChain(correlationId)` | `ListResponse<FlowRun>` | Get runs in correlation chain |
| `getTransaction(transactionId)` | `Map<String, Object>` | Get transaction by ID |

## Examples

See the [`examples/`](./examples) directory for runnable code samples:

- [BasicGenerate.java](./src/main/java/dev/rynko/examples/BasicGenerate.java) - Generate a PDF and wait for completion
- [BatchGenerate.java](./src/main/java/dev/rynko/examples/BatchGenerate.java) - Generate multiple documents
- [WebhookHandler.java](./src/main/java/dev/rynko/examples/WebhookHandler.java) - Spring Boot webhook handler
- [ErrorHandling.java](./src/main/java/dev/rynko/examples/ErrorHandling.java) - Handle API errors
- [FlowSubmitAndWait.java](./src/main/java/dev/rynko/examples/FlowSubmitAndWait.java) - Submit a run and wait for validation
- [FlowApprovalWorkflow.java](./src/main/java/dev/rynko/examples/FlowApprovalWorkflow.java) - Programmatic approval automation

For complete project templates with full setup, see the [developer-resources](https://github.com/rynko-dev/developer-resources) repository.

## License

MIT License - see [LICENSE](LICENSE) for details.

## Support

- **Documentation**: https://docs.rynko.dev/sdk/java
- **API Reference**: https://docs.rynko.dev/api
- **Examples**: https://github.com/rynko-dev/developer-resources
- **GitHub Issues**: https://github.com/rynko-dev/sdk-java/issues
- **Email**: support@rynko.dev
