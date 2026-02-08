# Zevaro-Core Comprehensive Audit

**Audit Date:** 2026-02-08
**Codebase Location:** `/Users/adamallard/Documents/GitHub/Zevaro-Core`

---

## 1. Project Overview

**Zevaro-Core** is the backend API service for the Zevaro platform -- a **Continuous Outcome Engineering** system. It provides a structured workflow for managing outcomes (goals), hypotheses (beliefs about how to achieve outcomes), experiments (tests to validate hypotheses), and decisions (choices that must be made to move forward). The platform is designed around product teams that track work from strategic outcome definition through hypothesis testing to validation.

### Tech Stack

| Component         | Technology                          |
| ----------------- | ----------------------------------- |
| Language          | Java 21 (pom.xml declares 21)       |
| Framework         | Spring Boot 3.3.5                   |
| Build Tool        | Maven (with Maven Wrapper)          |
| Database          | PostgreSQL 16                       |
| Cache             | Redis 7                             |
| Messaging         | Apache Kafka 3.6 (KRaft mode)       |
| ORM               | Hibernate / Spring Data JPA         |
| Auth              | JWT (jjwt 0.12.6)                   |
| API Docs          | SpringDoc OpenAPI 2.6.0             |
| Rate Limiting     | Bucket4j 8.10.1                     |
| Boilerplate       | Lombok 1.18.42                      |
| Container Runtime | Docker (multi-stage, Eclipse Temurin)|

### Artifact

- **Group:** `ai.zevaro`
- **Artifact:** `zevaro-core`
- **Version:** `0.0.1-SNAPSHOT`
- **Port:** 8080
- **API Prefix:** `/api/v1/`

---

## 2. Architecture

### Package Structure

```
ai.zevaro.core
+-- ZevaroCoreApplication.java          # Spring Boot entry point
+-- config/                             # Configuration classes
|   +-- AppConstants.java               # SLA defaults, pagination limits
|   +-- JpaConfig.java                  # Enables JPA Auditing
|   +-- KafkaConfig.java                # Kafka producer + topic definitions
|   +-- KafkaProducerService.java       # Circuit breaker-protected Kafka sender
|   +-- KafkaProducerServiceNoOp.java   # No-op Kafka for dev without Kafka
|   +-- KafkaTopics.java                # Topic name constants
|   +-- OpenApiConfig.java              # Swagger/OpenAPI setup
|   +-- SecurityConfig.java             # Spring Security filter chain
|   +-- WebConfig.java                  # CORS configuration
+-- domain/                             # Domain-driven packages (one per aggregate)
|   +-- audit/                          # Audit logging
|   +-- auth/                           # Authentication (login, register, refresh)
|   +-- common/                         # BaseEntity superclass
|   +-- decision/                       # Decision management (the core domain)
|   +-- experiment/                     # Experiment tracking
|   +-- hypothesis/                     # Hypothesis lifecycle
|   +-- outcome/                        # Outcome + Key Result management
|   +-- project/                        # Project container
|   +-- queue/                          # Decision queues
|   +-- rbac/                           # Roles, Permissions, data loaders
|   +-- stakeholder/                    # Stakeholder management + scorecards
|   +-- team/                           # Team + Team Members
|   +-- tenant/                         # Tenant (multi-tenancy root)
|   +-- user/                           # User management
+-- event/                              # Domain event system
|   +-- DomainEvent.java                # Event interface
|   +-- BaseEvent.java                  # Abstract base event
|   +-- EventPublisher.java             # Publishes events to Kafka
|   +-- decision/                       # Decision-specific events
|   +-- hypothesis/                     # Hypothesis-specific events
|   +-- outcome/                        # Outcome-specific events
+-- exception/                          # Global exception handling
|   +-- ApiError.java
|   +-- GlobalExceptionHandler.java
|   +-- ResourceNotFoundException.java
+-- security/                           # Security infrastructure
|   +-- CurrentUser.java                # @CurrentUser annotation
|   +-- JwtAuthenticationFilter.java    # JWT token extraction filter
|   +-- JwtTokenProvider.java           # JWT generation/validation
|   +-- RateLimiterService.java         # Bucket4j rate limiter
|   +-- RequestContextFilter.java       # IP/UA/RequestID capture
|   +-- TenantContext.java              # ThreadLocal tenant ID holder
|   +-- UserPrincipal.java              # Spring Security UserDetails impl
+-- util/
    +-- SlugGenerator.java              # URL-safe slug generation
```

### Layering Pattern

Each domain package follows a consistent layered pattern:

| Layer      | Naming Convention       | Purpose                                  |
| ---------- | ----------------------- | ---------------------------------------- |
| Entity     | `Foo.java`              | JPA entity with Lombok annotations       |
| Repository | `FooRepository.java`    | Spring Data JPA interface                |
| Service    | `FooService.java`       | Business logic, transactions             |
| Controller | `FooController.java`    | REST endpoints with `@PreAuthorize`      |
| Mapper     | `FooMapper.java`        | Entity-to-DTO mapping (manual, no MapStruct) |
| DTOs       | `dto/` subdirectory     | Java records for requests/responses      |

### Multi-Tenancy Model

The application uses a **shared database, shared schema** multi-tenancy model. Every entity includes a `tenant_id` column, and all queries are scoped by tenant ID extracted from the JWT token. Tenant isolation is enforced at the service/repository layer, not via Hibernate filters.

---

## 3. Dependencies

### Spring Boot Starters

| Starter                          | Purpose                                        |
| -------------------------------- | ---------------------------------------------- |
| `spring-boot-starter-web`        | REST API, embedded Tomcat                      |
| `spring-boot-starter-data-jpa`   | JPA/Hibernate ORM                              |
| `spring-boot-starter-security`   | Spring Security framework                      |
| `spring-boot-starter-validation` | Bean validation (`@Valid`, `@NotBlank`, etc.)   |
| `spring-boot-starter-actuator`   | Health checks, metrics (health, info, metrics)  |
| `spring-boot-starter-data-redis` | Redis cache support                            |

### Third-Party

| Dependency                     | Version | Purpose                                  |
| ------------------------------ | ------- | ---------------------------------------- |
| `spring-kafka`                 | (BOM)   | Kafka producer integration               |
| `postgresql`                   | (BOM)   | PostgreSQL JDBC driver (runtime)         |
| `jjwt-api/impl/jackson`        | 0.12.6  | JWT token creation/validation            |
| `springdoc-openapi-starter`     | 2.6.0   | Swagger UI + OpenAPI 3 spec generation   |
| `bucket4j-core`                | 8.10.1  | In-memory rate limiting                  |
| `lombok`                       | 1.18.42 | Boilerplate reduction                    |

### Build Plugins

- **maven-compiler-plugin** with explicit Lombok annotation processor paths (required for Java 22+)
- **spring-boot-maven-plugin** with Lombok exclusion from final JAR

