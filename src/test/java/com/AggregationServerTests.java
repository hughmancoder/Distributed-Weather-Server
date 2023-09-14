package com;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.models.WeatherData;
import com.utility.JsonUtils;
import com.utility.LamportClock;

public class AggregationServerTests extends TestCase {

    public AggregationServerTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AggregationServerTests.class);
    }

    // Test if HTTP 200 is returned for updates on existing data
    public void testHttpStatusForUpdate() {
        // Simulate a PUT request with existing data
        WeatherData wd = new WeatherData();
        wd.setId("existingId");
        wd.setName("existingName");

        // Manually populate weatherDataMap to simulate existing data
        AggregationServer.weatherDataMap.put("existingId", wd);

        int status = AggregationServer.handlePutRequest(wd);
        assertEquals(200, status);
    }

    // Test if HTTP 201 is returned for creation of new data
    public void testHttpStatusForCreation() {
        // Simulate a PUT request with new data
        WeatherData wd = new WeatherData();
        wd.setId("newId");
        wd.setName("newName");

        // Ensure weatherDataMap is empty to simulate no existing data
        AggregationServer.weatherDataMap.clear();

        int status = AggregationServer.handlePutRequest(wd);
        assertEquals(201, status);
    }

    // TODO
    // Test if data is loaded properly from file
    /*
     * public void testLoadDataFromFile() {
     * try {
     * // Write some data to the file
     * WeatherData wd = new WeatherData();
     * wd.setId("fileId");
     * wd.setName("fileName");
     * 
     * HashMap<String, WeatherData> testMap = new HashMap<>();
     * testMap.put("fileId", wd);
     * 
     * Files.write(Paths.get(AggregationServer.DATA_FILE_PATH),
     * JsonUtils.toJson(testMap).getBytes());
     * 
     * // Clear the existing map and reload it from the file
     * AggregationServer.weatherDataMap.clear();
     * AggregationServer.loadDataFromFile();
     * 
     * // Verify that the map contains the correct data
     * assertTrue(AggregationServer.weatherDataMap.containsKey("fileId"));
     * assertEquals("fileName",
     * AggregationServer.weatherDataMap.get("fileId").getName());
     * 
     * } catch (IOException e) {
     * fail("Test failed due to IOException");
     * }
     * }
     */
}
