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

# all: compile-javac
all: gradescope-setup

# Compile Java files using javac
compile-javac:
	mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) -cp $(CLASSPATH) $(JAVA_FILES)

# Compile Java files using Maven
compile-mvn:
	$(MVN) compile

clean:
	mvn clean

build:
	mvn clean compile

test-all: run-unit-tests run-integration-tests run-other-tests
	

run-unit-tests:
	mvn test -Dtest=WeatherDataFileManagerTests,JsonUtilsTests

run-integration-tests:
	mvn test -Dtest=serverIntegrationTests

run-other-tests:
	mvn test -Dtest=TestDataExpiry,DistributedSystemTests,StatusCodeTests

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

# Installation of necessary tools and dependencies
install-mvn-mac:
	brew update
	brew install maven

# Ubuntu 22.04
install-mvn-linux-ubuntu:
	if command -v sudo > /dev/null; then \
		sudo apt-get install apt-utils; \
		sudo apt-get update && sudo apt-get install -y maven; \
	else \
		apt-get install apt-utils; \
		apt-get update && apt-get install -y maven; \
	fi	

# Setting up the Maven project
setup:
	$(MVN) package
	$(MVN) install
	$(MVN) compile

MVN_PATH := $(shell which mvn 2> /dev/null)

check-mvn:
ifndef MVN_PATH
		@echo "Maven not found. Installing..."
		make install-mvn-linux-ubuntu
else
		@echo "Maven is already installed at $(MVN_PATH)"
endif

gradescope-setup: install-mvn-linux-ubuntu compile-mvn