---

## 4. Domain Model

### Entity Relationship Overview

```
Tenant (root)
  +-- User (belongs to Tenant, has one Role)
  +-- Project (belongs to Tenant, has owner User)
  |     +-- Team (belongs to Project, has lead User)
  |     |     +-- TeamMember (join: Team + User + TeamMemberRole)
  |     +-- Outcome (belongs to Project, Team, Owner User)
  |     |     +-- KeyResult (belongs to Outcome)
  |     |     +-- Hypothesis (belongs to Outcome, Project, Owner User)
  |     |           +-- Experiment (belongs to Hypothesis, Project, Owner User)
  |     +-- Decision (belongs to Project, Team, Outcome, Hypothesis, Queue, Stakeholder)
  |           +-- DecisionVote (belongs to Decision + User)
  |           +-- DecisionComment (belongs to Decision + User, self-referencing parent)
  +-- DecisionQueue (belongs to Tenant)
  +-- Stakeholder (belongs to Tenant, Project; optionally linked to User)
  |     +-- StakeholderResponse (join: Stakeholder + Decision)
  +-- Role (optionally scoped to Tenant)
  |     +-- Permission (many-to-many via role_permissions)
  +-- AuditLog (belongs to Tenant)
```

### All Entities in Detail

#### BaseEntity (MappedSuperclass)
- `id` (UUID, auto-generated)
- `tenantId` (UUID, not null)
- `createdAt` (Instant, JPA auditing, not updatable)
- `updatedAt` (Instant, JPA auditing)

**Note:** Not all entities extend BaseEntity. Many define their own id/tenantId/timestamps directly.

#### Tenant
- **Table:** `tenants`
- **Fields:** id, name (unique), slug (unique), logoUrl, settings (text/JSON), status (ACTIVE, SUSPENDED, TRIAL, CANCELLED), createdAt, updatedAt
- **Does NOT extend BaseEntity** (standalone entity)

#### User
- **Table:** `users`
- **Fields:** id, tenantId, email, passwordHash, firstName, lastName, title, department, avatarUrl, role (ManyToOne -> Role, EAGER), manager (ManyToOne -> User, LAZY), active, lastLoginAt, refreshToken, createdAt, updatedAt
- **Unique Constraint:** (tenant_id, email)
- **Indexes:** tenant_id, email, (tenant_id, is_active), role_id
- **Methods:** `getFullName()` (Transient), `hasPermission(code)`, `hasAnyPermission(codes...)`
- **Does NOT extend BaseEntity**

#### Project
- **Table:** `projects`
- **Fields:** id, tenantId, name, slug, description (TEXT), status (ACTIVE, PLANNING, COMPLETED, ARCHIVED), color (hex, 7 chars), owner (ManyToOne -> User), iconUrl, createdAt, updatedAt, createdById
- **Unique Constraint:** (tenant_id, slug)
- **Index:** (tenant_id, status)
- **Does NOT extend BaseEntity**

#### Team
- **Table:** `teams`
- **Fields:** id, tenantId, project (ManyToOne -> Project), name, slug, description, iconUrl, color, lead (ManyToOne -> User), active, settings (text/JSON), members (OneToMany -> TeamMember, cascade ALL, orphanRemoval), createdAt, updatedAt
- **Unique Constraint:** (tenant_id, slug)
- **Index:** project_id
- **Methods:** `addMember(user, role)`, `removeMember(user)`
- **Does NOT extend BaseEntity**

#### TeamMember
- **Table:** `team_members`
- **Fields:** id, team (ManyToOne -> Team), user (ManyToOne -> User, EAGER), teamRole (LEAD, MEMBER, OBSERVER), joinedAt, addedById
- **Unique Constraint:** (team_id, user_id)
- **Does NOT extend BaseEntity**, no AuditingEntityListener

#### Outcome
- **Table:** `outcomes`
- **Fields:** id, tenantId, project (ManyToOne -> Project), title (500), description (TEXT), successCriteria (TEXT), targetMetrics (text/JSON), currentMetrics (text/JSON), status (DRAFT, NOT_STARTED, IN_PROGRESS, VALIDATING, VALIDATED, INVALIDATED, ABANDONED), priority (CRITICAL, HIGH, MEDIUM, LOW, BACKLOG), team (ManyToOne -> Team), owner (ManyToOne -> User), targetDate (LocalDate), startedAt, validatedAt, invalidatedAt, validationNotes (TEXT), validatedBy (ManyToOne -> User), invalidatedBy (ManyToOne -> User), externalRefs (text/JSON), tags (text/JSON), keyResults (OneToMany -> KeyResult, cascade ALL, orphanRemoval), createdAt, updatedAt, createdById
- **Indexes:** (tenant_id, status), (tenant_id, team_id), project_id
- **Does NOT extend BaseEntity**

#### KeyResult
- **Table:** `key_results`
- **Fields:** id, outcome (ManyToOne -> Outcome), title (255), description (1000), targetValue (BigDecimal 15,4), currentValue (BigDecimal 15,4, default 0), unit (50), createdAt, updatedAt
- **Index:** outcome_id
- **Method:** `getProgressPercent()` (Transient) -- calculates (current/target * 100)
- **Uses @Builder, @PreUpdate** instead of JPA auditing

#### Hypothesis
- **Table:** `hypotheses`
- **Fields:** id, tenantId, project (ManyToOne -> Project), outcome (ManyToOne -> Outcome, not null), title (500), belief (TEXT, not null), expectedResult (TEXT, not null), measurementCriteria (TEXT), status (DRAFT, READY, BLOCKED, BUILDING, DEPLOYED, MEASURING, VALIDATED, INVALIDATED, ABANDONED), priority (CRITICAL, HIGH, MEDIUM, LOW), effort (TShirtSize: XS, S, M, L, XL), impact (TShirtSize), confidence (ConfidenceLevel: LOW, MEDIUM, HIGH), owner (ManyToOne -> User), experimentConfig (text/JSON), experimentResults (text/JSON), blockedReason (TEXT), conclusionNotes (TEXT), externalRefs (text/JSON), tags (text/JSON), startedAt, deployedAt, measuringStartedAt, concludedAt, concludedBy (ManyToOne -> User), createdAt, updatedAt, createdById
- **Indexes:** (tenant_id, status), (tenant_id, outcome_id), (tenant_id, owner_id), project_id
- **State Machine:** DRAFT -> READY -> BUILDING -> DEPLOYED -> MEASURING -> VALIDATED/INVALIDATED; BLOCKED and ABANDONED reachable from most states

