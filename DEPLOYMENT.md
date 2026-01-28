# Java SDK Deployment Guide

This guide covers publishing and maintaining the Rynko Java SDK (`com.rynko:sdk`).

## Prerequisites

- Java 8 or higher
- Maven 3.6+
- Sonatype OSSRH account (for Maven Central)
- GPG key for signing artifacts
- GitHub repository access

## Package Overview

```
sdk-java/
├── src/
│   ├── main/java/com/rynko/
│   │   ├── Rynko.java           # Main client class
│   │   ├── RynkoConfig.java     # Configuration builder
│   │   ├── exceptions/               # Exception classes
│   │   ├── models/                   # Request/response models
│   │   ├── resources/                # API resource classes
│   │   └── utils/                    # HTTP client utilities
│   └── test/java/com/rynko/     # Unit tests
├── pom.xml
├── README.md
└── LICENSE
```

## Build Process

### 1. Install Dependencies

```bash
cd integrations/sdk-java
mvn clean install
```

### 2. Run Tests

```bash
mvn test
```

### 3. Build JAR

```bash
mvn package
```

This creates:
- `target/sdk-1.0.0.jar` - Main JAR
- `target/sdk-1.0.0-sources.jar` - Source JAR
- `target/sdk-1.0.0-javadoc.jar` - Javadoc JAR

## Publishing to Maven Central

### Initial Setup

1. **Create Sonatype OSSRH Account**:
   - Register at https://issues.sonatype.org/
   - Create a new project ticket for `com.rynko` group ID
   - Wait for approval (usually 1-2 business days)

2. **Generate GPG Key**:
   ```bash
   gpg --gen-key
   gpg --list-keys
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

3. **Configure Maven Settings**:
   Create or update `~/.m2/settings.xml`:
   ```xml
   <settings>
     <servers>
       <server>
         <id>ossrh</id>
         <username>your-sonatype-username</username>
         <password>your-sonatype-password</password>
       </server>
     </servers>
     <profiles>
       <profile>
         <id>ossrh</id>
         <activation>
           <activeByDefault>true</activeByDefault>
         </activation>
         <properties>
           <gpg.executable>gpg</gpg.executable>
           <gpg.passphrase>your-gpg-passphrase</gpg.passphrase>
         </properties>
       </profile>
     </profiles>
   </settings>
   ```

### Publishing a New Version

1. **Update Version** in `pom.xml`:
   ```xml
   <version>1.0.1</version>
   ```

2. **Update Changelog**

3. **Build and Test**:
   ```bash
   mvn clean test
   ```

4. **Deploy to Maven Central**:
   ```bash
   mvn clean deploy -P release
   ```

5. **Release from Staging** (if not using auto-release):
   - Log into https://s01.oss.sonatype.org/
   - Find your staging repository
   - Click "Close" then "Release"

6. **Tag Release**:
   ```bash
   git tag sdk-java-v1.0.1
   git push origin main --tags
   ```

### Automated Publishing (CI/CD)

The GitHub Actions workflow at `.github/workflows/publish-sdk-java.yml` handles automated publishing when you push a tag matching `sdk-java-v*`.

**Required Secrets**:
- `OSSRH_USERNAME` - Sonatype OSSRH username
- `OSSRH_PASSWORD` - Sonatype OSSRH password
- `GPG_PRIVATE_KEY` - Base64 encoded GPG private key
- `GPG_PASSPHRASE` - GPG key passphrase

To export your GPG key for CI:
```bash
gpg --export-secret-keys YOUR_KEY_ID | base64 > private-key.txt
```

## Version Management

### Semantic Versioning

- **MAJOR**: Breaking API changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

### Changelog

Maintain `CHANGELOG.md`:

```markdown
# Changelog

## [1.1.0] - 2025-01-20
### Added
- `waitForCompletion()` method for polling job status
- `statusUrl` and `estimatedWaitSeconds` fields in GenerateResult

### Fixed
- Timeout handling in HTTP client

## [1.0.0] - 2025-01-01
### Initial Release
- Document generation (PDF, Excel)
- Template management
- Webhook signature verification
```

## Configuration Requirements

### Environment Variables

Users should set:
```bash
export RYNKO_API_KEY=your_api_key
```

### SDK Configuration

```java
import com.rynko.Rynko;
import com.rynko.RynkoConfig;

// Simple initialization
Rynko client = new Rynko("your-api-key");

// Advanced configuration
RynkoConfig config = RynkoConfig.builder()
    .apiKey(System.getenv("RYNKO_API_KEY"))
    .baseUrl("https://api.rynko.dev/api/v1")
    .timeoutMs(60000)
    .build();
