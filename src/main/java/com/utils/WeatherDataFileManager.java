package com.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.models.WeatherData;

public class WeatherDataFileManager {

    private static final Lock dataLock = new ReentrantLock();
    private static HashMap<String, WeatherData> weatherDataMap;

    public static void lockData(Runnable action) {
        dataLock.lock();
        try {
            action.run();
        } finally {
            dataLock.unlock();
        }
    }

    public static void weatherDataMapToFile(String filePath) throws IOException {
        lockData(() -> {
            try {
                Path path = Paths.get(filePath);
                Path parentDir = path.getParent();
                if (parentDir != null) {
                    Files.createDirectories(parentDir);
                }
                String json = JsonUtils.toJson(weatherDataMap); // Replace with your util
                ByteBuffer buffer = ByteBuffer.allocate(json.getBytes(StandardCharsets.UTF_8).length);
                buffer.put(json.getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE)) {
                    fileChannel.truncate(0);
                    while (buffer.hasRemaining()) {
                        fileChannel.write(buffer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void fileToWeatherDataMap(String filePath) throws IOException {
        lockData(() -> {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
                        ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
                        fileChannel.read(buffer);
                        buffer.flip();
                        String json = StandardCharsets.UTF_8.decode(buffer).toString();
                        weatherDataMap = JsonUtils.jsonToWeatherDataMap(json); // Replace with your util
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static WeatherData readFileAndParse(String fileLocation) {
        HashMap<String, String> map = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileLocation))) {
            lockData(() -> {
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length >= 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            map.put(key, value);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error reading the line from file", e);
                }
            });
        } catch (IOException e) {
            System.err.println("Error reading file located at " + fileLocation + ": " + e.getMessage());
            return null;
        }

        try {
            return new WeatherData(
                    map.getOrDefault("id", ""),
                    map.getOrDefault("name", ""),
                    map.getOrDefault("state", ""),
                    map.getOrDefault("time_zone", ""),
                    Double.parseDouble(map.getOrDefault("lat", "0")),
                    Double.parseDouble(map.getOrDefault("lon", "0")),
                    map.getOrDefault("local_date_time", ""),
                    map.getOrDefault("local_date_time_full", ""),
                    Double.parseDouble(map.getOrDefault("air_temp", "0")),
                    Double.parseDouble(map.getOrDefault("apparent_t", "0")),
                    map.getOrDefault("cloud", ""),
                    Double.parseDouble(map.getOrDefault("dewpt", "0")),
                    Double.parseDouble(map.getOrDefault("press", "0")),
                    Integer.parseInt(map.getOrDefault("rel_hum", "0")),
                    map.getOrDefault("wind_dir", ""),
                    Integer.parseInt(map.getOrDefault("wind_spd_kmh", "0")),
                    Integer.parseInt(map.getOrDefault("wind_spd_kt", "0")));
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number fields: " + e.getMessage());
            return null;
        }
    }
}
