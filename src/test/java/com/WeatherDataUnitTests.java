package com;

import com.models.WeatherData;
import junit.framework.TestCase;

public class WeatherDataUnitTests extends TestCase {
    private static final String TEST_FILE_PATH1 = "src/test/resources/test_weather_data_IDS60901.txt";

    public void testReadFileAndParse() {
        WeatherData data = WeatherData.readFileAndParse(TEST_FILE_PATH1);
        assertNotNull(data);
        assertEquals("IDS60901", data.getId());
        System.out.println("\ntestReadFileAndParse: ");
        data.showWeatherData();
    }

}
