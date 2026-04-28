# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**DOSW-TechCup-users** — Backend REST API microservice within the TechCup Fútbol platform. Responsible exclusively for user profile management and player sports profiles. Part of a larger microservices architecture where authentication/authorization is handled by a separate identity service and an orchestrator (API Gateway).

Base package: `edu.dosw.users`

## Microservices Architecture Context

This service does **not** handle authentication or JWT — those belong to other services:

| Service | Responsibility |
|---|---|
| **users** (this repo) | User profile CRUD, sports profiles, player search, team invitations |
| **identity** | Registration, login, JWT generation, roles and permissions |
| **orchestrator** | API Gateway — JWT validation, request routing |
| **entity** | Teams management |

Requests arrive at this service already authenticated (validated by the orchestrator). This service trusts incoming requests and applies only business-level authorization.

## Commands

```bash
# Build (compile only)
mvn -B -ntp clean compile

# Run all tests with coverage check (85% minimum enforced via JaCoCo)
mvn -B -ntp verify

# Run a single test class
mvn -B -ntp test -Dtest=ClassName

# Run the application locally (requires active profile, see Configuration section)
mvn spring-boot:run

# Package JAR
mvn -B -ntp clean verify
# Output: target/DOSW-TechCup-users-1.0-SNAPSHOT.jar
```

## Architecture

Layered REST API — controllers → service → repository:

- **Persistence (relational):** Spring Data JPA → PostgreSQL (production) / H2 in-memory (local + CI)
- **Persistence (documents):** Spring Data MongoDB — used for player photo storage
- **API docs:** springdoc-openapi v2.3.0 (Swagger UI at `/swagger-ui.html`, disabled in production)
- **DTO mapping:** MapStruct v1.6.0 (annotation processor — mappers generated at compile time, must run before Lombok)
- **Boilerplate reduction:** Lombok (annotation processor)

## Domain

Users have two data layers:
1. **User** — basic info (full name, email, school relation, academic program, semester, status). Email and password are managed by the identity service — not stored or modified here.
2. **SportProfile** — player position (goalkeeper/defender/midfielder/forward), jersey number, photo (stored in MongoDB), availability flag. Cannot be deleted; cannot be updated while assigned to a team.

Roles in the system: `jugador`, `capitan`, `organizador`, `arbitro`, `administrador`. Role assignment is managed by the identity service.

## Configuration

Three properties files control environment-specific behavior:

| File | Loaded when | Database |
|---|---|---|
| `application.properties` | Always (base config) | — |
| `application-local.properties` | Profile = `local` | H2 in-memory |
| `application-prod.properties` | Profile = `prod` | PostgreSQL (Neon) + MongoDB |

`application-local.properties` is in `.gitignore` — each developer creates it locally from `application-local.properties.example`.

**IntelliJ setup:** Run → Edit Configurations → Active profiles → `local`

**Azure App Service variables required:**

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | Neon PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Neon username |
| `SPRING_DATASOURCE_PASSWORD` | Neon password |
| `SPRING_DATA_MONGODB_URI` | MongoDB Atlas connection URI |

## CI/CD Pipeline

`.github/workflows/ci-cd.yml` — four sequential stages:

1. **BUILD** — `mvn clean compile` (all branches)
2. **TEST** — `mvn verify` + JaCoCo report upload (all branches)
3. **QUALITY** — SonarCloud analysis with `App.java` excluded from coverage (all branches)
4. **DEPLOY** — Azure Web App deployment (`main` branch only)

**Branch strategy:** `feature/**` and `develop` → stages 1–3. `main` → all stages including deploy.

## Quality Gates

- **JaCoCo:** ≥ 85% instruction coverage enforced on `mvn verify`. `App.java` is excluded.
- **SonarCloud:** ≥ 80% coverage on new code. `App.java` excluded via `-Dsonar.coverage.exclusions=**/App.java`.
- GitHub Actions variables required: `SONAR_ORGANIZATION`, `SONAR_PROJECT_KEY` (vars) and `SONAR_TOKEN`, `AZURE_WEBAPP_PUBLISH_PROFILE` (secrets).