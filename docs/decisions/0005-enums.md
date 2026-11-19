# 0005. Enums that may grow

- Status: Accepted, follow-up open (see the end)
- Date: 2026-11-19
- Tag: architecture

## Context

Adding a value to an enum in a **response** is formally additive, but it
breaks clients that handle enums exhaustively, which TypeScript and Kotlin
both encourage. An old frontend in an open browser tab would receive a value
its generated types say cannot exist. Enums in **requests** can grow safely.
Removing a value is breaking in both directions.

## Decision

Decide per enum, and make the decision mandatory:

- **Truly fixed sets** (sort direction, days of the week): closed enums.
  Adding a value is a breaking change.
- **Response enums that may grow**: marked `x-extensible: true` in the spec.
  Clients must handle unknown values (a fallback branch, enforced by a helper
  and lint rule in the frontend when the first such enum appears).
- **Sets the user can extend** (likely activity types): not an enum at all,
  but data served by a resource (for example `GET /activity-types`).
- A **Redocly lint rule** (`rule/enum-declares-extensibility` in
  `api/redocly.yaml`) fails the build for any enum that does not declare
  `x-extensible: true` or `false`.

## Alternatives considered

- **Every addition is breaking.** Too rigid for sets that naturally grow.
- **Plain `string` with documented values** (Zalando's `x-extensible-enum`).
  Honest types, but the generators produce plain strings and lose
  autocompletion.

## Follow-up found by the tooling test (2026-11-19)

oasdiff (v1.37.0) reports adding a value to a response enum as a breaking
change, even when the schema carries our `x-extensible: true`. It only accepts
growth for Zalando's `x-extensible-enum` *instead of* `enum` (verified: with
`enum` plus any marker it still fails; with `x-extensible-enum` alone it
passes). So as configured, CI would block every new value of an extensible
enum. Options, to be decided before the first extensible enum is added (the
skeleton has none):

1. Switch extensible enums to `x-extensible-enum` (the alternative above):
   oasdiff understands it natively; generators then produce `string`.
2. Keep `enum` + `x-extensible: true`, and add a small CI step that rewrites
   such enums to `x-extensible-enum` before running oasdiff.
3. Keep `enum` + `x-extensible: true`, and acknowledge each addition in an
   oasdiff ignore file in the same pull request.
