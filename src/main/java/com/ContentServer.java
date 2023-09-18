package com;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import com.models.WeatherData;

public class ContentServer {

    private int port;
    private String fileLocation;

    public ContentServer(int port, String fileLocation) {
        this.port = port;
        this.fileLocation = fileLocation;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ContentServer running on " + port + "...");
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("New client connected");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Received: " + line);
                        // Handle the client's request here
                    }
                } catch (IOException e) {
                    System.err.println("Error with client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
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
        server.start();
    }
}
