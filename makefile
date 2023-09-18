build:
	mvn clean compile

# run-app:
# 	@echo "Running App..."
# 	java -cp target/my-app-1.0-SNAPSHOT.jar com.App

run-server:
	mvn exec:java -Dexec.mainClass="com.AggregationServer"


CLIENT_ARGS = https://localhost 4567 IDS60901

run-client:
	mvn exec:java -Dexec.mainClass="com.GETClient" -Dexec.args="$(CLIENT_ARGS)"


test:
	@echo "Running Tests..."
	mvn test
