# Distributed Systems Assignment 2

Hugh Signoriello | a1829716

## Weather Data Aggregator

Weather Data Aggregator is a distributed system designed to aggregate and serve weather data efficiently. It consists of the following primary components:

1. **Aggregation Server:** Serves as the central repository for storing and managing weather data.
2. **Content Server:** Facilitates reading and uploading weather data to the Aggregation Server.
3. **GET Client:** Enables querying the Aggregation Server to retrieve and display weather data.

## Setup Instructions

### Prerequisites

Ensure Maven is installed on your machine:

For **Fedora Linux**:

```bash
make install-mvn-linux-fedora
```

For **Mac**:

```bash
make install-mvn-mac
```

### Initial Setup

Execute the following commands for initial setup:

```bash
make setup
```

> **Note:** All tests will be executed as Maven compiles.

### Compilation

To compile the code:

```bash
make compile-mvn
```

> `compile-javac` is merely a placeholder to ensure compatibility with the GradeScope environment.

## Running the Servers

For **Mac**:

```bash
make run-all-servers-mac
```

For **Linux**:

```bash
make run-all-servers-linux
```

Alternatively, you can run the Aggregation Server, Content Server, and GET Client in separate terminal windows:

- Aggregation Server: `make run-aggregation-server`
- Content Server: `make run-content-server`

## API Endpoints

### Aggregation Server

- **Fetch all weather data:**

  [http://localhost:4567/weather/](http://localhost:4567/weather/)

- **Retrieve weather data by station ID:**

  [http://localhost:4567/weather?station=STATION_ID](http://localhost:4567/weather?station=STATION_ID)

### Content Server

- **Upload new weather data via PUT request:**

  [http://localhost:4567/weather.json](http://localhost:4567/weather.json)

## Running Tests

Ensure that the ports for the Aggregation Server (`4567`) and Content Server (`4568`) are not in use:

- Run all tests: `make test-all`
- Execute unit tests: `make run-unit-tests`
- Execute integration tests: `make run-integration-tests`
- Run other tests: `make run-other-tests`

## Makefile Details

For a detailed understanding of the Makefile and its commands, please refer to the provided Makefile. It contains scripts for installing dependencies, setting up the environment, compiling sources, and executing various components of the system.

---

This format is well-structured and should provide clarity on the steps required to set up, run, and test the Weather Data Aggregator system.

## Features

- data expiry on aggregation server every 30 seconds
- data persistenence and recovery in case of server crash through intermediate storage
- lamport clocks maintain multithreaded and distributed system environment read and write safety
- get and put request available from api endpoints
- get requests available for all weather data and specific weather station ids

### Aggregation Server Features

- **HTTP Endpoints**: Supports HTTP PUT for ingesting new weather data and HTTP GET for retrieving stored weather data.
- **JSON Management**: Handles the storage and retrieval of weather data in JSON format.
- **Lamport Clocks**: Utilises Lamport clocks for synchronising operations and maintaining the order of incoming data.
- **Data Expiry**: Automatically removes stale data based on age or if the content server that provided it goes offline.

### Content Server Features

- **Data Reading**: Reads weather data from a local file.
- **JSON Transformation**: Converts the local data into JSON format for standardisation.
- **Data Upload**: Sends the JSON-formatted weather data to the Aggregation Server using HTTP PUT requests.

### GET Client Features

- **HTTP Requests**: Makes HTTP GET requests to the Aggregation Server for fetching weather data.
- **Data Parsing and Display**: Receives, parses, and displays the JSON-formatted weather data.

## Data Flow

1. The Content Server reads local weather data and converts it to JSON format.
2. The Content Server sends this JSON data to the Aggregation Server using an HTTP PUT request.
3. The Aggregation Server stores the data and updates its Lamport clock.
4. A GET Client requests weather data from the Aggregation Server using an HTTP GET request.
5. The Aggregation Server responds with the most recent JSON-formatted weather data.
6. The GET Client parses and displays the received data.

## Aggregation System with Lamport Clocks

## Implementation Details

- **Assigning Timestamps to Events:**
  - Every event, such as a request or response, triggers the `tick()` method in the `LamportClock`.
  - Nodes synchronise their clock using the `sync()` method upon receiving an event from another node.
  - The ordering time variable, a number type inside the `LamportClock` class, is updated during GET and POST requests.
  - The relationship is determined using timestamps: if event A's timestamp is less than B's, A happened before B.

## Testing Methodologies

### 1. **Regressive Testing:**

- After modifying methods, previous tests were rerun to ensure consistency.
- Significant in refactoring phases, like when changing from `gson` to manual JSON parsing.

### 2. **Unit Testing:**

- **WeatherDataFileManagerTests:**

  - `testReadFileAndParse()`: Checks data retrieval and parsing.
  - `testWeatherDataMapToFileAndReadBack()`: Verifies read-write consistency.
  - `testConcurrentWrite()`: Examines concurrent write capability.
  - `testInvalidFilePath()`: Checks invalid file path handling.
  - `testEmptyFile()`: Verifies handling of an empty file.

- **JsonUtilsTests:**
  - `testToJsonAndFromJson()`: Validates bidirectional JSON conversion.
  - `testGetDataFromJsonFile()`: Checks JSON file parsing.
  - `testJsonToWeatherDataMap()`: Validates JSON to HashMap conversion.
  - `testParseStringToJson()`: Checks JSON string parsing.
  - `testManualJsonToMap()`: Tests manual JSON-to-map conversion.

### 3. **Testing Harness for Distributed Entities:**

- Simulates interactions among distributed entities.
- Uses multi-threading to emulate multiple clients and servers.
- Ensures order and synchronisation using a Lamport clock instance.

### 4. **Synchronisation and Fault Testing:**

- **Synchronisation Testing with Lamport Clocks:**

  - Ensure entities maintain operation order based on Lamport clocks.
  - Test clock ticking and synchronisation after PUT or GET requests.

- **Fault Testing:**
  - Includes server crashes, invalid requests, unexpected client behaviour, and network issues.
  - Tests include checking error responses, restart recoverability, and network issue handling.

### 5. **Edge Cases:**

- Explored various scenarios like missing station IDs, empty inputs, and concurrent PUT operations.
- Used locks and Lamport clocks to handle concurrent interactions and ensure synchronisation.

## References

- [Java Documentation](https://docs.oracle.com/)
- [Distributed Systems Course Notes - MyUni](https://myuni.adelaide.edu.au/courses/85272/modules)

---

This README is now more structured with concise descriptions for better readability.
