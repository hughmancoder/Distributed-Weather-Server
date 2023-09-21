package com;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.net.HttpURLConnection;
import java.util.HashMap;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.models.WeatherData;
import com.utils.HttpUtils;
import com.utils.JsonUtils;
import com.utils.LamportClock;
import com.utils.ServerHandler;

public class TestDataExpiry {
    private static final String TEXT_FILE = "src/test/resources/test_weather_data_IDS60901.txt";
    private static final String JSON_FILE_2 = "src/test/resources/test_weather_data_IDS60902.json";
    private static final String AGGREGATION_SERVER_PORT = "4567";
    private static final String AGGREGATION_SERVER_URL = "http://localhost:" +
            AGGREGATION_SERVER_PORT;
    private static LamportClock lamportClock;
    private static ServerHandler aggregationServerHandler;

    @BeforeClass
    public static void setupClass() throws ExecutionException,
            InterruptedException {
        lamportClock = new LamportClock();
        ReentrantLock lock = new ReentrantLock();
        HashMap<String, WeatherData> weatherDataMap = new HashMap<>();

        Thread aggregationServerThread = new Thread(() -> {
            aggregationServerHandler = new ServerHandler(Integer.parseInt(AGGREGATION_SERVER_PORT), lock, lamportClock,
                    weatherDataMap);
            aggregationServerHandler.start();
        });

        aggregationServerThread.start();
        Thread.sleep(1000);

        String[] contentServerArgs = new String[] { AGGREGATION_SERVER_URL, TEXT_FILE
        };
        ContentServer.main(contentServerArgs);
    }

    @AfterClass
    public static void teardownClass() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.err.println("Failed to sleep thread");
        }
        aggregationServerHandler.stop();
    }

    @Test
    public void testExpiredData() throws InterruptedException {

        System.out.println("Testing expired data. Initialising...");

        // Create some test WeatherData and set its timestamp to some time that would be
        // considered 'expired'
        WeatherData expiredWeatherData = JsonUtils.getDataFromJsonFile(JSON_FILE_2);

        // in milliseconds

        // Send this data to the server
        String jsonPayload = JsonUtils.toJson(expiredWeatherData);
        try {
            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL, jsonPayload);
        } catch (Exception e) {
            fail("An exception should not have been thrown during PUT: " + e.getMessage());
        }

        // Wait for the period that should trigger expiration.
        System.out.println("Waiting for 30 seonds for data to expire...");
        Thread.sleep(30000); // sleep for 30 secons

        try {
            String getRequestUrl = HttpUtils.buildGetRequestUrl("http://localhost",
                    Integer.parseInt(AGGREGATION_SERVER_PORT), null);
            HttpURLConnection conn = HttpUtils.createConnection(getRequestUrl, lamportClock);
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                JsonObject responseJSON = JsonUtils.getJSONResponse(conn);
                HashMap<String, WeatherData> weatherDataMap = JsonUtils.jsonToWeatherDataMap(responseJSON.toString());

                // Assert that the data has indeed expired and is NOT present.
                assertNull("Expired data should not be available", weatherDataMap.get(expiredWeatherData.getId()));
            } else {
                fail("Expected HTTP 200 but got " + responseCode);
            }
        } catch (Exception e) {
            fail("An exception should not have been thrown during GET: " + e.getMessage());
        }
    }

}
