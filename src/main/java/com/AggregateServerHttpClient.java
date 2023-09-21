package com;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.utils.LamportClock;

public class AggregateServerHttpClient {
    private Socket clientSocket;
    private LamportClock lamportClock;

    public AggregateServerHttpClient(Socket clientSocket, LamportClock lamportClock) {
        this.clientSocket = clientSocket;
        this.lamportClock = lamportClock;
    }

    public void handle() {
        lamportClock.tick();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = in.readLine();
            String[] requestParts = requestLine.split(" ");

            if (requestParts.length < 3) {
                out.println("HTTP/1.1 400 Bad Request");
                return;
            }

            String method = requestParts[0];
            URI uri = URI.create(requestParts[1]);
            Map<String, String> queryParameters = parseQueryParameters(uri.getQuery());

            System.out.println("request line" + requestLine);
            // String incomingClockStr = ...;
            // lamportClock.sync(Long.parseLong(incomingClockStr));
            handleWeatherRequest(method, in, out, queryParameters);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleWeatherRequest(String method, BufferedReader in, PrintWriter out,
            Map<String, String> queryParameters) {
        switch (method) {
            case "GET":
                String stationId = queryParameters.get("station");
                System.out.println("Handling GET request for " + stationId);
                AggregationGETRequest(out, stationId);
                break;
            case "PUT":
                System.out.println("Handling PUT request");
                StringBuilder payload = new StringBuilder();
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        payload.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    out.println("HTTP/1.1 500 Internal Server Error");
                    return;
                }
                AggregationPUTRequest(out, payload.toString());
                break;
            default:
                out.println("HTTP/1.1 405 Method Not Allowed");
        }
    }

    private void AggregationGETRequest(PrintWriter out, String stationId) {
        lamportClock.tick();
        // Replace with your actual logic
        String jsonResponse = "{response}"; // Placeholder
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("X-Lamport-Clock: " + lamportClock.getTime());
        out.println();
        out.println(jsonResponse);
    }

    private void AggregationPUTRequest(PrintWriter out, String jsonPayload) {
        lamportClock.tick();
        // Implement your logic to update weather data using jsonPayload

        out.println("HTTP/1.1 200 OK");
        out.println("X-Lamport-Clock: " + lamportClock.getTime());
        out.println();
    }

    public static Map<String, String> parseQueryParameters(String query) throws UnsupportedEncodingException {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(URLDecoder.decode(entry[0], "UTF-8"), URLDecoder.decode(entry[1], "UTF-8"));
                }
            }
        }
        return result;
    }
}
