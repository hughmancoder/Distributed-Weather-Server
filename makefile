build:
	mvn clean compile

# run-app:
# 	@echo "Running App..."
# 	java -cp target/my-app-1.0-SNAPSHOT.jar com.App

run-server:
	mvn exec:java -Dexec.mainClass="com.AggregationServer"

run-client:
	mvn exec:java -Dexec.mainClass="com.GETClient"

test:
	@echo "Running Tests..."
	mvn test
