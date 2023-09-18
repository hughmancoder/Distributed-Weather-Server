package com;

import com.utility.LamportClock;

import junit.framework.TestCase;

public class ContentServerIntegrationTests extends TestCase {
    private static final String TEST_FILE_PATH1 = "src/test/resources/test_weather_data_IDS60902.txt";

    public void testReadFileAndParse() {
        ContentServer contentServer = new ContentServer(4568, TEST_FILE_PATH1);

        String aggregationServerUrl = "http://localhost:4567/weather";
        contentServer.start(aggregationServerUrl);
    }
}
