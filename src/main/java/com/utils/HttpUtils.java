package com.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpUtils {
    public static final String LAMPORT_CLOCK_HEADER = "X-Lamport-Clock";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";

    public long lamportClock;
    public int contentLength;
    public String requestBody;

    public static HttpUtils readRequest(BufferedReader in) throws IOException {
        HttpUtils requestData = new HttpUtils();
        readHeaders(in, requestData);
        readBody(in, requestData);
        return requestData;
    }

    private static void readHeaders(BufferedReader in, HttpUtils requestData) throws IOException {
        String header;
        while (!(header = in.readLine()).isEmpty()) {
            if (header.startsWith(LAMPORT_CLOCK_HEADER)) {
                requestData.lamportClock = Long.parseLong(header.substring((LAMPORT_CLOCK_HEADER + ": ").length()));
            }
            if (header.startsWith(CONTENT_LENGTH_HEADER)) {
                requestData.contentLength = Integer.parseInt(header.substring((CONTENT_LENGTH_HEADER + ": ").length()));
            }
        }
    }

    private static void readBody(BufferedReader in, HttpUtils requestData) throws IOException {
        if (requestData.contentLength > 0) {
            char[] buffer = new char[requestData.contentLength];
            int bytesRead = in.read(buffer, 0, requestData.contentLength);
            while (bytesRead < requestData.contentLength) {
                bytesRead += in.read(buffer, bytesRead, requestData.contentLength - bytesRead);
            }
            requestData.requestBody = new String(buffer);
        }
    }

    public static HttpURLConnection createConnection(String urlString, LamportClock lamportClock) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty(LAMPORT_CLOCK_HEADER, String.valueOf(lamportClock.getTime()));
        return conn;
    }

    public static String buildUrlString(String serverName, int portNumber, String stationId) {
        StringBuilder urlString = new StringBuilder(serverName)
                .append(":")
                .append(portNumber)
                .append("/weather");
        if (stationId != null) {
            urlString.append("?station=").append(stationId);
        }
        return urlString.toString();
    }

    public static JsonObject getJSONResponse(HttpURLConnection conn) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            // Reading the JSON response, then close the BufferedReader
            return JsonParser.parseReader(br).getAsJsonObject();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
