package com;

import junit.framework.TestCase;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.models.WeatherData;
import com.utility.JsonUtils;

public class AggregationServerUnitTests extends TestCase {

    private static final String TEST_FILE_PATH1 = "src/test/resources/test_weather_data_IDS60901.json";
    private static final String TEST_FILE_PATH2 = "src/test/resources/test_weather_data_IDS60902.json";

    private static final String STORAGE_DATA = "src/test/resources/test_stored_weather_map.json";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AggregationServer.clearWeatherDataMap(); // clear the map before every test
    }

    public void testLoadDataFromFile() throws IOException {
        AggregationServer.FileToWeatherDataMap(STORAGE_DATA);
        assertNotNull("weatherDataMap should not be null",
                AggregationServer.getWeatherDataMap());
        assertFalse("weatherDataMap should not be empty", AggregationServer.getWeatherDataMap().isEmpty());

        int expectedSize = 2;

        assertEquals("weatherDataMap should contain " + expectedSize + " entry",
                expectedSize,
                AggregationServer.getWeatherDataMap().size());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonMap = gson.toJson(AggregationServer.getWeatherDataMap());
        String prettyJson = gson.toJson(JsonParser.parseString(jsonMap));
        System.out.println("\ntestLoadDataFromFile: "); // Consider using logging
        System.out.println(prettyJson);
    }

    public void testWeatherDataMapToFile() {
        try {

            WeatherData weatherData1 = JsonUtils.getDataFromJsonFile(TEST_FILE_PATH1);
            WeatherData weatherData2 = JsonUtils.getDataFromJsonFile(TEST_FILE_PATH2);

            // populate the map
            AggregationServer.putToWeatherDataMap(weatherData1);
            AggregationServer.putToWeatherDataMap(weatherData2);
            AggregationServer.WeatherDataMapToFile();

            // Read saved data from file
            String savedData = new String(Files.readAllBytes(Paths.get(AggregationServer.TEMP_STORAGE_PATH)),
                    StandardCharsets.UTF_8);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String prettyJson = gson.toJson(JsonParser.parseString(savedData));
            System.out.println("\ntestWeatherDataMapToFile: ");
            System.out.println(prettyJson);

        } catch (IOException e) {
            fail("An IOException occurred: " + e.getMessage());
        } catch (Exception e) {
            fail("An unexpected exception occurred: " + e.getMessage());
        }
    }

}
