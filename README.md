# Zevaro-Core

> **Core Backend Service for the Continuous Outcome Engineering Platform**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](LICENSE)

Zevaro-Core is the central backend service powering **Zevaro** — an AI-first, multi-tenant SaaS platform that replaces traditional Agile tooling with decision-velocity-optimized product management designed for 50-500x development velocity environments.

---

## 🎯 Core Philosophy

Traditional Agile assumes development is the bottleneck. In AI-first environments where code generation happens at 500x speed, **decisions become the bottleneck**. Zevaro shifts focus accordingly:

| Traditional Agile | Zevaro COE |
|-------------------|------------|
| Backlog of work items | **Decision Queue** — pending decisions blocking progress |
| Time-boxed sprints | **Outcome-boxed cycles** — end when outcomes validate |
| Story points | **Decision response time** — stakeholder accountability |
| Feature factories | **Hypothesis-driven development** — every feature is an experiment |

---

## ✨ Features

### Multi-Tenant Architecture
- Organization-level isolation with tenant-scoped data
- Configurable tenant settings and branding
- Cross-tenant admin capabilities for platform operators

### Identity & Access Management
- JWT-based authentication with refresh tokens
- Role-based access control (RBAC)
- SSO integration ready (SAML, OIDC)
- API key management for service accounts

### Outcome Management
- Define measurable business outcomes
- Link outcomes to strategic objectives
- Track outcome validation status
- Outcome-boxed cycle management

### Hypothesis Tracking
- Create hypotheses tied to outcomes
- Define success criteria and metrics
- Track experiment status and results
- Evidence collection and validation

### Decision Queue
- Centralized pending decision tracking
- SLA timers with escalation rules
- Stakeholder assignment and accountability
- Decision history and audit trail
- Blocking item identification

### Team & Stakeholder Management
- Team structures with role assignments
- Stakeholder response time tracking
- Notification preferences
- Availability and delegation rules

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                               │
│                    (Zevaro-Gateway)                              │
└─────────────────────┬───────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┬─────────────────┐
        ▼             ▼             ▼                 ▼
┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────────┐
│  Zevaro   │  │  Zevaro   │  │  Zevaro   │  │    Zevaro     │
│   Core    │  │ Analytics │  │Integration│  │  Flutter SDK  │
│  :8080    │  │  :8081    │  │  :8082    │  │   (clients)   │
└─────┬─────┘  └─────┬─────┘  └─────┬─────┘  └───────────────┘
      │              │              │
      └──────────────┼──────────────┘
                     ▼
        ┌────────────────────────┐
        │   PostgreSQL │ Redis   │
        │      Kafka             │
        └────────────────────────┘
```

### Service Responsibilities

| Service | Port | Responsibilities |
|---------|------|------------------|
| **Zevaro-Core** | 8080 | Auth, Tenants, Users, Outcomes, Hypotheses, Decisions, Stakeholders, Teams |
| Zevaro-Analytics | 8081 | Metrics, Dashboards, Reports, AI Insights |
| Zevaro-Integrations | 8082 | Jira, Confluence, GitHub, Slack, Elaro bridge |

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Message Queue | Apache Kafka |
| API Docs | OpenAPI 3.0 / Swagger |
| Build Tool | Maven |
| Containerization | Docker |

---

## 📁 Project Structure

```
zevaro-core/
├── src/
│   ├── main/
│   │   ├── java/ai/zevaro/core/
│   │   │   ├── config/           # Spring configuration
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Request/Response DTOs
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── mapper/           # Entity-DTO mappers
│   │   │   ├── repository/       # JPA repositories
│   │   │   ├── security/         # Auth & JWT
│   │   │   ├── service/          # Business logic
│   │   │   └── ZevaroCoreApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
├── docker/
│   └── Dockerfile
├── docs/
│   └── api/
├── CONVENTIONS.md
├── pom.xml
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### Quick Start

```bash
# Clone the repository
git clone https://github.com/your-org/Zevaro-Core.git
cd Zevaro-Core

# Start infrastructure
docker-compose up -d postgres redis kafka

# Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Verify health
curl http://localhost:8080/actuator/health
```

### Docker

```bash
# Build image
docker build -t zevaro-core:latest .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/zevaro \
  zevaro-core:latest
```

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/zevaro` |
| `DATABASE_USERNAME` | Database username | `zevaro` |
| `DATABASE_PASSWORD` | Database password | `zevaro` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | `localhost:9092` |
| `JWT_SECRET` | JWT signing key | (required) |
| `JWT_EXPIRATION_MS` | Token expiration | `86400000` (24h) |

### Application Profiles

| Profile | Purpose |
|---------|---------|
| `dev` | Local development, Hibernate DDL auto-update |
| `test` | Integration testing with H2 |
| `prod` | Production with Flyway migrations |

---

## 📡 API Overview

Base URL: `http://localhost:8080/api/v1`

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Authenticate user |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Invalidate tokens |

### Tenants

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/tenants` | List tenants (admin) |
| POST | `/tenants` | Create tenant |
| GET | `/tenants/{id}` | Get tenant details |
| PUT | `/tenants/{id}` | Update tenant |

### Outcomes

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/outcomes` | List outcomes for tenant |
| POST | `/outcomes` | Create outcome |
| GET | `/outcomes/{id}` | Get outcome details |
| PUT | `/outcomes/{id}` | Update outcome |
| DELETE | `/outcomes/{id}` | Archive outcome |
| POST | `/outcomes/{id}/validate` | Mark outcome validated |

