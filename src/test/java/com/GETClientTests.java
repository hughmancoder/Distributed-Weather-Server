package com;

import static org.mockito.Mockito.mock;

import com.models.WeatherData;

import junit.framework.TestCase;

// Aggregation server must be running
public class GETClientTests extends TestCase {
    public void testGetRequest() {
        String serverUrl = "http://localhost";
        String port = "4567"; // Match with AggregationServer's port
        String stationId = "1";

        /*
         * WeatherData mockWeatherData = mock(WeatherData.class);
         * System.out.println("MOCK ID" + mockWeatherData.getId() + " MOCK TEMP" +
         * mockWeatherData.getAirTemperature());
         */

        try {
            String result = GETClient.sendGetRequest(serverUrl, port, stationId);
            System.out.println("GetClient Ouput: " + result);
            assertNotNull("Result should not be null", result);
            assertFalse("Result should not be empty", result.isEmpty());
        } catch (Exception e) {
            fail("An exception should not have been thrown" + e);
        }
    }
}
