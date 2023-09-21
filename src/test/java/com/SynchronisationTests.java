package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.models.WeatherData;
import com.utils.JsonUtils;
import com.utils.LamportClock;

import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class SynchronisationTests {

    private static final String SERVER_URL = "http://localhost";
    private static final String AGGREGATION_SERVER_PORT = "4567";
    private static final String CONTENT_SERVER_PORT = "4568";
    private static final String TEST_FILE_PATH_1 = "src/test/resources/test_weather_data_IDS60901.json";
    private static final String TEST_FILE_PATH_2 = "src/test/resources/test_weather_data_IDS60902.json";
    private static final String TEST_FILE_PATH_3 = "src/test/resources/test_weather_data_IDS60903.txt";

    private ContentServer contentServer;
    private final String aggregationServerUrl = String.format("%s:%s/weather", SERVER_URL, AGGREGATION_SERVER_PORT);
    private final String contentServerURL = String.format("%s:%s", SERVER_URL, CONTENT_SERVER_PORT); // Added
                                                                                                     // /weather
                                                                                                     // to

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        // Start AggregationServer
        AggregationServer.start(Integer.parseInt(AGGREGATION_SERVER_PORT));
        WeatherData weatherData1 = JsonUtils.getDataFromJsonFile(TEST_FILE_PATH_1);
        AggregationServer.putToWeatherDataMap(weatherData1);

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
    public void testClockSync() throws Exception {
        Thread.sleep(1000); // Allow some time for the ContentServer to upload data to the AggregationServer

        // TODO: fix
        // long contentServerLamportTime =
        // AggregationServer.getRemoteLamportTime(contentServerURL);
        // long aggregationServerLamportTime = AggregationServer.getLamportTime();

        // assertEquals("Lamport times should be equal when accessed locally and
        // remotely",
        // contentServerLamportTime, ContentServer.getLamportTime());

        // aggregationServerLamportTime = AggregationServer.getLamportTime();

        // String result = GETClient.GETRequest(SERVER_URL, AGGREGATION_SERVER_PORT,
        // "IDS60903");

        // assertNotNull("Result should not be null", result);
        // assertFalse("Result should not be empty", result.isEmpty());

        // WeatherData wd = JsonUtils.fromJson(result);

        // assertEquals("Lamport times should be equal when accessed locally and
        // remotely",
        // aggregationServerLamportTime, wd.getLamportTime());

        // TODO: check that aggregation server and content server clocks sync

    }

    // @Test
    // public void testLamportIncrement() throws Exception {
    // long initialTime1 = AggregationServer.getLamportTime();

    // GETClient.GETRequest(SERVER_URL, AGGREGATION_SERVER_PORT,
    // null);

    // long newTime1 = AggregationServer.getLamportTime();

    // assertTrue(newTime1 > initialTime1);

    // }
}