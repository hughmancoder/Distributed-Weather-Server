package com.utility;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.models.WeatherData;

public class JsonUtils {
    private static final Gson gson = new Gson();

    // TODO: test
    public static String hashMapToJson(HashMap<String, WeatherData> map) {
        return gson.toJson(map);
    }

    // TODO: test
    public static HashMap<String, WeatherData> jsonToHashMap(String json) {
        Type type = new TypeToken<HashMap<String, WeatherData>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    // TODO: test
    public static JsonObject parseStringToJson(String jsonResponse) {
        return JsonParser.parseString(jsonResponse).getAsJsonObject();
    }

    public static String toJson(WeatherData weatherData) {
        return gson.toJson(weatherData);
    }

    public static WeatherData fromJson(String json) {
        try {
            WeatherData weatherData = gson.fromJson(json, WeatherData.class);
            return weatherData;
        } catch (JsonSyntaxException e) {
            System.err.println("An error occurred while parsing the JSON string: " + e.getMessage());
            return null;
        }
    }

    public static WeatherData getDataFromJsonFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        try (Reader reader = Files.newBufferedReader(path)) {
            WeatherData weatherData = gson.fromJson(reader, WeatherData.class);
            if (weatherData == null) {
                throw new IOException("Parsed data is null, likely due to incorrect JSON format.");
            }
            return weatherData;
        } catch (IOException e) {
            throw new IOException("An error occurred while reading the JSON file", e);
        }
    }
}
