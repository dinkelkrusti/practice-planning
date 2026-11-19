# 0009. Backend runtime: Spring MVC with virtual threads

- Status: Accepted
- Date: 2026-11-19
- Tag: architecture

## Context

Spring offers a blocking stack (MVC, one thread per request) and a reactive
one (WebFlux, a few event-loop threads, non-blocking end to end). The
persistence layer is Hibernate over JDBC, which is blocking.

## Decision

- **Spring MVC with virtual threads** (`spring.threads.virtual.enabled=true`).
  Blocking code stays simple; virtual threads make waiting on the database
  cheap. The pinning problem with `synchronized` was fixed in JDK 24.
- Versions (all at least 7 days old when chosen, see [0003](0003-supply-chain.md)):
  JDK 25, Spring Boot 4.1.2 (Spring Framework 7, Hibernate 7.4, Jackson 3),
  Kotlin 2.3.21 as managed by Spring Boot, Liquibase 5, PostgreSQL 18.7,
  Maven 3.9.16.
- Spring Boot's built-in RFC 9457 problem details for errors (provisional until
  the API conventions are discussed).
- Ids are time-ordered UUIDs (v7), generated in the application.

## Alternatives considered

- **WebFlux.** Would rule out regular Hibernate (R2DBC or Hibernate Reactive
  instead) and make debugging, transactions and security context harder, for
  throughput this app does not need.
- **Spring Boot 4.0.x** (what was discussed). 4.1.2 was the newest release
  older than 7 days, so the policy picked it.
- **Maven 3.10.1.** Released eight days earlier as the first patch of a new
  minor line; we stay on the mature 3.9 line for now.

## Consequences

- Live updates, if ever needed (for example a timer synced between devices),
  use Server-Sent Events in MVC.
