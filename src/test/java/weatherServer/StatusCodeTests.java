package weatherServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import weatherServer.models.WeatherData;
import weatherServer.utils.JsonUtils;
import weatherServer.utils.LamportClock;
import weatherServer.utils.ServerHandler;

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
        ContentServer.MAX_RETRIES = 1;
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
            String invalidRequest = "..."; // Example representation

            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL, invalidRequest,
                    lamportClock);
            assertEquals(400, ContentServer.responseCode);
        } catch (Exception e) {

        }
    }

    @Test
    public void testMissingStationId404() {
        try {
            String missingStationIdPayload = JsonUtils.toJson(new WeatherData(null));

            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL, missingStationIdPayload,
                    lamportClock);
        } catch (Exception e) {
            System.out.println(ContentServer.responseCode);
            assertEquals(ContentServer.responseCode, 404);
        }
    }

    @Test
    public void testNoContent204() {
        try {
            // If the PUT request does not contain a payload, the server returns a 204.
            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL, "{}", lamportClock);
        } catch (Exception e) {
            assertEquals(204, ContentServer.responseCode);
        }
    }

    @Test
    public void testSuccessfulPUTRequest() {
        try {

            String validPayload = JsonUtils.toJson(new WeatherData("IDS60901"));
            ContentServer.sendPUTRequest(AGGREGATION_SERVER_URL, validPayload,
                    lamportClock);
            assertEquals(201, ContentServer.responseCode);
        } catch (Exception e) {
            fail("An exception should not have been thrown: " + e.getMessage());
        }
    }
}
