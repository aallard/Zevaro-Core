# Zevaro-Core CONVENTIONS

> Core backend service for Zevaro - Continuous Outcome Engineering Platform

---

## Service Overview

| Property | Value |
|----------|-------|
| Service Name | zevaro-core |
| Port (Backend) | 8080 |
| Base Package | `ai.zevaro.core` |
| Database | PostgreSQL (shared `zevaro` database) |
| Schema | `core` |

---

## Tech Stack

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 21 | LTS, virtual threads enabled |
| Spring Boot | 3.3.x | Latest stable |
| Spring Security | 6.x | JWT + OAuth2 |
| PostgreSQL | 16 | Primary database |
| Redis | 7.x | Sessions, caching |
| Kafka | 3.x | Event publishing |
| Maven | 3.9+ | Build tool |

---

## Project Structure

```
zevaro-core/
├── src/main/java/ai/zevaro/core/
│   ├── ZevaroCoreApplication.java
│   ├── config/
│   │   ├── AppConstants.java          # All constants here, NO magic numbers
│   │   ├── SecurityConfig.java
│   │   ├── JpaConfig.java
│   │   ├── RedisConfig.java
│   │   ├── KafkaConfig.java
│   │   └── WebConfig.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── TenantContext.java
│   │   ├── TenantFilter.java
│   │   └── CurrentUser.java
│   ├── domain/
│   │   ├── tenant/
│   │   ├── user/
│   │   ├── team/
│   │   ├── outcome/
│   │   ├── hypothesis/
│   │   ├── decision/
│   │   ├── stakeholder/
│   │   └── experiment/
│   ├── api/v1/
│   │   ├── dto/
│   │   └── mapper/
│   ├── event/
│   └── exception/
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
├── src/test/java/
├── pom.xml
├── Dockerfile
└── CONVENTIONS.md
```

---

## Domain Package Structure

Each domain follows this pattern:

```
domain/outcome/
├── Outcome.java              # Entity
├── OutcomeStatus.java        # Enum (if applicable)
├── OutcomeRepository.java    # Spring Data JPA
├── OutcomeService.java       # Business logic
└── OutcomeController.java    # REST endpoints
```

---

## API Paths

```
Base URL: /api/v1

Authentication:
  POST   /api/v1/auth/login
  POST   /api/v1/auth/register
  POST   /api/v1/auth/refresh
  POST   /api/v1/auth/logout

Tenants:
  GET    /api/v1/tenants/current
  PUT    /api/v1/tenants/current

Users:
  GET    /api/v1/users
  POST   /api/v1/users
  GET    /api/v1/users/{id}
  PUT    /api/v1/users/{id}
  DELETE /api/v1/users/{id}
  GET    /api/v1/users/me

Teams:
  GET    /api/v1/teams
  POST   /api/v1/teams
  GET    /api/v1/teams/{id}
  PUT    /api/v1/teams/{id}
  DELETE /api/v1/teams/{id}
  POST   /api/v1/teams/{id}/members
  DELETE /api/v1/teams/{id}/members/{userId}

Outcomes:
  GET    /api/v1/outcomes
  POST   /api/v1/outcomes
  GET    /api/v1/outcomes/{id}
  PUT    /api/v1/outcomes/{id}
  DELETE /api/v1/outcomes/{id}
  POST   /api/v1/outcomes/{id}/validate
  POST   /api/v1/outcomes/{id}/invalidate

Hypotheses:
  GET    /api/v1/hypotheses
  POST   /api/v1/hypotheses
  GET    /api/v1/hypotheses/{id}
  PUT    /api/v1/hypotheses/{id}
  DELETE /api/v1/hypotheses/{id}
  POST   /api/v1/hypotheses/{id}/transition

Decisions:
  GET    /api/v1/decisions
  POST   /api/v1/decisions
  GET    /api/v1/decisions/{id}
  PUT    /api/v1/decisions/{id}
  DELETE /api/v1/decisions/{id}
  POST   /api/v1/decisions/{id}/resolve
  POST   /api/v1/decisions/{id}/escalate
  GET    /api/v1/decisions/{id}/comments
  POST   /api/v1/decisions/{id}/comments
  GET    /api/v1/decisions/queue
  GET    /api/v1/decisions/my-pending

Stakeholders:
  GET    /api/v1/stakeholders
  POST   /api/v1/stakeholders
  GET    /api/v1/stakeholders/{id}
  PUT    /api/v1/stakeholders/{id}
  DELETE /api/v1/stakeholders/{id}

Experiments:
  GET    /api/v1/experiments
  POST   /api/v1/experiments
  GET    /api/v1/experiments/{id}
  PUT    /api/v1/experiments/{id}
  POST   /api/v1/experiments/{id}/start
  POST   /api/v1/experiments/{id}/conclude
```

---

## Security Rules

### RBAC Roles

