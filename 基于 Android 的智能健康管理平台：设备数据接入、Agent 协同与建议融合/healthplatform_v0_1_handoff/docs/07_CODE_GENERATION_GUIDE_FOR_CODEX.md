# Codex Code Generation Guide

This document tells Codex how to interpret and continue the current project snapshot.

## 1. Intent
Treat `android-app/` as the authoritative v0.1 source freeze.  
Do not redesign architecture unless requested.

## 2. Priority rules
1. preserve current packages and layer boundaries
2. prefer explicit code over abstract indirection
3. keep Kotlin-first
4. keep mock agents for v0.1
5. keep manual AppContainer for v0.1
6. compile before optimizing

## 3. Expected next-step tasks after v0.1
Only do these when explicitly requested:
- improve settings page
- add agent toggles
- unify time helpers
- add chart rendering for trends
- replace mock agents with real remote APIs
- add BLE ingestion adapter
- add tests

## 4. Constraints
- do not add Java without a concrete external dependency reason
- do not add Hilt in v0.1
- do not introduce large libraries casually
- do not convert list-based trend rendering to charts unless requested
- do not change domain model names without migration need

## 5. Known limitations
- repeated mock import overwrites user metrics through delete-then-insert
- dashboard summary only covers today's data
- advice generation uses today's summary instead of multi-day context
- settings page is placeholder only
- no chart library yet
- no BLE ingestion yet
- no instrumentation tests yet

## 6. Completion standard for Codex
When asked to continue from this freeze, Codex should:
- keep existing modules compiling
- extend use cases rather than bypass them in UI
- extend repository contracts carefully
- preserve simple navigation structure
