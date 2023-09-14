package com;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import com.utility.LamportClock;

public class AggregationServer {
    private static Map<String, WeatherData> weatherDataMap = new HashMap<>();
    private static LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 4567;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    String request = in.readLine();
                    // parse request and update Lamport clock
                    // handle GET and PUT accordingly
                    // read & write from/to persistent storage

                    out.println("HTTP/1.1 200 OK");
                }
            }
        }
    }

    public static void loadDataFromFile() {
        // File I/O to populate weatherDataMap
    }

    public static void saveDataToFile() {
        // File I/O to save weatherDataMap
    }
}