| Role | Description |
|------|-------------|
| OWNER | Tenant owner, full access |
| ADMIN | Administrator, manage users/teams/settings |
| PRODUCT_LEAD | Manage outcomes, hypotheses, decisions |
| TEAM_MEMBER | Standard member, limited create/edit |
| STAKEHOLDER | External, can view/resolve assigned decisions |
| VIEWER | Read-only access |

### Controller Authorization

**CRITICAL:** All controllers must use proper authorization:

```java
// Correct - role OR specific permission
@PreAuthorize("hasRole('ADMIN') or hasAuthority('outcome:read')")

// WRONG - never just hasAuthority alone for admin functions
@PreAuthorize("hasAuthority('outcome:read')")  // DON'T DO THIS
```

---

## Entity Conventions

### Base Entity

All entities extend:

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

### Tenant Isolation

**CRITICAL:** Every query must filter by tenant:

```java
@Repository
public interface OutcomeRepository extends JpaRepository<Outcome, UUID> {
    
    // Always include tenantId
    List<Outcome> findByTenantId(UUID tenantId);
    
    Optional<Outcome> findByIdAndTenantId(UUID id, UUID tenantId);
    
    // Use @Query for complex queries
    @Query("SELECT o FROM Outcome o WHERE o.tenantId = :tenantId AND o.status = :status")
    List<Outcome> findByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") OutcomeStatus status);
}
```

---

## DTO Conventions

```java
// Request DTO - for incoming data
public record CreateOutcomeRequest(
    @NotBlank String title,
    String description,
    String successCriteria,
    UUID teamId,
    LocalDate targetDate
) {}

// Response DTO - for outgoing data
public record OutcomeResponse(
    UUID id,
    String title,
    String description,
    String successCriteria,
    OutcomeStatus status,
    LocalDate targetDate,
    Instant validatedAt,
    UserSummary owner,
    TeamSummary team,
    Instant createdAt,
    Instant updatedAt
) {}

// Summary DTO - for nested references
public record OutcomeSummary(
    UUID id,
    String title,
    OutcomeStatus status
) {}
```

---

## Kafka Topics

Published by this service:

```
zevaro.core.outcome.created
zevaro.core.outcome.updated
zevaro.core.outcome.validated
zevaro.core.outcome.invalidated
zevaro.core.hypothesis.created
zevaro.core.hypothesis.updated
zevaro.core.hypothesis.status-changed
zevaro.core.decision.created
zevaro.core.decision.updated
zevaro.core.decision.resolved
zevaro.core.decision.escalated
zevaro.core.user.created
zevaro.core.team.updated
```

---

## Configuration

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: zevaro-core
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/zevaro
    username: ${DB_USER:zevaro}
    password: ${DB_PASSWORD:zevaro}
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:update}  # 'update' for dev, 'validate' for prod
    properties:
      hibernate:
        default_schema: core
        format_sql: true
  redis:
    host: ${REDIS_HOST:localhost}
    port: 6379
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here}
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days

zevaro:
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3000,http://localhost:5000}
```

### application-dev.yml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  flyway:
    enabled: false  # CRITICAL: No Flyway in development

logging:
  level:
    ai.zevaro: DEBUG
    org.springframework.security: DEBUG
```

---

## Constants (AppConstants.java)

**CRITICAL:** No magic numbers anywhere. All constants go here:

```java
public final class AppConstants {
    private AppConstants() {}
    
    // Timeouts
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    public static final int DECISION_SLA_DEFAULT_HOURS = 24;
    public static final int ESCALATION_THRESHOLD_HOURS = 48;
    
    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // Decision priorities SLA (hours)
    public static final int SLA_BLOCKING = 4;
    public static final int SLA_HIGH = 8;
    public static final int SLA_NORMAL = 24;
    public static final int SLA_LOW = 72;
    
    // Validation
    public static final int TITLE_MAX_LENGTH = 500;
    public static final int DESCRIPTION_MAX_LENGTH = 10000;
}
```

---

## Error Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403)
            .body(new ErrorResponse("FORBIDDEN", "Access denied"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        return ResponseEntity.status(400)
            .body(new ErrorResponse("VALIDATION_ERROR", String.join(", ", errors)));
    }
}

public record ErrorResponse(String code, String message) {}
```

---

## Development Workflow

### Local Startup

```bash
# Start dependencies
docker-compose up -d postgres redis kafka

