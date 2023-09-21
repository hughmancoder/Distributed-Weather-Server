import java.net.HttpURLConnection;
import java.net.URL;

import com.utils.LamportClock;

public class GETClient {
    public static void main(String[] args) {
        LamportClock lamportClock = new LamportClock();

        // Read command-line arguments for server name and port number
        String serverName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String stationId = args.length > 2 ? args[2] : null;

        String urlString = "http://" + serverName + ":" + portNumber;
        if (stationId != null) {
            urlString += "?station=" + stationId;
        }

        while (true) {
            try {
                lamportClock.tick();

                // Create URL and open connection
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // Set request method and headers
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Lamport-Clock", String.valueOf(lamportClock.getTime()));

                // Get Response and Lamport Clock synchronization
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    String lamportHeader = conn.getHeaderField("X-Lamport-Clock");
                    if (lamportHeader != null) {
                        lamportClock.sync(Long.parseLong(lamportHeader));
                    }

                    //
                    JSONObject responseJSON = getJSONResponse(conn);

                    // Display data
                    for (String key : responseJSON.keySet()) {
                        System.out.println(key + ": " + responseJSON.get(key));
                    }
                } else {
                    // Handle errors
                }

                // Add failure-tolerance logic (retries, backoff, etc.)
            } catch (Exception e) {
                // Log the exception and consider retrying
            }
        }
    }
}
