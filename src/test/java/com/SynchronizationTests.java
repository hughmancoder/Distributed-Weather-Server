// package com;

// import junit.framework.TestCase;

// public class SynchronizationTests extends TestCase {

// public class ContentServerUnitTest extends TestCase {
// private static final String TEST_FILE_PATH1 =
// "src/test/resources/test_weather_data_IDS60901.txt";

// public void testSyncMultipleConetntServers() {

// /*
// * AggregationServer.clearWeatherDataMap();
// * ContentServer contentServer = new ContentServer("4569", TEST_FILE_PATH1);
// * WeatherData wd = WeatherData.readFileAndParse(TEST_FILE_PATH1);
// */

// /*
// * assertNotNull(data);
// * assertEquals("IDS60901", data.getId());
// * System.out.println("\ntestReadFileAndParse: ");
// * data.showWeatherData();
// */
// }

// }

// // TODO: test aggregation server, start, stop and file recovery

// // TODO: test error status code

// // The first
// // time weather
// // data is
// // received and
// // the storage
// // file is created,
// // you should return status 201-
// // HTTP_CREATED. If later

// // uploads (updates) are successful, you should return status 200. (This
// means,
// // if a Content Server first connects to the Aggregation Server, then return
// 201
// // as succeed code, then before the content server lost connection, all other
// // succeed response should use 200). Any request other than GET or PUT should
// // return status 400 (note: this is not standard but to simplify your task).
// // Sending no content to the server should cause a 204 status code to be
// // returned. Finally, if the JSON data does not make

// // sense (incorrect JSON) you may return status code 500 - Internal server
// // error.

// // Your server will, by default, start on port 4567 but will accept a single
// // command line argument that gives the starting port number. Your server's
// main
// // method will reside in a file called AggregationServer.java.

// // Your server is designed to stay current and will remove any items in the
// JSON
// // that have come from c

// }
