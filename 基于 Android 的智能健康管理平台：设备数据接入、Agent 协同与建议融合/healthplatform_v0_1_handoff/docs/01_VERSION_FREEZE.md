# HealthPlatform v0.1 Version Freeze

## Project name
HealthPlatform

Recommended Chinese title:
基于 Android 的智能健康管理平台：设备数据接入、Agent 协同与建议融合

## Freeze objective
This version freezes the first demonstrable MVP, centered on a complete vertical slice instead of full product completeness.

## What is included in v0.1
1. Android client skeleton built with Kotlin and Jetpack Compose
2. Room local persistence
3. Internal standardized health metric model
4. Three mock health agents:
   - exercise agent
   - diet agent
   - sleep agent
5. Agent coordination and rule-based suggestion fusion
6. Four pages:
   - dashboard
   - trends
   - advice
   - settings
7. Mock data import for recent 7 days
8. Dashboard daily summary
9. Trend aggregation for recent 7 days in list form
10. Suggestion generation and display

## What is intentionally excluded from v0.1
1. Real wearable device Bluetooth integration
2. Real backend API calls
3. Authentication and multi-account management
4. Chart libraries
5. Dependency injection frameworks such as Hilt
6. Cloud sync
7. Clinical diagnosis logic
8. Background periodic scheduling
9. Agent configuration persistence
10. Comprehensive testing suite

## Technical stance
- Primary language: Kotlin
- Architecture bias: layered / clean-ish separation
- Priority order:
  1. implementation efficiency
  2. code clarity
  3. maintainability
  4. extensibility

## Core value of this version
This is not yet a complete health product. It is a platform-oriented mobile prototype proving:
- wearable-like data can be standardized locally
- multiple agents can be orchestrated uniformly
- returned suggestions can be fused into user-facing guidance
- the same data pipeline can feed dashboard, trends, and advice pages

## Codex boundary rules
When continuing from this freeze:
- do not expand scope unless explicitly requested
- do not replace mock agents with real APIs in-place
- do not introduce Java unless a concrete SDK requires it
- do not add charts before the list-based trend page is kept stable
- do not refactor to Hilt in this version unless dependency management becomes blocking
