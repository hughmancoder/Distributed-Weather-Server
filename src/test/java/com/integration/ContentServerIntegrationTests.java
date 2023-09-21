package com.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.AggregationServer;
import com.ContentServer;
import com.GETClient;
import com.google.gson.JsonObject;
import com.models.WeatherData;
import com.utils.JsonUtils;

public class ContentServerIntegrationTests {
    private static final String TEST_FILE_PATH_1 = "src/test/resources/test_weather_data_IDS60901.json";
    private static final String TEST_FILE_PATH_2 = "src/test/resources/test_weather_data_IDS60902.json";
    private static final String TEST_FILE_PATH_3 = "src/test/resources/test_weather_data_IDS60903.txt";
    private static final String SERVER_URL = "http://localhost";
    private static final String AGGREGATION_SERVER_PORT = "4567";
    private static final String CONTENT_SERVER_PORT = "4568";
    private ContentServer contentServer;

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        // Start AggregationServer
        AggregationServer.start(Integer.parseInt(AGGREGATION_SERVER_PORT));
        WeatherData weatherData1 = JsonUtils.getDataFromJsonFile(TEST_FILE_PATH_1);
        WeatherData weatherData2 = JsonUtils.getDataFromJsonFile(TEST_FILE_PATH_2);
        AggregationServer.putToWeatherDataMap(weatherData1);
        AggregationServer.putToWeatherDataMap(weatherData2);

        // Start ContentServer asynchronously
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            contentServer = new ContentServer(CONTENT_SERVER_PORT, TEST_FILE_PATH_3);
            contentServer.start();
        });
    }

    @After
    public void teardown() {
        AggregationServer.stop();
        contentServer.stop();
    }

    @Test
    public void testUploadWeatherDataToAggregateServer() {

        try {
            Thread.sleep(2000); // Allow some time for the ContentServer to upload data to the AggregationServer

            // Make GET request to AggregationServer to retrieve PUT data
            String stationId = null;
            String result = GETClient.GETRequest(SERVER_URL, AGGREGATION_SERVER_PORT, stationId);
            assertNotNull("Result should not be null", result);
            assertFalse("Result should not be empty", result.isEmpty());

            JsonObject json = JsonUtils.parseStringToJson(result);
            int jsonSize = json.entrySet().size();
            assertEquals("Result should have expected size", 3, jsonSize);

            int sizeOfIDS60903 = json.getAsJsonObject("IDS60903").size();
            assertEquals("Result should have expected size", 18, sizeOfIDS60903);

        } catch (

        Exception e) {
            fail("An exception should not have been thrown: " + e.getMessage());
        }
    }
}
