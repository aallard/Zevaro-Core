# Zevaro

### Continuous Outcome Engineering Platform

> **Replacing Agile with decision-velocity-optimized product management for the AI-first era**

[![License](https://img.shields.io/badge/license-Proprietary-blue.svg)]()
[![Java](https://img.shields.io/badge/Java-21-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green.svg)]()
[![Flutter](https://img.shields.io/badge/Flutter-3.x-blue.svg)]()

---

## The Problem

**Agile was designed for a world where development was the bottleneck.**

Teams had limited coding capacity, so methodologies emerged to manage that scarcity: sprints, story points, velocity charts, backlog grooming. Jira became the cockpit for this world—tracking who's working on what, estimating effort, measuring throughput.

**That world is ending.**

With AI-first development achieving **40x-500x speed improvements**, the bottleneck has fundamentally shifted. When a feature that took 2 weeks now takes 2 hours, the constraint is no longer *"how fast can we build?"* but **"how fast can we decide what to build?"**

Consider the math:

```
If your team can ship 50 features/week instead of 2,
but stakeholders can only approve 5 decisions/week...

You have a 10x mismatch.

Development sits idle—not because they can't code,
but because they're waiting on decisions.
```

**Zevaro makes this invisible bottleneck visible, measurable, and optimizable.**

---

## The Core Innovation: Decision Queue

Traditional tools track a **Backlog**—a list of work to be done.

Zevaro tracks a **Decision Queue**—a list of decisions blocking progress.

| Concept | Agile / Jira | Zevaro |
|---------|--------------|--------|
| **Primary artifact** | Backlog (work items) | Decision Queue (pending decisions) |
| **Success metric** | Velocity (points/sprint) | Decision Velocity (hours to resolution) |
| **Accountability** | Developer (story owner) | Stakeholder (decision owner) |
| **Cycle boundary** | Time-boxed (2-week sprint) | Outcome-boxed (until validated) |
| **Progress measure** | % complete | Hypothesis validated/invalidated |

### How It Works

The Decision Queue is a Kanban board tracking decisions moving from **Pending** → **Resolved**.

Each decision has:

- 🚨 **Urgency level** — Blocking, High, Normal, Low
- ⏰ **SLA deadline** — Based on urgency
- 👤 **Assigned stakeholder** — Responsible for resolution
- ⏱️ **Real-time timer** — How long the team has been waiting
- 📈 **Escalation path** — If SLA is breached

```
Developer: "I need to know if we're supporting offline mode."
                              ↓
              Creates a BLOCKING Decision
                              ↓
         Assigned to Product Manager (4-hour SLA)
                              ↓
         PM doesn't respond → Auto-escalates to Director
                              ↓
         System creates accountability where none existed
```

---

## The Methodology: Continuous Outcome Engineering

Zevaro implements **Continuous Outcome Engineering (COE)**—a methodology designed for AI-first development teams.

### 1. Outcomes, Not Features

In Agile, you ship features. In COE, you **validate outcomes**.

An **Outcome** is a measurable business result:
- *"Reduce customer support tickets by 20%"*
- *"Increase checkout conversion by 5%"*

Features are just hypotheses about how to achieve outcomes.

```
Outcome (business goal)
└── Hypothesis (proposed solution)
    └── Experiment (implementation + measurement)
        └── Decision (blocking question)
```

**If you can't articulate what outcome a feature serves, it doesn't get built.**

### 2. Hypothesis-Driven Development

Every feature is framed as a hypothesis with explicit success criteria:

```markdown
HYPOTHESIS: Adding a progress bar to checkout will reduce 
            cart abandonment by 15%.

VALIDATION: A/B test shows statistically significant improvement 
            (p < 0.05) over 2 weeks with 10,000 users.

BUILD TIME: 3 hours

DECISIONS REQUIRED:
  • Product approval (2hr SLA)
  • Legal review for data collection (24hr SLA)
```

Failed hypotheses aren't failures—they're **learnings that prevent wasted effort**.

### 3. Outcome-Boxed Cycles

| Agile | COE |
|-------|-----|
| Sprints are **time-boxed**: 2 weeks, regardless of completion | Cycles are **outcome-boxed**: ends when validated/invalidated |
| Artificial deadline pressure → cut corners | Natural completion → ship when ready |
| "Carry over" incomplete work sprint after sprint | Cycle ends when outcome is known |

### 4. Stakeholder Accountability Metrics

Agile measures **developer productivity** (story points, velocity).

COE measures **stakeholder responsiveness**.

Zevaro tracks:

| Metric | Description |
|--------|-------------|
| **Decision Velocity** | Average time from decision created → resolved |
| **Stakeholder Response Time** | Per-person resolution speed |
| **SLA Compliance Rate** | % of decisions resolved within SLA |
| **Escalation Rate** | How often decisions require escalation |

```
┌─────────────────────────────────────────────────────────┐
│  DECISION VELOCITY DASHBOARD                            │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Average Decision Wait Time:  3.2 days                  │
│                                                         │
│  BOTTLENECK IDENTIFIED:                                 │
│  └─ Legal Team: 8.4 days average response               │
│                                                         │
│  Development team idle time this week: 47 hours         │
│  └─ Waiting on: Product (23hr), Design (14hr),          │
│                 Legal (10hr)                            │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## How Teams Transform

### Product Teams

| Before | After |
|--------|-------|
| Write PRDs, groom backlogs, estimate stories | Define Outcomes with measurable criteria |
| Hope developers understand requirements | Approve/reject hypotheses, respond to Decisions within SLA |
| Judged on features shipped | Judged on outcomes achieved |

**The system surfaces when Product is the bottleneck:**

> *"You have 7 Blocking decisions awaiting response. Your average response time is 2.1 days. The development team has been idle for 14 hours waiting on you."*

### UX Teams

| Before | After |
|--------|-------|
| Create mockups, hand off, review, revise, repeat | Define visual hypotheses with acceptance criteria |
| Design reviews disappear into the void | Design reviews tracked as Decisions with SLAs |
| Weeks perfecting designs | Ship minimal, measure, iterate |

### Engineering Teams

| Before | After |
|--------|-------|
| Pick up stories, estimate points, code, wait | Validate hypotheses by building experiments |
| Blocked? Update ticket and wait | Blocked? Create Decision with SLA |
| Invisible waiting time | Idle time visible to leadership |

With AI-first development, an engineer's job becomes:

1. Translate hypotheses into technical experiments
2. Review and validate AI-generated code
3. Create Decisions when product/design input is needed
4. Measure outcomes and report results

### Leadership

| Before | After |
|--------|-------|
| Velocity charts showing points, not value | Decision Velocity showing exactly where pipeline is constrained |
| Guess at why projects are delayed | See which stakeholders are bottlenecks |
| Wonder why AI tools only deliver 2x improvement | Understand that decisions take 10x longer than development |

---

## Why This Replaces Jira

**Jira was built for tracking work** in a world where work was the bottleneck.

**Zevaro is built for tracking decisions** in a world where decisions are the bottleneck.

| Jira | Zevaro |
|------|--------|
| Backlog of work items | Queue of pending decisions |
| Story points for estimation | SLAs for accountability |
| Sprint velocity | Decision velocity |
| Developer-centric | Stakeholder-centric |
| Time-boxed sprints | Outcome-boxed cycles |
| Features shipped | Hypotheses validated |
| Burndown charts | Decision wait time charts |

When a developer can build a feature in **2 hours**, tracking that work in a 2-week sprint with story point estimation is **absurd overhead**.

What matters is: *how long did they wait for the decision to build it?*

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                           CLIENTS                                │
│   Flutter Web    │    Flutter Mobile    │    CLI / API          │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         API GATEWAY                              │
│            Rate Limiting │ Auth │ Routing │ Load Balancing      │
└─────────────────────────────┬───────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  ZEVARO CORE  │     │   ANALYTICS   │     │ INTEGRATIONS  │
│               │     │               │     │               │
│ • Outcomes    │     │ • Metrics     │     │ • Jira Sync   │
│ • Hypotheses  │     │ • Dashboards  │     │ • GitHub      │
│ • Decisions   │     │ • Reports     │     │ • Slack       │
│ • Stakeholders│     │ • AI Insights │     │ • Confluence  │
└───────┬───────┘     └───────┬───────┘     └───────┬───────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                               │
│     PostgreSQL (Primary)  │  Redis (Cache)  │  Kafka (Events)   │
└─────────────────────────────────────────────────────────────────┘
```

### Services

| Service | Purpose |
|---------|---------|
| **Core** | Outcomes, Hypotheses, Decisions, Stakeholders, Teams. SLAs, escalations, resolution tracking. |
| **Analytics** | Decision Velocity, Stakeholder Response Time, Outcome Velocity. Dashboards, weekly digests, trend analysis. |
| **Integrations** | Jira sync, GitHub commits → hypotheses, Slack notifications, Confluence publishing. |
| **Flutter SDK** | For teams building AI-first tools that integrate with the Decision Queue. |

### Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 21, Spring Boot 3.3, PostgreSQL 16, Redis 7, Kafka 3 |
| **Frontend** | Flutter 3, Riverpod, GoRouter |
| **Infrastructure** | Docker, Kubernetes, GitHub Actions, Prometheus + Grafana |

---

## The Meta-Point

**Zevaro was built using the methodology it enables.**

| Metric | Traditional | AI-First with Zevaro |
|--------|-------------|----------------------|
| **Estimated time** | 4-6 weeks | ~16 hours |
| **Speedup** | — | **100x** |

That speedup was only possible because decisions were made in **seconds, not days**:

- *"Flyway or Hibernate DDL?"* → Decided in seconds
- *"Separate Analytics service or embed in Core?"* → Decided in minutes
- *"What's the SLA for Blocking decisions?"* → Decided immediately

The bottleneck wasn't coding. **It was decisions.**

Zevaro makes that velocity achievable for entire organizations—not just individuals who happen to be fast decision-makers.

---

## The Market Opportunity

Every organization adopting AI-assisted development will hit this wall:

1. Deploy Copilot, Cursor, Claude
2. Find throughput only improves 2-3x, not 50x
3. Blame the AI tools

**The real problem:** Decision-making infrastructure wasn't designed for this speed.

Approval processes, review cycles, stakeholder availability—all calibrated for a world where development took weeks, not hours.

**Zevaro is the operating system for AI-first product development.**

It's not a better Jira—it's a **replacement for the entire Agile methodology**, built from first principles for a world where **code is cheap and decisions are expensive**.

---

## Getting Started

```bash
# Clone repository
git clone https://github.com/your-org/zevaro.git
cd zevaro

# Start infrastructure
docker-compose up -d postgres redis kafka

# Start backend
cd zevaro-core
./mvnw spring-boot:run

# Start frontend
cd ../zevaro-web
flutter run -d chrome
```

---

## Repository Structure

```
zevaro/
├── zevaro-core/           # Core backend service
├── zevaro-analytics/      # Analytics service
├── zevaro-integrations/   # Integrations service (planned)
├── zevaro-web/            # Flutter web frontend
├── zevaro-flutter-sdk/    # Client SDK for Flutter apps
├── docker-compose.yml     # Local development
└── docs/                  # Documentation
```

<p align="center">
  <b>Built for the AI-first era.</b><br>
  <i>Because in a world where code takes hours, decisions shouldn't take days.</i>
</p>



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
