package com;

import com.models.QueryData;
import com.models.WeatherData;
import com.utility.JsonUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class ContentServer {
    private static String aggregate_server_url = "http://localhost:4567/weather";
    private String port;
    private String fileLocation;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;

    public ContentServer(String port, String fileLocation) {
        this.port = port;
        this.fileLocation = fileLocation;
    }

    public void start() {
        isRunning = true;
        uploadWeatherDataToAggregateServer(aggregate_server_url, fileLocation);
        try {
            serverSocket = new ServerSocket(Integer.parseInt(port));
            System.out.println("ContentServer running on port " + port + "...");
            listenForClients();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    private void listenForClients() {
        String lastProcessedRoute = "";

        while (isRunning) {
            try (Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                String receivedData = reader.readLine();
                System.out.println("Received from client: " + receivedData + " Client IP: "
                        + socket.getInetAddress().getHostAddress());

                URI requestURI;
                String route = "";
                Map<String, String> queryParameters = null;

                if (receivedData != null && receivedData.split(" ").length > 1) {
                    route = receivedData.split(" ")[1];

                    // only process request if route changes
                    if (!route.equals(lastProcessedRoute)) {
                        lastProcessedRoute = route;

                        try {
                            requestURI = new URI(route);
                            queryParameters = QueryData.parseQueryParameters(requestURI.getQuery());

                            String filePath = queryParameters.get("filePath");
                            uploadWeatherDataToAggregateServer(aggregate_server_url, filePath);

                        } catch (URISyntaxException e) {
                            System.err.println("Invalid URI syntax: " + e.getMessage());
                        }
                    }
                }

            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error with client connection: " + e.getMessage());
                }
            }
        }
    }

    private void uploadWeatherDataToAggregateServer(String aggregateServerUrl, String fileLocation) {
        WeatherData weatherData = WeatherData.readFileAndParse(fileLocation);
        if (weatherData == null) {
            return;
        }

        String json = JsonUtils.toJson(weatherData);
        PUTRequest(json, aggregateServerUrl);
    }

    public static void PUTRequest(String json, String aggregateServerUrl) {
        try {
            URL url = new URL(aggregateServerUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setDoOutput(true);

            try (DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
                writer.writeBytes(json);
                writer.flush();
            }

            int responseCode = con.getResponseCode();
            System.out.println("Sent PUT request to aggregate server. Response Code: " + responseCode);
        } catch (IOException e) {
            System.err.println("Failed to upload data to aggregate server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <port> <filePath>");
            return;
        }

        String port = args[0];
        String fileLocation = args[1];
        if (args.length >= 3) {
            aggregate_server_url = args[2];
        }
        ContentServer server = new ContentServer(port, fileLocation);
        server.start();

        // For demonstration purposes: stop the server after 60 seconds
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // Handle interruption
        }
        server.stop();
    }
}
