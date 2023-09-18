package com;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.TimerTask;
import java.util.HashMap;
import java.nio.file.*;
import java.util.Timer;

import com.google.gson.annotations.Since;
import com.models.WeatherData;
import com.utility.LamportClock;
import com.utility.JsonUtils;

public class AggregationServer {
    private static final ReentrantLock lock = new ReentrantLock();
    public static final String TEMP_STORAGE_PATH = "../../resources/temp_storage.json";
    // TODO: args[1] is the path to the data file
    private static final String DATA_FILE_PATH = "../../resources/weather_data.json";

    private static LamportClock lamportClock = new LamportClock();
    private static PriorityBlockingQueue<WeatherData> weatherDataQueue = new PriorityBlockingQueue<>();
    private static HashMap<String, Long> lastActiveMap = new HashMap<>();
    private static HashMap<String, WeatherData> weatherDataMap = new HashMap<>();

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
        System.out.println("Running aggregation server on port " + port + "..");
        // try {
        // // TODO: data from content server
        // // loadDataFromFile(DATA_FILE_PATH);
        // } catch (IOException e) {
        // e.printStackTrace();
        // System.out.println("Failed to load data from file.");
        // return;
        // }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/weather", new DataHandler());
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

    public static void clearWeatherDataMap() {
        lock.lock();
        try {
            weatherDataMap.clear();
        } finally {
            lock.unlock();
        }
    }

    public static HashMap<String, WeatherData> getWeatherDataMap() {
        return weatherDataMap;
    }

    public static HashMap<String, WeatherData> putToWeatherDataMap(WeatherData weatherData)
            throws IllegalArgumentException {
        lock.lock();
        try {
            String id = weatherData.getId();

            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("WeatherData object must have a valid id");
            }

            weatherDataMap.put(id, weatherData);

            return new HashMap<>(weatherDataMap); // returns a shallow copy
        } finally {
            lock.unlock();
        }
    }

    public static void WeatherDataMapToFile() {
        lock.lock();
        try {
            Path path = Paths.get(TEMP_STORAGE_PATH);
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            String json = JsonUtils.hashMapToJson(weatherDataMap);
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

    public static void FileToWeatherDataMap(String filePath) throws IOException {
        lock.lock();
        try {
            if (Files.exists(Paths.get(filePath))) {
                String json = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
                HashMap<String, WeatherData> dataMap = JsonUtils.jsonToHashMap(json);
                if (dataMap != null) {
                    weatherDataMap.putAll(dataMap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to load data from file", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private static void removeOldEntries() {
        lock.lock();
        try {
            long currentTime = lamportClock.getTime();
            // TODO: get string key and remove from weatherDataMap;
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

        // TODO
        private void handleGetRequest(HttpExchange httpExchange) throws IOException {
            lock.lock();
            try {
                // String json = JsonUtils.toJson(weatherDataMap);
                // byte[] response = json.getBytes();
                // httpExchange.sendResponseHeaders(200, response.length);
                // httpExchange.getResponseBody().write(response);
                httpExchange.close();
            } finally {
                lock.unlock();
            }
        }

        // TODO
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