CONTENT_SERVER_ARGS = 4568 src/main/resources/weather_data.txt
AGGREGATION_SERVER_ARGS = 4567
CLIENT_ARGS = http://localhost 4567 

build:
	mvn clean compile

run-servers:
	run-aggregation-server run-content-server
	
run-aggregation-server:
	mvn exec:java -Dexec.mainClass="com.AggregationServer" -Dexec.args="$(AGGREGATION_SERVER_ARGS)"

run-content-server:
	mvn exec:java -Dexec.mainClass="com.ContentServer" -Dexec.args="$(CONTENT_SERVER_ARGS)"
	
run-client:
	mvn exec:java -Dexec.mainClass="com.GETClient" -Dexec.args="$(CLIENT_ARGS)"

test:
	@echo "Running Tests..."
	mvn test

test_: 
	mvn test -Dtest=ContentServerIntegrationTests.java