#### Experiment
- **Table:** `experiments`
- **Fields:** id, tenantId, project (ManyToOne -> Project), hypothesis (ManyToOne -> Hypothesis), name (255), description (TEXT), type (A_B_TEST, FEATURE_FLAG, CANARY, MANUAL), status (DRAFT, RUNNING, CONCLUDED, CANCELLED), config (TEXT/JSON), startDate, endDate, durationDays, results (TEXT/JSON), conclusion (TEXT), trafficSplit (20), primaryMetric (255), secondaryMetrics (TEXT/JSON), audienceFilter (TEXT/JSON), sampleSizeTarget, currentSampleSize (default 0), controlValue (BigDecimal 10,4), variantValue (BigDecimal 10,4), confidenceLevel (BigDecimal 5,2), owner (ManyToOne -> User), createdById, createdAt, updatedAt
- **Indexes:** (tenant_id, status), (tenant_id, project_id), hypothesis_id

#### Decision
- **Table:** `decisions`
- **Fields:** id, tenantId, project (ManyToOne -> Project), title (500), description (TEXT), context (TEXT), options (text/JSON), status (NEEDS_INPUT, UNDER_DISCUSSION, DECIDED, IMPLEMENTED, DEFERRED, CANCELLED), priority (BLOCKING, HIGH, NORMAL, LOW), decisionType (PRODUCT, UX, TECHNICAL, ARCHITECTURAL, STRATEGIC, OPERATIONAL, RESOURCE, SCOPE, TIMELINE), owner (ManyToOne -> User), assignedTo (ManyToOne -> User), outcome (ManyToOne -> Outcome), hypothesis (ManyToOne -> Hypothesis), team (ManyToOne -> Team), queue (ManyToOne -> DecisionQueue), stakeholder (ManyToOne -> Stakeholder), slaHours, dueAt, escalationLevel (default 0), escalatedAt, escalatedTo (ManyToOne -> User), decidedBy (ManyToOne -> User), decidedAt, decisionRationale (TEXT), selectedOption (text/JSON), resolution (2000), wasEscalated (default false), votes (OneToMany -> DecisionVote, cascade ALL, orphanRemoval), blockedItems (text/JSON), externalRefs (text/JSON), tags (text/JSON), createdAt, updatedAt, createdById
- **Indexes:** (tenant_id, status), (tenant_id, priority), owner_id, assigned_to_id, queue_id, due_at, stakeholder_id, project_id
- **Methods:** `isOverdue()`, `getWaitTimeHours()`

#### DecisionVote
- **Table:** `decision_votes`
- **Fields:** id, decision (ManyToOne -> Decision), user (ManyToOne -> User), vote (APPROVE, REJECT, ABSTAIN, NEEDS_MORE_INFO), comment (1000), createdAt
- **Unique Constraint:** (decision_id, user_id)
- **Indexes:** decision_id, user_id

#### DecisionComment
- **Table:** `decision_comments`
- **Fields:** id, decision (ManyToOne -> Decision), author (ManyToOne -> User), content (TEXT, not null), optionId (String), parent (ManyToOne -> DecisionComment, self-referencing for threading), edited (boolean, default false), createdAt, updatedAt
- **Index:** decision_id

#### DecisionQueue
- **Table:** `decision_queues`
- **Fields:** id, tenantId, name (255), description (500), isDefault (boolean, default false), slaConfig (JSON Map<String, Integer>), createdAt, updatedAt
- **Unique Constraint:** (tenant_id, name)
- **Index:** tenant_id

#### Stakeholder
- **Table:** `stakeholders`
- **Fields:** id, tenantId, project (ManyToOne -> Project), name, email, title, organization, phone, avatarUrl, type (INTERNAL, EXTERNAL, EXECUTIVE, CUSTOMER, REGULATORY, TECHNICAL), user (OneToOne -> User), expertise (text/JSON), preferredContactMethod, availabilityNotes (TEXT), timezone, decisionsPending (default 0), decisionsCompleted (default 0), decisionsEscalated (default 0), avgResponseTimeHours, lastDecisionAt, active (default true), notes (TEXT), externalRefs (text/JSON), createdAt, updatedAt, createdById
- **Indexes:** tenant_id, user_id, project_id

#### StakeholderResponse
- **Table:** `stakeholder_responses`
- **Fields:** id, decision (ManyToOne -> Decision), stakeholder (ManyToOne -> Stakeholder), response (2000), responseTimeHours (BigDecimal 10,2), withinSla, requestedAt, respondedAt, createdAt
- **Unique Constraint:** (decision_id, stakeholder_id)
- **Indexes:** decision_id, stakeholder_id

#### Role
- **Table:** `roles`
- **Fields:** id, tenantId (nullable -- null for system roles), code, name, description, category (RoleCategory), level (RoleLevel), systemRole (boolean, default false), permissions (ManyToMany -> Permission via `role_permissions` join table, EAGER), createdAt
- **Unique Constraint:** (tenant_id, code)

#### Permission
- **Table:** `permissions`
- **Fields:** id, code (unique), name, description, category
- No tenant scoping -- permissions are global

#### AuditLog
- **Table:** `audit_logs`
- **Fields:** id, tenantId, actorId, actorEmail, actorName, action (AuditAction enum), entityType, entityId, entityTitle, description (TEXT), oldValues (text/JSON), newValues (text/JSON), ipAddress, userAgent, requestId, timestamp (Instant, default now)
- **Indexes:** (tenant_id, timestamp DESC), (entity_type, entity_id), actor_id

---

## 5. REST API Endpoints

### AuthController -- `/api/v1/auth` (PUBLIC)

| Method | Path          | Description                        | Auth Required | Rate Limited          |
| ------ | ------------- | ---------------------------------- | ------------- | --------------------- |
| POST   | `/login`      | Authenticate user, return JWT      | No            | 5/min per IP          |
| POST   | `/register`   | Register new tenant + admin user   | No            | 10/min per IP         |
| POST   | `/refresh`    | Refresh access token               | No            | 10/min per IP         |
| POST   | `/logout`     | Invalidate refresh token           | Yes (@CurrentUser) | No               |

### UserController -- `/api/v1/users`

| Method | Path                    | Description                    | Permission Required                              |
| ------ | ----------------------- | ------------------------------ | ------------------------------------------------ |
| GET    | `/`                     | List users (optional dept filter) | TENANT_ADMIN / SUPER_ADMIN / user:read          |
| GET    | `/{id}`                 | Get user by ID                 | TENANT_ADMIN / SUPER_ADMIN / user:read           |
| PUT    | `/{id}`                 | Update user                    | TENANT_ADMIN / SUPER_ADMIN / user:update         |
| DELETE | `/{id}`                 | Deactivate user                | TENANT_ADMIN / SUPER_ADMIN / user:delete         |
| GET    | `/me`                   | Get current authenticated user | Any authenticated user                           |
| GET    | `/{id}/direct-reports`  | Get user's direct reports      | TENANT_ADMIN / SUPER_ADMIN / user:read           |
| PUT    | `/{id}/role`            | Assign role to user            | TENANT_ADMIN / SUPER_ADMIN / user:manage_roles   |

### RoleController -- `/api/v1/roles`

