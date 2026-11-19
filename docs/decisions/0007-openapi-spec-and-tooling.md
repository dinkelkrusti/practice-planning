# 0007. Spec-first OpenAPI 3.1 and its tooling

- Status: Accepted
- Date: 2026-11-19
- Tag: architecture

## Context

The API should be designed on its own terms and be the single source of truth
for server and client. We had to choose the OpenAPI version, how the spec is
organized, and the tools that lint, bundle, compare and generate from it. We
agreed to let a test with the real tools decide the OpenAPI version.

## Decision

- **Spec-first.** The hand-written spec in `api/` defines the API. The Kotlin
  server interfaces and the Angular client are generated from it; Kotlin code
  never defines API shape.
- **OpenAPI 3.1.**
- **Split by resource**: `paths/`, `schemas/`, `parameters/`, `responses/`,
  one file each. A schema's file name is its name in the bundle.
- **Redocly CLI** (one tool) lints (`recommended-strict` plus our rules) and
  bundles.
- **The bundled single-file spec `api/openapi.bundled.yaml` is committed.**
  Both generators and oasdiff read it. CI fails if it is not up to date.
- **Server generator**: openapi-generator `kotlin-spring` through its Maven
  plugin, `interfaceOnly`, `useSpringBoot4`, Jackson 3, no Swagger
  annotations, no springdoc. Controllers implement the generated interfaces.
- **Client generator**: see [0008](0008-angular-client-generator.md).
- **Breaking change check**: oasdiff, see [0004](0004-api-evolution.md).

## Tooling test (2026-11-19)

A probe spec with the cases where 3.1 differs from 3.0 (required nullable
field, optional nullable field for PATCH, `oneOf` with discriminator and
`const`, an `x-extensible` enum, a `$ref` with a sibling `description`) was
written in both versions and run through every tool:

| Tool | Version | 3.1 result |
| --- | --- | --- |
| Redocly CLI | 2.77.0 | Lints and bundles cleanly |
| openapi-generator `kotlin-spring` | 7.27.0 | Same Kotlin as from 3.0; nullability correct. Logs "3.1 support is still in beta" |
| ng-openapi-gen | 1.1.0 | Correct types, `$ref` description kept |
| @hey-api/openapi-ts | 0.99.0 | Correct types, `$ref` description kept |
| oasdiff | 1.37.0 | Same breaking-change findings as for 3.0 |

Findings worth remembering:

- **Generators lose schema names when they read the split files directly**
  (openapi-generator produced `ListPieces200Response` and duplicates). Hence
  the committed bundle.
- **`oneOf` with a discriminator** generates non-compiling Kotlin when the
  subtypes declare the discriminator as `const` or single-value `enum` (in 3.0
  and 3.1 alike). Declare the discriminator property as plain `string` in
  subtypes when we first need `oneOf`.
- **PATCH "absent vs null"**: by default the Kotlin model cannot tell an absent
  field from an explicit `null`. It needs `openApiNullable=true` (adds the
  `jackson-databind-nullable` dependency). To be decided with the PATCH
  conventions.
- Extensible enums and oasdiff: see [0005](0005-enums.md).

## Alternatives considered

- **Code-first** (spec derived from annotated Kotlin). Rejected: the API would
  be shaped by the implementation, and spec diffs would not be reviewable.
- **OpenAPI 3.0.3.** No tool needed it, and 3.1 expresses nullability and
  documentation more cleanly.
- **Spectral for linting plus a separate bundler.** Redocly covers both.
- **Generating the bundle at build time instead of committing it.** The
  Maven build would then need Node. The committed file also gives reviewers
  the whole contract in one place.
- **fabrikt** for Kotlin: nicer output, but no real Maven integration.

## Consequences

- A spec change means: edit the split files, run `pnpm bundle` in `api/`,
  commit both.
- When the generators' 3.1 support leaves beta, nothing changes for us.
