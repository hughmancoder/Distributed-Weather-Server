package com;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import com.models.WeatherData;
import com.utils.JsonUtils;
import com.utils.LamportClock;
import com.utils.ServerHandler;
import com.models.TimedEntry;

public class AggregationServer {
    public static final String TEMP_STORAGE_PATH = "../../resources/temp_storage.json";
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
                e.printStackTrace();
                System.out.println("Invalid port number. Using default port 4567");
            }
        }

        // start aggregation server server
        ServerHandler serverHandler = new ServerHandler(port, lock, lamportClock, weatherDataMap, true);
        serverHandler.start();
    }

    public static void removeOldEntries() {
        lockData(() -> {
            long thresholdTime = System.currentTimeMillis() - THIRTY_SECONDS;
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
        });
    }

    public static void clearWeatherDataMap() {
        lockData(() -> weatherDataMap.clear());
    }

    private static void lockData(Runnable task) {
        lock.lock();
        try {
            // lamportClock.tick();
            task.run();
        } finally {
            lock.unlock();
        }
    }

    public static Map<String, WeatherData> getWeatherDataMap() {
        return Collections.unmodifiableMap(weatherDataMap);
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

}
