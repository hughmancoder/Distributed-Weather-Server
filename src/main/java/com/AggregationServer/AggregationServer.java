package com.AggregationServer;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import com.models.WeatherData;
import com.utils.JsonUtils;
import com.utils.LamportClock;
import com.utils.ServerHandler;
import com.utils.WeatherDataFileManager;
import com.models.TimedEntry;

public class AggregationServer {
    public static final String DATA_STORAGE_PATH = "../../resources/temp_storage";
    private static final long THIRTY_SECONDS = 30 * 1000;
    private static final ReentrantLock lock = new ReentrantLock();
    private static LamportClock lamportClock = new LamportClock();

    private static HashMap<String, WeatherData> weatherDataMap = new HashMap<>();
    private static PriorityBlockingQueue<TimedEntry> weatherDataQueue = new PriorityBlockingQueue<>();

    public static void main(String[] args) {
        int port = 4567;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default port 4567");
            }
        }

        try {
            recoverFromCrash(DATA_STORAGE_PATH);
        } catch (IOException e) {
            System.out.println("Failed to recover from crash");
        }
        // start aggregation server
        ServerHandler serverHandler = new ServerHandler(port, lock, lamportClock, weatherDataMap);
        serverHandler.start();

    }

    public static void removeOldEntries() {
        lockData(() -> {
            long thresholdTime = System.currentTimeMillis() - THIRTY_SECONDS;
            System.out.println("Purging expired entries...");

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
            try {
                WeatherDataFileManager.writeFile(DATA_STORAGE_PATH, weatherDataMap);
            } catch (IOException e) {
                System.out.println("Erorr writing to file" + e.getMessage());
            }
        });

    }

    public static void clearWeatherDataMap() {
        lockData(() -> weatherDataMap.clear());
    }

    private static void lockData(Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    public static HashMap<String, WeatherData> getWeatherDataMap() {
        return new HashMap<>(weatherDataMap); // Deep copy
    }

    public static String getAllWeatherData() {
        lock.lock();
        try {
            HashMap<String, WeatherData> copyMap = new HashMap<>(weatherDataMap); // Deep copy
            return JsonUtils.toJson(copyMap);
        } finally {
            lock.unlock();
        }
    }

    public static String getWeatherByStation(String stationId) {
        lock.lock();
        try {
            WeatherData stationData = weatherDataMap.get(stationId);
            if (stationData != null) {
                return JsonUtils.toJson(stationData);
            }
            return null;

        } finally {
            lock.unlock();
        }
    }

    public static void putToWeatherDataMap(WeatherData weatherData) {
        lockData(() -> {
            String id = weatherData.getId();

            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("WeatherData object must have a valid id");
            }
            weatherDataMap.put(id, weatherData);
        });
    }

    public static void recoverFromCrash(String filePath) throws IOException {
        String tempFilePath = filePath + ".tmp";
        if (Files.exists(Paths.get(tempFilePath))) {
            HashMap<String, WeatherData> recoveredData = WeatherDataFileManager.fileToWeatherDataMap(tempFilePath);
            // Merge with existing data ssuming weatherDataMap is your primary data store
            weatherDataMap.putAll(recoveredData);
            Files.delete(Paths.get(tempFilePath));
        } else if (Files.exists(Paths.get(filePath))) {
            HashMap<String, WeatherData> loadedData = WeatherDataFileManager.fileToWeatherDataMap(filePath);

            weatherDataMap.putAll(loadedData);
        }
    }

}