# Run service
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Verify health
curl http://localhost:8080/actuator/health
```

### Commit Rules

**CRITICAL:** Commit after EVERY fix immediately. Each fix = one commit. Don't batch.

```bash
git add -A && git commit -m "feat: add outcome CRUD endpoints"
git add -A && git commit -m "fix: tenant isolation in decision query"
```

### 30-Second Rule

**CRITICAL:** Never wait silently. If a command hangs > 30 seconds:
1. Check logs
2. Report the issue
3. Don't keep waiting

---

## Inter-Service Communication

### To Analytics Service (8081)

```java
@FeignClient(name = "zevaro-analytics", url = "${services.analytics.url:http://localhost:8081}")
public interface AnalyticsClient {
    @PostMapping("/api/v1/metrics/decision-resolved")
    void recordDecisionResolved(@RequestBody DecisionMetricEvent event);
}
```

### To Integrations Service (8082)

```java
@FeignClient(name = "zevaro-integrations", url = "${services.integrations.url:http://localhost:8082}")
public interface IntegrationsClient {
    @PostMapping("/api/v1/sync/jira")
    void syncToJira(@RequestBody JiraSyncRequest request);
}
```

---

## Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OutcomeControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;

    @Test
    void createOutcome_ValidRequest_Returns201() throws Exception {
        var request = """
            {
                "title": "Increase user activation",
                "description": "Improve day-1 activation rate",
                "successCriteria": "30% activation within 24h"
            }
            """;

        mockMvc.perform(post("/api/v1/outcomes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Increase user activation"));
    }
}
```

---

## Docker

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/zevaro-core-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Checklist for New Features

- [ ] Entity created with proper tenant isolation
- [ ] Repository with tenant-filtered queries
- [ ] Service with business logic
- [ ] Controller with proper @PreAuthorize
- [ ] Request/Response DTOs
- [ ] Mapper (MapStruct or manual)
- [ ] Kafka event published on state change
- [ ] Tests written
- [ ] Committed immediately after working

---

## Kafka Integration Requirements

> **CRITICAL:** These settings are MANDATORY. A 278GB log file incident occurred due to improper Kafka configuration.

### The 278GB Log Incident (January 2026)

**Root Cause:** When Kafka is unavailable, Spring Kafka's default retry settings (50ms backoff) combined with multiple consumer threads created a log flooding scenario:
- 3 Kafka listeners × 3 concurrent threads = 9 retry loops
- Each loop logging at 50ms intervals = ~180 log entries/second
- Result: 278GB log file in hours, disk full, service crash

### Mandatory Producer Settings (Zevaro-Core)

```yaml
spring:
  kafka:
    enabled: ${KAFKA_ENABLED:true}
    producer:
      properties:
        reconnect.backoff.ms: 1000        # 1 second (NOT default 50ms)
        reconnect.backoff.max.ms: 60000   # 60 seconds max
        retry.backoff.ms: 1000
        request.timeout.ms: 30000
        delivery.timeout.ms: 120000
```

### Circuit Breaker Pattern (KafkaProducerService)

**CRITICAL:** Never use KafkaTemplate directly. Always use KafkaProducerService:

```java
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerService {
    // Circuit breaker: opens after 5 failures, resets after 5 minutes
    // Rate-limited logging: max 1 log per minute when circuit is open
    // Tracks dropped event count for monitoring
}
```

### Mandatory Consumer Settings (Zevaro-Analytics)

```yaml
spring:
  kafka:
    enabled: ${KAFKA_ENABLED:true}
    consumer:
      properties:
        reconnect.backoff.ms: 1000
        reconnect.backoff.max.ms: 60000
        retry.backoff.ms: 1000
    listener:
      concurrency: 1  # CRITICAL: Reduced from 3 to prevent 9 retry loops
```

### Error Handler with Exponential Backoff

```java
factory.setCommonErrorHandler(new DefaultErrorHandler(
    new ExponentialBackOff(1000L, 2.0)  // 1s, 2s, 4s, 8s...
));
```

### Logback Configuration (MANDATORY)

Both services MUST have a separate Kafka log file with strict size limits:

```xml
<!-- Separate Kafka log file with strict size limits -->
<appender name="KAFKA_RATE_LIMITED" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_FILE}-kafka.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
        <maxFileSize>10MB</maxFileSize>
        <maxHistory>3</maxHistory>
        <totalSizeCap>50MB</totalSizeCap>
    </rollingPolicy>
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>WARN</level>
    </filter>
</appender>

<!-- Kafka client loggers - ERROR only, separate file -->
<logger name="org.apache.kafka" level="ERROR" additivity="false">
    <appender-ref ref="KAFKA_RATE_LIMITED"/>
</logger>
```

### KAFKA_ENABLED Toggle

For local development without Kafka:

```bash
export KAFKA_ENABLED=false
./mvnw spring-boot:run
```

When disabled:
- **Core:** KafkaProducerServiceNoOp drops all events silently
- **Analytics:** @ConditionalOnProperty prevents consumer bean creation

### Checklist for Kafka Changes

- [ ] Never use KafkaTemplate directly (use KafkaProducerService)
- [ ] Verify reconnect.backoff.ms >= 1000 (NOT 50ms default)
- [ ] Verify listener concurrency = 1 for consumers
- [ ] Confirm logback-spring.xml has separate Kafka appender
- [ ] Test with Kafka DOWN before deploying
- [ ] Monitor dropped event count in production
