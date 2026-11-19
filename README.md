# Practice Planning

An app for planning structured practice of musical pieces, piano first. A
musician cuts a piece into a tree of segments, practices activities per
segment (reading, tempo, articulation, ...), and gets practice sessions
planned from time estimates that learn from how long things actually take.

Status: walking skeleton. The architecture runs end to end with one resource
(pieces); the domain model is being designed next.

## Getting started

New to the project? Follow the [developer onboarding guide](docs/onboarding.md)
to set up WSL, Docker Engine and everything else needed to build and test the app.

Short version, once set up (Windows, PowerShell, two windows):

```powershell
cd backend;  .\mvnw.cmd spring-boot:run     # API on :8080, starts PostgreSQL in Docker
cd frontend; pnpm install; pnpm start        # UI on http://localhost:4200
```

## Repository layout

| Folder | What | Stack |
| --- | --- | --- |
| [`api/`](api/) | OpenAPI 3.1 spec, the single source of truth for the API | Redocly CLI (lint, bundle) |
| [`backend/`](backend/) | REST service; implements interfaces generated from the spec | Kotlin, Spring Boot 4 (MVC, virtual threads), Hibernate, Liquibase, PostgreSQL 18, Maven |
| [`frontend/`](frontend/) | Web app; calls the API through a client generated from the spec | Angular 22 (standalone, signals, zoneless), ng-openapi-gen, Vitest, pnpm |
| [`docs/`](docs/) | [Onboarding guide](docs/onboarding.md) and [decision records](docs/decisions/) | |

How a change to the API flows:

```
api/ (split YAML) ──pnpm bundle──▶ api/openapi.bundled.yaml ─┬─▶ Maven: Kotlin server interfaces
                                                             ├─▶ pnpm: Angular client
                                                             └─▶ CI: oasdiff blocks breaking changes
```

## How decisions are made

Every architecture, domain and process decision is recorded with its
reasoning and rejected alternatives in [`docs/decisions/`](docs/decisions/).

## Quality gates (CI)

- **API**: Redocly lint, committed bundle up to date, no breaking changes against `main` (oasdiff)
- **Backend**: generate, compile, unit and integration tests against a real PostgreSQL (Testcontainers)
- **Frontend**: Prettier, Vitest, production build
- **Supply chain**: OSV-Scanner on every change and nightly; exact pins, 7-day release cooldown, Actions pinned to SHAs, Renovate for updates ([details](docs/decisions/0003-supply-chain.md))
