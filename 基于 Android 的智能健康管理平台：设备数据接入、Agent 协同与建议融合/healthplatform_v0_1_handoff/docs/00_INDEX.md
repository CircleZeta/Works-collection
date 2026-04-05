# HealthPlatform v0.1 Handoff Index

This package freezes the current version of the project discussion into a Codex-oriented handoff.

Contents:
- `01_VERSION_FREEZE.md`: version boundary and scope freeze
- `02_PRODUCT_REQUIREMENTS.md`: product and software requirements
- `03_USE_CASES.md`: use cases and functional flows
- `04_ARCHITECTURE.md`: architecture and module design
- `05_DATA_MODEL_AND_DB.md`: core models and Room schema
- `06_UI_AND_NAVIGATION.md`: page structure and UX flow
- `07_CODE_GENERATION_GUIDE_FOR_CODEX.md`: instructions for Codex to continue implementation
- `08_SOURCE_TREE.md`: current Android source tree
- `13_CURRENT_STATUS_FOR_MANAGER_GPT.md`: current verified status snapshot for manager-style GPT handoff
- `android-app/`: current total code snapshot for the v0.1 prototype

This v0.1 handoff is intentionally bounded:
- keep Kotlin-first
- keep local Room persistence
- keep mock agents instead of real remote APIs
- keep trends in list-form instead of chart-form
- keep manual dependency assembly instead of Hilt
