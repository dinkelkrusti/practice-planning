# 0003. Supply chain policy for dependencies

- Status: Accepted
- Date: 2026-11-18
- Tag: process

## Context

Hijacked packages and malicious releases are usually caught within days of
publication. At the same time, staying on old versions means staying on known
vulnerabilities. The dependency set should stay small enough to overview.

## Decision

**Both sides**
- Exact versions only, lockfiles committed, CI installs strictly from them
  (`pnpm install --frozen-lockfile`, Maven Enforcer `banDynamicVersions`).
- **7-day release cooldown**: a version is only used once it has been public
  for at least 7 days. pnpm enforces it (`minimumReleaseAge: 10080`,
  `minimumReleaseAgeStrict`), Renovate applies it to update pull requests.
- **Security fixes skip the cooldown** (Renovate `vulnerabilityAlerts`).
- **OSV-Scanner** in CI on every pull request and push to `main` covers
  `backend/pom.xml` and both pnpm lockfiles. A finding **blocks** the build.
  An exception goes into `osv-scanner.toml` with a reason and an expiry date
  of at most 30 days, so it is a reviewed commit and comes back on its own.
- A **nightly scan of `main`** catches new advisories for unchanged
  dependencies and opens an issue instead of blocking anything.
- **GitHub Actions are pinned to commit SHAs**, with the version as a comment.
- **Renovate** proposes updates (grouped for Angular and Spring Boot).
- A **new direct dependency needs a one-line justification** in its pull request.

**Maven**
- The Spring Boot parent manages most versions; everything else is pinned.
- Enforcer rules: no version ranges or SNAPSHOTs, every plugin version
  explicit, dependency convergence (conflicting transitive versions fail the
  build and are resolved explicitly in `dependencyManagement`), minimum Maven
  and Java versions.
- Maven Wrapper committed, with `distributionSha256Sum` verifying the Maven
  download (checked against the checksum published by Apache).

**pnpm**
- Install scripts are blocked (`strictDepBuilds`); packages that need one are
  allowed one by one in `allowBuilds`.
- `trustPolicy: no-downgrade` rejects a version published with weaker
  provenance than earlier versions of the same package.
- `saveExact: true`, so nothing is added with a `^`.
- pnpm and Node versions are pinned in `package.json` (`packageManager`,
  `engines`).

**Other tools**
- oasdiff is installed with `go install` at a pinned version; Go verifies it
  against the public checksum database.
- The Postgres image is pinned by tag and digest.

## Alternatives considered

- **Dependabot.** Rejected for Renovate: better cooldown, grouping and
  security bypass.
- **Warn-only vulnerability scan.** Rejected: warnings get ignored.
- **14-day cooldown.** Considered; 7 days catches most incidents with less lag.

## Consequences

- Upgrades lag a week behind releases, by design.
- Occasionally a CVE without a fix blocks an unrelated pull request until a
  documented exception is added.
- Even with few direct dependencies, the Angular toolchain brings about 300
  packages in total. "Easy to overview" holds for direct dependencies.
- Known gap: the walking skeleton pins the Postgres image digest that was
  current when it was written, which was about a day old. From now on Renovate
  applies the cooldown to image updates too.
