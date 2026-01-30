# Examples

This directory contains example code demonstrating how to use the Rynko Java SDK.

## Prerequisites

- Java 8+
- Maven 3.6+
- A Rynko API key ([get one here](https://app.rynko.dev/settings/api-keys))
- At least one template created in your workspace

## Example Classes

The examples are located in `src/main/java/dev/rynko/examples/`:

| Example | Description |
|---------|-------------|
| [BasicGenerate.java](../src/main/java/dev/rynko/examples/BasicGenerate.java) | Generate a PDF and wait for completion |
| [BatchGenerate.java](../src/main/java/dev/rynko/examples/BatchGenerate.java) | Generate multiple documents |
| [ErrorHandling.java](../src/main/java/dev/rynko/examples/ErrorHandling.java) | Handle API errors gracefully |
| [WebhookHandler.java](../src/main/java/dev/rynko/examples/WebhookHandler.java) | Spring Boot webhook handler example |

## Running Examples

From the SDK root directory:

```bash
# Build the SDK
mvn clean install

# Run an example
export RYNKO_API_KEY=your_key
mvn exec:java -Dexec.mainClass="dev.rynko.examples.BasicGenerate"
```

Or using the compiled JAR:

```bash
java -cp target/sdk-1.0.0.jar:target/dependency/* dev.rynko.examples.BasicGenerate
```

## Spring Boot Integration

For Spring Boot projects, add the SDK dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.rynko</groupId>
    <artifactId>sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then create a configuration bean:

```java
@Configuration
public class RynkoConfig {
    @Value("${rynko.api-key}")
    private String apiKey;

    @Bean
    public Rynko rynko() {
        return new Rynko(apiKey);
    }
}
```

## More Examples

For complete project templates with full setup, see the [developer-resources](https://github.com/rynko-dev/developer-resources) repository.
