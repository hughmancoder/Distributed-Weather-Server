package com;

import java.net.HttpURLConnection;
import java.io.IOException;

import com.utils.LamportClock;
import com.utils.HttpUtils;

public class GETClient {

    /**
     * Entry point for the GETClient application.
     * 
     * @param args Command-line arguments, expecting <server-name>, <port-number>,
     *             and optionally <station-id>.
     */
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
        getRequest(lamportClock, serverName, portNumber, stationId);
    }

    /**
     * Executes a GET request to the given server and port, optionally filtering by
     * station ID.
     * 
     * @param lamportClock LamportClock instance for logical time tracking.
     * @param serverName   Name of the server to connect to.
     * @param portNumber   Port number for the server connection.
     * @param stationId    Optional station ID to filter data.
     */
    public static void getRequest(LamportClock lamportClock, String serverName, int portNumber, String stationId) {
        String urlString = HttpUtils.buildGetRequestUrl(serverName, portNumber, stationId);

        HttpURLConnection conn = null;
        try {
            lamportClock.tick();
            conn = HttpUtils.createConnection(urlString, lamportClock);
            lamportClock = HttpUtils.displayGetRequestResponse(conn, lamportClock);
        } catch (IOException e) {
            System.err.println("An IO error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
