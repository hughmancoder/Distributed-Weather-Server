package com;

import com.models.WeatherData;
import com.utility.JsonUtils;
import com.utility.LamportClock;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UtilityTests extends TestCase {

    public static Test suite() {
        return new TestSuite(AggregationServerTests.class);
    }

    public void testLamportClock() {
        LamportClock lc = new LamportClock();
        lc.tick();
        lc.update(5);
        assertEquals(6, lc.getTime());
    }

    public void testJsonUtils() {
        WeatherData wd = new WeatherData();
        wd.setId("sampleId");
        wd.setName("sampleName");
        wd.setAirTemperature(20.5);

        String json = JsonUtils.toJson(wd);
        WeatherData wdFromJson = JsonUtils.fromJson(json, WeatherData.class);

        assertEquals(wd.getId(), wdFromJson.getId());
        assertEquals(wd.getName(), wdFromJson.getName());
        assertEquals(wd.getAirTemperature(), wdFromJson.getAirTemperature());
    }
}