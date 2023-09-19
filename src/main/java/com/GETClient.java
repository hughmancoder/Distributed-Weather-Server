package com;

import com.google.gson.JsonObject;
import com.models.WeatherData;
import com.utility.JsonUtils;
import com.utility.LamportClock;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GETClient {
    public static final String AGGREGATION_SERVER_PORT = "4567";

    public static String GETRequest(String serverUrl, String port, String stationId, LamportClock lamportClock)
            throws Exception {

        if (serverUrl == null || serverUrl.isEmpty()) {
            serverUrl = "http://localhost";
        }
        if (port == null || port.isEmpty()) {
            port = AGGREGATION_SERVER_PORT;
        }
        String finalUrl = (stationId != null && !stationId.isEmpty())
                ? serverUrl + ":" + port + "/weather?station=" + stationId
                : serverUrl + ":" + port + "/weather";

        StringBuilder response = new StringBuilder();

        URL url = new URL(finalUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Lamport-Clock", String.valueOf(lamportClock)); // Include Lamport clock as an HTTP
                                                                                  // header
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

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
