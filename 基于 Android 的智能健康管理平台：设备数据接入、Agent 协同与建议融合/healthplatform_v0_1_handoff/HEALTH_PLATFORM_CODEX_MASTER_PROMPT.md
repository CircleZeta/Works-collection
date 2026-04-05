# Codex Master Prompt — HealthPlatform v0.1 Freeze

You are acting as the code generation and completion agent for an Android project.

Your task is to continue implementation **strictly based on the already-frozen v0.1 scope** of the project below. You must treat the existing architecture, naming, layering, responsibilities, and version boundaries as the source of truth. Do **not** redesign the project unless a change is absolutely necessary to make the current v0.1 implementation compile and run.

---

## 1. Core mission

Build and complete an Android prototype project named:

**HealthPlatform**

Project title in product documents:

**基于 Android 的智能健康管理平台：设备数据接入、Agent 协同与建议融合**

The project is a **platform-style health management prototype**, not a medical diagnosis app. It focuses on:

1. receiving wearable or simulated health data,
2. standardizing and storing time-series data,
3. constructing a unified health context,
4. calling multiple health Agents,
5. fusing multiple Agent outputs,
6. showing dashboard, trends, and advice in the Android UI.

This is **not** a model-accuracy project. This is primarily a **software engineering and Android architecture project**.

---

## 2. v0.1 freeze boundary

You must anchor all implementation to the following frozen version boundary.

### Included in v0.1

- Android app implemented in **Kotlin-first**.
- Jetpack Compose UI.
- Manual dependency wiring through an `AppContainer`.
- Room local storage.
- Mock data import for recent 7 days.
- Three mock health agents:
  - exercise agent
  - diet agent
  - sleep agent
- Agent coordinator.
- Rule-based suggestion fusion engine.
- Four bottom navigation screens:
  - Dashboard
  - Trends
  - Advice
  - Settings
- Advice screen can:
  - import mock data
  - generate suggestions
  - display fused suggestions
- Dashboard screen reads current-day summary.
- Trends screen reads 7-day aggregated data.

### Explicitly excluded from v0.1

Do not add these unless required later by a new instruction:

- real Bluetooth integration
- real wearable SDK integration
- real backend services
- authentication / login
- cloud sync
- Hilt / DI framework migration
- chart library integration unless specifically requested later
- clinical diagnosis logic
- complex rule engine
- multiple users beyond a hardcoded demo user
- large-scale refactoring away from the current package plan

If some excluded capability is mentioned in comments or docs, treat it as future work, not current scope.

---

## 3. Product positioning

This software is a **platform prototype for wearable-data-driven health assistance**.

It is **not** intended to provide medical diagnosis. It only provides:

- health status assistance,
- lifestyle suggestions,
- trend display,
- software architecture demonstration of:
  - data ingestion,
  - standardization,
  - local persistence,
  - multi-agent orchestration,
  - result fusion,
  - UI presentation.

Keep all wording and behavior aligned with “daily health management assistance”, not diagnosis.

---

## 4. Required architecture constraints

Use the following layered structure and keep responsibilities clean.

### Package structure

```text
com.example.healthplatform
├── data
│   ├── local
│   │   ├── dao
│   │   ├── db
│   │   └── entity
│   ├── mapper
│   └── repository
├── domain
│   ├── agent
│   │   └── impl
│   ├── fusion
│   ├── model
│   ├── repository
│   └── usecase
├── presentation
│   ├── advice
│   ├── dashboard
│   ├── settings
│   ├── trends
│   └── navigation
├── ui
│   └── theme
└── common
```

### Responsibility constraints

- `data`: Room entities, DAO, DB, mappers, repository implementations.
- `domain`: domain models, repository interfaces, use cases, agents, fusion logic.
- `presentation`: ViewModels, UI state models, Compose screens, navigation.
- `common`: app container and shared utilities.
- `ui.theme`: Compose theme and styling.

### Important rules

- Do not place business logic directly in Compose screens.
- Do not place orchestration logic directly in ViewModel if a use case or coordinator is more appropriate.
- Do not bypass repository interfaces from presentation layer.
- Do not merge Room entity models directly into domain models.
- Do not introduce unnecessary abstraction layers beyond current scope.

---

## 5. Existing domain model contract

The following concepts are already part of the frozen version and should remain stable.

### MetricType
Includes at least:
- `HEART_RATE`
- `STEPS`
- `SLEEP_DURATION`
- `SLEEP_DEEP`
- `SLEEP_LIGHT`

