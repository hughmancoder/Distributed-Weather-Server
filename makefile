AGGREGATION_SERVER_ARGS = 4567
CONTENT_SERVER_ARGS = http://localhost:4567 src/main/resources/weather_data.txt
CLIENT_ARGS = http://localhost 4567 
CURRENT_DIR := $(shell pwd)

build:
	mvn clean compile

test-all:
	@echo "Running Tests..."
	mvn test

run-unit-tests:
	mvn test -Dtest=WeatherDataFileManagerTests,JsonUtilsTests

run-integration-tests: 
	mvn test -Dtest=serverIntegrationTests

run-synchronisation-tests: 
	mvn test -Dtest=SynchronisationTests

run-all-servers-linux:
	gnome-terminal --tab --active --title="Aggregation Server" -- make run-aggregation-server
	gnome-terminal --tab --active --title="Content Server" -- make run-content-server	

run-all-servers-mac:
	osascript -e 'tell app "Terminal" to do script "cd $(CURRENT_DIR) && make run-aggregation-server"'
	osascript -e 'tell app "Terminal" to do script "cd $(CURRENT_DIR) && make run-content-server"'

run-aggregation-server:
	mvn exec:java -Dexec.mainClass="com.AggregationServer" -Dexec.args="$(AGGREGATION_SERVER_ARGS)"

run-content-server:
	mvn exec:java -Dexec.mainClass="com.ContentServer" -Dexec.args="$(CONTENT_SERVER_ARGS)"

run-client:
	mvn exec:java -Dexec.mainClass="com.GETClient" -Dexec.args="$(CLIENT_ARGS)"
