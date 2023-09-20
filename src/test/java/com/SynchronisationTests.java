package com;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.models.WeatherData;
import com.utility.JsonUtils;
import com.utility.LamportClock;

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
    public void testIntegration() throws Exception {
        Thread.sleep(1000); // Allow some time for the ContentServer to upload data to the AggregationServer

        LamportClock localClock = new LamportClock();
        HttpURLConnection agg_conn = (HttpURLConnection) new URL(aggregationServerUrl).openConnection();

        // Read response body from agg_conn
        String responseBody1;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(agg_conn.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            responseBody1 = builder.toString();
        }
        System.out.println("Response Body from conn1: " + responseBody1);

        HttpURLConnection content_conn = (HttpURLConnection) new URL(contentServerURL).openConnection();

        // Read response body from content_conn
        String responseBody2;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(content_conn.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            responseBody2 = builder.toString();
        }
        System.out.println("Response Body from conn1: " + responseBody2);
        // Thread.sleep(1000); // Allow some time for the ContentServer to upload data
        // to the AggregationServer
        // LamportClock localClock = new LamportClock();
        // HttpURLConnection conn1 = (HttpURLConnection) new
        // URL(aggregationServerUrl).openConnection();

        // // Assert that both server clocks and the local clock are synchronised
        // assertEquals(Long.parseLong(server1ClockValue),
        // Long.parseLong(server1ClockValue));
        // assertEquals(localClock.getTime(), Long.parseLong(server2ClockValue));
    }

    @Test
    public void testLamportIncrement() {
        // long initialTime1 = AggregationServer.getLamportTime();
        // long initialTime2 = ContentServer.getLamportTime();
        // System.out.println("Initial times: " + initialTime1 + ", " + initialTime2);

        // Perform operations, like HTTP requests, that would trigger the Lamport clocks
        // to increment
        // ...

        // long newTime1 = AggregationServer.getLamportTime();
        // long newTime2 = ContentServer.getLamportTime();

        // assertTrue(newTime1 > initialTime1);
        // assertTrue(newTime2 > initialTime2);
    }

    // @Test
    // public void testLamportReceiveAndUpdate() {
    // // Simulate receiving a message with a Lamport timestamp
    // long simulatedTime = 50;
    // AggregationServer.getLamportClock().update(simulatedTime);

    // // Perform operations that would trigger a Lamport time update on
    // // AggregationServer
    // // ...

    // long newTime = AggregationServer.getLamportTime();
    // assertTrue(newTime > simulatedTime);
    // }

    // @Test
    // public void testIntegration() throws Exception {
    // LamportClock localClock = new LamportClock();

    // // Initial request to Server 1
    // HttpURLConnection conn1 = (HttpURLConnection) new
    // URL(aggregationServerUrl).openConnection();
    // conn1.setRequestProperty("X-Lamport-Clock",
    // String.valueOf(localClock.getTime()));
    // // ... (read and process response)
    // String server1ClockValue = conn1.getHeaderField("X-Lamport-Clock");
    // localClock.update(Long.parseLong(server1ClockValue));

    // // Initial request to Server 2
    // HttpURLConnection conn2 = (HttpURLConnection) new
    // URL(server2Url).openConnection();
    // conn2.setRequestProperty("X-Lamport-Clock",
    // String.valueOf(localClock.getTime()));
    // // ... (read and process response)
    // String server2ClockValue = conn2.getHeaderField("X-Lamport-Clock");
    // localClock.update(Long.parseLong(server2ClockValue));

    // // Assert that both server clocks and the local clock are synchronized
    // assertEquals(Long.parseLong(server1ClockValue),
    // Long.parseLong(server2ClockValue));
    // assertEquals(localClock.getTime(), Long.parseLong(server2ClockValue));
    // }

    // TODO: test aggregation server, start, stop and file recovery
    // TODO: test error status code

    /*
     * 
     * 
     * 
     * The first
     * time weather
     * data is
     * received and
     * the storage
     * file is created,
     * you should return status 201-
     * HTTP_CREATED. If later
     * 
     * uploads (updates) are successful, you should return status 200. (This
     * if a Content Server first connects to the Aggregation Server, then return201
     * as succeed code, then before the content server lost connection, all other
     * succeed response should use 200). Any request other than GET or PUT should
     * return status 400 (note: this is not standard but to simplify your task).
     * Sending no content to the server should cause a 204 status code to be
     * returned. Finally, if the JSON data does not make
     * 
     * sense (incorrect JSON) you may return status code 500 - Internal server
     * error.
     * 
     * Your server will, by default, start on port 4567 but will accept a single
     * command line argument that gives the starting port number. Your server's main
     * method will reside in a file called AggregationServer.java.
     * 
     * Your server is designed to stay current and will remove any items in theJSON
     * that have come from c
     */

}
