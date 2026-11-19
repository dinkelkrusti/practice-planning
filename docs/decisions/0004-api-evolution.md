# 0004. API versioning and evolution

- Status: Accepted
- Date: 2026-11-19
- Tag: architecture

## Context

The long-term goal is continuous deployment with rolling updates. During a
rollout, old and new backend pods run side by side, and browsers that loaded
the old frontend keep calling the new backend. Every release must therefore be
compatible with its neighbors, and breaking changes must be prevented
mechanically rather than by discipline.

## Decision

- **The spec has its own semver** in `info.version`: additive change → minor,
  documentation only → patch. It is versioned independently of backend and
  frontend releases.
- **Only the major version is in the URL** (`/api/v1/...`). A new major
  version is an emergency exit for a change that cannot be made compatibly,
  not a normal tool.
- **oasdiff in CI** compares every pull request's spec with `main` and fails
  on breaking changes. The job summary shows the API changelog.
- **Evolve, don't break**: add the new element, mark the old one
  `deprecated: true` (plus `Deprecation` and `Sunset` response headers),
  migrate clients, remove it only when no supported client uses it.
- **Deploy order**: backend first, then frontend.
- **The spec is not published as a package.** Backend and frontend read
  `api/` directly. This changes once there are clients we do not deploy
  together (for example a mobile app).
- **Deprecation window: short** (web only): an element can go once the
  frontend release that stopped using it has been live for a few days.
- **Machinery for a long window, built now**, so that switching to a long
  window later is a policy change only:
  1. Every client sends `Client-Version` (frontend: `clientVersionInterceptor`).
  2. The backend adds it to the logging context and logs every call to a
     deprecated operation with that version (`ClientVersionInterceptor`).
  3. The frontend detects a newer deployment and asks the user to reload.
     *Not built yet: it needs a deployment to compare against; it comes with
     the deployment work.*
  4. A "minimum supported client version" endpoint waits until a mobile app
     exists.

## Alternatives considered

- **Header or media-type versioning.** Rejected: the URL is visible, easy to
  route at the ingress and conventional.
- **Publishing the spec as Maven and npm packages consumed at pinned
  versions.** Rejected for now: overhead without benefit while one repository
  deploys both sides.
- **A long deprecation window from day one.** Rejected: keeps legacy code and
  database columns around for months without a client that needs it.

## Consequences

- Removing anything from the API takes at least two deploys.
- The API must be designed with care, since mistakes can only be fixed by
  adding, not by changing.
