# 0008. Angular client generator: ng-openapi-gen

- Status: Proposed (Erik to confirm)
- Date: 2026-11-19
- Tag: architecture

## Context

The usual choice, openapi-generator's `typescript-angular` through its npm
CLI, downloads a Java JAR at runtime, which conflicts with
[0003](0003-supply-chain.md). We agreed to compare two pure-npm generators on
the same spec.

## Comparison

Both were run on the tooling probe spec ([0007](0007-openapi-spec-and-tooling.md))
and installed under the supply chain policy.

| | ng-openapi-gen 1.1.0 | @hey-api/openapi-ts 0.99.0 |
| --- | --- | --- |
| Packages installed (incl. transitive) | 24 | 55, plus TypeScript as a peer |
| Release line | 1.x, stable; last release 2026-09-23 | 0.x (minors may break); last release 2026-06-22 |
| Generated lines for the probe | ~600 | ~2,150 (it copies its own HTTP runtime into the output) |
| Angular integration | `HttpClient`, one tree-shakeable function per operation, an `Api` service with `invoke()` returning a Promise | Own client on top of `HttpClient`; optional plugin generating `httpResource` (signal) functions |
| Types | Interfaces, `oneOf` as plain union | Types, `oneOf` as discriminated union |
| 3.1 handling | Correct | Correct |
| Coupling | None to the TypeScript compiler | Uses the TypeScript compiler API; with TypeScript 7 installed it crashed (`ts.SyntaxKind` undefined) and only worked with TypeScript 6 |

## Decision

**ng-openapi-gen.** It is smaller, stable, has no coupling to the TypeScript
compiler version (which matters because Angular moves to new TypeScript
versions every few months), and its output is plain and easy to read.

Signals come from our side: each feature wraps the generated functions in a
small service that exposes Angular's `resource()` (see
`frontend/src/app/pieces/pieces.service.ts`). Components never import
generated code, so switching generators later stays local to these services.

## Alternatives considered

- **@hey-api/openapi-ts.** Its generated `httpResource` functions are
  attractive and it is familiar from earlier work, but it brings twice the
  dependencies, is pre-1.0, and its dependence on the TypeScript compiler API
  already broke once in this test.
- **openapi-generator `typescript-angular`.** Rejected: runtime JAR download
  and Java in the frontend build.

## Consequences

- The generated client is not committed; `pnpm generate:api` runs before
  `start`, `build` and `test`.
- We write one small wrapper service per feature.
