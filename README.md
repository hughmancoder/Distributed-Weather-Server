# Distributed Systems Assignment 2

Hugh Signoriello | a1829716

## Weather Data Aggregator

A distributed system to aggregate and serve weather data in a coordinated manner. The system comprises three main components:

1. **Aggregation Server**: Central hub that stores and manages weather data.
2. **Content Server**: Reads and uploads weather data to the Aggregation Server.
3. **GET Client**: Queries the Aggregation Server for weather data and displays it.

## How to run

### First install maven

`make install`

Mac
`brew install maven`

Linux For Ubuntu-based systems:

`sudo apt update
sudo apt install maven`

### First time setup

`make setup`

Note that all tests will run as maven compiles

`compile-mvn` compiles folders(note compile-javac does not use maven and is simply a placeholder to pass gradescope environment compilation)

### Refer to makefile

`make run-all-servers-mac` runs all servers on mac
`make run-all-servers-linux` runs all servers on linux

alternatively in new terminal windows you can run the aggregate server, content server and get client respectively

`make test-all` runs all tests

`you can also seperate testing with`

`make run-unit-tests`

`make run-integration-tests`

`make run-other-tests`

## API Documentation

## Aggregation server

To get all weather on aggregate server

<http://localhost:4567/weather/>

To get weather by station on aggregate server

<http://localhost:4567/weather?station=STATION_ID>

## Content server

### Uploading a new file to aggregation server via content server thorugh PUT request

Use the api endpoint:

<http://localhost:4567/weather.json>

## Running tests

Make sure port aggregation server and content server are not running or alternatively 4567 and 4568 are free

## Features

- data expiry on aggregation server every 30 seconds
- data persistenence and recovery in case of server crash through intermediate storage
- lamport clocks maintain multithreaded and distributed system environment read and write safety
- get and put request available from api endpoints
- get requests available for all weather data and specific weather station ids

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

```

```
