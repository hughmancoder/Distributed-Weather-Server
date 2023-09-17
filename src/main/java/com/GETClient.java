package com;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GETClient {

    public static String sendGetRequest(String serverUrl, String port, String stationId) throws Exception {

        // String finalUrl = serverUrl + ":" + port + "/weather?station=" + stationId;

        String finalUrl = serverUrl + ":" + port + "/weather";

        StringBuilder response = new StringBuilder();

        URL url = new URL(finalUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

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

        try {
            String jsonResponse = sendGetRequest(serverUrl, port, stationId);

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            for (String key : jsonObject.keySet()) {
                System.out.println(key + ": " + jsonObject.get(key).getAsString());
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}
