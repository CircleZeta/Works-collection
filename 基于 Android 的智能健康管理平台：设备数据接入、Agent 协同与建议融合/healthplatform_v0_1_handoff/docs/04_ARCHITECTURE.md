# Architecture

## 1. High-level architecture
The project follows a lightweight layered structure:

- Presentation layer
- Domain layer
- Data layer

External systems are abstracted behind interfaces.

## 2. Layer responsibilities
### Presentation
Contains Compose screens, UI state, ViewModels, and navigation.

### Domain
Contains:
- models
- repository contracts
- use cases
- health agent abstraction
- agent coordinator
- suggestion fusion engine

### Data
Contains:
- Room entities, DAO, and database
- repository implementations
- entity/domain mappers

## 3. Core data flow
Mock import
-> standardized health metrics
-> Room persistence
-> summary or trend aggregation
-> health context construction
-> concurrent agent execution
-> fusion engine
-> final suggestions
-> Compose UI rendering

## 4. Rationale
This split keeps v0.1 explicit and easy for a code agent to extend without hidden framework behavior.

## 5. Dependency strategy
v0.1 uses a manual `AppContainer` instead of a dependency injection framework.

Benefits:
- no DI setup overhead
- small code footprint
- transparent object construction

## 6. Agent architecture
### Abstraction
`HealthAgent` defines a stable interface:
- `name`
- `analyze(context)`

### Coordinator
`AgentCoordinator` orchestrates all registered agents concurrently.

### Fusion
`SuggestionFusionEngine` sorts by risk/confidence and resolves simple conflicts.

## 7. Current conflict rule
If sleep advice has high risk, low-risk exercise advice is filtered out.  
This encodes the principle: recovery protection overrides low-priority activity promotion.

## 8. Deferred architectural items
The following are intentionally deferred:
- remote API adapters
- BLE adapters
- background workers
- charting module
- DI framework
- test suite organization
