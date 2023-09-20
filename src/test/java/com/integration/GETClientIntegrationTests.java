package com.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.AggregationServer;
import com.GETClient;
import com.google.gson.JsonObject;
import com.models.WeatherData;
import com.utility.JsonUtils;

public class GETClientIntegrationTests {
    private final String testFilePath1 = "src/test/resources/test_weather_data_IDS60901.json";
    private final String testFilePath2 = "src/test/resources/test_weather_data_IDS60902.json";
    private final String serverUrl = "http://localhost";
    private final String AGGREGATION_SERVER_PORT = "4567";

    @Before
    public void setup() {
        AggregationServer.start(4567);
        WeatherData weatherData1 = JsonUtils.getDataFromJsonFile(testFilePath1);
        WeatherData weatherData2 = JsonUtils.getDataFromJsonFile(testFilePath2);
        AggregationServer.putToWeatherDataMap(weatherData1);
        AggregationServer.putToWeatherDataMap(weatherData2);
    }

    @After
    public void teardown() {
        AggregationServer.stop();
    }

    @Test
    public void testGETRequestReturnsValidResult() {
        performGETRequest(null, 2);
    }

    @Test
    public void testGETRequestByIdReturnsValidResult() {
        performGETRequest("IDS60901", 18);
    }

    private void performGETRequest(String stationId, int expectedJsonSize) {
        try {
            String result = GETClient.GETRequest(serverUrl, AGGREGATION_SERVER_PORT, stationId);
            assertNotNull("Result should not be null", result);
            assertFalse("Result should not be empty", result.isEmpty());

            JsonObject json = JsonUtils.parseStringToJson(result);
            int jsonSize = json.entrySet().size();
            assertEquals("Result should have expected size", expectedJsonSize, jsonSize);
        } catch (Exception e) {
            fail("An exception should not have been thrown: " + e.getMessage());
        }
    }
}
