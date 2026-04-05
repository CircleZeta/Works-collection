# Use Cases

## Actors
- End user
- Mock data source
- Health agents

## Core use cases
### UC-01 Import mock data
Actor: End user  
Goal: seed demo records for recent 7 days

Main flow:
1. user opens Advice page
2. user taps import mock data
3. app generates recent 7-day metrics
4. app stores metrics in Room
5. downstream pages can now read data

### UC-02 View dashboard
Actor: End user  
Goal: see today's summary

Main flow:
1. user opens Dashboard
2. app loads today's records
3. app builds summary
4. app renders steps, average heart rate, sleep duration, and a status text

### UC-03 View trends
Actor: End user  
Goal: see recent changes

Main flow:
1. user opens Trends page
2. app aggregates recent 7-day data by metric type
3. app renders list-based trend sections for steps, heart rate, and sleep

### UC-04 Generate advice
Actor: End user  
Goal: get current fused suggestions

Main flow:
1. user opens Advice page
2. user taps generate advice
3. app builds today's health context
4. app invokes three health agents concurrently
5. app fuses returned agent results
6. app stores final suggestions
7. app renders suggestion cards

### UC-05 View advice explanation
Actor: End user  
Goal: understand advice origin

Main flow:
1. advice card appears in Advice page
2. user reads content, explanation, and source agent labels

## Failure handling
- if no data exists, dashboard/trends show placeholders
- if some agents fail, coordinator returns surviving results
- if all agents fail, advice screen reports failure without app crash
