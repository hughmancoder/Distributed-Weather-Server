# Weather Data Aggregator: Implementation & Testing Strategy

## Implementation Phases

### Phase 1: Basic Setup & Infrastructure

#### Aggregation Server

- [x] Basic `AggregationServer.java` with minimal functionality
- [x] File I/O operations for JSON data
- [x] Server startup on a specified port
- [x] Handling HTTP status codes 201 and 200
- [x] **Tests**: Unit tests for each functionality

#### GET Client

- [x] Basic `GETClient.java` with minimal functionality
- [x] Read command-line parameters for server and port
- [x] Implement GET request functionality
- [x] **Tests**: Unit tests for basic GET operations

#### Content Server

- [x] Basic `ContentServer.java` with minimal functionality
- [x] Read command-line parameters for server and file location
- [x] File reading and text parsing
- [x] **Tests**: Unit tests for individual features

### Phase 2: Communication & Basic Operations

#### Basic Communication

- [x] Communication between Aggregation Server and GET Client
- [x] Communication between Aggregation Server and Content Server
- [x] **Tests**: Integration tests for communication

#### PUT Operations

- [x] Implement PUT operations in Content Server
- [x] PUT request handling in Aggregation Server
- [x] **Tests**: Unit and Integration tests for PUT operations

#### GET Operations

- [x] Advanced GET operations in GET Client
- [x] **Tests**: Unit and Integration tests for GET operations

### Phase 3: Advanced Features & Error Handling

#### Lamport Clocks

- [x] Implementation in all components: Maintain a local Lamport clock. You may need to include it in the HTTP headers or in the JSON payload itsmake relf to synchronize with other systems.
- [x] add remote lamport method for getClient
- [x] **Tests**: Unit and Integration tests for synchronization

#### Error Handling

- [ ] HTTP status codes 400, 500, and 204 in Aggregation Server
- [ ] Retry mechanisms in both GET and Content Servers
- [ ] **Tests**: Unit tests for error codes and retry mechanisms

#### Data Expiry

- [ ] 30-second data expiry in Aggregation Server
- [ ] **Tests**: Unit and Integration tests for data expiry

### Phase 4: Optimisation & Cleanup

- [ ] Code quality and comments
- [ ] Comprehensive and integration testing
- [ ] Test persistance among server restarts and crashes
- [ ] Optional: Implement bonus features like custom JSON parsing

## Code Quality Checklist (Appendix A)

### Do

- [ ] Write comments above methods
- [ ] Describe special cases
- [x] Maintain modular code

### Don't

- [ ] Use magic numbers
- [x] Use comments as structural elements
- [ ] Leave spelling errors or TODO blocks

## Functionality Checklist (Appendix B)

### Basic Functionality

- [x] Text sending and receiving
- [x] Startup and initial communication
- [x] PUT and GET operations
- [ ] Data expiry within 30 seconds
- [ ] Error retry mechanism

### Full Functionality

- [x] Lamport clock synchronization
- [ ] Implement all error codes
- [ ] Server replication for fault-tolerance

### Bonus Functionality

- [ ] Manual JSON parsing for extra points

## Testing

- [ ] Unit tests for individual components and methods
- [ ] More rigorous testing, try invalid files and different data
- [ ] Integration tests for interactions and parallel requests
- [ ] Tests for server crash and recovery, network failure, and incorrect JSON

## Other

- [ ] API documentation
- [ ] JSON Parser in JsonUtils
