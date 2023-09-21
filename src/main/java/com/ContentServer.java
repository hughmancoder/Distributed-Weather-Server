package com;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private static void sendPUTRequest(String serverUrl, String jsonPayload) {
        try {
            lamportClock.tick();
            URL url = new URL(serverUrl + "/weather.json");
            System.out.println("url: " + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Lamport-Clock", Long.toString(lamportClock.getTime()));

            // Send payload
            try (OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream())) {
                out.write(jsonPayload);
            }

            // TODO: add to class
            // Get the Lamport Clock value from the server's response and syncronise
            String serverClockStr = conn.getHeaderField("X-Lamport-Clock");

            if (serverClockStr != null) {
                long serverClock = Long.parseLong(serverClockStr);
                lamportClock.sync(serverClock);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
