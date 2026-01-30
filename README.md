# Rynko Java SDK

Official Java SDK for [Rynko](https://rynko.dev) - the document generation platform with unified template design for PDF and Excel documents.

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
- [Templates](#templates)
  - [List Templates](#list-templates)
  - [Get Template Details](#get-template-details)
- [Webhooks](#webhooks)
  - [List Webhooks](#list-webhooks)
  - [Verify Webhook Signatures](#verify-webhook-signatures)
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
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation 'dev.rynko:sdk:1.0.0'
```

### Gradle (Kotlin DSL)

```kotlin
implementation("dev.rynko:sdk:1.0.0")
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
- **Workspace support** - Generate documents in specific workspaces
- **Webhook verification** - Secure HMAC signature verification for incoming webhooks
- **Polling utility** - Built-in `waitForCompletion()` method with configurable timeout

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
System.out.println("Team: " + user.getTeamName());
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
        .format("xlsx")
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
    .format("pdf")  // "pdf", "xlsx", or "csv"

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
    .workspaceId("ws_abc123")            // Generate in specific workspace
    .webhookUrl("https://your-app.com/webhooks/document-ready")
    .metadata(Map.of(                     // Custom metadata (passed to webhook)
        "orderId", "ORD-12345",
        "userId", "user_abc"
    ))
    .useDraft(false)                      // Use draft template version (for testing)
    .useCredit(false)                     // Force use of purchased credits
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
    System.out.println("File size: " + completed.getFileSize() + " bytes");
    System.out.println("Expires at: " + completed.getDownloadUrlExpiresAt());
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

System.out.println("Template: " + job.getTemplateName());
System.out.println("Format: " + job.getFormat());
System.out.println("Created: " + job.getCreatedAt());

if (job.isCompleted()) {
    System.out.println("Download URL: " + job.getDownloadUrl());
    System.out.println("File size: " + job.getFileSize());
    System.out.println("URL expires: " + job.getDownloadUrlExpiresAt());
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
    System.out.println(job.getJobId() + ": " + job.getStatus() + " - " + job.getTemplateName());
}

// Check for more pages
if (result.hasMore()) {
    int nextPage = result.getMeta().getPage() + 1;
    ListResponse<GenerateResult> nextResult = client.documents().list(nextPage, 20);
}

// Filter by template
ListResponse<GenerateResult> invoiceJobs = client.documents().list(1, 20, "tmpl_invoice", null);

// Filter by workspace
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

// Filter by type
ListResponse<Template> pdfTemplates = client.templates().list(1, 20, "pdf");
ListResponse<Template> excelTemplates = client.templates().list(1, 20, "excel");

// Or use convenience methods
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

Webhook subscriptions are managed through the [Rynko Dashboard](https://app.rynko.dev). The SDK provides read-only access to view webhooks and utilities for signature verification.

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
                handleDocumentGenerated(event);
                break;
            case "document.failed":
                handleDocumentFailed(event);
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

private void handleDocumentGenerated(WebhookEvent event) {
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) event.getData();

    String jobId = (String) data.get("jobId");
    String downloadUrl = (String) data.get("downloadUrl");
    String templateId = (String) data.get("templateId");

    System.out.println("Document " + jobId + " ready: " + downloadUrl);
    // Download or process the document
}

private void handleDocumentFailed(WebhookEvent event) {
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) event.getData();

    String jobId = (String) data.get("jobId");
    String error = (String) data.get("error");
    String errorCode = (String) data.get("errorCode");

    System.err.println("Document " + jobId + " failed: " + error);
    // Handle failure (retry, notify user, etc.)
}

private void handleDocumentDownloaded(WebhookEvent event) {
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) event.getData();

    String jobId = (String) data.get("jobId");
    System.out.println("Document " + jobId + " was downloaded");
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
| `document.generated` | Document successfully generated | `jobId`, `templateId`, `format`, `downloadUrl`, `fileSize` |
| `document.failed` | Document generation failed | `jobId`, `templateId`, `error`, `errorCode` |
| `document.downloaded` | Document was downloaded | `jobId`, `downloadedAt` |

#### Webhook Headers

Rynko sends these headers with each webhook request:

| Header | Description |
|--------|-------------|
| `X-Rynko-Signature` | HMAC-SHA256 signature (format: `v1=<hex>`) |
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
    .timeoutMs(60000)  // 60 seconds
    .build();

Rynko client = new Rynko(config);
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
| `templates()` | `TemplatesResource` | Access template operations |
| `webhooks()` | `WebhooksResource` | Access webhook operations |

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
| `delete(jobId)` | `void` | Delete a generated document |
| `download(url)` | `byte[]` | Download document as bytes |

### TemplatesResource

| Method | Returns | Description |
|--------|---------|-------------|
| `list()` | `ListResponse<Template>` | List all templates |
| `list(page, limit)` | `ListResponse<Template>` | List with pagination |
| `list(page, limit, type)` | `ListResponse<Template>` | List with type filter ("pdf" or "excel") |
| `listPdf()` | `ListResponse<Template>` | List PDF templates only |
| `listPdf(page, limit)` | `ListResponse<Template>` | List PDF templates with pagination |
| `listExcel()` | `ListResponse<Template>` | List Excel templates only |
| `listExcel(page, limit)` | `ListResponse<Template>` | List Excel templates with pagination |
| `get(templateId)` | `Template` | Get template by ID (UUID, shortId, or slug) |

### WebhooksResource

| Method | Returns | Description |
|--------|---------|-------------|
| `list()` | `ListResponse<WebhookSubscription>` | List webhook subscriptions |
| `list(page, limit)` | `ListResponse<WebhookSubscription>` | List with pagination |
| `get(webhookId)` | `WebhookSubscription` | Get webhook by ID |
| `verifySignature(payload, signature, timestamp, secret)` | `void` | Verify webhook signature (throws on failure) |
| `constructEvent(payload)` | `WebhookEvent` | Parse webhook event from payload |
| `constructEvent(payload, signature, timestamp, secret)` | `WebhookEvent` | Verify and parse webhook event |

## License

MIT License - see [LICENSE](LICENSE) for details.

## Support

- **Documentation**: https://docs.rynko.dev/sdk/java
- **API Reference**: https://docs.rynko.dev/api
- **GitHub Issues**: https://github.com/rynko-dev/sdk-java/issues
- **Email**: support@rynko.dev
