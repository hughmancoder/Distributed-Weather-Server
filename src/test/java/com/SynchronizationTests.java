package com;

import com.models.WeatherData;
import junit.framework.TestCase;

public class SynchronizationTests extends TestCase {

    public class ContentServerUnitTest extends TestCase {
        private static final String TEST_FILE_PATH1 = "src/test/resources/test_weather_data_IDS60901.txt";

        public void testSyncMultipleConetntServers() {

            /*
             * AggregationServer.clearWeatherDataMap();
             * ContentServer contentServer = new ContentServer("4569", TEST_FILE_PATH1);
             * WeatherData wd = WeatherData.readFileAndParse(TEST_FILE_PATH1);
             */

            /*
             * assertNotNull(data);
             * assertEquals("IDS60901", data.getId());
             * System.out.println("\ntestReadFileAndParse: ");
             * data.showWeatherData();
             */
        }

    }

    // TODO: test aggregation server, start, stop and file recovery

}