| Method | Path           | Description              | Permission Required                    |
| ------ | -------------- | ------------------------ | -------------------------------------- |
| GET    | `/`            | List available roles     | TENANT_ADMIN / SUPER_ADMIN / user:read |
| GET    | `/{id}`        | Get role by ID           | TENANT_ADMIN / SUPER_ADMIN / user:read |
| POST   | `/`            | Create custom role       | TENANT_ADMIN / SUPER_ADMIN             |
| GET    | `/categories`  | List role categories     | TENANT_ADMIN / SUPER_ADMIN / user:read |

### ProjectController -- `/api/v1/projects`

| Method | Path             | Description            | Permission Required                                         |
| ------ | ---------------- | ---------------------- | ----------------------------------------------------------- |
| GET    | `/`              | List projects          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / project:read    |
| GET    | `/paged`         | List projects (paged)  | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / project:read    |
| GET    | `/{id}`          | Get project by ID      | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / project:read    |
| POST   | `/`              | Create project         | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / project:create  |
| PUT    | `/{id}`          | Update project         | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / project:update  |
| DELETE | `/{id}`          | Delete project         | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / project:delete  |
| GET    | `/{id}/stats`    | Get project statistics | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / project:read    |
| GET    | `/{id}/dashboard`| Get project dashboard  | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / project:read    |

### TeamController -- `/api/v1/teams`

| Method | Path                        | Description              | Permission Required                                       |
| ------ | --------------------------- | ------------------------ | --------------------------------------------------------- |
| GET    | `/`                         | List teams               | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:read     |
| GET    | `/paged`                    | List teams (paged)       | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:read     |
| GET    | `/{id}`                     | Get team detail          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:read     |
| GET    | `/slug/{slug}`              | Get team by slug         | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:read     |
| GET    | `/my-teams`                 | Get current user's teams | Any authenticated                                         |
| POST   | `/`                         | Create team              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:create   |
| PUT    | `/{id}`                     | Update team              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:update   |
| DELETE | `/{id}`                     | Delete team              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:delete   |
| GET    | `/{id}/members`             | List team members        | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:read     |
| POST   | `/{id}/members`             | Add team member          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:manage_members |
| PUT    | `/{id}/members/{userId}`    | Update member role       | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:manage_members |
| DELETE | `/{id}/members/{userId}`    | Remove team member       | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:manage_members |
| GET    | `/{id}/workload`            | Get team workload        | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / team:read     |

### OutcomeController -- `/api/v1/outcomes`

| Method | Path                | Description                 | Permission Required                                         |
| ------ | ------------------- | --------------------------- | ----------------------------------------------------------- |
| GET    | `/`                 | List outcomes (filterable)  | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:read    |
| GET    | `/paged`            | List outcomes (paged)       | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:read    |
| GET    | `/{id}`             | Get outcome by ID           | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:read    |
| GET    | `/my-outcomes`      | Current user's outcomes     | Any authenticated                                           |
| GET    | `/team/{teamId}`    | Team outcomes               | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:read    |
| GET    | `/overdue`          | Overdue outcomes            | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:read    |
| POST   | `/`                 | Create outcome              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:create  |
| PUT    | `/{id}`             | Update outcome              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:update  |
| DELETE | `/{id}`             | Delete outcome              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:delete  |
| POST   | `/{id}/start`       | Start outcome               | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:update  |
| POST   | `/{id}/validate`    | Validate outcome            | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:validate|
| POST   | `/{id}/invalidate`  | Invalidate outcome          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:validate|
| POST   | `/{id}/abandon`     | Abandon outcome             | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:delete  |
| PATCH  | `/{id}/metrics`     | Update current metrics      | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:update  |
| GET    | `/stats`            | Status count breakdown      | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / outcome:read    |

### KeyResultController -- `/api/v1/outcomes/{outcomeId}/key-results`

| Method | Path              | Description           | Permission Required                             |
| ------ | ----------------- | --------------------- | ----------------------------------------------- |
| GET    | `/`               | List key results      | TENANT_ADMIN / SUPER_ADMIN / outcome:read       |
| GET    | `/{id}`           | Get key result        | TENANT_ADMIN / SUPER_ADMIN / outcome:read       |
| POST   | `/`               | Create key result     | TENANT_ADMIN / SUPER_ADMIN / outcome:update     |
| PUT    | `/{id}`           | Update key result     | TENANT_ADMIN / SUPER_ADMIN / outcome:update     |
| POST   | `/{id}/progress`  | Update progress value | TENANT_ADMIN / SUPER_ADMIN / outcome:update     |
| DELETE | `/{id}`           | Delete key result     | TENANT_ADMIN / SUPER_ADMIN / outcome:update     |

**Note:** KeyResultController does not include `TENANT_OWNER` role in its `@PreAuthorize` checks, unlike other controllers. This is likely a bug (see Known Issues).

### HypothesisController -- `/api/v1/hypotheses`

| Method | Path                          | Description                     | Permission Required                                           |
| ------ | ----------------------------- | ------------------------------- | ------------------------------------------------------------- |
| GET    | `/`                           | List hypotheses (filterable)    | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:read   |
| GET    | `/paged`                      | List hypotheses (paged)         | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:read   |
| GET    | `/{id}`                       | Get hypothesis by ID            | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:read   |
| GET    | `/my-hypotheses`              | Current user's hypotheses       | Any authenticated                                             |
| GET    | `/outcome/{outcomeId}`        | Hypotheses for outcome          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:read   |
| GET    | `/blocked`                    | Blocked hypotheses              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:read   |
| GET    | `/active`                     | Active hypotheses               | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:read   |
| POST   | `/`                           | Create hypothesis               | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:create |
| PUT    | `/{id}`                       | Update hypothesis               | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:update |
| DELETE | `/{id}`                       | Delete hypothesis               | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:delete |
| POST   | `/{id}/transition`            | Transition status               | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:update |
| POST   | `/{id}/conclude`              | Conclude hypothesis             | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:validate|
| POST   | `/{id}/abandon`               | Abandon hypothesis              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:delete |
| GET    | `/stats`                      | Status counts                   | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:read   |
| GET    | `/outcome/{outcomeId}/stats`  | Status counts for outcome       | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / hypothesis:read   |

### ExperimentController -- `/api/v1/experiments`

| Method | Path                           | Description                  | Permission Required                                           |
| ------ | ------------------------------ | ---------------------------- | ------------------------------------------------------------- |
| GET    | `/`                            | List experiments (filterable)| TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:read   |
| GET    | `/paged`                       | List experiments (paged)     | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:read   |
| GET    | `/{id}`                        | Get experiment by ID         | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:read   |
| POST   | `/`                            | Create experiment            | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:create |
| PUT    | `/{id}`                        | Update experiment            | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:update |
| DELETE | `/{id}`                        | Delete experiment            | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:delete |
| POST   | `/{id}/start`                  | Start experiment             | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:update |
| POST   | `/{id}/conclude`               | Conclude experiment          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:update |
| POST   | `/{id}/cancel`                 | Cancel experiment            | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:update |
| POST   | `/{id}/extend`                 | Extend experiment duration   | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:update |
| POST   | `/{id}/results`                | Record experiment results    | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:update |
| GET    | `/hypothesis/{hypothesisId}`   | Experiments for hypothesis   | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / experiment:read   |

