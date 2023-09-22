package weatherServer;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import weatherServer.utils.HttpUtils;
import weatherServer.utils.JsonUtils;
import weatherServer.utils.LamportClock;
import weatherServer.utils.WeatherDataFileManager;
import weatherServer.models.WeatherData;

public class ContentServer {

    private static LamportClock lamportClock = new LamportClock();

    /**
     * Main entry point for the ContentServer application.
     * 
     * @param args Command line arguments, expecting <server:port> and <file_path>.
     */
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
            sendPUTRequest(serverUrl, jsonPayload, lamportClock);
        } else {
            System.err.println("Failed to read or convert the file to JSON");
        }
    }

    /**
     * Send a PUT request to the given server URL.
     * 
     * @param serverUrl    The server URL to send the PUT request to.
     * @param jsonPayload  The JSON payload to include in the PUT request.
     * @param lamportClock The Servers LamportClock.
     * @returns lamportClock: The updated LamportClock.
     */
    public static LamportClock sendPUTRequest(String serverUrl, String jsonPayload, LamportClock lamportClock) {
        HttpURLConnection conn = null;
        try {
            lamportClock.tick();

            // Initialising the HttpURLConnection
            URL url = new URL(serverUrl + "/weather.json");
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Lamport-Clock", Long.toString(lamportClock.getTime()));

            // Send payload
            try (OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream())) {
                if (jsonPayload != null) {
                    out.write(jsonPayload);
                } else {
                    System.out.println("jsonPayload is null. Exiting");
                    return lamportClock;
                }
            }

            // Check HTTP Response Code
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 201) {
                lamportClock = HttpUtils.displayPostRequestResponse(conn, lamportClock);
            } else {
                System.out.println("PUT request failed: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return lamportClock;
    }
}
