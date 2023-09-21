package com;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.utils.HttpUtils;
import com.utils.JsonUtils;
import com.utils.LamportClock;
import com.utils.WeatherDataFileManager;
import com.models.WeatherData;

public class ContentServer {

    private static LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: java YourContentServer <server:port> <file_path>");
            return;
        }

        String serverUrl = args[0];
        String filePath = args[1];

        WeatherData wd = WeatherDataFileManager.readFileAndParse(filePath);
        String jsonPayload = JsonUtils.toJson(wd);

        if (jsonPayload != null) {
            sendPUTRequest(serverUrl, jsonPayload);
        } else {
            System.err.println("Failed to read or convert the file to JSON");
        }
    }

    public static void sendPUTRequest(String serverUrl, String jsonPayload) {
        HttpURLConnection conn = null;
        try {
            lamportClock.tick();

            // Initialising the HttpURLConnection
            URL url = new URL(serverUrl + "/weather.json");
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Lamport-Clock", Long.toString(lamportClock.getTime()));

            // Send payload
            try (OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream())) {
                if (jsonPayload != null) {
                    out.write(jsonPayload);
                } else {
                    System.out.println("jsonPayload is null. Exiting");
                    return;
                }
            }

            // Check HTTP Response Code
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 201) {
                lamportClock = HttpUtils.displayPostRequestResponse(conn, lamportClock);
            } else {
                System.out.println("PUT request failed: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
