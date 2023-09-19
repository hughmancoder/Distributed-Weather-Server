package com;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.models.WeatherData;
import com.utility.JsonUtils;
import com.utility.LamportClock;

public class ContentServerIntegrationTest {
    private static final String testFilePath1 = "src/test/resources/test_weather_data_IDS60901.json";
    private static final String testFilePath2 = "src/test/resources/test_weather_data_IDS60902.json";
    private static final String testFilePath3 = "src/test/resources/test_weather_data_IDS60903.txt";
    private final String serverUrl = "http://localhost";
    private static final String AGGREGATION_SERVER_PORT = "4567";
    private static final String CONTENT_SERVER_PORT = "4568";
    private ContentServer contentServer;

    @Before
    public void setup() {
        // Start AggregationServer
        AggregationServer.start(Integer.parseInt(AGGREGATION_SERVER_PORT));
        WeatherData weatherData1 = JsonUtils.getDataFromJsonFile(testFilePath1);
        WeatherData weatherData2 = JsonUtils.getDataFromJsonFile(testFilePath2);
        AggregationServer.putToWeatherDataMap(weatherData1);
        AggregationServer.putToWeatherDataMap(weatherData2);

        // Start ContentServer
        contentServer = new ContentServer(CONTENT_SERVER_PORT, testFilePath3);
        contentServer.start();
    }

    @After
    public void teardown() {
        AggregationServer.stop();
        contentServer.stop();

    }

    @Test
    public void testUploadWeatherDataToAggregateServer() {
        LamportClock lamportClock = new LamportClock();

        try {
            Thread.sleep(1000); // Allow some time for the ContentServer to upload data to the AggregationServer

            // Make GET request to AggregationServer to retreived PUT data
            String stationId = null;
            String result = GETClient.GETRequest(serverUrl, AGGREGATION_SERVER_PORT, stationId, lamportClock);
            assertNotNull("Result should not be null", result);
            assertFalse("Result should not be empty", result.isEmpty());
            JsonObject json = JsonUtils.parseStringToJson(result);
            int jsonSize = json.entrySet().size();
            assertEquals("Result should have expected size", 3, jsonSize);

            int IDS60903_data_size = json.get("IDS60903").getAsJsonArray().size();

            assertEquals("Result should have expected size", 17, IDS60903_data_size);

        } catch (Exception e) {
            fail("An exception should not have been thrown: " + e.getMessage());
        }
    }
}
