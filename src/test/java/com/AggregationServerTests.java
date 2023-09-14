package com;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;

import com.utility.JsonUtils;
import com.utility.LamportClock;

public class AggregationServerTests extends TestCase {
    public AggregationServerTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AggregationServerTests.class);
    }

    // Test if the JSON conversion works as expected
    public void testJsonConversion() {
        WeatherData wd = new WeatherData();
        wd.id = "sampleId";
        wd.name = "sampleName";

        // TODO: add other attributes
        String json = JsonUtils.toJson(wd);
        WeatherData wdFromJson = JsonUtils.fromJson(json, WeatherData.class);
        assertEquals(wd.id, wdFromJson.id);
        assertEquals(wd.name, wdFromJson.name);

    }

    public void testLamportClock() {
        LamportClock lc = new LamportClock();
        lc.tick();
        lc.update(5);
        assertEquals(6, lc.getTime());
    }

    // Test if the data is loaded correctly from the file
    public void testDataLoadFromFile() {
        // Assuming that loadDataFromFile populates weatherDataMap
        AggregationServer.loadDataFromFile();
        assertTrue(AggregationServer.weatherDataMap.containsKey("someExpectedId"));
    }

    // Test if the data is saved correctly to the file
    public void testDataSaveToFile() {
        // Add some data to weatherDataMap
        AggregationServer.weatherDataMap.put("testId", new WeatherData());
        AggregationServer.saveDataToFile();

        // Clear the current data and reload it
        AggregationServer.weatherDataMap.clear();
        AggregationServer.loadDataFromFile();
        assertTrue(AggregationServer.weatherDataMap.containsKey("testId"));
    }

    // Test if HTTP 201 is returned when new data is created
    public void testHttpStatusForNewData() {
        // Simulate a PUT request with new data
        int status = AggregationServer.handlePutRequest("newData");
        assertEquals(201, status);
    }

    // Test if HTTP 200 is returned for updates
    public void testHttpStatusForUpdate() {
        // Simulate a PUT request with existing data
        int status = AggregationServer.handlePutRequest("existingData");
        assertEquals(200, status);
    }
}
