package weatherServer.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import weatherServer.ContentServer;
import weatherServer.GETClient;
import weatherServer.models.WeatherData;
import weatherServer.utils.HttpUtils;
import weatherServer.utils.JsonUtils;
import weatherServer.utils.LamportClock;
import weatherServer.utils.ServerHandler;

public class serverIntegrationTests {
    private static final String TEXT_FILE = "src/test/resources/test_weather_data_IDS60901.txt";
    private static final String JSON_FILE_2 = "src/test/resources/test_weather_data_IDS60902.json";
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
    public void testSendPutRequestToAggregationServer() {
        WeatherData weatherData = JsonUtils.getDataFromJsonFile(JSON_FILE_2);
        String jsonPayload = JsonUtils.toJson(weatherData);

        try {
            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL, jsonPayload,
                    lamportClock);
        } catch (Exception e) {
            fail("An exception should not have been thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetRequestFromGetClient() {
        try {
            GETClient.getRequest(lamportClock, "http://localhost",
                    Integer.parseInt(AGGREGATION_SERVER_PORT), null);
        } catch (Exception e) {
            fail("An exception should not have been thrown: " + e.getMessage());
        }
    }

    @Test
    public void testDataSynchronisation() {
        WeatherData weatherData = JsonUtils.getDataFromJsonFile(JSON_FILE_2);
        String jsonPayload = JsonUtils.toJson(weatherData);

        // TESTING CONTENT SERVER GET REQUEST
        try {
            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL, jsonPayload, lamportClock);
        } catch (Exception e) {
            fail("An exception should not have been thrown: " + e.getMessage());
        }

        try {
            String GetRequestUrl = HttpUtils.buildGetRequestUrl("http://localhost",
                    Integer.parseInt(AGGREGATION_SERVER_PORT), null);
            HttpURLConnection conn = HttpUtils.createConnection(GetRequestUrl, lamportClock);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200 && responseCode != 201) {
                fail("Expected response code 200 or 201 but received: " + responseCode);
                return;
            }

            try (InputStream inputStream = conn.getInputStream()) {
                JsonObject responseJSON = JsonUtils.getJSONResponse(conn);
                System.out.println("Server Response: " + responseJSON);

                HashMap<String, WeatherData> weatherDataMap = JsonUtils.jsonToWeatherDataMap(responseJSON.toString());

                WeatherData loadedWeatherData = weatherDataMap.get(weatherData.getId());
                assertEquals(weatherData.getId(), loadedWeatherData.getId());
                assertEquals(weatherData.getName(), loadedWeatherData.getName());
            }
        } catch (IOException e) {
            fail("An IO exception should not have been thrown: " + e.getMessage());
        } catch (Exception e) {
            fail("An exception should not have been thrown: " + e.getMessage());
        }
    }
}
