package com;

import java.io.IOException;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.models.WeatherData;
import com.utility.JsonUtils;
import com.utility.LamportClock;

public class UtilityTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("UtilityTests");
        suite.addTest(new UtilityTests("testLamportClock"));
        suite.addTest(new UtilityTests("testToJsonAndFromJson"));
        suite.addTest(new UtilityTests("testGetDataFromJsonFile"));
        return suite;
    }

    public UtilityTests(String testName) {
        super(testName);
    }

    public void testLamportClock() {
        LamportClock lc = new LamportClock();
        lc.tick();
        lc.update(5);
        assertEquals(6, lc.getTime());
    }

    public void testToJsonAndFromJson() {
        WeatherData wd = new WeatherData("sampleId");
        wd.setId("sampleId");
        wd.setName("sampleName");
        wd.setAirTemperature(20.5);

        String json = JsonUtils.toJson(wd);
        WeatherData wdFromJson = JsonUtils.fromJson(json);

        assertEquals(wd.getId(), wdFromJson.getId());
        assertEquals(wd.getName(), wdFromJson.getName());
        assertEquals(wd.getAirTemperature(), wdFromJson.getAirTemperature());
    }

    public void testGetDataFromJsonFile() {
        String testFilePath = "src/test/resources/test_weather_data_IDS60901.json";
        try {
            WeatherData weatherData = JsonUtils.getDataFromJsonFile(testFilePath);
            assertNotNull(weatherData);
            assertEquals("IDS60901", weatherData.getId());
            assertEquals("SA", weatherData.getState());
            assertEquals(60, weatherData.getRelativeHumidity());
        } catch (IOException e) {
            fail("An IOException occurred.");
        }
    }
}
