package com;

import java.nio.file.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.models.WeatherData;
import com.utility.LamportClock;
import com.utility.JsonUtils;
import java.io.IOException;

public class AggregationServer {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final String TEMP_STORAGE_PATH = "../../resources/temp_storage.json";
    public static final String DATA_FILE_PATH = "../../resources/weather_data.json";
    private static LamportClock lamportClock = new LamportClock();
    public static HashMap<String, WeatherData> weatherDataMap = new HashMap<>();

    public static void main(String[] args) {
        int port = 4567;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.out.println("Invalid port number. Using default port 4567.");
            }
        }

        try {
            loadDataFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load data from file.");
            return; // If data loading is critical, we should terminate the application
        }

        try {
            // Start HTTP server
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/data", new DataHandler());
            server.setExecutor(Executors.newFixedThreadPool(10)); // Set executor (null will create a default executor)
            server.start();

            // Initialize timer to remove old data entries
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    removeOldEntries();
                }
            }, 30000, 30000); // Run every 30 seconds

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to start HTTP server.");
        }
    }

    public static void saveDataToFile() {
        lock.lock();
        try {
            String json = JsonUtils.toJson(weatherDataMap);
            atomicFileWrite(json, TEMP_STORAGE_PATH);
            Files.move(Paths.get(TEMP_STORAGE_PATH), Paths.get(DATA_FILE_PATH), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Handle exception
        } finally {
            lock.unlock();
        }
    }

    public static void loadDataFromFile() throws IOException {
        try {
            if (Files.exists(Paths.get(DATA_FILE_PATH))) {
                String json = new String(Files.readAllBytes(Paths.get(DATA_FILE_PATH)));
                // Assuming that JsonUtils.fromJson() returns the correct type
                HashMap<String, WeatherData> dataMap = JsonUtils.fromJson(json, HashMap.class);
                if (dataMap != null) {
                    weatherDataMap.putAll(dataMap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle IO Exception (maybe log it or notify the user)
        } catch (Exception e) {
            e.printStackTrace();
            // Handle any other exceptions (maybe log it or notify the user)
        }
    }

    private static void atomicFileWrite(String content, String path) throws IOException {
        Files.write(Paths.get(path), content.getBytes(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    // TODO
    private static void removeOldEntries() {
        // Efficiently remove entries older than 30 seconds
        // You could either a timestamp or the Lamport clock to identify stale
    }

    static class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestMethod = httpExchange.getRequestMethod();

            if ("GET".equals(requestMethod)) {
                handleGetRequest(httpExchange);
            } else if ("PUT".equals(requestMethod)) {
                handlePutRequest(httpExchange);
            } else {
                httpExchange.sendResponseHeaders(400, 0); // Bad Request
            }
        }

        // TODO
        private void handleGetRequest(HttpExchange httpExchange) {
            // Handle GET Request here
        }

        private void handlePutRequest(HttpExchange httpExchange) throws IOException {
            // Extract the WeatherData from the HttpExchange and call the static method
            int statusCode = AggregationServer.handlePutRequest(new WeatherData());
            httpExchange.sendResponseHeaders(statusCode, 0); // Respond with the status code
        }
    }

    // TODO
    public static int handlePutRequest(WeatherData data) {
        int statusCode;
        if (weatherDataMap.isEmpty()) {
            statusCode = 201; // HTTP_CREATED
        } else {
            statusCode = 200; // HTTP_OK
        }

        lock.lock();
        try {
            weatherDataMap.put(data.getId(), data);
            saveDataToFile();
        } catch (Exception e) {
            e.printStackTrace();
            statusCode = 500; // Internal Server Error
        } finally {
            lock.unlock();
        }

        return statusCode;
    }
}
