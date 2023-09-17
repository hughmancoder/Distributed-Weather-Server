package com;

import java.nio.charset.StandardCharsets;
import junit.framework.TestCase;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import com.models.WeatherData;
import com.utility.JsonUtils;

public class AggregationServerTests extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    public void testLoadDataFromFile() {
        String testFilePath = "src/test/resources/test_weather_data.json";

        try {
            AggregationServer.loadDataFromFile(testFilePath);

            assertNotNull("weatherDataMap should not be null", AggregationServer.weatherDataMap);
            assertFalse("weatherDataMap should not be empty", AggregationServer.weatherDataMap.isEmpty());
            System.out.println(
                    "testLoadDataFromFile: " + AggregationServer.weatherDataMap.toString());

        } catch (IOException e) {
            fail("IOException should not be thrown: " + e.getMessage());
        }

    }

    public void testSaveDataToFile() {
        try {
            // Step 1: Populate weatherDataMap with some test data
            WeatherData weatherData1 = new WeatherData("id1", "London", "Cloudy", "GMT",
                    51.5074, -0.1278, "2023-09-17T12:00",
                    "2023-09-17T12:00:00", 20.0, 19.0,
                    "Overcast", 10.0, 1013.0, 80, "NW", 10, 5);

            WeatherData weatherData2 = new WeatherData("id2", "New York", "Sunny", "EST",
                    40.7128, -74.0060, "2023-09-17T08:00",
                    "2023-09-17T08:00:00", 25.0, 24.0,
                    "Clear", 12.0, 1010.0, 60, "SE", 8, 4);

            AggregationServer.weatherDataMap.put("key1", weatherData1);
            AggregationServer.weatherDataMap.put("key2", weatherData2);

            AggregationServer.saveWeatherDataMapToFile();

            String serializedMap = JsonUtils.toJson(AggregationServer.weatherDataMap);

            // Read back the file to verify
            Path path = Paths.get(AggregationServer.TEMP_STORAGE_PATH);
            String jsonFromFile = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            // Deserialize and compare
            HashMap<String, WeatherData> deserializedMap = JsonUtils.fromJson(jsonFromFile, HashMap.class);
            HashMap<String, WeatherData> expectedMap = JsonUtils.fromJson(serializedMap, HashMap.class);

            assertEquals(expectedMap, deserializedMap);

            System.out.println("testSaveDataToFile: " + jsonFromFile);

        } catch (IOException e) {
            e.printStackTrace();
            fail("An IOException occurred.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("An unexpected exception occurred.");
        }
    }

}
