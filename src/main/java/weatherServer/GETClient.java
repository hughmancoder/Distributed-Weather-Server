package weatherServer;

import java.net.HttpURLConnection;
import java.io.IOException;

import weatherServer.utils.LamportClock;
import weatherServer.utils.HttpUtils;

public class GETClient {

    private static final int MAX_RETRIES = 10;

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
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            HttpURLConnection conn = null;
            try {
                lamportClock.tick();
                conn = HttpUtils.createConnection(urlString, lamportClock);
                lamportClock = HttpUtils.displayGetRequestResponse(conn, lamportClock);
                break; // Exit the loop if the request was successful
            } catch (IOException e) {
                System.err.println("Attempt " + (attempt + 1) + ": An IO error occurred: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Attempt " + (attempt + 1) + ": An exception occurred: " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            attempt++;

            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(2000); // Wait for 2 seconds before the next attempt
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        if (attempt == MAX_RETRIES) {
            System.out.println("Maximum attempts reached. GET request failed after " + MAX_RETRIES + " tries.");
        }
    }
}
