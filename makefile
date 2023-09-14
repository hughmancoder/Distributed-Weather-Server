compile:
	mvn compile
	mvn package

run-app:
	@echo "Running APP..."
	java -cp target/my-app-1.0-SNAPSHOT.jar com.example.App

run: compile run-app