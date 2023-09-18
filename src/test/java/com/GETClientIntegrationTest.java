
package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.utility.LamportClock;

import junit.framework.TestCase;

// Aggregation server must be running
public class GETClientIntegrationTest extends TestCase {
    public void testGETRequest() {
        String serverUrl = "http://localhost";
        String port = "4567";

        LamportClock lamportClock = new LamportClock();

        try {
            String result = GETClient.GETRequest(serverUrl, port, null,
                    lamportClock);
            System.out.println("GetClient Ouput: " + result);
            assertNotNull("Result should not be null", result);
            assertFalse("Result should not be empty", result.isEmpty());
        } catch (Exception e) {
            fail("An exception should not have been thrown" + e);
        }
    }

    public void testGETRequestById() {
        String serverUrl = "http://localhost";
        String port = "4567";
        String stationId = "IDS60901";
        LamportClock lamportClock = new LamportClock();

        try {
            String result = GETClient.GETRequest(serverUrl, port, stationId,
                    lamportClock);
            System.out.println("GetClient Ouput: " + result);
            assertNotNull("Result should not be null", result);
            assertFalse("Result should not be empty", result.isEmpty());
        } catch (Exception e) {
            fail("An exception should not have been thrown" + e);
        }
    }

}