### HealthMetric
Represents standardized internal time-series metric.
Fields:
- `id: Long = 0L`
- `userId: String`
- `metricType: MetricType`
- `value: Double`
- `unit: String`
- `timestamp: Long`
- `source: String`

### HealthSummary
Represents summarized health data for agent input and dashboard.
Fields:
- `avgHeartRate: Double?`
- `restingHeartRate: Double?`
- `todaySteps: Int`
- `yesterdaySteps: Int`
- `sleepDurationHours: Double?`
- `deepSleepHours: Double?`
- `lightSleepHours: Double?`
- `stepTrendRatio: Double?`
- `sleepTrendRatio: Double?`

### HealthContext
Fields:
- `userId`
- `startTime`
- `endTime`
- `summary: HealthSummary`

### AdviceCategory
Includes:
- `EXERCISE`
- `DIET`
- `SLEEP`
- `RISK`

### AgentResult
Fields:
- `agentName`
- `category`
- `riskLevel`
- `confidence`
- `suggestion`
- `explanation`
- `createdAt`

### FinalSuggestion
Fields:
- `title`
- `category`
- `content`
- `explanation`
- `priority`
- `sourceAgents`
- `createdAt`

### TrendPoint
Fields:
- `dayLabel`
- `metricType`
- `value`

Do not arbitrarily rename these unless necessary for compile correctness and consistency across all layers.

---

## 6. Existing local database contract

Room database already conceptually contains two main tables.

### `health_metrics`
Fields:
- `id`
- `userId`
- `metricType`
- `value`
- `unit`
- `timestamp`
- `source`

### `final_suggestions`
Fields:
- `id`
- `title`
- `category`
- `content`
- `explanation`
- `priority`
- `sourceAgents`
- `createdAt`

Codex should preserve this schema unless a minimal correction is necessary for actual Room compilation.

---

## 7. Existing repository contract

### `HealthDataRepository`
Must support at least:
- saving metrics
- retrieving metrics in time range
- building summary for a time window
- building trend data for a metric over recent N days

### `SuggestionRepository`
Must support at least:
- saving fused suggestions
- observing fused suggestions as flow

Preserve interface/implementation separation.

---

## 8. Existing agent contract

There is a unified agent abstraction.

### `HealthAgent`
- property: `name`
- function: `analyze(context: HealthContext): AgentResult`

### Existing agents
- `MockExerciseAgent`
- `MockDietAgent`
- `MockSleepAgent`

### Existing coordinator
`AgentCoordinator` should orchestrate multiple agents, ideally concurrently using coroutines.

Do not replace the mock agents with remote API integration in v0.1.

---

## 9. Existing fusion contract

A rule-based `SuggestionFusionEngine` already exists conceptually.

Expected behavior:
- sort agent results by risk level descending, then confidence descending
- resolve simple conflicts
- prefer recovery-related sleep advice over low-priority exercise advice when sleep risk is high
- output up to 3 final suggestions

Do not replace with complex inference or ML fusion.

---

## 10. Existing use case contract

The following use cases exist conceptually and should remain the center of orchestration.

- `ImportMockDataUseCase`
- `GenerateSuggestionsUseCase`
- `GetDashboardSummaryUseCase`
- `GetTrendDataUseCase`

### `ImportMockDataUseCase`
Must generate mock data for **recent 7 days**, not just one day.

It should provide at least three metric families:
- steps
- heart rate
- sleep duration

Important behavior:
- avoid destructive duplication from repeated import if possible,
- if dedup or replacement logic is implemented, keep it simple and deterministic.

### `GenerateSuggestionsUseCase`
Expected sequence:
1. read data summary from repository,
2. build `HealthContext`,
3. call coordinator,
4. fuse results,
5. save final suggestions.

---

## 11. Existing UI structure contract

There are four bottom navigation pages.

### Dashboard
Shows today summary:
- today steps
- average heart rate
- sleep duration
- short status summary

### Trends
Shows 7-day trend data for:
- steps
- average heart rate
- sleep duration

At v0.1, list/card rendering is acceptable. Do not force chart libraries.

### Advice
Supports:
- import mock data
- generate today suggestions
- observe and show fused suggestion list

### Settings
Placeholder page is acceptable in v0.1, but the page should exist and fit navigation structure.

---

## 12. Existing dependency wiring contract

