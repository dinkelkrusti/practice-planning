# 0011. Deployment on an own Kubernetes cluster on Hetzner

- Status: Accepted, details open
- Date: 2026-11-18
- Tag: architecture

## Context

The app will run on a small Kubernetes cluster on Hetzner that serves only
this app. Running it is also a learning goal and part of showing the project.

## Decision

- Cluster infrastructure (Terraform) lives in this repository under `infra/`,
  with its own pipeline that app commits never trigger. App manifests live
  under `deploy/`.
- The Angular app is served by its own small nginx container. An ingress routes
  `/api` to the backend on the same domain, so there is no CORS and both
  deploy independently.
- Terraform state goes to Hetzner Object Storage, never into git. Secrets are
  stored encrypted (SOPS or Sealed Secrets), never in plain text.

## Open, to be decided when there is an app worth running

Cluster distribution (for example k3s via kube-hetzner, or Talos), Helm or
Kustomize, GitOps (Argo CD or Flux) or CI push, PostgreSQL operation
(likely CloudNativePG with backups to Object Storage), and environments.
