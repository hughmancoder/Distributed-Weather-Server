package com;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.*;
import java.nio.file.*;

import com.google.gson.JsonParseException;
import com.models.QueryData;
import com.models.WeatherData;
import com.models.TimedEntry;
import com.utility.LamportClock;
import com.utility.JsonUtils;

public class AggregationServer {
    public static final String TEMP_STORAGE_PATH = "../../resources/temp_storage.json";
    private static int THIRTY_SECONDS = 300000;

    private static HttpServer server;
    private static Timer timer;
    private static boolean isRunning = false;
    private static final ReentrantLock lock = new ReentrantLock();
    private static LamportClock lamportClock = new LamportClock();
    private static AtomicLong lastUpdateTime = new AtomicLong(0);
    private static HashMap<String, WeatherData> weatherDataMap = new HashMap<>();
    private static PriorityBlockingQueue<TimedEntry> weatherDataQueue = new PriorityBlockingQueue<>();

    public static void main(String[] args) {
        int port = 4567;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.out.println("Invalid port number. Using default port 4567");
            }
        }

        start(port);
    }

    public static void start(int port) {
        System.out.println("Running aggregation server on port " + port + "..");
        try {

            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/weather", new DataHandler());
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Removing old entries...");
                    removeOldEntries();
                }
            }, THIRTY_SECONDS, THIRTY_SECONDS);

            isRunning = true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to start HTTP server.");
        }
    }

    public static void stop() {
        if (isRunning) {
            isRunning = false;
            if (timer != null) {
                timer.cancel();
            }
            if (server != null) {
                server.stop(0);
            }
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

    static class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestMethod = httpExchange.getRequestMethod();
            // TODO: remove
            // httpExchange.getResponseHeaders().add("X-Lamport-Clock",
            // String.valueOf(lamportClock.getTime()));

            if ("GET".equals(requestMethod)) {
                GETRequest(httpExchange);
            } else if ("PUT".equals(requestMethod)) {
                PUTRequest(httpExchange);
            } else {
                httpExchange.sendResponseHeaders(400, 0); // Bad Request
                httpExchange.close();
            }
        }

        private void GETRequest(HttpExchange httpExchange) throws IOException {
            lock.lock();
            try {
                lamportClock.tick();

                URI requestURI = httpExchange.getRequestURI();
                Map<String, String> queryParameters = QueryData.parseQueryParameters(requestURI.getQuery());
                String stationId = queryParameters.get("station");
                String json;

                if (stationId != null) {
                    // Send data for the specific station
                    WeatherData weatherData = weatherDataMap.get(stationId);
                    if (weatherData != null) {
                        json = JsonUtils.toJson(weatherData);
                    } else {
                        json = "{ \"error\": \"No data available for stationId: " + stationId + "\" }";
                        httpExchange.sendResponseHeaders(404, json.length());
                    }
                } else {
                    // Send all data
                    json = JsonUtils.hashMapToJson(weatherDataMap);
                }

                byte[] response = json.getBytes();
                httpExchange.sendResponseHeaders(200, response.length);
                httpExchange.getResponseBody().write(response);

            } finally {
                lock.unlock();
                httpExchange.close();
            }
        }

        private void PUTRequest(HttpExchange httpExchange) throws IOException {
            lock.lock();
            try {
                lamportClock.tick();
                // Read the request body
                InputStream is = httpExchange.getRequestBody();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                String requestBody = sb.toString();
                WeatherData weatherData = JsonUtils.fromJson(requestBody);

                // Validate the received WeatherData object
                if (weatherData == null || weatherData.getId() == null || weatherData.getId().isEmpty()) {
                    httpExchange.sendResponseHeaders(400, 0); // Bad Request
                    return;
                }
                // Call the static PUTRequest to update data and get the status code
                long currentTime = System.currentTimeMillis();
                weatherData.setLamportTime(lamportClock.getTime());
                int statusCode = AggregationServer.PUTRequest(weatherData, currentTime);
                httpExchange.sendResponseHeaders(statusCode, 0);

            } catch (JsonParseException e) {
                e.printStackTrace();
                httpExchange.sendResponseHeaders(400, 0); // Bad Request
            } catch (Exception e) {
                e.printStackTrace();
                httpExchange.sendResponseHeaders(500, 0); // Internal Server Error
            } finally {
                lock.unlock();
                httpExchange.close();
            }
        }
    }

    public static int PUTRequest(WeatherData data, long incomingTime) {
        int statusCode;
        lock.lock();
        try {
            lamportClock.update(incomingTime);
            lamportClock.tick();

            data.setLamportTime(lamportClock.getTime());
            lastUpdateTime.set(lamportClock.getTime());

            weatherDataMap.put(data.getId(), data);
            weatherDataQueue.add(new TimedEntry(data.getId(), incomingTime));
            statusCode = (weatherDataMap.size() == 1) ? 201 : 200;
        } catch (Exception e) {
            e.printStackTrace();
            statusCode = 500; // Internal Server Error
        } finally {
            lock.unlock();
        }
        return statusCode;
    }

    public static long getLamportTime() {
        return lamportClock.getTime();
    }

    private static void removeOldEntries() {
        lock.lock();
        try {
            long thresholdTime = System.currentTimeMillis() - THIRTY_SECONDS;
            // TODO
            System.out.println("Removing entries older than " + thresholdTime);

            // Remove old entries from PriorityBlockingQueue
            while (true) {
                TimedEntry entry = weatherDataQueue.peek();
                if (entry == null || entry.getTime() >= thresholdTime) {
                    break;
                }
                weatherDataQueue.poll();
            }

            // Create a set of IDs currently in the queue
            Set<String> keysInQueue = new HashSet<>();
            for (TimedEntry entry : weatherDataQueue) {
                keysInQueue.add(entry.getId());
            }

            // Remove HashMap entries not present in the queue
            Iterator<Map.Entry<String, WeatherData>> iterator = weatherDataMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, WeatherData> entry = iterator.next();
                if (!keysInQueue.contains(entry.getKey())) {
                    iterator.remove();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static long getRemoteLamportTime(String serverUrl) {
        long lamportTime = -1;
        try {
            URL url = new URL(serverUrl + "/lamport");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String response = reader.readLine();
                    lamportTime = Long.parseLong(response);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to get Lamport time from remote server: " + e.getMessage());
        }
        return lamportTime;
    }

}