Dependency injection is **manual**, via an `AppContainer`.

`AppContainer` should create and expose:
- Room database
- repositories
- coordinator
- fusion engine
- use cases

Do not migrate to Hilt in this version.

---

## 13. Technical stack constraints

Use the following stack unless compile constraints force minor version changes:

- Kotlin
- Jetpack Compose
- Navigation Compose
- Lifecycle ViewModel
- Coroutines / Flow
- Room
- KSP for Room compiler

Keep implementation simple and robust.

---

## 14. Coding style requirements

Follow these rules while generating code.

1. Prefer clarity over abstraction.
2. Keep file names aligned with class names.
3. Keep packages stable.
4. Use Kotlin idioms where appropriate.
5. Use `StateFlow` for UI state.
6. Use `viewModelScope` in ViewModels.
7. Keep Compose screens mostly declarative.
8. Avoid one giant file.
9. Add minimal comments only where structure benefits.
10. Make the app compile as a normal Android Studio project.

---

## 15. What you should generate or complete

Your default responsibility is to:

1. complete missing files,
2. repair compile issues,
3. ensure package imports are correct,
4. ensure Room compiles,
5. ensure navigation compiles,
6. ensure screens render,
7. ensure the end-to-end prototype runs.

### Minimal runnable scenario

The resulting v0.1 project should support this demo flow:

1. launch app,
2. enter Advice page,
3. click “import mock data”,
4. click “generate today suggestions”,
5. see fused suggestions,
6. switch to Dashboard and see current-day summary,
7. switch to Trends and see 7-day aggregated values.

This is the core acceptance path.

---

## 16. What you must not do

Do **not** do the following unless explicitly requested later:

- redesign the architecture from scratch
- convert project into MVI, Redux, or other large pattern shift
- add networking for real APIs
- add Bluetooth stack
- add cloud services
- add authentication
- add analytics SDKs
- add chart libraries by default
- add complicated caching beyond current local DB flow
- replace mock agents with LLMs or backend integrations
- introduce multi-module Gradle architecture unless asked

---

## 17. Recommended implementation order

If files are missing or incomplete, complete them in the following order:

### Phase 1: compile-safe domain and data foundation
1. domain models
2. Room entities
3. DAO interfaces
4. Room database
5. mappers
6. repository interfaces and implementations

### Phase 2: business chain
7. mock agents
8. `AgentCoordinator`
9. `SuggestionFusionEngine`
10. use cases

### Phase 3: presentation chain
11. UI state classes
12. ViewModels
13. ViewModel factories
14. navigation route definitions
15. Compose screens
16. top-level app scaffold
17. `MainActivity`

### Phase 4: stabilization
18. fix imports and package mismatches
19. fix build.gradle dependencies
20. ensure manifest/theme/resources are sufficient
21. validate runnable demo path

---

## 18. Acceptance checklist

Your output should satisfy all of the following.

### Build / project
- Android Studio project structure is valid.
- Gradle sync should be possible.
- Room compiler config is correct.
- Navigation Compose imports are correct.

### Runtime flow
- App launches without crashing.
- Bottom navigation works.
- Advice screen buttons work.
- Imported mock data produces visible UI changes.
- Suggestion generation produces 1–3 fused items.
- Dashboard can show same-day summary after import.
- Trends can show recent 7-day aggregated values.

### Architecture
- Data/domain/presentation responsibilities remain separate.
- Manual `AppContainer` is used.
- Use cases remain the orchestration boundary.

---

## 19. If source files already exist

When existing files are present:

- preserve naming and directory structure,
- modify minimally,
- do not delete stable architectural pieces,
- prefer filling gaps over rewriting everything.

If there is a mismatch between docs and code, prefer the implementation path that best preserves the v0.1 frozen intent and produces a working project with minimal drift.

---

## 20. Expected output format from Codex

When you work on this project, respond with:

1. a brief summary of what files you created or changed,
2. the actual code or patch content,
3. any compile caveats if something remains unresolved,
4. a final note on whether the v0.1 demo path is satisfied.

Avoid high-level discussion unless necessary. Prioritize concrete implementation.

---

## 21. Short operational instruction

Proceed as the implementation agent for the frozen v0.1 HealthPlatform Android prototype.

Do not expand scope.
Do not redesign.
Do not optimize prematurely.
Complete the current codebase into a stable, compilable, runnable Android prototype aligned with the architecture and contracts above.

