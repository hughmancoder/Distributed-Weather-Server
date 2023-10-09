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
- [x] An event occurs locally (like processing a request). You tick the Lamport clock to move it forward.
      Before sending a message/request to another entity, you tick the Lamport clock and include the new value in the outgoing message.
      After receiving a message/request from another entity, you synchronize your local Lamport clock with the received Lamport clock value to keep them in sync.
- [x] **Tests**: Unit and Integration tests for synchronization

#### Error Handling

- [x] HTTP status codes 400, 500, and 204 in Aggregation Server
- [x] Retry mechanisms in both GET and Content Servers
- [x] **Tests**: Unit tests for error codes and retry mechanisms

#### Data Expiry

- [x] 30-second data expiry in Aggregation Server
- [x] **Tests**: Unit and Integration tests for data expiry

### Phase 4: Optimisation & Cleanup

- [x] Code quality and comments
- [x] Comprehensive and integration testing
- [x] Test persistance among server restarts and crashes
- [x] Optional: Implement bonus features like custom JSON parsing

## Other tests and tasks

- [x] Add retry mechanism to put request of order 10
- [x] Status code tests
- [x] Test concurrent and parallel content server environment
- [x] Write a script to test multiple content server put requests and get lamport clock

## Code Quality Checklist (Appendix A)

### Do

- [x] Write comments above methods
- [x] Describe special cases
- [x] Maintain modular code

### Don't

- [x] Use magic numbers
- [x] Use comments as structural elements
- [x] Leave spelling errors or TODO blocks

## Functionality Checklist (Appendix B)

### Basic Functionality

- [x] Text sending and receiving
- [x] Startup and initial communication
- [x] PUT and GET operations
- [x] Data expiry within 30 seconds
- [x] Error retry mechanism

### Full Functionality

- [x] Lamport clock synchronization
- [x] Implement all error codes
- [x] Server replication for fault-tolerance

### Bonus Functionality

- [x] Manual JSON parsing for extra points

## Testing

- [x] Unit tests for individual components and methods
- [x] More rigorous testing, try invalid files and different data
- [x] Integration tests for interactions and parallel requests
- [x] Tests for server crash and recovery, network failure, and incorrect JSON

## Other

- [x] API documentation
- [x] JSON Parser in JsonUtils

## Feedback

## Lamport Clock Implementation

### README UPDATE

1. **Introduction**:

   - Briefly describe the concept of Lamport Clocks.
   - Explain the significance of logical clocks in distributed systems.

2. **Implementation Details**:

   - Discuss how timestamps are assigned to events.
   - Elaborate on how the "happened before" relationship is determined using the Lamport timestamps.
   - Mention any optimizations or extensions you've implemented.

## References

- List any sources or references you used during your implementation and testing.

## My Uni Announcments

- [x] So, I highly advice everyone to write about their Lamport clock implementation (Write how you've implemented and it's working according to your code) and how you're planning on testing you Assignment 2 in the README file
- [x] Edit: Test files should be in text format and not makefile format.
