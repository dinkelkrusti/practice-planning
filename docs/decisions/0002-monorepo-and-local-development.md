# 0002. Monorepo layout and local development

- Status: Accepted
- Date: 2026-11-19
- Tag: architecture

## Context

The app has an OpenAPI spec, a Kotlin backend and an Angular frontend, later
deployment manifests and Terraform for its own Kubernetes cluster. Changes to
the API usually touch the spec, the server and the client at once.

## Decision

One repository:

| Folder | Content | Build tool |
| --- | --- | --- |
| `api/` | OpenAPI spec, the source of truth for the API | pnpm (Redocly CLI) |
| `backend/` | Spring Boot service | Maven (wrapper) |
| `frontend/` | Angular app | pnpm (Angular CLI) |
| `deploy/` | Kubernetes manifests (later) | |
| `infra/` | Terraform for the Hetzner cluster (later) | |
| `docs/` | Onboarding guide and decision records | |

- Each part keeps its native tooling. There is **no root task runner** (no
  Makefile, no root `package.json`); CI calls `mvnw` and `pnpm` directly, and
  the README documents the few local commands.
- Local development uses a **real PostgreSQL 18** in Docker:
  `backend/compose.yaml` is started automatically by Spring Boot's Docker
  Compose support on `spring-boot:run`, and integration tests use
  Testcontainers with the same, digest-pinned image.
- CI runs on GitHub Actions, one workflow per part, triggered only by changes
  to that part (or to `api/`, which feeds both sides).

## Alternatives considered

- **Separate repositories.** Rejected: an API change would need three
  coordinated pull requests.
- **Gradle driving everything, or a Makefile at the root.** Rejected: Gradle
  is not wanted, and a root runner adds a tool to learn and maintain for two
  commands.
- **H2 instead of PostgreSQL locally.** Rejected: the segment tree will want
  recursive queries and Postgres features H2 only imitates, and Liquibase
  scripts would have to target both databases.

## Consequences

- One pull request can change spec, server and client together.
- Local development needs Docker; `docs/onboarding.md` sets it up on Windows
  without Docker Desktop.
