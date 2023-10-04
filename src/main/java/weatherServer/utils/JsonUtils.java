package weatherServer.utils;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import weatherServer.models.WeatherData;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class JsonUtils {
    private static final Gson gson = new Gson();

    /**
     * Converts a JSON string into a map of String to WeatherData.
     * 
     * @param json The JSON string to be converted.
     * @return A map representation of the JSON.
     */
    public static HashMap<String, WeatherData> jsonToWeatherDataMap(String json) {
        Type type = new TypeToken<HashMap<String, WeatherData>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Parses a string to obtain its JsonObject representation.
     * 
     * @param jsonResponse The string containing the JSON response.
     * @return A JsonObject parsed from the provided string.
     */
    public static JsonObject parseStringToJson(String jsonResponse) {
        return JsonParser.parseString(jsonResponse).getAsJsonObject();
    }

    /**
     * Converts a WeatherData object into its JSON string representation.
     * 
     * @param weatherData The WeatherData object to be converted.
     * @return A JSON string representation of the WeatherData object.
     */

    public static String toJson(WeatherData weatherData) {
        return gson.toJson(weatherData);
    }

    /**
     * Converts a map of String to WeatherData into its JSON string representation.
     * 
     * @param map The map to be converted.
     * @return A JSON string representation of the map.
     */
    public static String toJson(HashMap<String, WeatherData> map) {
        return gson.toJson(map);
    }

    /**
     * Converts a JSON string into a WeatherData object.
     * 
     * @param json The JSON string to be converted.
     * @return A WeatherData object representation of the JSON.
     */
    public static WeatherData fromJson(String json) {
        try {
            WeatherData weatherData = gson.fromJson(json, WeatherData.class);
            return weatherData;
        } catch (JsonSyntaxException e) {
            System.err.println("An error occurred while parsing the JSON string: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reads a JSON file and converts its content into a WeatherData object.
     * 
     * @param filePath The path to the JSON file.
     * @return A WeatherData object representation of the JSON file content.
     */
    public static WeatherData getDataFromJsonFile(String filePath) {
        Path path = Paths.get(filePath);
        try (Reader reader = Files.newBufferedReader(path)) {
            WeatherData weatherData = gson.fromJson(reader, WeatherData.class);
            if (weatherData == null) {
                System.err.println("Parsed data is null, likely due to incorrect JSON format");
                return null;
            }
            return weatherData;
        } catch (IOException e) {
            System.err.println("An error occurred while reading the JSON file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reads the response from an HttpURLConnection and returns its JsonObject
     * representation.
     * 
     * @param conn The HttpURLConnection from which the response will be read.
     * @return A JsonObject parsed from the connection's response.
     * @throws IOException If there's an error reading the connection's response.
     */
    public static JsonObject getJSONResponse(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return JsonUtils.parseStringToJson(response.toString());
    }

    /**
     * Prints the contents of a JsonElement to the console.
     * 
     * @param jsonElement The JsonElement whose contents will be printed.
     * @param keyPrefix   A prefix string for printing, typically the key associated
     *                    with this JsonElement.
     */
    public static void printJson(JsonElement jsonElement, String keyPrefix) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (String key : jsonObject.keySet()) {
                printJson(jsonObject.get(key), key);
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            int index = 0;
            for (JsonElement element : jsonArray) {
                printJson(element, keyPrefix + "[" + (index++) + "]");
            }
        } else {
            System.out.println(keyPrefix + ": " + jsonElement.toString());
        }
    }
}