package com;

import com.google.gson.JsonObject;
import com.models.WeatherData;
import com.utility.JsonUtils;
import com.utility.LamportClock;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class GETClient {
    public static final String AGGREGATION_SERVER_PORT = "4567";
    public static final String DEFAULT_SERVER_URL = "http://localhost";
    public static final String WEATHER_ENDPOINT = "/weather?station=";

    public static String buildUrl(String serverUrl, String port, String stationId) throws UnsupportedEncodingException {
        String query = (stationId != null && !stationId.isEmpty())
                ? WEATHER_ENDPOINT + URLEncoder.encode(stationId, StandardCharsets.UTF_8.toString())
                : "/weather";
        return serverUrl + ":" + port + query;
    }

    public static String GETRequest(String serverUrl, String port, String stationId, LamportClock lamportClock)
            throws Exception {
        lamportClock.tick();
        serverUrl = Optional.ofNullable(serverUrl).orElse(DEFAULT_SERVER_URL);
        port = Optional.ofNullable(port).orElse(AGGREGATION_SERVER_PORT);

        String finalUrl = buildUrl(serverUrl, port, stationId);
        StringBuilder response = new StringBuilder();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(finalUrl).openStream()))) {
            HttpURLConnection conn = (HttpURLConnection) new URL(finalUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Lamport-Clock", String.valueOf(lamportClock.getTime()));

            String serverClockStr = conn.getHeaderField("X-Lamport-Clock");
            if (serverClockStr != null) {
                lamportClock.update(Long.parseLong(serverClockStr));
            }

            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    public static void main(String[] args) {
        String serverUrl = args[0];
        String port = args[1];
        String stationId = args.length > 2 ? args[2] : null;
        LamportClock GetClientLamportClock = new LamportClock();
        System.out.println("Running client with stationId " + stationId);

        try {
            String jsonResponse = GETRequest(serverUrl, port, stationId, GetClientLamportClock);

            if (stationId != null) {
                WeatherData wd = JsonUtils.fromJson(jsonResponse);
                wd.showWeatherData();
            } else {
                JsonObject jsonObject = JsonUtils.parseStringToJson(jsonResponse);

                for (String key : jsonObject.keySet()) {
                    WeatherData wd = JsonUtils.fromJson(jsonObject.get(key).toString());
                    wd.showWeatherData();
                    System.out.println('\n');
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}
