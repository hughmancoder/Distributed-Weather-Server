compile:
	mvn compile
	mvn package

run-app:
	@echo "Running App..."
	java -cp target/my-app-1.0-SNAPSHOT.jar com.App

run: compile run-app

test:
	@echo "Running Tests..."
	mvn test
