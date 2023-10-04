package weatherServer.unit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import weatherServer.models.WeatherData;
import weatherServer.utils.JsonUtils;

public class JsonUtilsTests extends TestCase {
    private static final String JSON_PATH = "src/test/resources/test_weather_data_IDS60901.json";

    public JsonUtilsTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("UtilityTests");
        suite.addTest(new JsonUtilsTests("testToJsonAndFromJson"));
        suite.addTest(new JsonUtilsTests("testGetDataFromJsonFile"));
        suite.addTest(new JsonUtilsTests("testJsonToWeatherDataMap"));
        suite.addTest(new JsonUtilsTests("testParseStringToJson"));
        suite.addTest(new JsonUtilsTests("testManualJsonToMap"));
        return suite;
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

        WeatherData weatherData = JsonUtils.getDataFromJsonFile(testFilePath);
        assertNotNull(weatherData);
        assertEquals("IDS60901", weatherData.getId());
        assertEquals("SA", weatherData.getState());
        assertEquals(60, weatherData.getRelativeHumidity());

    }

    public void testJsonToWeatherDataMap() {
        HashMap<String, WeatherData> expected = new HashMap<>();
        WeatherData wd = new WeatherData("sampleId");
        wd.setName("sampleName");
        expected.put("key1", wd);

        String json = JsonUtils.toJson(expected);
        HashMap<String, WeatherData> actual = JsonUtils.jsonToWeatherDataMap(json);

        for (String key : expected.keySet()) {
            WeatherData expectedData = expected.get(key);
            WeatherData actualData = actual.get(key);

            assertEquals(expectedData.getId(), actualData.getId());

        }

    }

    public void testParseStringToJson() {
        String json = "{\"key\":\"value\"}";
        JsonObject expected = new JsonObject();
        expected.addProperty("key", "value");

        JsonObject actual = JsonUtils.parseStringToJson(json);

        assertEquals(expected, actual);
    }

    public void testManualJsonToMap() throws Exception {

        WeatherData wd = JsonUtils.getDataFromJsonFile(JSON_PATH);
        String json = JsonUtils.toJson(wd);

        // Create the expected HashMap based on the content of the JSON file
        HashMap<String, Object> expected = new HashMap<>();
        expected.put("id", "IDS60901");
        expected.put("name", "Adelaide (West Terrace /  ngayirdapira)");
        expected.put("state", "SA");
        expected.put("time_zone", "CST");
        expected.put("lat", -34.9);
        expected.put("lon", 138.6);
        expected.put("local_date_time", "15/04:00pm");
        expected.put("local_date_time_full", "20230715160000");
        expected.put("air_temp", 13.3);
        expected.put("apparent_t", 9.5);
        expected.put("cloud", "Partly cloudy");
        expected.put("dewpt", 5.7);
        expected.put("press", 1023.9);
        expected.put("rel_hum", 60);
        expected.put("wind_dir", "S");
        expected.put("wind_spd_kmh", 15);
        expected.put("wind_spd_kt", 8);

        // Parse the JSON string using the manualJsonToMap method
        System.out.println("json: " + json);
        HashMap<String, Object> actual = JsonUtils.manualJsonToMap(json);

        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = actual.get(key);

            // Check for floating point numbers and use assertEquals with a tolerance
            if (expectedValue instanceof Double || expectedValue instanceof Float) {
                double expectedDouble = ((Number) expectedValue).doubleValue();
                double actualDouble = ((Number) actualValue).doubleValue();
                assertEquals("Mismatch at key: " + key, expectedDouble, actualDouble, 1e-4);
            } else {
                // Use assertEquals for all other types
                assertEquals("Mismatch at key: " + key, expectedValue, actualValue);
            }
        }
    }

}
