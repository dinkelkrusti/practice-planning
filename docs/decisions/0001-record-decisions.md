# 0001. Record architecture and domain decisions

- Status: Accepted
- Date: 2026-11-18
- Tag: process

## Context

The design of this app is worked out in long discussions. Without a written
trail, the reasons behind a choice get lost, and later changes either repeat
old debates or break assumptions nobody remembers. The project is also meant to
show how it was engineered, and the reasoning matters more than the stack.

## Decision

Every significant decision gets a short record in `docs/decisions/`, numbered
in order, tagged `architecture`, `domain` or `process`. Domain decisions (how
pieces, segments, activities and estimates work) are recorded the same way as
technical ones. Records are immutable once accepted; a change of mind is a new
record that supersedes the old one.

## Alternatives considered

- **Architecture-only ADRs in `docs/adr/`.** Rejected: domain decisions are
  just as important here, and one numbered sequence keeps them together.
- **A single design document.** Rejected: it hides when and why things
  changed, and grows into something nobody rereads.

## Consequences

- A pull request that makes a significant decision includes its record.
- Writing a record takes a few minutes and forces the alternatives to be named.
