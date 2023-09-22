package weatherServer.unit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;

import com.google.gson.JsonObject;
import weatherServer.models.WeatherData;
import weatherServer.utils.JsonUtils;

public class JsonUtilsTests extends TestCase {

    public JsonUtilsTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("UtilityTests");
        suite.addTest(new JsonUtilsTests("testToJsonAndFromJson"));
        suite.addTest(new JsonUtilsTests("testGetDataFromJsonFile"));
        suite.addTest(new JsonUtilsTests("testJsonToWeatherDataMap")); // new
        suite.addTest(new JsonUtilsTests("testParseStringToJson")); // new
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

    // New test for parseStringToJson
    public void testParseStringToJson() {
        String json = "{\"key\":\"value\"}";
        JsonObject expected = new JsonObject();
        expected.addProperty("key", "value");

        JsonObject actual = JsonUtils.parseStringToJson(json);

        assertEquals(expected, actual);
    }

}