### DecisionController -- `/api/v1/decisions`

| Method | Path                              | Description                    | Permission Required                                        |
| ------ | --------------------------------- | ------------------------------ | ---------------------------------------------------------- |
| GET    | `/`                               | List decisions (filterable)    | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| GET    | `/paged`                          | List decisions (paged)         | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| GET    | `/{id}`                           | Get decision (opt votes/comments) | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read |
| GET    | `/queue`                          | Get decision queue view        | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| GET    | `/my-pending`                     | Current user's pending decisions| Any authenticated                                         |
| GET    | `/pending`                        | All pending decisions          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| GET    | `/blocking`                       | BLOCKING priority decisions    | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| GET    | `/overdue`                        | Overdue decisions              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| GET    | `/outcome/{outcomeId}`            | Decisions for outcome          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| GET    | `/hypothesis/{hypothesisId}`      | Decisions for hypothesis       | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| POST   | `/`                               | Create decision                | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:create|
| PUT    | `/{id}`                           | Update decision                | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:update|
| DELETE | `/{id}`                           | Delete decision                | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:delete|
| POST   | `/{id}/start-discussion`          | Move to UNDER_DISCUSSION       | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:update|
| POST   | `/{id}/resolve`                   | Resolve decision               | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:resolve|
| POST   | `/{id}/implement`                 | Mark as implemented            | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:update|
| POST   | `/{id}/defer`                     | Defer decision                 | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:update|
| POST   | `/{id}/cancel`                    | Cancel decision                | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:delete|
| POST   | `/{id}/reopen`                    | Reopen deferred/cancelled      | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:update|
| POST   | `/{id}/escalate`                  | Escalate decision              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:escalate|
| POST   | `/{id}/assign`                    | Assign decision                | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:assign|
| POST   | `/{id}/reassign`                  | Reassign decision              | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:assign|
| GET    | `/{id}/comments`                  | List comments                  | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| POST   | `/{id}/comments`                  | Add comment                    | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:comment|
| PUT    | `/{id}/comments/{commentId}`      | Update comment                 | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:comment|
| DELETE | `/{id}/comments/{commentId}`      | Delete comment                 | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:comment|
| GET    | `/stats`                          | Status counts                  | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| GET    | `/metrics/avg-time`               | Average decision time          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / analytics:read |
| GET    | `/{id}/votes`                     | List votes                     | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| GET    | `/{id}/votes/summary`             | Vote summary                   | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:read  |
| POST   | `/{id}/votes`                     | Cast vote                      | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:vote  |
| DELETE | `/{id}/votes`                     | Remove vote                    | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / decision:vote  |

### DecisionQueueController -- `/api/v1/queues`

| Method | Path               | Description          | Permission Required                      |
| ------ | ------------------ | -------------------- | ---------------------------------------- |
| GET    | `/`                | List queues          | hasPermission(tenantId, 'QUEUE', 'READ') |
| GET    | `/{id}`            | Get queue by ID      | hasPermission(tenantId, 'QUEUE', 'READ') |
| GET    | `/default`         | Get default queue    | hasPermission(tenantId, 'QUEUE', 'READ') |
| POST   | `/`                | Create queue         | hasPermission(tenantId, 'QUEUE', 'CREATE')|
| PUT    | `/{id}`            | Update queue         | hasPermission(tenantId, 'QUEUE', 'UPDATE')|
| DELETE | `/{id}`            | Delete queue         | hasPermission(tenantId, 'QUEUE', 'DELETE')|
| POST   | `/{id}/set-default`| Set as default queue | hasPermission(tenantId, 'QUEUE', 'UPDATE')|

**Note:** This controller uses `hasPermission()` SpEL expressions instead of the `hasRole()/hasAuthority()` pattern used by all other controllers. There is no custom `PermissionEvaluator` bean registered, so these checks will likely **always fail** at runtime (see Known Issues).

### StakeholderController -- `/api/v1/stakeholders`

| Method | Path                          | Description                  | Permission Required                                            |
| ------ | ----------------------------- | ---------------------------- | -------------------------------------------------------------- |
| GET    | `/`                           | List stakeholders            | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / stakeholder:read   |
| GET    | `/{id}`                       | Get stakeholder by ID        | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / stakeholder:read   |
| GET    | `/email/{email}`              | Get stakeholder by email     | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / stakeholder:read   |
| GET    | `/with-pending`               | Stakeholders with pending    | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / stakeholder:read   |
| GET    | `/slow-responders`            | Slow responding stakeholders | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / analytics:read     |
| GET    | `/search/expertise`           | Search by expertise          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / stakeholder:read   |
| POST   | `/`                           | Create stakeholder           | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / stakeholder:create |
| PUT    | `/{id}`                       | Update stakeholder           | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / stakeholder:update |
| DELETE | `/{id}`                       | Delete stakeholder           | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / stakeholder:delete |
| GET    | `/{id}/metrics`               | Stakeholder metrics          | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / analytics:read     |
| GET    | `/leaderboard`                | Stakeholder leaderboard      | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / analytics:read     |
| GET    | `/me/responses/pending`       | Current user's pending       | Any authenticated                                              |
| GET    | `/{id}/stats`                 | Stakeholder stats (=metrics) | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / analytics:read     |
| GET    | `/{id}/responses`             | Stakeholder responses (paged)| TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / stakeholder:read   |
| GET    | `/scorecard`                  | Stakeholder scorecard        | TENANT_OWNER / TENANT_ADMIN / SUPER_ADMIN / analytics:read     |

### AuditController -- `/api/v1/audit`

| Method | Path                                 | Description          | Permission Required                               |
| ------ | ------------------------------------ | -------------------- | ------------------------------------------------- |
| GET    | `/`                                  | Query audit logs     | TENANT_ADMIN / SUPER_ADMIN / system:audit_read    |
| GET    | `/entity/{entityType}/{entityId}`    | Entity change history| TENANT_ADMIN / SUPER_ADMIN / system:audit_read    |
| GET    | `/stats`                             | Action count stats   | TENANT_ADMIN / SUPER_ADMIN / system:audit_read    |

### Public/Unprotected Endpoints

- `/api/v1/auth/**` -- all auth endpoints
- `/actuator/health` -- health check
- `/swagger-ui/**` -- Swagger UI
- `/v3/api-docs/**` -- OpenAPI spec

---

## 6. Security

### Authentication Mechanism

