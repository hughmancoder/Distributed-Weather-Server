# Distributed Systems Assignment 2

Hugh Signoriello | a1829716

## Weather Data Aggregator

A distributed system to aggregate and serve weather data in a coordinated manner. The system comprises three main components:

1. **Aggregation Server**: Central hub that stores and manages weather data.
2. **Content Server**: Reads and uploads weather data to the Aggregation Server.
3. **GET Client**: Queries the Aggregation Server for weather data and displays it.

### Aggregation Server

- **HTTP Endpoints**: Supports HTTP PUT for ingesting new weather data and HTTP GET for retrieving stored weather data.
- **JSON Management**: Handles the storage and retrieval of weather data in JSON format.
- **Lamport Clocks**: Utilises Lamport clocks for synchronising operations and maintaining the order of incoming data.
- **Data Expiry**: Automatically removes stale data based on age or if the content server that provided it goes offline.

### Content Server

- **Data Reading**: Reads weather data from a local file.
- **JSON Transformation**: Converts the local data into JSON format for standardisation.
- **Data Upload**: Sends the JSON-formatted weather data to the Aggregation Server using HTTP PUT requests.

### GET Client

- **HTTP Requests**: Makes HTTP GET requests to the Aggregation Server for fetching weather data.
- **Data Parsing and Display**: Receives, parses, and displays the JSON-formatted weather data.

## Data Flow

1. The Content Server reads local weather data and converts it to JSON format.
2. The Content Server sends this JSON data to the Aggregation Server using an HTTP PUT request.
3. The Aggregation Server stores the data and updates its Lamport clock.
4. A GET Client requests weather data from the Aggregation Server using an HTTP GET request.
5. The Aggregation Server responds with the most recent JSON-formatted weather data.
6. The GET Client parses and displays the received data.

## Additional features

- The system is able to handle data concurrency and eventual consistency, for which Lamport clocks are implemented.
- Data from inactive Content Servers or outdated weather data are be purged to maintain data integrity and relevance.

---

## How to run

`Refer to makefile`

### Compiling

From root directory run `mvn compile`

### Running the app

`mvn exec:java -Dexec.mainClass="com.example.App`

### Running tests

`mvn test`

### cleanup

`mvn clean`
