package weatherServer.utils;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

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
import java.util.Map;
import java.util.StringTokenizer;

public class JsonUtils {
    private static final Gson gson = new Gson();

    /**
     * Converts a JSON string into a HashMap representation.
     * 
     * This function is designed to manually parse a simple flat JSON string (i.e.,
     * no nested objects or arrays)
     * into a Java HashMap. The function handles both string and numeric values,
     * distinguishing between
     * integral and floating-point numbers.
     * 
     * Key assumptions and limitations:
     * 1. The JSON string contains no nested objects or arrays.
     * 2. The provided JSON string is correctly formatted.
     * 3. The function does not handle boolean, null, or other data types aside from
     * strings and numbers.
     * 
     * @param json The JSON string to be parsed.
     * @return A HashMap representation of the given JSON string. The keys of the
     *         map are the JSON keys,
     *         and the values of the map are the corresponding JSON values. Numeric
     *         values are represented as
     *         either Integer or Double objects in the map, depending on whether
     *         they're integral or floating-point numbers.
     */
    public static HashMap<String, Object> manualJsonToMap(String json) {
        HashMap<String, Object> resultMap = new HashMap<>();

        // Trim the starting and ending curly braces
        json = json.trim().substring(1, json.length() - 1);

        // Use StringTokenizer to split the string by commas
        StringTokenizer st = new StringTokenizer(json, ",");

        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            // Split the token into key and value by the colon
            int colonIndex = token.indexOf(":");
            String key = token.substring(1, colonIndex - 1); // Removing starting and ending quotes
            String value = token.substring(colonIndex + 1).trim();

            // Check if the value is a string (starts with a quote)
            if (value.startsWith("\"")) {
                value = value.substring(1, value.length() - 1); // Removing starting and ending quotes
            } else {
                // Attempt to convert to a number (float or integer)
                try {
                    if (value.contains(".")) {
                        resultMap.put(key, Float.parseFloat(value));
                    } else {
                        resultMap.put(key, Integer.parseInt(value));
                    }
                    continue;
                } catch (NumberFormatException e) {
                    // It's not a number, just treat as a string without quotes
                }
            }

            resultMap.put(key, value);
        }

        return resultMap;
    }

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
     * Parses a string to obtain its JsonObject representation by calling
     * manualJsonToMap helper function.
     * 
     * @param jsonResponse The string containing the JSON response.
     * @return A JsonObject parsed from the provided string.
     */
    public static JsonObject parseStringToJson(String jsonResponse) {
        HashMap<String, Object> parsedMap = manualJsonToMap(jsonResponse);
        JsonObject result = new JsonObject();

        for (Map.Entry<String, Object> entry : parsedMap.entrySet()) {
            if (entry.getValue() instanceof String) {
                result.add(entry.getKey(), new JsonPrimitive((String) entry.getValue()));
            } else if (entry.getValue() instanceof Double) {
                result.add(entry.getKey(), new JsonPrimitive((Double) entry.getValue()));
            }
        }
        // return JsonParser.parseString(jsonResponse).getAsJsonObject();
        return result;
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

    /**
     * Reads the response from an HttpURLConnection and returns its JsonObject
     * representation.
     * 
     * @param conn The HttpURLConnection from which the response will be read.
     * @return A JsonObject parsed from the connection's response.
     * @throws IOException If there's an error reading the connection's response.
     */
    public static JsonObject getJSONResponse(HttpURLConnection conn) {
        try (InputStreamReader isr = new InputStreamReader(conn.getInputStream())) {
            return JsonParser.parseReader(isr).getAsJsonObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response.", e);
        }
    }
}
