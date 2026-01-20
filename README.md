# Renderbase Java SDK

Official Java SDK for [Renderbase](https://renderbase.dev) - the document generation platform with unified template design for PDF and Excel documents.

## Requirements

- Java 8 or higher
- Maven or Gradle

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.renderbase</groupId>
    <artifactId>sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation 'com.renderbase:sdk:1.0.0'
```

## Quick Start

```java
import com.renderbase.Renderbase;
import com.renderbase.models.GenerateRequest;
import com.renderbase.models.GenerateResult;

import java.util.HashMap;
import java.util.Map;

public class Example {
    public static void main(String[] args) {
        // Initialize the client
        Renderbase client = new Renderbase("your-api-key");

        // Prepare variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("invoiceNumber", "INV-2026-001");
        variables.put("customerName", "Acme Corporation");
        variables.put("amount", 1250.00);

        // Queue document generation (async operation)
        GenerateResult job = client.documents().generate(
            GenerateRequest.builder()
                .templateId("tmpl_invoice")
                .format("pdf")
                .variables(variables)
                .build()
        );

        System.out.println("Job ID: " + job.getJobId());
        System.out.println("Status: " + job.getStatus());  // "queued"

        // Wait for completion to get download URL
        GenerateResult completed = client.documents().waitForCompletion(job.getJobId());

        if (completed.isCompleted()) {
            System.out.println("Download URL: " + completed.getDownloadUrl());
        } else {
            System.err.println("Job failed");
        }
    }
}
```

## Configuration

### Basic Configuration

```java
// Simple initialization with API key
Renderbase client = new Renderbase("your-api-key");

// With custom base URL
Renderbase client = new Renderbase("your-api-key", "https://api.renderbase.dev/api/v1");
```

### Advanced Configuration

```java
import com.renderbase.RenderbaseConfig;

RenderbaseConfig config = RenderbaseConfig.builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.renderbase.dev/api/v1")
    .timeoutMs(60000)  // 60 seconds
    .build();

Renderbase client = new Renderbase(config);
```

## Usage

### Generating Documents

Document generation is an **asynchronous operation**. When you call `generate()`, the job is queued for processing. Use `waitForCompletion()` to poll until the document is ready.

#### Generate PDF

```java
// Queue document generation
GenerateResult job = client.documents().generate(
    GenerateRequest.builder()
        .templateId("tmpl_invoice")
        .format("pdf")
        .variable("invoiceNumber", "INV-001")
        .variable("customerName", "John Doe")
        .variable("items", Arrays.asList(
            Map.of("name", "Widget", "price", 29.99),
            Map.of("name", "Gadget", "price", 49.99)
        ))
        .filename("invoice-001")
        .build()
);

System.out.println("Job ID: " + job.getJobId());
System.out.println("Status: " + job.getStatus());  // "queued"

// Wait for completion
GenerateResult completed = client.documents().waitForCompletion(job.getJobId());
System.out.println("Download: " + completed.getDownloadUrl());
```

#### Generate Excel

```java
GenerateResult job = client.documents().generate(
    GenerateRequest.builder()
        .templateId("tmpl_report")
        .format("xlsx")
        .variables(reportData)
        .build()
);

GenerateResult completed = client.documents().waitForCompletion(job.getJobId());
```

#### Custom Polling Settings

```java
// Wait with custom poll interval (2 seconds) and timeout (60 seconds)
GenerateResult completed = client.documents().waitForCompletion(
    job.getJobId(),
    2000,   // pollIntervalMs
    60000   // timeoutMs
);
```

#### Download Document

```java
// After waiting for completion
byte[] documentBytes = client.documents().download(completed.getDownloadUrl());

// Save to file
try (FileOutputStream fos = new FileOutputStream("document.pdf")) {
    fos.write(documentBytes);
}
```

### Working with Templates

#### List Templates

```java
import com.renderbase.models.ListResponse;
import com.renderbase.models.Template;

ListResponse<Template> templates = client.templates().list();

for (Template template : templates.getData()) {
    System.out.println(template.getName() + " (" + template.getId() + ")");
    System.out.println("  Type: " + template.getType());
    System.out.println("  Variables: " + template.getVariables().size());
}

// Check for more pages
if (templates.hasMore()) {
    int nextPage = templates.getMeta().getPage() + 1;
    ListResponse<Template> nextTemplates = client.templates().list(nextPage, 10);
}
```

#### Get Template Details

```java
// By ID
Template template = client.templates().get("550e8400-e29b-41d4-a716-446655440000");

// By short ID
Template template = client.templates().getByShortId("tmpl_abc123");

// By slug
Template template = client.templates().getBySlug("invoice-template");

// Access template variables
for (TemplateVariable variable : template.getVariables()) {
    System.out.println(variable.getName() + " (" + variable.getType() + ")");
    System.out.println("  Required: " + variable.isRequired());
    if (variable.getDefaultValue() != null) {
        System.out.println("  Default: " + variable.getDefaultValue());
    }
}
```

### Webhook Handling

#### Verify Webhook Signatures

```java
import com.renderbase.resources.WebhooksResource.WebhookEvent;
import com.renderbase.exceptions.WebhookSignatureException;

// In your webhook endpoint handler
public void handleWebhook(HttpServletRequest request) {
    String payload = readRequestBody(request);
    String signature = request.getHeader("X-Renderbase-Signature");
    String timestamp = request.getHeader("X-Renderbase-Timestamp");
    String webhookSecret = "whsec_your_webhook_secret";

    try {
        // Verify and construct event
        WebhookEvent event = client.webhooks().constructEvent(
            payload, signature, timestamp, webhookSecret
        );

        // Handle different event types
        switch (event.getType()) {
            case "document.completed":
                handleDocumentCompleted(event);
                break;
            case "document.failed":
                handleDocumentFailed(event);
                break;
            case "batch.completed":
                handleBatchCompleted(event);
                break;
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }
    } catch (WebhookSignatureException e) {
        // Invalid signature - reject the webhook
        response.setStatus(401);
        return;
    }
}
```

#### Manage Webhook Subscriptions

```java
import com.renderbase.resources.WebhooksResource.*;

// Create a webhook subscription
WebhookSubscription webhook = client.webhooks().create(
    new CreateWebhookRequest(
        "https://your-server.com/webhooks/renderbase",
        new String[]{"document.completed", "document.failed", "batch.completed"}
    )
);

System.out.println("Webhook ID: " + webhook.getId());
System.out.println("Secret: " + webhook.getSecret());  // Save this securely!

// List webhooks
ListResponse<WebhookSubscription> webhooks = client.webhooks().list();

// Update webhook
UpdateWebhookRequest update = new UpdateWebhookRequest();
update.setActive(false);
client.webhooks().update(webhook.getId(), update);

// Delete webhook
client.webhooks().delete(webhook.getId());
```

## Error Handling

```java
import com.renderbase.exceptions.RenderbaseException;

try {
    GenerateResult result = client.documents().generate(request);
} catch (RenderbaseException e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("Error Code: " + e.getCode());
    System.err.println("Status Code: " + e.getStatusCode());

    // Handle specific error codes
    if ("ERR_TMPL_001".equals(e.getCode())) {
        System.err.println("Template not found");
    } else if ("ERR_QUOTA_001".equals(e.getCode())) {
        System.err.println("Quota exceeded - please upgrade your plan");
    }
}
```

## Thread Safety

The `Renderbase` client is thread-safe and can be shared across multiple threads. We recommend creating a single instance and reusing it throughout your application.

```java
// Application-wide singleton
public class RenderbaseClient {
    private static final Renderbase INSTANCE = new Renderbase(
        System.getenv("RENDERBASE_API_KEY")
    );

    public static Renderbase getInstance() {
        return INSTANCE;
    }
}
```

## Spring Boot Integration

```java
@Configuration
public class RenderbaseConfiguration {

    @Value("${renderbase.api-key}")
    private String apiKey;

    @Value("${renderbase.base-url:https://api.renderbase.dev/api/v1}")
    private String baseUrl;

    @Bean
    public Renderbase renderbase() {
        return new Renderbase(apiKey, baseUrl);
    }
}

@Service
public class DocumentService {

    private final Renderbase renderbase;

    public DocumentService(Renderbase renderbase) {
        this.renderbase = renderbase;
    }

    public String generateInvoice(Invoice invoice) {
        // Queue document generation
        GenerateResult job = renderbase.documents().generate(
            GenerateRequest.builder()
                .templateId("tmpl_invoice")
                .format("pdf")
                .variable("invoiceNumber", invoice.getNumber())
                .variable("customerName", invoice.getCustomer().getName())
                .variable("lineItems", invoice.getLineItems())
                .build()
        );

        // Wait for completion
        GenerateResult completed = renderbase.documents().waitForCompletion(job.getJobId());

        if (completed.isFailed()) {
            throw new RuntimeException("Document generation failed");
        }

        return completed.getDownloadUrl();
    }
}
```

## API Reference

### Renderbase

| Method | Description |
|--------|-------------|
| `documents()` | Access document generation operations |
| `templates()` | Access template operations |
| `webhooks()` | Access webhook operations |

### DocumentsResource

| Method | Description |
|--------|-------------|
| `generate(request)` | Queue document generation (returns job with `queued` status) |
| `get(jobId)` | Get a document generation job by ID |
| `list()` | List document generation jobs |
| `list(page, limit)` | List with pagination |
| `list(page, limit, templateId, workspaceId)` | List with filters |
| `waitForCompletion(jobId)` | Poll until job completes (default: 1s interval, 30s timeout) |
| `waitForCompletion(jobId, pollIntervalMs, timeoutMs)` | Poll with custom settings |
| `delete(jobId)` | Delete a generated document |
| `download(url)` | Download document as bytes |

### TemplatesResource

| Method | Description |
|--------|-------------|
| `list()` | List all templates |
| `list(page, limit)` | List with pagination |
| `list(page, limit, type)` | List with filtering |
| `get(templateId)` | Get template by ID/shortId/slug |

### WebhooksResource

| Method | Description |
|--------|-------------|
| `list()` | List webhook subscriptions |
| `get(webhookId)` | Get webhook by ID |
| `create(request)` | Create webhook subscription |
| `update(webhookId, request)` | Update webhook |
| `delete(webhookId)` | Delete webhook |
| `verifySignature(...)` | Verify webhook signature |
| `constructEvent(payload)` | Parse webhook event |

## License

MIT License - see [LICENSE](LICENSE) for details.

## Support

- Documentation: https://docs.renderbase.dev
- API Reference: https://docs.renderbase.dev/api
- Email: support@renderbase.dev