1. **JWT-based stateless authentication** using HMAC-SHA256 signing
2. Access token contains: userId (subject), tenantId, email, role code, permission codes
3. Access token expiration: 86,400,000ms (24 hours)
4. Refresh token expiration: 604,800,000ms (7 days)
5. Refresh tokens are bcrypt-hashed and stored in the `users` table
6. Token refresh rotates the refresh token (old one becomes invalid)
7. Logout clears the stored refresh token

### Security Filter Chain

1. **RequestContextFilter** (HIGHEST_PRECEDENCE + 1) -- captures IP, User-Agent, X-Request-ID into ThreadLocal
2. **JwtAuthenticationFilter** (before UsernamePasswordAuthenticationFilter) -- extracts Bearer token, validates JWT, creates `UserPrincipal`, sets `TenantContext`
3. Standard Spring Security filters

### RBAC System

The system implements a sophisticated role-based access control model with permissions:

**Role Categories:** SYSTEM, EXECUTIVE, PRODUCT, ENGINEERING, UX, QA, DATA, BUSINESS, STAKEHOLDER, CUSTOM

**Role Levels (9 tiers):**
- L1_INDIVIDUAL -- Read-only permissions
- L2_SENIOR -- Read-only permissions
- L3_LEAD -- Read + Create/Update
- L4_MANAGER -- Full CRUD + Assign + Manage
- L5_SENIOR_MANAGER -- Full CRUD + Assign + Manage
- L6_VP -- Cross-domain + Management
- L7_SVP -- Cross-domain + Management
- L8_CXXX -- All permissions including admin
- L9_OWNER -- All permissions including admin

**System Roles (42 pre-seeded roles):** Including SUPER_ADMIN, TENANT_ADMIN, CEO, CPO, CTO, VPs, Directors, Managers, Leads, Individual Contributors across Product, Engineering, UX, QA, Data, Business, and Stakeholder categories.

**Permission Categories (48 permissions):**
- PROJECT: read, create, update, delete
- OUTCOME: read, create, update, delete, validate, assign
- HYPOTHESIS: read, create, update, delete, transition, build
- EXPERIMENT: read, create, update, delete
- DECISION: read, create, update, delete, resolve, escalate, assign, comment
- TEAM: read, create, update, delete, manage_members
- USER: read, create, update, delete, manage_roles
- STAKEHOLDER: read, create, update, delete
- ANALYTICS: read, export, admin
- INTEGRATION: read, create, update, delete, sync
- SYSTEM: admin, settings, audit_read, tenant_manage

### Rate Limiting

- In-memory Bucket4j rate limiter (not distributed via Redis)
- Login: 5 requests/minute per IP
- General auth: 10 requests/minute per IP
- Applied only to auth endpoints, not to API endpoints

### Security Headers

- CSRF disabled (stateless API)
- Content-Type options (nosniff)
- Frame options: DENY
- XSS protection: enabled with block mode
- HSTS: enabled with subdomains, 1-year max-age
- CORS: `http://localhost:*` allowed (dev-only pattern)

### Tenant Isolation

- `TenantContext` uses ThreadLocal to hold current tenant UUID
- Set during JWT filter, cleared in finally block
- All service methods accept `tenantId` and pass it to repository queries
- No Hibernate-level tenant filtering (manual enforcement only)

---

## 7. Database

### PostgreSQL Configuration

- **Version:** 16-alpine (Docker)
- **Default credentials:** zevaro/zevaro
- **Database name:** zevaro
- **Extensions:** uuid-ossp, pg_trgm (init-db.sql)

### Schema Management

- **Hibernate `ddl-auto: update`** -- tables are auto-created/altered on startup
- No Flyway or Liquibase migrations
- No explicit schema definition files beyond Hibernate entity annotations

### Tables (19 tables)

1. `tenants`
2. `users`
3. `roles`
4. `permissions`
5. `role_permissions` (join table)
6. `projects`
7. `teams`
8. `team_members`
9. `outcomes`
10. `key_results`
11. `hypotheses`
12. `experiments`
13. `decisions`
14. `decision_votes`
15. `decision_comments`
16. `decision_queues`
17. `stakeholders`
18. `stakeholder_responses`
19. `audit_logs`

### Notable Indexes

The application defines numerous JPA-level indexes:

- **Users:** tenant_id, email, (tenant_id, is_active), role_id
- **Projects:** (tenant_id, status)
- **Teams:** project_id
- **Outcomes:** (tenant_id, status), (tenant_id, team_id), project_id
- **Key Results:** outcome_id
- **Hypotheses:** (tenant_id, status), (tenant_id, outcome_id), (tenant_id, owner_id), project_id
- **Experiments:** (tenant_id, status), (tenant_id, project_id), hypothesis_id
- **Decisions:** (tenant_id, status), (tenant_id, priority), owner_id, assigned_to_id, queue_id, due_at, stakeholder_id, project_id
- **Decision Votes:** decision_id, user_id
- **Decision Comments:** decision_id
- **Decision Queues:** tenant_id
- **Stakeholders:** tenant_id, user_id, project_id
- **Stakeholder Responses:** decision_id, stakeholder_id
- **Audit Logs:** (tenant_id, timestamp DESC), (entity_type, entity_id), actor_id

### Data Initialization

Two `CommandLineRunner` components seed data on first startup (when tables are empty):

1. **PermissionDataLoader** (Order 1) -- creates 48 permissions
2. **RoleDataLoader** (Order 2) -- creates 42 system roles with permission assignments based on level

---

## 8. Event System

### Architecture

Events are published via **Apache Kafka** with a circuit-breaker pattern:

1. Domain services call `EventPublisher` methods
2. `EventPublisher` creates typed event DTOs extending `BaseEvent`
3. Events are sent via `KafkaProducerService` with circuit breaker protection
4. `KafkaProducerServiceNoOp` is used when Kafka is disabled (`KAFKA_ENABLED=false`)

### Kafka Topics (10 defined in KafkaConfig)

| Topic Name                          | Event Type               |
| ----------------------------------- | ------------------------ |
| `zevaro.decisions.created`          | Decision created         |
| `zevaro.decisions.resolved`         | Decision resolved        |
| `zevaro.decisions.escalated`        | Decision escalated       |
| `zevaro.outcomes.created`           | Outcome created          |
| `zevaro.outcomes.validated`         | Outcome validated        |
| `zevaro.outcomes.invalidated`       | Outcome invalidated      |
| `zevaro.hypotheses.created`         | Hypothesis created       |
| `zevaro.hypotheses.status-changed`  | Hypothesis status change |
| `zevaro.hypotheses.concluded`       | Hypothesis concluded     |
| `zevaro.audit.events`              | Audit log entries        |

### Topic Name Mismatch (Bug)

There is an inconsistency between topic names defined in `KafkaConfig` (bean definitions) and `KafkaTopics` (constants class):