### Hypotheses

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/hypotheses` | List hypotheses |
| POST | `/hypotheses` | Create hypothesis |
| GET | `/hypotheses/{id}` | Get hypothesis details |
| PUT | `/hypotheses/{id}` | Update hypothesis |
| POST | `/hypotheses/{id}/evidence` | Add evidence |
| POST | `/hypotheses/{id}/validate` | Validate/invalidate |

### Decisions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/decisions` | List decisions (with filters) |
| GET | `/decisions/queue` | Get pending decision queue |
| POST | `/decisions` | Create decision request |
| GET | `/decisions/{id}` | Get decision details |
| PUT | `/decisions/{id}` | Update decision |
| POST | `/decisions/{id}/resolve` | Resolve decision |
| POST | `/decisions/{id}/escalate` | Escalate decision |

### Teams & Stakeholders

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/teams` | List teams |
| POST | `/teams` | Create team |
| GET | `/stakeholders` | List stakeholders |
| POST | `/stakeholders` | Create stakeholder |
| GET | `/stakeholders/{id}/metrics` | Response time metrics |

---

## 🗄️ Database Schema

### Core Entities

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Tenant    │────<│    User     │     │    Team     │
└─────────────┘     └─────────────┘     └─────────────┘
       │                   │                   │
       │            ┌──────┴──────┐            │
       ▼            ▼             ▼            ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────────┐
│   Outcome   │  │   Decision  │  │   Stakeholder   │
└─────────────┘  └─────────────┘  └─────────────────┘
       │                │
       ▼                │
┌─────────────┐         │
│ Hypothesis  │─────────┘
└─────────────┘
```

### Key Tables

| Table | Description |
|-------|-------------|
| `tenants` | Organization/workspace isolation |
| `users` | User accounts with tenant membership |
| `outcomes` | Business outcomes being pursued |
| `hypotheses` | Testable hypotheses linked to outcomes |
| `decisions` | Pending/resolved decisions with SLAs |
| `stakeholders` | Decision makers with accountability metrics |
| `teams` | Team structures and membership |
| `decision_history` | Audit trail of decision changes |

---

## 🔌 Kafka Topics

| Topic | Publisher | Consumer | Purpose |
|-------|-----------|----------|---------|
| `zevaro.decisions.pending` | Core | Analytics, Integrations | New decision created |
| `zevaro.decisions.resolved` | Core | Analytics | Decision resolved |
| `zevaro.decisions.escalated` | Core | Integrations | SLA breach escalation |
| `zevaro.outcomes.validated` | Core | Analytics | Outcome validation events |
| `zevaro.notifications` | Core | Integrations | User notifications |

---

## 🧪 Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify -P integration-test

# Run with coverage
./mvnw test jacoco:report
```

---

## 📦 Related Repositories

| Repository | Description |
|------------|-------------|
| [Zevaro-Analytics](https://github.com/your-org/Zevaro-Analytics) | Metrics, dashboards, AI insights |
| [Zevaro-Integrations](https://github.com/your-org/Zevaro-Integrations) | Jira, Confluence, GitHub, Slack |
| [Zevaro-Flutter-SDK](https://github.com/your-org/Zevaro-Flutter-SDK) | Shared Flutter client library |
| [Zevaro-Web](https://github.com/your-org/Zevaro-Web) | Flutter web frontend |
| [Zevaro-iOS](https://github.com/your-org/Zevaro-iOS) | iOS application |
| [Zevaro-Android](https://github.com/your-org/Zevaro-Android) | Android application |
| [Zevaro-Windows](https://github.com/your-org/Zevaro-Windows) | Windows desktop application |
| [Zevaro-Mac](https://github.com/your-org/Zevaro-Mac) | macOS desktop application |
| [Zevaro-Linux](https://github.com/your-org/Zevaro-Linux) | Linux desktop application |

---

## 🤝 Contributing

1. Read `CONVENTIONS.md` before making changes
2. Create feature branch from `main`
3. Follow existing code patterns
4. Include tests for new functionality
5. Submit PR with clear description

---

## 📄 License

This project is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)** — see the [LICENSE](LICENSE) file for details.

### What This Means

✅ **You can**: Use, modify, distribute, and self-host freely  
✅ **You must**: Open-source any modifications if you host this as a service  
✅ **You must**: Include license and copyright notices  

### Commercial Licensing

If you want to use Zevaro in a proprietary product or offer it as a hosted service without open-sourcing your modifications, commercial licenses are available. Contact: [licensing@zevaro.ai](mailto:licensing@zevaro.ai)

---

## 🏢 About Zevaro

Zevaro is built with **AI-first methodology** where AI writes 100% of production code while humans provide architectural oversight. This platform was designed for environments achieving 50-500x traditional development velocity, where the bottleneck shifts from development capacity to decision-making speed.

**Core Innovation**: Decision Queue replaces Backlog. Track what's blocking progress, not what's waiting to be built.

---

<p align="center">
  <b>Built for the AI-first development era</b><br>
  <sub>Where decisions, not development, are the bottleneck</sub>
</p>
