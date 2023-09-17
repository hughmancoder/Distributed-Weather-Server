package com;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
// TODO: manually implement 
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.models.WeatherData;
import com.utility.LamportClock;
import com.utility.JsonUtils;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

public class AggregationServer {
    private static final ReentrantLock lock = new ReentrantLock();
    public static final String TEMP_STORAGE_PATH = "../../resources/temp_storage.json";
    public static final String DATA_FILE_PATH = "../../resources/weather_data.json";
    private static LamportClock lamportClock = new LamportClock();
    private static PriorityBlockingQueue<WeatherData> weatherDataQueue = new PriorityBlockingQueue<>();
    public static HashMap<String, Long> lastActiveMap = new HashMap<>();
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
            // TODO: data from content server
            loadDataFromFile(DATA_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load data from file.");
            return;
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/data", new DataHandler());
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    removeOldEntries();
                }
            }, 30000, 30000);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to start HTTP server.");
        }
    }

    public static void saveWeatherDataMapToFile() {
        // Lock to ensure the map is not modified while saving
        lock.lock();
        try {
            String json = JsonUtils.toJson(weatherDataMap);
            Path path = Paths.get(TEMP_STORAGE_PATH);

            // ensure directory exists
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }

            Files.write(path, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save data to file.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static void loadDataFromFile(String filePath) throws IOException {
        try {
            if (Files.exists(Paths.get(filePath))) {
                String json = new String(Files.readAllBytes(Paths.get(filePath)));
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

    private static void removeOldEntries() {
        lock.lock();
        try {
            long currentTime = lamportClock.getTime();
            lastActiveMap.entrySet().removeIf(entry -> currentTime - entry.getValue() > 30);
        } finally {
            lock.unlock();
        }
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
                httpExchange.sendResponseHeaders(400, 0);
            }
        }

        private void handleGetRequest(HttpExchange httpExchange) throws IOException {
            lock.lock();
            try {
                String json = JsonUtils.toJson(weatherDataMap);
                byte[] response = json.getBytes();
                httpExchange.sendResponseHeaders(200, response.length);
                httpExchange.getResponseBody().write(response);
                httpExchange.close();
            } finally {
                lock.unlock();
            }
        }

        private void handlePutRequest(HttpExchange httpExchange) throws IOException {
            // TODO: Extract WeatherData and Lamport timestamp here
            // Example: WeatherData data = extractWeatherData(httpExchange);
            // int statusCode = AggregationServer.handlePutRequest(new WeatherData());
            // httpExchange.sendResponseHeaders(statusCode, 0);
        }
    }

    public static int handlePutRequest(WeatherData data) {
        int statusCode;
        if (weatherDataMap.isEmpty()) {
            statusCode = 201;
        } else {
            statusCode = 200;
        }

        lock.lock();
        try {
            weatherDataQueue.offer(data);
            WeatherData nextData;
            /*
             * while ((nextData = weatherDataQueue.peek()) != null
             * && nextData.getLamportTimestamp() <= lamportClock.getTime()) {
             * weatherDataQueue.poll();
             * weatherDataMap.put(nextData.getId(), nextData);
             * lastActiveMap.put(nextData.getId(), lamportClock.getTime());
             * }
             */

        } catch (Exception e) {
            e.printStackTrace();
            statusCode = 500; // Internal server error
        } finally {
            lock.unlock();
        }

        return statusCode;
    }
}