package weatherServer;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import weatherServer.models.WeatherData;
import weatherServer.utils.LamportClock;
import weatherServer.utils.ServerHandler;

public class DistributedSystemTests {
    private static final String TEXT_FILE = "src/test/resources/test_weather_data_IDS60901.txt";
    private static ConcurrentLinkedQueue<Long> timestamps = new ConcurrentLinkedQueue<>();
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
    public void testParallelGetClients() {
        int numberOfGetClients = 10;
        Thread[] getClientThreads = new Thread[numberOfGetClients];
        for (int i = 0; i < numberOfGetClients; i++) {
            getClientThreads[i] = new Thread(() -> {
                try {
                    GETClient.getRequest(lamportClock, "http://localhost", Integer.parseInt(AGGREGATION_SERVER_PORT),
                            null);
                    timestamps.add(lamportClock.getTime()); // Track the timestamp after a GET request
                } catch (Exception e) {
                    fail("Error in GETClient: " + e.getMessage());
                }
            });
            getClientThreads[i].start();
        }

        for (int i = 0; i < numberOfGetClients; i++) {
            try {
                getClientThreads[i].join();
            } catch (InterruptedException e) {
                fail("Thread interrupted: " + e.getMessage());
            }
        }

        verifyTimestamps();
    }

    @Test
    public void testParallelContentServers() {
        int numberOfContentServers = 5;
        Thread[] contentServerThreads = new Thread[numberOfContentServers];
        for (int i = 0; i < numberOfContentServers; i++) {
            contentServerThreads[i] = new Thread(() -> {
                try {
                    ContentServer.main(new String[] { AGGREGATION_SERVER_URL, TEXT_FILE });
                    timestamps.add(lamportClock.getTime()); // Track the timestamp after a PUT request
                } catch (Exception e) {
                    fail("Error in ContentServer: " + e.getMessage());
                }
            });
            contentServerThreads[i].start();
        }

        for (int i = 0; i < numberOfContentServers; i++) {
            try {
                contentServerThreads[i].join();
            } catch (InterruptedException e) {
                fail("Thread interrupted: " + e.getMessage());
            }
        }
        verifyTimestamps();
    }

    public void verifyTimestamps() {
        long previousTime = -1;
        for (long time : timestamps) {
            System.out.println("Timestamp: " + time + ", Previous Time: " + previousTime);
            if (time < previousTime) {
                fail("Timestamps are not in ascending order");
            }
            previousTime = time;
        }
    }
}