Rynko client = new Rynko(config);
```

## API Compatibility

### Backend Requirements

The SDK requires these Rynko API endpoints:

| Endpoint | SDK Method |
|----------|------------|
| `POST /api/v1/documents/generate` | `client.documents().generate()` |
| `GET /api/v1/documents/jobs/:id` | `client.documents().get()` |
| `GET /api/v1/documents/jobs` | `client.documents().list()` |
| `GET /api/v1/templates` | `client.templates().list()` |
| `GET /api/v1/templates/:id` | `client.templates().get()` |
| `POST /api/v1/webhook-subscriptions` | `client.webhooks().create()` |
| `GET /api/v1/webhook-subscriptions` | `client.webhooks().list()` |
| `DELETE /api/v1/webhook-subscriptions/:id` | `client.webhooks().delete()` |

### Java Version Compatibility

| SDK Version | Java Versions |
|-------------|---------------|
| 1.x | 8, 11, 17, 21 |

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

Create `src/test/resources/test.properties`:
```properties
rynko.api.key=test_api_key
rynko.base.url=http://localhost:3000/api/v1
```

Run integration tests:
```bash
mvn test -P integration
```

### Test Coverage

```bash
mvn jacoco:report
```

Coverage report in `target/site/jacoco/index.html`.

### Manual Testing

```java
import com.rynko.Rynko;
import com.rynko.models.GenerateRequest;
import com.rynko.models.GenerateResult;

public class ManualTest {
    public static void main(String[] args) {
        Rynko client = new Rynko("your_test_key");

        // Queue document generation
        GenerateResult job = client.documents().generate(
            GenerateRequest.builder()
                .templateId("tmpl_test")
                .format("pdf")
                .variable("name", "Test")
                .build()
        );
        System.out.println("Job ID: " + job.getJobId());

        // Wait for completion
        GenerateResult completed = client.documents().waitForCompletion(job.getJobId());
        System.out.println("Download URL: " + completed.getDownloadUrl());
    }
}
```

## Documentation

### Generate Javadoc

```bash
mvn javadoc:javadoc
```

Output in `target/site/apidocs/`.

### README Updates

Update `README.md` with:
- Installation instructions (Maven/Gradle)
- Quick start examples
- API reference
- Error handling
- Spring Boot integration examples

## Troubleshooting

### Common Issues

1. **GPG Signing Failures**
   - Ensure GPG key is not expired
   - Check passphrase is correct
   - Verify key is uploaded to keyserver

2. **Deployment Failures**
   - Verify Sonatype credentials
   - Check version doesn't already exist
   - Ensure all required metadata is in pom.xml

3. **Build Errors**
   - Clear local Maven cache: `rm -rf ~/.m2/repository/com/rynko`
   - Update Maven: `mvn -v`

4. **Runtime Errors**
   - Ensure OkHttp and Jackson dependencies are available
   - Check Java version compatibility

### Support Channels

- GitHub Issues: Report bugs and feature requests
- Email: sdk-support@rynko.dev
- Documentation: https://docs.rynko.dev/sdk/java

## Security

### API Key Handling

- Never commit API keys to source control
- Use environment variables
- Rotate keys if compromised

### Dependency Updates

Regularly update dependencies:
```bash
mvn versions:display-dependency-updates
mvn versions:use-latest-releases
```

### Vulnerability Scanning

```bash
mvn org.owasp:dependency-check-maven:check
```

### Vulnerability Disclosure

Report security issues to: security@rynko.dev

## Framework Examples

### Spring Boot

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

@Service
public class DocumentService {

    private final Rynko rynko;

    public DocumentService(Rynko rynko) {
        this.rynko = rynko;
    }

    public String generateInvoice(Invoice invoice) {
        GenerateResult job = rynko.documents().generate(
            GenerateRequest.builder()
                .templateId("tmpl_invoice")
                .format("pdf")
                .variable("invoiceNumber", invoice.getNumber())
                .build()
        );

        GenerateResult completed = rynko.documents().waitForCompletion(job.getJobId());
        return completed.getDownloadUrl();
    }
}
```

### Micronaut

```java
@Singleton
public class RynkoFactory {

    @Property(name = "rynko.api-key")
    private String apiKey;

    @Singleton
    public Rynko rynko() {
        return new Rynko(apiKey);
    }
}
```

### Quarkus

```java
@ApplicationScoped
public class RynkoProducer {

    @ConfigProperty(name = "rynko.api-key")
    String apiKey;

    @Produces
    @ApplicationScoped
    public Rynko rynko() {
        return new Rynko(apiKey);
    }
}
```