- `KafkaConfig` creates topics like `zevaro.decisions.created`
- `KafkaTopics` constants use `zevaro.core.decision.created`
- `EventPublisher` uses `KafkaTopics` constants, so events go to `zevaro.core.*` topics
- The auto-created topics from `KafkaConfig` (`zevaro.decisions.*`) would be empty

### Circuit Breaker Details

- Opens after 5 consecutive failures
- Resets after 5 minutes
- Rate-limited logging (1 log per 5 minutes while open)
- Tracks dropped event count for observability
- Events are silently dropped when circuit is open (fire-and-forget)

### Event Types

| Event Class                     | Data Included                                                  |
| ------------------------------- | -------------------------------------------------------------- |
| `DecisionCreatedEvent`          | decisionId, title, priority, type, assignedToId, outcomeId, hypothesisId, dueAt |
| `DecisionResolvedEvent`         | decisionId, title, priority, decidedById, rationale, cycleTimeHours, wasEscalated, escalationLevel, unblockedHypothesisIds |
| `DecisionEscalatedEvent`        | decisionId, title, priority, escalatedFromId, escalatedToId, escalationLevel, reason, waitTimeHours |
| `OutcomeCreatedEvent`           | outcomeId, title, priority, teamId, ownerId |
| `OutcomeValidatedEvent`         | outcomeId, title, validatedById, notes, finalMetrics, daysToValidation |
| `OutcomeInvalidatedEvent`       | outcomeId, title, invalidatedById, notes |
| `HypothesisCreatedEvent`        | hypothesisId, title, outcomeId, ownerId |
| `HypothesisStatusChangedEvent`  | hypothesisId, title, previousStatus, newStatus, outcomeId, blockedByDecisionId |
| `HypothesisConcludedEvent`      | hypothesisId, title, validated (boolean), outcomeId, experimentResults, daysToConclusion |

---

## 9. Configuration

### application.yml (Default Profile)

| Property                      | Value                                                       |
| ----------------------------- | ----------------------------------------------------------- |
| `server.port`                 | 8080                                                        |
| `spring.datasource.url`       | `jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:zevaro}` |
| `spring.jpa.ddl-auto`         | update                                                      |
| `spring.jpa.show-sql`         | true                                                        |
| `spring.data.redis.host`      | `${REDIS_HOST:localhost}`                                   |
| `spring.kafka.enabled`        | `${KAFKA_ENABLED:true}`                                     |
| `spring.kafka.bootstrap-servers` | `${KAFKA_SERVERS:localhost:9092}`                         |
| `jwt.secret`                  | `${JWT_SECRET:zevaro-development-secret-key-min-256-bits-long}` |
| `jwt.expiration`              | 86400000 (24 hours)                                         |
| `jwt.refresh-expiration`      | 604800000 (7 days)                                          |
| Actuator endpoints            | health, info, metrics                                       |

### application-dev.yml

- `ddl-auto: update`
- `show-sql: true`
- Logging: `ai.zevaro: DEBUG`, `org.springframework.security: DEBUG`

### application-docker.yml

- PostgreSQL host defaults to `postgres` (Docker service name)
- Redis host defaults to `redis`
- Kafka defaults to `kafka:9092`
- Separate JWT secret for Docker
- Logging: `ai.zevaro: INFO`, `org.springframework: INFO`

### Docker Compose Services

| Service     | Image                    | Port(s)     | Notes                          |
| ----------- | ------------------------ | ----------- | ------------------------------ |
| postgres    | postgres:16-alpine       | 5432        | Health check enabled           |
| redis       | redis:7-alpine           | 6379        | Health check enabled           |
| kafka       | bitnami/kafka:3.6        | 9092, 9094  | KRaft mode (no Zookeeper)      |
| kafka-ui    | provectuslabs/kafka-ui   | 8090        | Debug profile only             |
| zevaro-core | Custom Dockerfile        | 8080        | Full profile only              |

### Dockerfile

- Multi-stage build: `eclipse-temurin:21-jdk-alpine` (build) + `eclipse-temurin:21-jre-alpine` (runtime)
- Runs as non-root user (`zevaro:zevaro`, UID/GID 1001)
- Health check via wget to `/actuator/health`

---

## 10. Code Quality Observations

### Positive Observations

1. **Consistent package structure** -- every domain follows the same layered pattern with entity, repository, service, controller, mapper, and DTOs.

2. **DTOs use Java records** -- clean, immutable request/response objects throughout.

3. **Tenant isolation** -- all queries are scoped by tenantId, preventing cross-tenant data leaks.

4. **Kafka circuit breaker** -- well-implemented fault tolerance pattern with rate-limited logging to prevent log flooding.

5. **Security headers** -- proper HSTS, XSS protection, frame options, and content-type options configured.

6. **Pagination support** -- most list endpoints have both unpaginated and paginated variants with configurable sorting and a max page size cap of 100.

7. **Comprehensive RBAC** -- 48 granular permissions with a logical hierarchy mapped across 42 roles.

8. **Event-driven architecture** -- domain events published for key state changes enabling downstream analytics.

9. **Rate limiting** -- login and auth endpoints are rate-limited per IP.

10. **Slug generation** -- `SlugGenerator` handles uniqueness checking with sequential suffixes.

### Concerns and Areas for Improvement

1. **No unit or integration tests** -- the `src/test` directory contains no Java test files. Zero test coverage.

2. **Hardcoded JWT secret in config files** -- the default JWT secrets are committed in plaintext in `application.yml` and `application-docker.yml`. While environment variables can override them, the defaults are insecure and could be accidentally used in production.

3. **`ddl-auto: update` in all profiles** -- including Docker (which may be used for staging/prod-like environments). There are no database migration scripts (no Flyway/Liquibase). Schema changes are entirely managed by Hibernate auto-DDL, which is unsuitable for production.

4. **CORS allows any localhost port** -- `allowedOriginPatterns("http://localhost:*")` is fine for development but there is no production CORS configuration. This should be profile-dependent.

5. **`show-sql: true` in default profile** -- SQL logging is enabled by default, which is noisy and can leak sensitive data in production logs.

6. **Inconsistent use of `BaseEntity`** -- the `BaseEntity` superclass exists but no entity actually extends it. Every entity redefines id, tenantId, createdAt, and updatedAt fields manually.

7. **N+1 query potential** -- `toResponseWithCount()` in `DecisionService` issues two additional queries (comment count + vote list) for every decision in a list query. For paginated results of 20 decisions, this means 40 extra queries per request. The `findByTenantIdWithDetails` JOIN FETCH queries exist in the repository but are never used in the service.

8. **In-memory rate limiter is not distributed** -- despite Redis being in the stack, rate limiting uses `ConcurrentHashMap`, making it ineffective across multiple application instances. The map also has no eviction policy, so it grows unbounded.

