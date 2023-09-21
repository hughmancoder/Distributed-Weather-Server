package com.unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import com.models.WeatherData;
import com.utils.JsonUtils;
import com.utils.WeatherDataFileManager;

import junit.framework.TestCase;

public class WeatherDataFileManagerTests extends TestCase {

    private static final String VALID_FILE_PATH = "src/test/resources/test_weather_data_IDS60901.txt";
    private static final String JSON_PATH = "src/test/resources/test_weather_data_IDS60901.json";
    private static final String OUTPUT_PATH = "src/test/resources/test_weather_data_out.json";
    private static final String INVALID_FILE_PATH = "src/test/resources/invalid_file_path.txt";
    WeatherData weatherData;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        weatherData = JsonUtils.getDataFromJsonFile(JSON_PATH);
    }

    public void testReadFileAndParse() {
        WeatherData data = WeatherDataFileManager.readFileAndParse(VALID_FILE_PATH);
        assertNotNull("The data should not be null", data);
        assertEquals("The ID should match", "IDS60901", data.getId());
        System.out.println("\ntestReadFileAndParse: ");
        data.showWeatherData();
    }

    public void testWeatherDataMapToFileAndReadBack() throws IOException {
        HashMap<String, WeatherData> initialData = new HashMap<>();
        initialData.put("key1", weatherData);

        // Write to file
        WeatherDataFileManager.weatherDataMapToFile(OUTPUT_PATH, initialData);

        // Read back
        HashMap<String, WeatherData> readData = WeatherDataFileManager.fileToWeatherDataMap(OUTPUT_PATH);

        // Deep equality check
        assertEquals(initialData.size(), readData.size());

        for (String key : initialData.keySet()) {
            WeatherData original = initialData.get(key);
            WeatherData copied = readData.get(key);

            assertEquals(original.getId(), copied.getId());
        }
    }

    public void testConcurrentWrite() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                WeatherDataFileManager.weatherDataMapToFile(OUTPUT_PATH, new HashMap<>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                WeatherDataFileManager.weatherDataMapToFile(OUTPUT_PATH, new HashMap<>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        try {
            WeatherDataFileManager.weatherDataMapToFile(OUTPUT_PATH, new HashMap<>());
            HashMap<String, WeatherData> readData = WeatherDataFileManager.fileToWeatherDataMap(OUTPUT_PATH);

            // as both threads are writing to an empty HashMap, we expect an empty HashMap
            // file should be empty.
            assertEquals("Data should be an empty HashMap", new HashMap<>(), readData);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testInvalidFilePath() {
        try {
            WeatherDataFileManager.fileToWeatherDataMap(INVALID_FILE_PATH);
            fail("Should have thrown IOException");
        } catch (IOException e) {

            assertEquals("An error occurred in reading file", e.getMessage()); // Add this if you are going with the
                                                                               // second strategy
        }
    }

    public void testEmptyFile() throws IOException {
        Path emptyFilePath = Paths.get("src/test/resources/empty_file.txt");
        Files.createFile(emptyFilePath);

        try {
            HashMap<String, WeatherData> readData = WeatherDataFileManager
                    .fileToWeatherDataMap(emptyFilePath.toString());
            assertEquals("Data should be an empty HashMap for an empty file", null, readData);
        } finally {
            Files.deleteIfExists(emptyFilePath);
        }
    }

}
