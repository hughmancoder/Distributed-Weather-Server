package weatherServer.utils;

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
import weatherServer.models.WeatherData;

public class WeatherDataFileManager {

    private static final Lock dataLock = new ReentrantLock();

    public static void lockData(Runnable action) {
        dataLock.lock();
        try {
            action.run();
        } finally {
            dataLock.unlock();
        }
    }

    public static HashMap<String, WeatherData> fileToWeatherDataMap(String filePath) throws IOException {
        final HashMap<String, WeatherData>[] weatherDataMap = new HashMap[] { new HashMap<>() };
        try {
            lockData(() -> {
                try {
                    Path path = Paths.get(filePath);
                    if (Files.exists(path)) {
                        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
                            ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
                            fileChannel.read(buffer);
                            buffer.flip();
                            String json = StandardCharsets.UTF_8.decode(buffer).toString();
                            HashMap<String, WeatherData> deserializedMap = JsonUtils.jsonToWeatherDataMap(json);
                            if (deserializedMap != null) {
                                weatherDataMap[0] = deserializedMap;
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new IOException("An error occurred while reading the file", e);
        }
        return weatherDataMap[0];
    }

    public static void writeFile(String filePath, HashMap<String, WeatherData> weatherDataMap) throws IOException {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            String json = JsonUtils.toJson(weatherDataMap);
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
            throw new IOException("Failed to write to file", e);
        }
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

        if (!map.containsKey("id")) {
            System.out.println("File " + fileLocation + " format invalid as id field cannot be extracted");
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