9. **UserPrincipal has two constructors with different semantics** -- one takes a `Role` entity, the other takes a role code string and permission set. The `getRoleCode()` method only works with the first constructor. The second constructor (used by JWT filter) sets `role` to null, so `getRoleCode()` always returns null for authenticated API requests.

10. **Refresh token stored as bcrypt hash in user row** -- while bcrypt is appropriate for passwords, it makes refresh token validation slow (~100ms per check). A faster hash (SHA-256) would be more appropriate since refresh tokens are already high-entropy random values.

11. **`@Async` audit logging without configuration** -- `AuditService.log()` is annotated with `@Async` but there is no `@EnableAsync` configuration class or custom executor pool defined. This means `@Async` may not actually work, or it uses the default `SimpleAsyncTaskExecutor` which creates a new thread per invocation with no pooling.

12. **`decision:vote` permission exists in controller but not seeded** -- the permission `decision:vote` is referenced in `DecisionController` `@PreAuthorize` annotations but is never created by the `PermissionDataLoader`. Users would need to rely on role-based checks only.

13. **JSON fields stored as text** -- fields like `options`, `tags`, `externalRefs`, `metrics`, `config` are stored as `text` columns with JSON serialized/deserialized manually. Only `DecisionQueue.slaConfig` uses Hibernate's `@JdbcTypeCode(SqlTypes.JSON)` for proper JSONB support.

14. **`findByEmail` in auth login is not tenant-scoped** -- `AuthService.login()` calls `userRepository.findByEmail(email)` without a tenant ID. If two tenants have users with the same email, this would return an ambiguous result. However, `register()` checks `existsByEmail()` globally, preventing cross-tenant email duplicates. This is a design tension -- multi-tenancy suggests users could have the same email across tenants, but the login mechanism does not support this.

---

## 11. Known Issues

### Critical

1. **DecisionQueueController uses `hasPermission()` without a `PermissionEvaluator`** -- All endpoints in `DecisionQueueController` use `@PreAuthorize("hasPermission(#principal.tenantId, 'QUEUE', 'READ')")` style expressions. Spring Security's `hasPermission()` requires a custom `PermissionEvaluator` bean, which is not registered anywhere in the codebase. This means **all DecisionQueue endpoints will throw `AccessDeniedException` for every user**, regardless of their role or permissions.

2. **Kafka topic name mismatch** -- `KafkaConfig` creates topics named `zevaro.decisions.created`, `zevaro.outcomes.created`, etc. But `KafkaTopics` constants (used by `EventPublisher`) reference `zevaro.core.decision.created`, `zevaro.core.outcome.created`, etc. Events are published to topics that may not exist (Kafka auto-create is enabled, so they would be auto-created with default settings, but the pre-configured topics with 3 partitions and 1 replica would be unused).

3. **`KafkaProducerServiceNoOp` extends `KafkaProducerService` and passes `null` to its constructor** -- The no-op class calls `super(null)` which sets the internal `kafkaTemplate` field to null in the parent class. If any code path accidentally calls the parent's `send()` method instead of the overridden one, it would NPE. This is fragile inheritance.

### Moderate

4. **Missing `TENANT_OWNER` role in `KeyResultController` `@PreAuthorize`** -- Unlike every other controller which includes `hasRole('TENANT_OWNER')`, `KeyResultController` only checks for `TENANT_ADMIN` and `SUPER_ADMIN`. Tenant owners cannot manage key results through the API unless they also have `outcome:read`/`outcome:update` permissions through their role.

5. **Duplicate `hasRole('TENANT_OWNER')` check in blocked hypotheses endpoint** -- `HypothesisController.getBlockedHypotheses()` has `hasRole('TENANT_OWNER')` listed twice in its `@PreAuthorize` annotation. Not a functional issue but indicates copy-paste error.

6. **`reassign()` ignores the `reason` parameter** -- `DecisionService.reassign()` accepts a `reason` parameter but delegates to `assign()` which does not use it. The reassignment reason is silently discarded.

7. **`addBlockedItem()` in DecisionService has a bug** -- It calls `decisionMapper.optionsToJson(null)` instead of serializing the updated blocked items list, effectively clearing the blocked items instead of adding one.

8. **`getDirectReports()` endpoint is not tenant-scoped** -- `UserController.getDirectReports()` does not pass the current user's tenantId to the service. The `userService.getDirectReports(id)` likely queries by `managerId` without tenant filtering, potentially leaking data across tenants.

9. **`decision:vote` permission not seeded** -- The permission is used in `@PreAuthorize` checks on vote endpoints but is not created by `PermissionDataLoader`. This means permission-based access (as opposed to role-based) to voting functionality is broken.

10. **`hypothesis:validate` permission not seeded** -- Used in `HypothesisController.concludeHypothesis()` and `OutcomeController.validateOutcome()`/`invalidateOutcome()` but not in the permission seed data.

### Low / Cosmetic

11. **`TENANT_OWNER` role referenced in `@PreAuthorize` but not in `RoleDataLoader`** -- There is no `TENANT_OWNER` role defined in `RoleDataLoader`. The `@PreAuthorize` annotations reference `hasRole('TENANT_OWNER')` but this role is never created. It would only work if manually created.

12. **`ApiError` record is defined but never used** -- The `exception/ApiError.java` record exists but `GlobalExceptionHandler` returns `Map<String, Object>` instead.

13. **`StakeholderResponse` entity class name conflicts with DTO naming convention** -- The entity `ai.zevaro.core.domain.stakeholder.StakeholderResponse` (a JPA entity representing a stakeholder's response to a decision) has the same simple name as what would be expected for a DTO. The actual DTO is in `ai.zevaro.core.domain.stakeholder.dto.StakeholderResponse`. This can cause import confusion.

14. **`Outcome.invalidatedBy` field uses `validatedBy` in event publisher** -- `EventPublisher.publishOutcomeInvalidated()` accesses `outcome.getValidatedBy()` instead of `outcome.getInvalidatedBy()`. If invalidation sets the `invalidatedBy` field but not `validatedBy`, this would NPE.

15. **No `@EnableScheduling`** -- There are no scheduled tasks for SLA breach detection, escalation reminders, or stale decision cleanup, despite the data model supporting these features (dueAt, escalationLevel, etc.).

16. **Redis dependency included but never used functionally** -- `spring-boot-starter-data-redis` is in the POM and Redis is in Docker Compose, but there is no caching configuration (`@Cacheable`), no Redis-based rate limiting, and no Redis-based session management. Redis is listed in health checks but otherwise unused.

---

## Appendix: File Count Summary

| Category      | Count |
| ------------- | ----- |
| Java source files | 131 |
| Entities      | 15    |
| Controllers   | 12    |
| Services      | 12    |
| Repositories  | 14    |
| Mappers       | 8     |
| DTOs          | 50+   |
| Config classes| 9     |
| Event classes | 10    |
| Enums         | 18    |
| Test files    | 0     |

---

*End of audit.*
