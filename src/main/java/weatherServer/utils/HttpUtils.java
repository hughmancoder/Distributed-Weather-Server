package weatherServer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;

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

    public static String buildGetRequestUrl(String serverName, int portNumber, String stationId) {
        StringBuilder urlString = new StringBuilder(serverName)
                .append(":")
                .append(portNumber)
                .append("/weather");
        if (stationId != null) {
            urlString.append("?station=").append(stationId);
        }
        return urlString.toString();
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

    public static LamportClock displayGetRequestResponse(HttpURLConnection conn, LamportClock lamportClock)
            throws IOException {
        InputStream inputStream = null;
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                inputStream = conn.getInputStream();
                lamportClock.syncFromHttpResponse(conn);
                JsonObject responseJSON = JsonUtils.getJSONResponse(conn);

                if (responseJSON == null || responseJSON.size() == 0) {
                    System.out.println("No weather data available");
                } else {
                    JsonUtils.printJson(responseJSON, "");
                }
            } else {
                System.out.println("GET request response: " + responseCode);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return lamportClock;
    }

    public static LamportClock displayPostRequestResponse(HttpURLConnection conn, LamportClock lamportClock) {
        try {
            lamportClock.syncFromHttpResponse(conn);
            int responseCode = conn.getResponseCode();
            System.out.println("PUT request response: " + responseCode);
            return lamportClock;
        } catch (IOException e) {
            System.out.println("PUT reponse error: " + e.getMessage());
        }
        return lamportClock;
    }
}