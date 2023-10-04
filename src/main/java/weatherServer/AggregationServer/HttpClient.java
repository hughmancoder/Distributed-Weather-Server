package weatherServer.AggregationServer;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import weatherServer.models.WeatherData;
import weatherServer.utils.JsonUtils;
import weatherServer.utils.LamportClock;
import weatherServer.utils.WeatherDataFileManager;
import weatherServer.utils.HttpUtils;

public class HttpClient {
    private Socket clientSocket;
    private LamportClock lamportClock;
    private boolean firstUpload = true;

    /**
     * Constructor for the HttpClient.
     * 
     * @param clientSocket The socket for client-server communication.
     * @param lamportClock The instance of the LamportClock for maintaining logical
     *                     time.
     */
    public HttpClient(Socket clientSocket, LamportClock lamportClock) {
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
            Map<String, String> queryParameters = HttpUtils.parseQueryParameters(uri.getQuery());

            HttpUtils requestData = HttpUtils.readRequest(in);
            lamportClock.sync(requestData.lamportClock);
            // Proceed to handle the request based on the method
            handleWeatherRequest(method, in, out, queryParameters, requestData.requestBody);

        } catch (Exception e) {
            System.out.println("Exception in handle" + e.getMessage());

        }
    }

    /**
     * Determines the type of the request (GET or PUT) and processes it accordingly.
     * 
     * @param method          HTTP method type (GET or PUT).
     * @param in              Input reader.
     * @param out             Output writer.
     * @param queryParameters The parameters for the HTTP query.
     * @param requestBody     The body of the HTTP request.
     */
    private void handleWeatherRequest(String method, BufferedReader in, PrintWriter out,
            Map<String, String> queryParameters, String requestBody) {
        switch (method) {
            case "GET":
                String stationId = queryParameters.get("station");
                AggregationGETRequest(out, stationId);
                break;
            case "PUT":
                AggregationPUTRequest(out, requestBody);
                break;
            default:
                out.println("HTTP/1.1 405 Method Not Allowed");
        }
    }

    /**
     * Handles an aggregation GET request.
     * Retrieves the weather data either for a specific station or for all stations,
     * then sends it as a response.
     * 
     * @param out       Output writer.
     * @param stationId The ID of the weather station (can be null).
     */
    private void AggregationGETRequest(PrintWriter out, String stationId) {
        lamportClock.tick();
        String jsonResponse = stationId == null ? AggregationServer.getAllWeatherData()
                : AggregationServer.getWeatherByStation(stationId);

        if (jsonResponse == null) {
            out.println("HTTP/1.1 404 Not Found");
            return;
        }

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("X-Lamport-Clock: " + lamportClock.getTime());
        out.println();
        out.println(jsonResponse);
    }

    /**
     * Handles an aggregation PUT request.
     * Updates the weather data map and storage file with the new weather data
     * provided in the request.
     * 
     * @param out         Output writer.
     * @param jsonPayload The payload in JSON format containing the new weather
     *                    data.
     */
    private void AggregationPUTRequest(PrintWriter out, String jsonPayload) {
        lamportClock.tick();

        if (jsonPayload == null || jsonPayload.isEmpty()) {
            out.println("HTTP/1.1 204 No Content");
            return;
        }

        WeatherData wd;
        try {
            wd = JsonUtils.fromJson(jsonPayload);
        } catch (Exception e) {
            out.println("HTTP/1.1 500 Internal Server Error");
            return;
        }

        if (wd == null) {
            out.println("HTTP/1.1 400 Bad Request");
            return;
        }

        AggregationServer.putToWeatherDataMap(wd);
        HashMap<String, WeatherData> weatherDataMap = AggregationServer.getWeatherDataMap();
        try {
            WeatherDataFileManager.writeFile(AggregationServer.DATA_STORAGE_PATH, weatherDataMap);
        } catch (IOException e) {
            System.out.println("Erorr writing to file" + e.getMessage());
        }

        if (firstUpload) {
            out.println("HTTP/1.1 201 Created");
            firstUpload = false;
        } else {
            out.println("HTTP/1.1 200 OK");
        }

        out.println("X-Lamport-Clock: " + lamportClock.getTime());
        out.println();
    }
}
