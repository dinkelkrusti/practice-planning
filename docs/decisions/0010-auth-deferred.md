# 0010. No authentication in the walking skeleton

- Status: Accepted
- Date: 2026-11-19
- Tag: architecture

## Context

A security scheme in the OpenAPI spec only describes authentication; Spring
Security enforces it. Declaring one now would anticipate an undecided choice:
for single-page apps, a session cookie through a backend-for-frontend is often
preferred over bearer tokens in the browser, and the two are different
schemes.

## Decision

The skeleton has no authentication and no security scheme. Both are added in
a dedicated auth round, **before anything is deployed**. Until then the Redocly
rules `security-defined` and `operation-4xx-response` are switched off, with a
comment to turn them back on.

## Consequences

- No client exists before auth arrives, so adding it is not a breaking change
  for anyone.
- The auth round adds a test that checks every operation's declared security
  against what Spring Security actually enforces.
