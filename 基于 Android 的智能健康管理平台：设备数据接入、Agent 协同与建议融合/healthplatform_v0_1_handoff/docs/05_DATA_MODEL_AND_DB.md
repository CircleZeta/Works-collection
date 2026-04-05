# Data Model and Database

## 1. Domain models
### MetricType
- HEART_RATE
- STEPS
- SLEEP_DURATION
- SLEEP_DEEP
- SLEEP_LIGHT

### HealthMetric
Internal normalized metric record.

Fields:
- id
- userId
- metricType
- value
- unit
- timestamp
- source

### HealthSummary
Aggregate summary used by dashboard and agent context.

### HealthContext
Agent-facing packaged summary for a time range.

### AdviceCategory
- EXERCISE
- DIET
- SLEEP
- RISK

### AgentResult
Single raw output from one agent.

### FinalSuggestion
User-facing fused suggestion.

### TrendPoint
Daily aggregated value for trend rendering.

## 2. Room schema
### Table: health_metrics
Purpose: store normalized health metric records

Columns:
- id INTEGER PRIMARY KEY AUTOINCREMENT
- userId TEXT
- metricType TEXT
- value REAL
- unit TEXT
- timestamp INTEGER
- source TEXT

### Table: final_suggestions
Purpose: store latest fused suggestions

Columns:
- id INTEGER PRIMARY KEY AUTOINCREMENT
- title TEXT
- category TEXT
- content TEXT
- explanation TEXT
- priority INTEGER
- sourceAgents TEXT
- createdAt INTEGER

## 3. DAO capabilities
### HealthMetricDao
- insert one
- insert many
- get metrics by date range
- get metrics by metric type and date range
- observe recent metrics
- delete by user

### FinalSuggestionDao
- insert many
- clear all
- observe suggestions

## 4. Aggregation strategy in v0.1
Aggregation is done in Kotlin inside the repository implementation, not in SQL.

Why:
- easier to reason about
- enough for small demo dataset
- better for Codex-controlled expansion

## 5. Summary rules
- average heart rate = mean of heart rate values in range
- resting heart rate = minimum heart rate value in range
- steps = sum of step metrics in range
- sleep duration = sum of sleep duration metrics in range

## 6. Trend rules
For each day and metric type:
- HEART_RATE -> average
- STEPS -> sum
- SLEEP_DURATION -> sum
- SLEEP_DEEP -> sum
- SLEEP_LIGHT -> sum
