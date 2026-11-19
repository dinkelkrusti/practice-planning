# 0006. Database migrations with Liquibase, expand/contract

- Status: Accepted
- Date: 2026-11-19
- Tag: architecture

## Context

Rolling updates mean that the previous backend version keeps running against
the database for a while after a migration has run.

## Decision

- **Liquibase** owns the schema. Hibernate only validates it
  (`ddl-auto: validate`).
- Changesets are **Liquibase formatted SQL**, one file per changeset in
  `backend/src/main/resources/db/changelog/changes/`, listed in order in
  `db.changelog-master.yaml`. A merged changeset is never edited.
- **Expand/contract**: every changeset must work with the current and the
  previous backend version. A rename becomes: add the new column, write to
  both, backfill, switch reads, and drop the old column in a later release.
  A destructive change never ships together with the code that stops using
  the old structure.
- **Forward-only**: a bad migration is fixed with a new changeset, not with
  Liquibase rollback.
- In production, migrations run as a **Kubernetes Job before the rollout**,
  not on startup of every pod. Locally and in tests they run on startup.

## Alternatives considered

- **Flyway.** Equivalent for this purpose; Liquibase is better known here.
- **XML or YAML changesets.** Rejected: SQL is exactly what runs, needs no
  workarounds for Postgres features, and reads well in review.

## Consequences

- Schema changes often take two or three releases.
- Reviewers must check each changeset for compatibility with the previous
  release; this is enforced by review, not by tooling.
