// package com;

// import java.io.BufferedReader;
// import java.io.InputStreamReader;
// import java.net.HttpURLConnection;
// import java.net.URL;
// import com.utils.LamportClock;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;

// public class GETClient {

// private static LamportClock lamportClock = new LamportClock();

// public static void main(String[] args) {
// String serverUrl = args[0];
// String optionalStationId = args.length > 1 ? args[1] : null;

// try {
// lamportClock.tick();
// String jsonResponse = sendGETRequest(serverUrl, optionalStationId);
// JsonObject jsonObject =
// JsonParser.parseString(jsonResponse).getAsJsonObject();

// for (String key : jsonObject.keySet()) {
// System.out.println(key + ": " + jsonObject.get(key).getAsString());
// }

// } catch (Exception e) {
// e.printStackTrace();
// }
// }

// private static String sendGETRequest(String serverUrl, String
// optionalStationId) throws IOException {
// String fullUrl = serverUrl + "/weather";
// if (optionalStationId != null) {
// fullUrl += "?station=" + optionalStationId;
// }

// URL url = new URL(fullUrl);
// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
// conn.setRequestMethod("GET");
// conn.setRequestProperty("X-Lamport-Clock",
// Integer.toString(lamportClock.getTime()));

// BufferedReader in = new BufferedReader(new
// InputStreamReader(conn.getInputStream()));
// String inputLine;
// StringBuilder response = new StringBuilder();

// while ((inputLine = in.readLine()) != null) {
// response.append(inputLine);
// }
// in.close();

// return response.toString();
// }
// }
