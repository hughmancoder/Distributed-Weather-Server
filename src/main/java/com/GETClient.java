package com;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.utility.LamportClock;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GETClient {

    public static String sendGetRequest(String serverUrl, String port, String stationId, LamportClock lamportClock)
            throws Exception {

        if (serverUrl == null || serverUrl.isEmpty()) {
            serverUrl = "https://localhost";
        }
        String finalUrl = (stationId != null && !stationId.isEmpty())
                ? serverUrl + ":" + port + "/weather?station=" + stationId
                : serverUrl + ":" + port + "/weather";

        System.out.println("Sending GET request to " + finalUrl);
        StringBuilder response = new StringBuilder();

        URL url = new URL(finalUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Include Lamport clock as an HTTP header
        conn.setRequestProperty("X-Lamport-Clock", String.valueOf(lamportClock));

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    public static void main(String[] args) {
        // Parsing command-line parameters
        String serverUrl = args[0];
        String port = args[1];
        String stationId = args.length > 2 ? args[2] : "";
        LamportClock GetClientLamportClock = new LamportClock();
        System.out.println("Running client with stationId " + stationId);

        try {
            String jsonResponse = sendGetRequest(serverUrl, port, stationId, GetClientLamportClock);

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            for (String key : jsonObject.keySet()) {
                System.out.println(key + ": " + jsonObject.get(key).getAsString());
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}
