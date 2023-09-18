package com;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import com.models.WeatherData;
import com.utility.JsonUtils;

public class ContentServer {

    private int port;
    private String fileLocation;

    public ContentServer(int port, String fileLocation) {
        this.port = port;
        this.fileLocation = fileLocation;
    }

    public void uploadWeatherDataToAggregateServer(String aggregateServerUrl) {
        WeatherData weatherData = readFileAndParse();
        if (weatherData != null) {
            String json = JsonUtils.toJson(weatherData);
            try {
                PUTRequest(json, aggregateServerUrl);
            } catch (IOException e) {
                System.err.println("Failed to upload data to aggregate server: " + e.getMessage());
            }
        }
    }

    public void PUTRequest(String json, String aggregateServerUrl) throws IOException {
        URL url = new URL(aggregateServerUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // Set up the connection for a PUT request
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setDoOutput(true);

        // Write JSON payload
        try (DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
            writer.writeBytes(json);
            writer.flush();
        }

        int responseCode = con.getResponseCode();
        System.out.println("Sent PUT request to aggregate server. Response Code: " + responseCode);
    }

    public void start(String aggregateServerUrl) {
        uploadWeatherDataToAggregateServer(aggregateServerUrl);

        // Initialise the ServerSocket to listen for incoming connections
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ContentServer running on port " + port + "...");

            // Continuously listen for new client connections
            while (true) {
                try (
                        // Accept a new client connection
                        Socket socket = serverSocket.accept();

                        // Initialise BufferedReader to read incoming messages from the client
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        // Write outgoing messages to the client
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                    System.out.println("New client connected");

                    // Receive data from the connected client
                    String receivedData = reader.readLine();
                    System.out.println("Received from client: " + receivedData);
                    // TODO: send put request to aggregate server
                } catch (IOException e) {
                    System.err.println("Error with client connection: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            // Handle exceptions that occur during ServerSocket initialisation or operation
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public WeatherData readFileAndParse() {
        HashMap<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileLocation))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    map.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("File reading error: " + e.getMessage());
            return null;
        }

        // Use the map to construct a WeatherData object
        WeatherData weatherData = new WeatherData(
                map.get("id"),
                map.get("name"),
                map.get("state"),
                map.get("time_zone"),
                Double.parseDouble(map.get("lat")),
                Double.parseDouble(map.get("lon")),
                map.get("local_date_time"),
                map.get("local_date_time_full"),
                Double.parseDouble(map.get("air_temp")),
                Double.parseDouble(map.get("apparent_t")),
                map.get("cloud"),
                Double.parseDouble(map.get("dewpt")),
                Double.parseDouble(map.get("press")),
                Integer.parseInt(map.get("rel_hum")),
                map.get("wind_dir"),
                Integer.parseInt(map.get("wind_spd_kmh")),
                Integer.parseInt(map.get("wind_spd_kt")));

        return weatherData;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <port> <fileLocation>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String fileLocation = args[1];

        ContentServer server = new ContentServer(port, fileLocation);
        String aggregateServerUrl = (args.length >= 3) ? args[2] : "http://localhost:4567/weather";
        server.start(aggregateServerUrl);
    }
}
