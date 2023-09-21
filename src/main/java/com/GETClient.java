package com;

import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStream;

import com.google.gson.JsonObject;

import com.utils.LamportClock;
import com.utils.HttpUtils;
import com.utils.JsonUtils;

public class GETClient {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java GETClient <server-name> <port-number> [station-id]");
            return;
        }

        LamportClock lamportClock = new LamportClock();
        String serverName = args[0];
        int portNumber;
        try {
            portNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number.");
            return;
        }
        String stationId = args.length > 2 ? args[2] : null;
        String urlString = HttpUtils.buildUrlString(serverName, portNumber, stationId);
        System.out.println("urlString: " + urlString);

        HttpURLConnection conn = null;
        try {
            lamportClock.tick();
            conn = HttpUtils.createConnection(urlString, lamportClock);
            handleResponse(conn, lamportClock);
        } catch (IOException e) {
            System.err.println("An IO error occurred: " + e.getMessage());
            if ("Too many open files".equals(e.getMessage())) {

            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Add a delay of 1 second before next attempt
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Handle InterruptedException
            Thread.currentThread().interrupt();

        }

    }

    private static void handleResponse(HttpURLConnection conn, LamportClock lamportClock) throws IOException {
        InputStream inputStream = null;
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                inputStream = conn.getInputStream();
                lamportClock.syncFromHttpResponse(conn);

                JsonObject responseJSON = JsonUtils.getJSONResponse(conn);

                if (responseJSON == null || responseJSON.size() == 0) {
                    System.out.println("No weather data available");

                } else {
                    JsonUtils.printJson(responseJSON, "");
                }

            } else {
                System.out.println("Received response code: " + responseCode);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

}
