# Product Requirements and Software Requirements

## 1. Problem statement
The application targets a wearable-data scenario. It is not a medical diagnosis tool. It is a mobile health assistance platform that:
- receives health-related time-series data
- standardizes data into internal models
- constructs a unified health context
- invokes multiple specialist agents
- fuses returned suggestions
- displays understandable user-facing recommendations

## 2. Business goals
1. Accept and manage user health time-series data
2. Support unified invocation of multiple health analysis agents
3. Fuse potentially conflicting or redundant suggestions
4. Present status summaries, trends, and advice clearly on Android

## 3. Primary actors
### 3.1 End user
The user checks dashboard summaries, trends, and generated advice.

### 3.2 Device data source
The source can be a wearable device in the future, but v0.1 uses a mock generator.

### 3.3 External agent service
In v0.1, agents are in-app mock implementations. The interface is already separated so real services can replace them later.

## 4. Functional requirements
### FR-01 Data import
The system shall accept health data from a mock source and save it locally.

### FR-02 Data standardization
The system shall convert all incoming records into a unified internal metric model.

### FR-03 Local persistence
The system shall persist standardized metrics in Room.

### FR-04 Summary construction
The system shall compute health summaries from local records for a time range.

### FR-05 Multi-agent invocation
The system shall dispatch a unified health context to multiple agents.

### FR-06 Suggestion fusion
The system shall rank, filter, and fuse agent results into final suggestions.

### FR-07 Dashboard view
The system shall display today's key health summary.

### FR-08 Trend view
The system shall display recent 7-day aggregated trends.

### FR-09 Advice view
The system shall display fused advice and agent-origin explanations.

### FR-10 Settings placeholder
The system shall expose a settings page placeholder for future configuration.

## 5. Non-functional requirements
### NFR-01 Maintainability
Code shall separate data, domain, and presentation responsibilities.

### NFR-02 Extensibility
New agents and new metric sources shall be addable without large-scale rewrites.

### NFR-03 Stability
Partial failures from agents shall not crash the UI.

### NFR-04 Local-first privacy
Health data shall be stored locally first.

### NFR-05 Readability
The codebase shall remain small, explicit, and understandable by a code agent.

## 6. MVP acceptance criteria
A v0.1 build is acceptable when:
1. mock data import works
2. imported data persists in Room
3. dashboard displays same-day summary
4. trends page shows 7-day values
5. advice page generates fused suggestions from three mock agents
6. no manual code edits are needed across layers for ordinary demo flow
