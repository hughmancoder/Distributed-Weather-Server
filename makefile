MVN = mvn
JAVAC = javac
SRC_DIR = src/main/java
BIN_DIR = target/classes
GSON_JAR = bin/gson-2.8.8.jar
CLASSPATH = $(BIN_DIR):lib/*:$(GSON_JAR)
JAVA_FILES = $(shell find $(SRC_DIR) -name "*.java")

AGGREGATION_SERVER_ARGS = 4567
CONTENT_SERVER_ARGS = http://localhost:4567 src/main/resources/weather_data.txt
CLIENT_ARGS = http://localhost 4567
CURRENT_DIR := $(shell pwd)

all: install-mvn-linux-fedora setup

# Installation of necessary tools and dependencies
install-mvn-mac:
	brew update
	brew install maven

install-mvn-linux-fedora:
	sudo dnf install maven

# Setting up the Maven project
setup:
	$(MVN) compile
	$(MVN) package
	$(MVN) install

# Compile Java files using javac
compile-javac:
	mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) -cp $(CLASSPATH) $(JAVA_FILES)

# Compile Java files using Maven
compile-mvn:
	$(MVN) compile

clean:
	mvn clean

setup:
	mvn compile
	mvn package
	mvn install

build:
	mvn clean compile

test-all:
	@echo "Running Tests..."
	mvn test

run-unit-tests:
	mvn test -Dtest=WeatherDataFileManagerTests,JsonUtilsTests

run-integration-tests:
	mvn test -Dtest=serverIntegrationTests

run-other-tests:
	mvn test -Dtest=TestDataExpiry,StatusCodeTests

run-all-servers-linux:
	gnome-terminal --tab --active --title="Aggregation Server" -- make run-aggregation-server
	gnome-terminal --tab --active --title="Content Server" -- make run-content-server

run-all-servers-mac:
	osascript -e 'tell app "Terminal" to do script "cd $(CURRENT_DIR) && make run-aggregation-server"'
	osascript -e 'tell app "Terminal" to do script "cd $(CURRENT_DIR) && make run-content-server"'

run-aggregation-server:
	mvn exec:java -Dexec.mainClass="weatherServer.AggregationServer.AggregationServer" -Dexec.args="$(AGGREGATION_SERVER_ARGS)"

run-content-server:
	mvn exec:java -Dexec.mainClass="weatherServer.ContentServer" -Dexec.args="$(CONTENT_SERVER_ARGS)"



run-client:
	mvn exec:java -Dexec.mainClass="weatherServer.GETClient" -Dexec.args="$(CLIENT_ARGS)"
