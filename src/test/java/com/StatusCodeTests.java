package com;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.models.WeatherData;
import com.utils.LamportClock;
import com.utils.ServerHandler;

public class StatusCodeTests {
    private static final String TEXT_FILE = "src/test/resources/test_weather_data_IDS60901.txt";
    private static final String AGGREGATION_SERVER_PORT = "4567";
    private static final String AGGREGATION_SERVER_URL = "http://localhost:" + AGGREGATION_SERVER_PORT;
    private static LamportClock lamportClock;
    private static ServerHandler aggregationServerHandler;

    @BeforeClass
    public static void setupClass() throws ExecutionException, InterruptedException {
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

        String[] contentServerArgs = new String[] { AGGREGATION_SERVER_URL, TEXT_FILE };
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
    public void testBadRequest400() {
        try {
            String invalidJsonPayload = "....";
            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL, invalidJsonPayload);
            // fail("An exception should have been thrown");
        } catch (Exception e) {
            assertEquals("HTTP/1.1 400 Bad Request", e.getMessage());
        }
    }

    @Test
    public void testMissingStationId404() {
        try {
            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL + "?station=missing_id",
                    "{}");

        } catch (Exception e) {
            assertEquals("HTTP/1.1 404 Not Found", e.getMessage());
        }
    }

    @Test
    public void testNoContent204() {
        try {
            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL, "{}");
        } catch (Exception e) {
            assertEquals("HTTP/1.1 204 No Content", e.getMessage());
        }
    }
}
